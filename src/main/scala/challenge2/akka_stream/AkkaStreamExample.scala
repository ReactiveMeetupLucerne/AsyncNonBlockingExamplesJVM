package challenge2.akka_stream

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import externalLegacyCodeNotUnderOurControl.PriceService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

/**
  * Challenge 2: fallback in case of timeout
  *
  * Simple Akka Stream Example (taking the Flow example from challenge 1)
  * Created by pascal.mengelt on 29.11.2016.
  */
object AkkaStreamExample extends App {

  implicit val system = ActorSystem("AkkaStreams")
  implicit val materializer = ActorMaterializer()
  val serviceCount = 20
  val start = System.currentTimeMillis()


  // self documenting process
  source
    .via(createPriceServices)
    .via(getPrices)
    .runWith(collectPrices)
    .map(calcAverage)
    .foreach(printAverage)

  TimeUnit.SECONDS.sleep(20)
  system.terminate()

  // implementation of each step
  // Source
  private lazy val source = Source.fromIterator(() => (1 to serviceCount).iterator)
  // Flows
  // changed PriceService creation with Random delay
  private lazy val createPriceServices = Flow[Int].map(_ => new PriceService(Random.nextInt(3)))
  // changed with adding a Timeout
  private lazy val getPrices =
    Flow[PriceService]
      .mapAsyncUnordered(serviceCount)(s => Future.firstCompletedOf(Seq(
        Future(s.getPrice)
        , Future {
          TimeUnit.SECONDS.sleep(2)
          println(s"[${Thread.currentThread().getName}] The special price is 42")
          42
        })))

  // Sink
  private lazy val collectPrices = Sink.seq[Int]

  private def calcAverage(prices: Seq[Int]) = {
    println(s"called service: ${prices.size} > $prices")
    prices.sum / serviceCount
  }

  private def printAverage(average: Int) = println(s"The average price is $average (${System.currentTimeMillis() - start} ms): " + Thread.currentThread().getName)

}
