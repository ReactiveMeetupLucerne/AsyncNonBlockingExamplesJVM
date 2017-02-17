package externalLegacyCodeNotUnderOurControl;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PrintlnWithThreadname {

    private static DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;

    public static void println(Object message) {
        System.out.println(timeFormatter.format(LocalTime.now()) + " [" + Thread.currentThread().getName() + "] " + message);
    }

}
