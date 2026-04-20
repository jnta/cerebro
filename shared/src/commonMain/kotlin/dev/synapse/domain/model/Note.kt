package dev.synapse.domain.model

enum class NoteCategory {
    RAW, LIT, EVERGREEN;
    
    val displayName: String get() = when(this) {
        RAW -> "Raw"
        LIT -> "Lit"
        EVERGREEN -> "Evergreen"
    }
}

data class NoteMetadata(
    val id: String,
    val title: String,
    val snippet: String,
    val updatedAt: Long,
    val category: NoteCategory = NoteCategory.RAW,
    val tags: List<String> = emptyList()
)

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val category: NoteCategory = NoteCategory.RAW,
    val attributes: List<Attribute> = emptyList(),
    val connections: List<Edge> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long,
    val viewCount: Int = 0,
    val embedding: FloatArray? = null
) {
    val snippet: String get() = if (content.length > 100) content.take(100) + "..." else content
}


data class Attribute(
    val id: String,
    val key: String,
    val value: String
)

data class Edge(
    val id: String,
    val sourceId: String,
    val targetId: String,
    val label: String
)
