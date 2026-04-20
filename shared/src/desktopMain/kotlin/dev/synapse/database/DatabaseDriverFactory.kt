package dev.synapse.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val databaseFile = File(System.getProperty("user.home"), ".synapse/synapse.db")
        if (!databaseFile.parentFile.exists()) {
            databaseFile.parentFile.mkdirs()
        }
        val databaseExists = databaseFile.exists()
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")
        
        var currentVersion = driver.executeQuery(
            null, 
            "PRAGMA user_version;", 
            { cursor ->
                val version = if (cursor.next().value) cursor.getLong(0) ?: 0L else 0L
                app.cash.sqldelight.db.QueryResult.Value(version)
            }, 
            0
        ).value

        // If database exists but has no version, it's at least version 1 (legacy)
        if (databaseExists && currentVersion == 0L) {
            currentVersion = 1L
        }

        val newVersion = SynapseDatabase.Schema.version

        if (currentVersion == 0L) {
            SynapseDatabase.Schema.create(driver)
            driver.execute(null, "PRAGMA user_version = $newVersion;", 0)
        } else if (currentVersion < newVersion) {
            SynapseDatabase.Schema.migrate(driver, currentVersion, newVersion)
            driver.execute(null, "PRAGMA user_version = $newVersion;", 0)
        }
        return driver
    }
}
