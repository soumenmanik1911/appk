package com.ainotif.assistant.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NotifEntity::class, ActionEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notifDao(): NotifDao
    abstract fun actionDao(): ActionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notif_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
