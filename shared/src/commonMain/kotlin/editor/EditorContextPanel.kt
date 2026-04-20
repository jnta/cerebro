package editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.synapse.domain.model.NoteMetadata

@Composable
fun ContextPanel(
    state: EditorUiState,
    onEvent: (EditorUiEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .width(SynapseDimensions.ContextPanelWidth)
            .fillMaxHeight()
            .background(SynapseColors.Panel)
            .padding(16.dp)
    ) {
        // Forward Links
        if (state.forwardLinks.isNotEmpty()) {
            Text(
                text = "Forward Links".uppercase(),
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.Gray.copy(alpha = 0.5f)
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(state.forwardLinks) { note ->
                    LinkCard(note, onEvent)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Back Links
        if (state.backLinks.isNotEmpty()) {
            Text(
                text = "Back Links".uppercase(),
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.Gray.copy(alpha = 0.5f)
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(state.backLinks) { note ->
                    LinkCard(note, onEvent)
                }
            }
        }
    }
}


@Composable
fun LinkCard(note: NoteMetadata, onEvent: (EditorUiEvent) -> Unit) {
    Card(
        backgroundColor = SynapseColors.SurfaceContainerLowest,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
        elevation = 0.dp,
        modifier = Modifier.fillMaxWidth().clickable { onEvent(EditorUiEvent.SelectNote(note.id)) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = note.title.ifEmpty { "Untitled" },
                style = MaterialTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold),
                color = SynapseColors.OnSurface
            )
            
            // Meta-Pills
            if (note.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    note.tags.take(3).forEach { tag ->
                        Text(
                            text = "#${tag.uppercase()}",
                            style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp),
                            color = SynapseColors.OnSurfaceVariant,
                            modifier = Modifier
                                .background(
                                    SynapseColors.SurfaceContainerHigh,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
