package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.itemForTest
import com.gildedrose.march1
import com.gildedrose.persistence.StockListLoadingError.BlankID
import com.gildedrose.persistence.StockListLoadingError.BlankName
import com.gildedrose.persistence.StockListLoadingError.CouldntParseLastModified
import com.gildedrose.persistence.StockListLoadingError.CouldntParseQuality
import com.gildedrose.persistence.StockListLoadingError.CouldntParseSellByDate
import com.gildedrose.persistence.StockListLoadingError.NotEnoughFields
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.time.Instant

class PersistenceTests {

    private val now = Instant.now()

    private val items = listOf(
        itemForTest("banana", march1, 42),
        itemForTest("peach", march1.plusDays(1), 100),
        itemForTest("undated", null, 50)
    )

    @Test
    fun `save and load`(@TempDir dir: File) {
        val file = File(dir, "stock.tsv")

        val stockList = StockList(now, items)

        stockList.saveTo(file)

        assertEquals(
            Success(stockList),
            file.loadItems()
        )
    }

    @Test
    fun `save and load empty stock list`() {
        val stockList = StockList(now, emptyList())

        assertEquals(
            Success(stockList),
            stockList.toLines().toStockList()
        )
    }

    @Test
    fun `load from empty file`() {
        assertEquals(
            Success(StockList(Instant.EPOCH, emptyList())),
            emptySequence<String>().toStockList()
        )
    }

    @Test
    fun `load with no last modified header`() {
        val lines = sequenceOf("# Banana")

        assertEquals(
            Success(StockList(Instant.EPOCH, emptyList())),
            lines.toStockList()
        )
    }

    @Test
    fun `fails to load with blank LastModified header`() {
        assertEquals(
            Failure(CouldntParseLastModified("Could not parse LastModified header: Text '' could not be parsed at index 0")),
            sequenceOf("# LastModified:").toStockList()
        )
    }

    @Test
    fun `fails to load with negative quality`() {
        assertEquals(
            Failure(CouldntParseQuality("B1\tbanana\t2023-08-26\t-1")),
            sequenceOf("B1\tbanana\t2023-08-26\t-1").toStockList()
        )
    }

    @Test
    fun `fails to load with blank name`() {
        assertEquals(
            Failure(BlankName("id\t\t2023-08-26\t42")),
            sequenceOf("id\t\t2023-08-26\t42").toStockList()
        )
    }

    @Test
    fun `fails to load with blank id`() {
        assertEquals(
            Failure(BlankID("\tbanana\t2023-08-26\t42")),
            sequenceOf("\tbanana\t2023-08-26\t42").toStockList()
        )
    }

    @Test
    fun `fails to load with too few fields`() {
        assertEquals(
            Failure(NotEnoughFields("B1\tbanana\t2023-08-26")),
            sequenceOf("B1\tbanana\t2023-08-26").toStockList()
        )
    }

    @Test
    fun `fails to load with no quality`() {
        assertEquals(
            Failure(CouldntParseQuality("B1\tbanana\t2023-08-26\t")),
            sequenceOf("B1\tbanana\t2023-08-26\t").toStockList()
        )
    }

    @Test
    fun `fails to load with duff quality`() {
        assertEquals(
            Failure(CouldntParseQuality("B1\tbanana\t2023-08-26\teh?")),
            sequenceOf("B1\tbanana\t2023-08-26\teh?").toStockList()
        )
    }

    @Test
    fun `fails to load with bad sellByDate`() {
        assertEquals(
            Failure(CouldntParseSellByDate("B1\tbanana\teh?\t42")),
            sequenceOf("B1\tbanana\teh?\t42").toStockList()
        )
    }
}
