package com.novastore.app.data.fdroid.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fdroid_apps")
data class FdroidEntity(
    @PrimaryKey val packageName: String,
    val name: String,
    val summary: String,
    val description: String,
    val author: String,
    val category: String,
    val iconUrl: String,
    val version: String,
    val versionCode: Long,
    val apkUrl: String
)
