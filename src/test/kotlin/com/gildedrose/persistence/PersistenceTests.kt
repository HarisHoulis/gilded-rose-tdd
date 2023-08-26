package com.gildedrose.persistence

import com.gildedrose.domain.ItemCreationError.BlankName
import com.gildedrose.domain.ItemCreationError.NegativeQuality
import com.gildedrose.domain.StockList
import com.gildedrose.march1
import com.gildedrose.persistence.StockListLoadingError.CouldntCreateItem
import com.gildedrose.persistence.StockListLoadingError.CouldntParseLastModified
import com.gildedrose.persistence.StockListLoadingError.CouldntParseQuality
import com.gildedrose.persistence.StockListLoadingError.CouldntParseSellByDate
import com.gildedrose.persistence.StockListLoadingError.NotEnoughFields
import com.gildedrose.testItem
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
        testItem("banana", march1, 42),
        testItem("peach", march1.plusDays(1), 100),
        testItem("undated", null, 50)
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
            Failure(CouldntCreateItem(NegativeQuality(-1))),
            sequenceOf("banana\t2023-08-26\t-1").toStockList()
        )
    }

    @Test
    fun `fails to load with blank name`() {
        assertEquals(
            Failure(CouldntCreateItem(BlankName)),
            sequenceOf("\t2023-08-26\t42").toStockList()
        )
    }

    @Test
    fun `fails to load with too few fields`() {
        assertEquals(
            Failure(NotEnoughFields("banana\t2023-08-26")),
            sequenceOf("banana\t2023-08-26").toStockList()
        )
    }

    @Test
    fun `fails to load with no quality`() {
        assertEquals(
            Failure(CouldntParseQuality("banana\t2023-08-26\t")),
            sequenceOf("banana\t2023-08-26\t").toStockList()
        )
    }

    @Test
    fun `fails to load with duff quality`() {
        assertEquals(
            Failure(CouldntParseQuality("banana\t2023-08-26\teh?")),
            sequenceOf("banana\t2023-08-26\teh?").toStockList()
        )
    }

    @Test
    fun `fails to load with bad sellByDate`() {
        assertEquals(
            Failure(CouldntParseSellByDate("banana\teh?\t42")),
            sequenceOf("banana\teh?\t42").toStockList()
        )
    }
}
