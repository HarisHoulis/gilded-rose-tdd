package com.gildedrose.persistence

import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class Stock(
    private val items: Items,
    private val zoneId: ZoneId,
    private val itemUpdate: Item.(days: Int, on: LocalDate) -> Item,
) {
    fun stockList(now: Instant): Result<StockList, StockListLoadingError> =
        items.load().flatMap { loaded ->
            val daysOutOfDate = loaded.lastModified.daysTo(now, zoneId)
            when {
                daysOutOfDate > 0L -> items.save(
                    loaded.updated(
                        now,
                        daysOutOfDate.toInt(),
                        LocalDate.ofInstant(now, zoneId)
                    )
                )

                else -> Success(loaded)
            }
        }

    private fun StockList.updated(
        now: Instant,
        daysOutOfDate: Int,
        localDate: LocalDate,
    ) = copy(
        lastModified = now,
        items = items.map {
            it.itemUpdate(daysOutOfDate, localDate)
        }
    )
}

internal fun Instant.daysTo(that: Instant, zone: ZoneId): Long =
    LocalDate.ofInstant(that, zone).toEpochDay() - LocalDate.ofInstant(this, zone).toEpochDay()
