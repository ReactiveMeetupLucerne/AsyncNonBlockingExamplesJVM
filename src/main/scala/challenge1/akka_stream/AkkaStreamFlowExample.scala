package challenge1.akka_stream

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import externalLegacyCodeNotUnderOurControl.PriceService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Challenge 1: combining the results of "parallel" calls
  *
  * Akka Stream Example that uses Flows/maps to decompose functionality.
  * See 'Clean Code: same level of abstraction'.
  * Created by pascal.mengelt on 01.12.2016.
  */
object AkkaStreamFlowExample extends App {

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

  TimeUnit.SECONDS.sleep(10)
  system.terminate()

  // implementation of each step
  // Source
  private lazy val source = Source.fromIterator(() => (1 to serviceCount).iterator)
  // Flows
  private lazy val createPriceServices = Flow[Int].map(_ => new PriceService())
  private lazy val getPrices = Flow[PriceService].mapAsyncUnordered(serviceCount)(s => Future(s.getPrice))
  // Sink
  private lazy val collectPrices = Sink.seq[Int]

  // Post Process
  private def calcAverage(prices: Seq[Int]) = prices.sum / serviceCount

  private def printAverage(average: Int) = println(s"The average price is $average (${System.currentTimeMillis() - start} ms): " + Thread.currentThread().getName)

}
