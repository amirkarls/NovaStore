package com.novastore.app.data.fdroid

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

object FdroidParser {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun parseIndexV1(text: String): List<FdroidAppData> {
        val root = json.parseToJsonElement(text).jsonObject

        // "apps" и "packages" — это ОБЪЕКТЫ (Map), а не массивы!
        val appsObj = root["apps"]?.jsonObject ?: return emptyList()
        val packagesObj = root["packages"]?.jsonObject ?: return emptyList()

        // Собираем версии: packageName -> (versionName, versionCode, apkName)
        val versionsMap = mutableMapOf<String, Triple<String, Long, String>>()

        for ((pkgName, pkgElement) in packagesObj) {
            try {
                val pkgArr = pkgElement as? kotlinx.serialization.json.JsonArray ?: continue
                for (verElement in pkgArr) {
                    val v = verElement.jsonObject
                    val versionName = v["versionName"]?.jsonPrimitive?.content ?: ""
                    val versionCode = v["versionCode"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L
                    val apkName = v["apkName"]?.jsonPrimitive?.content ?: ""

                    val current = versionsMap[pkgName]
                    if (current == null || versionCode > current.second) {
                        versionsMap[pkgName] = Triple(versionName, versionCode, apkName)
                    }
                }
            } catch (e: Exception) {
            }
        }

        val result = mutableListOf<FdroidAppData>()

        for ((pkgName, appElement) in appsObj) {
            try {
                val obj = appElement.jsonObject
                val name = obj["name"]?.jsonPrimitive?.content ?: pkgName
                val summary = obj["summary"]?.jsonPrimitive?.content ?: ""
                val description = obj["description"]?.jsonPrimitive?.content ?: ""
                val author = obj["authorName"]?.jsonPrimitive?.content
                    ?: obj["author"]?.jsonPrimitive?.content ?: ""
                val icon = obj["icon"]?.jsonPrimitive?.content ?: "icon.png"

                val category = try {
                    val cats = obj["categories"]
                    if (cats is kotlinx.serialization.json.JsonArray && cats.isNotEmpty())
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
            }
        }

        return result.sortedBy { it.name.lowercase() }
    }

    fun parseIndex(text: String): List<FdroidAppData> {
        return parseIndexV1(text)
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
