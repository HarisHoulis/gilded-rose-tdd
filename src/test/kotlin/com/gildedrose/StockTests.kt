package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.Callable
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors

class StockTests {

    private val initialStockList = StockList(
        lastModified = Instant.parse("2023-03-13T23:59:59Z"),
        items = listOf(
            itemOf("banana", march1.minusDays(1), 42),
            itemOf("kumquat", march1.plusDays(1), 101)
        )
    )
    private val fixture = Fixture(initialStockList, now = initialStockList.lastModified)
    private val stock = Stock(
        stockFile = fixture.stockFile,
        zoneId = ZoneId.of("Europe/London"),
        update = ::simpleUpdateItems
    )

    @Test
    fun `loads stock from file`() {
        val now = Instant.parse("2023-03-13T23:59:59Z")

        assertEquals(initialStockList, stock.stockList(now))
    }

    @Test
    fun `updates stock if last modified date is yesterday`() {
        val now = Instant.parse("2023-03-14T00:00:01Z")
        val expected = StockList(
            lastModified = now,
            items = listOf(
                itemOf("banana", march1.minusDays(1), 41),
                itemOf("kumquat", march1.plusDays(1), 100)
            )
        )

        assertEquals(expected, stock.stockList(now))
        assertEquals(expected, fixture.load())
    }

    @Test
    fun `updates stock by two days if last modified date is before yesterday`() {
        val now = Instant.parse("2023-03-15T00:00:01Z")
        val expected = StockList(
            lastModified = now,
            items = listOf(
                itemOf("banana", march1.minusDays(1), 40),
                itemOf("kumquat", march1.plusDays(1), 99)
            )
        )

        assertEquals(expected, stock.stockList(now))
        assertEquals(expected, fixture.load())
    }

    @Test
    fun `does not update last modified date tomorrow`() {
        val now = Instant.parse("2023-03-12T00:00:01Z")

        assertEquals(initialStockList, stock.stockList(now))
        assertEquals(initialStockList, fixture.load())
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

private fun simpleUpdateItems(
    items: List<Item>,
    days: Int,
    @Suppress("UNUSED_PARAMETER") on: LocalDate,
) =
    items.map {
        it.withQuality(quality = it.quality - days)
    }
