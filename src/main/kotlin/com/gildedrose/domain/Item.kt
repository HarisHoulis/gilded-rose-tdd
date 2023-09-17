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

    fun degradedBy(degradation: Int): Item {
        val qualityCap = max(quality.value, 50)
        val newQuality = NonNegativeInt((quality - degradation).coerceIn(0, qualityCap))
            ?: error("tried to create a negative int")
        return copy(quality = newQuality)
    }
}
