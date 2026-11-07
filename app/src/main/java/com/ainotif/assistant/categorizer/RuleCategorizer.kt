package com.ainotif.assistant.categorizer

fun categorize(pkg: String, title: String?, text: String?): String {
    val source = buildString {
        append(pkg.lowercase())
        append(' ')
        append(title?.lowercase() ?: "")
        append(' ')
        append(text?.lowercase() ?: "")
    }

    val workKeywords = listOf("gmail", "outlook", "slack", "meeting", "deadline", "zoom", "teams")
    val familyKeywords = listOf("whatsapp", "messages", "mom", "dad", "family", "sister", "brother")
    val shoppingKeywords = listOf("amazon", "ebay", "shop", "order", "delivery", "store", "receipt")
    val collegeKeywords = listOf("canvas", "blackboard", "assignment", "class", "lecture", "university", "course")

    for (kw in workKeywords) {
        if (source.contains(kw)) return "work"
    }
    for (kw in familyKeywords) {
        if (source.contains(kw)) return "family"
    }
    for (kw in shoppingKeywords) {
        if (source.contains(kw)) return "shopping"
    }
    for (kw in collegeKeywords) {
        if (source.contains(kw)) return "college"
    }

    return "other"
}
