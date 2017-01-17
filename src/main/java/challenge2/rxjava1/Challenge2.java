package challenge2.rxjava1;

import externalLegacyCodeNotUnderOurControl.PriceService;
import rx.Single;
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

public class Challenge2 {
    PriceService priceService;

    public static void main(String[] args) throws InterruptedException {
        new Challenge2().run();
    }

    public Challenge2() {
        priceService = new PriceService(5);
    }

    public void run() {
        Single.fromCallable(
                () -> priceService.getPrice())
                .subscribeOn(Schedulers.io())
                .doOnUnsubscribe(()-> System.out.println("doOnUnsubscribe"))
                .timeout(2, TimeUnit.SECONDS, Single.fromCallable(() -> 42).doOnSuccess(value-> System.out.println("Used fallback value")))
                .subscribe(System.out::println);

        sleep(10_000);
    }

    private void sleep(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
