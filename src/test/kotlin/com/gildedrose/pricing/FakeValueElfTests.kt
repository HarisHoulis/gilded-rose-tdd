package com.gildedrose.pricing

import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.Quality
import com.gildedrose.http.serverFor
import com.gildedrose.testItem
import com.natpryce.hamkrest.assertion.assertThat
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.hamkrest.hasStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.LocalDate

internal class FakeValueElfTests {

    private val uri = URI.create("http://localhost:8888/prices")
    private val item = testItem("banana", "doesn't matter", LocalDate.now(), 9)
    private val priceLookup = mutableMapOf<Pair<ID<Item>, Quality>, Price?>()
    private val routes = fakeValueElfRoutes { id, quality ->
        priceLookup[id to quality]
    }
    private val client = valueElfClient(uri, routes)

    @Disabled("Slows down tests")
    @Test
    fun `actually call server`() {
        val client: (Item) -> Price? = valueElfClient(uri)
        val item = testItem("banana", "doesn't matter", LocalDate.now(), 9)
        priceLookup[item.id to item.quality] = Price(609)
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

    @Test
    fun `price that exists`() {
        priceLookup[item.id to item.quality] = Price(609)
        assertEquals(Price(609), client(item))
    }

    @Test
    fun `no price`() {
        assertEquals(null, client(item))
    }

    @Test
    fun `returns BAD_REQUEST for invalid query strings`() {
        val baseRequest = Request(GET, uri.toString())
        assertThat(routes(baseRequest), hasStatus(BAD_REQUEST))
        assertThat(routes(baseRequest.query("id", "some-id")), hasStatus(BAD_REQUEST))
        assertThat(routes(baseRequest.query("id", "")), hasStatus(BAD_REQUEST))
        assertThat(
            routes(baseRequest.query("id", "some-id").query("quality", "")),
            hasStatus(BAD_REQUEST)
        )
        assertThat(
            routes(baseRequest.query("id", "some-id").query("quality", "nan")),
            hasStatus(BAD_REQUEST)
        )
        assertThat(
            routes(baseRequest.query("id", "some-id").query("quality", "-1")),
            hasStatus(BAD_REQUEST)
        )
    }
}

private fun fakeValueElfServer(port: Int, pricing: (ID<Item>, Quality) -> Price?) = serverFor(
    port,
    fakeValueElfRoutes(pricing)
)

