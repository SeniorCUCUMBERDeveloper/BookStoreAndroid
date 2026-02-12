package com.example.bookstore.domain.repository

import com.example.bookstore.domain.model.BookReview
import kotlinx.coroutines.flow.Flow

interface ReviewsRepository {
    fun observeReviews(bookId: String): Flow<List<BookReview>>
    suspend fun hasReview(bookId: String, uid: String): Boolean
    suspend fun addReview(
        bookId: String,
        uid: String,
        userName: String,
        text: String,
        rating: Int
    )
}
