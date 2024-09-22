package com.gildedrose.com.gildedrose.persistence

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.condition.EnabledIfSystemProperty

@EnabledIfSystemProperty(named = "run-db-tests", matches = "true")
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
