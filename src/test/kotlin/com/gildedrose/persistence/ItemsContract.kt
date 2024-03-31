package com.gildedrose.com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.itemForTest
import com.gildedrose.march1
import com.gildedrose.persistence.Items
import dev.forkhandles.result4k.Success
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.Instant
import java.time.temporal.ChronoUnit

internal abstract class ItemsContract(val items: Items) {

    val initialStockList = StockList(
        lastModified = Instant.parse("2023-03-13T23:59:59Z"),
        items = listOf(
            itemForTest("banana", march1.minusDays(1), 42),
            itemForTest("kumquat", march1.plusDays(1), 101)
        )
    )

    @Test
    fun `returns empty StockList before any save`() {
        expectThat(items.load())
            .isEqualTo(
                Success(
                    StockList(
                        lastModified = Instant.EPOCH,
                        items = emptyList()
                    )
                )
            )
    }

    @Test
    fun `returns last saved stockList`() {
        items.save(initialStockList)

        expectThat(items.load())
            .isEqualTo(Success(initialStockList))

        val modifiedStockList = initialStockList.copy(
            lastModified = initialStockList.lastModified.plus(1, ChronoUnit.HOURS),
            items = initialStockList.items.drop(1)
        )
        items.save(modifiedStockList)

        expectThat(items.load())
            .isEqualTo(Success(modifiedStockList))
    }
}
