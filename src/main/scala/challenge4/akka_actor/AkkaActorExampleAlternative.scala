package challenge4.akka_actor

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props, TypedActor, TypedProps}
import externalLegacyCodeNotUnderOurControl.PriceService

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

/**
  * Challenge 4: request collapsing
  *
  * Jarek Ratajski  - Handler changed to Typed Actor
  */
object AkkaActorExampleWithTypedActor extends App {

  case class  PriceRequest(  handler :IServiceHandler)

  case class Price(amount: Int)

  implicit val system = ActorSystem("AkkaActor")
  implicit val executor = system.dispatcher

  val serviceCount = 20
  val start = System.currentTimeMillis()

  val serviceRunner = system.actorOf(Props[ServiceRunner], "serviceRunner")
  val serviceHandler: IServiceHandler =
    TypedActor(system).typedActorOf(TypedProps[ServiceHandlerImple]())
  val serviceCallers = for (i <- 1 to serviceCount) yield system.actorOf(Props[ServiceCaller], s"ServiceCaller_$i")

  // create Scheduler that triggers the ServiceRunner to fire the Price requests
  val scheduler = system.scheduler


  scheduler.schedule(
    initialDelay = 0 seconds
    , interval = 2 seconds
    , receiver = serviceRunner
    , PriceRequest)

  // invokes all the ServiceCallers
  // this simulates
  class ServiceRunner extends Actor {
    val service = new PriceService()

    def receive: PartialFunction[Any, Unit] = {
      case PriceRequest =>
        for (caller <- serviceCallers) caller ! PriceRequest
    }
  }

  // asks for the Price and prints it out
  class ServiceCaller extends Actor {

    def receive: PartialFunction[Any, Unit] = {
      case PriceRequest =>
        serviceHandler.priceRequest(self)
      case Price(amount) =>
        println(s"${this.self.toString}[${Thread.currentThread().getName}] The price is $amount")
    }
  }



  // the worker gets the price from the PriceService
  class ServiceWorker extends Actor {

    def receive: PartialFunction[Any, Unit] = {
      case PriceRequest(handler) =>
        handler.price(new PriceService(Random.nextInt(3)).getPrice)
    }
  }

  println("Did not block!")
  TimeUnit.SECONDS.sleep(10)
  system.terminate()


  trait IServiceHandler {
    def priceRequest(sender: ActorRef)

    def price(amount: Int)
  }

  class ServiceHandlerImple extends IServiceHandler {
    val serviceWorker: ActorRef = system.actorOf(Props[ServiceWorker], s"serviceWorker")
    // keeps track of the requesters
    val requesters: ListBuffer[ActorRef] = mutable.ListBuffer()
    // flag that indicates that it is calling the service already
    var isRunning = false

    override def priceRequest(sender: ActorRef): Unit = {
      // add the sender to the requesters
      requesters += sender
      // if not running call the service - otherwise we already wait for an answer
      if (!isRunning) {
        serviceWorker ! PriceRequest(TypedActor.self)
        isRunning = true
      }

    }

    override def price(amount: Int): Unit  = {
      // send the Price to all requesters
      for (r <- requesters)
        r ! Price(amount)
      requesters.clear()
      isRunning = false
    }
  }


}