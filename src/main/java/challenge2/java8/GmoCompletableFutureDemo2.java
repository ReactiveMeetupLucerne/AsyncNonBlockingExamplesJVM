package challenge2.java8;

import externalLegacyCodeNotUnderOurControl.PriceService;

import java.util.concurrent.*;
import java.util.function.Supplier;

import static externalLegacyCodeNotUnderOurControl.PrintlnWithThreadname.println;

public class GmoCompletableFutureDemo2 {

    private static final ScheduledExecutorService schedulerExecutor = Executors.newScheduledThreadPool(1); //corePoolSize - the number of threads to keep in the pool, even if they are idle size = 1
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static void main(String[] args) throws InterruptedException {
        Supplier<Integer> priceSupplier = () -> new PriceService(5).getPrice();
        int fallbackPrice = 42;

        supplyAsyncWithTimeout(
                priceSupplier,
                2, TimeUnit.SECONDS,
                fallbackPrice
        ).thenAccept(price -> println("Got price: " + price));

        println("I wasn't blocked");
        TimeUnit.SECONDS.sleep(10);

        println("Shutting down thread pools");
        executorService.shutdown();
        schedulerExecutor.shutdown();
    }

    //Inspired by http://stackoverflow.com/questions/23575067/timeout-with-default-value-in-java-8-completablefuture/24457111#24457111
    //and https://github.com/ReactiveMeetupLucerne/AsyncNonBlockingExamplesJVM/issues/8
    public static <T> CompletableFuture<T> supplyAsyncWithTimeout(final Supplier<T> supplier, long timeoutValue, TimeUnit timeUnit, T defaultValue) {
        final CompletableFuture<T> cf = new CompletableFuture<>();

        Future<?> future = executorService.submit(() -> {
            try {
                cf.complete(supplier.get());
            } catch (Throwable ex) {
                cf.completeExceptionally(ex);
            }
        });

        //schedule watcher (for timeout)
        schedulerExecutor.schedule(() -> {
            if (!cf.isDone()) {
                println("Fallback to default value due timeout: " + defaultValue);
                cf.complete(defaultValue);
                future.cancel(true);
            }
        }, timeoutValue, timeUnit);

        return cf;
    }


}
