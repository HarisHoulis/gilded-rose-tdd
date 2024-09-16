package com.gildedrose.com.gildedrose.persistence

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach

internal class DatabaseItemTests : ItemsContract<Transaction>(
    items = DatabaseItems(testDatabase)
) {

    @BeforeEach
    fun setUp() {
        transaction(testDatabase) {
            SchemaUtils.drop(DatabaseItems.Items)
            SchemaUtils.createMissingTablesAndColumns(DatabaseItems.Items)
        }
    }
}
