package com.example.bookstore.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookstore.domain.model.Book
import com.example.bookstore.domain.repository.BooksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val searched: Boolean = false
)

class SearchViewModel(
    private val booksRepository: BooksRepository
) : ViewModel() {

    val featuredNew: StateFlow<List<Book>> =
        booksRepository.observeFeaturedNew().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val featuredHits: StateFlow<List<Book>> =
        booksRepository.observeFeaturedHits().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val results: StateFlow<List<Book>> =
        booksRepository.observeSearchResults().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state


    fun setQuery(v: String) {
        _state.update { it.copy(query = v, error = null) }
    }

    fun search() {
        val q = _state.value.query.trim()
        if (q.isBlank()) {
            _state.update { it.copy(error = "Введите запрос", searched = true) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, searched = true) }
            try {
                booksRepository.search(q)
                _state.update { it.copy(loading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Ошибка сети") }
            }
        }
    }

}
