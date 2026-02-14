package com.example.bookstore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "faq_items")
data class FaqEntity(
    @PrimaryKey val id: String,
    val question: String,
    val answer: String,
    val position: Int
)
