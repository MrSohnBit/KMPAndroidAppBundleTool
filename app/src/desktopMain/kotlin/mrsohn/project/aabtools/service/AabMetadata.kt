package mrsohn.project.aabtools.service

import com.android.tools.build.bundletool.model.AppBundle
import java.io.File
import java.util.zip.ZipFile

data class AabMetadata(
    val packageName: String,
    val versionCode: Int,
    val versionName: String
)

object AabMetadataExtractor {
    fun extract(aabFile: File): AabMetadata? {
        return try {
            val bundle = AppBundle.buildFromZip(ZipFile(aabFile))
            val manifest = bundle.baseModule.androidManifest
            
            AabMetadata(
                packageName = manifest.packageName,
                versionCode = manifest.versionCode.orElse(0) ?: 0,
                versionName = manifest.versionName.orElse("N/A") ?: "N/A"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
