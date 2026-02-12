package com.example.bookstore.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookstore.domain.model.UserSession
import com.example.bookstore.domain.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SessionViewModel(
    userRepository: UserRepository
) : ViewModel() {

    val session: StateFlow<UserSession?> =
        userRepository.session.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
