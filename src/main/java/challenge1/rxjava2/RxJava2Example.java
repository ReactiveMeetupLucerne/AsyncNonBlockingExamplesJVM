package challenge1.rxjava2;

import externalLegacyCodeNotUnderOurControl.PriceService;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

import static externalLegacyCodeNotUnderOurControl.PrintlnWithThreadname.println;

/**
 * This example uses RxJava2 {@link Flowable}s.
 *
 * @author Andy Nyffenegger
 * @author Marcus Fihlon, www.fihlon.ch
 * @author Peti Koch
 */
public class RxJava2Example {

    public static void main(final String... args) throws InterruptedException {
        int numberOfPriceServices = 3;

        Flowable<PriceService> priceServices =
                Flowable.range(1, numberOfPriceServices)
                        .map(integer -> new PriceService());

        priceServices
                .flatMap(priceService -> Flowable.fromCallable(priceService::getPrice).subscribeOn(Schedulers.io()))
                .reduce((number1, number2) -> number1 + number2)
                .map(sum -> sum / (double) numberOfPriceServices)
                .subscribe(avg -> println("The average price is: " + avg));

        TimeUnit.SECONDS.sleep(10);
    }


}
