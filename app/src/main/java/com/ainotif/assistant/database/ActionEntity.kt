package com.ainotif.assistant.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "actions",
    foreignKeys = [
        ForeignKey(
            entity = NotifEntity::class,
            parentColumns = ["id"],
            childColumns = ["notifId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ActionEntity(
    @PrimaryKey
    val notifId: Int,
    val choice: String,
    val delayMin: Long?,
    val hour: Int?,
    val weekday: Int?,
    @ColumnInfo(name = "opened_after")
    val openedAfter: Boolean? = null,
    @ColumnInfo(name = "suggested_choice")
    val suggestedChoice: String? = null,
    @ColumnInfo(name = "synced")
    val synced: Boolean = false
)
