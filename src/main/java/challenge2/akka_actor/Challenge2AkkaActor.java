package challenge2.akka_actor;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import externalLegacyCodeNotUnderOurControl.PriceService;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class Challenge2AkkaActor {

    public static class PriceServiceActor extends AbstractActor {
        private PriceService service = new PriceService();

        public PriceServiceActor() {
            receive(ReceiveBuilder
                    .matchEquals("calc", s -> {
                        int result = service.getPrice();
                        sender().tell(result, self());
                    })
                    .build()
            );
        }

        static Props props() {
            return Props.create(PriceServiceActor.class);
        }
    }


    public static class MyActor extends AbstractActor {
        private final LoggingAdapter log = Logging.getLogger(context().system(), this);
        private ActorRef origin;
        private int result;
        private ActorRef service;

        public MyActor(ActorRef service) {
            this.service = service;
            receive(ReceiveBuilder
                    .matchEquals("calc", s -> {
                        origin = sender();
                        getContext().setReceiveTimeout(Duration.create("300 milliseconds"));
                        service.tell("calc", self());
                    }).match(Integer.class, i -> {
                        origin.tell(i, self());
                    }).match(ReceiveTimeout.class, i -> {
                        origin.tell(42, self());
                    })
                    .build()
            );
        }

        public static Props props(ActorRef priceService) {
            return Props.create(MyActor.class, priceService);
        }
    }


    private void run() throws Exception {
        ActorSystem system = ActorSystem.create("MySystem");

        Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));
        final ActorRef priceServiceActor = system.actorOf(PriceServiceActor.props());
        final ActorRef myActor = system.actorOf(MyActor.props(priceServiceActor));
        Future<Object> future = Patterns.ask(myActor, "calc", timeout);
        Integer amount = (Integer) Await.result(future, timeout.duration());
        System.out.println("Price with timeout is: " + amount);

        system.shutdown();
        System.out.println("Main thread done.");
    }

    public static void main(String[] args) throws Exception {
        new Challenge2AkkaActor().run();
    }


}
