package com.novastore.app.data

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
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

    suspend fun resolveGitHubUrl(repo: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.github.com/repos/$repo/releases/latest")
                .header("Accept", "application/vnd.github+json")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            val assets = json.optJSONArray("assets") ?: return@withContext null
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.optString("name", "")
                if (name.endsWith(".apk")) {
                    return@withContext asset.optString("browser_download_url")
                }
            }
            null
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
        return resolveGitHubUrl(repo)
    }

    fun enqueue(context: Context, url: String, fileName: String): Long {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription("Скачивание из NovaStore")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setMimeType("application/vnd.android.package-archive")
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return dm.enqueue(request)
    }
}
