package editor

data class Note(
    val id: String,
    val title: String,
    val snippet: String,
    val lastModified: Long
)

data class EditorUiState(
    val isLoading: Boolean = false,
    val notes: List<Note> = emptyList(),
    val selectedNoteId: String? = null,
    val currentNoteContent: String = "",
    val isSidebarVisible: Boolean = true
)

sealed class EditorUiEvent {
    object LoadNotes : EditorUiEvent()
    data class SelectNote(val noteId: String) : EditorUiEvent()
    data class UpdateNoteContent(val newContent: String) : EditorUiEvent()
    object CreateNewNote : EditorUiEvent()
    object ToggleSidebar : EditorUiEvent()
    object SaveCurrentNote : EditorUiEvent() 
    object TriggerSearch : EditorUiEvent()
}
