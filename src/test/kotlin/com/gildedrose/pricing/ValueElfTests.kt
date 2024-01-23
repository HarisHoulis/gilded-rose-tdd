package com.gildedrose.pricing

import com.gildedrose.domain.Price
import com.gildedrose.foundation.retry
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.resultFrom
import org.http4k.client.ApacheClient
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.net.URI

@EnabledIfSystemProperty(named = "run-external-tests", matches = "true")
internal class ValueElfTests : ValueElfContract(
    Fixture(
        uri = URI.create("http://value-elf.com:8080/prices"),
        handler = ApacheClient(),
        expectedPrice = Price(709)!!
    )
) {

    @Test
    fun Fixture.`fails sometimes`() {
        val result = (1..50).toList().stream().parallel().map {
            resultFrom {
                client.invoke(aFoundItem)
            }
        }.toList()
        val (successes, failures) = result.partition { it is Success }
        assertTrue(successes.all { it is Success && it.value == expectedPrice })
        val successRatio = successes.size / failures.size.toDouble()
        println("Successes = ${successes.size}, failures = ${failures.size}, ratio = $successRatio")
    }

    @Test
    fun Fixture.`retry prevents failure`() {
        val retryingClient = retry(retries = 1, function = client)
        val result = (1..50).map {
            resultFrom {
                retryingClient.invoke(aFoundItem)
            }
        }
        val (successes, failures) = result.partition { it is Success }
        assertTrue(successes.all { it is Success && it.value == expectedPrice })
        assertTrue(failures.isEmpty())
    }
}
