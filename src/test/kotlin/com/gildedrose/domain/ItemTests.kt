package com.gildedrose.domain

import com.gildedrose.march1
import com.gildedrose.testItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class ItemTests {

    @Test
    fun `toString shows type`() {
        assertEquals(
            "Item(name=banana, sellByDate=2023-03-01, quality=50, type=STANDARD)",
            testItem("banana", march1, 50).toString()
        )
    }

    @Test
    fun `no item should have a quality above 50 by updating`() {
        assertEquals(
            testItem("banana", null, 50),
            testItem("banana", null, 50).degradedBy(-1)
        )
    }

    @Test
    fun `items can keep a quality of above 50`() {
        assertEquals(
            testItem("banana", null, 54),
            testItem("banana", null, 55).degradedBy(1)
        )
        assertEquals(
            testItem("banana", null, 55),
            testItem("banana", null, 55).degradedBy(-1)
        )
    }

    @Test
    fun `no item should have its quality reduced below 0 by updating`() {
        assertEquals(
            testItem("banana", null, 0),
            testItem("banana", null, 2).degradedBy(3)
        )
    }

    @Test
    fun `item types for equality`() {
        assertNotEquals(
            testItem("Conjured banana", march1, 50),
            testItem(
                "Conjured Aged Brie",
                march1,
                50
            ).copy(name = NonBlankString("Conjured banana")!!)
        )
    }

    @Test
    fun `item types for toString`() {
        assertEquals(
            "Item(name=Conjured banana, sellByDate=2023-03-01, quality=50, type=CONJURED STANDARD)",
            testItem("Conjured banana", march1, 50).toString()
        )
    }
}
