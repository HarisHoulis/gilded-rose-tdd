package com.gildedrose.domain

import java.time.LocalDate

fun interface ItemType {
    fun update(item: Item, on: LocalDate): Item
}

fun typeFor(sellByDate: LocalDate?, name: NonBlankString): ItemType {
    val baseType = when {
        sellByDate == null -> UndatedType
        name.contains("Aged Brie", ignoreCase = true) -> BrieType
        name.contains("Backstage Pass", ignoreCase = true) -> PassType
        else -> StandardType
    }
    return when {
        name.startsWith("Conjured", ignoreCase = true) -> conjured(baseType)
        else -> baseType
    }
}

fun conjured(baseType: ItemType) = typeFor("CONJURED $baseType") { item, on ->
    val updated = baseType.update(item, on)
    val change = item.quality.value - updated.quality.value
    item.withQuality(item.quality.value - 2 * change)
}

val StandardType = typeFor("STANDARD") { item, on ->
    requireNotNull(item.sellByDate)
    val degradation = when {
        on.isAfter(item.sellByDate) -> 2
        else -> 1
    }
    item.withQuality(item.quality.value - degradation)
}

val UndatedType = typeFor("UNDATED") { item, _ -> item }

val BrieType = typeFor("BRIE") { item, on ->
    requireNotNull(item.sellByDate)
    val improvement = when {
        on.isAfter(item.sellByDate) -> 2
        else -> 1
    }
    item.withQuality(item.quality.value + improvement)
}

val PassType = typeFor("PASS") { item, on ->
    requireNotNull(item.sellByDate)
    val daysUntilSellBy = item.sellByDate.toEpochDay() - on.toEpochDay()
    val newQuality = when {
        daysUntilSellBy < 0 -> 0
        daysUntilSellBy < 5 -> 3 + item.quality.value
        daysUntilSellBy < 10 -> 2 + item.quality.value
        else -> 1 + item.quality.value
    }
    item.withQuality(newQuality)
}

private fun typeFor(name: String, updater: ItemType) =
    object : ItemType by updater {
        override fun toString(): String = name

        override fun equals(other: Any?): Boolean {
            if (other !is ItemType) return false

            return this.toString() == other.toString()
        }
    }
