package com.ainotif.assistant.service

import android.content.Context
import androidx.core.content.ContextCompat

fun isPostNotificationsGranted(context: Context): Boolean {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
        return true
    }
    return ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.POST_NOTIFICATIONS
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
}
