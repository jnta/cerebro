package editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.TextFieldValue
import dev.synapse.domain.model.Note
import dev.synapse.domain.model.NoteMetadata

import dev.synapse.domain.model.NoteCollection

@Composable
fun NoteListItem(
    note: Note,
    collections: List<NoteCollection>,
    isSelected: Boolean,
    onSelect: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val collection = collections.find { it.id == note.collectionId }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(note.id) }
            .background(if (isSelected) SynapseColors.SurfaceContainerHighest else Color.Transparent)
            .padding(vertical = 12.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CollectionDot(collection, modifier = Modifier.padding(end = 12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = note.title.ifEmpty { "Untitled" },
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) SynapseColors.Primary else SynapseColors.OnSurface
                )
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatTimestamp(note.updatedAt),
                    style = TextStyle(fontSize = 10.sp, color = Color.Gray)
                )
            }
        }
        
        DeleteButton(onDelete = { onDelete(note.id) })
    }
}

fun formatTimestamp(timestamp: Long): String {
    return "${timestamp % 24}h ago"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteHeader(
    note: Note,
    collections: List<NoteCollection>,
    onEvent: (EditorUiEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
    ) {
        CollectionTag(
            currentCollectionId = note.collectionId,
            collections = collections,
            onCollectionSelected = { onEvent(EditorUiEvent.UpdateNoteCollection(it)) },
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            note.attributes.filter { it.key == "tag" }.forEach { tag ->
                Box(
                    modifier = Modifier
                        .background(
                            SynapseColors.SurfaceContainerHigh,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tag.value.uppercase(),
                        style = TextStyle(
                            fontSize = SynapseTypography.TagFontSize,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = SynapseColors.OnSurfaceVariant
                    )
                }
            }
        }
        
        androidx.compose.foundation.text.BasicTextField(
            value = note.title,
            onValueChange = { onEvent(EditorUiEvent.UpdateNoteTitle(it)) },
            textStyle = TextStyle(
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1.5).sp,
                lineHeight = 52.sp,
                color = SynapseColors.Primary
            ),
            modifier = Modifier.fillMaxWidth(),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(SynapseColors.Primary),
            decorationBox = { innerTextField ->
                if (note.title.isEmpty()) {
                    Text(
                        text = "Untitled Note",
                        style = TextStyle(
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1.5).sp,
                            lineHeight = 52.sp,
                            color = SynapseColors.OnSurfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                }
                innerTextField()
            }
        )
    }
}



@Composable
fun LeftNav(
    state: EditorUiState,
    onEvent: (EditorUiEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .width(SynapseDimensions.LeftNavWidth)
            .fillMaxHeight()
            .background(SynapseColors.Panel)
            .padding(vertical = 32.dp, horizontal = 24.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 32.dp).padding(horizontal = 8.dp)) {
            Text(
                text = "Synapse",
                style = TextStyle(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    letterSpacing = (-0.5).sp,
                    color = SynapseColors.Primary
                )
            )
            Text(
                text = "THE DIGITAL CURATOR",
                style = TextStyle(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp,
                    color = SynapseColors.OnSurfaceVariant.copy(alpha = 0.4f)
                )
            )
        }

        Button(
            onClick = { onEvent(EditorUiEvent.CreateNewNote) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = SynapseColors.Primary,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.elevation(0.dp, 0.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "New Note",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        NavigationList(activeItem = state.currentDestination, onEvent = onEvent)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "COLLECTIONS",
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = SynapseColors.OnSurfaceVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )

        CollectionList(
            collections = state.collections,
            selectedCollectionIds = state.selectedCollectionIds,
            onEvent = onEvent
        )

        Spacer(modifier = Modifier.weight(1f))

        UserProfileSection()
    }
}


@Composable
fun SynapseHeader(
    state: EditorUiState,
    onEvent: (EditorUiEvent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(SynapseColors.Panel)
            .padding(horizontal = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Page Title or Breadcrumb
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (state.currentDestination == "Editor") "EDITOR" else "VAULT",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = SynapseColors.OnSurfaceVariant.copy(alpha = 0.4f)
                )
            )
        }

        // Center: Search Bar
        Row(
            modifier = Modifier
                .width(480.dp)
                .height(40.dp)
                .background(SynapseColors.SurfaceContainerLowest, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = SynapseColors.OnSurfaceVariant.copy(alpha = 0.5f)
            )
            
            Box(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                if (state.searchQuery.isEmpty()) {
                    Text(
                        text = "Search knowledge...",
                        style = TextStyle(fontSize = 13.sp, color = SynapseColors.OnSurfaceVariant.copy(alpha = 0.3f))
                    )
                }
                BasicTextField(
                    value = state.searchQuery,
                    onValueChange = { onEvent(EditorUiEvent.UpdateSearchQuery(it)) },
                    textStyle = TextStyle(fontSize = 13.sp, color = SynapseColors.Primary),
                    modifier = Modifier.fillMaxWidth(),
                    cursorBrush = SolidColor(SynapseColors.Primary),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier
                    .background(SynapseColors.SurfaceContainer, shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchModeButton(
                    label = "HYBRID",
                    active = state.searchMode == SearchMode.HYBRID,
                    onClick = { onEvent(EditorUiEvent.UpdateSearchMode(SearchMode.HYBRID)) }
                )
                SearchModeButton(
                    label = "SEMANTIC",
                    active = state.searchMode == SearchMode.SEMANTIC,
                    onClick = { onEvent(EditorUiEvent.UpdateSearchMode(SearchMode.SEMANTIC)) }
                )
                SearchModeButton(
                    label = "EXACT",
                    active = state.searchMode == SearchMode.EXACT,
                    onClick = { onEvent(EditorUiEvent.UpdateSearchMode(SearchMode.EXACT)) }
                )
            }
        }

        // Right side: Actions
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.currentDestination == "Editor" && state.noteId.isNotEmpty()) {
                DeleteButton(onDelete = { onEvent(EditorUiEvent.DeleteNote(state.noteId)) })
            }
            
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null,
                tint = SynapseColors.OnSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SearchModeButton(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (active) SynapseColors.SurfaceContainerLowest else Color.Transparent,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (active) SynapseColors.Primary else SynapseColors.OnSurfaceVariant.copy(alpha = 0.4f)
            )
        )
    }
}

@Composable
fun BreadcrumbTrail(
    navigationStack: List<String>,
    activeNoteId: String,
    noteSummaries: List<NoteMetadata>,
    onEvent: (EditorUiEvent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SynapseDimensions.BreadcrumbHorizontalPadding, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        navigationStack.forEachIndexed { index, noteId ->
            val noteTitle = noteSummaries.find { it.id == noteId }?.title ?: "Untitled"
            
            Text(
                text = noteTitle,
                style = MaterialTheme.typography.caption,
                color = if (noteId == activeNoteId) SynapseColors.Primary else Color.Gray,
                modifier = Modifier.clickable { onEvent(EditorUiEvent.SelectNote(noteId)) }
            )
            
            if (index < navigationStack.lastIndex) {
                Text(
                    text = " / ",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray.copy(alpha = 0.2f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyEditorState(onEvent: (EditorUiEvent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Select or create a note to begin", 
            style = MaterialTheme.typography.h6,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onEvent(EditorUiEvent.CreateNewNote) },
            colors = ButtonDefaults.buttonColors(backgroundColor = SynapseColors.Primary, contentColor = Color.White)
        ) {
            Text("Create New Note")
        }
    }
}

@Composable
fun ResonanceFilterModal(
    state: EditorUiState,
    onEvent: (EditorUiEvent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Force interaction */ },
        backgroundColor = SynapseColors.Selection,
        contentColor = Color.White,
        title = { Text("The Resonance Filter", style = MaterialTheme.typography.h6) },
        text = {
            Column {
                Text(
                    "Silent saves are blocked. Friction as a feature.",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                val synthesisText = "Synthesis required: Add at least ${state.minThoughtLength} " +
                    "characters of 'Original Thought' to commit this note."
                Text(
                    text = synthesisText,
                    style = MaterialTheme.typography.body1
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.originalThought,
                    onValueChange = { 
                        onEvent(EditorUiEvent.UpdateOriginalThought(it)) 
                    },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    placeholder = { Text("Synthesis/Reflection...", color = Color.Gray) },
                    label = { Text("Original Thought") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                Text(
                    "${state.originalThought.length} / ${state.minThoughtLength}",
                    modifier = Modifier.align(Alignment.End),
                    style = MaterialTheme.typography.caption,
                    color = if (state.originalThought.length >= state.minThoughtLength) {
                        SynapseColors.Success 
                    } else {
                        SynapseColors.Error
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onEvent(EditorUiEvent.CommitNote) },
                enabled = state.originalThought.length >= state.minThoughtLength,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (state.originalThought.length >= state.minThoughtLength) {
                        SynapseColors.Primary 
                    } else {
                        Color.Gray
                    },
                    contentColor = Color.White
                )
            ) {
                Text("Commit to Vault")
            }
        }
    )
}
@Composable
fun DeleteButton(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVerifying by remember { mutableStateOf(false) }
    
    Button(
        onClick = {
            if (isVerifying) {
                onDelete()
                isVerifying = false
            } else {
                isVerifying = true
            }
        },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isVerifying) SynapseColors.Error else Color.Transparent,
            contentColor = if (isVerifying) Color.White else SynapseColors.OnSurfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = ButtonDefaults.elevation(0.dp, 0.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                modifier = Modifier.size(16.dp)
            )
            if (isVerifying) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "CONFIRM",
                    style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
