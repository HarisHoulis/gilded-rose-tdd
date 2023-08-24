package com.gildedrose.domain

import com.gildedrose.march1
import com.gildedrose.testItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ConjuredStandardUpdatingTests {

    @Test
    fun `items decrease in quality two per day`() {
        assertEquals(
            testItem("Conjured banana", march1, 40),
            testItem("Conjured banana", march1, 42).updatedBy(1, on = march1)
        )
        assertEquals(
            testItem("Conjured banana", march1, 42),
            testItem("Conjured banana", march1, 42).updatedBy(0, on = march1),
        )
        assertEquals(
            testItem("Conjured banana", march1, 38),
            testItem("Conjured banana", march1, 42).updatedBy(2, on = march1)
        )
    }

    @Test
    fun `items quality does not become negative`() {
        assertEquals(
            testItem("Conjured banana", march1, 0),
            testItem("Conjured banana", march1, 0).updatedBy(1, on = march1)
        )
        assertEquals(
            testItem("Conjured banana", march1, 0),
            testItem("Conjured banana", march1, 1).updatedBy(2, on = march1)
        )
    }

    @Test
    fun `items decrease in quality four per day after sell-by date`() {
        assertEquals(
            testItem("Conjured banana", march1, 38),
            testItem("Conjured banana", march1, 42).updatedBy(1, on = march1.plusDays(1))
        )
        assertEquals(
            testItem("Conjured banana", march1, 42),
            testItem("Conjured banana", march1, 42).updatedBy(0, on = march1.plusDays(1))
        )
        assertEquals(
            testItem("Conjured banana", march1, 34),
            testItem("Conjured banana", march1, 42).updatedBy(2, on = march1.plusDays(2))
        )
        assertEquals(
            testItem("Conjured banana", march1, 36),
            testItem("Conjured banana", march1, 42).updatedBy(2, on = march1.plusDays(1))
        )
    }

    @Test
    fun `items with no sell-by date don't degrade in quality`() {
        assertEquals(
            testItem("Conjured banana", null, 42),
            testItem("Conjured banana", null, 42).updatedBy(1, on = march1.plusDays(1))
        )
    }

    @Test
    fun `items with a quality above 50 degrade gradually`() {
        assertEquals(
            testItem("Conjured banana", march1, 50),
            testItem("Conjured banana", march1, 52).updatedBy(1, on = march1)
        )
        assertEquals(
            testItem("Conjured banana", march1, 49),
            testItem("Conjured banana", march1, 51).updatedBy(1, on = march1)
        )
        assertEquals(
            testItem("Conjured banana", march1, 47),
            testItem("Conjured banana", march1, 51).updatedBy(1, on = march1.plusDays(1))
        )
    }
}
