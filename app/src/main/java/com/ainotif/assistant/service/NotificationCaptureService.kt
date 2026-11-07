package com.ainotif.assistant.service

import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.ainotif.assistant.categorizer.categorize
import com.ainotif.assistant.database.AppDatabase
import com.ainotif.assistant.database.NotifRepository
import com.ainotif.assistant.ui.log.LogCollector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationCaptureService : NotificationListenerService() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var repository: NotifRepository

    override fun onCreate() {
        super.onCreate()
        LogCollector.info("Notification listener connected")
        val db = AppDatabase.getDatabase(applicationContext)
        repository = NotifRepository(db.notifDao())
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val pkg = sbn.packageName ?: ""
            val notification = sbn.notification
            val title = notification.extras?.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString()
            val text = notification.extras?.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString()
            val postedAt = sbn.postTime

            val category = categorize(pkg, title, text)

            scope.launch {
                repository.insert(pkg, title, text, category, postedAt)
                LogCollector.info("Notification saved: pkg=$pkg category=$category")
            }
        } catch (e: Exception) {
            LogCollector.error("Failed to save notification: ${e.message}")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // No-op: keep history
    }
}
