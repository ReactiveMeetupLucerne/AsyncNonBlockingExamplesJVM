package challenge1.guava;

import com.google.common.util.concurrent.*;
import externalLegacyCodeNotUnderOurControl.PriceService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static externalLegacyCodeNotUnderOurControl.PrintlnWithThreadname.println;

public class ListenableFutureExample {

    public static void main(String[] args) throws InterruptedException {
        PriceService priceService1 = new PriceService();
        PriceService priceService2 = new PriceService();
        PriceService priceService3 = new PriceService();

        calculateAveragePriceAsync(
                priceService1,
                priceService2,
                priceService3,
                avgPrice -> println("Average: " + avgPrice)
        );

        println("I wasn't blocked... :-)");

        TimeUnit.SECONDS.sleep(10);
    }

    private static void calculateAveragePriceAsync(PriceService priceService1,
                                                   PriceService priceService2,
                                                   PriceService priceService3,
                                                   Consumer<Double> resultConsumer) {
        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(
                Executors.newFixedThreadPool(
                        3,
                        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("threadPool-%d").build()
                )
        );

        ListenableFuture<Integer> price1Future = executorService.submit(priceService1::getPrice);
        ListenableFuture<Integer> price2Future = executorService.submit(priceService2::getPrice);
        ListenableFuture<Integer> price3Future = executorService.submit(priceService3::getPrice);

        Futures.addCallback(
                Futures.allAsList(price1Future, price2Future, price3Future),
                new FutureCallback<List<Integer>>() {
                    @Override
                    public void onSuccess(List<Integer> result) {
                        double average = result.stream().mapToInt(value -> value).average().getAsDouble();
                        resultConsumer.accept(average);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        t.printStackTrace();
                    }
                },
                executorService
        );
    }

}
