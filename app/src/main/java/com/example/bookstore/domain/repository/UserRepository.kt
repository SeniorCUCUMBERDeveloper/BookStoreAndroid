package com.example.bookstore.domain.repository

import com.example.bookstore.domain.model.UserProfile
import com.example.bookstore.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val session: Flow<UserSession?>
    fun currentUid(): String?
    suspend fun login(email: String, password: String)
    suspend fun register(name: String, email: String, password: String)
    suspend fun sendPasswordReset(email: String)
    suspend fun logout()
    suspend fun getProfile(): UserProfile?
    suspend fun updateProfile(name: String, phone: String, deliveryAddress: String)
}
