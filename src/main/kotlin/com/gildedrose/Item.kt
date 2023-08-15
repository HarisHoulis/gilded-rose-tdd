package com.gildedrose

import java.time.LocalDate

class Item(
    val name: String,
    val sellByDate: LocalDate?,
    val quality: Int,
    private val updater: Item.(on: LocalDate) -> Item
) {
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

    fun withQuality(quality: Int) = itemOf(name, sellByDate, quality)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Item

        if (name != other.name) return false
        if (sellByDate != other.sellByDate) return false
        return quality == other.quality
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (sellByDate?.hashCode() ?: 0)
        result = 31 * result + quality
        return result
    }


}
