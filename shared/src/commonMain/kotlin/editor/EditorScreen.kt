package editor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditorScreen(viewModel: EditorViewModel) {
    val state by viewModel.state.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.isCtrlPressed && event.key == Key.N) {
                    viewModel.onEvent(EditorUiEvent.CreateNewNote)
                    true
                } else {
                    false
                }
            }
    ) {
        // Sidebar
        AnimatedVisibility(visible = state.isSidebarVisible) {
            Sidebar(
                notes = state.notes,
                selectedNoteId = state.selectedNoteId,
                onEvent = viewModel::onEvent,
                modifier = Modifier.width(300.dp).fillMaxHeight()
            )
        }

        Divider(modifier = Modifier.width(1.dp).fillMaxHeight())

        // Main Editor Area
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            EditorToolbar(
                isSidebarVisible = state.isSidebarVisible,
                onEvent = viewModel::onEvent
            )
            
            BlockEditorArea(
                blocks = state.blocks,
                focusedBlockId = state.focusedBlockId,
                shouldMask = (state.notes.find { 
                    it.id == state.selectedNoteId 
                }?.viewCount ?: 0) >= 2,
                onEvent = viewModel::onEvent,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        }
    }
    
    // Resonance Filter Modal
    if (state.showResonanceFilter) {
        AlertDialog(
            onDismissRequest = { /* Force interaction */ },
            title = { Text("The Resonance Filter", style = MaterialTheme.typography.h6) },
            text = {
                Column {
                    Text(
                        "Silent saves are blocked. Friction as a feature.",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Synthesis required: Add at least ${state.minThoughtLength} " +
                        "characters of 'Original Thought' to commit this note.",
                        style = MaterialTheme.typography.body1
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.originalThought,
                        onValueChange = { 
                            viewModel.onEvent(EditorUiEvent.UpdateOriginalThought(it)) 
                        },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        placeholder = { Text("Synthesis/Reflection...") },
                        label = { Text("Original Thought") }
                    )
                    Text(
                        "${state.originalThought.length} / ${state.minThoughtLength}",
                        modifier = Modifier.align(Alignment.End),
                        style = MaterialTheme.typography.caption,
                        color = if (state.originalThought.length >= state.minThoughtLength) {
                            Color.Green 
                        } else {
                            Color.Red
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onEvent(EditorUiEvent.CommitNote) },
                    enabled = state.originalThought.length >= state.minThoughtLength
                ) {
                    Text("Commit to Vault")
                }
            },
            dismissButton = {
                TextButton(onClick = { /* Could add cancel if needed */ }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun Sidebar(
    notes: List<Note>,
    selectedNoteId: String?,
    onEvent: (EditorUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(MaterialTheme.colors.surface)) {
        // Sidebar Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Cerebro Vault", style = MaterialTheme.typography.h6)
            Row {
                IconButton(onClick = { onEvent(EditorUiEvent.TriggerSearch) }) {
                    Icon(Icons.Default.Search, contentDescription = "Search Notes")
                }
                IconButton(onClick = { onEvent(EditorUiEvent.CreateNewNote) }) {
                    Icon(Icons.Default.Add, contentDescription = "New Note")
                }
            }
        }
        
        Divider()

        // Notes List
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(notes, key = { it.id }) { note ->
                val onClick = remember(note.id) { { onEvent(EditorUiEvent.SelectNote(note.id)) } }
                NoteListItem(
                    note = note,
                    isSelected = note.id == selectedNoteId,
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
fun NoteListItem(
    note: Note, 
    isSelected: Boolean, 
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Text(
            text = note.title.ifEmpty { "Untitled" },
            style = MaterialTheme.typography.subtitle1,
            color = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = note.snippet.ifEmpty { "No additional text..." },
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
            maxLines = 2
        )
    }
    Divider()
}

@Composable
fun EditorToolbar(
    isSidebarVisible: Boolean, 
    onEvent: (EditorUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isSidebarVisible) {
            IconButton(onClick = { onEvent(EditorUiEvent.ToggleSidebar) }) {
                Icon(Icons.Default.Menu, contentDescription = "Toggle Sidebar")
            }
        } else {
            IconButton(onClick = { onEvent(EditorUiEvent.ToggleSidebar) }) {
                Icon(Icons.Default.Menu, contentDescription = "Toggle Sidebar")
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text("Block Editor", style = MaterialTheme.typography.subtitle2, color = Color.Gray)
    }
}



@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BlockEditorArea(
    blocks: List<TextBlock>,
    focusedBlockId: String?,
    shouldMask: Boolean,
    onEvent: (EditorUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }

    LaunchedEffect(focusedBlockId) {
        focusedBlockId?.let { id ->
            focusRequesters[id]?.requestFocus()
        }
    }

    Box(modifier = modifier.padding(16.dp)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(blocks, key = { _, block -> block.id }) { index, block ->
                val focusRequester = remember { FocusRequester() }
                focusRequesters[block.id] = focusRequester

                BlockItem(
                    index = index,
                    block = block,
                    isFocused = focusedBlockId == block.id,
                    shouldMask = shouldMask,
                    focusRequester = focusRequester,
                    onEvent = onEvent
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BlockItem(
    index: Int,
    block: TextBlock,
    isFocused: Boolean,
    shouldMask: Boolean,
    focusRequester: FocusRequester,
    onEvent: (EditorUiEvent) -> Unit
) {
    var showSlashMenu by remember { mutableStateOf(false) }
    var textValue by remember(block.id) { 
        mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(block.rawContent)) 
    }

    LaunchedEffect(block.rawContent) {
        if (textValue.text != block.rawContent) {
            textValue = textValue.copy(
                text = block.rawContent,
                selection = androidx.compose.ui.text.TextRange(block.rawContent.length)
            )
        }
    }

    val intent = parseIntent(textValue.text)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(
                if (intent == BlockIntent.Theory) Color.Gray.copy(alpha = 0.1f) 
                else Color.Transparent
            ),
        verticalAlignment = Alignment.Top
    ) {
        BlockPrefix(index, intent, block)

        // Drag Handle (Visual Placeholder)
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Reorder",
            tint = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier
                .padding(top = 4.dp, end = 8.dp, start = if (intent != BlockIntent.Process) 8.dp else 4.dp)
                .size(20.dp)
        )

        Box(modifier = Modifier.weight(1f).padding(vertical = 4.dp)) {
            LaunchedEffect(textValue.text) {
                onEvent(EditorUiEvent.UpdateBlockContent(block.id, textValue.text))
                showSlashMenu = textValue.text.endsWith("/")
            }
            
            BlockTextField(
                textValue = textValue,
                onValueChange = { textValue = it },
                block = block,
                isFocused = isFocused,
                shouldMask = shouldMask,
                focusRequester = focusRequester,
                intent = intent,
                onEvent = onEvent
            )

            SlashCommandMenu(
                expanded = showSlashMenu,
                onDismiss = { showSlashMenu = false },
                onSelect = { prefix ->
                    onEvent(EditorUiEvent.UpdateBlockContent(block.id, prefix))
                    showSlashMenu = false
                }
            )
        }
    }
}


