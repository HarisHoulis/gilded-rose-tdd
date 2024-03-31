package com.gildedrose.com.gildedrose.persistence

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled

@Disabled
internal class DatabaseItemTests : ItemsContract(DatabaseItems(db)) {

    @BeforeEach
    fun setUp() {
        transaction(db) {
            SchemaUtils.drop(DatabaseItems.Items)
            SchemaUtils.createMissingTablesAndColumns(DatabaseItems.Items)
        }
    }
}
