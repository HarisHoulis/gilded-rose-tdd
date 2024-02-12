package com.gildedrose.http

import com.gildedrose.com.gildedrose.testing.assertAll
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.test.assertEquals

internal class ReportHttpTransactionTests {

    private val events: MutableList<Any> = mutableListOf()
    private val filter = reportHttpTransactions(Duration.ofMillis(20)) { event ->
        events.add(event)
    }

    @Test
    fun `reports transactions to analytics`() {
        filter.then {
            Response(Status.OK)
        }.invoke(Request(Method.GET, "/"))
        assertAll(
            events.single() as HttpEvent,
            { assertEquals("/", uri) },
            { assertEquals(Method.GET.toString(), method) },
            { assertEquals(Status.OK.code, status) }
        )
    }

    @Test
    fun `reports slow transactions to analytics with an additional event`() {
        filter.then {
            Thread.sleep(25)
            Response(Status.OK)
        }.invoke(Request(Method.GET, "/"))
        assertAll(
            events.first() as HttpEvent,
            { assertEquals("/", uri) },
            { assertEquals(Method.GET.toString(), method) },
            { assertEquals(Status.OK.code, status) }
        )
        assertAll(
            events[1] as SlowHttpEvent,
            { assertEquals("/", uri) },
            { assertEquals(Method.GET.toString(), method) },
            { assertEquals(Status.OK.code, status) }
        )
    }
}
