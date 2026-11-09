package com.ainotif.assistant.sync

import android.content.Context
import android.content.SharedPreferences
import com.ainotif.assistant.database.ActionEntity
import com.ainotif.assistant.database.NotifEntity
import com.ainotif.assistant.database.NotifRepository
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class SyncRepository(
    private val context: Context,
    private val repository: NotifRepository,
    private val regSecret: String
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ainotif_sync", Context.MODE_PRIVATE)

    fun isSyncEnabled(): Boolean = prefs.getBoolean("sync_enabled", false)
    fun setSyncEnabled(enabled: Boolean) = prefs.edit().putBoolean("sync_enabled", enabled).apply()
    fun getDeviceToken(): String? = prefs.getString("device_token", null)
    fun setDeviceToken(token: String) = prefs.edit().putString("device_token", token).apply()
    fun clearToken() = prefs.edit().remove("device_token").apply()

    suspend fun register(deviceToken: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val resp = api.register(deviceToken, regSecret).execute()
                if (resp.isSuccessful && resp.body() != null) {
                    resp.body()!!.device_id
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun syncUnsynced(): Boolean {
        val token = getDeviceToken() ?: return false
        val auth = "Bearer $token"

        val unsyncedNotifs = repository.getUnsyncedNotifs()
        val unsyncedActions = repository.getUnsyncedActions()
            .filter { it.choice != "debug_1min" }

        if (unsyncedNotifs.isEmpty() && unsyncedActions.isEmpty()) return true

        val syncNotifs = unsyncedNotifs.map { n ->
            SyncNotif(n.id, n.pkg, n.category, n.postedAt, n.status, "n_${n.id}")
        }
        val syncActions = unsyncedActions.map { a ->
            SyncAction(a.notifId, "a_${a.notifId}", a.choice, a.delayMin, a.hour, a.weekday, a.openedAfter, a.suggestedChoice)
        }

        return withContext(Dispatchers.IO) {
            try {
                val resp = api.sync(auth, SyncRequest(syncNotifs, syncActions)).execute()
                if (resp.isSuccessful) {
                    for (n in unsyncedNotifs) repository.markNotifSynced(n.id)
                    for (a in unsyncedActions) repository.markActionSynced(a.notifId)
                    true
                } else false
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun deleteCloudData(token: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val resp = api.deleteMe("Bearer $token").execute()
                if (resp.isSuccessful) {
                    clearToken()
                    prefs.edit().putBoolean("sync_enabled", false).apply()
                }
                resp.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }

    private val api = RetrofitClient.api
}
