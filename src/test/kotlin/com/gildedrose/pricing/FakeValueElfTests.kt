package com.gildedrose.pricing

import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.Quality
import com.gildedrose.http.serverFor
import com.gildedrose.itemForTest
import com.gildedrose.pricing.ValueElfContract.Fixture
import org.http4k.client.ApacheClient
import org.http4k.core.Response
import org.http4k.core.Status
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.LocalDate

private val baseFixture = Fixture(
    uri = URI.create("http://localhost:8888/prices"),
    handler = { Response(Status.I_M_A_TEAPOT) },
    expectedPrice = Price(609)!!
)

private val priceLookup: Map<Pair<ID<Item>, Quality>, Price?> = mapOf(
    (baseFixture.aFoundItem.id to baseFixture.aFoundItem.quality) to baseFixture.expectedPrice,
    (baseFixture.aNotFoundItem.id to baseFixture.aNotFoundItem.quality) to null,
)

internal class FakeValueElfTests : ValueElfContract(
    baseFixture.copy(handler = fakeValueElfRoutes { id, quality ->
        priceLookup[id to quality]
    })
)

@Disabled("Slows down tests")
internal class FakeValueElfHttpTests :
    ValueElfContract(baseFixture.copy(handler = ApacheClient())) {

    companion object {
        val server = fakeValueElfServer(8888) { id, quality ->
            priceLookup[id to quality]
        }
    }

    @Test
    fun `actually call server`() {
        val client: (Item) -> Price? = valueElfClient(baseFixture.aUri)
        val item = itemForTest("banana", "doesn't matter", LocalDate.now(), 9)

        server.start().use {
            Assertions.assertEquals(
                Price(609),
                client(item)
            )
        }
    }
}

private fun fakeValueElfServer(port: Int, pricing: (ID<Item>, Quality) -> Price?) = serverFor(
    port,
    fakeValueElfRoutes(pricing)
)

