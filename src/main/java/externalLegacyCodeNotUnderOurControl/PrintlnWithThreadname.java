package externalLegacyCodeNotUnderOurControl;

public class PrintlnWithThreadname {

    public static void println(Object message) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + message);
    }

}
