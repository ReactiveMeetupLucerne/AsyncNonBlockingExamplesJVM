package challenge1.rxjava2;

import externalLegacyCodeNotUnderOurControl.PriceService;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static externalLegacyCodeNotUnderOurControl.PrintlnWithThreadname.println;

/**
 * This example uses RxJava2.
 *
 * @author Andy Nyffenegger
 * @author Marcus Fihlon, www.fihlon.ch
 */
public class RxJava2Example {

    private static final int NUMBER_OF_SERVICE_CALLS = 10;

    private final ExecutorService executorService;
    private final Set<PriceService> services;
    private final AtomicInteger price;
    private final AtomicInteger count;

    /**
     * Create the price services.
     */
    private RxJava2Example() {
        this.executorService = Executors.newCachedThreadPool();
        this.price = new AtomicInteger(0);
        this.count = new AtomicInteger(0);
        this.services = new HashSet<>();
        for (int i = 0; i < NUMBER_OF_SERVICE_CALLS; i++) {
            this.services.add(new PriceService());
        }
    }

    public static void main(final String... args) throws InterruptedException {
        new RxJava2Example().run();
    }

    /**
     * Use RxJava2 to call the price services.
     */
    private void run() {
        Flowable.fromIterable(services)
                .flatMap(priceService -> Flowable.fromCallable(priceService::getPrice)
                        .subscribeOn(Schedulers.from(this.executorService)))
                .subscribe(this::collector);
        this.executorService.shutdown();
    }

    /**
     * Collect the answers and calculate the average price.
     */
    private void collector(final int price) {
        this.price.addAndGet(price);
        if (this.count.incrementAndGet() == NUMBER_OF_SERVICE_CALLS) {
            println("The average price is: " + this.price.get() / this.count.get());
        }
    }

}
