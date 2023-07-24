package com.gildedrose

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
        analytics(TestEvent("kumquat"))
        assertEquals(
            listOf("""{"timestamp":"2023-07-24T05:30:33.233518Z","event":{"value":"kumquat","eventName":"TestEvent"}}"""),
            logged
        )
    }
}

data class TestEvent(val value: String) : AnalyticsEvent
