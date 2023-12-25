package com.gildedrose

import App
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.then
import com.gildedrose.persistence.loadItems
import com.gildedrose.persistence.saveTo
import dev.forkhandles.result4k.onFailure
import java.io.File
import java.nio.file.Files
import java.time.Instant

internal fun App.fixture(
    stockFile: File = Files.createTempFile("stock", ".tsv").toFile(),
    now: Instant,
    events: MutableList<Any> = mutableListOf(),
    initialStockList: StockList,
) = Fixture(
    events = events,
    app = copy(
        stockFile = stockFile,
        clock = { now },
        analytics = analytics then { events.add(it) }
    )
).apply { save(initialStockList) }

class Fixture(
    internal val app: App,
    val events: MutableList<Any>,
) {

    val stockFile get() = app.stockFile
    val routes = app.routes

    fun save(stockList: StockList) {
        stockList.saveTo(stockFile)
    }

    fun load(): StockList = stockFile
        .loadItems()
        .onFailure {
            error("Could not load stock")
        }
}
