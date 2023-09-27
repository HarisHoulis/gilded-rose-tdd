package com.gildedrose.pricing

import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.Quality
import com.gildedrose.http.serverFor
import com.gildedrose.testItem
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.LocalDate

internal class FakeValueElfTests {

    private val uri = URI.create("http://localhost:8888/prices")
    private val client: (Item) -> Price? = valueElfClientFor(uri)

    @Test
    fun test() {
        val item = testItem("banana", "doesn't matter", LocalDate.now(), 9)
        val priceLookup = mapOf(
            (item.id to item.quality) to Price(609)
        )
        val server = fakeValueElfServer(8888) { id, quality ->
            priceLookup[id to quality]
        }
        server.start().use {
            assertEquals(
                Price(609),
                client(item)
            )
        }
    }
}

private fun fakeValueElfServer(port: Int, pricing: (ID<Item>, Quality) -> Price?) = serverFor(
    port,
    routes(
        "/prices" bind Method.GET to { request ->
            val id: ID<Item> = ID(request.query("id")!!)!!
            val quality: Quality = Quality(request.query("quality")?.toInt()!!)!!
            val price = pricing(id, quality)
            if (price == null) TODO()
            Response(Status.OK).body(price.cents.toString())
        }
    )
)
