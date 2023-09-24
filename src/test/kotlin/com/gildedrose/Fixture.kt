package com.gildedrose

import com.gildedrose.domain.Features
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.then
import com.gildedrose.persistence.loadItems
import com.gildedrose.persistence.saveTo
import dev.forkhandles.result4k.onFailure
import dummyPricing
import java.io.File
import java.nio.file.Files
import java.time.Instant

class Fixture(
    initialStockList: StockList,
    val now: Instant,
    pricing: (Item) -> Price? = ::dummyPricing,
    val events: MutableList<Any> = mutableListOf(),
    val stockFile: File = Files.createTempFile("stock", ".tsv").toFile(),
    val features: Features = Features(),
) {

    val routes = routesFor(
        stockFile = stockFile,
        clock = { now },
        pricing = pricing,
        analytics = analytics then { events.add(it) },
        features = features
    )

    init {
        save(initialStockList)
    }

    fun save(stockList: StockList) {
        stockList.saveTo(stockFile)
    }

    fun load(): StockList = stockFile.loadItems().onFailure { error("Could not load stock") }
}
