package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class UpdatingTests {

    @Test
    fun `items decrease in quality one per day`() {
        assertEquals(
            Item("banana", march1, 41),
            Item("banana", march1, 42).updatedBy(1, on = march1)
        )
        assertEquals(
            Item("banana", march1, 42),
            Item("banana", march1, 42).updatedBy(0, on = march1),
        )
        assertEquals(
            Item("banana", march1, 40),
            Item("banana", march1, 42).updatedBy(2, on = march1)
        )
    }

    @Test
    fun `items quality does not become negative`() {
        assertEquals(
            Item("banana", march1, 0),
            Item("banana", march1, 0).updatedBy(1, on = march1)
        )
        assertEquals(
            Item("banana", march1, 0),
            Item("banana", march1, 1).updatedBy(2, on = march1)
        )
    }

    @Test
    fun `items decrease in quality two per day after sell-by date`() {
        assertEquals(
            Item("banana", march1, 40),
            Item("banana", march1, 42).updatedBy(1, on = march1.plusDays(1))
        )
        assertEquals(
            Item("banana", march1, 42),
            Item("banana", march1, 42).updatedBy(0, on = march1.plusDays(1))
        )
        assertEquals(
            Item("banana", march1, 38),
            Item("banana", march1, 42).updatedBy(2, on = march1.plusDays(2))
        )
        assertEquals(
            Item("banana", march1, 39),
            Item("banana", march1, 42).updatedBy(2, on = march1.plusDays(1))
        )
    }

    @Test
    fun `items with no sell-by date don't degrade in quality`() {
        assertEquals(
            Item("banana", null, 42),
            Item("banana", null, 42).updatedBy(1, on = march1.plusDays(1))
        )
    }

    @Test
    fun `Aged Brie increases in quality by one every day until its sell-by date`() {
        assertEquals(
            Item("Aged Brie", march1, 43),
            Item("Aged Brie", march1, 42).updatedBy(1, on = march1)
        )
    }

    @Test
    fun `Aged Brie increases in quality by two every day after its sell-by date`() {
        assertEquals(
            Item("Aged Brie", march1, 44),
            Item("Aged Brie", march1, 42).updatedBy(1, on = march1.plusDays(1))
        )
    }

    @Test
    fun `Aged Brie doesn't get better than 50`() {
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
