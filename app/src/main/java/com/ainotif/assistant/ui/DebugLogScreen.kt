package com.ainotif.assistant.ui

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.ainotif.assistant.ui.log.LogCollector

class DebugLogViewModel : ViewModel() {
    val logs by LogCollector.entries.collectAsState()
}

@Composable
fun DebugLogScreen(
    viewModel: DebugLogViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val logs by viewModel.logs

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(id = R.string.debug_log_title)) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (logs.isEmpty()) {
                Text(text = "No logs yet")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(logs) { entry ->
                        androidx.compose.material3.Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            elevation = 1.dp
                        ) {
                            androidx.compose.foundation.layout.Column(
                                modifier = androidx.compose.foundation.layout.Column(
                                    Modifier.padding(12.dp)
                                )
                            ) {
                                Text(
                                    text = "${entry.timestamp}  [${entry.level}]",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = entry.message,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
