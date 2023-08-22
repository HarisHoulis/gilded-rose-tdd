package com.gildedrose

import analytics
import com.gildedrose.persistence.loadItems
import com.gildedrose.persistence.saveTo
import routesFor
import java.io.File
import java.nio.file.Files
import java.time.Instant

class Fixture(
    initialStockList: StockList,
    val now: Instant,
    val events: MutableList<Any> = mutableListOf(),
    val stockFile: File = Files.createTempFile("stock", ".tsv").toFile(),
) {

    val routes = routesFor(
        stockFile = stockFile,
        clock = { now },
        analytics = analytics then { events.add(it) }
    )

    init {
        save(initialStockList)
    }

    fun save(stockList: StockList) {
        stockList.saveTo(stockFile)
    }

    fun load(): StockList = stockFile.loadItems()
}

private infix fun Analytics.then(that: Analytics): Analytics = { event ->
    this(event)
    that(event)
}
