package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import dev.forkhandles.result4k.Result

interface Items {
    fun load(): Result<StockList, StockListLoadingError>
    fun save(stockList: StockList): Result<StockList, StockListLoadingError.IO>
}
