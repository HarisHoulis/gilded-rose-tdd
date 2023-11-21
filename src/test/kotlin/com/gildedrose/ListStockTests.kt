package com.gildedrose

import com.gildedrose.domain.Features
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.StockList
import com.gildedrose.persistence.StockListLoadingError.BlankName
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.http4k.testing.assertApproved
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(ApprovalTest::class)
internal class ListStockTests {

    private val stockList = StockList(
        lastModified = Instant.parse("2023-03-13T12:00:00Z"),
        items = listOf(
            testItem("banana", march1.minusDays(1), 42),
            testItem("kumquat", march1.plusDays(1), 101),
            testItem("undated", null, 50)
        )
    )

    @Test
    fun `list stock`(approver: Approver) =
        with(
            Fixture(stockList, now = Instant.parse("2023-03-01T12:00:00Z"))
        ) {
            approver.assertApproved(routes(Request(GET, "/")), OK)
        }

    @Test
    fun `list stock with pricing enabled`(approver: Approver) {
        val pricing: (Item) -> Price? = {
            when (it) {
                stockList.items[0] -> Price(100)
                stockList.items[1] -> error("simulated price failure")
                else -> null
            }
        }
        with(
            Fixture(
                initialStockList = stockList,
                now = Instant.parse("2023-03-01T12:00:00Z"),
                pricing = pricing,
                features = Features(pricing = true),
            )
        ) {
            approver.assertApproved(routes(Request(GET, "/")), OK)
        }
    }

    @Test
    fun `list stock sees file updates`(approver: Approver) =
        with(
            Fixture(stockList, now = Instant.parse("2023-03-01T12:00:00Z"))
        ) {
            assertEquals(
                OK,
                routes(Request(GET, "/")).status
            )

            save(StockList(Instant.now(), emptyList()))
            approver.assertApproved(routes(Request(GET, "/")), OK)
        }

    @Test
    fun `doesn't update when last modified date is today`(approver: Approver) {
        val sameDayAsLastModifiedDate = Instant.parse("2023-03-13T23:59:59Z")
        with(
            Fixture(stockList, now = sameDayAsLastModifiedDate)
        ) {
            approver.assertApproved(routes(Request(GET, "/")), OK)
            assertEquals(stockList, load())
        }
    }

    @Test
    fun `updates when last modified date was yesterday`(approver: Approver) {
        val nextDayFromLastModifiedDate = Instant.parse("2023-03-14T00:00:00Z")
        with(
            Fixture(stockList, now = nextDayFromLastModifiedDate)
        ) {
            approver.assertApproved(routes(Request(GET, "/")), OK)
            Assertions.assertNotEquals(stockList, load())
        }
    }

    @Test
    fun `reports errors`(approver: Approver) {
        with(
            Fixture(stockList, now = Instant.parse("2023-03-14T00:00:00Z"))
        ) {
            stockFile.writeText(stockFile.readText().replace("banana", ""))
            approver.assertApproved(routes(Request(GET, "/")), INTERNAL_SERVER_ERROR)
            assertEquals(
                BlankName("B1\t\t2023-02-28\t42"),
                events.first()
            )
        }
    }
}
