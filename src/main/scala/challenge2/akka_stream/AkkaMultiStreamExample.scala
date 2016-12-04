package challenge2.akka_stream

import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import externalLegacyCodeNotUnderOurControl.PriceService

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, TimeoutException}
import scala.util.Random
import scala.concurrent.duration._

/**
  * Challenge 2: fallback in case of timeout
  *
  * Akka Stream Example
  * Created by pascal.mengelt on 29.11.2016.
  */
object AkkaMultiStreamExample extends App {

  implicit val system = ActorSystem("AkkaStreams")
  implicit val materializer = ActorMaterializer()
  val serviceCount = 20
  val start = System.currentTimeMillis()

  val serviceCalls = sources
    .map { (s: Source[PriceService, NotUsed]) =>
      s.via(getPrices)
        .runWith(Sink.head)
    }
  collectPrices
    .map(calcAverage)
    .foreach(printAverage)

  TimeUnit.SECONDS.sleep(20)
  system.terminate()

  // implementation of each step
  // Each Service is a Source
  private lazy val sources = for (_ <- 1 to serviceCount) yield Source.single(new PriceService(Random.nextInt(100) % 3))
  // Flows
  // changed with adding a Timeout
  private lazy val getPrices: Flow[PriceService, Int, NotUsed] =
  Flow[PriceService]
    .mapAsync(1)(s => Future(s.getPrice))
    .completionTimeout(2 seconds)
    .recoverWithRetries(-1, {
      // handle TimeoutException
      case ex: Exception =>
        println(s"[${Thread.currentThread().getName}] The special price is 42")
        Source.single(42)
    })

  // PostProcesses
  private lazy val collectPrices = Future.fold(serviceCalls)(List[Int]())((a, b) => b :: a)

  private def calcAverage(prices: Seq[Int]) = {
    println(s"called service: ${prices.size} > $prices")
    prices.sum / serviceCount
  }

  private def printAverage(average: Int) = println(s"The average price is $average (${System.currentTimeMillis() - start} ms): " + Thread.currentThread().getName)

}
