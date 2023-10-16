package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.StockList
import com.gildedrose.http.ResponseErrors.withError
import com.gildedrose.persistence.StockListLoadingError
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.recover
import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.template.HandlebarsTemplates
import org.http4k.template.ViewModel
import org.http4k.template.viewModel
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
private val view = Body.viewModel(handlebars, ContentType.TEXT_HTML).toLens()

fun listHandler(
    clock: () -> Instant,
    zoneId: ZoneId,
    pricing: (Item) -> Price?,
    isPricingEnabled: Boolean,
    listing: (Instant) -> Result<StockList, StockListLoadingError>,
): HttpHandler = { _ ->
    val now = clock()
    val today = LocalDate.ofInstant(now, zoneId)
    listing(now).map { stockList ->
        Response(OK)
            .with(
                view of StockListViewModel(
                    now = dateFormat.format(today),
                    items = stockList.map { item -> item.toMap(today, pricing(item)) },
                    isPricingEnabled = isPricingEnabled
                )
            )
    }.recover { error ->
        Response(INTERNAL_SERVER_ERROR)
            .withError(error)
            .body("Something went wrong, we're really sorry.")
    }
}

private data class StockListViewModel(
    val now: String,
    val items: List<Map<String, String>>,
    val isPricingEnabled: Boolean,
) : ViewModel

private fun Item.toMap(now: LocalDate, price: Price?): Map<String, String> = mapOf(
    "id" to id.toString(),
    "name" to name.value,
    "sellByDate" to if (sellByDate == null) "" else dateFormat.format(sellByDate),
    "sellByDays" to daysUntilSellBy(now).toString(),
    "quality" to quality.toString(),
    "price" to (price?.toString() ?: "")
)

private fun Item.daysUntilSellBy(now: LocalDate): Long =
    if (sellByDate == null)
        0
    else
        ChronoUnit.DAYS.between(now, sellByDate)
