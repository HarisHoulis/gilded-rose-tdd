package com.gildedrose.com.gildedrose.persistence

import com.gildedrose.persistence.StockFileItems
import java.nio.file.Files

internal class StockFileItemsTests : ItemsContract<Nothing?>(
    items = StockFileItems(Files.createTempFile("stock", ".tsv").toFile())
)
