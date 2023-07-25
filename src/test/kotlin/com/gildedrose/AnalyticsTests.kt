package com.gildedrose

import org.http4k.filter.TraceId
import org.http4k.filter.ZipkinTraces
import org.http4k.filter.ZipkinTracesStorage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class AnalyticsTests {

    @Test
    fun `outputs json of the events`() {
        val logged = mutableListOf<String>()
        val now = Instant.parse("2023-07-24T05:30:33.233518Z")
        val analytics = LoggingAnalytics(
            logger = logged::add,
            clock = { now }
        )
        assertEquals(0, logged.size)

        withTraces(ZipkinTraces(TraceId("trace"), TraceId("span"), TraceId("parent"))) {
            analytics(TestEvent("kumquat"))
            assertEquals(
                listOf("""{"timestamp":"2023-07-24T05:30:33.233518Z","traceId":"trace","spanId":"span","parentSpanId":"parent","event":{"value":"kumquat","eventName":"TestEvent"}}"""),
                logged
            )
        }
    }
}

data class TestEvent(val value: String) : AnalyticsEvent

private fun withTraces(traces: ZipkinTraces, f: () -> Unit) {
    val oldTraces = ZipkinTracesStorage.THREAD_LOCAL.forCurrentThread()
    ZipkinTracesStorage.THREAD_LOCAL.setForCurrentThread(traces)
    try {
        f()
    } finally {
        ZipkinTracesStorage.THREAD_LOCAL.setForCurrentThread(oldTraces)
    }
}
