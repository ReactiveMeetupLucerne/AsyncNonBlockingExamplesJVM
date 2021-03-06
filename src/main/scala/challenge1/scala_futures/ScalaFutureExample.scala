package challenge1.scala_futures

import java.util.concurrent.TimeUnit

import externalLegacyCodeNotUnderOurControl.PriceService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

/**
  * Challenge 1: combining the results of "parallel" calls
  *
  * Simple example with fixed Services - using for comprehensions.
  * Created by pascal.mengelt on 29.11.2016.
  */
object ScalaFutureExample extends App {
  ScalaFutureExample().run()
  TimeUnit.SECONDS.sleep(10)
}

case class ScalaFutureExample() {

  def run(): Future[Int] = {
    // Starting all the services
    val price1 = Future(new PriceService().getPrice)
    val price2 = Future(new PriceService().getPrice)
    val price3: Future[Int] = Future(new PriceService().getPrice)
    // collect the results
    (for {
      a <- price1
      b <- price2
      c <- price3
    // calc average
    } yield (a + b + c) / 3)
      // print result
      .map { (price: Int) =>
      println(s"The average price is $price: " + Thread.currentThread().getName)
      price
    }
  }
}
