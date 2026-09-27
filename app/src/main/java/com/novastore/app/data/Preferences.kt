package com.novastore.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore by preferencesDataStore(name = "novastore_settings")

class Preferences(private val context: Context) {
    companion object {
        val GITHUB_TOKEN = stringPreferencesKey("github_token")
        val LANGUAGE = stringPreferencesKey("language")
        val SHOWN_WARNING = booleanPreferencesKey("shown_warning")
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

    suspend fun wasWarningShown(): Boolean =
        context.dataStore.data.first()[SHOWN_WARNING] ?: false

    suspend fun setWarningShown() {
        context.dataStore.edit { it[SHOWN_WARNING] = true }
    }
}
