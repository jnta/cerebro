package editor

import dev.synapse.domain.model.Note
import dev.synapse.domain.model.Edge
import dev.synapse.domain.repository.NoteRepository
import dev.synapse.domain.repository.ResonanceRepository
import dev.synapse.domain.util.NoteParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class EditorOperations(
    private val coroutineScope: CoroutineScope,
    private val repository: NoteRepository,
    private val resonanceRepository: ResonanceRepository,
    private val state: MutableStateFlow<EditorUiState>
) {
    fun resonate() {
        coroutineScope.launch {
            val currentContent = state.value.blocks.joinToString("\n\n") { it.content }
            val resonance = resonanceRepository.getResonance(currentContent)
            state.update { it.copy(resonanceItems = resonance) }
        }
    }

    fun linkNotes(event: EditorUiEvent.LinkNotes) {
        coroutineScope.launch {
            val edge = Edge(
                id = EditorLogic.generateId(),
                sourceId = event.sourceNoteId,
                targetId = event.targetNoteId,
                label = "manual_link"
            )
            repository.getNoteById(event.sourceNoteId)?.let { note ->
                repository.saveNote(note.copy(connections = note.connections + edge))
            }
        }
    }

    fun handleNoteOverflow(blockId: String, onNoteSelected: (String) -> Unit) {
        val currentState = state.value
        val parentNoteId = currentState.noteId
        val block = currentState.blocks.find { it.id == blockId } ?: return
        val content = block.content
        if (content.isBlank()) return

        coroutineScope.launch {
            val parentNote = repository.getNoteById(parentNoteId)
            val inheritedAttributes = parentNote?.attributes?.map { 
                it.copy(id = EditorLogic.generateId()) 
            } ?: emptyList()

            val newId = EditorLogic.generateId()
            val derivedTitle = content.lineSequence()
                .firstOrNull()?.removePrefix("# ")?.take(50) ?: "Untitled"
            
            val newNote = Note(
                id = newId,
                title = derivedTitle,
                content = content,
                attributes = inheritedAttributes + NoteParser.extractAttributes(content),
                connections = listOf(
                    Edge(
                        id = EditorLogic.generateId(),
                        sourceId = newId,
                        targetId = parentNoteId,
                        label = "overflow_from"
                    )
                ),
                createdAt = Clock.System.now().toEpochMilliseconds(),
                updatedAt = Clock.System.now().toEpochMilliseconds()
            )
            repository.saveNote(newNote)

            val updatedBlocks = currentState.blocks.filter { it.id != blockId }
            val newParentContent = updatedBlocks.joinToString("\n\n") { it.content }
            if (parentNote != null) {
                repository.saveNote(parentNote.copy(
                    content = newParentContent,
                    updatedAt = Clock.System.now().toEpochMilliseconds()
                ))
            }

            onNoteSelected(newId)
        }
    }

    fun createNewNote() {
        coroutineScope.launch {
            val newId = EditorLogic.generateId()
            val initialBlock = EditorLogic.createInitialBlock()
            val newNote = Note(
                id = newId,
                title = "Untitled",
                content = "",
                category = dev.synapse.domain.model.NoteCategory.RAW,
                attributes = emptyList(),
                connections = emptyList(),
                createdAt = Clock.System.now().toEpochMilliseconds(),
                updatedAt = Clock.System.now().toEpochMilliseconds()
            )
            repository.saveNote(newNote)
            state.update { 
                it.copy(
                    noteId = newId,
                    currentDestination = "Editor",
                    navigationStack = EditorLogic.updateNavigationStack(it.navigationStack, newId),
                    blocks = listOf(initialBlock),
                    focusedBlockId = initialBlock.id,
                    showResonanceFilter = false,
                    originalThought = ""
                ) 
            }
        }
    }

    fun deleteNote(noteId: String) {
        coroutineScope.launch {
            repository.deleteNote(noteId)
            state.update { currentState ->
                val newNotes = currentState.notes.filter { it.id != noteId }
                val newStack = currentState.navigationStack.filter { it != noteId }
                val isCurrentNote = currentState.noteId == noteId
                currentState.copy(
                    notes = newNotes,
                    noteId = if (isCurrentNote) "" else currentState.noteId,
                    blocks = if (isCurrentNote) emptyList() else currentState.blocks,
                    navigationStack = newStack,
                    currentDestination = if (isCurrentNote) "All Notes" else currentState.currentDestination
                )
            }
        }
    }

    fun updateNoteCategory(category: dev.synapse.domain.model.NoteCategory, onComplete: () -> Unit) {
        state.update { currentState ->
            val updatedNotes = currentState.notes.map { note ->
                if (note.id == currentState.noteId) note.copy(category = category) else note
            }
            currentState.copy(notes = updatedNotes)
        }
        onComplete()
    }
}
