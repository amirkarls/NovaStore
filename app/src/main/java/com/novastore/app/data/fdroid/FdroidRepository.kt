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
import java.util.zip.ZipInputStream

object FdroidRepository {
    private const val TAG = "FdroidSync"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .build()

    private const val JAR_URL = "https://f-droid.org/repo/index-v1.jar"

    suspend fun sync(context: Context, onProgress: (Float) -> Unit = {}): SyncResult =
        withContext(Dispatchers.IO) {
            try {
                onProgress(0.05f)

                val request = Request.Builder().url(JAR_URL).build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    val err = "HTTP ${response.code}: ${response.message}"
                    Log.e(TAG, err)
                    return@withContext SyncResult.Error(err)
                }

                val body = response.body ?: return@withContext SyncResult.Error("Empty body")
                val total = body.contentLength()
                Log.d(TAG, "JAR size: $total bytes")

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
                        onProgress(0.05f + downloadProgress * 0.5f)
                    }
                }

                Log.d(TAG, "Downloaded $totalRead bytes")
                onProgress(0.55f)

                // Распаковываем JAR (это ZIP) и достаём index-v1.json
                val jarBytes = output.toByteArray()
                val jsonText = try {
                    extractIndexFromJar(jarBytes)
                } catch (e: Exception) {
                    val err = "Unzip error: ${e.javaClass.simpleName}: ${e.message}"
                    Log.e(TAG, err, e)
                    return@withContext SyncResult.Error(err)
                }

                if (jsonText.isNullOrBlank()) {
                    return@withContext SyncResult.Error("index-v1.json not found in JAR")
                }

                Log.d(TAG, "JSON length: ${jsonText.length}")
                onProgress(0.6f)

                val apps = try {
                    FdroidParser.parseIndexV1(jsonText)
                } catch (e: Exception) {
                    val err = "Parse error: ${e.javaClass.simpleName}: ${e.message}"
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
                    val err = "DB error: ${e.javaClass.simpleName}: ${e.message}"
                    Log.e(TAG, err, e)
                    return@withContext SyncResult.Error(err)
                }

                onProgress(1f)
                SyncResult.Success(entities.size)
            } catch (e: Exception) {
                val err = "Network error: ${e.javaClass.simpleName}: ${e.message}"
                Log.e(TAG, err, e)
                SyncResult.Error(err)
            }
        }

    private fun extractIndexFromJar(jarBytes: ByteArray): String? {
        val zis = ZipInputStream(jarBytes.inputStream())
        var entry = zis.nextEntry
        while (entry != null) {
            if (entry.name == "index-v1.json") {
                val out = ByteArrayOutputStream()
                val buf = ByteArray(8192)
                var n: Int
                while (zis.read(buf).also { n = it } != -1) {
                    out.write(buf, 0, n)
                }
                zis.closeEntry()
                zis.close()
                return out.toString("UTF-8")
            }
            zis.closeEntry()
            entry = zis.nextEntry
        }
        zis.close()
        return null
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
