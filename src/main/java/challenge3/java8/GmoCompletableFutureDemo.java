package challenge3.java8;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import externalLegacyCodeNotUnderOurControl.TemperatureValueSource;

public class GmoCompletableFutureDemo
{
	private class TemperatureObserver
	implements TemperatureValueSource.TemperatureListener
	{
		private int min=Integer.MAX_VALUE;
		private int max=Integer.MIN_VALUE;
		
		@Override
		public synchronized void onNext(int temperature) {
			System.out.println("Got temperature: "+temperature);
			min = Math.min(min, temperature);
			max = Math.max(max, temperature);
		}
		
		public synchronized void printStatus()
		{
			System.out.println("Observer status: min="+min+", max="+max);
		}
	}
	
	private void run()
	{
		final TemperatureObserver tvo = new TemperatureObserver();
		
		TemperatureValueSource tvsource = new TemperatureValueSource();
		tvsource.addListener(tvo);

		ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
		ses.scheduleAtFixedRate(() -> tvo.printStatus(), 10, 10, TimeUnit.SECONDS);
	        
		System.out.println("Main thread done.");
	}
	
	public static void main(String[] args) {
		new GmoCompletableFutureDemo().run();	
	}

}
