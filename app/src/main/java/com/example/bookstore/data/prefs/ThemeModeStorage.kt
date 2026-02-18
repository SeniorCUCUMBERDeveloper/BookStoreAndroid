package com.example.bookstore.data.prefs

import android.content.Context
import com.example.bookstore.domain.model.ThemeMode

object ThemeModeStorage {

    fun read(context: Context): ThemeMode {
        val rawValue = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME_MODE, null)
        return rawValue
            ?.let { saved -> ThemeMode.entries.firstOrNull { it.name == saved } }
            ?: ThemeMode.SYSTEM
    }

    fun write(context: Context, themeMode: ThemeMode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, themeMode.name)
            .apply()
    }

    private const val PREFS_NAME = "bookstore_theme"
    private const val KEY_THEME_MODE = "theme_mode"
}
