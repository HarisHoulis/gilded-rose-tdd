package com.gildedrose

import java.time.LocalDate

fun interface ItemType {
    fun update(item: Item, on: LocalDate): Item
}

fun typeFor(sellByDate: LocalDate?, name: String): ItemType = when {
    sellByDate == null -> UndatedType
    name == "Aged Brie" -> BrieType
    name.startsWith("Backstage Pass") -> PassType
    else -> StandardType
}

val StandardType = typeFor("STANDARD") { item, on ->
    requireNotNull(item.sellByDate)
    val degradation = when {
        on.isAfter(item.sellByDate) -> 2
        else -> 1
    }
    item.withQuality(item.quality - degradation)
}

val UndatedType = typeFor("UNDATED") { item, _ -> item }

val BrieType = typeFor("BRIE") { item, on ->
    requireNotNull(item.sellByDate)
    val improvement = when {
        on.isAfter(item.sellByDate) -> 2
        else -> 1
    }
    item.withQuality(item.quality + improvement)
}

val PassType = typeFor("PASS") { item, on ->
    requireNotNull(item.sellByDate)
    val daysUntilSellBy = item.sellByDate.toEpochDay() - on.toEpochDay()
    val newQuality = when {
        daysUntilSellBy < 0 -> 0
        daysUntilSellBy < 5 -> 3 + item.quality
        daysUntilSellBy < 10 -> 2 + item.quality
        else -> 1 + item.quality
    }
    item.withQuality(newQuality)
}

private fun typeFor(name: String, updater: ItemType) =
    object : ItemType by updater {
        override fun toString(): String = name
    }
