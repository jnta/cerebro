package dev.synapse.data.repository

import dev.synapse.database.DatabaseDriverFactory
import dev.synapse.database.SynapseDatabase
import dev.synapse.util.DesktopEmbeddingEngine
import dev.synapse.util.PlatformContext
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class ResonanceRepositoryTest {
    @Test
    fun testSemanticSearch() = runBlocking {
        val driver = DatabaseDriverFactory(PlatformContext()).createDriver()
        val database = SynapseDatabase(driver)
        val engine = DesktopEmbeddingEngine()

        val repo = ResonanceRepositoryImpl(database, engine)

        val noteId = "1"
        database.synapseDatabaseQueries.insertNote(noteId, "Apple", "An apple is a sweet, edible fruit produced by an apple tree.", "raw", 0L, 0L, 0L)
        repo.updateEmbedding(noteId, "An apple is a sweet, edible fruit produced by an apple tree.")

        val noteId2 = "2"
        database.synapseDatabaseQueries.insertNote(noteId2, "Computer", "A computer is a machine that can be programmed to carry out sequences of arithmetic or logical operations.", "raw", 0L, 0L, 0L)
        repo.updateEmbedding(noteId2, "A computer is a machine that can be programmed to carry out sequences of arithmetic or logical operations.")

        val results = repo.searchSemanticIds("fruits", limit = 1)
        println("Results for 'fruits': $results")

        assertTrue(results.isNotEmpty(), "Expected semantic search to return results")
    }
}
