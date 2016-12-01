package challenge2.scala_futures

import java.util.concurrent.TimeUnit

import externalLegacyCodeNotUnderOurControl.PriceService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

/**
  * Challenge 2: fallback in case of timeout
  *
  * Simple example with fixed Services - using for comprehensions.
  * Created by pascal.mengelt on 29.11.2016.
  */
object ScalaFutureExample extends App {

  // Starting all the services
  val price1 = runService(1)
  val price2 = runService(3)
  val price3 = runService(1)

  // collect the results
  (for {
    a <- price1
    b <- price2
    c <- price3
  // calc average
  } yield (a + b + c) / 3)
    // print result
    .foreach(price => println(s"[${Thread.currentThread().getName}] The average price is $price"))

  TimeUnit.SECONDS.sleep(10)

  private def runService(waitFor: Int): Future[Int] = Future.firstCompletedOf(Seq(Future(new PriceService(waitFor).getPrice),
    Future {
      TimeUnit.SECONDS.sleep(2)
      println(s"[${Thread.currentThread().getName}] The special price is 42")
      42
    }))
}
