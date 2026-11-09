package com.ainotif.assistant.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.ainotif.assistant.service.AlarmScheduler
import com.ainotif.assistant.stats.DefaultRules
import com.ainotif.assistant.stats.StatsRepository
import dagger.hilt.android.hiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.LocalContext

class InboxViewModel(private val repository: NotifRepository) : ViewModel() {
    val notifications: StateFlow<List<NotifEntity>> = repository.getAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val statsRepository = StatsRepository(repository)

    fun getRepository(): NotifRepository = repository
}

@Composable
fun InboxScreen(
    viewModel: InboxViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val navController = androidx.navigation.compose.rememberNavController()
    val repository = viewModel.getRepository()
    val statsRepo = viewModel.statsRepository
    val context = LocalContext.current
    val scheduler = remember { AlarmScheduler(context, repository) }

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
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(notifications) { notif ->
                        InboxItem(
                            notif = notif,
                            statsRepository = statsRepo,
                            onSnoozeClick = { choice ->
                                val notifId = notif.id ?: 0
                                val now = System.currentTimeMillis()
                                val delayMin = when (choice) {
                                    "debug_1min" -> 1L
                                    "snooze_1h" -> 60L
                                    "tonight" -> {
                                        val target = com.ainotif.assistant.scheduler.rollForwardTonight(now)
                                        com.ainotif.assistant.scheduler.delayMinutesFor(target, now)
                                    }
                                    "tomorrow" -> {
                                        val target = com.ainotif.assistant.scheduler.rollForwardTomorrow(now)
                                        com.ainotif.assistant.scheduler.delayMinutesFor(target, now)
                                    }
                                    else -> null
                                }
                                val hour = when (choice) {
                                    "tonight" -> 18
                                    "tomorrow" -> 9
                                    else -> null
                                }
                                val suggestedDelay = statsRepo.getSuggestedDelay(notif.category ?: "other")
                                val suggestedChoice = if (suggestedDelay != null) {
                                    DefaultRules.defaultSuggestion(notif.category ?: "other")
                                } else {
                                    DefaultRules.defaultSuggestion(notif.category ?: "other")
                                }
                                repository.recordAction(notifId, choice, delayMin, hour, null, suggestedChoice)
                                scheduler.schedule(notifId, com.ainotif.assistant.database.ActionEntity(
                                    notifId = notifId,
                                    choice = choice,
                                    delayMin = delayMin,
                                    hour = hour,
                                    weekday = null,
                                    suggestedChoice = suggestedChoice
                                ))
                            },
                            onOpenSnoozeSheet = { notifId ->
                                navController.navigate(Screen.Snooze.name) {
                                    androidx.navigation.compose.navArgument("notifId") {
                                        type = androidx.navigation.NavType.IntType
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InboxItem(
    notif: NotifEntity,
    statsRepository: StatsRepository,
    onSnoozeClick: (String) -> Unit,
    onOpenSnoozeSheet: (Int) -> Unit
) {
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
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                if (BuildConfig.DEBUG) {
                    Button(onClick = { onSnoozeClick("debug_1min") }) {
                        Text(text = "1 min (debug)")
                    }
                }
                Button(onClick = { onSnoozeClick("now") }) {
                    Text(text = "Now")
                }
                Button(onClick = { onSnoozeClick("snooze_1h") }) {
                    Text(text = "1 hour")
                }
                Button(onClick = { onSnoozeClick("tonight") }) {
                    Text(text = "Tonight")
                }
                Button(onClick = { onSnoozeClick("tomorrow") }) {
                    Text(text = "Tomorrow")
                }
                Button(onClick = { onSnoozeClick("important") }) {
                    Text(text = "Important")
                }
                Button(onClick = { onSnoozeClick("ignore") }) {
                    Text(text = "Ignore")
                }
                Button(onClick = { onOpenSnoozeSheet(notif.id ?: 0) }) {
                    Text(text = "Sheet")
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val date = java.util.Date(millis)
    return java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(date)
}

import androidx.compose.foundation.LocalContext
