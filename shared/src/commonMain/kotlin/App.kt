import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import editor.SynapseColors
import editor.MainScreen
import editor.EditorViewModel
import dev.synapse.database.DatabaseDriverFactory
import dev.synapse.database.SynapseDatabase
import dev.synapse.data.repository.NoteRepositoryImpl

import dev.synapse.util.PlatformContext

@Composable
fun App(context: PlatformContext) {
    val lightColors = lightColors(
        primary = SynapseColors.Primary,
        primaryVariant = SynapseColors.SurfaceContainerHighest,
        secondary = SynapseColors.Selection,
        background = SynapseColors.Background,
        surface = SynapseColors.Surface,
        onPrimary = SynapseColors.OnPrimary,
        onBackground = SynapseColors.OnSurface,
        onSurface = SynapseColors.OnSurface,
        error = SynapseColors.Error
    )

    MaterialTheme(colors = lightColors) {
        val coroutineScope = rememberCoroutineScope()
        val driver = remember { DatabaseDriverFactory(context).createDriver() }
        val database = remember { SynapseDatabase(driver) }
        val resonanceRepository = remember { dev.synapse.di.createResonanceRepository(database) }
        val noteRepository = remember { NoteRepositoryImpl(database, resonanceRepository) }
        val editorViewModel = remember { EditorViewModel(coroutineScope, noteRepository, resonanceRepository) }
        
        MainScreen(editorViewModel)
    }
}

expect fun getPlatformName(): String
