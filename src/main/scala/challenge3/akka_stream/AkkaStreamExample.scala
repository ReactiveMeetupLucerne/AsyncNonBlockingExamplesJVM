package challenge3.akka_stream

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream._
import akka.stream.scaladsl._

import scala.concurrent.duration._
import scala.util.Random

/**
  * Challenge 3: stream of temperature values
  *
  * Simple Akka Stream Example
  * Created by pascal.mengelt on 29.11.2016.
  */
object AkkaStreamExample extends App {

  implicit val system = ActorSystem("AkkaStreams")
  implicit val materializer = ActorMaterializer()
  val serviceCount = 20
  val start = System.currentTimeMillis()

  val tempHandler = system.actorOf(Props[TempHandler], "TempHandler")

  // self documenting processes
  tempSource
    .via(createTemperature)
    .via(pushTemperature)
    .runWith(Sink.ignore)

  printSource
    .via(pushPrintTemperature)
    .runWith(Sink.ignore)

  TimeUnit.SECONDS.sleep(120)
  system.terminate()

  // implementation of each step
  // Sources
  private lazy val tempSource = Source.tick(1 seconds, 2 seconds, "temp")
  private lazy val printSource = Source.tick(10 seconds, 10 seconds, "print")

  // Flows
  private lazy val createTemperature = Flow[String].map(_ => Random.nextInt(123))
  private lazy val pushTemperature = Flow[Int].map(temp => tempHandler ! Temp(temp))
  private lazy val pushPrintTemperature = Flow[String].map(_ => tempHandler ! PrintMinMax)

  // Akka Actor for handling the mutable state and the parallel Streams.
  // same as with challenge3.akka_actor.AkkaActorExample
  case object PrintMinMax

  case class Temp(amount: Int)

  class TempHandler extends Actor {
    // keeps track of the min- /max Value
    var minTemp = Int.MaxValue
    var maxTemp = Int.MinValue

    def receive: PartialFunction[Any, Unit] = {
      case Temp(amount) =>
        println(s"[${Thread.currentThread().getName}] Temp received $amount")
        if (amount < minTemp) minTemp = amount
        if (amount > maxTemp) maxTemp = amount
      case PrintMinMax =>
        println(s"[${Thread.currentThread().getName}] The min. temperature is $minTemp and the max. is $maxTemp")
    }
  }

}
