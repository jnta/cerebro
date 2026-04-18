package editor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditorViewModel(private val coroutineScope: CoroutineScope) {
    private val _state = MutableStateFlow(EditorUiState())
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    init {
        onEvent(EditorUiEvent.LoadNotes)
    }

    fun onEvent(event: EditorUiEvent) {
        when (event) {
            is EditorUiEvent.LoadNotes -> loadDummyNotes()
            is EditorUiEvent.SelectNote -> selectNote(event.noteId)
            is EditorUiEvent.UpdateNoteContent -> updateContent(event.newContent)
            is EditorUiEvent.CreateNewNote -> createNote()
            is EditorUiEvent.ToggleSidebar -> _state.update { it.copy(isSidebarVisible = !it.isSidebarVisible) }
            is EditorUiEvent.SaveCurrentNote -> saveContent()
            is EditorUiEvent.TriggerSearch -> { /* Handle search button click */ }
        }
    }

    private fun loadDummyNotes() {
        val dummyNotes = listOf(
            Note("1", "Project Cerebro", "The AI-Native Cognitive Gym...", 1713400000000L),
            Note("2", "Meeting Notes", "Discussed the new Fog of War feature.", 1713300000000L),
            Note("3", "Rust Tauri Backend", "Tauri + Rust is blazingly fast.", 1713200000000L)
        )
        _state.update { it.copy(notes = dummyNotes, isLoading = false) }
    }

    private fun selectNote(noteId: String) {
        val content = if (noteId == "1") "# Project Cerebro\n\nThe AI-Native Cognitive Gym (Active Learning & Desirable Difficulty)." else "Content for $noteId"
        _state.update { 
            it.copy(
                selectedNoteId = noteId,
                currentNoteContent = content
            ) 
        }
    }

    private fun updateContent(newContent: String) {
        _state.update { it.copy(currentNoteContent = newContent) }
    }

    private fun createNote() {
        val newNote = Note("new_id", "Untitled", "", System.currentTimeMillis())
        _state.update { 
            it.copy(
                notes = listOf(newNote) + it.notes,
                selectedNoteId = newNote.id,
                currentNoteContent = ""
            ) 
        }
    }

    private fun saveContent() {
        // Implement save logic to local vault here
    }
}
