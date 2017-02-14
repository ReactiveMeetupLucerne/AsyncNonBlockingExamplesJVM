package challenge2.java8;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import externalLegacyCodeNotUnderOurControl.PriceService;

public class GmoCompletableFutureDemo {

	//In java 9, there will be an "orTimeout" function,
	//see: http://www.esynergy-solutions.co.uk/blog/asynchronous-timeouts-completable-future-java-8-and-9
	//title "Java 9 improvements"

	private PriceService service = new PriceService(5);
	
	private class SupplierWithTimeout<T>
	implements Supplier<T>
	{
		private Callable<T> callable;
		private final int timeout;
		private final T defaultvalue;

		public SupplierWithTimeout(Callable<T> callable, int timeout, T defaultvalue)
		{
			this.callable = callable;
			this.timeout = timeout;
			this.defaultvalue = defaultvalue;
		}
		
		@Override
		public T get() {
			Lock lvalueavailable = new ReentrantLock();
			Condition cvalueavailable = lvalueavailable.newCondition();
			
			final AtomicReference<T> result = new AtomicReference<T>();
			
			Thread t = new Thread(() -> {
				try {
					result.set(callable.call()); //Eigentlicher call zum Service.
					lvalueavailable.lock();
					cvalueavailable.signal();
				} catch(Exception e) {
					e.printStackTrace();
				} finally {
					lvalueavailable.unlock();
				}
			});
			
			try {
				lvalueavailable.lock();
				t.start();
				if(cvalueavailable.await(timeout, TimeUnit.SECONDS)) {
					//Resultat vor Timeout vorhanden.
					return result.get();
				} else {
					System.out.println("Timeout. Using default.");
					t.interrupt();
				}
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				lvalueavailable.unlock();
			}

			return defaultvalue;
		}
		
	}

	private void run() {
		
		Supplier<Integer> sup_getprice_with_timeout = new SupplierWithTimeout<Integer>(() -> service.getPrice(), 2, 42);

		CompletableFuture.supplyAsync(sup_getprice_with_timeout).thenAccept(amount -> {
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
		new GmoCompletableFutureDemo().run();
	}

}
