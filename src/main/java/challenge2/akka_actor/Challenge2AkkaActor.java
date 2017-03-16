package challenge2.akka_actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import externalLegacyCodeNotUnderOurControl.PriceService;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static externalLegacyCodeNotUnderOurControl.PrintlnWithThreadname.println;

public class Challenge2AkkaActor {

    public static class MyActor extends AbstractActor {
        private ActorRef origin;
        private PriceService service = new PriceService(5);
        private Thread thread = null;

        public MyActor() {
            receive(ReceiveBuilder
                    .matchEquals("calc", s -> {
                        origin = sender();
                        thread = new Thread(() -> {
                            int result = service.getPrice();
                            if (origin != null) {
                                origin.tell(result, ActorRef.noSender());
                            }
                        });
                        thread.start();
                        getContext().setReceiveTimeout(Duration.create("2 seconds"));
                    })
                    .match(ReceiveTimeout.class, i -> {
                        thread.interrupt();
                        origin.tell(42, self());
                        origin = null;
                    })
                    .build()
            );
        }
    }

    private void run() throws Exception {
        ActorSystem system = ActorSystem.create("MySystem");

        Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));
        final ActorRef myActor = system.actorOf(Props.create(MyActor.class));
        Future<Object> future = Patterns.ask(myActor, "calc", timeout);
        Integer amount = (Integer) Await.result(future, timeout.duration());
        println("Price with timeout is: " + amount);

        system.shutdown();
        println("Main thread done.");
    }

    public static void main(String[] args) throws Exception {
        new Challenge2AkkaActor().run();
    }
}
