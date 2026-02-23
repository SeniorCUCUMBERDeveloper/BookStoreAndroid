package com.example.bookstore.domain.repository

import com.example.bookstore.domain.model.FaqItem
import kotlinx.coroutines.flow.Flow

interface FaqRepository {
    fun observeFaq(): Flow<List<FaqItem>>
    suspend fun syncFromRemoteIfNeeded()
}
