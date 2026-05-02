package dev.synapse.data.repository

import dev.synapse.database.DatabaseDriverFactory
import dev.synapse.database.SynapseDatabase
import dev.synapse.domain.repository.ResonanceRepository
import dev.synapse.util.PlatformContext
import editor.SearchMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NoteRepositoryTest {
    private lateinit var database: SynapseDatabase
    private lateinit var repository: NoteRepositoryImpl
    private val resonanceRepository: ResonanceRepository = object : ResonanceRepository {
        override suspend fun getResonance(text: String): List<editor.ResonanceItem> = emptyList()
        override suspend fun updateEmbedding(noteId: String, content: String) {}
        override suspend fun searchSemanticIds(query: String, limit: Int): List<Long> = emptyList()
    }

    @BeforeTest
    fun setup() {
        val driver = DatabaseDriverFactory(PlatformContext()).createDriver()
        database = SynapseDatabase(driver)
        repository = NoteRepositoryImpl(database, resonanceRepository)
        
        // Seed initial collections as required by the schema/queries if needed
        database.synapseDatabaseQueries.seedInitialCollections()
    }

    @Test
    fun testPrefixSearch() = runBlocking {
        val noteId = "1"
        database.synapseDatabaseQueries.insertNote(
            id = noteId,
            title = "Apples",
            content_raw = "I like eating apples every day.",
            collection_id = "raw",
            created_at = 0L,
            updated_at = 0L,
            view_count = 0L
        )

        // Search for "apple" (exact mode)
        val results = repository.searchNotes("apple", SearchMode.EXACT).first()
        
        assertTrue(results.isNotEmpty(), "Search for 'apple' should return 'Apples' note via prefix matching")
        assertEquals(noteId, results[0].id)
    }

    @Test
    fun testMultiWordPrefixSearch() = runBlocking {
        val noteId = "2"
        database.synapseDatabaseQueries.insertNote(
            id = noteId,
            title = "Quick Brown Fox",
            content_raw = "The quick brown fox jumps over the lazy dog.",
            collection_id = "raw",
            created_at = 0L,
            updated_at = 0L,
            view_count = 0L
        )

        // Search for "qui bro"
        val results = repository.searchNotes("qui bro", SearchMode.EXACT).first()
        
        assertTrue(results.isNotEmpty(), "Search for 'qui bro' should return 'Quick Brown Fox' note")
        assertEquals(noteId, results[0].id)
    }
}
