package mrsohn.project.aabtools

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import mrsohn.project.aabtools.settings.WindowSettings
import mrsohn.project.aabtools.ui.screens.MainScreen
import mrsohn.project.aabtools.ui.theme.AABToolsTheme
import mrsohn.project.aabtools.viewmodel.ConversionViewModel
import org.jetbrains.compose.resources.painterResource
import java.io.File
import java.awt.Window as AwtWindow

val LocalWindow = staticCompositionLocalOf<AwtWindow?> { null }

fun main() = application {
    val viewModel = ConversionViewModel()
    val icon = painterResource(Res.drawable.icon)

    val settingsFile = File("window_settings.properties")
    val settings = WindowSettings(settingsFile)
    val savedState = settings.load()

    val windowState = rememberWindowState(
        position = savedState.position,
        size = savedState.size
    )
//    val windowState = rememberWindowState(size = DpSize(800.dp, 760.dp))

    
    Window(
        onCloseRequest = {
            settings.save(
                windowState.size.width.value.toInt(),
                windowState.size.height.value.toInt(),
                windowState.position.x.value.toInt(),
                windowState.position.y.value.toInt()
            )
            exitApplication()
        },
        title = "AABTools - Your AAB to APK Converter",
        state = windowState,
        resizable = false,
        icon = icon
    ) {
        AABToolsTheme {
            CompositionLocalProvider(LocalWindow provides this.window) {
                MainScreen(viewModel, exitApplication = ::exitApplication)
            }
        }
    }
}
