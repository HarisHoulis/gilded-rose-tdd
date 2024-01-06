package com.gildedrose

import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.NonBlankString
import com.gildedrose.domain.Price
import com.gildedrose.domain.Quality
import com.gildedrose.domain.StockList
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import java.time.LocalDate

val march1: LocalDate = LocalDate.parse("2023-03-01")

fun testItem(
    name: String,
    sellByDate: LocalDate?,
    quality: Int,
): Item = testItem(initialsFrom(name) + "1", name, sellByDate, quality)

fun testItem(
    id: String,
    name: String,
    sellByDate: LocalDate?,
    quality: Int,
): Item = Item(
    ID(id)!!,
    NonBlankString(name)!!,
    sellByDate,
    Quality(quality)!!
)

fun initialsFrom(name: String): String =
    name.split(" ")
        .map { it[0] }
        .joinToString("")
        .uppercase()

fun Item.withPrice(price: Price?) = withPrice(Success(price))

fun Item.withPrice(price: Result4k<Price?, Exception>) = copy(price = price)

fun StockList.withItems(vararg items: Item): StockList = copy(items = items.toList())
