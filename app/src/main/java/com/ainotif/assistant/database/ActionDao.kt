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
interface ActionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(action: ActionEntity)

    @Query("UPDATE actions SET opened_after = :openedAfter WHERE notifId = :notifId")
    suspend fun updateOpenedAfter(notifId: Int, openedAfter: Boolean)

    @Query("SELECT * FROM actions WHERE notifId = :notifId")
    suspend fun getByNotifId(notifId: Int): ActionEntity?

    @Query("SELECT * FROM actions WHERE synced = 0")
    suspend fun getUnsynced(): List<ActionEntity>

    @Query("SELECT * FROM actions")
    suspend fun getAllActions(): List<ActionEntity>

    @Query(
        "SELECT * FROM actions WHERE choice != 'debug_1min'"
    )
    suspend fun getAllActionsNonDebug(): List<ActionEntity>

    @Query(
        "SELECT n.category AS category, a.delayMin AS delayMin, a.choice AS choice " +
        "FROM actions a JOIN notifs n ON a.notifId = n.id " +
        "WHERE a.opened_after = 1"
    )
    fun getOpenedActionsWithCategory(): List<ActionWithCategory>

    @Query("UPDATE actions SET synced = 1 WHERE notifId = :notifId")
    suspend fun markSynced(notifId: Int)
}
