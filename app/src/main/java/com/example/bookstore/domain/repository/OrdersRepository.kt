package com.example.bookstore.domain.repository

import com.example.bookstore.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrdersRepository {
    fun observeMyOrders(limit: Int): Flow<List<Order>>
    suspend fun createOrder(
        customerName: String,
        customerPhone: String,
        customerEmail: String,
        deliveryAddress: String,
        items: List<Pair<String, Int>>
    ): String

    suspend fun cancelOrder(orderId: String)
}
