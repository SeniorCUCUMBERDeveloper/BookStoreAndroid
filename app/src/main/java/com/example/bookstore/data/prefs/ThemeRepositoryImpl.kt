package com.example.bookstore.data.prefs

import android.content.Context
import android.content.res.Configuration
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.bookstore.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class ThemeRepositoryImpl(
    private val context: Context
) : ThemeRepository {

    override val darkTheme: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_DARK_THEME] ?: isSystemInDarkTheme()
        }

    override suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_THEME] = enabled }
    }

    private companion object {
        val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    private fun isSystemInDarkTheme(): Boolean {
        val currentNightMode =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}
