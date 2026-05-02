package dev.synapse.domain.repository

import dev.synapse.domain.model.Note
import dev.synapse.domain.model.NoteMetadata
import dev.synapse.domain.model.NoteCollection
import editor.SearchMode
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    fun getNoteSummaries(): Flow<List<NoteMetadata>>
    suspend fun getNoteById(id: String): Note?
    suspend fun getNoteByTitle(title: String): Note?
    suspend fun saveNote(note: Note)
    suspend fun deleteNote(id: String)
    fun getForwardLinks(id: String): Flow<List<NoteMetadata>>
    fun getBackLinks(id: String): Flow<List<NoteMetadata>>
    
    // Collection management
    fun getCollections(): Flow<List<NoteCollection>>
    suspend fun saveCollection(collection: NoteCollection)
    suspend fun deleteCollection(id: String)
    suspend fun isCollectionEmpty(id: String): Boolean
    
    fun searchNotes(query: String, mode: SearchMode): Flow<List<NoteMetadata>>
}
