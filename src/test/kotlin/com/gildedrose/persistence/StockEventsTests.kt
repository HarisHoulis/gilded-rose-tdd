package com.gildedrose.com.gildedrose.persistence

import com.gildedrose.domain.ID
import com.gildedrose.domain.NonBlankString
import com.gildedrose.domain.Quality
import com.gildedrose.itemForTest
import com.gildedrose.persistence.StockAdded
import com.gildedrose.persistence.StockEvent
import com.gildedrose.persistence.StockRemoved
import com.gildedrose.persistence.toItems
import londonZoneId
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime

internal class StockEventsTests {

    private val aTimestamp = Instant.parse("2023-02-08T12:00:00Z")
    private val sameTimeAsTimestamp = ZonedDateTime.ofInstant(aTimestamp, londonZoneId)

    @Test
    fun `no events returns no items`() {
        expectThat(
            emptyList<StockEvent>().toItems(sameTimeAsTimestamp)
        ).isEmpty()
    }


    @Test
    fun `StockAdded events create Items`() {
        expectThat(
            listOf(
                stockAddedForTest(aTimestamp, "id-1", "name", LocalDate.of(2023, 2, 8), 42),
                stockAddedForTest(aTimestamp, "id-2", "another name", null, 99)
            ).toItems(sameTimeAsTimestamp)
        ).isEqualTo(
            listOf(
                itemForTest("id-1", "name", LocalDate.of(2023, 2, 8), 42),
                itemForTest("id-2", "another name", null, 99),
            )
        )
    }

    @Test
    fun `StockAdded events can create items with the same id`() {
        expectThat(
            listOf(
                stockAddedForTest(aTimestamp, "id-1", "name", LocalDate.of(2023, 2, 8), 42),
                stockAddedForTest(aTimestamp, "id-1", "another name", null, 99)
            ).toItems(sameTimeAsTimestamp)
        ).isEqualTo(
            listOf(
                itemForTest("id-1", "name", LocalDate.of(2023, 2, 8), 42),
                itemForTest("id-1", "another name", null, 99),
            )
        )
    }

    @Test
    fun `StockRemoved events remove Items`() {
        expectThat(
            listOf(
                stockAddedForTest(aTimestamp, "id-1", "name", LocalDate.of(2023, 2, 8), 42),
                stockAddedForTest(aTimestamp, "id-2", "another name", null, 99),
                stockRemovedForTest("id-2"),
            ).toItems(sameTimeAsTimestamp)
        ).isEqualTo(
            listOf(
                itemForTest("id-1", "name", LocalDate.of(2023, 2, 8), 42),
            )
        )
    }

    @Test
    fun `StockRemoved events are ignored if no item exists`() {
        expectThat(
            listOf(
                stockAddedForTest(aTimestamp, "id-1", "name", LocalDate.of(2023, 2, 8), 42),
                stockAddedForTest(aTimestamp, "id-2", "another name", null, 99),
                stockRemovedForTest("no-such"),
            ).toItems(sameTimeAsTimestamp)
        ).isEqualTo(
            listOf(
                itemForTest("id-1", "name", LocalDate.of(2023, 2, 8), 42),
                itemForTest("id-2", "another name", null, 99)
            )
        )
    }

    @Test
    fun `items are degraded the day after the added event`() {
        val addedTimestamp = Instant.parse("2023-02-08T12:00:00Z")
        val dayOfAddedTimestamp = ZonedDateTime.parse("2023-02-08T23:59:59Z")
        val dayAfterAddedTimestamp = ZonedDateTime.parse("2023-02-09T00:00:00Z")

        expectThat(
            listOf(
                stockAddedForTest(addedTimestamp, "id-1", "name", LocalDate.of(2023, 3, 8), 42),
            ).toItems(dayOfAddedTimestamp)
        ).isEqualTo(
            listOf(
                itemForTest("id-1", "name", LocalDate.of(2023, 3, 8), 42),
            )
        )


        expectThat(
            listOf(
                stockAddedForTest(addedTimestamp, "id-1", "name", LocalDate.of(2023, 3, 8), 42),
            ).toItems(dayAfterAddedTimestamp)
        ).isEqualTo(
            listOf(
                itemForTest("id-1", "name", LocalDate.of(2023, 3, 8), 41),
            )
        )
    }
}

private fun stockAddedForTest(
    timestamp: Instant,
    id: String,
    name: String,
    sellByDate: LocalDate?,
    quality: Int,
) = StockAdded(
    timestamp,
    ID(id)!!,
    NonBlankString(name)!!,
    sellByDate,
    Quality(quality)!!
)

private fun stockRemovedForTest(id: String) = StockRemoved(ID(id)!!)
