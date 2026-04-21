package mrsohn.project.aabtools.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import mrsohn.project.aabtools.viewmodel.ConversionViewModel

@Preview
@Composable
fun MainScreenPreview() {
    MainScreen(viewModel = ConversionViewModel(), {})
}
