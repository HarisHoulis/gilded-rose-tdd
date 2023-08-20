package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class StandardUpdatingTests {

    @Test
    fun `items decrease in quality one per day`() {
        assertEquals(
            itemOf("banana", march1, 41),
            itemOf("banana", march1, 42).updatedBy(1, on = march1)
        )
        assertEquals(
            itemOf("banana", march1, 42),
            itemOf("banana", march1, 42).updatedBy(0, on = march1),
        )
        assertEquals(
            itemOf("banana", march1, 40),
            itemOf("banana", march1, 42).updatedBy(2, on = march1)
        )
    }

    @Test
    fun `items quality does not become negative`() {
        assertEquals(
            itemOf("banana", march1, 0),
            itemOf("banana", march1, 0).updatedBy(1, on = march1)
        )
        assertEquals(
            itemOf("banana", march1, 0),
            itemOf("banana", march1, 1).updatedBy(2, on = march1)
        )
    }

    @Test
    fun `items decrease in quality two per day after sell-by date`() {
        assertEquals(
            itemOf("banana", march1, 40),
            itemOf("banana", march1, 42).updatedBy(1, on = march1.plusDays(1))
        )
        assertEquals(
            itemOf("banana", march1, 42),
            itemOf("banana", march1, 42).updatedBy(0, on = march1.plusDays(1))
        )
        assertEquals(
            itemOf("banana", march1, 38),
            itemOf("banana", march1, 42).updatedBy(2, on = march1.plusDays(2))
        )
        assertEquals(
            itemOf("banana", march1, 39),
            itemOf("banana", march1, 42).updatedBy(2, on = march1.plusDays(1))
        )
    }

    @Test
    fun `items with no sell-by date don't degrade in quality`() {
        assertEquals(
            itemOf("banana", null, 42),
            itemOf("banana", null, 42).updatedBy(1, on = march1.plusDays(1))
        )
    }

    @Test
    fun `items with a quality above 50 degrade gradually`() {
        assertEquals(
            itemOf("banana", march1, 51),
            itemOf("banana", march1, 52).updatedBy(1, on = march1)
        )
        assertEquals(
            itemOf("banana", march1, 50),
            itemOf("banana", march1, 51).updatedBy(1, on = march1)
        )
        assertEquals(
            itemOf("banana", march1, 49),
            itemOf("banana", march1, 51).updatedBy(1, on = march1.plusDays(1))
        )
    }
}
