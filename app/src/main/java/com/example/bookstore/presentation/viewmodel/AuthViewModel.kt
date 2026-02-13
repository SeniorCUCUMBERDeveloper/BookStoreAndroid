package com.example.bookstore.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookstore.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val mode: AuthMode = AuthMode.Login,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val message: String? = null
)

enum class AuthMode {
    Login,
    Register
}

class AuthViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    fun setMode(mode: AuthMode) {
        _state.update { it.copy(mode = mode, message = null) }
    }

    fun setName(v: String) {
        _state.update { it.copy(name = v, message = null) }
    }

    fun setEmail(v: String) {
        _state.update { it.copy(email = v, message = null) }
    }

    fun setPassword(v: String) {
        _state.update { it.copy(password = v, message = null) }
    }

    fun submit() {
        val s = _state.value
        if (s.loading) return

        val trimmedEmail = s.email.trim()
        val trimmedName = s.name.trim()

        if (trimmedEmail.isBlank()) {
            _state.update { it.copy(message = "Введите email") }
            return
        }

        if (s.password.isBlank()) {
            _state.update { it.copy(message = "Введите пароль") }
            return
        }

        if (s.mode == AuthMode.Register && trimmedName.isBlank()) {
            _state.update { it.copy(message = "Введите имя") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null) }
            try {
                if (s.mode == AuthMode.Login) {
                    userRepository.login(trimmedEmail, s.password)
                } else {
                    userRepository.register(trimmedName, trimmedEmail, s.password)
                }
                _state.update { it.copy(loading = false, message = null) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, message = e.message ?: "Ошибка") }
            }
        }
    }

    fun resetPassword() {
        val email = _state.value.email.trim()
        if (email.isBlank()) {
            _state.update { it.copy(message = "Введите email для восстановления") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null) }
            try {
                userRepository.sendPasswordReset(email)
                _state.update { it.copy(loading = false, message = "Письмо отправлено") }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, message = e.message ?: "Ошибка") }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}
