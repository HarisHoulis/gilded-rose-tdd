package com.gildedrose

import com.gildedrose.domain.Item
import dev.forkhandles.result4k.onFailure
import java.time.LocalDate

val march1: LocalDate = LocalDate.parse("2023-03-01")

fun testItem(
    name: String,
    sellByDate: LocalDate?,
    quality: Int
): Item = Item(name, sellByDate, quality).onFailure { error("Could not create an item") }
