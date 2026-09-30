package com.novastore.app.data.fdroid

import android.util.Log
import kotlinx.serialization.json.*

object FdroidParser {
    private const val TAG = "FdroidParser"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * Универсальный парсер index-v1.json.
     * Поддерживает и старый формат (apps/packages = JsonObject),
     * и новый (apps/packages = JsonArray).
     */
    fun parseIndexV1(text: String): List<FdroidAppData> {
        val root = json.parseToJsonElement(text).jsonObject

        // === APPS ===
        val appsMap: Map<String, JsonObject> = when (val appsElement = root["apps"]) {
            is JsonObject -> appsElement.mapValues { it.value.jsonObject }

            is JsonArray -> appsElement.mapNotNull { el ->
                val obj = el.jsonObjectOrNull() ?: return@mapNotNull null
                val pkg = obj["packageName"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                pkg to obj
            }.toMap()

            else -> {
                Log.e(TAG, "apps is neither object nor array: ${appsElement?.javaClass?.simpleName}")
                throw IllegalArgumentException("apps is not object/array, it's ${appsElement?.javaClass?.simpleName}")
            }
        }

        // === PACKAGES ===
        // Каждый элемент packages: либо {versions: [...]}, либо [...]
        val versionsMap = mutableMapOf<String, Triple<String, Long, String>>()

        when (val packagesElement = root["packages"]) {
            is JsonObject -> {
                for ((pkgName, versionsElement) in packagesElement) {
                    parseVersions(pkgName, versionsElement, versionsMap)
                }
            }

            is JsonArray -> {
                for (el in packagesElement) {
                    val obj = el.jsonObjectOrNull() ?: continue
                    val pkg = obj["packageName"]?.jsonPrimitive?.contentOrNull ?: continue
                    val versions = obj["versions"] ?: obj["version"] ?: continue
                    parseVersions(pkg, versions, versionsMap)
                }
            }

            else -> {
                Log.w(TAG, "packages is neither object nor array, skipping versions: ${packagesElement?.javaClass?.simpleName}")
            }
        }

        // === Собираем результат ===
        val result = mutableListOf<FdroidAppData>()

        for ((pkgName, appObj) in appsMap) {
            try {
                val localized = appObj["localized"]?.jsonObjectOrNull()
                val enUS = localized?.get("en-US")?.jsonObjectOrNull()
                val ruRU = localized?.get("ru")?.jsonObjectOrNull()

                val name = ruRU?.get("name")?.jsonPrimitive?.contentOrNull
                    ?: enUS?.get("name")?.jsonPrimitive?.contentOrNull
                    ?: pkgName
                val summary = ruRU?.get("summary")?.jsonPrimitive?.contentOrNull
                    ?: enUS?.get("summary")?.jsonPrimitive?.contentOrNull
                    ?: ""
                val description = ruRU?.get("description")?.jsonPrimitive?.contentOrNull
                    ?: enUS?.get("description")?.jsonPrimitive?.contentOrNull
                    ?: ""

                val author = appObj["authorName"]?.jsonPrimitive?.contentOrNull
                    ?: appObj["author"]?.jsonPrimitive?.contentOrNull ?: ""
                val icon = appObj["icon"]?.jsonPrimitive?.contentOrNull ?: "icon.png"

                val category = try {
                    val cats = appObj["categories"]
                    if (cats is JsonArray && cats.isNotEmpty())
                        cats[0].jsonPrimitive.contentOrNull ?: "Other"
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

        Log.d(TAG, "Parsed ${result.size} apps from ${appsMap.size} entries")
        return result.sortedBy { it.name.lowercase() }
    }

    /**
     * Разбор массива версий (или объекта версий) для одного пакета.
     * Записывает в versionsMap самую свежую версию (по versionCode).
     */
    private fun parseVersions(
        pkgName: String,
        versionsElement: JsonElement,
        versionsMap: MutableMap<String, Triple<String, Long, String>>
    ) {
        val versions: List<JsonObject> = when (versionsElement) {
            is JsonArray -> versionsElement.mapNotNull { it.jsonObjectOrNull() }
            is JsonObject -> listOf(versionsElement)
            else -> return
        }

        for (v in versions) {
            try {
                val versionName = v["versionName"]?.jsonPrimitive?.contentOrNull ?: ""
                val versionCode = v["versionCode"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: 0L
                val apkName = v["apkName"]?.jsonPrimitive?.contentOrNull ?: ""

                val current = versionsMap[pkgName]
                if (current == null || versionCode > current.second) {
                    versionsMap[pkgName] = Triple(versionName, versionCode, apkName)
                }
            } catch (e: Exception) {
                // пропускаем битую версию
            }
        }
    }

    fun parseIndex(text: String): List<FdroidAppData> = parseIndexV1(text)

    // === Хелперы ===
    private fun JsonElement.jsonObjectOrNull(): JsonObject? =
        this as? JsonObject

    private fun JsonElement?.contentOrNull(): String? =
        (this as? JsonPrimitive)?.contentOrNull

    private val JsonPrimitive.contentOrNull: String?
        get() = if (this is JsonNull) null else content
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
