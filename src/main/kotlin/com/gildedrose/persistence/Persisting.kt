package com.gildedrose.persistence

import com.gildedrose.domain.Item
import com.gildedrose.domain.ItemCreationError
import com.gildedrose.domain.StockList
import com.gildedrose.persistence.StockListLoadingError.CouldntCreateItem
import com.gildedrose.persistence.StockListLoadingError.CouldntParseLastModified
import com.gildedrose.persistence.StockListLoadingError.CouldntParseQuality
import com.gildedrose.persistence.StockListLoadingError.CouldntParseSellByDate
import com.gildedrose.persistence.StockListLoadingError.IO
import com.gildedrose.persistence.StockListLoadingError.NotEnoughFields
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.result4k.onFailure
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException

fun StockList.saveTo(file: File) {
    file.writer().buffered().use { writer ->
        toLines().forEach(writer::appendLine)
    }
}

fun StockList.toLines(): Sequence<String> = sequenceOf("# LastModified: $lastModified") +
    items.map(Item::toLine)

fun File.loadItems(): Result4k<StockList, StockListLoadingError> =
    try {
        useLines { lines ->
            lines.toStockList()
        }
    } catch (x: IOException) {
        Failure(IO(x.message.toString()))
    }

fun Sequence<String>.toStockList(): Result4k<StockList, StockListLoadingError> {
    val (header, body) = partition { it.startsWith("#") }
    val items: List<Item> =
        body.map { line -> line.toItem().onFailure { return it } }
    return lastModifiedFrom(header).map { lastModified ->
        StockList(
            lastModified = lastModified ?: Instant.EPOCH,
            items = items
        )
    }
}

private fun Item.toLine() = "$name\t${sellByDate ?: ""}\t$quality"

private fun lastModifiedFrom(header: List<String>): Result<Instant?, CouldntParseLastModified> =
    header
        .lastOrNull { it.startsWith("# LastModified:") }
        ?.substring("# LastModified:".length)
        ?.trim()
        ?.toInstant() ?: Success(null)

private fun String.toInstant(): Result<Instant, CouldntParseLastModified> =
    try {
        Success(Instant.parse(this))
    } catch (x: DateTimeParseException) {
        Failure(CouldntParseLastModified("Could not parse LastModified header: ${x.message}"))
    }

private fun String.toItem(): Result4k<Item, StockListLoadingError> {
    val parts = split("\t")
    if (parts.size < 3)
        return Failure(NotEnoughFields(this))
    val quality = parts[2].toIntOrNull() ?: return Failure(CouldntParseQuality(this))
    val sellByDate = parts[1].toLocalDate(this).onFailure { return it }
    return Item(
        name = parts[0],
        sellByDate = sellByDate,
        quality = quality
    ).mapFailure {
        CouldntCreateItem(it)
    }
}

private fun String.toLocalDate(line: String): Result<LocalDate?, CouldntParseSellByDate> =
    try {
        Success(if (isBlank()) null else LocalDate.parse(this))
    } catch (x: DateTimeParseException) {
        Failure(CouldntParseSellByDate(line))
    }

sealed interface StockListLoadingError {
    data class CouldntParseLastModified(val message: String) : StockListLoadingError
    data class CouldntCreateItem(val reason: ItemCreationError) : StockListLoadingError
    data class NotEnoughFields(val line: String) : StockListLoadingError
    data class CouldntParseQuality(val line: String) : StockListLoadingError
    data class CouldntParseSellByDate(val line: String) : StockListLoadingError
    data class IO(val message: String) : StockListLoadingError
}
