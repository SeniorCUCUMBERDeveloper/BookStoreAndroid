package com.example.bookstore.domain.model

data class OrderItem(
    val bookId: String,
    val title: String,
    val priceRub: Int,
    val quantity: Int
)

data class Order(
    val id: String,
    val createdAt: Long,
    val status: String,
    val paymentStatus: String,
    val totalRub: Int,
    val customerName: String,
    val customerPhone: String,
    val customerEmail: String,
    val deliveryAddress: String,
    val items: List<OrderItem>
)
