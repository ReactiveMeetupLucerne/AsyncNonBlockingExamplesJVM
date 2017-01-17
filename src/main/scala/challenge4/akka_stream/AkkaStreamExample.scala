package challenge4.akka_stream

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import externalLegacyCodeNotUnderOurControl.PriceService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
/**
  * Challenge 4: request collapsing
  *
  * Akka Stream Example.
  * Created by pascal.mengelt on 06.12.2016.
  */
object AkkaStreamExample extends App {

  implicit val system = ActorSystem("AkkaStreams")
  implicit val materializer = ActorMaterializer()
  val serviceCount = 20
  val start = System.currentTimeMillis()


  // self documenting process
 /* callSource
    .via(callPriceServices)
    .via(getPrices)
    .runWith(collectPrices)
    .map(calcAverage)
    .foreach(printAverage)
*/
  TimeUnit.SECONDS.sleep(10)
  system.terminate()

  // implementation of each step
  // Source
  private lazy val callSource = Source.tick(0 seconds, 2 seconds, "run")

  // Flows
  private lazy val callPriceServices = Flow[String].map(_ =>for (i <- 1 to serviceCount) yield "getPrice")
  private lazy val getPrices = Flow[PriceService].mapAsyncUnordered(serviceCount)(s => Future(s.getPrice))
  // Sink
  private lazy val collectPrices = Sink.seq[Int]

  // Post Process
  private def calcAverage(prices: Seq[Int]) = prices.sum / serviceCount

  private def printAverage(average: Int) = println(s"The average price is $average (${System.currentTimeMillis() - start} ms): " + Thread.currentThread().getName)

}
