package com.ainotif.assistant.stats

import com.ainotif.assistant.database.ActionEntity
import com.ainotif.assistant.database.ActionWithCategory
import com.ainotif.assistant.database.NotifRepository

class StatsRepository(private val repository: NotifRepository) {
    fun getSuggestedDelay(category: String): Int? {
        val all = repository.getAllActionsNonDebug()
        val opened = all.filter { it.category == category && it.delayMin != null }
        return suggestDelayByDelay(opened.map { ActionWithCategory(it.category, it.delayMin, it.choice) }, category)
    }

    fun getSuggestedChoice(category: String): String? {
        val all = repository.getAllActionsNonDebug()
        val filtered = all.filter { it.category == category }
        return suggestChoiceByAction(filtered.map { ActionWithCategory(it.category, it.delayMin, it.choice) })
    }

    fun getDefaultSuggestion(category: String): String {
        return defaultSuggestion(category)
    }

    fun getDefaultSuggestionLabel(category: String): String {
        return defaultSuggestionLabel(category)
    }

    fun getPerCategoryMedian(): Map<String, Int> {
        val all = repository.getAllActionsNonDebug()
        val categories = listOf("work", "family", "shopping", "college", "other")
        val result = mutableMapOf<String, Int>()
        for (cat in categories) {
            val opened = all.filter { it.category == cat && it.delayMin != null }
            val mapped = opened.map { ActionWithCategory(it.category, it.delayMin, it.choice) }
            val delay = suggestDelayByDelay(mapped, cat)
            if (delay != null) {
                result[cat] = delay
            }
        }
        return result
    }

    suspend fun getAcceptanceRate(): Float? {
        val allActions = repository.getAllActions()
        var total = 0
        var accepted = 0
        for (a in allActions) {
            val suggested = a.suggestedChoice
            if (suggested != null && suggested != "debug_1min" && a.choice != "debug_1min") {
                total++
                if (a.choice == suggested) {
                    accepted++
                }
            }
        }
        return if (total == 0) null else (accepted.toFloat() / total * 100)
    }
}

fun suggestDelayByDelay(history: List<ActionWithCategory>, category: String): Int? {
    val delays = mutableListOf<Long>()
    for (a in history) {
        if (a.category == category && a.delayMin != null && a.choice != "debug_1min") {
            delays.add(a.delayMin)
        }
    }
    if (delays.size < 5) return null

    for (i in 1 until delays.size) {
        val key = delays[i]
        var j = i - 1
        while (j >= 0 && delays[j] > key) {
            delays[j + 1] = delays[j]
            j--
        }
        delays[j + 1] = key
    }

    return when {
        delays.size % 2 == 1 -> delays[delays.size / 2].toInt()
        else -> ((delays[delays.size / 2 - 1] + delays[delays.size / 2]) / 2).toInt()
    }
}

fun suggestChoiceByAction(history: List<ActionWithCategory>): String? {
    val counts = mutableMapOf<String, Int>()
    for (a in history) {
        if (a.choice == "debug_1min") continue
        counts[a.choice] = (counts[a.choice] ?: 0) + 1
    }
    if (counts.isEmpty()) return null

    var bestChoice: String? = null
    var bestCount = 0
    for ((choice, count) in counts) {
        if (count > bestCount) {
            bestCount = count
            bestChoice = choice
        }
    }
    return bestChoice
}

fun defaultSuggestion(category: String): String {
    return when (category) {
        "work" -> "tonight"
        "family" -> "now"
        "shopping" -> "ignore"
        "college" -> "tomorrow"
        else -> "ignore"
    }
}

fun defaultSuggestionLabel(category: String): String {
    return when (category) {
        "work" -> "Tonight (6 PM)"
        "family" -> "Now"
        "shopping" -> "Ignore"
        "college" -> "Tomorrow (9 AM)"
        else -> "Ignore"
    }
}
