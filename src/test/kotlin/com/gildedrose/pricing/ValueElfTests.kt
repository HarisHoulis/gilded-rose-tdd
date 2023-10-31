package com.gildedrose.pricing

import com.gildedrose.domain.Price
import com.gildedrose.foundation.retry
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.resultFrom
import org.http4k.client.ApacheClient
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.net.URI

@Disabled("Talks to outside test resources")
internal class ValueElfTests : ValueElfContract(
    Fixture(
        uri = URI.create("http://value-elf.com:8080/prices"),
        handler = ApacheClient(),
        expectedPrice = Price(709)!!
    )
) {
    @Test
    fun Fixture.`retry prevents failure`() {
        val retryingClient = retry(retries = 1, function = client)
        val result = (1..500).map {
            resultFrom {
                retryingClient.invoke(aFoundItem)
            }
        }
        val (successes, failures) = result.partition { it is Success }
        assertTrue(successes.all { it is Success && it.value == expectedPrice })
        assertTrue(failures.isEmpty())
    }

}
