package challenge1.akka_stream

import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import externalLegacyCodeNotUnderOurControl.PriceService

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Challenge 1: combining the results of "parallel" calls
  *
  * Example with a Graph. Interesting is that the Balancer does not automatically create Threads!
  * .async is needed - and even so only a few are created (way slower than the other solutions).
  * Created by pascal.mengelt on 29.11.2016.
  */
object AkkaStreamGraphExample extends App {

  implicit val system = ActorSystem("AkkaStreams")
  implicit val materializer = ActorMaterializer()
  val serviceCount = 20
  val start = System.currentTimeMillis()

  sourceGraph.runWith(Sink.seq)
    .map(m => m.sum / serviceCount)
    .foreach(price =>
      println(s"The average price is $price (${System.currentTimeMillis() - start} ms): " + Thread.currentThread().getName))


  lazy val sourceGraph: Source[Int, NotUsed] = Source.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._
    val nrOfWorker = serviceCount
    // create the parts of the graph
    val source = Source.fromIterator(() => (1 to serviceCount).iterator)
    val balancer = builder.add(Balance[Int](nrOfWorker))
    val flow: Flow[Int, Int, NotUsed] = Flow[Int].map(_ => new PriceService().getPrice)
    val merge = builder.add(Merge[Int](nrOfWorker))

    // construct the graph
    // @formatter:off
    source ~> balancer
    for (i <- 0 until nrOfWorker) {
      balancer ~> flow.async ~> merge
    }
    // @formatter:on
    SourceShape(merge.out)
  })

  TimeUnit.SECONDS.sleep(20)
  system.terminate()
}
