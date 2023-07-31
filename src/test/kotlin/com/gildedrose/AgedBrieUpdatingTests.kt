package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AgedBrieUpdatingTests {

    @Test
    fun `increases in quality by one every day until its sell-by date`() {
        assertEquals(
            Item("Aged Brie", march1, 43),
            Item("Aged Brie", march1, 42).updatedBy(1, on = march1)
        )
    }

    @Test
    fun `increases in quality by two every day after its sell-by date`() {
        assertEquals(
            Item("Aged Brie", march1, 44),
            Item("Aged Brie", march1, 42).updatedBy(1, on = march1.plusDays(1))
        )
    }

    @Test
    fun `doesn't get better than 50`() {
        assertEquals(
            Item("Aged Brie", march1, 50),
            Item("Aged Brie", march1, 50).updatedBy(1, on = march1)
        )
        assertEquals(
            Item("Aged Brie", march1, 50),
            Item("Aged Brie", march1, 49).updatedBy(1, on = march1.plusDays(1))
        )
    }
}
