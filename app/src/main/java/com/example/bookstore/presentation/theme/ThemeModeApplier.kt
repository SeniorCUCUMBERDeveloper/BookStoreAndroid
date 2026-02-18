package com.example.bookstore.presentation.theme

import androidx.appcompat.app.AppCompatDelegate
import com.example.bookstore.domain.model.ThemeMode

object ThemeModeApplier {

    fun apply(themeMode: ThemeMode) {
        AppCompatDelegate.setDefaultNightMode(
            when (themeMode) {
                ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            }
        )
    }
}
