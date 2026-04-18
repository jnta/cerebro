package editor

import androidx.compose.runtime.Immutable
import editor.ast.NoteNode
import editor.ast.AstParser
import dev.synapse.domain.model.Note




@Immutable
data class TextBlock(
    val id: String,
    val rawContent: String,
    val astNode: NoteNode.BlockNode
)

@Immutable
data class EditorUiState(
    val isLoading: Boolean = true,
    val notes: List<Note> = emptyList(),
    val selectedNoteId: String? = null,
    val navigationStack: List<String> = emptyList(), // Breadcrumb history
    val blocks: List<TextBlock> = emptyList(),
    val focusedBlockId: String? = null,
    val isSidebarVisible: Boolean = true,
    val isContextPanelVisible: Boolean = true,
    val showResonanceFilter: Boolean = false,
    val originalThought: String = "",
    val minThoughtLength: Int = 50
)

sealed interface EditorUiEvent {
    data object LoadNotes : EditorUiEvent
    data class SelectNote(val noteId: String) : EditorUiEvent
    
    // Block events
    data class UpdateBlockContent(val blockId: String, val newContent: String) : EditorUiEvent
    data class AddBlockAfter(val blockId: String) : EditorUiEvent
    data class RemoveBlock(val blockId: String) : EditorUiEvent
    data class MoveBlock(val fromIndex: Int, val toIndex: Int) : EditorUiEvent
    data class FocusBlock(val blockId: String?) : EditorUiEvent

    data object CreateNewNote : EditorUiEvent
    data object ToggleSidebar : EditorUiEvent
    data object ToggleContextPanel : EditorUiEvent
    data object SaveCurrentNote : EditorUiEvent 
    data object TriggerSearch : EditorUiEvent
    
    // Resonance Filter
    data class UpdateOriginalThought(val text: String) : EditorUiEvent
    data object CommitNote : EditorUiEvent
}
