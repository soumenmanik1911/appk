package com.ainotif.assistant.scheduler

import org.junit.Test
import kotlin.test.assertEquals

class TimeRollForwardTest {

    @Test
    fun testTonightBefore6pm() {
        // 3 PM on Nov 7 -> tonight 6 PM same day (3 hours = 180 min)
        val now = buildTime(2026, 11, 7, 15, 0)
        val target = rollForwardTonight(now)
        val delay = delayMinutesFor(target, now)
        assertEquals(180, delay)
    }

    @Test
    fun testTonightAfter6pm() {
        // 7 PM on Nov 7 -> tomorrow 6 PM (23 hours = 1380 min)
        val now = buildTime(2026, 11, 7, 19, 0)
        val target = rollForwardTonight(now)
        val delay = delayMinutesFor(target, now)
        assertEquals(1380, delay)
    }

    @Test
    fun testTonightExactly6pm() {
        // 6 PM exactly -> target equals now, roll forward to tomorrow 6 PM (24 hours = 1440 min)
        val now = buildTime(2026, 11, 7, 18, 0)
        val target = rollForwardTonight(now)
        val delay = delayMinutesFor(target, now)
        assertEquals(1440, delay)
    }

    @Test
    fun testTomorrowAlwaysNextDay() {
        // 8 AM on Nov 7 -> tomorrow 9 AM (25 hours = 1500 min)
        val now = buildTime(2026, 11, 7, 8, 0)
        val target = rollForwardTomorrow(now)
        val delay = delayMinutesFor(target, now)
        assertEquals(1500, delay)
    }

    @Test
    fun testTomorrowAfter9am() {
        // 10 AM on Nov 7 -> tomorrow 9 AM (23 hours = 1380 min)
        val now = buildTime(2026, 11, 7, 10, 0)
        val target = rollForwardTomorrow(now)
        val delay = delayMinutesFor(target, now)
        assertEquals(1380, delay)
    }

    @Test
    fun testTomorrowBeforeMidnight() {
        // 11 PM on Nov 7 -> tomorrow 9 AM (10 hours = 600 min)
        val now = buildTime(2026, 11, 7, 23, 0)
        val target = rollForwardTomorrow(now)
        val delay = delayMinutesFor(target, now)
        assertEquals(600, delay)
    }

    private fun buildTime(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        val cal = java.util.Calendar.getInstance()
        cal.clear()
        cal.set(year, month - 1, day, hour, minute, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
