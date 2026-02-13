package com.example.bookstore.data.firebase

import com.example.bookstore.domain.model.BookReview
import com.example.bookstore.domain.repository.ReviewsRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReviewsRepositoryImpl(
    private val firestore: FirebaseFirestore
) : ReviewsRepository {

    override fun observeReviews(bookId: String): Flow<List<BookReview>> = callbackFlow {
        val registration = firestore.collection("catalog").document(bookId)
            .collection("reviews")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { doc ->
                    val rating = doc.getLong("rating")?.toInt() ?: return@mapNotNull null
                    BookReview(
                        userName = doc.getString("userName") ?: "Покупатель",
                        reviewText = doc.getString("text") ?: "",
                        rating = rating
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun hasReview(bookId: String, uid: String): Boolean {
        return firestore.collection("catalog")
            .document(bookId)
            .collection("reviews")
            .document(uid)
            .get()
            .await()
            .exists()
    }

    override suspend fun addReview(
        bookId: String,
        uid: String,
        userName: String,
        text: String,
        rating: Int
    ) {
        firestore.collection("catalog")
            .document(bookId)
            .collection("reviews")
            .document(uid)
            .set(
                mapOf(
                    "userName" to userName,
                    "text" to text,
                    "rating" to rating,
                    "createdAt" to System.currentTimeMillis()
                )
            )
            .await()
    }
}
