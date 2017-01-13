package challenge2.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Dmytro Rud
 */
public class Challenge2 {

    public static void main(String... args) throws Exception {
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("challenge2/camel/context.xml");
        CamelContext camelContext = appContext.getBean("camelContext", CamelContext.class);
        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();

        producerTemplate.asyncSendBody("direct:start", null);

        Thread.currentThread().join();
    }
}

