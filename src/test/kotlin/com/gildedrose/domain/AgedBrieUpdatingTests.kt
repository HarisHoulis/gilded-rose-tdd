package com.gildedrose.domain

import com.gildedrose.itemForTest
import com.gildedrose.march1
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class AgedBrieUpdatingTests {

    @Test
    fun `increases in quality by one every day until its sell-by date`() {
        Assertions.assertEquals(
            itemForTest("Aged Brie", march1, 43),
            itemForTest("Aged Brie", march1, 42).updatedBy(1, on = march1)
        )
    }

    @Test
    fun `increases in quality by two every day after its sell-by date`() {
        Assertions.assertEquals(
            itemForTest("Aged Brie", march1, 44),
            itemForTest("Aged Brie", march1, 42).updatedBy(1, on = march1.plusDays(1))
        )
    }

    @Test
    fun `doesn't get better than 50`() {
        Assertions.assertEquals(
            itemForTest("Aged Brie", march1, 50),
            itemForTest("Aged Brie", march1, 50).updatedBy(1, on = march1)
        )
        Assertions.assertEquals(
            itemForTest("Aged Brie", march1, 50),
            itemForTest("Aged Brie", march1, 49).updatedBy(1, on = march1.plusDays(1))
        )
    }
}
