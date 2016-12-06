package challenge2.scala_futures

import java.util.concurrent.TimeUnit

import externalLegacyCodeNotUnderOurControl.PriceService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.util.Random

/**
  * Challenge 2: fallback in case of timeout
  *
  * Version that uses a configurable number of services.
  * I used 20 to get some indication of speed.
  * Created by pascal.mengelt on 29.11.2016.
  */
object ScalaFutureMoreCallsExample extends App {

  val serviceCount = 20
  val start = System.currentTimeMillis()

  // create the Services
  val services = for (i <- 1 to serviceCount) yield new PriceService(Random.nextInt(4))
  // call the services with Timeout
  val serviceCalls = services.map(s =>
    Future.firstCompletedOf(Seq(
      Future(s.getPrice)
      , Future {
        TimeUnit.SECONDS.sleep(2)
        println(s"[${Thread.currentThread().getName}] The special price is 42")
        42
      })))
  // collect the results (from Seq[Future[Int]] to Future[Seq[Int]]
  val results = Future.fold(serviceCalls.toList)(List[Int]())((a: List[Int], b: Int) => b :: a)
  // calculate average
  val average = results.map { prices =>
    println(s"called service: ${prices.size} > $prices")
    prices.sum / serviceCount
  }
  // print result when finished
  average.foreach(price =>
    println(s"[${Thread.currentThread().getName}] The average price is $price (${System.currentTimeMillis() - start} ms)"))

  println("Did not block!")
  TimeUnit.SECONDS.sleep(20)
}
