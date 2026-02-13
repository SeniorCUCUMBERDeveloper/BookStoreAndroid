package com.example.bookstore.domain.repository

import kotlinx.coroutines.flow.StateFlow

data class CartLine(
    val bookId: String,
    val quantity: Int
)

interface CartRepository {
    val lines: StateFlow<List<CartLine>>
    fun add(bookId: String)
    fun setQuantity(bookId: String, quantity: Int)
    fun clear()
}
