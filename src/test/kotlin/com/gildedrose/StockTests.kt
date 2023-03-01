package com.gildedrose

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDate

class StockTests {

    private val sellBy = LocalDate.parse("2023-03-01")

    @Test
    fun `add item to stock`() {
        val stock = emptyList<Item>()
        stock shouldBe emptyList()

        val newStock = stock + Item("banana", sellBy, 42u)
        newStock shouldBe listOf(Item("banana", sellBy, 42u))
    }
}
