package challenge1.rxjava2;

import externalLegacyCodeNotUnderOurControl.PriceService;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

import java.util.HashSet;
import java.util.Set;

import static externalLegacyCodeNotUnderOurControl.PrintlnWithThreadname.println;
import static java.lang.Thread.sleep;

/**
 * This example uses RxJava2.
 *
 * @author Andy Nyffenegger
 * @author Marcus Fihlon, www.fihlon.ch
 */
public class RxJava2Example {

    private static final int NUMBER_OF_SERVICE_CALLS = 10;

    private final Set<PriceService> services;
    private int price = 0;
    private int count = 0;

    public static void main(final String... args) throws InterruptedException {
        new RxJava2Example().run();
        sleep(10_000);
    }

    /**
     * Create the price services.
     */
    private RxJava2Example() {
        this.services = new HashSet<>();
        for (int i = 0; i < NUMBER_OF_SERVICE_CALLS; i++) {
            this.services.add(new PriceService());
        }
    }

    /**
     * Use RxJava2 to call the price services.
     */
    private void run() {
        Flowable.fromIterable(services)
                .flatMap(priceService -> Flowable.fromCallable(priceService::getPrice)
                        .subscribeOn(Schedulers.io()))
                .subscribe(this::collector);
    }

    /**
     * Collect the answers and calculate the average price.
     */
    private void collector(final int price) {
        this.price += price;
        if (++this.count == NUMBER_OF_SERVICE_CALLS) {
            println("The average price is: " + this.price / this.count);
            System.exit(0);
        }
    }

}
