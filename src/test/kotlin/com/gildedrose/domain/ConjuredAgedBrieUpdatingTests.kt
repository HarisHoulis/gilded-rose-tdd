package com.gildedrose.domain

import com.gildedrose.march1
import com.gildedrose.testItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ConjuredAgedBrieUpdatingTests {

    @Test
    fun `increases in quality by two every day until its sell-by date`() {
        assertEquals(
            testItem("Conjured Aged Brie", march1, 44),
            testItem("Conjured Aged Brie", march1, 42).updatedBy(1, on = march1)
        )
    }

    @Test
    fun `increases in quality by four every day after its sell-by date`() {
        assertEquals(
            testItem("Conjured Aged Brie", march1, 46),
            testItem("Conjured Aged Brie", march1, 42).updatedBy(1, on = march1.plusDays(1))
        )
    }

    @Test
    fun `doesn't get better than 50`() {
        assertEquals(
            testItem("Conjured Aged Brie", march1, 50),
            testItem("Conjured Aged Brie", march1, 50).updatedBy(1, on = march1)
        )
        assertEquals(
            testItem("Conjured Aged Brie", march1, 50),
            testItem("Conjured Aged Brie", march1, 49).updatedBy(1, on = march1.plusDays(1))
        )
    }
}
