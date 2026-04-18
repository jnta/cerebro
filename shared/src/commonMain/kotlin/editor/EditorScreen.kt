package editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
    val editorFocusRequester = remember { FocusRequester() }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .onKeyEvent { event ->
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
            
            MarkdownEditorArea(
                content = state.currentNoteContent,
                onContentChange = { viewModel.onEvent(EditorUiEvent.UpdateNoteContent(it)) },
                focusRequester = editorFocusRequester,
                modifier = Modifier.fillMaxSize()
            )
        }
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
            items(notes) { note ->
                NoteListItem(
                    note = note,
                    isSelected = note.id == selectedNoteId,
                    onClick = { onEvent(EditorUiEvent.SelectNote(note.id)) }
                )
            }
        }
    }
}

@Composable
fun NoteListItem(note: Note, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent
    
    Column(
        modifier = Modifier
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
fun EditorToolbar(isSidebarVisible: Boolean, onEvent: (EditorUiEvent) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
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
        Text("Markdown Editor (In-Place)", style = MaterialTheme.typography.subtitle2, color = Color.Gray)
    }
}

@Composable
fun MarkdownEditorArea(
    content: String,
    onContentChange: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(16.dp)) {
        BasicTextField(
            value = content,
            onValueChange = onContentChange,
            textStyle = TextStyle(
                color = MaterialTheme.colors.onBackground,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 24.sp
            ),
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester),
            decorationBox = { innerTextField ->
                if (content.isEmpty()) {
                    Text(
                        text = "Start capturing your thoughts...",
                        color = Color.Gray
                    )
                }
                innerTextField()
            }
        )
    }
}
