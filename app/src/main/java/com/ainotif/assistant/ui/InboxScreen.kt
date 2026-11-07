package com.ainotif.assistant.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ainotif.assistant.database.NotifEntity
import com.ainotif.assistant.database.NotifRepository
import dagger.hilt.android.hiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

class InboxViewModel(private val repository: NotifRepository) : ViewModel() {
    val notifications: StateFlow<List<NotifEntity>> = repository.getAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}

@Composable
fun InboxScreen(
    viewModel: InboxViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.inbox_title)) }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (notifications.isEmpty()) {
                Text(text = stringResource(id = R.string.no_notifications))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(notifications) { notif ->
                        InboxItem(notif = notif)
                    }
                }
            }
        }
    }
}

@Composable
fun InboxItem(notif: NotifEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        elevation = 2.dp
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "${notif.pkg}  •  ${formatTime(notif.postedAt)}",
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = notif.title ?: stringResource(id = R.string.no_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = notif.text ?: stringResource(id = R.string.no_text),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${notif.category.capitalize()}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    val date = java.util.Date(millis)
    return java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(date)
}
