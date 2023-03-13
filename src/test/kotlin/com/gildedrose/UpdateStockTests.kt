package com.gildedrose

import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

internal class UpdateStockTests {

    private val stockList: StockList = standardStockList.copy(
        lastModified = Instant.parse("2023-03-13T12:00:00Z")
    )

    @Test
    fun `doesn't update when last modified date is today`() {
        val sameDayAsLastModifiedDate = LocalDate.parse("2023-03-13")
        with(Fixture(stockList, now = sameDayAsLastModifiedDate)) {
            routes(Request(Method.GET, "/")).status shouldBe OK
            assertEquals(stockList, load())
        }
    }

    @Disabled("WIP")
    @Test
    fun `updates when last modified date was yesterday`() {
        val nextDayFromLastModifiedDate = LocalDate.parse("2023-03-14")
        with(Fixture(standardStockList, now = nextDayFromLastModifiedDate)) {
            routes(Request(Method.GET, "/")).status shouldBe OK
            assertNotEquals(stockList, load())
        }
    }
}
