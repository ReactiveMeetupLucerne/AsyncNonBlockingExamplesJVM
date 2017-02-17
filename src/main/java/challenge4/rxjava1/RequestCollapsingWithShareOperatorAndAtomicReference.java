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

		collapsingRequestPriceService.getPrice()
				.subscribe(price -> println("1: " + price));

		TimeUnit.SECONDS.sleep(3);

		collapsingRequestPriceService.getPrice()
				.subscribe(price -> println("2a: " + price));
		collapsingRequestPriceService.getPrice()
				.subscribe(price -> println("2b: " + price));
		collapsingRequestPriceService.getPrice()
				.subscribe(price -> println("2c: " + price));

		TimeUnit.SECONDS.sleep(3);

		collapsingRequestPriceService.getPrice()
				.subscribe(price -> println("3: " + price));

		TimeUnit.SECONDS.sleep(3);
	}

	static class CollapsingRequestPriceService {

		private final PriceService delegate;
		private final AtomicReference<Single<Integer>> ongoingDelegateRequestRef = new AtomicReference<>();

		CollapsingRequestPriceService(final PriceService delegate) {
			this.delegate = delegate;
		}

		public Single<Integer> getPrice() {
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
							.toSingle();
					if (ongoingDelegateRequestRef.compareAndSet(null, newOngoingRequestCandidate)) {
						return newOngoingRequestCandidate;
					}
				}
			}
		}
	}
}