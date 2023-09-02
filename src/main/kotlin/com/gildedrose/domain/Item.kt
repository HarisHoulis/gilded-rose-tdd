package com.gildedrose.domain

import java.time.LocalDate
import kotlin.math.max

data class Item(
    val name: NonBlankString,
    val sellByDate: LocalDate?,
    val quality: NonNegativeInt,
    private val type: ItemType,
) {
    constructor(name: NonBlankString, sellByDate: LocalDate?, quality: NonNegativeInt) :
        this(name, sellByDate, quality, typeFor(sellByDate, name))

    fun updatedBy(days: Int, on: LocalDate): Item {
        val dates = (1 - days..0).map { on.plusDays(it.toLong()) }
        return dates.fold(this, type::update)
    }

    fun withQuality(quality: Int): Item {
        val qualityCap = max(this.quality.value, 50)
        return copy(
            quality = NonNegativeInt(quality.coerceIn(0, qualityCap))
                ?: error("tried to create a negative int")
        )
    }
}
