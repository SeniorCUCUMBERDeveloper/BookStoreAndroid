package com.example.bookstore.data.local.entity

import androidx.room.Entity

@Entity(tableName = "viewed_books", primaryKeys = ["userId", "bookId"])
data class ViewedBookEntity(
    val userId: String,
    val bookId: String,
    val viewedAt: Long
)
