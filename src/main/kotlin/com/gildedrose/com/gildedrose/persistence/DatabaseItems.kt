package com.gildedrose.com.gildedrose.persistence

import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.NonBlankString
import com.gildedrose.domain.Quality
import com.gildedrose.domain.StockList
import com.gildedrose.persistence.Items
import com.gildedrose.persistence.StockListLoadingError
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDate

internal class DatabaseItems(private val database: Database) : Items<Transaction> {

    override fun <R> inTransaction(block: context(Transaction) () -> R): R =
        transaction(database) {
            block(this)
        }

    context(Transaction) override fun load(): Result<StockList, StockListLoadingError> =
        getLastUpdate()?.let { lastUpdate ->
            Success(StockList(lastUpdate, allItemsUpdatedAt(lastUpdate)))
        } ?: Success(StockList(Instant.EPOCH, emptyList()))

    private fun getLastUpdate() = Items
        .select(Items.modified.max())
        .firstOrNull()
        ?.getOrNull(Items.modified.max())

    private fun allItemsUpdatedAt(lastUpdate: Instant) = Items
        .selectAll()
        .where { Items.modified eq lastUpdate }
        .map { it.toItem() }

    context(Transaction) override fun save(
        stockList: StockList,
    ): Result<StockList, StockListLoadingError.IO> {
        stockList.items.forEach { item ->
            Items.insert {
                it[id] = item.id.toString()
                it[modified] = stockList.lastModified
                it[name] = item.name.toString()
                it[sellByDate] = item.sellByDate
                it[quality] = item.quality.valueInt
            }
        }
        return Success(stockList)
    }

    object Items : Table() {
        val id: Column<String> = varchar("id", 100)
        val modified: Column<Instant> = timestamp("modified").index()
        val name: Column<String> = varchar("name", 100)
        val sellByDate: Column<LocalDate?> = date("sellByDate").nullable()
        val quality: Column<Int> = integer("quality")
    }

    private fun Items.all() = selectAll().map {
        it.toItem()
    }

    private fun ResultRow.toItem() = Item(
        ID(this[Items.id]) ?: error("Could not parse id ${this[Items.id]}"),
        NonBlankString(this[Items.name]) ?: error("Invalid name ${this[Items.name]}"),
        this[Items.sellByDate],
        Quality(this[Items.quality]) ?: error("Invalid quality ${this[Items.quality]}"),
    )
}
