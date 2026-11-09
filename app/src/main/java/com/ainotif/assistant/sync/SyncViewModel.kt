package com.ainotif.assistant.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ainotif.assistant.database.NotifRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val context: Context,
    private val repository: NotifRepository,
) : ViewModel() {
    private val prefs: SharedPreferences = context.getSharedPreferences("ainotif_sync", Context.MODE_PRIVATE)

    var syncEnabled: Boolean = prefs.getBoolean("sync_enabled", false)
        private set

    var deviceToken: String? = prefs.getString("device_token", null)
        private set

    fun toggleSync(enabled: Boolean) {
        syncEnabled = enabled
        prefs.edit().putBoolean("sync_enabled", enabled).apply()
    }

    fun ensureDeviceToken(): String {
        val existing = deviceToken
        if (existing != null) return existing
        val token = "device_" + java.util.UUID.randomUUID().toString()
        deviceToken = token
        prefs.edit().putString("device_token", token).apply()
        return token
    }

    fun deleteCloudData() {
        val token = deviceToken ?: return
        viewModelScope.launch {
            val success = SyncRepository(context, repository, BuildConfig.REG_SECRET).deleteCloudData(token)
            if (success) {
                syncEnabled = false
                prefs.edit().putBoolean("sync_enabled", false).apply()
                deviceToken = null
                prefs.edit().remove("device_token").apply()
            }
        }
    }
}
