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

@OptIn(FlowPreview::class)
class EditorViewModel(private val coroutineScope: CoroutineScope) {
    companion object {
        private const val DEBOUNCE_MS = 300L
        private const val DUMMY_TIMESTAMP_1 = 1713400000000L
        private const val DUMMY_TIMESTAMP_2 = 1713300000000L
        private const val DUMMY_TIMESTAMP_3 = 1713200000000L
        private const val ID_RADIX = 36
        private const val H1_LEVEL = 1
        private const val H2_LEVEL = 2
        private const val H3_LEVEL = 3

        private fun generateId(): String = Random.nextLong().toString(ID_RADIX)

        fun getDummyNotes(): List<Note> = listOf(
            Note("1", "Project Cerebro", "The AI-Native Cognitive Gym...", DUMMY_TIMESTAMP_1),
            Note("2", "Meeting Notes", "Discussed the new Fog of War feature.", DUMMY_TIMESTAMP_2),
            Note("3", "Rust Tauri Backend", "Tauri + Rust is blazingly fast.", DUMMY_TIMESTAMP_3)
        )

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
            textChangeFlow
                .debounce(DEBOUNCE_MS)
                .collectLatest { (blockId, content) ->
                    parseBlockBackground(blockId, content)
                }
        }
    }

    fun onEvent(event: EditorUiEvent) {
        when (event) {
            is EditorUiEvent.LoadNotes -> {
                _state.update { it.copy(notes = getDummyNotes(), isLoading = false) }
            }
            is EditorUiEvent.SelectNote -> selectNote(event.noteId)
            is EditorUiEvent.UpdateBlockContent -> updateBlockContent(event.blockId, event.newContent)
            is EditorUiEvent.AddBlockAfter -> addBlockAfter(event.blockId)
            is EditorUiEvent.RemoveBlock -> removeBlock(event.blockId)
            is EditorUiEvent.MoveBlock -> moveBlock(event.fromIndex, event.toIndex)
            is EditorUiEvent.FocusBlock -> _state.update { it.copy(focusedBlockId = event.blockId) }
            is EditorUiEvent.CreateNewNote -> createNote()
            is EditorUiEvent.ToggleSidebar -> _state.update { 
                it.copy(isSidebarVisible = !it.isSidebarVisible) 
            }
            is EditorUiEvent.SaveCurrentNote -> handleSave(isCommit = false)
            is EditorUiEvent.TriggerSearch -> { /* Handle search */ }
            is EditorUiEvent.UpdateOriginalThought -> {
                _state.update { it.copy(originalThought = event.text) }
            }
            is EditorUiEvent.CommitNote -> handleSave(isCommit = true)
        }
    }

    private fun handleSave(isCommit: Boolean) {
        val currentThought = _state.value.originalThought.length
        val minThought = _state.value.minThoughtLength
        
        if (isCommit || currentThought < minThought) {
            if (currentThought >= minThought) {
                _state.update { it.copy(showResonanceFilter = false) }
                saveContent()
            } else {
                _state.update { it.copy(showResonanceFilter = true) }
            }
        } else {
            saveContent()
        }
    }



    private fun selectNote(noteId: String) {
        _state.value.notes.find { it.id == noteId } ?: return
        val updatedNotes = _state.value.notes.map { 
            if (it.id == noteId) it.copy(viewCount = it.viewCount + 1) else it 
        }
        
        val content = if (noteId == "1") {
            "# Project Cerebro\n\nThe AI-Native Cognitive Gym (Active Learning & Desirable Difficulty)."
        } else {
            "Content for $noteId"
        }
        
        val initialBlocks = content.split("\n\n").map { 
            TextBlock(
                id = generateId(), 
                rawContent = it, 
                astNode = editor.ast.NoteNode.BlockNode.Paragraph(
                    generateId(), 
                    listOf(editor.ast.NoteNode.InlineNode.Text(it))
                )
            ) 
        }
        // Note: For initial loading, we keep it simple for now as it's dummy data, 
        // but normally we would parse the full note AST.
        val blocks = initialBlocks.ifEmpty { listOf(createInitialBlock()) }
        
        _state.update { 
            it.copy(
                notes = updatedNotes,
                selectedNoteId = noteId,
                blocks = blocks,
                focusedBlockId = blocks.firstOrNull()?.id
            ) 
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

    private fun addBlockAfter(blockId: String) {
        _state.update { state ->
            val index = state.blocks.indexOfFirst { it.id == blockId }
            if (index == -1) return@update state
            val newBlock = createInitialBlock()
            val newBlocks = state.blocks.toMutableList()
            newBlocks.add(index + 1, newBlock)
            state.copy(blocks = newBlocks, focusedBlockId = newBlock.id)
        }
    }

    private fun removeBlock(blockId: String) {
        _state.update { state ->
            if (state.blocks.size <= 1) return@update state // Don't remove the last block
            val index = state.blocks.indexOfFirst { it.id == blockId }
            if (index == -1) return@update state
            val newBlocks = state.blocks.toMutableList()
            newBlocks.removeAt(index)
            
            val newFocusId = if (index > 0) newBlocks[index - 1].id else newBlocks[0].id
            state.copy(blocks = newBlocks, focusedBlockId = newFocusId)
        }
    }

    private fun moveBlock(fromIndex: Int, toIndex: Int) {
        _state.update { state ->
            val newBlocks = state.blocks.toMutableList()
            val block = newBlocks.removeAt(fromIndex)
            newBlocks.add(toIndex, block)
            state.copy(blocks = newBlocks)
        }
    }



    private fun createNote() {
        val newNote = Note("new_id", "Untitled", "", System.currentTimeMillis())
        val initialBlock = createInitialBlock()
        _state.update { 
            it.copy(
                notes = listOf(newNote) + it.notes,
                selectedNoteId = newNote.id,
                blocks = listOf(initialBlock),
                focusedBlockId = initialBlock.id,
                showResonanceFilter = false,
                originalThought = ""
            ) 
        }
    }

    private fun saveContent() {
        val fullContent = _state.value.blocks.joinToString("\n\n") { it.rawContent }
        val allAttributes = _state.value.blocks.flatMap { it.astNode.attributes.entries }
            .associate { it.key to it.value }
        
        println("--- VAULT COMMIT ---")
        println("Content length: ${fullContent.length}")
        println("Attributes: $allAttributes")
        println("Resonance Synthesis: ${_state.value.originalThought}")
        println("---------------------")
        
        // Finalize state
        _state.update { it.copy(originalThought = "", showResonanceFilter = false) }
    }
}
