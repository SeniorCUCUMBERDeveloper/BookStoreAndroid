package com.example.bookstore.data.repository

import com.example.bookstore.domain.repository.CartLine
import com.example.bookstore.domain.repository.CartRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CartRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : CartRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _lines = MutableStateFlow<List<CartLine>>(emptyList())
    override val lines: StateFlow<List<CartLine>> = _lines.asStateFlow()

    private var cartRegistration: ListenerRegistration? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            subscribeToCart(uid)
        }
        subscribeToCart(auth.currentUser?.uid)
    }

    override fun add(bookId: String) {
        val uid = auth.currentUser?.uid ?: return
        safeLaunch {
            val docRef = userCartCollection(uid).document(bookId)
            firestore.runTransaction { tx ->
                val snap = tx.get(docRef)
                val currentQty = (snap.getLong("quantity") ?: 0L).toInt()
                val nextQty = (currentQty + 1).coerceAtLeast(1)
                tx.set(
                    docRef,
                    mapOf(
                        "bookId" to bookId,
                        "quantity" to nextQty,
                        "updatedAt" to Timestamp.now()
                    )
                )
            }.await()
        }
    }

    override fun setQuantity(bookId: String, quantity: Int) {
        val uid = auth.currentUser?.uid ?: return
        safeLaunch {
            val docRef = userCartCollection(uid).document(bookId)
            if (quantity <= 0) {
                docRef.delete().await()
            } else {
                docRef.set(
                    mapOf(
                        "bookId" to bookId,
                        "quantity" to quantity,
                        "updatedAt" to Timestamp.now()
                    )
                ).await()
            }
        }
    }

    override fun clear() {
        val uid = auth.currentUser?.uid ?: return
        safeLaunch {
            val collection = userCartCollection(uid)
            val snap = collection.get().await()
            val batch = firestore.batch()
            snap.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
    }
    private fun subscribeToCart(uid: String?) {
        cartRegistration?.remove()
        cartRegistration = null

        if (uid == null) {
            _lines.value = emptyList()
            return
        }

        cartRegistration = userCartCollection(uid)
            .addSnapshotListener { snap, _ ->
                if (snap == null) {
                    _lines.value = emptyList()
                    return@addSnapshotListener
                }
                _lines.value = snap.documents.mapNotNull { doc ->
                    val bookId = doc.getString("bookId")?.takeIf { it.isNotBlank() } ?: doc.id
                    val quantity = (doc.getLong("quantity") ?: 0L).toInt()
                    if (quantity <= 0) null else CartLine(bookId = bookId, quantity = quantity)
                }
            }
    }

    private fun userCartCollection(uid: String) =
        firestore.collection("users").document(uid).collection("cart")

    private fun safeLaunch(block: suspend () -> Unit) {
        scope.launch {
            runCatching { block() }
        }
    }
}
