package challenge1.rxjava1;

import externalLegacyCodeNotUnderOurControl.PriceService;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

import static externalLegacyCodeNotUnderOurControl.PrintlnWithThreadname.println;

public class RxJavaObservableExample {

    public static void main(String[] args) throws InterruptedException {
        Observable.just(
                new PriceService(2),
                new PriceService(3),
                new PriceService(2)
        )
                .flatMap(priceService -> Observable.fromCallable(priceService::getPrice).subscribeOn(Schedulers.io()))
                .reduce(0, (aPrice, anotherPrice) -> aPrice + anotherPrice)
                .map(sumOfAllPrices -> (sumOfAllPrices / 3.0))
                .subscribe(avg -> println("Average price " + avg));

        TimeUnit.SECONDS.sleep(10);
    }
}
