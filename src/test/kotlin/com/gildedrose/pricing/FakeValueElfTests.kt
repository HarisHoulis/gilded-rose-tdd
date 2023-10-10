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

private val aUri = URI.create("http://localhost:8888/prices")
private val anItem = testItem("banana", "doesn't matter", LocalDate.now(), 9)
private val aNotFoundItem = testItem("not-such", "doesn't matter", LocalDate.now(), 9)

internal class FakeValueElfTests {

    private val priceLookup = mapOf(
        (anItem.id to anItem.quality) to Price(609),
        (aNotFoundItem.id to aNotFoundItem.quality) to null,
    )
    private val routes = fakeValueElfRoutes { id, quality ->
        priceLookup[id to quality]
    }
    private val client = valueElfClient(aUri, routes)

    @Test
    fun `returns price that does exist`() {
        assertEquals(Price(609), client(anItem))
    }

    @Test
    fun `returns null for no price`() {
        assertEquals(null, client(aNotFoundItem))
    }

    @Test
    fun `returns BAD_REQUEST for invalid query strings`() {
        val baseRequest = Request(GET, aUri.toString())
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

    @Disabled("Slows down tests")
    @Test
    fun `actually call server`() {
        val client: (Item) -> Price? = valueElfClient(aUri)
        val item = testItem("banana", "doesn't matter", LocalDate.now(), 9)
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
    fakeValueElfRoutes(pricing)
)

