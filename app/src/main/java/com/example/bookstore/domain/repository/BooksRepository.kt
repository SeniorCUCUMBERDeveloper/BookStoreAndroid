package com.example.bookstore.domain.repository

import com.example.bookstore.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BooksRepository {
    fun observeFeaturedNew(): Flow<List<Book>>
    fun observeFeaturedHits(): Flow<List<Book>>
    fun observeFeaturedBasic(): Flow<List<Book>>
    fun observeSearchResults(): Flow<List<Book>>
    fun observeBook(id: String): Flow<Book?>
    fun observeViewed(userId: String, limit: Int): Flow<List<Book>>
    suspend fun search(query: String)
    suspend fun markViewed(userId: String, bookId: String)
}
