package com.gildedrose.com.gildedrose.persistence

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.condition.EnabledIfSystemProperty

@EnabledIfSystemProperty(named = "isCI", matches = "false")
internal class DatabaseItemTests : ItemsContract(DatabaseItems(testDatabase)) {

    @BeforeEach
    fun setUp() {
        transaction(testDatabase) {
            SchemaUtils.drop(DatabaseItems.Items)
            SchemaUtils.createMissingTablesAndColumns(DatabaseItems.Items)
        }
    }
}
