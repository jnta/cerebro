package dev.synapse.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.synapse.util.PlatformContext
import java.io.File

actual class DatabaseDriverFactory actual constructor(context: PlatformContext) {
    actual fun createDriver(): SqlDriver {
        val databasePath = File(System.getProperty("user.home"), ".synapse/synapse.db")
        databasePath.parentFile.mkdirs()
        
        val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
        
        // Check if tables exist by querying sqlite_master
        val tablesExist = try {
            driver.executeQuery(
                identifier = null,
                sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='Notes'",
                mapper = { cursor ->
                    cursor.next()
                },
                parameters = 0
            ).value
        } catch (e: Exception) {
            false
        }

        if (!tablesExist) {
            SynapseDatabase.Schema.create(driver)
        }
        
        return driver
    }
}
