package com.gildedrose

import java.time.Instant
import java.time.LocalDate

val march1: LocalDate = LocalDate.parse("2023-03-01")
val standardStockList = StockList(
    Instant.now(),
    listOf(
        Item("banana", march1.minusDays(1), 42u),
        Item("kumquat", march1.plusDays(1), 101u)
    )
)
