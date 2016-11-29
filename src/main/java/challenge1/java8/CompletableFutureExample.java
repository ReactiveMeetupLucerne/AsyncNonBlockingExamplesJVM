package challenge1.java8;

import externalLegacyCodeNotUnderOurControl.PriceService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static externalLegacyCodeNotUnderOurControl.PrintlnWithThreadname.println;
import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * This example uses CompletableFuture which is available since Java 8.
 *
 * @author Marcus Fihlon, www.fihlon.ch
 */
public class CompletableFutureExample {

    private static final int NUMBER_OF_SERVICE_CALLS = 10;

    private final ExecutorService pool;
    private int price;
    private int count;

    public static void main(final String... args) {
        new CompletableFutureExample().run();
    }

    private CompletableFutureExample() {
        this.pool = Executors.newFixedThreadPool(3);
        this.price = 0;
        this.count = 0;
    }

    /**
     * Create the price services asynchronously.
     */
    private void run() {
        for (int i = 0; i < NUMBER_OF_SERVICE_CALLS; i++) {
            supplyAsync(() -> new PriceService().getPrice(), this.pool)
                    .thenAcceptAsync(this::collector);
        }
    }

    /**
     * Collect the answers and calculate the average price.
     */
    private synchronized void collector(final int price) {
        this.price += price;
        if (++this.count == NUMBER_OF_SERVICE_CALLS) {
            println("The average price is: " + this.price / this.count);
            System.exit(0);
        }
    }

}
