package com.ainotif.assistant.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.ainotif.assistant.MyApplication
import com.ainotif.assistant.database.NotifRepository
import com.ainotif.assistant.ui.log.LogCollector

class ReminderReceiver : BroadcastReceiver() {

    private fun repository(context: Context): NotifRepository {
        return (context.applicationContext as MyApplication).repository
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val notifId = intent.getIntExtra("notifId", -1)

        if (action == "DISMISS_REMINDER") {
            if (notifId != -1) {
                LogCollector.info("Reminder dismissed: notifId=$notifId")
                val repository = repository(context)
                val actionEntity = repository.getActionByNotifId(notifId)
                if (actionEntity?.openedAfter == null) {
                    repository.markOpened(notifId, opened = false)
                    LogCollector.info("Reminder timed out: notifId=$notifId openedAfter=false")
                }
            }
            return
        }

        if (notifId == -1) return

        val app = context.applicationContext
        val repository = repository(app)
        val notif = repository.getNotifById(notifId) ?: return

        val channelId = "reminder_channel"
        val manager = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "Reminders", NotificationManager.IMPORTANCE_HIGH)
            )
        }

        val openIntent = Intent(app, com.ainotif.assistant.ui.MainActivity::class.java).apply {
            action = "OPEN_REMINDER"
            putExtra("notifId", notifId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            app, notifId, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(app, ReminderReceiver::class.java).apply {
            action = "DISMISS_REMINDER"
            putExtra("notifId", notifId)
        }
        val dismissPending = PendingIntent.getBroadcast(
            app, notifId + 10000, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(app, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(notif.title ?: "Reminder")
            .setContentText(notif.text ?: "Tap to open")
            .setContentIntent(openPending)
            .addAction(android.R.drawable.ic_input_delete, "Dismiss", dismissPending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(notifId, notification)
        LogCollector.info("Reminder posted: notifId=$notifId")

        androidx.work.WorkManager.getInstance(app)
            .enqueueUnique(
                "reminder_timeout_$notifId",
                androidx.work.ExistingWorkPolicy.REPLACE,
                com.ainotif.assistant.service.ReminderWorker.createWorkRequest(notifId)
            )
    }
}
