package com.example.bookstore.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookstore.domain.model.Book
import com.example.bookstore.domain.repository.BooksRepository
import com.example.bookstore.domain.repository.CartRepository
import com.example.bookstore.domain.repository.OrdersRepository
import com.example.bookstore.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val loading: Boolean = false,
    val message: String? = null,
    val orderId: String? = null
)

data class CheckoutItem(
    val book: Book,
    val quantity: Int
)

class CheckoutViewModel(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
    private val ordersRepository: OrdersRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    val items: StateFlow<List<CheckoutItem>> = cartRepository.lines
        .flatMapLatest { lines ->
            val ids = lines.map { it.bookId }.distinct()
            if (ids.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(ids.map(booksRepository::observeBook)) { books ->
                    val byId = ids.zip(books.toList()).toMap()
                    lines.mapNotNull { line ->
                        byId[line.bookId]?.let { CheckoutItem(it, line.quantity) }
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _state = MutableStateFlow(CheckoutUiState())
    val state: StateFlow<CheckoutUiState> = _state

    init {
        loadProfileIntoState()
    }

    fun refreshProfile() {
        loadProfileIntoState()
    }

    private fun loadProfileIntoState() {

        viewModelScope.launch {
            val profile = runCatching { userRepository.getProfile() }.getOrNull()
            _state.update {
                it.copy(
                    name = profile?.name ?: "",
                    email = profile?.email ?: "",
                    phone = profile?.phone ?: "",
                    address = profile?.deliveryAddress ?: ""
                )
            }
        }
    }

    fun setName(v: String) { _state.update { it.copy(name = v, message = null) } }
    fun setPhone(v: String) { _state.update { it.copy(phone = v, message = null) } }
    fun setEmail(v: String) { _state.update { it.copy(email = v, message = null) } }
    fun setAddress(v: String) { _state.update { it.copy(address = v, message = null) } }

    fun increase(bookId: String, currentQty: Int) {
        cartRepository.setQuantity(bookId, currentQty + 1)
    }

    fun decrease(bookId: String, currentQty: Int) {
        cartRepository.setQuantity(bookId, currentQty - 1)
    }

    fun submit() {
        val s = _state.value
        if (s.loading) return
        if (s.name.trim().isBlank() || s.phone.trim().isBlank() || s.email.trim().isBlank() || s.address.trim().isBlank()) {
            _state.update { it.copy(message = "Заполните все поля") }
            return
        }
        val currentItems = items.value
        if (currentItems.isEmpty()) {
            _state.update { it.copy(message = "Корзина пуста") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null, orderId = null) }
            try {
                val orderId = ordersRepository.createOrder(
                    customerName = s.name.trim(),
                    customerPhone = s.phone.trim(),
                    customerEmail = s.email.trim(),
                    deliveryAddress = s.address.trim(),
                    items = currentItems.map { it.book.id to it.quantity }
                )
                cartRepository.clear()
                _state.update { it.copy(loading = false, orderId = orderId, message = "Заказ создан. Онлайн-оплата временно недоступна.") }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, message = e.message ?: "Ошибка") }
            }
        }
    }

    fun clearMessage() { _state.update { it.copy(message = null) } }

    fun clearOrderResult() {
        _state.update { it.copy(orderId = null) }
    }
}
