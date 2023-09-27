package com.gildedrose.pricing

import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.testItem
import org.http4k.client.ApacheClient
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.Status.Companion.NOT_FOUND
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.LocalDate

@Disabled
internal class ValueElfTests {

    private val uri = URI.create("http://value-elf.com:8080/prices")
    private val client: (Item) -> Price? = valueElfClientFor(uri)

    @Test
    fun `returns price when there is one`() {
        val item = testItem("banana", "doesn't matter", LocalDate.now(), 9)
        assertEquals(
            Price(609),
            client(item)
        )
    }

    @Test
    fun `returns null when no price`() {
        val item = testItem("no-such", "doesn't matter", LocalDate.now(), 9)
        assertEquals(
            null,
            client(item)
        )
    }
}

fun valueElfClientFor(uri: URI): (Item) -> Price? {
    val client: HttpHandler = ApacheClient()
    return { item ->
        val request = Request(Method.GET, uri.toString())
            .query("id", item.id.toString())
            .query("quality", item.quality.toString())
        val response = client.invoke(request)
        when (response.status) {
            in listOf(NOT_FOUND) -> null
            Status.OK -> Price(response.bodyString().toLong())
            else -> error("Unexpected API response ${response.status}")
        }
    }
}
