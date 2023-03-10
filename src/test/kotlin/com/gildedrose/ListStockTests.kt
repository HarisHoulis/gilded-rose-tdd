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
import org.junit.jupiter.api.io.TempDir
import routesFor
import java.io.File
import java.time.Instant

@ExtendWith(ApprovalTest::class)
internal class ListStockTests {

    @TempDir
    lateinit var dir: File
    private val stockFile by lazy { dir.resolve("stock.tsv") }
    private val routes by lazy { routesFor(stockFile) { march1 } }

    @Test
    fun `list stock`(approver: Approver) {
        standardStockList.saveTo(stockFile)

        approver.assertApproved(routes(Request(GET, "/")), OK)
    }

    @Test
    fun `list stock sees file updates`(approver: Approver) {
        standardStockList.saveTo(stockFile)
        routes(Request(GET, "/")).status shouldBe OK

        StockList(Instant.now(), emptyList()).saveTo(stockFile)
        approver.assertApproved(routes(Request(GET, "/")), OK)
    }
}
