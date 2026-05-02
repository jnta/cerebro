package dev.synapse.di

import dev.synapse.database.SynapseDatabase
import dev.synapse.domain.repository.ResonanceRepository
import dev.synapse.data.repository.ResonanceRepositoryImpl
import dev.synapse.util.DesktopEmbeddingEngine

actual fun createResonanceRepository(database: SynapseDatabase): ResonanceRepository {
    return ResonanceRepositoryImpl(database, DesktopEmbeddingEngine())
}
