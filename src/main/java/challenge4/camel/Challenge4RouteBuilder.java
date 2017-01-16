package challenge4.camel;

import challenge2.camel.PriceExpression;
import externalLegacyCodeNotUnderOurControl.PriceService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Dmytro Rud
 */
@Slf4j
public class Challenge4RouteBuilder extends RouteBuilder {

    public static final String RECEIVED_COUNT = "meetup.received.count";

    private volatile double average;
    private volatile long lastTimestamp = -1;
    private AtomicReference<CountDownLatch> latch = new AtomicReference<>(new CountDownLatch(0));

    // to be injected by Spring
    @Getter @Setter private List<PriceService> priceServices;

    @Override
    public void configure() throws Exception {
        final String[] serverUris = new String[priceServices.size()];
        for (int i = 0; i < priceServices.size(); ++i) {
            serverUris[i] = "direct:get-price-" + i;
            from(serverUris[i]).setBody(new PriceExpression(priceServices.get(i)));
        }

        from("quartz2:x?trigger.repeatInterval=7000&trigger.repeatCount=-1")
                .process(exchange -> {
                    log.debug("Start query {}, probably need to wait first", exchange.getExchangeId());
                    this.latch.get().await();
                })
                .choice()
                    .when(exchange -> (System.currentTimeMillis() - this.lastTimestamp > 50_000))
                        .process(exchange -> {
                            log.debug("Last calculation too old, re-calculate");
                        })
                        .to("direct:start")
                    .otherwise()
                        .process(exchange -> {
                            log.debug("Reuse existing average {} for query {}", this.average, exchange.getExchangeId());
                        });


        from("direct:start")
                .process(exchange -> {
                    this.latch.set(new CountDownLatch(1));
                })
                .to("seda:multicast")
                .process(exchange -> log.debug("Caller thread continues processing"));


        from("seda:multicast")
                .multicast()
                    .to(serverUris)
                    .parallelProcessing()
                    .streaming()
                    .timeout(15_000)

                    .aggregationStrategy((oldExchange, newExchange) -> {
                        int newPrice = newExchange.getIn().getBody(int.class);
                        log.debug("Arrived price: {}", newPrice);
                        if (oldExchange == null) {
                            newExchange.setProperty(RECEIVED_COUNT, 1);
                        } else {
                            int oldSum = oldExchange.getIn().getBody(int.class);
                            newExchange.getIn().setBody(oldSum + newPrice);
                            newExchange.setProperty(RECEIVED_COUNT, oldExchange.getProperty(RECEIVED_COUNT, int.class) + 1);
                        }
                        return newExchange;
                    })
                    .end()

                .process(exchange -> {
                    int expectedCount = serverUris.length;
                    int receivedCount = exchange.getProperty(RECEIVED_COUNT, 0, int.class);
                    int sum = (receivedCount == 0) ? 0 : exchange.getIn().getBody(int.class);
                    log.debug("Collected sum: {}", sum);

                    int missingCount = expectedCount - receivedCount;
                    if (missingCount > 0) {
                        sum += (missingCount * 42);
                        log.debug("{} responses are missing, fallback them to 42, corrected sum: {}", missingCount, sum);
                    }

                    this.average = ((double) sum) / serverUris.length;
                    this.lastTimestamp = System.currentTimeMillis();
                    log.debug("Fresh average: {}", this.average);

                    this.latch.get().countDown();
                });

    }

}
