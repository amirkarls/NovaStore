package com.novastore.app.data.fdroid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FdroidIndex(
    val repo: FdroidRepo? = null,
    val packages: Map<String, FdroidPackage> = emptyMap()
)

@Serializable
data class FdroidRepo(
    val name: String = "",
    val address: String = "",
    val timestamp: Long = 0L
)

@Serializable
data class FdroidPackage(
    val metadata: FdroidMetadata? = null,
    val versions: Map<String, FdroidVersion> = emptyMap()
)

@Serializable
data class FdroidMetadata(
    val name: String = "",
    val summary: String = "",
    val description: String = "",
    @SerialName("authorName") val authorName: String = "",
    val categories: List<String> = emptyList(),
    val icon: FdroidIcon? = null,
    val license: String = ""
)

@Serializable
data class FdroidIcon(
    val name: String = "icon.png"
)

@Serializable
data class FdroidVersion(
    @SerialName("versionCode") val versionCode: Long = 0L,
    @SerialName("versionName") val versionName: String = "",
    val file: FdroidFile? = null
)

@Serializable
data class FdroidFile(
    val name: String = "",
    val sha256: String = "",
    val size: Long = 0L
)
