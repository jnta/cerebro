package dev.synapse.database

import app.cash.sqldelight.db.SqlDriver
import dev.synapse.util.PlatformContext

expect class DatabaseDriverFactory(context: PlatformContext) {
    fun createDriver(): SqlDriver
}
