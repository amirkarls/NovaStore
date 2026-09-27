package com.novastore.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore by preferencesDataStore(name = "novastore_settings")

class Preferences(private val context: Context) {
    companion object {
        val GITHUB_TOKEN = stringPreferencesKey("github_token")
    }

    suspend fun getGithubToken(): String =
        context.dataStore.data.first()[GITHUB_TOKEN] ?: ""

    suspend fun setGithubToken(token: String) {
        context.dataStore.edit { it[GITHUB_TOKEN] = token }
    }
}
