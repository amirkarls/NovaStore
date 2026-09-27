package com.novastore.app.data

data class AppInfo(
    val name: String,
    val packageName: String,
    val description: String,
    val descriptionRu: String,
    val github: String?,
    val category: String,
    val iconUrl: String,
    val apkUrl: String? = null,
    val fdroid: Boolean = false
)
