package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.IOException
import java.time.Instant

class PersistenceTests {

    private val now = Instant.now()

    private val items = listOf(
        itemOf("banana", march1, 42),
        itemOf("peach", march1.plusDays(1), 100),
        itemOf("undated", null, 50)
    )

    @Test
    fun `save and load`(@TempDir dir: File) {
        val file = File(dir, "stock.tsv")

        val stockList = StockList(now, items)

        stockList.saveTo(file)

        assertEquals(
            stockList,
            file.loadItems()
        )
    }

    @Test
    fun `save and load empty stock list`() {
        val stockList = StockList(now, emptyList())

        assertEquals(
            stockList,
            stockList.toLines().toStockList()
        )
    }

    @Test
    fun `load from empty file`() {
        assertEquals(
            StockList(Instant.EPOCH, emptyList()),
            emptySequence<String>().toStockList()
        )
    }

    @Test
    fun `load with no last modified date`() {
        val lines = sequenceOf("# Banana")

        assertEquals(
            StockList(Instant.EPOCH, emptyList()),
            lines.toStockList()
        )
    }

    @Test
    fun `load with blank last modified date`() {
        val lines = sequenceOf("# LastModified:")

        try {
            lines.toStockList()
            fail("didn't throw")
        } catch (e: IOException) {
            assertEquals(
                "Could not parse LastModified header: Text '' could not be parsed at index 0",
                e.message
            )
        }
    }
}
