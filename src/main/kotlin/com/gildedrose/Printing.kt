package com.gildedrose

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

private val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

fun List<Item>.printout(now: LocalDate): List<String> =
    listOf(dateFormat.format(now)) +
        map {
            it.printout(now)
        }

private fun Item.printout(now: LocalDate): String = "$name, ${daysUntilSellBy(now)}, $quality"

private fun Item.daysUntilSellBy(now: LocalDate): Long = ChronoUnit.DAYS.between(now, sellByDate)

