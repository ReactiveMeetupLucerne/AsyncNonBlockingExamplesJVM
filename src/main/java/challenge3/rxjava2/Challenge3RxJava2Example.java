package challenge3.rxjava2;

import externalLegacyCodeNotUnderOurControl.TemperatureValueSource;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.TimeUnit;

import static externalLegacyCodeNotUnderOurControl.PrintlnWithThreadname.println;

public class Challenge3RxJava2Example {

    public static void main(String[] args) throws InterruptedException {
        TemperatureValueSource temperatureValueSource = new TemperatureValueSource();

        Observable<Integer> temperatureValues = Observable.<Integer>create(emitter -> {
            TemperatureValueSource.TemperatureListener temperatureListener = emitter::onNext;
            temperatureValueSource.addListener(temperatureListener);
            emitter.setCancellable(() -> temperatureValueSource.removeListener(temperatureListener));
        }).doOnNext(tempValue -> println(tempValue + "°Celsius from temperature source"));

        Observable<Pair<Integer, Integer>> minMaxValuesWithinWindow = temperatureValues.window(10, TimeUnit.SECONDS, Schedulers.computation())
                .flatMap(temperatureValuesWithinWindow ->
                        temperatureValuesWithinWindow.reduce(
                                Pair.of(Integer.MAX_VALUE, Integer.MIN_VALUE),
                                (minMaxPair, tempValue) -> {
                                    int newMin = minMaxPair.getLeft();
                                    if (tempValue < newMin) {
                                        newMin = tempValue;
                                    }
                                    int newMax = minMaxPair.getRight();
                                    if (tempValue > newMax) {
                                        newMax = tempValue;
                                    }
                                    return Pair.of(newMin, newMax);
                                }
                        ).toObservable()
                );

        minMaxValuesWithinWindow.subscribe(minMaxPair -> println("Within window: Min=" + minMaxPair.getLeft() + "°Celsius, Max=" + minMaxPair.getRight() + "°Celsius. Calculated async and non-blocking TM :-)"));

        println("I wasn't blocked");

        TimeUnit.MINUTES.sleep(1);
    }
}
