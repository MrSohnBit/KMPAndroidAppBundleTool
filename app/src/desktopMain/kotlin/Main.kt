import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import mrsohn.project.aabtools.ui.screens.MainScreen
import mrsohn.project.aabtools.ui.theme.AABToolsTheme
import mrsohn.project.aabtools.viewmodel.ConversionViewModel
import java.awt.Window as AwtWindow

val LocalWindow = staticCompositionLocalOf<AwtWindow?> { null }

fun main() = application {
    val viewModel = ConversionViewModel()
    val icon = painterResource("icon.png")
    val windowState = rememberWindowState(size = DpSize(800.dp, 760.dp))
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "AABTools - Your AAB to APK Converter",
        state = windowState,
        resizable = false,
        icon = icon
    ) {
        AABToolsTheme {
            CompositionLocalProvider(LocalWindow provides this.window) {
                MainScreen(viewModel)
            }
        }
    }
}
