import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable

import dev.synapse.util.PlatformContext

actual fun getPlatformName(): String = "Desktop"

@Composable fun MainView() = App(PlatformContext())

@Preview
@Composable
fun AppPreview() {
    App(PlatformContext())
}
