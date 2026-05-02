package dev.synapse.database

import app.cash.sqldelight.db.SqlDriver
import tech.turso.libsql.LibsqlDriver
import dev.synapse.util.PlatformContext

actual class DatabaseDriverFactory actual constructor(context: PlatformContext) {
    private val platformContext = context
    actual fun createDriver(): SqlDriver {
        val databasePath = platformContext.context.getDatabasePath("synapse.db").absolutePath
        val driver = LibsqlDriver("file:$databasePath")

        // Initialize schema if needed
        try {
            SynapseDatabase.Schema.create(driver)
        } catch (e: Exception) {
            // Tables might already exist
        }

        // Initialize vector support (LibSQL specific)
        driver.execute(null, "CREATE VIRTUAL TABLE IF NOT EXISTS NotesVec USING vec0(embedding float32[384])", 0)
        
        return driver
    }
}
