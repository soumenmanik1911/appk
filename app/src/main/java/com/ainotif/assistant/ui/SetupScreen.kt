package com.ainotif.assistant.ui

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalContext
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ainotif.assistant.database.NotifRepository
import dagger.hilt.android.hiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

class SetupViewModel(private val repository: NotifRepository) : ViewModel() {
    val isGranted: StateFlow<Boolean> = flow {
        emit(false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
}

fun checkNotificationAccess(context: Context): Boolean {
    val enabled = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_NOTIFICATION_LISTENERS
    )
    return enabled?.contains(context.packageName) == true
}

@Composable
fun SetupScreen(
    onGranted: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val granted = checkNotificationAccess(context)

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.setup_title)) }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.notification_access),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = if (granted) stringResource(id = R.string.access_granted) else stringResource(id = R.string.access_not_granted),
                style = MaterialTheme.typography.bodyLarge
            )
            Button(
                onClick = {
                    val intent = android.content.Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    context.startActivity(intent)
                }
            ) {
                Text(text = stringResource(id = R.string.grant_access_button))
            }
            if (granted) {
                Button(onClick = onGranted) {
                    Text(text = stringResource(id = R.string.go_to_inbox))
                }
            }
        }
    }
}
