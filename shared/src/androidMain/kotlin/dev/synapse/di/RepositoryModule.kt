package dev.synapse.di

import dev.synapse.database.SynapseDatabase
import dev.synapse.domain.repository.ResonanceRepository
import dev.synapse.data.repository.ResonanceRepositoryImpl
import dev.synapse.util.EmbeddingEngine

actual fun createResonanceRepository(database: SynapseDatabase): ResonanceRepository {
    val noOpEngine = object : EmbeddingEngine {
        override suspend fun generateEmbedding(text: String): FloatArray? = null
    }
    return ResonanceRepositoryImpl(database, noOpEngine)
}
