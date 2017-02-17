package challenge3.rxjava2;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.apache.commons.lang3.tuple.Triple;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static externalLegacyCodeNotUnderOurControl.PrintlnWithThreadname.println;

// https://github.com/ReactiveMeetupLucerne/AsyncNonBlockingExamplesJVM/issues/9
public class Challenge3RxJava2ExampleWithThrotteling {

    public static void main(String[] args) throws InterruptedException {
        Observable<Integer> speedyTemperatureSource = Observable.range(1, 100_000_000)
                .subscribeOn(Schedulers.io())
                .map(tick -> ThreadLocalRandom.current().nextInt(20, 25))
                .doOnComplete(() -> println("Speedy temperature source created " + 100_000_000 + " random temperature values. Will do that again..."))
                .repeat();

        Observable<Triple<Integer, Integer, Integer>> minMaxValuesWithinWindow = speedyTemperatureSource
                .sample(1, TimeUnit.MILLISECONDS) // throttle the speedy source a bit
                .window(10, TimeUnit.SECONDS)
                .flatMap(temperatureValuesWithinWindow ->
                        temperatureValuesWithinWindow.reduce(
                                Triple.of(Integer.MAX_VALUE, Integer.MIN_VALUE, 0 /* count */),
                                (minMaxCountTriple, tempValue) -> {
                                    int newMin = minMaxCountTriple.getLeft();
                                    if (tempValue < newMin) {
                                        newMin = tempValue;
                                    }
                                    int newMax = minMaxCountTriple.getMiddle();
                                    if (tempValue > newMax) {
                                        newMax = tempValue;
                                    }
                                    return Triple.of(newMin, newMax, minMaxCountTriple.getRight() + 1);
                                }
                        ).toObservable()
                );

        minMaxValuesWithinWindow.subscribe(minMaxCountTriple ->
                println("Within 10 seconds window: Min="
                        + minMaxCountTriple.getLeft()
                        + "°Celsius, Max=" + minMaxCountTriple.getMiddle()
                        + "°Celsius. Had " + minMaxCountTriple.getRight()
                        + " values in window. "
                        + "Calculated async and non-blocking TM :-)"));

        println("I wasn't blocked");

        TimeUnit.MINUTES.sleep(1);
    }
}
