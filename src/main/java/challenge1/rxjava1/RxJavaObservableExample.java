package challenge1.rxjava1;

import externalLegacyCodeNotUnderOurControl.PriceService;
import rx.Observable;
import rx.observables.MathObservable;
import rx.schedulers.Schedulers;

import static externalLegacyCodeNotUnderOurControl.PrintlnWithThreadname.println;

public class RxJavaObservableExample {
    PriceService priceService;

    public static void main(String[] args) throws InterruptedException {
        new RxJavaObservableExample().run();
    }

    public RxJavaObservableExample() {
        priceService = new PriceService();
    }

    public void run() {
        Observable<Integer> prices = Observable.range(1, 3)
                .flatMap(id -> Observable.<Integer>create(s -> {
                            s.onNext(priceService.getPrice());
                            s.onCompleted();
                        }).subscribeOn(Schedulers.computation())
                );

        MathObservable<Integer> m = MathObservable.from(prices);
        m.averageInteger(o -> {
            println("calculating with " + o);
            return o;
        })
                .observeOn(Schedulers.io())
                .subscribe(d -> println("AVERAGE is " + d));

        sleep(20000);
    }


    private void sleep(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException ex) {
        }
    }
}
