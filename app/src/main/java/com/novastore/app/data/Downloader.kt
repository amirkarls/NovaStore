package com.novastore.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object Downloader {
    private val client = OkHttpClient()

    suspend fun resolveFdroidUrl(packageName: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://f-droid.org/api/v1/packages/$packageName")
                .header("Accept", "application/json")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            val versionCode = json.optInt("suggestedVersionCode", 0)
            if (versionCode <= 0) return@withContext null
            "https://f-droid.org/repo/${packageName}_$versionCode.apk"
        } catch (e: Exception) {
            null
        }
    }

    suspend fun resolveApkUrl(app: AppInfo): String? {
        app.apkUrl?.let { return it }
        if (app.fdroid) {
            resolveFdroidUrl(app.packageName)?.let { return it }
        }
        val repo = app.github ?: return null
        return GitHubApi.getLatestRelease(repo)?.apkUrl
    }
}
