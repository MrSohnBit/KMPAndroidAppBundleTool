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
    private val keystoreStorage: KeystoreStorage = KeystoreStorage(),
    private val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    var aabPath by mutableStateOf("")
        private set

    var savedProfiles by mutableStateOf<List<KeystoreProfile>>(emptyList())
        private set

    init {
        loadKeystoreProfiles()
        loadRecentConfig()
    }

    private fun loadKeystoreProfiles() {
        viewModelScope.launch(Dispatchers.IO) {
            savedProfiles = keystoreStorage.loadProfiles()
        }
    }

    private fun loadRecentConfig() {
        viewModelScope.launch(Dispatchers.IO) {
            keystoreStorage.loadRecentConfig()?.let { profile ->
                _keystorePath = profile.path
                _keystorePassword = profile.password
                _keyAlias = profile.alias
                _keyPassword = profile.keyPassword
            }
        }
    }

    private fun saveRecentConfig() {
        val current = KeystoreProfile("recent", keystorePath, keystorePassword, keyAlias, keyPassword)
        viewModelScope.launch(Dispatchers.IO) {
            keystoreStorage.saveRecentConfig(current)
        }
    }

    fun applyProfile(profile: KeystoreProfile) {
        keystorePath = profile.path
        keystorePassword = profile.password
        keyAlias = profile.alias
        keyPassword = profile.keyPassword
        // saveRecentConfig() is called within the setters
    }

    fun saveCurrentAsProfile(name: String) {
        if (keystorePath.isEmpty() || keystorePassword.isEmpty() || keyAlias.isEmpty() || keyPassword.isEmpty()) return
        
        val newProfile = KeystoreProfile(name, keystorePath, keystorePassword, keyAlias, keyPassword)
        val updatedList = savedProfiles.toMutableList().apply {
            removeIf { it.name == name || it.path == keystorePath }
            add(0, newProfile)
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            keystoreStorage.saveProfiles(updatedList)
            savedProfiles = updatedList
        }
    }

    fun deleteProfile(profile: KeystoreProfile) {
        val updatedList = savedProfiles.filter { it != profile }
        viewModelScope.launch(Dispatchers.IO) {
            keystoreStorage.saveProfiles(updatedList)
            savedProfiles = updatedList
        }
    }

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
    
    private var _keystorePath by mutableStateOf("")
    var keystorePath: String
        get() = _keystorePath
        set(value) {
            _keystorePath = value
            saveRecentConfig()
        }

    private var _keystorePassword by mutableStateOf("")
    var keystorePassword: String
        get() = _keystorePassword
        set(value) {
            _keystorePassword = value
            saveRecentConfig()
        }

    private var _keyAlias by mutableStateOf("")
    var keyAlias: String
        get() = _keyAlias
        set(value) {
            _keyAlias = value
            saveRecentConfig()
        }

    private var _keyPassword by mutableStateOf("")
    var keyPassword: String
        get() = _keyPassword
        set(value) {
            _keyPassword = value
            saveRecentConfig()
        }

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

    fun reset() {
        aabPath = ""
        metadata = null
        installationStatus = null
        converter.resetStatus()
    }
}
