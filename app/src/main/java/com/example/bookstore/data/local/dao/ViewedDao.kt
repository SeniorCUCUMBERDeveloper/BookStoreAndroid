package com.example.bookstore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bookstore.data.local.entity.ViewedBookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ViewedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertViewed(item: ViewedBookEntity)

    @Query(
        """
        SELECT * FROM viewed_books
        WHERE userId = :userId
        ORDER BY viewedAt DESC
        LIMIT :limit
        """
    )
    fun observeViewed(userId: String, limit: Int): Flow<List<ViewedBookEntity>>
}
