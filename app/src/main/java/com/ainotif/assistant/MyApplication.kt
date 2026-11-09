package com.ainotif.assistant

import android.app.Application
import androidx.work.Configuration
import androidx.work.impl.utils.futures.AbstractFuture
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    lateinit var repository: com.ainotif.assistant.database.NotifRepository

    companion object {
        @JvmStatic
        lateinit var instance: MyApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
    }
}
