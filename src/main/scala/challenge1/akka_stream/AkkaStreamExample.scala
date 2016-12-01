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
  * Simple Akka Stream Example
  * Created by pascal.mengelt on 29.11.2016.
  */
object AkkaStreamExample extends App {

  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()
  val serviceCount = 20
  val start = System.currentTimeMillis()

  // create Price Services
  Source.fromIterator(() => (1 to serviceCount).iterator)
      .map(_ => new PriceService())
    // call services
    .mapAsync(serviceCount) (s => Future(s.getPrice))
    // collect the result
    .runWith(Sink.seq)
    // calc the average
    .map(_.sum / serviceCount)
    // print the result
    .foreach(price =>
      println(s"The average price is $price (${System.currentTimeMillis() - start} ms): " + Thread.currentThread().getName))

  TimeUnit.SECONDS.sleep(10)
  system.terminate()
}
