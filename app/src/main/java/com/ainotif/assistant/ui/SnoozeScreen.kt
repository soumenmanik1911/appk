package com.ainotif.assistant.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ainotif.assistant.database.NotifRepository
import com.ainotif.assistant.service.AlarmScheduler
import com.ainotif.assistant.stats.DefaultRules
import com.ainotif.assistant.stats.StatsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Composable
fun SnoozeScreen(
    notifId: Int,
    repository: NotifRepository,
    scheduler: AlarmScheduler,
    onDismiss: () -> Unit
) {
    val statsRepository = StatsRepository(repository)
    var category by mutableStateOf<String?>(null)
    var suggestedChoice by mutableStateOf<String?>(null)
    var suggestedDelay by mutableStateOf<Int?>(null)

    LaunchedEffect(notifId) {
        CoroutineScope(Dispatchers.IO).launch {
            val notif = repository.getNotifById(notifId)
            val cat = notif?.category
            val openedActions = repository.getOpenedActionsWithCategory()
            val choice = com.ainotif.assistant.stats.suggestChoice(openedActions, cat ?: "other")
            val delay = com.ainotif.assistant.stats.suggestDelay(openedActions, cat ?: "other")
            category = cat
            suggestedChoice = choice
            suggestedDelay = delay
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Snooze Options",
            style = MaterialTheme.typography.titleMedium
        )

        if (suggestedChoice != null) {
            Text(
                text = "Suggested: ${defaultSuggestionLabelFor(suggestedChoice!!)} (${suggestedDelay ?: 0} min)",
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (BuildConfig.DEBUG) {
        Button(
            onClick = {
                repository.recordAction(notifId, "now", null, null, null, suggestedChoice)
                // No alarm scheduled — "Now" just marks the action
                onDismiss()
            },
            colors = if (suggestedChoice == "now") {
                androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                MaterialTheme.colorScheme
            }
        ) {
            androidx.compose.foundation.layout.Column {
                Text(text = "Now")
                if (suggestedChoice == "now") {
                    Text(text = "Suggested", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Button(
                onClick = {
                    repository.recordAction(notifId, "debug_1min", 1L, null, null, suggestedChoice)
                    scheduler.schedule(notifId, com.ainotif.assistant.database.ActionEntity(
                        notifId = notifId,
                        choice = "debug_1min",
                        delayMin = 1L,
                        hour = null,
                        weekday = null,
                        suggestedChoice = suggestedChoice
                    ))
                    onDismiss()
                },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = "In 1 minute (debug)")
            }
        }

        Button(
            onClick = {
                val now = System.currentTimeMillis()
                val target = com.ainotif.assistant.scheduler.rollForwardTonight(now)
                val delayMin = com.ainotif.assistant.scheduler.delayMinutesFor(target, now)
                repository.recordAction(notifId, "tonight", delayMin, 18, null, suggestedChoice)
                scheduler.schedule(notifId, com.ainotif.assistant.database.ActionEntity(
                    notifId = notifId,
                    choice = "tonight",
                    delayMin = delayMin,
                    hour = 18,
                    weekday = null,
                    suggestedChoice = suggestedChoice
                ))
                onDismiss()
            },
            colors = if (suggestedChoice == "tonight") {
                androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                MaterialTheme.colorScheme
            }
        ) {
            androidx.compose.foundation.layout.Column {
                Text(text = "Tonight (6 PM)")
                if (suggestedChoice == "tonight") {
                    Text(text = "Suggested", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Button(
            onClick = {
                val now = System.currentTimeMillis()
                val target = com.ainotif.assistant.scheduler.rollForwardTomorrow(now)
                val delayMin = com.ainotif.assistant.scheduler.delayMinutesFor(target, now)
                repository.recordAction(notifId, "tomorrow", delayMin, 9, null, suggestedChoice)
                scheduler.schedule(notifId, com.ainotif.assistant.database.ActionEntity(
                    notifId = notifId,
                    choice = "tomorrow",
                    delayMin = delayMin,
                    hour = 9,
                    weekday = null,
                    suggestedChoice = suggestedChoice
                ))
                onDismiss()
            },
            colors = if (suggestedChoice == "tomorrow") {
                androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                MaterialTheme.colorScheme
            }
        ) {
            androidx.compose.foundation.layout.Column {
                Text(text = "Tomorrow (9 AM)")
                if (suggestedChoice == "tomorrow") {
                    Text(text = "Suggested", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Button(
            onClick = {
                repository.recordAction(notifId, "important", null, null, null, suggestedChoice)
                onDismiss()
            },
            colors = if (suggestedChoice == "important") {
                androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                MaterialTheme.colorScheme
            }
        ) {
            androidx.compose.foundation.layout.Column {
                Text(text = "Mark important")
                if (suggestedChoice == "important") {
                    Text(text = "Suggested", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Button(
            onClick = {
                repository.recordAction(notifId, "ignore", null, null, null, suggestedChoice)
                onDismiss()
            },
            colors = if (suggestedChoice == "ignore") {
                androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                MaterialTheme.colorScheme
            }
        ) {
            androidx.compose.foundation.layout.Column {
                Text(text = "Ignore")
                if (suggestedChoice == "ignore") {
                    Text(text = "Suggested", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Button(
            onClick = {},
            enabled = false,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        ) {
            Text(text = "Let AI decide (disabled)")
        }
    }
}

private fun defaultSuggestionLabelFor(choice: String): String {
    return when (choice) {
        "tonight" -> "Tonight (6 PM)"
        "tomorrow" -> "Tomorrow (9 AM)"
        "important" -> "Mark important"
        "ignore" -> "Ignore"
        "debug_1min" -> "In 1 minute (debug)"
        "now" -> "Now"
        else -> choice
    }
}
