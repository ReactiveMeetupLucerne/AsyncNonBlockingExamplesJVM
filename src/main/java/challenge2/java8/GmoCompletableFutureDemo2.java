package challenge2.java8;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import externalLegacyCodeNotUnderOurControl.PriceService;

public class GmoCompletableFutureDemo2 {
	
	//Inspired by http://stackoverflow.com/questions/23575067/timeout-with-default-value-in-java-8-completablefuture/24457111#24457111
	//and https://github.com/ReactiveMeetupLucerne/AsyncNonBlockingExamplesJVM/issues/8

	private static final ScheduledExecutorService schedulerExecutor = Executors.newScheduledThreadPool(1); //corePoolSize - the number of threads to keep in the pool, even if they are idle size = 1
	private static final ExecutorService executorService = Executors.newCachedThreadPool();
	
	private PriceService service = new PriceService(5);
	
	public static <T> CompletableFuture<T> supplyAsyncWithTimeout(final Supplier<T> supplier, long timeoutValue, TimeUnit timeUnit, T defaultValue) {
	    final CompletableFuture<T> cf = new CompletableFuture<T>();

	    Future<?> future = executorService.submit(() -> {
	        try {
	            cf.complete(supplier.get());
	        } catch(Throwable ex) {
	            cf.completeExceptionally(ex);
	        }
	    });

	    //schedule watcher (for timeout)
	    schedulerExecutor.schedule(() -> {
	        if(!cf.isDone()) {
	            cf.complete(defaultValue);
	            future.cancel(true);
	        }
	    }, timeoutValue, timeUnit);

	    return cf;
	}
	
	private void run() {
		
		Supplier<Integer> sup_getprice = () -> service.getPrice();

		supplyAsyncWithTimeout(sup_getprice, 2, TimeUnit.SECONDS, 42).thenAccept(amount -> {
			System.out.println("Price with timeout is: "+amount);
		});
		
		try {
			System.out.println("Main thread waiting for completion.");
			Thread.sleep(10*1000);
			System.out.println("Main thread done.");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new GmoCompletableFutureDemo2().run();
	}

}
