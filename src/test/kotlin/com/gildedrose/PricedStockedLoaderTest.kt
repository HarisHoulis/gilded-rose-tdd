package com.gildedrose

import PricedStockedLoader
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.StockList
import com.gildedrose.persistence.StockListLoadingError
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.Instant.parse as t
import java.time.LocalDate.parse as d

internal class PricedStockedLoaderTest {

    private companion object {

        private val lastModified = t("2023-03-13T12:00:00Z")
        private val sameDayAsLastModifiedDate = t("2023-03-13T23:59:59Z")

        private val stockList = StockList(
            lastModified = lastModified,
            items = listOf(
                testItem("banana", d("2023-03-12"), 42),
                testItem("kumquat", d("2023-03-14"), 101),
                testItem("undated", null, 50)
            )
        )
    }

    private val stockValues = mutableMapOf<Instant, Result4k<StockList, StockListLoadingError>>()
    private val priceList = mutableMapOf<Item, Price?>()
    private val pricedStockedLoader = PricedStockedLoader(stockValues::getValue, priceList::get)

    @Test
    fun `loads and prices items`() {
        stockValues[sameDayAsLastModifiedDate] = Success(stockList)
        priceList.putAll(
            mapOf(
                stockList[0] to Price(666),
                stockList[2] to Price(999),
            )
        )

        assertEquals(
            Success(
                StockList(
                    lastModified = lastModified,
                    items = listOf(
                        testItem("banana", d("2023-03-12"), 42)
                            .copy(price = Success(Price(666))),
                        testItem("kumquat", d("2023-03-14"), 101)
                            .copy(price = Success(null)),
                        testItem("undated", null, 50)
                            .copy(price = Success(Price(999)))
                    )
                )
            ),
            pricedStockedLoader.load(sameDayAsLastModifiedDate)
        )
    }

}
