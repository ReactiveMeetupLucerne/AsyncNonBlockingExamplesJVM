package challenge1.java8;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import externalLegacyCodeNotUnderOurControl.PriceService;

public class GmoCompletableFutureDemo {

	private PriceService service = new PriceService();
	
	private void run() {
		Supplier<Integer> sup_getprice = () -> {
			return service.getPrice();
		};

		CompletableFuture<Integer> cf1 = CompletableFuture.supplyAsync(sup_getprice);
		CompletableFuture<Integer> cf2 = CompletableFuture.supplyAsync(sup_getprice);
		CompletableFuture<Integer> cf3 = CompletableFuture.supplyAsync(sup_getprice);
		
		CompletableFuture<?>[] cfa = {cf1, cf2, cf3};
		CompletableFuture<Void> alldone = CompletableFuture.allOf(cfa);
		//CompletableFuture<Void> alldone = CompletableFuture.allOf(cf1, cf2, cf3);
		
		alldone.thenRun(() -> {
			try {
				assert cf1.isDone();
				assert cf2.isDone();
				assert cf3.isDone();
				double avg = (cf1.get()+cf2.get()+cf3.get())/3.0;
				System.out.println("Average price = "+avg);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		});
		
		try {
			System.out.println("Main thread waiting for completion.");
			Thread.sleep(5000);
			System.out.println("Main thread done.");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new GmoCompletableFutureDemo().run();
	}

}
