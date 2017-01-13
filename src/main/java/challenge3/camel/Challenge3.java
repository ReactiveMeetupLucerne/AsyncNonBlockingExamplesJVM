package challenge3.camel;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Dmytro Rud
 */
public class Challenge3 {

    public static void main(String... args) throws Exception {
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("challenge3/camel/context.xml");
        Thread.currentThread().join();
    }
}

