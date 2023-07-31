package com.gildedrose

import java.time.LocalDate

data class Item(
    val name: String,
    val sellByDate: LocalDate?,
    val quality: Int,
) {
    private val updater: (on: LocalDate) -> Item =
        when {
            name == "Aged Brie" -> this::updateBrie
            name.startsWith("Backstage Pass") -> this::updatePass
            else -> this::updateStandard
        }

    init {
        require(quality >= 0) {
            "Quality (=$quality) can not be negative!"
        }
    }

    fun updatedBy(days: Int, on: LocalDate): Item {
        val dates = (1 - days..0).map { on.plusDays(it.toLong()) }
        return dates.fold(this) { item, date ->
            item.updater(date)
        }
    }
}

private fun Item.updateStandard(on: LocalDate): Item {
    val degradation = when {
        sellByDate == null -> 0
        on.isAfter(sellByDate) -> 2
        else -> 1
    }
    return copy(quality = (quality - degradation).coerceAtLeast(0))
}

private fun Item.updateBrie(on: LocalDate): Item {
    val improvement = when {
        sellByDate == null -> 0
        on.isAfter(sellByDate) -> 2
        else -> 1
    }
    return copy(quality = (quality + improvement).coerceAtMost(50))
}

private fun Item.updatePass(on: LocalDate): Item {
    if(sellByDate == null) return this

    val daysUntilSellBy = sellByDate.toEpochDay() - on.toEpochDay()
    val newQuality = when {
        daysUntilSellBy < 0 -> 0
        daysUntilSellBy < 5 -> 3 + quality
        daysUntilSellBy < 10 -> 2 + quality
        else -> 1 + quality
    }
    return copy(quality = newQuality.coerceAtMost(50))
}
