package com.example.bookstore.data.prefs

import android.content.Context
import com.example.bookstore.domain.model.ThemeMode
import com.example.bookstore.domain.repository.ThemeRepository
import com.example.bookstore.presentation.theme.ThemeModeApplier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeRepositoryImpl(
    context: Context
) : ThemeRepository {

    private val appContext = context.applicationContext
    private val themeModeState = MutableStateFlow(ThemeModeStorage.read(appContext))

    override val themeMode: Flow<ThemeMode> = themeModeState.asStateFlow()

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        if (themeModeState.value == themeMode) return

        ThemeModeStorage.write(appContext, themeMode)
        themeModeState.value = themeMode
        ThemeModeApplier.apply(themeMode)
    }
}
