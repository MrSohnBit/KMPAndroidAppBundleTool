package mrsohn.project.aabtools.service

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

class KeystoreStorage {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    
    private val listType = Types.newParameterizedType(List::class.java, KeystoreProfile::class.java)
    private val adapter = moshi.adapter<List<KeystoreProfile>>(listType)

    private val storageFile = File(System.getProperty("user.home"), ".aabtools/keystores.json")
    private val recentConfigFile = File(System.getProperty("user.home"), ".aabtools/recent_config.json")
    private val profileAdapter = moshi.adapter(KeystoreProfile::class.java)

    init {
        storageFile.parentFile.mkdirs()
    }

    fun loadRecentConfig(): KeystoreProfile? {
        return try {
            if (recentConfigFile.exists()) {
                profileAdapter.fromJson(recentConfigFile.readText())
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveRecentConfig(profile: KeystoreProfile) {
        try {
            val json = profileAdapter.toJson(profile)
            recentConfigFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadProfiles(): List<KeystoreProfile> {
        return try {
            if (storageFile.exists()) {
                adapter.fromJson(storageFile.readText()) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun saveProfiles(profiles: List<KeystoreProfile>) {
        try {
            val json = adapter.toJson(profiles)
            storageFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
