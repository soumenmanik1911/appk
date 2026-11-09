package com.ainotif.assistant.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

data class ActionWithCategory(
    val category: String,
    val delayMin: Long?,
    val choice: String
)

@Dao
interface NotifDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notif: NotifEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAndGetId(notif: NotifEntity): Int

    @Query("SELECT * FROM notifs ORDER BY postedAt DESC")
    fun getAll(): Flow<List<NotifEntity>>

    @Query("SELECT * FROM notifs WHERE synced = 0 ORDER BY postedAt DESC")
    fun getUnsynced(): List<NotifEntity>

    @Query("SELECT * FROM notifs WHERE id = :notifId")
    suspend fun getById(notifId: Int): NotifEntity?

    @Query("UPDATE notifs SET status = :status WHERE id = :notifId")
    suspend fun updateStatus(notifId: Int, status: String)

    @Query("UPDATE notifs SET synced = 1 WHERE id = :notifId")
    suspend fun markSynced(notifId: Int)
}

import kotlinx.coroutines.flow.Flow
