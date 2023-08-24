package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.march1
import com.gildedrose.testItem
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
