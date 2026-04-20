package editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import dev.synapse.domain.model.NoteCategory

@Composable
fun CategoryDot(
    category: NoteCategory,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 8.dp
) {
    val color = when (category) {
        NoteCategory.RAW -> SynapseColors.CategoryRaw
        NoteCategory.LIT -> SynapseColors.CategoryLit
        NoteCategory.EVERGREEN -> SynapseColors.CategoryEvergreen
    }
    Box(
        modifier = modifier
            .size(size)
            .background(color, shape = androidx.compose.foundation.shape.CircleShape)
    )
}

@Composable
fun CategoryTag(
    category: NoteCategory,
    onCategorySelected: (NoteCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

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
            CategoryDot(category, size = 6.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category.displayName.uppercase(),
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
            NoteCategory.entries.forEach { cat ->
                DropdownMenuItem(onClick = {
                    onCategorySelected(cat)
                    expanded = false
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryDot(cat, size = 8.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = cat.displayName,
                            style = TextStyle(fontSize = 14.sp)
                        )
                    }
                }
            }
        }
    }
}
