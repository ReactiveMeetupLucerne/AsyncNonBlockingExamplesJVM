package challenge1.scala_futures

import java.util.concurrent.TimeUnit

import externalLegacyCodeNotUnderOurControl.PriceService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

/**
  * Created by pascal.mengelt on 29.11.2016.
  */
object ScalaFutureExample extends App {

  val price1 = Future(new PriceService().getPrice)
  val price2 = Future(new PriceService().getPrice)
  val price3 = Future(new PriceService().getPrice)

  (for {
    a <- price1
    b <- price2
    c <- price3
  } yield (a + b + c) / 3)
    .foreach(price => println(s"The average price is $price: " + Thread.currentThread().getName))

  TimeUnit.SECONDS.sleep(10)
}
