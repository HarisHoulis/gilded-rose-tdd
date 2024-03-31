package com.gildedrose.persistence

import com.gildedrose.com.gildedrose.persistence.InMemoryItems
import com.gildedrose.domain.StockList
import com.gildedrose.itemForTest
import com.gildedrose.march1
import dev.forkhandles.result4k.valueOrNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.Callable
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors

internal class StockTests {

    private val initialStockList = StockList(
        lastModified = Instant.parse("2023-03-13T23:59:59Z"),
        items = listOf(
            itemForTest("banana", march1.minusDays(1), 42),
            itemForTest("kumquat", march1.plusDays(1), 101)
        )
    )

    private val items = InMemoryItems(initialStockList)

    private val stock = Stock(
        items = items,
        zoneId = ZoneId.of("Europe/London"),
        itemUpdate = { days, _ -> copy(quality = quality - days) }
    )

    @Test
    fun `loads stock from file`() {
        val now = Instant.parse("2023-03-13T23:59:59Z")

        assertEquals(initialStockList, stock.stockList(now).valueOrNull())
    }

    @Test
    fun `updates stock if last modified date is yesterday`() {
        val now = Instant.parse("2023-03-14T00:00:01Z")
        val expected = StockList(
            lastModified = now,
            items = listOf(
                itemForTest("banana", march1.minusDays(1), 41),
                itemForTest("kumquat", march1.plusDays(1), 100)
            )
        )

        assertEquals(expected, stock.stockList(now).valueOrNull())
        assertEquals(expected, items.load().valueOrNull())
    }

    @Test
    fun `updates stock by two days if last modified date is before yesterday`() {
        val now = Instant.parse("2023-03-15T00:00:01Z")
        val expected = StockList(
            lastModified = now,
            items = listOf(
                itemForTest("banana", march1.minusDays(1), 40),
                itemForTest("kumquat", march1.plusDays(1), 99)
            )
        )

        assertEquals(expected, stock.stockList(now).valueOrNull())
        assertEquals(expected, items.load().valueOrNull())
    }

    @Test
    fun `does not update last modified date tomorrow`() {
        val now = Instant.parse("2023-03-12T00:00:01Z")

        assertEquals(initialStockList, stock.stockList(now).valueOrNull())
        assertEquals(initialStockList, items.load().valueOrNull())
    }

    @Test
    fun `parallel execution`() {
        val count = 8
        val executor = Executors.newFixedThreadPool(count)
        val barrier = CyclicBarrier(count)

        val futures = executor.invokeAll(
            (1..count).map {
                Callable {
                    barrier.await()
                    `updates stock if last modified date is yesterday`()
                }
            }
        )

        futures.forEach { it.get() }
    }

    @Test
    fun `sanity check`() {
        for (i in 1..10) {
            `parallel execution`()
        }
    }
}
