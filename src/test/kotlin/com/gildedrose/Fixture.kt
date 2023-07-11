package com.gildedrose

import routesFor
import java.io.File
import java.nio.file.Files
import java.time.Instant

class Fixture(
    initialStockList: StockList,
    val now: Instant,
    val stockFile: File = Files.createTempFile("stock", ".tsv").toFile(),
) {

    val routes = routesFor(
        stockFile = stockFile,
        clock = { now }
    )

    init {
        save(initialStockList)
    }

    fun save(stockList: StockList) {
        stockList.saveTo(stockFile)
    }

    fun load(): StockList = stockFile.loadItems()
}
