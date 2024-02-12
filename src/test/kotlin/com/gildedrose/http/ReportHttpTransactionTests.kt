package com.gildedrose.http

import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.Duration

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

        with(events.single() as HttpEvent) {
            assertAll(
                { assertEquals("/", uri) },
                { assertEquals(Method.GET.toString(), method) },
                { assertEquals(Status.OK.code, status) }
            )
        }
    }

    @Test
    fun `reports slow transactions to analytics`() {
        filter.then {
            Thread.sleep(25)
            Response(Status.OK)
        }.invoke(Request(Method.GET, "/"))

        with(events.first() as HttpEvent) {
            assertAll(
                { assertEquals("/", uri) },
                { assertEquals(Method.GET.toString(), method) },
                { assertEquals(Status.OK.code, status) }
            )
        }

        with(events[1] as SlowHttpEvent) {
            assertAll(
                { assertEquals("/", uri) },
                { assertEquals(Method.GET.toString(), method) },
                { assertEquals(Status.OK.code, status) }
            )
        }
    }
}
