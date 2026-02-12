package com.example.bookstore.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookstore.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ThemeViewModel(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    val darkTheme: Flow<Boolean> = themeRepository.darkTheme

    fun toggle() {
        viewModelScope.launch {
            themeRepository.setDarkTheme(!darkTheme.first())
        }
    }
}
