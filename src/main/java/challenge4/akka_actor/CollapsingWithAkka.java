package challenge4.akka_actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import externalLegacyCodeNotUnderOurControl.PriceService;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static externalLegacyCodeNotUnderOurControl.PrintlnWithThreadname.println;
import static java.lang.Thread.sleep;

/**
 * Collapse _equal_ requests when sending to the {@link PriceServiceActor}.
 * <pre>
 * [current "book" price?] ->                    -> [println "book" price]
 *                            \                  /
 * [current "book" price?] -> [PriceService1::getPrice]  -> [println "book" price]
 *
 * </pre>
 */
public class CollapsingWithAkka {

    public static void main(String[] args) throws Exception {
        ActorSystem system = ActorSystem.create("MySystem");

        final ActorRef priceServiceActor = system.actorOf(Props.create(PriceServiceActor.class, new PriceService(4)));
        final ActorRef priceFacade = system.actorOf(Props.create(PriceFacade.class, priceServiceActor));
        final ActorRef bookSimulator = system.actorOf(Props.create(ClientSimulator.class, priceFacade, "Book"));
        final ActorRef toySimulator = system.actorOf(Props.create(ClientSimulator.class, priceFacade, "Toy"));

        sleep(10_000);
        system.shutdown();
        println("Main thread done.");
    }

    /**
     * Collapses {@link PriceRequest}s and returns the same result to all requesters for the same product.
     */
    private static class PriceFacade extends AbstractActor {
        Multimap<PriceRequest, ActorRef> requesters = ArrayListMultimap.create();

        public PriceFacade(ActorRef priceServiceActor) {
            receive(ReceiveBuilder.
                    match(PriceRequest.class, request -> {
                        println("PriceFacade: " + request);
                        boolean firstRequest = !requesters.containsKey(request);
                        requesters.put(request, sender());
                        if (firstRequest) {
                            priceServiceActor.tell(request, self());
                        }
                    }).
                    match(PriceEnvelope.class, priceEnvelope -> {
                        println("PriceFacade: response: " + priceEnvelope);
                        for (ActorRef requester : requesters.removeAll(priceEnvelope.priceRequest)) {
                            requester.tell(priceEnvelope.price, self());
                        }
                    }).
                    build()
            );
        }
    }

    /**
     * There is one simulator per Product, which sends requesters regularly.
     */
    public static class ClientSimulator extends AbstractActor {
        private ActorRef priceFacade;
        private String productName;

        public ClientSimulator(ActorRef priceFacade, String productName) {
            this.priceFacade = priceFacade;
            this.productName = productName;
            receive(ReceiveBuilder.
                    match(Price.class, price -> {
                        println("ClientSimulator " + productName + ": " + price);
                    }).
                    build());
        }

        @Override
        public void preStart() throws Exception {
            PriceRequest request = new PriceRequest(productName);
            context().system().scheduler().schedule(Duration.Zero(),
                    Duration.create(1, TimeUnit.SECONDS),
                    priceFacade, request,
                    context().system().dispatcher(), self());
        }
    }
}


