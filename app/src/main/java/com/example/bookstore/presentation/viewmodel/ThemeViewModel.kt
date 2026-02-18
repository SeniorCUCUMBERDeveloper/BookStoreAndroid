package com.example.bookstore.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookstore.domain.model.ThemeMode
import com.example.bookstore.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ThemeLoadState {
    data object Loading : ThemeLoadState
    data class Ready(val themeMode: ThemeMode) : ThemeLoadState
}

class ThemeViewModel(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    val themeLoadState: StateFlow<ThemeLoadState> =
        themeRepository.themeMode
            .map<ThemeMode, ThemeLoadState> { ThemeLoadState.Ready(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = ThemeLoadState.Loading
            )

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            themeRepository.setThemeMode(themeMode)
        }
    }
}
