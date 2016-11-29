package scala_futures

import externalLegacyCodeNotUnderOurControl.PriceService

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

/**
  * Created by pascal.mengelt on 29.11.2016.
  */
object ScalaFutureExample extends App {

  val service = new PriceService()

  val price1 = Future(service.getPrice)
  val price2 = Future(service.getPrice)
  val price3 = Future(service.getPrice)

  Await.ready((for {
    a <- price1
    b <- price2
    c <- price3
  } yield (a + b + c) / 3)
    .map(price => println(s"The average price is $price")), 10 seconds)
}
