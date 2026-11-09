package com.ainotif.assistant.stats

fun defaultSuggestion(category: String): String {
    return when (category) {
        "work" -> "tonight"
        "family" -> "important"
        "shopping" -> "ignore"
        "college" -> "tomorrow"
        else -> "ignore"
    }
}

fun defaultSuggestionLabel(category: String): String {
    return when (category) {
        "work" -> "Tonight (6 PM)"
        "family" -> "Mark important"
        "shopping" -> "Ignore"
        "college" -> "Tomorrow (9 AM)"
        else -> "Ignore"
    }
}
