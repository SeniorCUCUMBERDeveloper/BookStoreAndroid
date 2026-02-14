package com.example.bookstore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.bookstore.data.local.entity.FaqEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FaqDao {
    @Query("SELECT * FROM faq_items ORDER BY position ASC, id ASC")
    fun observeAll(): Flow<List<FaqEntity>>

    @Query("SELECT COUNT(*) FROM faq_items")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<FaqEntity>)

    @Query("DELETE FROM faq_items")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(items: List<FaqEntity>) {
        deleteAll()
        if (items.isNotEmpty()) {
            upsertAll(items)
        }
    }
}
