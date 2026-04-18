import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import editor.EditorScreen
import editor.EditorViewModel

import dev.synapse.database.DatabaseDriverFactory
import dev.synapse.database.SynapseDatabase
import dev.synapse.data.repository.NoteRepositoryImpl

@Composable
fun App() {
    MaterialTheme {
        val coroutineScope = rememberCoroutineScope()
        val database = remember { 
            SynapseDatabase(DatabaseDriverFactory().createDriver()) 
        }
        val noteRepository = remember { NoteRepositoryImpl(database) }
        val editorViewModel = remember { EditorViewModel(coroutineScope, noteRepository) }
        
        EditorScreen(editorViewModel)
    }
}

expect fun getPlatformName(): String
