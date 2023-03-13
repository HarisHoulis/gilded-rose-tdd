package com.gildedrose

import routesFor
import java.io.File
import java.nio.file.Files

class Fixture(
    initialStockList: StockList,
    private val stockFile: File = Files.createTempFile("stock", ".tsv").toFile(),
) {

    val routes = routesFor(stockFile) { march1 }

    init {
        save(initialStockList)
    }

    fun save(stockList: StockList) {
        stockList.saveTo(stockFile)
    }
}
