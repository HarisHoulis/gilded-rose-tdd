package com.gildedrose

import java.time.LocalDate

data class Item(
    val name: String,
    val sellByDate: LocalDate,
    val quality: Int,
) {
    init {
        require(quality >= 0) {
            "Quality (=$quality) can not be negative!"
        }
    }

    fun updatedBy(days: Int, on: LocalDate): Item {
        val dates = (1 - days..0).map { on.plusDays(it.toLong()) }
        dates.forEach { println(it) }
        return dates.fold(this) { item, date ->
            item.update(date)
        }
    }

    private fun update(on: LocalDate): Item {
        val degradation = if (on.isAfter(sellByDate)) 2 else 1
        return copy(quality = (quality - degradation).coerceAtLeast(0))
    }

}
