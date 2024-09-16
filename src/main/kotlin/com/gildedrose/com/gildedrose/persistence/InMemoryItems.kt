package com.gildedrose.com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.persistence.Items
import com.gildedrose.persistence.StockListLoadingError
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

internal class InMemoryItems(
    stockList: StockList = StockList(lastModified = Instant.EPOCH, items = emptyList()),
) : Items<Nothing?> {
    private val stockList = AtomicReference(stockList)

    context(Nothing?) override fun load(): Result<StockList, StockListLoadingError> {
        return Success(stockList.get())
    }

    override fun <R> inTransaction(block: context(Nothing?) () -> R): R = block(null)

    context(Nothing?) override fun save(
        stockList: StockList,
    ): Result<StockList, StockListLoadingError.IO> {
        this@InMemoryItems.stockList.set(stockList)
        return Success(stockList)
    }

}
