package com.example.bookstore.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.bookstore.data.local.dao.FaqDao
import com.example.bookstore.data.local.dao.ViewedDao
import com.example.bookstore.data.local.entity.FaqEntity
import com.example.bookstore.data.local.entity.ViewedBookEntity

@Database(
    entities = [ViewedBookEntity::class, FaqEntity::class],
    version = 4,
    exportSchema = false
)
abstract class BookStoreDatabase : RoomDatabase() {
    abstract fun viewedDao(): ViewedDao
    abstract fun faqDao(): FaqDao
}
