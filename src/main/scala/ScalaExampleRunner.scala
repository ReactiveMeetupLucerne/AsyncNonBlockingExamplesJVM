import challenge1.scala_futures.{ScalaFutureExample, ScalaFutureMoreCallsExample}
import org.scalameter.{Key, Quantity, Warmer, config}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by pascal.mengelt on 17.01.2017.
  */
object ScalaExampleRunner extends App {


  val standardConfig = config(
    Key.exec.minWarmupRuns -> 5,
    Key.exec.maxWarmupRuns -> 10,
    Key.exec.benchRuns -> 10,
    Key.verbose -> true
  ) withWarmer new Warmer.Default

  // challenge 1
  println("Challenge 1")
  run("Challenge 1: ScalaFutureExample", ScalaFutureExample().run)
  run("Challenge 1: ScalaFutureMoreCallsExample", ScalaFutureMoreCallsExample().run)

  private def run(label: String, runFunct: () => Future[Int]) {
    val seqtime: Quantity[Double] = standardConfig measure {
      val price = Await.result(runFunct(), 60 seconds)
      println(s"Price back by Runner: $price")
    }
    println(s">>>>>>>>>>>> $label: $seqtime")
  }


}
