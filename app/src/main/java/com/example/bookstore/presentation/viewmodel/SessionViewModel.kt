package com.example.bookstore.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookstore.domain.model.UserSession
import com.example.bookstore.domain.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface SessionState {
    data object Loading : SessionState
    data class Authorized(val session: UserSession) : SessionState
    data object Unauthorized : SessionState
}

class SessionViewModel(
    userRepository: UserRepository
) : ViewModel() {

    val sessionState: StateFlow<SessionState> =
        userRepository.session
            .map { session ->
                if (session == null) SessionState.Unauthorized else SessionState.Authorized(session)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SessionState.Loading
            )
}
