package com.gildedrose.domain

import com.gildedrose.march1
import com.gildedrose.testItem
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.containsSubstring
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class ItemTests {

    @Test
    fun `no item should have a quality above 50 by updating`() {
        val testItem = testItem("banana", null, 50)
        assertEquals(
            testItem("banana", null, 50),
            testItem.copy(quality = testItem.quality + 1)
        )
    }

    @Test
    fun `items can keep a quality of above 50`() {
        val testItem = testItem("banana", null, 55)
        assertEquals(
            testItem("banana", null, 54),
            testItem.copy(quality = testItem.quality - 1)
        )
        val testItem1 = testItem("banana", null, 55)
        assertEquals(
            testItem("banana", null, 55),
            testItem1.copy(quality = testItem1.quality - -1)
        )
    }

    @Test
    fun `no item should have its quality reduced below 0 by updating`() {
        val testItem = testItem("banana", null, 2)
        assertEquals(
            testItem("banana", null, 0),
            testItem.copy(quality = testItem.quality - 3)
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
        assertThat(
            testItem("Conjured banana", march1, 50).toString(),
            containsSubstring("type=CONJURED STANDARD")
        )
    }
}
