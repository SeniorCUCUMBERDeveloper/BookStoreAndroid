package com.example.bookstore.domain.model

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val imageUrl: String?,
    val priceRub: Int,
    val rating: Double,
    val ratingCount: Int,
    val subjects: List<String>,
    val description: String?
)
