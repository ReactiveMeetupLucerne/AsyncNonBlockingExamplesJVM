package challenge1.java8;

import externalLegacyCodeNotUnderOurControl.PriceService;

import java.util.HashSet;
import java.util.OptionalDouble;
import java.util.Set;

import static externalLegacyCodeNotUnderOurControl.PrintlnWithThreadname.println;

/**
 * This example uses parallel streams which are available since Java 8.
 *
 * @author Marcus Fihlon, www.fihlon.ch
 */
public class Java8StreamsExample {

    private static final int NUMBER_OF_SERVICE_CALLS = 10;

    private final Set<PriceService> services;

    public static void main(final String... args) {
        new Java8StreamsExample().run();
    }

    /**
     * Create the price services.
     */
    private Java8StreamsExample() {
        this.services = new HashSet<>();
        for (int i = 0; i < NUMBER_OF_SERVICE_CALLS; i++) {
            this.services.add(new PriceService());
        }
    }

    /**
     * Use parallel streams to calculate the average price of all price services.
     */
    private void run() {
        final OptionalDouble average = this.services.parallelStream()
                .mapToDouble(PriceService::getPrice) // this call is executed in parallel
                .average(); // this call blocks because it needs all answers from the price services to calculate

        println("The average price is: " + average.orElseGet(null));
    }

}
