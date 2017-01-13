package challenge4.camel;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Dmytro Rud
 */
@Slf4j
public class Challenge4RouteBuilder extends RouteBuilder {

    private double average;
    private long lastTimestamp = -1;
    private AtomicBoolean isCalculating = new AtomicBoolean(false);

    @Override
    public void configure() throws Exception {
        from("direct:get-price-1")
                .delay(300_000)
                .setBody().constant(Math.random() * 10.0);

        from("direct:get-price-2")
                .delay(5_000)
                .setBody().constant(Math.random() * 15.0);

        from("direct:get-price-3")
                .delay(8_000)
                .setBody().constant(Math.random() * 20.0);

        from("direct:get-price-4")
                .delay(100_000)
                .setBody().constant(Math.random() * 25.0);


        from("quartz2:x?trigger.repeatInterval=7000&trigger.repeatCount=-1")
                .to("seda:start-4");


        from("seda:start-4")
                .process(exchange -> {
                    if (this.isCalculating.get()) {
                        log.debug("Waiting...");
                        while (this.isCalculating.get());
                    }
                })
                .choice()
                    .when(exchange -> (System.currentTimeMillis() - this.lastTimestamp > 5))
                        .process(exchange -> {
                            log.debug("Last calculation too old, re-calculate");
                        })
                        .to("direct:start-2")
                    .otherwise()
                        .process(exchange -> {
                            log.debug("Reuse existing average: {}", this.average);
                        });


        from("direct:start-2")
                .process(exchange -> {
                    this.isCalculating.set(true);
                })
                .to("seda:multicast")
                .process(exchange -> log.debug("Caller thread continues processing"));

        final String[] serverUris = {
                "direct:get-price-1",
                "direct:get-price-2",
                "direct:get-price-3",
                "direct:get-price-4",
        };

        from("seda:multicast")
                .multicast()
                    .to(serverUris)
                    .parallelProcessing()
                    .streaming()
                    .timeout(15_000)

                    .aggregationStrategy((oldExchange, newExchange) -> {
                        double newPrice = newExchange.getIn().getBody(double.class);
                        log.debug("Arrived price: {}", newPrice);
                        if (oldExchange == null) {
                            newExchange.setProperty("meetup.received.count", 1);
                        } else {
                            double oldSum = oldExchange.getIn().getBody(double.class);
                            newExchange.getIn().setBody(oldSum + newPrice);
                            newExchange.setProperty("meetup.received.count", oldExchange.getProperty("meetup.received.count", int.class) + 1);
                        }
                        return newExchange;
                    })
                    .end()

                .process(exchange -> {
                    int expectedCount = serverUris.length;
                    int receivedCount = exchange.getProperty("meetup.received.count", 0, int.class);
                    double sum = (receivedCount == 0) ? 0.00 : exchange.getIn().getBody(double.class);
                    log.debug("Collected sum: {}", sum);

                    int missingCount = expectedCount - receivedCount;
                    if (missingCount > 0) {
                        log.debug("{} responses are missing, fallback them to 42,-", missingCount);
                        sum += (missingCount * 42.00);
                        log.debug("Corrected sum: {}", sum);
                    }

                    this.average = sum / serverUris.length;
                    this.lastTimestamp = System.currentTimeMillis();
                    log.debug("Fresh average: {}", this.average);

                    this.isCalculating.set(false);
                });

    }

}
