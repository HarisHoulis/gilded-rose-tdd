package com.gildedrose.com.gildedrose.persistence

import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.NonBlankString
import com.gildedrose.domain.Quality
import com.gildedrose.itemForTest
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFailure
import java.time.LocalDate

private val dataSource = PGSimpleDataSource().apply {
    user = "gilded"
    password = "rose"
    databaseName = "gilded-rose"
}

internal val db = Database.connect(dataSource)

@Disabled("Can't run on CI for now")
internal class PgStockFileItemsTests {

    private val item1 = itemForTest("id-1", "name", LocalDate.of(2023, 2, 14), 42)
    private val item2 = itemForTest("id-2", "another name", null, 99)

    private val items = PgItems()

    @BeforeEach
    fun setUp() {
        transaction(db) {
            SchemaUtils.drop(ItemsTable)
            SchemaUtils.createMissingTablesAndColumns(ItemsTable)
        }
    }

    @Test
    fun `add item`() {
        transaction(db) {
            expectThat(items.all()).isEmpty()
        }

        transaction(db) {
            items.add(item1)
            items.add(item2)
            expectThat(items.all()).isEqualTo(listOf(item1, item2))
        }

    }

    @Test
    fun `find by id`() {
        transaction(db) {
            items.add(item1)
            items.add(item2)
            expectThat(items.all()).isEqualTo(listOf(item1, item2))
        }

        transaction(db) {
            expectThat(items.findById(ID<Item>("no-such-id")!!))
                .isEqualTo(null)
            expectThat(items.findById(ID<Item>("id-1")!!))
                .isEqualTo(item1)
        }

    }

    @Test
    fun update() {
        transaction(db) {
            items.add(item1)
            items.add(item2)
        }

        val updatedItem = item1.copy(name = NonBlankString("new name")!!)
        transaction(db) {
            items.update(updatedItem)
        }

        transaction(db) {
            expectThat(items.findById(item1.id))
                .isEqualTo(updatedItem)
        }

        transaction(db) {
            expectCatching {
                items.update(item1.copy(id = ID("no-such-id")!!))
            }.isFailure().isA<IllegalStateException>()
        }
    }
}

internal class PgItems {
    fun all(): List<Item> {
        return ItemsTable.all()
    }

    fun add(item: Item) {
        ItemsTable.insert(item)
    }

    fun findById(id: ID<Item>): Item? {
        val items = ItemsTable.selectAll().where {
            ItemsTable.id eq id.toString()
        }.map(ResultRow::toItem)
        if (items.size > 1)
            TODO("Handle duplicate IDs")
        else
            return items.firstOrNull()
    }

    fun update(item: Item) {
        val rowsChanged = ItemsTable.update({ ItemsTable.id eq item.id.toString() }) {
            it[id] = item.id.toString()
            it[name] = item.name.toString()
            it[sellByDate] = item.sellByDate
            it[quality] = item.quality.valueInt
        }
        check(rowsChanged == 1)
    }
}

private object ItemsTable : Table() {
    val id: Column<String> = varchar("id", 100)
    val name: Column<String> = varchar("name", 100)
    val sellByDate: Column<LocalDate?> = date("sellByDate").nullable()
    val quality: Column<Int> = integer("quality")
}

private fun ItemsTable.insert(item: Item) {
    insert {
        it[id] = item.id.toString()
        it[name] = item.name.toString()
        it[sellByDate] = item.sellByDate
        it[quality] = item.quality.valueInt
    }
}

private fun ItemsTable.all() = selectAll().map(ResultRow::toItem)

private fun ResultRow.toItem() = Item(
    ID(this[ItemsTable.id]) ?: error("Could not parse id ${this[ItemsTable.id]}"),
    NonBlankString(this[ItemsTable.name]) ?: error("Invalid name ${this[ItemsTable.name]}"),
    this[ItemsTable.sellByDate],
    Quality(this[ItemsTable.quality]) ?: error("Invalid quality ${this[ItemsTable.quality]}"),
)
