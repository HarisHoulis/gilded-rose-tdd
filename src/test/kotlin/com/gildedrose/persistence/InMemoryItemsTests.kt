package com.gildedrose.com.gildedrose.persistence

internal class InMemoryItemsTests : ItemsContract<Nothing?>(
    items = InMemoryItems()
)
