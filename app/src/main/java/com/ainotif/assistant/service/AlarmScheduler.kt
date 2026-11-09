package com.ainotif.assistant.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.ainotif.assistant.database.ActionEntity
import com.ainotif.assistant.database.NotifRepository
import com.ainotif.assistant.scheduler.delayMinutesFor
import com.ainotif.assistant.scheduler.rollForwardTonight
import com.ainotif.assistant.scheduler.rollForwardTomorrow
import com.ainotif.assistant.ui.log.LogCollector

class AlarmScheduler(private val context: Context, private val repository: NotifRepository) {
    private val alarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    fun schedule(notifId: Int, action: ActionEntity) {
        val now = System.currentTimeMillis()
        val delayMs = when (action.choice) {
            "snooze_1h" -> action.delayMin?.times(60 * 1000) ?: 60 * 60 * 1000
            "tonight" -> delayMinutesFor(rollForwardTonight(now), now) * 60 * 1000
            "tomorrow" -> delayMinutesFor(rollForwardTomorrow(now), now) * 60 * 1000
            "debug_1min" -> 1L * 60 * 1000
            else -> return
        }

        val triggerAt = now + delayMs

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("notifId", notifId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            LogCollector.info("Alarm scheduled: notifId=$notifId choice=${action.choice} at=$triggerAt")
        } else {
            LogCollector.error("Exact alarm permission denied: notifId=$notifId")
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerAt, 60 * 1000, pendingIntent)
        }
    }

    fun cancel(notifId: Int) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("notifId", notifId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        LogCollector.info("Alarm cancelled: notifId=$notifId")
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val result = alarmManager.canScheduleExactAlarms()
            if (!result) LogCollector.error("Exact alarm permission not granted")
            return result
        } else {
            true
        }
    }

    fun requestExactAlarmPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val intent = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            val packageName = context.packageName
            val uri = android.net.Uri.parse("package:$packageName")
            val settingsIntent = android.content.Intent(intent).setData(uri)
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(settingsIntent)
        }
    }
}
