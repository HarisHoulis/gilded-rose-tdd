package com.gildedrose

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class PersistenceTests {

    @Test
    fun `load and save`(@TempDir dir: File) {
        val file = File(dir, "stock.tsv")
        val stock = listOf(
            Item("banana", march1, 42u),
            Item("peach", march1.plusDays(1), 100u),
        )

        stock.saveTo(file)

        file.loadItems() shouldBe stock
    }

    @Test
    fun `load and save empty`(@TempDir dir: File) {
        val file = File(dir, "stock.tsv")
        val stock = emptyList<Item>()

        stock.saveTo(file)

        file.loadItems() shouldBe stock
    }
}
