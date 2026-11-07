package com.ainotif.assistant.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotifDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notif: NotifEntity)

    @Query("SELECT * FROM notifs ORDER BY postedAt DESC")
    fun getAll(): Flow<List<NotifEntity>>
}
