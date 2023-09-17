package com.gildedrose

import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.NonBlankString
import com.gildedrose.domain.Quality
import java.time.LocalDate

val march1: LocalDate = LocalDate.parse("2023-03-01")

fun testItem(
    name: String,
    sellByDate: LocalDate?,
    quality: Int,
): Item = Item(
    ID(initialsFrom(name) + "1")!!,
    NonBlankString(name)!!,
    sellByDate,
    Quality(quality)!!
)

fun initialsFrom(name: String): String =
    name.split(" ")
        .map { it[0] }
        .joinToString("")
        .uppercase()

