package com.gildedrose

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDate

class Tests {

    @Test
    fun test() {
        val stock = emptyList<Item>()
        stock shouldBe emptyList()

        val newStock = stock + Item("banana", LocalDate.now(), 42u)
        newStock shouldBe listOf(Item("banana", LocalDate.now(), 42u))
    }
}

