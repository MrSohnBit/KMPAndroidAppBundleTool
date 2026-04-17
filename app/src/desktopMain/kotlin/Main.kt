import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import mrsohn.project.aabtools.ui.screens.MainScreen
import mrsohn.project.aabtools.ui.theme.AABToolsTheme
import mrsohn.project.aabtools.viewmodel.ConversionViewModel

fun main() = application {
    val viewModel = ConversionViewModel()
    val icon = painterResource("icon.png")
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "AABTools - Your AAB to APK Converter",
        icon = icon
    ) {
        AABToolsTheme {
            MainScreen(viewModel)
        }
    }
}
