package com.gildedrose.com.gildedrose.persistence

import com.gildedrose.persistence.StockFileItems
import java.nio.file.Files

internal class StockFileItemsTests :
    ItemsContract(StockFileItems(Files.createTempFile("stock", ".tsv").toFile()))
