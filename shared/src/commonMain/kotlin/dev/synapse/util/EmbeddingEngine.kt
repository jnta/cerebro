package dev.synapse.util

interface EmbeddingEngine {
    suspend fun generateEmbedding(text: String): FloatArray?
}
