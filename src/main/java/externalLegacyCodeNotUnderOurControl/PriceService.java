package externalLegacyCodeNotUnderOurControl;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static externalLegacyCodeNotUnderOurControl.PrintlnWithThreadname.println;

// DON'T CHANGE... it's legacy code and out of our control
public class PriceService {

    private static final int DEFAULT_DELAY_SECONDS = 1;

    private final int delay_seconds;

    public PriceService() {
        this(DEFAULT_DELAY_SECONDS);
    }

    public PriceService(int delay_seconds) {
        this.delay_seconds = delay_seconds;
    }

    public int getPrice() {
        println("Calculating price delayed by "+ delay_seconds+ " s");
        try {
            TimeUnit.SECONDS.sleep(delay_seconds);
            int result = ThreadLocalRandom.current().nextInt(1, 100);
            println("The price is " + result);
            return result;
        } catch (InterruptedException e) {
            println("PriceService#getPrice() was interrupted.");
            Thread.currentThread().interrupt();
            return -1;
        }
    }
}
