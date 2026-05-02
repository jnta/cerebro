package dev.synapse.domain.repository

import editor.ResonanceItem
import kotlinx.coroutines.flow.Flow

interface ResonanceRepository {
    suspend fun getResonance(text: String): List<ResonanceItem>
    suspend fun updateEmbedding(noteId: String, content: String)
    suspend fun searchSemanticIds(query: String, limit: Int = 20): List<Long>
}
