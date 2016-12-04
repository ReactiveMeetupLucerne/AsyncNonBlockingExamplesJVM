package challenge2.akka_actor

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import externalLegacyCodeNotUnderOurControl.PriceService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

/**
  * Challenge 2: fallback in case of timeout
  *
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
    println(s"The average price is ${average.amount} (${System.currentTimeMillis() - start} ms)"))

  // the Runner handles the Workers
  // simplified - as only one GetAverage is sent at once
  class ServiceRunner extends Actor {

    def receive: PartialFunction[Any, Unit] = {
      case GetAverage =>
        val requester = sender()
        // delegate the work to the workers
        Future.fold(
          serviceWorkers
            .map {
              _.ask(GetPrice)(Timeout(2 seconds))
                .mapTo[Price]
                .map(_.amount)
                .recoverWith {
                  //handle timeout exception
                  case exc =>
                    println(s"[${Thread.currentThread().getName}] The special price is 42 ${exc.getMessage}")
                    Future(42)
                }
            })(List[Int]())((a, b) => b :: a)
          .map { prices =>
            println(s"called service: ${prices.size} > $prices")
            requester ! Average(prices.sum / serviceCount)
          }
    }
  }

  // the worker gets the price from the PriceService
  class ServiceWorker extends Actor {
    val service = new PriceService(Random.nextInt(100) % 4)

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
