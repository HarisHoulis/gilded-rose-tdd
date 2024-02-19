package com.gildedrose.rendering

import com.gildedrose.domain.Price
import com.gildedrose.domain.StockList
import com.gildedrose.itemForTest
import com.gildedrose.march1
import com.gildedrose.persistence.StockListLoadingError
import com.gildedrose.withPrice
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
                itemForTest("banana", march1.minusDays(1), 42)
                    .withPrice(Price(100)),
                itemForTest("kumquat", march1.plusDays(1), 101)
                    .withPrice(Failure(RuntimeException("simulated price failure"))),
                itemForTest("undated", null, 50)
                    .withPrice(null)
            )
        )
        approver.assertApproved(
            render(
                stockListResult = Success(value = stockList),
                now = Instant.parse("2023-03-01T12:00:00Z"),
                zoneId = londonZoneId
            )
        )
    }

    @Test
    fun `reports errors`(approver: Approver) {
        approver.assertApproved(
            render(
                stockListResult = Failure(StockListLoadingError.BlankName("line")),
                now = Instant.parse("2023-03-01T12:00:00Z"),
                zoneId = londonZoneId
            )
        )
    }
}
