package challenge4.java8;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import externalLegacyCodeNotUnderOurControl.PriceService;

public class GmoCompletableFutureDemo {

	private PriceService priceservice = new PriceService(3);
	
	private Map<String, CompletableFuture<Integer>> cfcache = new HashMap<>();
	private Lock cfcachemap_lock = new ReentrantLock();
	
	private CompletableFuture<Integer> supplyOrCollapse(String servicename)
	{
		System.out.print("Request for "+servicename+" => ");
		try {
			cfcachemap_lock.lock();
			
			CompletableFuture<Integer> cf = cfcache.get(servicename);
			if(cf != null && !cf.isDone()) {
				//Cache hit => Collapse
				System.out.println("Collapsed.");
				return cf;
			}
			
			//No cache hit. Create new.
			Supplier<Integer> sup = null;
			if("getprice".equals(servicename)) {
				sup = () -> {
					return priceservice.getPrice();
				};
			} else if("dummy".equals(servicename)) {
				sup = () -> {
					return 99;
				};
			}
			
			if(sup != null) {
				System.out.println("supplyAsync.");
				cf = CompletableFuture.supplyAsync(sup);
				cfcache.put(servicename, cf);
				return cf;
			} else {
				return null;
			}
		} finally {
			cfcachemap_lock.unlock();
		}
	}
	
	private void run()
	{
		supplyOrCollapse("getprice").thenAccept(price -> { System.out.println("Price (requested after 0 seconds): "+price); });
		try { Thread.sleep(1*1000); } catch (InterruptedException e) { }
		supplyOrCollapse("getprice").thenAccept(price -> { System.out.println("Price (requested after 1 second):  "+price); });
		try { Thread.sleep(3*1000); } catch (InterruptedException e) { }
		supplyOrCollapse("getprice").thenAccept(price -> { System.out.println("Price (requested after 4 seconds): "+price); });
		try { Thread.sleep(1*1000); } catch (InterruptedException e) { }
		supplyOrCollapse("getprice").thenAccept(price -> { System.out.println("Price (requested after 5 seconds): "+price); });
		supplyOrCollapse("getprice").thenAccept(price -> { System.out.println("Price (requested after 5 seconds): "+price); });
		supplyOrCollapse("dummy").thenAccept(price -> { System.out.println("Dummy: "+price); });
	}
	
	public static void main(String[] args) {
		new GmoCompletableFutureDemo().run();
	}

}
