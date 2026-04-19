package mrsohn.project.aabtools.service

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class KeystoreProfile(
    val name: String,
    val path: String,
    val password: String,
    val alias: String,
    val keyPassword: String
)
