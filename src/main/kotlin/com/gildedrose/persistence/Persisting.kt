package com.gildedrose.persistence

import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.NonBlankString
import com.gildedrose.domain.Quality
import com.gildedrose.domain.StockList
import com.gildedrose.persistence.StockListLoadingError.BlankID
import com.gildedrose.persistence.StockListLoadingError.BlankName
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
import dev.forkhandles.result4k.onFailure
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException

private const val LAST_MODIFIED_HEADER = "# LastModified:"

@Throws(IOException::class)
fun StockList.saveTo(file: File) {
    file.writer().buffered().use { writer ->
        toLines().forEach(writer::appendLine)
    }
}

fun StockList.toLines(): Sequence<String> = sequenceOf("$LAST_MODIFIED_HEADER $lastModified") +
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

private fun Item.toLine() = "$id\t${name.value}\t${sellByDate ?: ""}\t$quality"

private fun lastModifiedFrom(header: List<String>): Result<Instant?, CouldntParseLastModified> =
    header
        .lastOrNull { it.startsWith(LAST_MODIFIED_HEADER) }
        ?.substring(LAST_MODIFIED_HEADER.length)
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
    return when {
        parts.size < 4 -> Failure(NotEnoughFields(this))
        else -> itemWithIdFrom(parts)
    }
}

private fun String.itemWithIdFrom(parts: List<String>): Result<Item, StockListLoadingError> {
    val id = ID<Item>(parts[0]) ?: return Failure(BlankID(this))
    val name = NonBlankString(parts[1]) ?: return Failure(BlankName(this))
    val sellByDate = parts[2].toLocalDate(this).onFailure { return it }
    val quality = parts[3].toIntOrNull()?.let { Quality(it) } ?: return Failure(
        CouldntParseQuality(this)
    )
    return Success(Item(id = id, name = name, sellByDate = sellByDate, quality = quality))
}

private fun String.toLocalDate(line: String): Result<LocalDate?, CouldntParseSellByDate> =
    try {
        Success(if (isBlank()) null else LocalDate.parse(this))
    } catch (x: DateTimeParseException) {
        Failure(CouldntParseSellByDate(line))
    }
