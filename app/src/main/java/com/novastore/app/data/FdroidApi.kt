package com.novastore.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File

data class FdroidApp(
    val name: String,
    val packageName: String,
    val summary: String,
    val description: String = "",
    val author: String = "",
    val category: String = "Other",
    val iconUrl: String,
    val version: String = "",
    val versionCode: Long = 0L,
    val apkUrl: String = ""
)

sealed class FdroidResult {
    data class Success(val apps: List<FdroidApp>) : FdroidResult()
    data class Error(val message: String) : FdroidResult()
}

object FdroidApi {
    private val client = OkHttpClient()

    private const val INDEX_URL = "https://f-droid.org/repo/index-v2.json"
    private const val CACHE_FILE = "fdroid_index.json"
    private const val CACHE_MAX_AGE_MS = 24 * 60 * 60 * 1000L

    suspend fun loadCatalog(context: Context, forceRefresh: Boolean = false): FdroidResult =
        withContext(Dispatchers.Default) {
            try {
                val cacheFile = File(context.filesDir, CACHE_FILE)
                val needDownload = forceRefresh ||
                    !cacheFile.exists() ||
                    (System.currentTimeMillis() - cacheFile.lastModified() > CACHE_MAX_AGE_MS)

                val json: String = if (needDownload) {
                    val downloaded = downloadIndex()
                        ?: return@withContext FdroidResult.Error("no_connection")
                    cacheFile.writeText(downloaded)
                    downloaded
                } else {
                    cacheFile.readText()
                }

                val apps = parseIndex(json)
                if (apps.isEmpty()) {
                    FdroidResult.Error("empty_catalog")
                } else {
                    FdroidResult.Success(apps)
                }
            } catch (e: Exception) {
                FdroidResult.Error(e.message ?: "unknown_error")
            }
        }

    private fun downloadIndex(): String? {
        return try {
            val request = Request.Builder().url(INDEX_URL).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return null
            response.body?.string()
        } catch (e: Exception) {
            null
        }
    }

    private fun parseIndex(json: String): List<FdroidApp> {
        val result = mutableListOf<FdroidApp>()
        try {
            val root = JSONObject(json)
            val packages = root.optJSONObject("packages") ?: return emptyList()

            val keys = packages.keys()
            while (keys.hasNext()) {
                val pkgName = keys.next()
                try {
                    val pkg = packages.getJSONObject(pkgName)
                    val metadata = pkg.optJSONObject("metadata") ?: continue

                    val name = metadata.optString("name", pkgName)
                    val summary = metadata.optString("summary", "")
                    val description = metadata.optString("description", "")
                    val authorName = metadata.optString("authorName", "")
                    val categories = metadata.optJSONArray("categories")
                    val category = if (categories != null && categories.length() > 0)
                        categories.getString(0) else "Other"

                    val iconObj = metadata.optJSONObject("icon")
                    val iconName = iconObj?.optString("name", "icon.png") ?: "icon.png"
                    val iconUrl = "https://f-droid.org/repo/$pkgName/en-US/$iconName"

                    val versions = pkg.optJSONObject("versions")
                    var version = ""
                    var versionCode = 0L
                    var apkUrl = ""
                    if (versions != null) {
                        val vKeys = versions.keys()
                        var best = 0L
                        while (vKeys.hasNext()) {
                            val vKey = vKeys.next()
                            val v = versions.getJSONObject(vKey)
                            val code = v.optLong("versionCode", 0L)
                            if (code > best) {
                                best = code
                                version = v.optString("versionName", vKey)
                                versionCode = code
                                val fileObj = v.optJSONObject("file")
                                val fileName = fileObj?.optString("name", "") ?: ""
                                if (fileName.isNotBlank()) {
                                    apkUrl = "https://f-droid.org/repo/$fileName"
                                }
                            }
                        }
                    }

                    if (apkUrl.isNotBlank()) {
                        result.add(
                            FdroidApp(
                                name = name,
                                packageName = pkgName,
                                summary = summary,
                                description = description,
                                author = authorName,
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
        } catch (e: Exception) {
        }
        return result.sortedBy { it.name.lowercase() }
    }
}
