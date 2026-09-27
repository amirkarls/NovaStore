package com.novastore.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore by preferencesDataStore(name = "novastore_settings")

class Preferences(private val context: Context) {
    companion object {
        val GITHUB_TOKEN = stringPreferencesKey("github_token")
        val LANGUAGE = stringPreferencesKey("language")
        val LANGUAGE_SELECTED = booleanPreferencesKey("language_selected")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val ACCENT_COLOR = intPreferencesKey("accent_color")
        val DOWNLOAD_ID = longPreferencesKey("download_id")
        val DOWNLOAD_APP = stringPreferencesKey("download_app")
    }

    suspend fun getGithubToken(): String =
        context.dataStore.data.first()[GITHUB_TOKEN] ?: ""
    suspend fun setGithubToken(token: String) {
        context.dataStore.edit { it[GITHUB_TOKEN] = token }
    }

    suspend fun getLanguage(): String =
        context.dataStore.data.first()[LANGUAGE] ?: "ru"
    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[LANGUAGE] = lang }
    }

    suspend fun isLanguageSelected(): Boolean =
        context.dataStore.data.first()[LANGUAGE_SELECTED] ?: false
    suspend fun setLanguageSelected() {
        context.dataStore.edit { it[LANGUAGE_SELECTED] = true }
    }

    suspend fun getDynamicColor(): Boolean =
        context.dataStore.data.first()[DYNAMIC_COLOR] ?: true
    suspend fun setDynamicColor(value: Boolean) {
        context.dataStore.edit { it[DYNAMIC_COLOR] = value }
    }

    suspend fun getAccentColor(): Int =
        context.dataStore.data.first()[ACCENT_COLOR] ?: 0
    suspend fun setAccentColor(value: Int) {
        context.dataStore.edit { it[ACCENT_COLOR] = value }
    }

    suspend fun getDownloadId(): Long =
        context.dataStore.data.first()[DOWNLOAD_ID] ?: -1L
    suspend fun setDownloadId(id: Long) {
        context.dataStore.edit { it[DOWNLOAD_ID] = id }
    }

    suspend fun getDownloadApp(): String =
        context.dataStore.data.first()[DOWNLOAD_APP] ?: ""
    suspend fun setDownloadApp(pkg: String) {
        context.dataStore.edit { it[DOWNLOAD_APP] = pkg }
    }

    suspend fun clearDownload() {
        context.dataStore.edit {
            it[DOWNLOAD_ID] = -1L
            it[DOWNLOAD_APP] = ""
        }
    }
}
