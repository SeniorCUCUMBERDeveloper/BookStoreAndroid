package com.example.bookstore.data.firebase

import com.example.bookstore.domain.model.Order
import com.example.bookstore.domain.model.OrderItem
import com.example.bookstore.domain.repository.BooksRepository
import com.example.bookstore.domain.repository.OrdersRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import java.util.UUID

class OrdersRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val booksRepository: BooksRepository
) : OrdersRepository {

    override fun observeMyOrders(limit: Int): Flow<List<Order>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val reg = firestore.collection("users")
            .document(uid)
            .collection("orders")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snap, _ ->
                if (snap == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val orders = snap.documents.mapNotNull { doc ->
                    val createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L
                    val status = doc.getString("status") ?: ""
                    val paymentStatus = doc.getString("paymentStatus") ?: ""
                    val totalRub = doc.getLong("totalRub")?.toInt() ?: 0
                    val customerName = doc.getString("customerName") ?: ""
                    val customerPhone = doc.getString("customerPhone") ?: ""
                    val customerEmail = doc.getString("customerEmail") ?: ""
                    val deliveryAddress = doc.getString("deliveryAddress") ?: ""
                    val itemsAny = doc.get("items") as? List<*>
                    val items = itemsAny?.mapNotNull { item ->
                        val m = item as? Map<*, *> ?: return@mapNotNull null
                        val bookId = m["bookId"] as? String ?: return@mapNotNull null
                        val title = m["title"] as? String ?: ""
                        val priceRub = (m["priceRub"] as? Number)?.toInt() ?: 0
                        val quantity = (m["quantity"] as? Number)?.toInt() ?: 1
                        OrderItem(bookId, title, priceRub, quantity)
                    } ?: emptyList()
                    Order(
                        id = doc.id,
                        createdAt = createdAt,
                        status = status,
                        paymentStatus = paymentStatus,
                        totalRub = totalRub,
                        customerName = customerName,
                        customerPhone = customerPhone,
                        customerEmail = customerEmail,
                        deliveryAddress = deliveryAddress,
                        items = items
                    )
                }
                trySend(orders)
            }

        awaitClose { reg.remove() }
    }

    override suspend fun createOrder(
        customerName: String,
        customerPhone: String,
        customerEmail: String,
        deliveryAddress: String,
        items: List<Pair<String, Int>>
    ): String {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not authorized")
        val orderId = UUID.randomUUID().toString()
        val orderItems = items.mapNotNull { (bookId, qty) ->
            val book = booksRepository.observeBook(bookId).firstOrNull() ?: return@mapNotNull null
            mapOf(
                "bookId" to book.id,
                "title" to book.title,
                "priceRub" to book.priceRub,
                "quantity" to qty
            )
        }
        if (orderItems.isEmpty()) {
            throw IllegalStateException("Книга не найдена")
        }
        val total = orderItems.sumOf { ((it["priceRub"] as? Number)?.toInt() ?: 0) * ((it["quantity"] as? Number)?.toInt() ?: 1) }
        val data = hashMapOf(
            "createdAt" to Timestamp.now(),
            "status" to "Создан",
            "paymentStatus" to "Онлайн-оплата недоступна",
            "totalRub" to total,
            "customerName" to customerName,
            "customerPhone" to customerPhone,
            "customerEmail" to customerEmail,
            "deliveryAddress" to deliveryAddress,
            "items" to orderItems
        )
        firestore.collection("users").document(uid).collection("orders").document(orderId).set(data).await()
        return orderId
    }

    override suspend fun cancelOrder(orderId: String) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not authorized")
        val ref = firestore.collection("users").document(uid).collection("orders").document(orderId)
        val snap = ref.get().await()
        if (!snap.exists()) throw IllegalStateException("Заказ не найден")
        val status = snap.getString("status").orEmpty()
        if (status.equals("Отменён", ignoreCase = true)) return
        ref.update(
            mapOf(
                "status" to "Отменён",
                "cancelledAt" to Timestamp.now()
            )
        ).await()
    }
}
