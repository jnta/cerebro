package dev.synapse.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.synapse.database.SynapseDatabase
import dev.synapse.domain.model.Note
import dev.synapse.domain.model.NoteMetadata
import dev.synapse.domain.model.NoteCollection
import dev.synapse.domain.model.Attribute
import dev.synapse.domain.model.Edge
import dev.synapse.domain.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val SNIPPET_LENGTH = 100

class NoteRepositoryImpl(
    private val database: SynapseDatabase
) : NoteRepository {
    private val queries = database.synapseDatabaseQueries

    override fun getAllNotes(): Flow<List<Note>> {
        return queries.getAllNotes()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { notes ->
                notes.map { noteEntity ->
                    mapToNote(noteEntity)
                }
            }
    }

    override fun getNoteSummaries(): Flow<List<NoteMetadata>> {
        return queries.getAllNoteMetadata()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { notes ->
                notes.map { noteEntity ->
                    NoteMetadata(
                        id = noteEntity.id,
                        title = noteEntity.title,
                        snippet = if (noteEntity.content_raw.length > SNIPPET_LENGTH) 
                            noteEntity.content_raw.take(SNIPPET_LENGTH) + "..." 
                            else noteEntity.content_raw,
                        updatedAt = noteEntity.updated_at,
                        collectionId = noteEntity.collection_id,
                        tags = queries.getTagsForNote(noteEntity.id).executeAsList()
                    )
                }
            }
    }



    private fun mapToNote(noteEntity: dev.synapse.database.Notes): Note {
        val id = noteEntity.id
        val attributes = queries.getAttributesForNote(id).executeAsList().map {
            Attribute(it.id, it.attr_key, it.attr_value)
        }
        val edges = queries.getEdgesForNote(id, id).executeAsList().map {
            Edge(it.id, it.source_id, it.target_id, it.label)
        }
        val embedding = queries.getEmbedding(id).executeAsOneOrNull()?.let { BlobUtils.fromBlob(it) }
        return Note(
            id = id,
            title = noteEntity.title,
            content = noteEntity.content_raw,
            collectionId = noteEntity.collection_id,
            attributes = attributes,
            connections = edges,
            createdAt = noteEntity.created_at,
            updatedAt = noteEntity.updated_at,
            viewCount = noteEntity.view_count.toInt(),
            embedding = embedding
        )
    }

    override suspend fun getNoteById(id: String): Note? = withContext(Dispatchers.IO) {
        queries.getNoteById(id).executeAsOneOrNull()?.let { mapToNote(it) }
    }

    override suspend fun getNoteByTitle(title: String): Note? = withContext(Dispatchers.IO) {
        queries.getNoteByTitle(title).executeAsOneOrNull()?.let { mapToNote(it) }
    }

    override fun getForwardLinks(id: String): Flow<List<NoteMetadata>> {
        return queries.getForwardLinksMetadata(id)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { notes ->
                notes.map { noteEntity ->
                    NoteMetadata(
                        id = noteEntity.id,
                        title = noteEntity.title,
                        snippet = if (noteEntity.content_raw.length > SNIPPET_LENGTH) 
                            noteEntity.content_raw.take(SNIPPET_LENGTH) + "..." 
                            else noteEntity.content_raw,
                        updatedAt = noteEntity.updated_at,
                        collectionId = noteEntity.collection_id,
                        tags = queries.getTagsForNote(noteEntity.id).executeAsList()
                    )
                }
            }
    }

    override fun getBackLinks(id: String): Flow<List<NoteMetadata>> {
        return queries.getBackLinksMetadata(id)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { notes ->
                notes.map { noteEntity ->
                    NoteMetadata(
                        id = noteEntity.id,
                        title = noteEntity.title,
                        snippet = if (noteEntity.content_raw.length > SNIPPET_LENGTH) 
                            noteEntity.content_raw.take(SNIPPET_LENGTH) + "..." 
                            else noteEntity.content_raw,
                        updatedAt = noteEntity.updated_at,
                        collectionId = noteEntity.collection_id,
                        tags = queries.getTagsForNote(noteEntity.id).executeAsList()
                    )
                }
            }
    }

    override suspend fun saveNote(note: Note) = withContext(Dispatchers.IO) {
        queries.transaction {
            queries.insertNote(
                id = note.id,
                title = note.title,
                content_raw = note.content,
                collection_id = note.collectionId,
                created_at = note.createdAt,
                updated_at = note.updatedAt,
                view_count = note.viewCount.toLong()
            )

            note.embedding?.let {
                queries.insertEmbedding(note.id, BlobUtils.toBlob(it))
            }
            
            queries.deleteAttributesForNote(note.id)
            note.attributes.forEach {
                queries.insertAttribute(it.id, note.id, it.key, it.value)
            }
            
            queries.deleteOutgoingEdges(note.id)
            note.connections.forEach {
                queries.insertEdge(it.id, it.sourceId, it.targetId, it.label)
            }
        }
    }

    override suspend fun deleteNote(id: String) = withContext(Dispatchers.IO) {
        queries.transaction {
            queries.deleteAttributesForNote(id)
            queries.deleteEdgesForNote(id, id)
            queries.deleteNote(id)
        }
    }

    override fun getCollections(): Flow<List<NoteCollection>> {
        return queries.getAllCollections()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { collections ->
                collections.map { 
                    NoteCollection(it.id, it.name, it.color)
                }
            }
    }

    override suspend fun saveCollection(collection: NoteCollection) = withContext(Dispatchers.IO) {
        queries.insertCollection(collection.id, collection.name, collection.color)
    }

    override suspend fun deleteCollection(id: String) = withContext(Dispatchers.IO) {
        queries.deleteCollection(id)
    }

    override suspend fun isCollectionEmpty(id: String): Boolean = withContext(Dispatchers.IO) {
        queries.countNotesInCollection(id).executeAsOne() == 0L
    }
}
