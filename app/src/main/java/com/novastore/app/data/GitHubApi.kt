package com.novastore.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object GitHubApi {
    private val client = OkHttpClient()

    suspend fun getLatestRelease(repo: String): ReleaseInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.github.com/repos/$repo/releases/latest")
                .header("Accept", "application/vnd.github+json")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            val tag = json.optString("tag_name", "")
            val assets = json.optJSONArray("assets")
            var apkUrl: String? = null
            var size = 0L
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk")) {
                        apkUrl = asset.optString("browser_download_url")
                        size = asset.optLong("size", 0)
                        break
                    }
                }
            }
            ReleaseInfo(tag, apkUrl, size)
        } catch (e: Exception) {
            null
        }
    }
}

data class ReleaseInfo(
    val version: String,
    val apkUrl: String?,
    val size: Long
)
