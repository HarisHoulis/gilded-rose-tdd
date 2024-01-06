package com.gildedrose

import App
import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.Quality
import com.gildedrose.domain.StockList
import com.gildedrose.http.serverFor
import com.gildedrose.persistence.StockListLoadingError.BlankName
import com.gildedrose.pricing.fakeValueElfRoutes
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
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.net.URI
import java.time.Instant.parse as t
import java.time.LocalDate.parse as d

@ExtendWith(ApprovalTest::class)
internal class ListStockTests {

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

        private val valueElfPricing = { id: ID<Item>, _: Quality ->
            when (id) {
                stockList[0].id -> Price(666)
                stockList[1].id -> null
                stockList[2].id -> Price(999)
                else -> null
            }
        }

        private val expectedPricedStockList = stockList.withItems(
            stockList[0].withPrice(Price(666)),
            stockList[1].withPrice(null),
            stockList[2].withPrice(Price(999))
        )

        private val baseApp = App(
            valueElfUri = URI.create("http://localhost:8888/prices")
        )

        val server = serverFor(8888, fakeValueElfRoutes(valueElfPricing))

        @BeforeAll
        @JvmStatic
        fun startServer() {
            server.start()
        }

        @AfterAll
        @JvmStatic
        fun stopServer() {
            server.stop()
        }
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
                Success(expectedPricedStockList),
                app.loadStockList()
            )
            approver.assertApproved(routes(Request(GET, "/")), OK)
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
