package com.ainotif.assistant.service

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ainotif.assistant.MyApplication
import com.ainotif.assistant.database.NotifRepository
import com.ainotif.assistant.ui.log.LogCollector

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    private fun repository(): NotifRepository {
        return (applicationContext as MyApplication).repository
    }

    override fun doWork(): androidx.work.Result {
        val notifId = inputData.getInt("notifId", -1)
        if (notifId == -1) return androidx.work.Result.failure()

        repository().markOpened(notifId, opened = false)
        LogCollector.info("Reminder worker timed out: notifId=$notifId openedAfter=false")
        return androidx.work.Result.success()
    }

    companion object {
        fun createWorkRequest(notifId: Int): androidx.work.WorkRequest {
            return androidx.work.OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInputData(Data.Builder().putInt("notifId", notifId).build())
                .setInitialDelay(30, androidx.work.TimeUnit.MINUTES)
                .build()
        }
    }
}
