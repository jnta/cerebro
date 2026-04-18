import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import editor.EditorScreen
import editor.EditorViewModel

@Composable
fun App() {
    MaterialTheme {
        val coroutineScope = rememberCoroutineScope()
        val editorViewModel = remember { EditorViewModel(coroutineScope) }
        
        EditorScreen(editorViewModel)
    }
}

expect fun getPlatformName(): String