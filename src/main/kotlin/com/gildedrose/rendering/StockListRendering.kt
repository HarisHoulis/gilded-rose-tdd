package com.gildedrose.rendering

import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import com.gildedrose.http.ResponseErrors.withError
import com.gildedrose.persistence.StockListLoadingError
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.recover
import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.Response
import org.http4k.core.Status
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

fun render(
    stockListResult: Result<StockList, StockListLoadingError>,
    now: Instant,
    zoneId: ZoneId,
): Response {
    val today = LocalDate.ofInstant(now, zoneId)
    return stockListResult.map { stockList ->
        Response(Status.OK)
            .with(
                view of StockListViewModel(
                    now = dateFormat.format(today),
                    items = stockList.map { item ->
                        val priceString = when (val price = item.price) {
                            is Failure -> "error"
                            is Success -> price.value?.toString().orEmpty()
                            null -> ""
                        }
                        item.toMap(today, priceString)
                    },
                )
            )
    }.recover { error ->
        Response(Status.INTERNAL_SERVER_ERROR)
            .withError(error)
            .body("Something went wrong, we're really sorry.")
    }
}

private data class StockListViewModel(
    val now: String,
    val items: List<Map<String, String>>,
) : ViewModel

private fun Item.toMap(now: LocalDate, priceString: String): Map<String, String> = mapOf(
    "id" to id.toString(),
    "name" to name.value,
    "sellByDate" to if (sellByDate == null) "" else dateFormat.format(sellByDate),
    "sellByDays" to daysUntilSellBy(now).toString(),
    "quality" to quality.toString(),
    "price" to priceString
)

private fun Item.daysUntilSellBy(now: LocalDate): Long =
    if (sellByDate == null)
        0
    else
        ChronoUnit.DAYS.between(now, sellByDate)
