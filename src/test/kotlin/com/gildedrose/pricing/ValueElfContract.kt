package com.gildedrose.pricing

import com.gildedrose.domain.Price
import com.gildedrose.pricing.FixtureResolver.FixtureSource
import com.gildedrose.testItem
import com.natpryce.hamkrest.assertion.assertThat
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.hamkrest.hasStatus
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import java.net.URI
import java.time.LocalDate

@ExtendWith(FixtureResolver::class)
abstract class ValueElfContract(
    override val fixture: Fixture,
) : FixtureSource {

    data class Fixture(
        val uri: URI,
        val handler: HttpHandler,
        val expectedPrice: Price,
    ) {
        val aUri: URI = URI.create("http://localhost:8888/prices")
        val aFoundItem = testItem("banana", "doesn't matter", LocalDate.now(), 9)
        val aNotFoundItem = testItem("not-such", "doesn't matter", LocalDate.now(), 9)

        val client = valueElfClient(uri, handler)
    }

    @Test
    fun Fixture.`returns price when there is one`() {
        Assertions.assertEquals(expectedPrice, this.client(aFoundItem))
    }

    @Test
    fun Fixture.`returns null when no price`() {
        Assertions.assertEquals(null, this.client(aNotFoundItem))
    }

    @Test
    fun Fixture.`returns BAD_REQUEST for invalid query strings`() {
        val baseRequest = Request(Method.GET, aUri.toString())
        assertThat(this.handler(baseRequest), hasStatus(Status.BAD_REQUEST))
        baseRequest.query("id", "some-id")
        assertThat(this.handler(baseRequest.query("id", "some-id")), hasStatus(Status.BAD_REQUEST))
        baseRequest.query("id", "")
        assertThat(this.handler(baseRequest.query("id", "")), hasStatus(Status.BAD_REQUEST))
        baseRequest.query("id", "some-id").query("quality", "")
        assertThat(
            this.handler(baseRequest.query("id", "some-id").query("quality", "")),
            hasStatus(Status.BAD_REQUEST)
        )
        baseRequest.query("id", "some-id").query("quality", "nan")
        assertThat(
            this.handler(baseRequest.query("id", "some-id").query("quality", "nan")),
            hasStatus(Status.BAD_REQUEST)
        )
        baseRequest.query("id", "some-id").query("quality", "-1")
        assertThat(
            this.handler(baseRequest.query("id", "some-id").query("quality", "-1")),
            hasStatus(Status.BAD_REQUEST)
        )
    }
}

class FixtureResolver : ParameterResolver {

    interface FixtureSource {
        val fixture: Any
    }

    override fun supportsParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext,
    ) = true

    override fun resolveParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext,
    ): Any {
        return (extensionContext.requiredTestInstance as? FixtureSource)?.fixture
            ?: error("Test is not a FixtureSource")
    }

}
