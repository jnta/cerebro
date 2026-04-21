package editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.synapse.domain.model.NoteCollection

@Composable
fun NavigationList(activeItem: String, onEvent: (EditorUiEvent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        NavigationItem("All Notes", Icons.AutoMirrored.Filled.List, activeItem == "All Notes") {
            onEvent(EditorUiEvent.NavigateTo("All Notes"))
        }
        NavigationItem("Search", Icons.Default.Search, activeItem == "Search") {
            onEvent(EditorUiEvent.NavigateTo("Search"))
        }
        NavigationItem("Graph", Icons.Default.Hub, activeItem == "Graph") {
            onEvent(EditorUiEvent.NavigateTo("Graph"))
        }
    }
}

@Composable
private fun NavigationItem(
    label: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    active: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (active) SynapseColors.SurfaceContainer else Color.Transparent,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (active) SynapseColors.Primary else SynapseColors.OnSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                color = if (active) SynapseColors.Primary else SynapseColors.OnSurfaceVariant.copy(alpha = 0.7f)
            )
        )
    }
}

@Composable
fun CollectionList(
    collections: List<NoteCollection>,
    selectedCollectionIds: Set<String>,
    onEvent: (EditorUiEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        collections.forEach { collection ->
            CollectionItem(
                collection = collection,
                isSelected = selectedCollectionIds.contains(collection.id),
                onEvent = onEvent
            )
        }
        
        // Add Collection Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEvent(EditorUiEvent.ShowCreateCollectionDialog) }
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = SynapseColors.OnSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Add Collection",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = SynapseColors.OnSurfaceVariant.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
private fun CollectionItem(
    collection: NoteCollection,
    isSelected: Boolean,
    onEvent: (EditorUiEvent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) SynapseColors.SurfaceContainer else Color.Transparent,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            )
            .clickable { onEvent(EditorUiEvent.ToggleCollectionFilter(collection.id)) }
            .padding(vertical = 6.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CollectionDot(collection, size = 6.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = collection.name,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) SynapseColors.Primary else SynapseColors.OnSurfaceVariant.copy(alpha = 0.7f)
            )
        )
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit",
            modifier = Modifier
                .size(14.dp)
                .clickable { onEvent(EditorUiEvent.EditCollection(collection)) },
            tint = SynapseColors.OnSurfaceVariant.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun UserProfileSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(SynapseColors.SurfaceContainerHigh, shape = androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = SynapseColors.OnSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Curator Mode",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SynapseColors.Primary
                )
            )
            Text(
                text = "Settings",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = SynapseColors.OnSurfaceVariant.copy(alpha = 0.5f)
                )
            )
        }
    }
}
