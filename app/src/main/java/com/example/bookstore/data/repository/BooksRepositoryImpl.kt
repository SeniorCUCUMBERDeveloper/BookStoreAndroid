package com.example.bookstore.data.repository

import com.example.bookstore.data.local.dao.ViewedDao
import com.example.bookstore.data.local.entity.ViewedBookEntity
import com.example.bookstore.domain.model.Book
import com.example.bookstore.domain.repository.BooksRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.Locale
import kotlin.math.absoluteValue

class BooksRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val viewedDao: ViewedDao
) : BooksRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val searchQuery = MutableStateFlow("")

    private val catalogBooks: Flow<List<Book>> = callbackFlow {
        val registration = firestore.collection("catalog")
            .addSnapshotListener { snap, error ->
                if (error != null && snap == null) {
                    return@addSnapshotListener
                }
                val books = snap?.documents?.mapNotNull(::toBookOrNull).orEmpty()
                trySend(books)
            }
        awaitClose { registration.remove() }
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 0),
        initialValue = emptyList()
    )

    override fun observeFeaturedNew(): Flow<List<Book>> = catalogBooks.map { books ->
        books.filterBySectionKeywords(sectionKeywords = listOf("new", "нов", "новинки", "featured_new"))
    }

    override fun observeFeaturedHits(): Flow<List<Book>> = catalogBooks.map { books ->
        books.filterBySectionKeywords(sectionKeywords = listOf("hit", "хит", "хиты", "featured_hit"))
    }

    override fun observeFeaturedBasic(): Flow<List<Book>> = catalogBooks.map { books ->
        books.filterBySectionKeywords(sectionKeywords = listOf("basic"))
    }

    override fun observeSearchResults(): Flow<List<Book>> = combine(catalogBooks, searchQuery) { books, query ->
        if (query.isBlank()) {
            emptyList()
        } else {
            books.filter { matchesSearchQuery(it.title, query) || matchesSearchQuery(it.author, query) }
        }
    }

    override fun observeBook(id: String): Flow<Book?> = catalogBooks.map { books ->
        books.firstOrNull { it.id == id }
    }

    override fun observeViewed(userId: String, limit: Int): Flow<List<Book>> = combine(
        viewedDao.observeViewed(userId = userId, limit = limit),
        catalogBooks
    ) { viewed, books ->
        val byId = books.associateBy { it.id }
        viewed.mapNotNull { byId[it.bookId] }
    }

    override suspend fun search(query: String) {
        searchQuery.update { query.trim().lowercase() }
    }

    override suspend fun markViewed(userId: String, bookId: String) {
        viewedDao.upsertViewed(
            ViewedBookEntity(
                userId = userId,
                bookId = bookId,
                viewedAt = System.currentTimeMillis()
            )
        )
    }

    private fun List<Book>.filterBySectionKeywords(sectionKeywords: List<String>): List<Book> =
        filter { book ->
            val tags = book.subjects.map { it.lowercase(Locale.ROOT) }
            sectionKeywords.any { keyword -> tags.any { it.contains(keyword) } }
        }

    private fun matchesSearchQuery(source: String, query: String): Boolean {
        val normalized = source.trim().lowercase()
        if (normalized.isBlank()) return false
        if (normalized.startsWith(query)) return true

        return normalized
            .split(" ", "-", "_", ".")
            .any { token -> token.isNotBlank() && token.startsWith(query) }
    }

    private fun toBookOrNull(doc: DocumentSnapshot): Book? {
        val id = doc.id
        val title = doc.getString("title")?.trim().orEmpty()
        if (title.isBlank()) return null
        val author = doc.getString("author")?.trim().orEmpty().ifBlank { "Неизвестный автор" }
        val h = id.hashCode().absoluteValue
        val priceRub = doc.readInt("priceRub") ?: (199 + (h % 801))
        val rating = doc.readDouble("rating") ?: 0.0
        val ratingCount = doc.readInt("ratingCount") ?: 0

        return Book(
            id = id,
            title = title,
            author = author,
            imageUrl = resolveImageUrlField(doc),
            priceRub = priceRub,
            rating = rating,
            ratingCount = ratingCount,
            subjects = (doc.get("subjects") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            description = resolveDescriptionField(doc)
        )
    }

    private fun resolveDescriptionField(doc: DocumentSnapshot): String? {
        val canonical = doc.getString("description")?.trim().orEmpty()
        if (canonical.isNotBlank()) return canonical

        val legacyMisspelled = doc.getString("discription")?.trim().orEmpty()
        return legacyMisspelled.ifBlank { null }
    }

    private fun DocumentSnapshot.readInt(field: String): Int? {
        val value = data?.get(field) ?: return null
        return when (value) {
            is Number -> value.toInt()
            is String -> value.trim().toIntOrNull()
            else -> null
        }
    }

    private fun DocumentSnapshot.readDouble(field: String): Double? {
        val value = data?.get(field) ?: return null
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.trim().replace(',', '.').toDoubleOrNull()
            else -> null
        }
    }

    private fun resolveImageUrlField(doc: DocumentSnapshot): String? {
        return doc.getString("imageUrl")?.trim()?.takeIf { it.isNotBlank() }
    }
}
