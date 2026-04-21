package editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.synapse.domain.model.NoteCollection

@Composable
fun CollectionDot(
    collection: NoteCollection?,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 8.dp
) {
    val color = remember(collection) {
        try {
            if (collection?.color != null) {
                Color(collection.color.removePrefix("#").prependZeroIfShort().toLong(16) or 0xFF000000L)
            } else {
                SynapseColors.CategoryRaw
            }
        } catch (e: Exception) {
            SynapseColors.CategoryRaw
        }
    }
    Box(
        modifier = modifier
            .size(size)
            .background(color, shape = androidx.compose.foundation.shape.CircleShape)
    )
}

private fun String.prependZeroIfShort(): String = if (this.length == 6) this else "0$this"

@Composable
fun CollectionTag(
    currentCollectionId: String,
    collections: List<NoteCollection>,
    onCollectionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val currentCollection = collections.find { it.id == currentCollectionId }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .background(
                    SynapseColors.SurfaceContainerHigh,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CollectionDot(currentCollection, size = 6.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = (currentCollection?.name ?: "RAW").uppercase(),
                style = TextStyle(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = SynapseColors.OnSurfaceVariant
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SynapseColors.SurfaceContainerLowest)
        ) {
            collections.forEach { coll ->
                DropdownMenuItem(onClick = {
                    onCollectionSelected(coll.id)
                    expanded = false
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CollectionDot(coll, size = 8.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = coll.name,
                            style = TextStyle(fontSize = 14.sp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CollectionManagementDialog(
    collection: NoteCollection?,
    onDismiss: () -> Unit,
    onSave: (name: String, color: String) -> Unit,
    onDelete: (() -> Unit)? = null,
    error: String? = null
) {
    var name by remember { mutableStateOf(collection?.name ?: "") }
    var selectedColor by remember { mutableStateOf(collection?.color ?: "#4CAF50") }

    val colors = listOf(
        "#4CAF50", // Green
        "#2196F3", // Blue
        "#FFC107", // Amber
        "#F44336", // Red
        "#9C27B0", // Purple
        "#00BCD4", // Cyan
        "#FF5722", // Deep Orange
        "#795548"  // Brown
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = SynapseColors.SurfaceContainerLow,
        title = {
            Text(
                text = if (collection == null) "Create Collection" else "Edit Collection",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = SynapseColors.Primary
            )
        },
        text = {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Collection Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = SynapseColors.OnSurface,
                        focusedBorderColor = SynapseColors.Primary,
                        unfocusedBorderColor = SynapseColors.OnSurfaceVariant.copy(alpha = 0.3f),
                        cursorColor = SynapseColors.Primary
                    )
                )
                
                if (error != null) {
                    Text(
                        text = error,
                        style = TextStyle(fontSize = 12.sp, color = SynapseColors.Error),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "COLLECTION COLOR",
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = SynapseColors.OnSurfaceVariant.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { colorHex ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    Color(colorHex.removePrefix("#").toLong(16) or 0xFF000000L),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                                .clickable { selectedColor = colorHex }
                                .let { 
                                    if (selectedColor == colorHex) {
                                        it.border(
                                            2.dp, 
                                            SynapseColors.Primary, 
                                            androidx.compose.foundation.shape.CircleShape
                                        )
                                    } else it
                                }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, selectedColor) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = SynapseColors.Primary,
                    contentColor = Color.White
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null && collection != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = SynapseColors.Error)
                    ) {
                        Text("Delete")
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = SynapseColors.OnSurfaceVariant)
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}
