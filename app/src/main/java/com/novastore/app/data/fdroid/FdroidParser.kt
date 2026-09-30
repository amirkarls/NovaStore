package com.novastore.app.data.fdroid

import kotlinx.serialization.json.Json

object FdroidParser {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun parseIndex(text: String): List<FdroidAppData> {
        val index = json.decodeFromString<FdroidIndex>(text)
        val result = mutableListOf<FdroidAppData>()

        for ((pkgName, pkg) in index.packages) {
            try {
                val meta = pkg.metadata ?: continue
                val name = meta.name.ifBlank { pkgName }
                val summary = meta.summary
                val description = meta.description
                val author = meta.authorName
                val category = meta.categories.firstOrNull() ?: "Other"
                val iconName = meta.icon?.name ?: "icon.png"
                val iconUrl = "https://f-droid.org/repo/$pkgName/en-US/$iconName"

                var version = ""
                var versionCode = 0L
                var apkUrl = ""

                for ((vKey, v) in pkg.versions) {
                    if (v.versionCode > versionCode) {
                        versionCode = v.versionCode
                        version = v.versionName.ifBlank { vKey }
                        val fileName = v.file?.name ?: ""
                        if (fileName.isNotBlank()) {
                            apkUrl = "https://f-droid.org/repo/$fileName"
                        }
                    }
                }

                if (apkUrl.isNotBlank()) {
                    result.add(
                        FdroidAppData(
                            name = name,
                            packageName = pkgName,
                            summary = summary,
                            description = description,
                            author = author,
                            category = category,
                            iconUrl = iconUrl,
                            version = version,
                            versionCode = versionCode,
                            apkUrl = apkUrl
                        )
                    )
                }
            } catch (e: Exception) {
            }
        }

        return result.sortedBy { it.name.lowercase() }
    }
}

data class FdroidAppData(
    val name: String,
    val packageName: String,
    val summary: String,
    val description: String,
    val author: String,
    val category: String,
    val iconUrl: String,
    val version: String,
    val versionCode: Long,
    val apkUrl: String
)
