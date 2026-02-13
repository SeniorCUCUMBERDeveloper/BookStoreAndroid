package com.example.bookstore.domain.repository

import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    val darkTheme: Flow<Boolean>
    suspend fun setDarkTheme(enabled: Boolean)
}
