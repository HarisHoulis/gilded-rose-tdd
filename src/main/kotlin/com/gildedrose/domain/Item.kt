package com.gildedrose.domain

import java.time.LocalDate
import kotlin.math.max

data class Item(
    val name: String,
    val sellByDate: LocalDate?,
    val quality: Int,
    private val type: ItemType
) {
    init {
        require(quality >= 0) {
            "Quality (=$quality) can not be negative!"
        }
    }

    fun updatedBy(days: Int, on: LocalDate): Item {
        val dates = (1 - days..0).map { on.plusDays(it.toLong()) }
        return dates.fold(this, type::update)
    }

    fun withQuality(quality: Int): Item {
        val qualityCap = max(this.quality, 50)
        return copy(quality = quality.coerceIn(0, qualityCap))
    }
}

fun itemOf(
    name: String,
    sellByDate: LocalDate?,
    quality: Int
) = Item(name, sellByDate, quality, typeFor(sellByDate, name))
