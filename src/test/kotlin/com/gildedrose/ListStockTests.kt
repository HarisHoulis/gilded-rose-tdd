package com.gildedrose

import App
import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import com.gildedrose.persistence.StockListLoadingError.BlankName
import com.natpryce.hamkrest.assertion.assertThat
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.hamkrest.hasStatus
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.http4k.testing.assertApproved
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.time.LocalDate

@ExtendWith(ApprovalTest::class)
internal class ListStockTests {

    private companion object {

        private val lastModified = Instant.parse("2023-03-13T12:00:00Z")
        private val sameDayAsLastModifiedDate = Instant.parse("2023-03-13T23:59:59Z")
        private val nextDayFromLastModifiedDate = Instant.parse("2023-03-14T00:00:00Z")

        private val stockList = StockList(
            lastModified = lastModified,
            items = listOf(
                testItem("banana", LocalDate.parse("2023-03-12"), 42),
                testItem("kumquat", LocalDate.parse("2023-03-14"), 101),
                testItem("undated", null, 50)
            )
        )
        private val baseApp = App()
    }

    @Test
    fun `list stock`(approver: Approver) =
        with(
            baseApp.fixture(
                now = sameDayAsLastModifiedDate,
                initialStockList = stockList
            )
        ) {
            assertEquals(
                Success(stockList.withNullPrices()),
                app.loadStockList()
            )
            approver.assertApproved(routes(Request(GET, "/")), OK)
        }

    @Test
    fun `list stock sees file updates`() =
        with(
            baseApp.fixture(
                now = sameDayAsLastModifiedDate,
                initialStockList = stockList
            )
        ) {
            val savedStockList = StockList(Instant.now(), emptyList())

            save(savedStockList)

            assertEquals(
                Success(savedStockList),
                app.loadStockList()
            )
        }

    @Test
    fun `doesn't update when last modified date is today`() {
        with(
            baseApp.fixture(
                now = sameDayAsLastModifiedDate,
                initialStockList = stockList
            )
        ) {
            assertEquals(
                Success(stockList.withNullPrices()),
                app.loadStockList()
            )
            assertEquals(stockList, load())
        }
    }

    @Test
    fun `updates when last modified date was yesterday`() {
        with(
            baseApp.fixture(
                now = nextDayFromLastModifiedDate,
                initialStockList = stockList
            )
        ) {
            val updatedStockList = StockList(
                lastModified = nextDayFromLastModifiedDate,
                items = listOf(
                    testItem("banana", LocalDate.parse("2023-03-12"), 40),
                    testItem("kumquat", LocalDate.parse("2023-03-14"), 100),
                    testItem("undated", null, 50)
                )
            )
            assertEquals(
                Success(updatedStockList.withNullPrices()),
                app.loadStockList()
            )
            assertEquals(updatedStockList, load())
        }
    }

    @Test
    fun `reports errors`(approver: Approver) {
        with(
            baseApp.fixture(
                now = sameDayAsLastModifiedDate,
                initialStockList = stockList
            )
        ) {
            val expectedFailure = BlankName("B1\t\t2023-03-12\t42")

            stockFile.writeText(stockFile.readText().replace("banana", ""))

            assertEquals(
                Failure(expectedFailure),
                app.loadStockList()
            )
            assertThat(routes(Request(GET, "/")), hasStatus(INTERNAL_SERVER_ERROR))
            assertEquals(
                expectedFailure,
                events.first()
            )
        }
    }
}

private fun StockList.withNullPrices() = copy(items = items.map { it.withNullPrice() })

private fun Item.withNullPrice() = copy(price = Success(null))

