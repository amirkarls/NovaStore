package com.novastore.app.data.fdroid

import android.content.Context
import com.novastore.app.data.fdroid.db.FdroidDatabase
import com.novastore.app.data.fdroid.db.FdroidEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

object FdroidRepository {
    private val client = OkHttpClient()
    private const val INDEX_URL = "https://f-droid.org/repo/index-v2.json"

    suspend fun sync(context: Context, onProgress: (Float) -> Unit = {}): Int =
        withContext(Dispatchers.IO) {
            try {
                onProgress(0.1f)

                val request = Request.Builder().url(INDEX_URL).build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) return@withContext -1

                val body = response.body ?: return@withContext -1
                val contentLength = body.contentLength()
                val bytes = body.bytes()

                onProgress(0.5f)

                val text = String(bytes, Charsets.UTF_8)
                val apps = FdroidParser.parseIndex(text)

                onProgress(0.8f)

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

                val db = FdroidDatabase.get(context)
                db.fdroidDao().clear()
                db.fdroidDao().insertAll(entities)

                onProgress(1f)
                entities.size
            } catch (e: Exception) {
                -1
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
