package com.novastore.app.data.fdroid

import android.content.Context
import android.util.Log
import com.novastore.app.data.fdroid.db.FdroidDatabase
import com.novastore.app.data.fdroid.db.FdroidEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object FdroidRepository {
    private const val TAG = "FdroidSync"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .build()

    private const val INDEX_URL = "https://f-droid.org/repo/index-v2.json"

    suspend fun sync(context: Context, onProgress: (Float) -> Unit = {}): SyncResult =
        withContext(Dispatchers.IO) {
            try {
                onProgress(0.05f)

                val request = Request.Builder().url(INDEX_URL).build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    val err = "HTTP ${response.code}: ${response.message}"
                    Log.e(TAG, err)
                    return@withContext SyncResult.Error(err)
                }

                val body = response.body ?: return@withContext SyncResult.Error("Empty body")
                val total = body.contentLength()
                Log.d(TAG, "Total size: $total bytes")

                val input = body.byteStream()
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var read: Int
                var totalRead = 0L

                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                    totalRead += read
                    if (total > 0) {
                        val downloadProgress = totalRead.toFloat() / total.toFloat()
                        onProgress(0.05f + downloadProgress * 0.45f)
                    }
                }

                Log.d(TAG, "Downloaded $totalRead bytes")
                onProgress(0.5f)

                val text = output.toString("UTF-8")
                Log.d(TAG, "Text length: ${text.length}")

                val apps = try {
                    FdroidParser.parseIndex(text)
                } catch (e: Exception) {
                    val err = "Parse error: ${e.message}"
                    Log.e(TAG, err, e)
                    return@withContext SyncResult.Error(err)
                }

                Log.d(TAG, "Parsed apps: ${apps.size}")
                onProgress(0.8f)

                if (apps.isEmpty()) {
                    return@withContext SyncResult.Error("Parsed 0 apps")
                }

                val entities = apps.map {
                    FdroidEntity(
                        packageName = it.packageName,
                        name = it.name,
                        summary = it.summary,
                        description = it.description,
                        author = it.author,
                        category = it.category,
                        iconUrl = it.iconUrl,
                        version = it.version,
                        versionCode = it.versionCode,
                        apkUrl = it.apkUrl
                    )
                }

                try {
                    val db = FdroidDatabase.get(context)
                    db.fdroidDao().clear()
                    db.fdroidDao().insertAll(entities)
                } catch (e: Exception) {
                    val err = "DB error: ${e.message}"
                    Log.e(TAG, err, e)
                    return@withContext SyncResult.Error(err)
                }

                onProgress(1f)
                SyncResult.Success(entities.size)
            } catch (e: Exception) {
                val err = "Network error: ${e.message}"
                Log.e(TAG, err, e)
                SyncResult.Error(err)
            }
        }

    suspend fun getPage(context: Context, limit: Int, offset: Int): List<FdroidEntity> =
        withContext(Dispatchers.IO) {
            FdroidDatabase.get(context).fdroidDao().getPage(limit, offset)
        }

    suspend fun searchPage(context: Context, query: String, limit: Int, offset: Int): List<FdroidEntity> =
        withContext(Dispatchers.IO) {
            FdroidDatabase.get(context).fdroidDao().searchPage(query, limit, offset)
        }

    suspend fun count(context: Context): Int =
        withContext(Dispatchers.IO) {
            FdroidDatabase.get(context).fdroidDao().count()
        }

    suspend fun searchCount(context: Context, query: String): Int =
        withContext(Dispatchers.IO) {
            FdroidDatabase.get(context).fdroidDao().searchCount(query)
        }

    suspend fun getByPackage(context: Context, pkg: String): FdroidEntity? =
        withContext(Dispatchers.IO) {
            FdroidDatabase.get(context).fdroidDao().getByPackage(pkg)
        }
}

sealed class SyncResult {
    data class Success(val count: Int) : SyncResult()
    data class Error(val message: String) : SyncResult()
}
