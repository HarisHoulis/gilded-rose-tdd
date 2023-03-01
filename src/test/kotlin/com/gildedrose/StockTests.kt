package com.gildedrose

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDate

class StockTests {

    @Test
    fun `add item to stock`() {
        val stock = emptyList<Item>()
        stock shouldBe emptyList()

        val newStock = stock + Item("banana", march1, 42u)
        newStock shouldBe listOf(Item("banana", march1, 42u))
    }
}

val march1: LocalDate = LocalDate.parse("2023-03-01")
