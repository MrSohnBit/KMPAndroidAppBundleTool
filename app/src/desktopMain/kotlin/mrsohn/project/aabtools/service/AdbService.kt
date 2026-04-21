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
        // 1. Try environment variables
        val sdkRoot = listOf(
            System.getenv("ANDROID_SDK_ROOT"),
            System.getenv("ANDROID_HOME")
        ).firstOrNull { !it.isNullOrBlank() }

        if (sdkRoot != null) {
            val adb = findAdbInSdk(sdkRoot)
            if (adb != null) return adb
        }

        // 2. Try common installation paths
        val home = System.getProperty("user.home")
        val commonPaths = if (System.getProperty("os.name").lowercase().contains("win")) {
            val localAppData = System.getenv("LOCALAPPDATA")
            listOfNotNull(
                localAppData?.let { "$it\\Android\\Sdk" },
                "$home\\AppData\\Local\\Android\\Sdk"
            )
        } else if (System.getProperty("os.name").lowercase().contains("mac")) {
            listOf("$home/Library/Android/sdk")
        } else {
            listOf("$home/Android/Sdk")
        }

        for (path in commonPaths) {
            val adb = findAdbInSdk(path)
            if (adb != null) return adb
        }

        // 3. Try to find in PATH using 'which' or 'where'
        try {
            val whichCmd = if (System.getProperty("os.name").lowercase().contains("win")) "where" else "which"
            val process = ProcessBuilder(whichCmd, "adb").start()
            val output = process.inputStream.bufferedReader().readText().trim()
            if (process.waitFor() == 0 && output.isNotEmpty()) {
                val adbFile = File(output.lineSequence().first())
                if (adbFile.exists() && adbFile.isFile) return adbFile.toPath()
            }
        } catch (e: Exception) {
            // Ignore
        }

        return null
    }

    private fun findAdbInSdk(sdkRoot: String): Path? {
        val adbName = if (System.getProperty("os.name").lowercase().contains("win")) "adb.exe" else "adb"
        val adbFile = File(File(sdkRoot, "platform-tools"), adbName)
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
