package com.ainotif.assistant.di

import android.content.Context
import com.ainotif.assistant.database.AppDatabase
import com.ainotif.assistant.database.NotifDao
import com.ainotif.assistant.database.NotifRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @SingletonComponent
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @SingletonComponent
    fun provideDao(database: AppDatabase): NotifDao {
        return database.notifDao()
    }

    @Provides
    @SingletonComponent
    fun provideRepository(dao: NotifDao): NotifRepository {
        return NotifRepository(dao)
    }
}
