package com.example.bookstore.data.prefs

import android.content.Context
import com.example.bookstore.presentation.theme.ThemeModeApplier

object ThemeStartupInitializer {

    fun initialize(context: Context) {
        ThemeModeApplier.apply(ThemeModeStorage.read(context))
    }
}
