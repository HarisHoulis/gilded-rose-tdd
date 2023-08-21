package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ItemTests {

    @Test
    fun `toString shows type`() {
        assertEquals(
            "Item(name=banana, sellByDate=2023-03-01, quality=50, type=STANDARD)",
            itemOf("banana", march1, 50).toString()
        )
    }

    @Test
    fun `no item should have a quality above 50 by updating`() {
        assertEquals(
            itemOf("banana", null, 50),
            itemOf("banana", null, 50).withQuality(51)
        )
    }

    @Test
    fun `items can keep a quality of above 50`() {
        assertEquals(
            itemOf("banana", null, 54),
            itemOf("banana", null, 55).withQuality(54)
        )
        assertEquals(
            itemOf("banana", null, 55),
            itemOf("banana", null, 55).withQuality(55)
        )
    }

    @Test
    fun `no item should have its quality reduced below 0 by updating`() {
        assertEquals(
            itemOf("banana", null, 0),
            itemOf("banana", null, 2).withQuality(-1)
        )
    }

    @Test
    fun `cannot create with negative quality`() {
        assertThrows<IllegalArgumentException> {
            itemOf("banana", null, -1)
        }
    }

    @Test
    fun `item types for equality`() {
        assertNotEquals(
            itemOf("Conjured banana", march1, 50),
            itemOf("Conjured Aged Brie", march1, 50).copy(name = "Conjured banana")
        )
    }

    @Test
    fun `item types for toString`() {
        assertEquals(
            "Item(name=Conjured banana, sellByDate=2023-03-01, quality=50, type=CONJURED STANDARD)",
            itemOf("Conjured banana", march1, 50).toString()
        )
    }
}
