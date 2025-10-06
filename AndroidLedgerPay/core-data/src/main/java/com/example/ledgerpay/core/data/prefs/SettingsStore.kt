package com.example.ledgerpay.core.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

object Keys { val BaseUrl = stringPreferencesKey("base_url") }

class SettingsStore(private val context: Context) {
    val baseUrl: Flow<String> = context.dataStore.data.map { prefs: Preferences ->
        prefs[Keys.BaseUrl] ?: "https://example.org/"
    }
    suspend fun setBaseUrl(url: String) { context.dataStore.edit { it[Keys.BaseUrl] = url } }
}
