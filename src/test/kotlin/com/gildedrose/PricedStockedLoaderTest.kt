package com.gildedrose

import PricedStockedLoader
import StockLoadingResult
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.AnalyticsEvent
import com.gildedrose.foundation.UncaughtExceptionEvent
import com.gildedrose.foundation.succeedAfter
import com.gildedrose.persistence.StockListLoadingError
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.Instant.parse as t
import java.time.LocalDate.parse as d

internal class PricedStockedLoaderTest {

    private companion object {
        private val lastModified = t("2023-03-13T12:00:00Z")
        private val sameDayAsLastModifiedDate = t("2023-03-13T23:59:59Z")

        private val loadedStockList = StockList(
            lastModified = lastModified,
            items = listOf(
                testItem("banana", d("2023-03-12"), 42),
                testItem("kumquat", d("2023-03-14"), 101),
                testItem("undated", null, 50)
            )
        )

        private val expectedPricedStockList = loadedStockList.withItems(
            loadedStockList[0].withPrice(Price(666)),
            loadedStockList[1].withPrice(null),
            loadedStockList[2].withPrice(Price(999))
        )
    }

    private val stockValues = mutableMapOf<Instant, StockLoadingResult>(
        sameDayAsLastModifiedDate to Success(loadedStockList)
    )
    private val priceList = mutableMapOf<Item, (Item) -> Price?>(
        loadedStockList[0] to { Price(666) },
        loadedStockList[1] to { null },
        loadedStockList[2] to { Price(999) },
    )
    private val analyticsEvents = mutableListOf<AnalyticsEvent>()
    private val pricedStockedLoader = PricedStockedLoader(
        loading = stockValues::getValue,
        pricing = { item -> priceList[item]?.invoke(item) },
        analytics = { event -> analyticsEvents.add(event) }
    )

    @Test
    fun `loads and prices items`() {
        assertEquals(
            Success(expectedPricedStockList),
            pricedStockedLoader.load(sameDayAsLastModifiedDate)
        )
        assertTrue(analyticsEvents.isEmpty())
    }

    @Test
    fun `passes on failures to load stock`() {
        val loadingError = StockListLoadingError.IO("deliberate")
        stockValues[sameDayAsLastModifiedDate] = Failure(loadingError)

        assertEquals(
            Failure(loadingError),
            pricedStockedLoader.load(sameDayAsLastModifiedDate)
        )
        assertTrue(analyticsEvents.isEmpty())
    }

    @Test
    fun `item price remembers pricing failures`() {
        val exception = Exception("deliberate")
        priceList[loadedStockList[2]] = { throw exception }

        assertEquals(
            Success(
                expectedPricedStockList.copy(
                    items = expectedPricedStockList.items.toMutableList().apply {
                        set(2, get(2).copy(price = Failure(exception)))
                    }
                )
            ),
            pricedStockedLoader.load(sameDayAsLastModifiedDate)
        )
        with(analyticsEvents) {
            assertEquals(2, size) // one for the try and one for the retry
            assertTrue(all { it is UncaughtExceptionEvent })
        }
    }

    @Test
    fun `retries pricing failures`() {
        val exception = Exception("deliberate")
        priceList[loadedStockList[2]] = succeedAfter(1, { throw exception }) {
            Price(999)
        }

        assertEquals(
            Success(expectedPricedStockList),
            pricedStockedLoader.load(sameDayAsLastModifiedDate)
        )
        with(analyticsEvents) {
            assertEquals(1, size)
            assertTrue(all { it is UncaughtExceptionEvent })
        }
    }
}
