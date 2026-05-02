package dev.synapse.data.repository

import dev.synapse.database.SynapseDatabase
import dev.synapse.domain.repository.ResonanceRepository
import dev.synapse.util.EmbeddingEngine
import editor.ResonanceItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

class ResonanceRepositoryImpl(
    private val database: SynapseDatabase,
    private val engine: EmbeddingEngine
) : ResonanceRepository {
    private val queries = database.synapseDatabaseQueries

    override suspend fun getResonance(text: String): List<ResonanceItem> = withContext(Dispatchers.IO) {
        searchSemanticIds(text, 5).mapNotNull { rowId ->
            queries.getNoteByRowId(rowId).executeAsOneOrNull()?.let { note ->
                ResonanceItem(
                    id = note.id,
                    title = note.title,
                    snippet = note.content_raw.take(100),
                    tags = emptyList()
                )
            }
        }
    }

    override suspend fun updateEmbedding(noteId: String, content: String) = withContext(Dispatchers.IO) {
        val embedding = engine.generateEmbedding(content) ?: return@withContext
        queries.insertEmbedding(noteId, BlobUtils.toBlob(embedding))
    }

    override suspend fun searchSemanticIds(query: String, limit: Int): List<Long> = withContext(Dispatchers.IO) {
        val queryEmbedding = engine.generateEmbedding(query) ?: return@withContext emptyList()

        queries.getAllNoteEmbeddings().executeAsList()
            .map { row -> row.note_rowid to cosineSimilarity(queryEmbedding, BlobUtils.fromBlob(row.embedding)) }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size) return 0f
        var dot = 0f
        var normA = 0f
        var normB = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        val denom = sqrt(normA) * sqrt(normB)
        return if (denom == 0f) 0f else dot / denom
    }
}
