package com.example.bookstore.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookstore.domain.model.Book
import com.example.bookstore.domain.model.Order
import com.example.bookstore.domain.model.UserProfile
import com.example.bookstore.domain.model.UserSession
import com.example.bookstore.domain.repository.BooksRepository
import com.example.bookstore.domain.repository.OrdersRepository
import com.example.bookstore.domain.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val deliveryAddress: String = "",
    val saving: Boolean = false,
    val cancellingOrderId: String? = null,
    val message: String? = null
)

private data class ProfileDraft(
    val name: String,
    val phone: String,
    val deliveryAddress: String
)

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val booksRepository: BooksRepository,
    private val ordersRepository: OrdersRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state

    private var lastSavedDraft = ProfileDraft(name = "", phone = "", deliveryAddress = "")
    private var isProfileLoaded = false
    private var autosaveJob: Job? = null

    val viewed: StateFlow<List<Book>> =
        userRepository.session
            .map { it?.uid }
            .flatMapLatest { uid ->
                if (uid == null) flowOf(emptyList()) else booksRepository.observeViewed(uid, limit = 12)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val orders: StateFlow<List<Order>> =
        ordersRepository.observeMyOrders(limit = 30)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            userRepository.session
                .distinctUntilChanged { old, new -> old?.uid == new?.uid }
                .collect { session ->
                    applySessionProfile(session)
                }
        }
    }

    fun refreshProfile() {
        viewModelScope.launch {
            applySessionProfile(userRepository.session.first())
        }
    }

    private suspend fun applySessionProfile(session: UserSession?) {
        autosaveJob?.cancel()
        isProfileLoaded = false

        if (session == null) {
            lastSavedDraft = ProfileDraft(name = "", phone = "", deliveryAddress = "")
            _state.value = ProfileUiState()
            isProfileLoaded = true
            return
        }

        _state.value = ProfileUiState(email = session.email)

        val profile = loadProfileWithRetry()
        if (profile != null) {
            val loadedDraft = ProfileDraft(
                name = profile.name,
                phone = profile.phone,
                deliveryAddress = profile.deliveryAddress
            )
            lastSavedDraft = loadedDraft
            _state.value = ProfileUiState(
                name = loadedDraft.name,
                email = profile.email,
                phone = loadedDraft.phone,
                deliveryAddress = loadedDraft.deliveryAddress
            )
        } else {
            lastSavedDraft = ProfileDraft(name = "", phone = "", deliveryAddress = "")
            _state.value = ProfileUiState(email = session.email)
        }
        isProfileLoaded = true
    }

    private suspend fun loadProfileWithRetry(): UserProfile? {
        repeat(6) { attempt ->
            val profile = userRepository.getProfile()
            if (profile != null) return profile
            if (attempt < 5) delay(250)
        }
        return null
    }

    fun setName(v: String) = updateDraft { it.copy(name = v, message = null) }

    fun setPhone(v: String) = updateDraft { it.copy(phone = v, message = null) }

    fun setDeliveryAddress(v: String) = updateDraft { it.copy(deliveryAddress = v, message = null) }

    private fun updateDraft(transform: (ProfileUiState) -> ProfileUiState) {
        _state.update(transform)
        scheduleAutosave()
    }

    private fun scheduleAutosave() {
        if (!isProfileLoaded) return
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            kotlinx.coroutines.delay(700)
            saveProfileIfChanged(showSuccessMessage = false)
        }
    }

    private suspend fun saveProfileIfChanged(showSuccessMessage: Boolean) {
        val draft = ProfileDraft(
            name = _state.value.name.trim(),
            phone = _state.value.phone.trim(),
            deliveryAddress = _state.value.deliveryAddress.trim()
        )
        if (draft == lastSavedDraft) return

        val isNameChanged = draft.name != lastSavedDraft.name
        if (draft.name.isBlank()) {
            if (isNameChanged) {
                _state.update { it.copy(message = "Имя не может быть пустым") }
            }
            return
        }

        _state.update { it.copy(saving = true, message = null) }
        try {
            userRepository.updateProfile(
                name = draft.name,
                phone = draft.phone,
                deliveryAddress = draft.deliveryAddress
            )
            lastSavedDraft = draft
            _state.update {
                it.copy(
                    name = draft.name,
                    phone = draft.phone,
                    deliveryAddress = draft.deliveryAddress,
                    saving = false,
                    message = if (showSuccessMessage) "Сохранено" else null
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(saving = false, message = e.message ?: "Ошибка") }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            _state.update { it.copy(cancellingOrderId = orderId, message = null) }
            try {
                ordersRepository.cancelOrder(orderId)
                _state.update { it.copy(cancellingOrderId = null, message = "Заказ отменён") }
            } catch (e: Exception) {
                _state.update { it.copy(cancellingOrderId = null, message = e.message ?: "Ошибка отмены заказа") }
            }
        }
    }

    fun logout() = viewModelScope.launch { userRepository.logout() }

    fun clearMessage() = _state.update { it.copy(message = null) }

    override fun onCleared() {
        autosaveJob?.cancel()
        super.onCleared()
    }
}
