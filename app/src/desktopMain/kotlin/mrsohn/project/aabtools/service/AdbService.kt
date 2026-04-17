package mrsohn.project.aabtools.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Path

data class AdbDevice(
    val serial: String,
    val model: String = ""
)

class AdbService {

    private fun resolveAdbPath(): Path? {
        val sdkRoot = listOf(
            System.getenv("ANDROID_SDK_ROOT"),
            System.getenv("ANDROID_HOME")
        ).firstOrNull { !it.isNullOrBlank() } ?: return null

        val platformToolsDir = File(sdkRoot, "platform-tools")
        if (!platformToolsDir.exists() || !platformToolsDir.isDirectory) return null

        val adbName = if (System.getProperty("os.name").lowercase().contains("win")) {
            "adb.exe"
        } else {
            "adb"
        }

        val adbFile = File(platformToolsDir, adbName)
        return if (adbFile.exists() && adbFile.isFile) adbFile.toPath() else null
    }

    suspend fun getConnectedDevices(): List<AdbDevice> = withContext(Dispatchers.IO) {
        val adbPath = resolveAdbPath() ?: return@withContext emptyList()
        try {
            val process = ProcessBuilder(adbPath.toString(), "devices", "-l")
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()

            output.lines()
                .filter { it.isNotBlank() && !it.startsWith("List of devices") }
                .mapNotNull { line ->
                    val parts = line.split(Regex("\\s+"))
                    if (parts.size >= 2 && parts[1] == "device") {
                        val serial = parts[0]
                        val model = parts.find { it.startsWith("model:") }?.removePrefix("model:") ?: ""
                        AdbDevice(serial, model)
                    } else {
                        null
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun installApk(deviceSerial: String, apkPath: String): Result<String> = withContext(Dispatchers.IO) {
        val adbPath = resolveAdbPath() ?: return@withContext Result.failure(Exception("ADB not found"))
        try {
            val process = ProcessBuilder(adbPath.toString(), "-s", deviceSerial, "install", "-r", apkPath)
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            if (exitCode == 0 && output.contains("Success")) {
                Result.success("Successfully installed to $deviceSerial")
            } else {
                Result.failure(Exception("Installation failed: $output"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
