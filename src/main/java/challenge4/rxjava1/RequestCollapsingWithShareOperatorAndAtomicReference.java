package challenge4.rxjava1;

import externalLegacyCodeNotUnderOurControl.PriceService;
import rx.Observable;
import rx.Single;
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static externalLegacyCodeNotUnderOurControl.PrintlnWithThreadname.println;

public class RequestCollapsingWithShareOperatorAndAtomicReference {

    public static void main(String[] args) throws InterruptedException {
        PriceService originalPriceService = new PriceService(2);
        CollapsingRequestPriceService collapsingRequestPriceService = new CollapsingRequestPriceService(originalPriceService);

        collapsingRequestPriceService.getPriceAsyncCollapsed()
                .subscribe(price -> println("1: " + price));

        TimeUnit.SECONDS.sleep(10);

        collapsingRequestPriceService.getPriceAsyncCollapsed()
                .subscribe(price -> println("2a: " + price));
        collapsingRequestPriceService.getPriceAsyncCollapsed()
                .subscribe(price -> println("2b: " + price));

        TimeUnit.SECONDS.sleep(10);

        collapsingRequestPriceService.getPriceAsyncCollapsed()
                .subscribe(price -> println("3: " + price));

        TimeUnit.SECONDS.sleep(10);

        collapsingRequestPriceService.getPriceAsyncCollapsed()
                .subscribe(price -> println("4a: " + price));
        collapsingRequestPriceService.getPriceAsyncCollapsed()
                .subscribe(price -> println("4b: " + price));
        collapsingRequestPriceService.getPriceAsyncCollapsed()
                .subscribe(price -> println("4c: " + price));
        collapsingRequestPriceService.getPriceAsyncCollapsed()
                .subscribe(price -> println("4d: " + price));
        collapsingRequestPriceService.getPriceAsyncCollapsed()
                .subscribe(price -> println("4e: " + price));

        TimeUnit.SECONDS.sleep(10);
    }

    static class CollapsingRequestPriceService {

        private final PriceService delegate;
        private final AtomicReference<Single<Integer>> ongoingDelegateRequestRef = new AtomicReference<>();

        CollapsingRequestPriceService(final PriceService delegate) {
            this.delegate = delegate;
        }

        Single<Integer> getPriceAsyncCollapsed() {
            println("Handling incoming request. Will collapse concurrent requests...");

            // compare and swap loop
            while (true) {
                Single<Integer> currentOngoingRequest = ongoingDelegateRequestRef.get();
                if (currentOngoingRequest != null) {
                    return currentOngoingRequest;
                } else {
                    final Single<Integer> newOngoingRequestCandidate = Observable.fromCallable(() -> {
                        try {
                            return delegate.getPrice();
                        } finally {
                            ongoingDelegateRequestRef.set(null);
                        }
                    }).subscribeOn(Schedulers.io())
                            .share() // here is the "trick"
                            .toSingle()
                            .flatMap(price -> Single.just(price).subscribeOn(Schedulers.io())); // parallelize again further processing
                    if (ongoingDelegateRequestRef.compareAndSet(null, newOngoingRequestCandidate)) {
                        return newOngoingRequestCandidate;
                    }
                }
            }
        }
    }
}