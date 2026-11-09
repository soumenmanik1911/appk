package com.ainotif.assistant.sync

import android.content.Context
import android.net.ConnectivityManager
import androidx.hilt.work.HiltWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.ainotif.assistant.MyApplication
import com.ainotif.assistant.database.NotifRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.runBlocking

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: NotifRepository,
) : ListenableWorker(context, params) {

    override fun startWork(): ListenableWorker.Result {
        val app = context.applicationContext as MyApplication
        val prefs = app.getSharedPreferences("ainotif_sync", Context.MODE_PRIVATE)
        val syncEnabled = prefs.getBoolean("sync_enabled", false)
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connMgr.activeNetwork
        val networkAvailable = network != null && connMgr.getNetworkCapabilities(network)?.hasCapability(ConnectivityManager.NET_CAPABILITY_INTERNET) == true

        if (!syncEnabled || !networkAvailable) {
            return Result.success()
        }

        val syncRepo = SyncRepository(context, repository, BuildConfig.REG_SECRET)
        return runBlocking {
            if (syncRepo.syncUnsynced()) Result.success() else Result.failure()
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted context: Context, @Assisted params: WorkerParameters): SyncWorker
    }
}
