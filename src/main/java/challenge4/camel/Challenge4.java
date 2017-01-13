package challenge4.camel;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Dmytro Rud
 */
public class Challenge4 {

    public static void main(String... args) throws Exception {
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("challenge4/camel/context.xml");
        Thread.currentThread().join();
    }
}

