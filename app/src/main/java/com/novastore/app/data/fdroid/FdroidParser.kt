package com.novastore.app.data.fdroid

import android.util.Log
import kotlinx.serialization.json.*
import java.io.InputStream

object FdroidParser {
    private const val TAG = "FdroidParser"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun parseIndexV1(text: String): List<FdroidAppData> {
        val root = json.parseToJsonElement(text).jsonObject

        val appsElement = root["apps"]
        if (appsElement !is JsonObject) {
            Log.e(TAG, "apps is not JsonObject! Type: ${appsElement?.javaClass?.simpleName}")
            throw IllegalArgumentException("apps is not a JsonObject, it's ${appsElement?.javaClass?.simpleName}")
        }
        val appsObj = appsElement.jsonObject

        val packagesElement = root["packages"]
        if (packagesElement !is JsonObject) {
            Log.e(TAG, "packages is not JsonObject! Type: ${packagesElement?.javaClass?.simpleName}")
            throw IllegalArgumentException("packages is not a JsonObject, it's ${packagesElement?.javaClass?.simpleName}")
        }
        val packagesObj = packagesElement.jsonObject

        val versionsMap = mutableMapOf<String, Triple<String, Long, String>>()

        for ((pkgName, versionsElement) in packagesObj) {
            try {
                if (versionsElement !is JsonArray) {
                    Log.w(TAG, "packages[$pkgName] is not JsonArray, skipping. Type: ${versionsElement.javaClass.simpleName}")
                    continue
                }
                for (vElement in versionsElement) {
                    if (vElement !is JsonObject) continue
                    val v = vElement.jsonObject
                    val versionName = v["versionName"]?.jsonPrimitive?.content ?: ""
                    val versionCode = v["versionCode"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L
                    val apkName = v["apkName"]?.jsonPrimitive?.content ?: ""

                    val current = versionsMap[pkgName]
                    if (current == null || versionCode > current.second) {
                        versionsMap[pkgName] = Triple(versionName, versionCode, apkName)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing packages[$pkgName]: ${e.message}")
            }
        }

        val result = mutableListOf<FdroidAppData>()

        for ((pkgName, appElement) in appsObj) {
            try {
                if (appElement !is JsonObject) {
                    Log.w(TAG, "apps[$pkgName] is not JsonObject, skipping. Type: ${appElement.javaClass.simpleName}")
                    continue
                }
                val appObj = appElement.jsonObject

                val localized = appObj["localized"]?.jsonObject
                val enUS = localized?.get("en-US")?.jsonObject
                val ruRU = localized?.get("ru")?.jsonObject

                val name = ruRU?.get("name")?.jsonPrimitive?.content
                    ?: enUS?.get("name")?.jsonPrimitive?.content
                    ?: pkgName
                val summary = ruRU?.get("summary")?.jsonPrimitive?.content
                    ?: enUS?.get("summary")?.jsonPrimitive?.content
                    ?: ""
                val description = ruRU?.get("description")?.jsonPrimitive?.content
                    ?: enUS?.get("description")?.jsonPrimitive?.content
                    ?: ""

                val author = appObj["authorName"]?.jsonPrimitive?.content
                    ?: appObj["author"]?.jsonPrimitive?.content ?: ""
                val icon = appObj["icon"]?.jsonPrimitive?.content ?: "icon.png"

                val category = try {
                    val cats = appObj["categories"]
                    if (cats is JsonArray && cats.isNotEmpty())
                        cats[0].jsonPrimitive.content
                    else "Other"
                } catch (e: Exception) { "Other" }

                val iconUrl = if (icon.startsWith("http")) icon
                    else "https://f-droid.org/repo/$pkgName/en-US/$icon"

                val versionInfo = versionsMap[pkgName]
                val version = versionInfo?.first ?: ""
                val versionCode = versionInfo?.second ?: 0L
                val apkName = versionInfo?.third ?: ""
                val apkUrl = if (apkName.isNotBlank()) "https://f-droid.org/repo/$apkName" else ""

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
                Log.e(TAG, "Error parsing apps[$pkgName]: ${e.message}")
            }
        }

        return result.sortedBy { it.name.lowercase() }
    }

    fun parseIndex(text: String): List<FdroidAppData> = parseIndexV1(text)
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
