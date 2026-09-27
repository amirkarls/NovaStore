package com.novastore.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

object GitHubApi {
    private val client = OkHttpClient()

    suspend fun getReleases(repo: String): List<ReleaseInfo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.github.com/repos/$repo/releases?per_page=20")
                .header("Accept", "application/vnd.github+json")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            val array = JSONArray(body)
            val result = mutableListOf<ReleaseInfo>()
            for (i in 0 until array.length()) {
                val rel = array.getJSONObject(i)
                val tag = rel.optString("tag_name", "")
                val assets = rel.optJSONArray("assets")
                var apkUrl: String? = null
                var size = 0L
                if (assets != null) {
                    for (j in 0 until assets.length()) {
                        val asset = assets.getJSONObject(j)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk")) {
                            apkUrl = asset.optString("browser_download_url")
                            size = asset.optLong("size", 0)
                            break
                        }
                    }
                }
                if (apkUrl != null) {
                    result.add(ReleaseInfo(tag, apkUrl, size))
                }
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getLatestRelease(repo: String): ReleaseInfo? = getReleases(repo).firstOrNull()
}
