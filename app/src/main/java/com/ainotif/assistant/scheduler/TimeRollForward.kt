package com.ainotif.assistant.scheduler

import java.util.Calendar

fun rollForwardTonight(now: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = now
    cal.set(Calendar.HOUR_OF_DAY, 18)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    var target = cal.timeInMillis
    if (target <= now) {
        cal.add(Calendar.DAY_OF_YEAR, 1)
        target = cal.timeInMillis
    }
    return target
}

fun rollForwardTomorrow(now: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = now
    cal.add(Calendar.DAY_OF_YEAR, 1)
    cal.set(Calendar.HOUR_OF_DAY, 9)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

fun delayMinutesFor(target: Long, now: Long): Long {
    val diff = target - now
    return if (diff <= 0) 1 else (diff + 59999) / 60000
}
