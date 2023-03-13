package com.gildedrose

import io.kotest.matchers.shouldBe
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.http4k.testing.assertApproved
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(ApprovalTest::class)
internal class ListStockTests {

    @Test
    fun `list stock`(approver: Approver) =
        with(Fixture(standardStockList)) {
            approver.assertApproved(routes(Request(GET, "/")), OK)
        }

    @Test
    fun `list stock sees file updates`(approver: Approver) =
        with(Fixture(standardStockList)) {
            routes(Request(GET, "/")).status shouldBe OK

            save(StockList(Instant.now(), emptyList()))
            approver.assertApproved(routes(Request(GET, "/")), OK)
        }
}
