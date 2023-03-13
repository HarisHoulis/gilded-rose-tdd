package com.gildedrose

import routesFor
import java.io.File
import java.nio.file.Files
import java.time.LocalDate

class Fixture(
    initialStockList: StockList,
    now: LocalDate = march1,
    val stockFile: File = Files.createTempFile("stock", ".tsv").toFile(),
) {

    val routes = routesFor(stockFile) { now }

    init {
        save(initialStockList)
    }

    fun save(stockList: StockList) {
        stockList.saveTo(stockFile)
    }

    fun load(): StockList = stockFile.loadItems()
}
