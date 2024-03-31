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
) : Items {
    private val stockList = AtomicReference(stockList)

    override fun load(): Result<StockList, StockListLoadingError> {
        return Success(stockList.get())
    }

    override fun save(stockList: StockList): Result<StockList, StockListLoadingError.IO> {
        this.stockList.set(stockList)
        return Success(stockList)
    }

}
