package com.ainotif.assistant.ui.log

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LogEntry(
    val timestamp: String,
    val level: String,
    val message: String
)

object LogCollector {
    private val _entries = MutableStateFlow(emptyList<LogEntry>())
    val entries: StateFlow<List<LogEntry>> = _entries.asStateFlow()
    private val maxEntries = 200

    fun log(level: String, message: String) {
        val entry = LogEntry(
            timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
            level = level,
            message = message
        )
        val current = _entries.value
        val updated = (current + entry).takeLast(maxEntries)
        _entries.value = updated
    }

    fun info(message: String) = log("INFO", message)
    fun error(message: String) = log("ERROR", message)
    fun debug(message: String) = log("DEBUG", message)
}
