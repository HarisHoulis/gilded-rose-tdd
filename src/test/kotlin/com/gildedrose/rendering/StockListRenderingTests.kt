package com.gildedrose.rendering

import com.gildedrose.domain.Price
import com.gildedrose.domain.StockList
import com.gildedrose.march1
import com.gildedrose.persistence.StockListLoadingError
import com.gildedrose.testItem
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import londonZoneId
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(ApprovalTest::class)
internal class StockListRenderingTests {

    private val sometime = Instant.now()

    @Test
    fun `list stock`(approver: Approver) {
        val stockList = StockList(
            lastModified = sometime,
            items = listOf(
                testItem("banana", march1.minusDays(1), 42),
                testItem("kumquat", march1.plusDays(1), 101),
                testItem("undated", null, 50)
            )
        )

        val result = render(
            stockListResult = Success(value = stockList),
            now = Instant.parse("2023-03-01T12:00:00Z"),
            zoneId = londonZoneId,
            isPricingEnabled = false
        )
        approver.assertApproved(result)
    }

    @Test
    fun `list stock with pricing enabled`(approver: Approver) {
        val stockList = StockList(
            lastModified = sometime,
            items = listOf(
                testItem("banana", march1.minusDays(1), 42).copy(price = Success(Price(100))),
                testItem(
                    "kumquat",
                    march1.plusDays(1),
                    101
                ).copy(price = Failure(RuntimeException("simulated price failure"))),
                testItem("undated", null, 50).copy(price = Success(null))
            )
        )
        val result = render(
            stockListResult = Success(value = stockList),
            now = Instant.parse("2023-03-01T12:00:00Z"),
            zoneId = londonZoneId,
            isPricingEnabled = true
        )
        approver.assertApproved(result)
    }

    @Test
    fun `reports errors`(approver: Approver) {
        val result = render(
            stockListResult = Failure(StockListLoadingError.BlankName("line")),
            now = Instant.parse("2023-03-01T12:00:00Z"),
            zoneId = londonZoneId,
            isPricingEnabled = false
        )
        approver.assertApproved(result)
    }
}
