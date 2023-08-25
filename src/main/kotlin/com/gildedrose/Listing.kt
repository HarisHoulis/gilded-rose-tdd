package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.onFailure
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.template.HandlebarsTemplates
import org.http4k.template.ViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
    .withLocale(Locale.UK)
private val handlebars = HandlebarsTemplates().HotReload("src/main/kotlin")

fun listHandler(
    clock: () -> Instant,
    zoneId: ZoneId,
    listing: (Instant) -> Result<StockList, Nothing?>,
): HttpHandler = { _ ->
    val now = clock()
    val today = LocalDate.ofInstant(now, zoneId)
    val stockList = listing(now).onFailure { error("Could not load stock") }
    Response(OK).body(handlebars(
        StockListViewModel(
            now = dateFormat.format(today),
            items = stockList.map { it.toMap(today) }
        )
    ))
}

private data class StockListViewModel(
    val now: String,
    val items: List<Map<String, String>>,
) : ViewModel

private fun Item.toMap(now: LocalDate): Map<String, String> = mapOf(
    "name" to name,
    "sellByDate" to if (sellByDate == null) "" else dateFormat.format(sellByDate),
    "sellByDays" to daysUntilSellBy(now).toString(),
    "quality" to quality.toString()
)

private fun Item.daysUntilSellBy(now: LocalDate): Long =
    if (sellByDate == null)
        0
    else
        ChronoUnit.DAYS.between(now, sellByDate)
