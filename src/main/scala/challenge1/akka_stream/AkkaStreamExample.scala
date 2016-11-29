package challenge1.akka_stream

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import externalLegacyCodeNotUnderOurControl.PriceService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AkkaStreamExample extends App {

  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()
  val serviceCount = 3

  Source.fromIterator(() => (1 to serviceCount).iterator)
    .mapAsync(3) { _ =>
      Future(new PriceService().getPrice)
    }.runFold(List[Double]())((a, b) => b :: a)
    .map(_.sum / serviceCount)
    .foreach(price => println(s"The average price is $price: " + Thread.currentThread().getName))

  TimeUnit.SECONDS.sleep(10)

}
