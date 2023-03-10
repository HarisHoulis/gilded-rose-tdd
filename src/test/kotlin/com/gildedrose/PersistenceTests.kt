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
        Item("banana", march1, 42u),
        Item("peach", march1.plusDays(1), 100u),
    )

    @Test
    fun `save and load`(@TempDir dir: File) {
        val file = File(dir, "stock.tsv")

        val stockList = StockList(now, items)

        stockList.saveTo(file)

        assertEquals(
            stockList,
            file.loadItems(defaultLastModified = now.plusSeconds(3600))
        )
    }

    @Test
    fun `save and load empty`() {
        val stockList = StockList(now, emptyList())

        assertEquals(
            stockList,
            stockList.toLines().toStockList(defaultLastModified = now.plusSeconds(3600))
        )
    }

    @Test
    fun `load with no last modified date`() {
        val lines = sequenceOf("# Banana")

        assertEquals(
            StockList(now, emptyList()),
            lines.toStockList(defaultLastModified = now)
        )
    }

    @Test
    fun `load with blank last modified date`() {
        val lines = sequenceOf("# LastModified:")

        try {
            lines.toStockList(defaultLastModified = now)
            fail("didn't throw")
        } catch (e: IOException) {
            assertEquals(
                "Could not parse LastModified header: Text '' could not be parsed at index 0",
                e.message
            )
        }
    }

    @Test
    fun `load legacy file`(@TempDir dir: File) {
        val file = File(dir, "stock.tsv")

        items.legacySaveTo(file)

        assertEquals(
            StockList(now, items),
            file.loadItems(now)
        )
    }

}

private fun List<Item>.legacySaveTo(file: File) {
    fun Item.toLine() = "$name\t$sellByDate\t$quality"
    file.writer().buffered().use { writer ->
        forEach { item ->
            writer.appendLine(item.toLine())
        }
    }
}
