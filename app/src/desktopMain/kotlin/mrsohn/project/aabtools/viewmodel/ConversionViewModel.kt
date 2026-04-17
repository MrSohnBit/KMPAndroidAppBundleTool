package mrsohn.project.aabtools.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mrsohn.project.aabtools.service.*
import java.io.File

class ConversionViewModel(
    private val converter: AabConverter = AabConverter(),
    private val adbService: AdbService = AdbService(),
    private val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    var aabPath by mutableStateOf("")
        private set

    fun updateAabPath(path: String) {
        aabPath = path
        if (path.isNotEmpty()) {
            extractMetadata(File(path))
            if (outputDirPath.isEmpty()) {
                outputDirPath = File(path).parent ?: ""
            }
        } else {
            metadata = null
        }
    }

    var outputDirPath by mutableStateOf("")
    var keystorePath by mutableStateOf("")
    var keystorePassword by mutableStateOf("")
    var keyAlias by mutableStateOf("")
    var keyPassword by mutableStateOf("")

    var metadata by mutableStateOf<AabMetadata?>(null)
        private set

    val status: StateFlow<ConversionStatus> = converter.status

    var connectedDevices by mutableStateOf<List<AdbDevice>>(emptyList())
        private set
    
    var installationStatus by mutableStateOf<String?>(null)
        private set

    private fun extractMetadata(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            metadata = AabMetadataExtractor.extract(file)
        }
    }

    fun convert() {
        val aabFile = File(aabPath)
        if (!aabFile.exists() || !aabFile.isFile) {
            return
        }

        val outputDir = if (outputDirPath.isNotEmpty()) File(outputDirPath) else aabFile.parentFile
        val keystoreFile = if (keystorePath.isNotEmpty()) File(keystorePath) else null

        viewModelScope.launch {
            converter.convert(
                aabFile = aabFile,
                outputDir = outputDir,
                keystoreFile = keystoreFile,
                keystorePassword = keystorePassword.takeIf { it.isNotEmpty() },
                keyAlias = keyAlias.takeIf { it.isNotEmpty() },
                keyPassword = keyPassword.takeIf { it.isNotEmpty() }
            )
        }
    }

    fun refreshDevices() {
        viewModelScope.launch {
            connectedDevices = adbService.getConnectedDevices()
        }
    }

    fun installToDevice(device: AdbDevice, apkPath: String) {
        installationStatus = "Installing to ${device.serial}..."
        viewModelScope.launch {
            val result = adbService.installApk(device.serial, apkPath)
            installationStatus = result.fold(
                onSuccess = { "Install Success!" },
                onFailure = { "Install Error: ${it.message}" }
            )
        }
    }
}
