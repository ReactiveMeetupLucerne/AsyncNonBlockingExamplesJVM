package challenge1.akka_actor

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import externalLegacyCodeNotUnderOurControl.PriceService

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
/**
  * Simple Akka Actor Example.
  * The fastest solution! Less than half the time!
  *
  * Created by pascal.mengelt on 29.11.2016.
  */
object AkkaActorExample extends App {

  case object GetAverage

  case object GetPrice

  case class Price(amount: Int)

  case class Average(amount: Int)

  implicit val system = ActorSystem("AkkaActor")
  implicit val timeout = Timeout(10 seconds) // needed for `?` below

  val serviceCount = 20
  val start = System.currentTimeMillis()

  val serviceRunner = system.actorOf(Props[ServiceRunner], "serviceRunner")
  val serviceWorkers = for (i <- 1 to serviceCount) yield system.actorOf(Props[ServiceWorker], s"serviceWorker_$i")

  // ask the ServiceRunner for the average
  (serviceRunner ? GetAverage).mapTo[Average]
    // print the result
    .foreach(average =>
    println(s"The average price is ${average.amount} (${System.currentTimeMillis() - start} ms): " + Thread.currentThread().getName))


  // the Runner handles the Workers
  // simplified - as only one GetAverage is sent at once
  class ServiceRunner extends Actor {
    // keeps track of the results
    val prices: ListBuffer[Int] = mutable.ListBuffer()
    // remember who has asked
    var requester: ActorRef = _

    def receive: PartialFunction[Any, Unit] = {
      case GetAverage =>
        requester = sender()
        // delegate the work to the workers
        serviceWorkers.foreach(_ ! GetPrice)
      case Price(amount) =>
        // add the Price to the results
        prices += amount
        // if all results are back - send back the average
        if (prices.length == serviceCount){
          requester ! Average(prices.sum / serviceCount)
        }
    }
  }

  // the worker gets the price from the PriceService
  class ServiceWorker extends Actor {
    val service = new PriceService()

    def receive: PartialFunction[Any, Unit] = {
      case GetPrice =>
        // get the price from the service and return it to the sender
        sender() ! Price(service.getPrice)
    }
  }

  println("Did not block!")
  TimeUnit.SECONDS.sleep(10)
  system.terminate()
}
