package com.example.bookstore.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookstore.domain.model.Book
import com.example.bookstore.domain.model.BookReview
import com.example.bookstore.domain.repository.BooksRepository
import com.example.bookstore.domain.repository.CartRepository
import com.example.bookstore.domain.repository.ReviewsRepository
import com.example.bookstore.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookDetailViewModel(
    val bookId: String,
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
    private val reviewsRepository: ReviewsRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    val book: StateFlow<Book?> =
        booksRepository.observeBook(bookId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val description: StateFlow<String?> =
        book.map { it?.description }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val reviews: StateFlow<List<BookReview>> =
        reviewsRepository.observeReviews(bookId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val canReviewMutable = MutableStateFlow(true)
    val canReview: StateFlow<Boolean> = canReviewMutable.asStateFlow()


    init {
        viewModelScope.launch {
            runCatching {
                val uid = userRepository.currentUid()
                if (uid != null) {
                    booksRepository.markViewed(uid, bookId)
                    canReviewMutable.value = !reviewsRepository.hasReview(bookId, uid)
                }
            }
        }
    }

    fun addReview(text: String, rating: Int) {
        val content = text.trim()
        if (content.isBlank() || rating !in 1..5 || !canReviewMutable.value) return
        viewModelScope.launch {
            val uid = userRepository.currentUid() ?: return@launch
            val user = userRepository.getProfile()
            try {
                reviewsRepository.addReview(
                    bookId = bookId,
                    uid = uid,
                    userName = user?.name?.ifBlank { "Покупатель" } ?: "Покупатель",
                    text = content,
                    rating = rating
                )

                canReviewMutable.value = false
            } catch (_: Exception) {
            }
        }
    }

    fun addToCart() {
        cartRepository.add(bookId)
    }
}
