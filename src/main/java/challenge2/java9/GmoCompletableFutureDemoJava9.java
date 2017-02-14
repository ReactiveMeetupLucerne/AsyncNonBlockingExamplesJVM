package challenge2.java9;

import externalLegacyCodeNotUnderOurControl.PriceService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class GmoCompletableFutureDemoJava9 {
    /***** JAVA9 Code disabled, in order not to break build.


    private PriceService service = new PriceService(5);

    private void run() {
        ExecutorService executor = Executors.newFixedThreadPool(1);

        Supplier<Integer> sup_getprice = () -> service.getPrice();


        CompletableFuture.supplyAsync(sup_getprice, executor)
                .completeOnTimeout(42, 2, TimeUnit.SECONDS)
                .thenAccept(amount -> {
                    System.out.println("Price with timeout is: " + amount);
                    // TODO: do not shutdown whole executor, but only the  CompletableFuture
                    // => Is there a better solution?
                    executor.shutdownNow(); //send interrupt to PriceService, if it is still running
                });

        try {
            System.out.println("Main thread waiting for completion.");
            Thread.sleep(10 * 1000);
            System.out.println("Main thread done.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        new GmoCompletableFutureDemoJava9().run();
    }

     */
}
