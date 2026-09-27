package com.novastore.app.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

object DownloadService {
    fun enqueue(context: Context, url: String, fileName: String): Long {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription("Скачивание из NovaStore")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "NovaStore/$fileName"
            )
            .setMimeType("application/vnd.android.package-archive")
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return dm.enqueue(request)
    }

    fun getProgress(context: Context, id: Long): DownloadProgress {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(id)
        val cursor = dm.query(query)
        if (cursor != null && cursor.moveToFirst()) {
            val downloaded = cursor.getLong(
                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            )
            val total = cursor.getLong(
                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            )
            val status = cursor.getInt(
                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
            )
            cursor.close()
            return DownloadProgress(downloaded, total, status)
        }
        cursor?.close()
        return DownloadProgress(0, 0, DownloadManager.STATUS_PENDING)
    }
}

data class DownloadProgress(val downloaded: Long, val total: Long, val status: Int) {
    val percent: Float
        get() = if (total > 0) downloaded.toFloat() / total.toFloat() else 0f
}
