package challenge3.camel;

import externalLegacyCodeNotUnderOurControl.TemperatureValueSource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author Dmytro Rud
 */
@Slf4j
public class Challenge3RouteBuilder extends RouteBuilder implements TemperatureValueSource.TemperatureListener {

    private final Queue<Integer> queue = new ArrayDeque<>();    // synchronized manually

    private int min = Integer.MAX_VALUE;
    private int max = Integer.MIN_VALUE;

    // to be injected by Spring
    @Getter @Setter private TemperatureValueSource temperatureValueSource;

    @Override
    public void configure() throws Exception {
        temperatureValueSource.addListener(this);

        from("quartz2:observer?trigger.repeatInterval=10000&trigger.repeatCount=-1")
                .process(exchange -> {
                    Integer d;
                    while ((d = pollQueue()) != null) {
                        if (d < min) {
                            min = d;
                        }
                        if (d > max) {
                            max = d;
                        }
                    }
                    log.debug("min = {}, max = {}", min, max);
                });
    }

    private Integer pollQueue() {
        synchronized (queue) {
            return queue.poll();
        }
    }

    @Override
    public void onNext(int temperature) {
        synchronized (queue) {
            queue.offer(temperature);
        }
        log.debug("Received temperature: {}", temperature);
    }

}
