package challenge4.rxjava2;

import externalLegacyCodeNotUnderOurControl.PriceService;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static externalLegacyCodeNotUnderOurControl.PrintlnWithThreadname.println;
import static java.lang.Thread.sleep;

/**
 * This example uses RxJava2.
 *
 * @author Andy Nyffenegger
 * @author Marcus Fihlon, www.fihlon.ch
 */
public class CollapsingWIthRxJava2Example {

    private static final int NUMBER_OF_SERVICE_CALLS = 10;


    private int price = 0;
    private int count = 0;

    public static void main(final String... args) throws InterruptedException {
        new CollapsingWIthRxJava2Example().run();
        sleep(10_000);
    }

    /**
     * Create the price services.
     */
    private CollapsingWIthRxJava2Example() {


    }

    /**
     * Use RxJava2 to call the price services.
     */
    private void run() {

        final PriceService priceService = new PriceService();


        final Flowable<Integer> priceCall = Flowable.fromCallable(priceService::getPrice);

        final List<Integer> callsList = Arrays.asList(1, 2, 3, 4);

        Flowable<Integer> calls = Flowable.fromIterable(callsList);

        Flowable.combineLatest(priceCall, calls, (price, ignored) -> price).subscribe(this::collector);


    }

    /**
     * Collect the answers and calculate the average price.
     */
    private synchronized void collector(final int price) {
        System.out.println(price);

    }

}
