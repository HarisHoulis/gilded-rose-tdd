package com.gildedrose

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.http4k.filter.ZipkinTracesStorage
import java.time.Instant

typealias Analytics = (AnalyticsEvent) -> Unit

@Suppress("unused")
interface AnalyticsEvent {
    val eventName: String get() = this::class.simpleName ?: "Event Name Unknown"
}

class LoggingAnalytics(
    private val logger: (String) -> Unit,
    private val objectMapper: ObjectMapper = loggingObjectMapper(),
    private val clock: () -> Instant = Instant::now
) : Analytics {

    override fun invoke(event: AnalyticsEvent) {
        val traces = ZipkinTracesStorage.THREAD_LOCAL.forCurrentThread()
        val envelope = Envelope(
            clock(),
            traces.traceId.value,
            traces.spanId.value,
            traces.parentSpanId?.value,
            event
        )
        logger(objectMapper.writeValueAsString(envelope))
    }

    @Suppress("unused")
    class Envelope(
        val timestamp: Instant,
        val traceId: String,
        val spanId: String,
        val parentSpanId: String?,
        val event: AnalyticsEvent
    )
}

fun loggingObjectMapper(): ObjectMapper = ObjectMapper().apply {
    registerModule(ParameterNamesModule())
    registerModule(Jdk8Module())
    registerModule(JavaTimeModule())
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
}
