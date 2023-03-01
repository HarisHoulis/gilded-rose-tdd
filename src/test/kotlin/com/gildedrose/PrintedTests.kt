package com.gildedrose

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PrintedTests {

    private val now = LocalDate.parse("2023-03-01")

    @Test
    fun `print empty stock list`() {
        val stock = emptyList<Item>()
        val expected = listOf("1 March 2023")

        stock.printout(now) shouldBe expected
    }


    @Test
    fun `print non empty stock list`() {
        val stock = listOf(
            Item("banana", now.minusDays(1), 42u),
            Item("kumquat", now.plusDays(1), 101u)
        )
        val expected = listOf(
            "1 March 2023",
            "banana, -1, 42",
            "kumquat, 1, 101"
        )

        stock.printout(now) shouldBe expected
    }
}
