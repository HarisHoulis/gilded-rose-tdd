package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class UpdatingTests {

    private val items = listOf(
        Item("banana", march1, 42),
//        Item("kumquat", march1, 101u)
    )

    @Test
    fun `items decrease in quality one per day`() {
        assertEquals(
            listOf(Item("banana", march1, 41)),
            updateItems(items, 1)
        )
        assertEquals(
            items,
            updateItems(items, 0)
        )
        assertEquals(
            listOf(Item("banana", march1, 40)),
            updateItems(items, 2)
        )
    }

    @Test
    fun `items quality does not become negative`() {
        assertEquals(
            listOf(Item("banana", march1, 0)),
            updateItems(listOf(Item("banana", march1, 0)), 1)
        )
        assertEquals(
            listOf(Item("banana", march1, 1)),
            updateItems(listOf(Item("banana", march1, 2)), 1)
        )
    }
}
