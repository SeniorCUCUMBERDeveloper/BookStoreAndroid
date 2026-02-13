package com.example.bookstore.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.bookstore.data.local.dao.ViewedDao
import com.example.bookstore.data.local.entity.ViewedBookEntity

@Database(
    entities = [ViewedBookEntity::class],
    version = 3,
    exportSchema = false
)
abstract class BookStoreDatabase : RoomDatabase() {
    abstract fun viewedDao(): ViewedDao
}
