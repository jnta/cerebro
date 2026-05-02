package editor

import androidx.compose.runtime.Immutable
import editor.ast.NoteNode
import editor.ast.AstParser
import dev.synapse.domain.model.Note
import dev.synapse.domain.model.NoteMetadata
import dev.synapse.domain.model.NoteCollection

@Immutable
enum class SearchMode {
    HYBRID,   // Semantic + Sparse (RRF)
    SEMANTIC, // Semantic only
    EXACT     // Specific Terms (Keyword)
}




@Immutable
data class NoteBlock(
    val id: String,
    val content: String,
    val detectedAttributes: List<String> = emptyList(),
    val astNode: NoteNode.BlockNode // Keeping for rendering logic
)

@Immutable
data class ResonanceItem(
    val id: String,
    val title: String,
    val snippet: String,
    val tags: List<String> = emptyList()
)

@Immutable
data class EditorUiState(
    val noteId: String = "",
    val isLoading: Boolean = true,
    val notes: List<Note> = emptyList(),
    val noteSummaries: List<NoteMetadata> = emptyList(),
    val navigationStack: List<String> = emptyList(), // Session Breadcrumbs
    val blocks: List<NoteBlock> = emptyList(),
    val forwardLinks: List<NoteMetadata> = emptyList(),
    val backLinks: List<NoteMetadata> = emptyList(),
    val resonanceItems: List<ResonanceItem> = emptyList(),
    val selectionMetadata: Map<String, String> = emptyMap(), // Dynamic attributes (e.g., "status" to "evergreen")
    val focusedBlockId: String? = null,
    val isSidebarVisible: Boolean = true,
    val isContextPanelVisible: Boolean = true,
    val showResonanceFilter: Boolean = false,
    val originalThought: String = "",
    val currentDestination: String = "All Notes",
    val collections: List<NoteCollection> = emptyList(),
    val selectedCollectionIds: Set<String> = emptySet(),
    val showCreateCollectionDialog: Boolean = false,
    val editingCollection: NoteCollection? = null,
    val collectionError: String? = null,
    val minThoughtLength: Int = 50,
    val searchQuery: String = "",
    val searchMode: SearchMode = SearchMode.HYBRID,
    val searchResults: List<NoteMetadata> = emptyList()
)

sealed interface EditorUiEvent {
    data object LoadNotes : EditorUiEvent
    data class SelectNote(val noteId: String) : EditorUiEvent
    data class MapsTo(val noteId: String) : EditorUiEvent // Phase 2 requirement
    
    // Block events
    data class UpdateBlockContent(val blockId: String, val newContent: String) : EditorUiEvent
    data class AddBlockAfter(val blockId: String) : EditorUiEvent
    data class RemoveBlock(val blockId: String) : EditorUiEvent
    data class MoveBlock(val fromIndex: Int, val toIndex: Int) : EditorUiEvent
    data class FocusBlock(val blockId: String?) : EditorUiEvent

    data object RequestResonance : EditorUiEvent // Phase 2 requirement
    data object CreateNewNote : EditorUiEvent
    data object ToggleSidebar : EditorUiEvent
    data object ToggleContextPanel : EditorUiEvent
    data object SaveCurrentNote : EditorUiEvent 
    data object TriggerSearch : EditorUiEvent
    data class NavigateTo(val destination: String) : EditorUiEvent
    
    // Resonance Filter
    data class UpdateOriginalThought(val text: String) : EditorUiEvent
    data object CommitNote : EditorUiEvent

    data class UpdateNoteTitle(val title: String) : EditorUiEvent
    data class NoteOverflow(val blockId: String) : EditorUiEvent
    data object Resonate : EditorUiEvent
    data class LinkNotes(val sourceNoteId: String, val targetNoteId: String) : EditorUiEvent
    data class DeleteNote(val noteId: String) : EditorUiEvent
    data class UpdateNoteCollection(val collectionId: String) : EditorUiEvent
    data class ToggleCollectionFilter(val collectionId: String) : EditorUiEvent
    
    // Collection CRUD
    data object ShowCreateCollectionDialog : EditorUiEvent
    data object DismissCollectionDialog : EditorUiEvent
    data class EditCollection(val collection: NoteCollection) : EditorUiEvent
    data class SaveCollection(val id: String, val name: String, val color: String) : EditorUiEvent
    data class DeleteCollection(val id: String) : EditorUiEvent
    
    // Search
    data class UpdateSearchQuery(val query: String) : EditorUiEvent
    data class UpdateSearchMode(val mode: SearchMode) : EditorUiEvent
    data object ExecuteSearch : EditorUiEvent
    data object ClearSearch : EditorUiEvent
}
