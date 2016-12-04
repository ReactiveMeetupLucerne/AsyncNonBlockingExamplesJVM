package challenge3.akka_actor

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorSystem, Props}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

/**
  * Challenge 3: stream of temperature values
  *
  * Simple Akka Actor Example.
  *
  * Created by pascal.mengelt on 04.12.2016.
  */
object AkkaActorExample extends App {

  case object PushTemperature

  case object PrintMinMax

  case class Temp(amount: Int)

  implicit val system = ActorSystem("AkkaActor")
  implicit val executor = system.dispatcher

  val tempSource = system.actorOf(Props[TempSource], "TempSource")
  val tempHandler = system.actorOf(Props[TempHandler], "TempHandler")

  val serviceCount = 20
  val start = System.currentTimeMillis()
  // create Schedulers
  val scheduler = system.scheduler
  // schedule to push the temperatures
  scheduler.schedule(
    initialDelay = 1 seconds
    , interval = 2 seconds
    , receiver = tempSource
    , PushTemperature)
  // schedule to print min-/ max-temperatures
  scheduler.schedule(
    initialDelay = 10 seconds
    , interval = 10 seconds
    , receiver = tempHandler
    , PrintMinMax)

  // the source that sends a random Temperature whenever triggered by the scheduler
  class TempSource extends Actor {

    def receive: PartialFunction[Any, Unit] = {
      case PushTemperature => tempHandler ! Temp(Random.nextInt(123))
    }
  }

  class TempHandler extends Actor {
    // keeps track of the min- /max Value
    var minTemp = Int.MaxValue
    var maxTemp = Int.MinValue

    def receive: PartialFunction[Any, Unit] = {
      case Temp(amount) =>
        println(s"[${Thread.currentThread().getName}] Temp received $amount")
        if(amount < minTemp) minTemp = amount
        if(amount > maxTemp) maxTemp = amount
      case PrintMinMax =>
        println(s"[${Thread.currentThread().getName}] The min. temperature is $minTemp and the max. is $maxTemp")
    }
  }

  println("Did not block!")
  TimeUnit.SECONDS.sleep(60)
  system.terminate()
}
