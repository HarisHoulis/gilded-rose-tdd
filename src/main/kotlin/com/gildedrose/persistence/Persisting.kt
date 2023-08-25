package com.gildedrose.persistence

import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
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

fun File.loadItems(): Result4k<StockList, Nothing?> = useLines { lines ->
    lines.toStockList()
}

fun Sequence<String>.toStockList(): Result4k<StockList, Nothing?> {
    val (header, body) = partition { it.startsWith("#") }
    val items: List<Item> = body.map { line -> line.toItem().onFailure { return Failure(null) } }
    return Success(
        StockList(
            lastModified = lastModifiedFrom(header) ?: Instant.EPOCH,
            items = items
        )
    )
}

private fun Item.toLine() = "$name\t${sellByDate ?: ""}\t$quality"

private fun lastModifiedFrom(header: List<String>): Instant? = header
    .lastOrNull { it.startsWith("# LastModified:") }
    ?.substring("# LastModified:".length)
    ?.trim()
    ?.toInstant()

private fun String.toInstant() = try {
    Instant.parse(this)
} catch (e: DateTimeParseException) {
    throw IOException("Could not parse LastModified header: ${e.message}")
}

private fun String.toItem(): Result4k<Item, Nothing?> {
    val parts = split("\t")
    return Item(
        name = parts[0],
        sellByDate = parts[1].toLocalDate(),
        quality = parts[2].toInt()
    )
}

private fun String.toLocalDate() =
    when (isBlank()) {
        true -> null
        false -> LocalDate.parse(this)
    }
