package com.example.bookstore.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookstore.domain.model.FaqItem
import com.example.bookstore.domain.repository.FaqRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FaqUiState(
    val loading: Boolean = false,
    val error: String? = null
)

class FaqViewModel(
    private val faqRepository: FaqRepository
) : ViewModel() {

    val items: StateFlow<List<FaqItem>> =
        faqRepository.observeFaq().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _state = MutableStateFlow(FaqUiState())
    val state: StateFlow<FaqUiState> = _state

    init {
        syncFaqOnce()
    }

    fun syncFaqOnce() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                faqRepository.syncFromRemoteIfNeeded()
                _state.update { it.copy(loading = false) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        loading = false,
                        error = if (items.value.isEmpty()) e.message ?: "Не удалось загрузить FAQ" else null
                    )
                }
            }
        }
    }
}
