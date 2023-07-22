package com.gildedrose

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AnalyticsTests {

    @Test
    fun `outputs json of the events`() {
        val logged = mutableListOf<String>()
        val analytics: (Any) -> Unit = LoggingAnalytics(logged::add)

        assertEquals(0, logged.size)
        analytics(TestEvent("s")) 
        assertEquals(listOf("""{"value":"s","eventName":"TestEvent"}"""), logged)
    }
}

data class TestEvent(val value: String)  {
    val eventName: String
    get() = this::class.simpleName ?: "Event Name Unknown"
}

class LoggingAnalytics(
    private val logger: (String) -> Unit,
    private val objectMapper: ObjectMapper = ObjectMapper()
) : (Any) -> Unit {

    override fun invoke(event: Any) {
        logger(objectMapper.writeValueAsString(event))
    }
}
