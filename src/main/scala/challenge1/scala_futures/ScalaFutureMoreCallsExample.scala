package challenge1.scala_futures

import java.util.concurrent.TimeUnit

import externalLegacyCodeNotUnderOurControl.PriceService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

/**
  * Challenge 1: combining the results of "parallel" calls
  *
  * Version that uses a configurable number of services.
  * I used 20 to get some indication of speed.
  * Created by pascal.mengelt on 29.11.2016.
  */
object ScalaFutureMoreCallsExample extends App {

  ScalaFutureMoreCallsExample() run();
  println("Did not block!")
  TimeUnit.SECONDS.sleep(10)
}

case class ScalaFutureMoreCallsExample() {

  def run(): Future[Int] = {
    val serviceCount = 20
    val start = System.currentTimeMillis()

    // create the Services
    val services = for (i <- 1 to serviceCount) yield new PriceService()
    // call the services
    val serviceCalls = services.map(s => Future(s.getPrice))
    // collect the results (from Seq[Future[Int]] to Future[Seq[Int]]
    val results = Future.fold(serviceCalls.toList)(List[Int]())((a: List[Int], b: Int) => b :: a)
    // calculate average
    val average: Future[Int] = results.map(_.sum / serviceCount)
    // print result when finished
    average.foreach(price =>
      println(s"The average price is $price (${System.currentTimeMillis() - start} ms): " + Thread.currentThread().getName))
    average
  }
}
