package mrsohn.project.aabtools.service

import com.android.tools.build.bundletool.androidtools.Aapt2Command
import com.android.tools.build.bundletool.commands.BuildApksCommand
import com.android.tools.build.bundletool.model.Password
import com.android.tools.build.bundletool.model.SignerConfig
import com.android.tools.build.bundletool.model.SigningConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.Optional
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

sealed class ConversionStatus {
    object Idle : ConversionStatus()
    data class Processing(val message: String) : ConversionStatus()
    data class Success(val apkPath: String) : ConversionStatus()
    data class Error(val message: String) : ConversionStatus()
}

class AabConverter {
    private val _status = MutableStateFlow<ConversionStatus>(ConversionStatus.Idle)
    val status: StateFlow<ConversionStatus> = _status

    suspend fun convert(
        aabFile: File,
        outputDir: File,
        keystoreFile: File? = null,
        keystorePassword: String? = null,
        keyAlias: String? = null,
        keyPassword: String? = null
    ) = withContext(Dispatchers.IO) {
        try {
            _status.value = ConversionStatus.Processing("Searching aapt2...")

            val aapt2Path = resolveAapt2Path()
                ?: throw IllegalStateException(
                    "aapt2를 찾을 수 없습니다. Android SDK Build Tools가 설치되어 있는지 확인하세요."
                )

            _status.value = ConversionStatus.Processing("Generating APKS...")

            val apksFile = File(outputDir, aabFile.nameWithoutExtension + ".apks")

            val commandBuilder = BuildApksCommand.builder()
                .setAapt2Command(Aapt2Command.createFromExecutablePath(aapt2Path))
                .setBundlePath(aabFile.toPath())
                .setOutputFile(apksFile.toPath())
                .setOverwriteOutput(true)
                .setApkBuildMode(BuildApksCommand.ApkBuildMode.UNIVERSAL)

            if (keystoreFile != null && keystorePassword != null && keyAlias != null && keyPassword != null) {
                val ksPassword = Password.createFromStringValue(keystorePassword)
                val kPassword = Password.createFromStringValue(keyPassword)

                val signerConfig = SignerConfig.extractFromKeystore(
                    keystoreFile.toPath(),
                    keyAlias,
                    Optional.of(ksPassword),
                    Optional.of(kPassword)
                )
                
                val signingConfig = SigningConfiguration.builder()
                    .setSignerConfig(signerConfig)
                    .build()

                commandBuilder.setSigningConfiguration(signingConfig)
            }

            commandBuilder.build().execute()

            _status.value = ConversionStatus.Processing("Extracting APK from APKS...")
            val finalApkName = aabFile.nameWithoutExtension + ".apk"
            val extractedApk = unzipAndRenameUniversalApk(apksFile.toPath(), outputDir.toPath(), finalApkName)

            apksFile.delete()

            if (extractedApk != null) {
                _status.value = ConversionStatus.Success(extractedApk.absolutePath)
            } else {
                _status.value = ConversionStatus.Error("Failed to extract universal APK from APKS.")
            }
        } catch (e: Exception) {
            _status.value = ConversionStatus.Error("Conversion failed: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun resolveAapt2Path(): Path? {
        val sdkRoot = listOf(
            System.getenv("ANDROID_SDK_ROOT"),
            System.getenv("ANDROID_HOME")
        ).firstOrNull { !it.isNullOrBlank() } ?: return null

        val buildToolsDir = File(sdkRoot, "build-tools")
        if (!buildToolsDir.exists() || !buildToolsDir.isDirectory) return null

        val aapt2Name = if (System.getProperty("os.name").lowercase().contains("win")) {
            "aapt2.exe"
        } else {
            "aapt2"
        }

        val candidates = buildToolsDir.listFiles()
            ?.filter { it.isDirectory }
            ?.sortedByDescending { it.name }
            ?: return null

        for (versionDir in candidates) {
            val candidate = File(versionDir, aapt2Name)
            if (candidate.exists() && candidate.isFile) {
                return candidate.toPath()
            }
        }

        return null
    }

    private fun unzipAndRenameUniversalApk(sourceZip: Path, targetDir: Path, finalName: String): File? {
        val tempExtractDir = targetDir.resolve("temp_extract_" + System.currentTimeMillis())
        Files.createDirectories(tempExtractDir)

        var universalApk: File? = null

        ZipInputStream(FileInputStream(sourceZip.toFile())).use { zis ->
            var zipEntry: ZipEntry? = zis.nextEntry
            while (zipEntry != null) {
                val newPath = zipSlipProtect(zipEntry, tempExtractDir)
                if (!zipEntry.isDirectory) {
                    if (newPath.parent != null && Files.notExists(newPath.parent)) {
                        Files.createDirectories(newPath.parent)
                    }

                    if (newPath.fileName.toString().endsWith(".apk")) {
                        val finalApkPath = targetDir.resolve(finalName)
                        Files.copy(zis, finalApkPath, StandardCopyOption.REPLACE_EXISTING)
                        universalApk = finalApkPath.toFile()
                    }
                }
                zipEntry = zis.nextEntry
            }
        }

        tempExtractDir.toFile().deleteRecursively()

        return universalApk
    }

    private fun zipSlipProtect(zipEntry: ZipEntry, targetDir: Path): Path {
        val targetDirResolved = targetDir.resolve(zipEntry.name)
        val normalizePath = targetDirResolved.normalize()
        if (!normalizePath.startsWith(targetDir)) {
            throw java.io.IOException("Bad zip entry: " + zipEntry.name)
        }
        return normalizePath
    }
}
