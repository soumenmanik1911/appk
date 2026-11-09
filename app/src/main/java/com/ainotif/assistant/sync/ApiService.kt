package com.ainotif.assistant.sync

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

data class RegisterRequest(val token: String)
data class RegisterResponse(val device_id: String)

data class SyncRequest(
    val notifs: List<SyncNotif>,
    val actions: List<SyncAction>
)

data class SyncNotif(
    val notif_id: Int,
    val pkg: String,
    val category: String,
    val posted_at: Long,
    val status: String,
    val local_id: String
)

data class SyncAction(
    val notif_id: Int,
    val local_id: String,
    val choice: String,
    val delay_min: Long?,
    val hour: Int?,
    val weekday: Int?,
    val opened_after: Boolean?,
    val suggested_choice: String?
)

data class SyncResponse(
    val synced: SyncSynced
)

data class SyncSynced(
    val notifs: Int,
    val actions: Int
)

interface ApiService {
    @FormUrlEncoded
    @POST("/register")
    fun register(
        @Field("token") token: String,
        @Header("X-Reg-Secret") regSecret: String
    ): Call<RegisterResponse>

    @POST("/sync")
    fun sync(@Header("Authorization") auth: String, @Body body: SyncRequest): Call<SyncResponse>

    @DELETE("/me")
    fun deleteMe(@Header("Authorization") auth: String): Call<retrofit2.Response<Void>>
}
