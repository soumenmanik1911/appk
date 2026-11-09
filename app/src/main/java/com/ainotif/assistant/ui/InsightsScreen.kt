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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ainotif.assistant.stats.StatsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InsightsViewModel(private val statsRepository: StatsRepository) {
    var perCategoryMedian by mutableStateOf<Map<String, Int>>(emptyMap())
    var acceptanceRate by mutableStateOf<Float?>(null)

    fun load() {
        perCategoryMedian = statsRepository.getPerCategoryMedian()
        CoroutineScope(Dispatchers.IO).launch {
            val rate = statsRepository.getAcceptanceRate()
            acceptanceRate = rate
        }
    }
}

@Composable
fun InsightsScreen(
    statsRepository: StatsRepository
) {
    val viewModel = androidx.compose.runtime.remember(statsRepository) {
        InsightsViewModel(statsRepository).also { it.load() }
    }
    val median by viewModel.perCategoryMedian
    val acceptanceRate by viewModel.acceptanceRate

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.insights_title)) }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (median.isEmpty()) {
                Text(text = stringResource(id = R.string.insights_no_data))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(median.entries.toList()) { (cat, delay) ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            elevation = 2.dp
                        ) {
                            androidx.compose.foundation.layout.Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "${cat.capitalize()}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = stringResource(id = R.string.insights_learned_delay, delay),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    elevation = 2.dp
                ) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.insights_acceptance_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (acceptanceRate == null) {
                            Text(
                                text = stringResource(id = R.string.insights_acceptance_no_data),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            Text(
                                text = "%.0f%%".format(acceptanceRate),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Text(
                            text = stringResource(id = R.string.insights_acceptance_subtitle),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
