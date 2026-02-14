package com.example.bookstore.data.repository

import com.example.bookstore.data.local.dao.FaqDao
import com.example.bookstore.data.local.entity.FaqEntity
import com.example.bookstore.domain.model.FaqItem
import com.example.bookstore.domain.repository.FaqRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

class FaqRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val faqDao: FaqDao
) : FaqRepository {

    private val syncMutex = Mutex()

    override fun observeFaq(): Flow<List<FaqItem>> = faqDao.observeAll().map { items ->
        items.map { entity ->
            FaqItem(
                id = entity.id,
                question = entity.question,
                answer = entity.answer,
                position = entity.position
            )
        }
    }

    override suspend fun syncFromRemoteIfNeeded() {
        syncMutex.withLock {
            val snapshot = firestore.collection("faq").get().await()
            val items = snapshot.documents
                .mapNotNull(::toFaqEntityOrNull)
                .sortedBy { it.position }

            if (items.isNotEmpty()) {
                faqDao.replaceAll(items)
            }
        }
    }

    private fun toFaqEntityOrNull(doc: DocumentSnapshot): FaqEntity? {
        val question = doc.getString("question")?.trim().orEmpty()
        val answer = doc.getString("answer")?.trim().orEmpty()

        if (question.isBlank() || answer.isBlank()) return null

        val position = doc.readInt("position")
            ?: doc.readInt("order")
            ?: Int.MAX_VALUE

        return FaqEntity(
            id = doc.id,
            question = question,
            answer = answer,
            position = position
        )
    }

    private fun DocumentSnapshot.readInt(field: String): Int? {
        val value = data?.get(field) ?: return null
        return when (value) {
            is Number -> value.toInt()
            is String -> value.trim().toIntOrNull()
            else -> null
        }
    }
}
