package com.gildedrose

import com.gildedrose.domain.itemOf
import io.kotest.matchers.shouldBe
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.http4k.testing.assertApproved
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(ApprovalTest::class)
internal class ListStockTests {

    private val stockList = StockList(
        lastModified = Instant.parse("2023-03-13T12:00:00Z"),
        items = listOf(
            itemOf("banana", march1.minusDays(1), 42),
            itemOf("kumquat", march1.plusDays(1), 101),
            itemOf("undated", null, 50)
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
    fun `list stock sees file updates`(approver: Approver) =
        with(
            Fixture(stockList, now = Instant.parse("2023-03-01T12:00:00Z"))
        ) {
            routes(Request(GET, "/")).status shouldBe OK

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
            Assertions.assertEquals(stockList, load())
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
}
