package challenge3.java5;

import externalLegacyCodeNotUnderOurControl.TemperatureValueSource;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static externalLegacyCodeNotUnderOurControl.PrintlnWithThreadname.println;

public class Challenge3Java5Example {

    public static void main(String[] args) throws InterruptedException {
        final AtomicInteger min = new AtomicInteger(Integer.MAX_VALUE);
        final AtomicInteger max = new AtomicInteger(Integer.MIN_VALUE);

        final TemperatureValueSource.TemperatureListener temperatureListener = temperature -> {
            System.out.println("Got temperature: " + temperature);
            min.getAndUpdate(operand -> Math.min(operand, temperature));
            max.getAndUpdate(operand -> Math.max(operand, temperature));
        };

        TemperatureValueSource tvsource = new TemperatureValueSource();
        tvsource.addListener(temperatureListener);

        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        ses.scheduleAtFixedRate(
                () -> println("Observer status: min=" + min.getAndSet(Integer.MAX_VALUE) + ", max=" + max.getAndSet(Integer.MIN_VALUE)),
                10, 10, TimeUnit.SECONDS
        );

        System.out.println("Main thread done.");
        TimeUnit.MINUTES.sleep(1);
        ses.shutdown();
    }
}