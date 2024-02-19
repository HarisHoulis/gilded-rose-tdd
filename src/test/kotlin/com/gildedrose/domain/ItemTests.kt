package com.gildedrose.domain

import com.gildedrose.itemForTest
import com.gildedrose.march1
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.containsSubstring
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class ItemTests {

    @Test
    fun `no item should have a quality above 50 by updating`() {
        val testItem = itemForTest("banana", null, 50)
        assertEquals(
            itemForTest("banana", null, 50),
            testItem.copy(quality = testItem.quality + 1)
        )
    }

    @Test
    fun `items can keep a quality of above 50`() {
        val testItem = itemForTest("banana", null, 55)
        assertEquals(
            itemForTest("banana", null, 54),
            testItem.copy(quality = testItem.quality - 1)
        )
        val testItem1 = itemForTest("banana", null, 55)
        assertEquals(
            itemForTest("banana", null, 55),
            testItem1.copy(quality = testItem1.quality - -1)
        )
    }

    @Test
    fun `no item should have its quality reduced below 0 by updating`() {
        val testItem = itemForTest("banana", null, 2)
        assertEquals(
            itemForTest("banana", null, 0),
            testItem.copy(quality = testItem.quality - 3)
        )
    }

    @Test
    fun `item types for equality`() {
        assertNotEquals(
            itemForTest("Conjured banana", march1, 50),
            itemForTest(
                "Conjured Aged Brie",
                march1,
                50
            ).copy(name = NonBlankString("Conjured banana")!!)
        )
    }

    @Test
    fun `item types for toString`() {
        assertThat(
            itemForTest("Conjured banana", march1, 50).toString(),
            containsSubstring("type=CONJURED STANDARD")
        )
    }
}
