package challenge1.coroutines

import externalLegacyCodeNotUnderOurControl.PriceService
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking

/**
 * This example uses Kotlin coroutines.
 *
 * @author Marcus Fihlon, www.fihlon.ch
 */

fun main(args: Array<String>): Unit = runBlocking {

    val nrOfPrices = 10

    val jobs = List(nrOfPrices) {
        async(CommonPool) {
            PriceService().price
        }
    }

    println(
            jobs.sumBy { it.await() } / nrOfPrices
    )

}
