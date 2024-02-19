package com.gildedrose.domain

import com.gildedrose.itemForTest
import com.gildedrose.march1
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PassesUpdatingTests {

    @Test
    fun `increases in quality by one every day until 10 days to sell by date`() {
        assertEquals(
            itemForTest("Backstage Passes", march1, 43),
            itemForTest("Backstage Passes", march1, 42).updatedBy(1, on = march1.minusDays(10))
        )
    }

    @Test
    fun `increases in quality by two every day from 10 to 5 days to sell by date`() {
        assertEquals(
            itemForTest("Backstage Passes", march1, 44),
            itemForTest("Backstage Passes", march1, 42).updatedBy(1, on = march1.minusDays(9))
        )
        assertEquals(
            itemForTest("Backstage Passes", march1, 44),
            itemForTest("Backstage Passes", march1, 42).updatedBy(1, on = march1.minusDays(5))
        )
    }

    @Test
    fun `increases in quality by three every day from 5 days to to sell by date`() {
        assertEquals(
            itemForTest("Backstage Passes", march1, 45),
            itemForTest("Backstage Passes", march1, 42).updatedBy(1, on = march1.minusDays(4))
        )
        assertEquals(
            itemForTest("Backstage Passes", march1, 45),
            itemForTest("Backstage Passes", march1, 42).updatedBy(1, on = march1)
        )
    }

    @Test
    fun `degrades completely after the sell by date`() {
        assertEquals(
            itemForTest("Backstage Passes", march1, 0),
            itemForTest("Backstage Passes", march1, 42).updatedBy(1, on = march1.plusDays(1))
        )
        assertEquals(
            itemForTest("Backstage Passes", march1, 0),
            itemForTest("Backstage Passes", march1, 42).updatedBy(1, on = march1.plusDays(2))
        )
    }

    @Test
    fun `doesn't get better than 50`() {
        assertEquals(
            itemForTest("Backstage Passes", march1, 50),
            itemForTest("Backstage Passes", march1, 49).updatedBy(1, on = march1)
        )
    }
}
