package com.example.bookstore.domain.model

data class UserSession(
    val uid: String,
    val email: String
)

data class UserProfile(
    val uid: String,
    val name: String,
    val email: String,
    val phone: String,
    val deliveryAddress: String
)
