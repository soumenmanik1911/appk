package com.ainotif.assistant.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifs")
data class NotifEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val pkg: String,
    val title: String?,
    val text: String?,
    val category: String,
    val postedAt: Long,
    val status: String = "pending"
)
