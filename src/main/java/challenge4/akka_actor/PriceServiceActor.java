package challenge4.akka_actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.dispatch.OnSuccess;
import akka.japi.pf.ReceiveBuilder;
import externalLegacyCodeNotUnderOurControl.PriceService;
import scala.concurrent.Future;

import static akka.dispatch.Futures.future;
import static externalLegacyCodeNotUnderOurControl.PrintlnWithThreadname.println;

/**
 * Calls the price Service (blocking) and returns the result
 */
public class PriceServiceActor extends AbstractActor {

    public PriceServiceActor(PriceService service) {

        receive(ReceiveBuilder.
                match(PriceRequest.class, (PriceRequest request) -> {
                    println("PriceServiceActor: got price request: " + request);

                    ActorRef sender = sender();
                    Future<Price> calc = future(
                            () -> new Price(service.getPrice()),
                            context().system().dispatcher());

                    calc.onSuccess(new OnSuccess<Price>() {
                        @Override
                        public void onSuccess(Price price) throws Throwable {
                            println("PriceServiceActor: Success: " + price);
                            sender.tell(new PriceEnvelope(request, price), self());
                        }
                    }, context().system().dispatcher());
                }).
                build()
        );
    }

}
