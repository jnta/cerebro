package editor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview
import kotlin.random.Random
import dev.synapse.domain.repository.NoteRepository
import dev.synapse.domain.model.Note
import dev.synapse.domain.util.NoteParser
import dev.synapse.domain.model.Attribute
import dev.synapse.domain.model.Edge
import kotlinx.coroutines.flow.collect

private const val DEBOUNCE_MS = 300L
private const val AUTO_SAVE_DEBOUNCE_MS = 2000L
private const val MAX_TITLE_LENGTH = 50

@OptIn(FlowPreview::class)
class EditorViewModel(
    private val coroutineScope: CoroutineScope,
    private val repository: NoteRepository
) {
object EditorUtils {
    fun generateId(): String = dev.synapse.domain.util.UUIDv7.generate()

    fun createInitialBlock(): TextBlock = TextBlock(
        id = generateId(), 
        rawContent = "", 
        astNode = editor.ast.NoteNode.BlockNode.Paragraph(generateId(), listOf())
    )
}

    private val _state = MutableStateFlow(EditorUiState())
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    private val textChangeFlow = MutableSharedFlow<Pair<String, String>>(
        extraBufferCapacity = 64
    )

    init {
        onEvent(EditorUiEvent.LoadNotes)
        
        coroutineScope.launch {
            repository.getAllNotes().collect { notes ->
                _state.update { it.copy(notes = notes, isLoading = false) }
            }
        }

        coroutineScope.launch {
            textChangeFlow
                .debounce(DEBOUNCE_MS)
                .collectLatest { (blockId, content) ->
                    parseBlockBackground(blockId, content)
                }
        }

        coroutineScope.launch {
            textChangeFlow
                .debounce(AUTO_SAVE_DEBOUNCE_MS)
                .collectLatest { 
                    handleSave(isCommit = false, isAutoSave = true) 
                }
        }
    }

    fun onEvent(event: EditorUiEvent) {
        if (!handleNoteEvent(event)) {
            handleBlockEvent(event)
        }
    }

    private fun handleBlockEvent(event: EditorUiEvent) {
        when (event) {
            is EditorUiEvent.UpdateBlockContent -> updateBlockContent(event.blockId, event.newContent)
            is EditorUiEvent.AddBlockAfter -> mutateBlocks(BlockAction.Add(event.blockId))
            is EditorUiEvent.RemoveBlock -> mutateBlocks(BlockAction.Remove(event.blockId))
            is EditorUiEvent.MoveBlock -> mutateBlocks(BlockAction.Move(event.fromIndex, event.toIndex))
            is EditorUiEvent.FocusBlock -> _state.update { it.copy(focusedBlockId = event.blockId) }
            else -> {}
        }
    }

    private fun handleNoteEvent(event: EditorUiEvent): Boolean {
        return when (event) {
            is EditorUiEvent.SelectNote -> { selectNote(event.noteId); true }
            is EditorUiEvent.CreateNewNote -> { launchCreateNote(); true }
            is EditorUiEvent.SaveCurrentNote -> { handleSave(isCommit = false); true }
            is EditorUiEvent.CommitNote -> { handleSave(isCommit = true); true }
            is EditorUiEvent.UpdateOriginalThought -> { 
                _state.update { it.copy(originalThought = event.text) }; true 
            }
            is EditorUiEvent.ToggleSidebar -> { 
                _state.update { it.copy(isSidebarVisible = !it.isSidebarVisible) }; true 
            }
            is EditorUiEvent.ToggleContextPanel -> { 
                _state.update { it.copy(isContextPanelVisible = !it.isContextPanelVisible) }; true 
            }
            else -> false
        }
    }

    private fun handleSave(isCommit: Boolean, isAutoSave: Boolean = false) {
        val currentThought = _state.value.originalThought.length
        val minThought = _state.value.minThoughtLength
        
        if (isCommit || (currentThought < minThought && !isAutoSave)) {
            if (currentThought >= minThought) {
                _state.update { it.copy(showResonanceFilter = false) }
                performSave(isAutoSave)
            } else {
                _state.update { it.copy(showResonanceFilter = true) }
            }
        } else {
            performSave(isAutoSave)
        }
    }

    private fun performSave(isAutoSave: Boolean) {
        val selectedId = _state.value.selectedNoteId ?: return
        val fullContent = _state.value.blocks.joinToString("\n\n") { it.rawContent }
        
        val derivedTitle = _state.value.blocks.firstOrNull()?.rawContent
            ?.lineSequence()?.firstOrNull()?.removePrefix("# ")?.take(MAX_TITLE_LENGTH) ?: "Untitled"

        coroutineScope.launch {
            val existingNote = repository.getNoteById(selectedId)
            val updatedNote = Note(
                id = selectedId,
                title = derivedTitle,
                content = fullContent,
                attributes = NoteParser.extractAttributes(fullContent),
                connections = NoteParser.extractEdges(selectedId, fullContent),
                createdAt = existingNote?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.saveNote(updatedNote)
            
            if (!isAutoSave) {
                _state.update { it.copy(originalThought = "", showResonanceFilter = false) }
            }
        }
    }



    private fun selectNote(noteId: String) {
        coroutineScope.launch {
            val note = repository.getNoteById(noteId) ?: return@launch
            
            val initialBlocks = note.content.split("\n\n").map { 
                TextBlock(
                    id = EditorUtils.generateId(), 
                    rawContent = it, 
                    astNode = editor.ast.AstParser.parseBlock(EditorUtils.generateId(), it)
                ) 
            }
            val blocks = initialBlocks.ifEmpty { listOf(EditorUtils.createInitialBlock()) }
            
            _state.update { 
                val newStack = it.navigationStack.toMutableList()
                if (newStack.lastOrNull() != noteId) {
                    newStack.add(noteId)
                }
                it.copy(
                    selectedNoteId = noteId,
                    navigationStack = newStack,
                    blocks = blocks,
                    focusedBlockId = blocks.firstOrNull()?.id
                ) 
            }
        }
    }

    private fun updateBlockContent(blockId: String, newContent: String) {
        // 1. Local State (instantâneo): O estado armazena exatamente o texto puro digitado sem bloquear a UI.
        _state.update { state ->
            val newBlocks = state.blocks.map { block ->
                if (block.id == blockId) {
                    block.copy(rawContent = newContent)
                } else {
                    block
                }
            }
            state.copy(blocks = newBlocks)
        }
        
        // 2. Envia para o fluxo de parsing em background
        textChangeFlow.tryEmit(blockId to newContent)
    }

    private suspend fun parseBlockBackground(blockId: String, content: String) {
        // 3. Parser (Background Thread): AST analisa o bloco
        val astNode = editor.ast.AstParser.parseBlock(blockId, content)

        // 4. UI Update: O componente muda para refletir a nova AST de forma atômica
        _state.update { state ->
            val newBlocks = state.blocks.map { block ->
                if (block.id == blockId) {
                    block.copy(astNode = astNode)
                } else {
                    block
                }
            }
            state.copy(blocks = newBlocks)
        }
    }

    private sealed class BlockAction {
        data class Add(val afterId: String) : BlockAction()
        data class Remove(val id: String) : BlockAction()
        data class Move(val from: Int, val to: Int) : BlockAction()
    }

    private fun mutateBlocks(action: BlockAction) {
        _state.update { state ->
            val newBlocks = state.blocks.toMutableList()
            when (action) {
                is BlockAction.Add -> {
                    val index = state.blocks.indexOfFirst { it.id == action.afterId }
                    if (index != -1) {
                        val newBlock = EditorUtils.createInitialBlock()
                        newBlocks.add(index + 1, newBlock)
                        state.copy(blocks = newBlocks, focusedBlockId = newBlock.id)
                    } else state
                }
                is BlockAction.Remove -> {
                    if (state.blocks.size <= 1) return@update state
                    val index = state.blocks.indexOfFirst { it.id == action.id }
                    if (index == -1) return@update state
                    newBlocks.removeAt(index)
                    val newFocusId = if (index > 0) newBlocks[index - 1].id else newBlocks[0].id
                    state.copy(blocks = newBlocks, focusedBlockId = newFocusId)
                }
                is BlockAction.Move -> {
                    val block = newBlocks.removeAt(action.from)
                    newBlocks.add(action.to, block)
                    state.copy(blocks = newBlocks)
                }
            }
        }
    }



    private fun launchCreateNote() {
        coroutineScope.launch {
            val newId = EditorUtils.generateId()
            val initialBlock = EditorUtils.createInitialBlock()
            val newNote = Note(
                id = newId,
                title = "Untitled",
                content = "",
                attributes = emptyList(),
                connections = emptyList(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.saveNote(newNote)
            
            _state.update { 
                val newStack = it.navigationStack.toMutableList()
                newStack.add(newId)
                it.copy(
                    selectedNoteId = newId,
                    navigationStack = newStack,
                    blocks = listOf(initialBlock),
                    focusedBlockId = initialBlock.id,
                    showResonanceFilter = false,
                    originalThought = ""
                ) 
            }
        }
    }

}
