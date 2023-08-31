package com.gildedrose.domain

import com.gildedrose.domain.ItemCreationError.ItemCreationException
import com.gildedrose.domain.ItemCreationError.NegativeQuality
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import java.time.LocalDate
import kotlin.math.max

@Suppress("DataClassPrivateConstructor") // protected by requires in init
data class Item private constructor(
    val name: NonBlankString,
    val sellByDate: LocalDate?,
    val quality: Int,
    private val type: ItemType,
) {
    companion object {
        operator fun invoke(
            name: NonBlankString,
            sellByDate: LocalDate?,
            quality: Int,
        ): Result4k<Item, ItemCreationError> = try {
            Success(Item(name, sellByDate, quality, typeFor(sellByDate, name.value)))
        } catch (x: Exception) {
            if (x is ItemCreationException)
                Failure(x.error)
            else
                error("")
        }
    }

    init {
        if (quality < 0) {
            throw ItemCreationException(NegativeQuality(quality))
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

sealed interface ItemCreationError {
    @Suppress("unused")
    val errorName: String get() = this::class.simpleName ?: "Error Name Unknown"

    data class NegativeQuality(val actual: Int) : ItemCreationError

    class ItemCreationException(val error: ItemCreationError) : Exception()
}
