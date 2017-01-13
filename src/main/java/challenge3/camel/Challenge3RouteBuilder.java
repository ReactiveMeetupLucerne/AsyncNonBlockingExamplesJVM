package challenge3.camel;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author Dmytro Rud
 */
@Slf4j
public class Challenge3RouteBuilder extends RouteBuilder {

    private final Queue<Double> queue = new ArrayDeque<>();

    private double min = Double.MAX_VALUE;
    private double max = Double.MIN_VALUE;

    @Override
    public void configure() throws Exception {
        from("quartz2:source?trigger.repeatInterval=2000&trigger.repeatCount=-1")
                .process(exchange -> {
                    double value = Math.random() * 100;
                    queue.offer(value);
                    log.debug("Pushed temperature: {}", value);
                });

        from("quartz2:observer?trigger.repeatInterval=10000&trigger.repeatCount=-1")
                .process(exchange -> {
                    Double d;
                    while ((d = queue.poll()) != null) {
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

}
