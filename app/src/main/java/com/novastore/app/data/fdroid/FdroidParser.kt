package com.novastore.app.data.fdroid

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull

object FdroidParser {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun parseIndexV1(text: String): List<FdroidAppData> {
        val root = json.parseToJsonElement(text).jsonObject
        val appsArray = root["apps"]?.jsonArray ?: return emptyList()
        val packagesArray = root["packages"]?.jsonArray ?: return emptyList()

        // Собираем все версии в map: packageName -> (version, versionCode, apkName)
        val versionsMap = mutableMapOf<String, Triple<String, Long, String>>()
        for (pkg in packagesArray) {
            try {
                val obj = pkg.jsonObject
                val pkgName = obj["packageName"]?.jsonPrimitive?.content ?: continue
                val versionName = obj["versionName"]?.jsonPrimitive?.content ?: ""
                val versionCode = obj["versionCode"]?.jsonPrimitive?.longOrNull ?: 0L
                val apkName = obj["apkName"]?.jsonPrimitive?.content ?: ""

                val current = versionsMap[pkgName]
                if (current == null || versionCode > current.second) {
                    versionsMap[pkgName] = Triple(versionName, versionCode, apkName)
                }
            } catch (e: Exception) {
            }
        }

        val result = mutableListOf<FdroidAppData>()
        for (app in appsArray) {
            try {
                val obj = app.jsonObject
                val pkgName = obj["packageName"]?.jsonPrimitive?.content ?: continue
                val name = obj["name"]?.jsonPrimitive?.content ?: pkgName
                val summary = obj["summary"]?.jsonPrimitive?.content ?: ""
                val description = obj["description"]?.jsonPrimitive?.content ?: ""
                val author = obj["authorName"]?.jsonPrimitive?.content
                    ?: obj["author"]?.jsonPrimitive?.content ?: ""
                val icon = obj["icon"]?.jsonPrimitive?.content ?: "icon.png"
                val license = obj["license"]?.jsonPrimitive?.content ?: ""

                val categoriesArray = obj["categories"]?.jsonArray
                val category = if (categoriesArray != null && categoriesArray.isNotEmpty())
                    categoriesArray[0].jsonPrimitive.content
                else "Other"

                val iconUrl = if (icon.startsWith("http")) icon
                    else "https://f-droid.org/repo/$pkgName/en-US/$icon"

                val versionInfo = versionsMap[pkgName]
                val version = versionInfo?.first ?: ""
                val versionCode = versionInfo?.second ?: 0L
                val apkName = versionInfo?.third ?: ""
                val apkUrl = if (apkName.isNotBlank())
                    "https://f-droid.org/repo/$apkName"
                else ""

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

    fun parseIndex(text: String): List<FdroidAppData> {
        // Оставляем для совместимости с v2 (не используется)
        return emptyList()
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
