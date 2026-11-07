package com.ainotif.assistant.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NotifEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notifDao(): NotifDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
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
