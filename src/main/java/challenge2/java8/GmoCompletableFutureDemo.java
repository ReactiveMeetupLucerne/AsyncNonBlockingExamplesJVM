package challenge2.java8;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import externalLegacyCodeNotUnderOurControl.PriceService;

public class GmoCompletableFutureDemo {

	private PriceService service = new PriceService(5);

	private void run() {
		//In Java 9 wird es eine "orTimeout" Funktion geben,
		//siehe: http://www.esynergy-solutions.co.uk/blog/asynchronous-timeouts-completable-future-java-8-and-9
		//Titel "Java 9 improvements"
		
		Supplier<Integer> sup_getprice_with_timeout = () -> {
			final int timeout = 2*1000;
			final AtomicInteger price = new AtomicInteger();
			Thread t = new Thread(() -> {
				price.set(service.getPrice());
			});
			t.start();
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(t.isAlive()) {
				System.out.println("Timeout. Using default.");
				t.interrupt();
				price.set(42);
			}
			return price.get();
		};

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
