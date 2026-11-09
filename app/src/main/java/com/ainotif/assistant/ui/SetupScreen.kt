package com.ainotif.assistant.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.ainotif.assistant.database.NotifEntity
import com.ainotif.assistant.database.NotifRepository
import com.ainotif.assistant.service.AlarmScheduler
import com.ainotif.assistant.stats.DefaultRules
import com.ainotif.assistant.stats.StatsRepository
import com.ainotif.assistant.sync.SyncViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import androidx.compose.foundation.LocalContext
import androidx.compose.foundation.Dialog
import androidx.compose.foundation.WindowInsetsSides
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDialogManager

class SetupViewModel(private val repository: NotifRepository) : ViewModel() {
    val isGranted: StateFlow<Boolean> = androidx.compose.runtime.flow {
        emit(false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
}

fun checkNotificationAccess(context: android.content.Context): Boolean {
    val enabled = android.provider.Settings.Secure.getString(
        context.contentResolver,
        android.provider.Settings.Secure.ENABLED_NOTIFICATION_LISTENERS
    )
    return enabled?.contains(context.packageName) == true
}

@Composable
fun SetupScreen(
    onGranted: () -> Unit,
    onInsights: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel(),
    syncViewModel: SyncViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val granted = checkNotificationAccess(context)
    var syncOn by remember { mutableStateOf(syncViewModel.syncEnabled) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(syncViewModel.syncEnabled) {
        syncOn = syncViewModel.syncEnabled
    }

    if (showDeleteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = stringResource(id = R.string.delete_confirm_title)) },
            text = { Text(text = stringResource(id = R.string.delete_confirm_text)) },
            confirmButton = {
                Button(onClick = {
                    syncViewModel.deleteCloudData()
                    showDeleteDialog = false
                }) {
                    Text(text = stringResource(id = R.string.delete_confirm_delete))
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text(text = stringResource(id = R.string.delete_confirm_cancel))
                }
            }
        )
    }

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
                Button(onClick = onInsights) {
                    Text(text = stringResource(id = R.string.insights_button))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = syncOn,
                        onCheckedChange = { syncViewModel.toggleSync(it); syncOn = it }
                    )
                    Text(text = stringResource(id = R.string.sync_to_cloud))
                }
                if (syncViewModel.syncEnabled) {
                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(text = stringResource(id = R.string.delete_cloud_data))
                    }
                }
            }
        }
    }
}
