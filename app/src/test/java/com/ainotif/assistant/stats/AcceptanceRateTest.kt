package com.ainotif.assistant.stats

import com.ainotif.assistant.database.ActionWithCategory
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AcceptanceRateTest {

    @Test
    fun testAcceptanceRateAllMatch() {
        val total = 5
        val accepted = 5
        val rate = if (total == 0) null else (accepted.toFloat() / total * 100)
        assertEquals(100f, rate)
    }

    @Test
    fun testAcceptanceRateNoneMatch() {
        val total = 3
        val accepted = 0
        val rate = if (total == 0) null else (accepted.toFloat() / total * 100)
        assertEquals(0f, rate)
    }

    @Test
    fun testAcceptanceRateNoSuggestions() {
        val total = 0
        val accepted = 0
        val rate = if (total == 0) null else (accepted.toFloat() / total * 100)
        assertNull(rate)
    }

    @Test
    fun testAcceptanceRatePartialMatch() {
        val total = 4
        val accepted = 3
        val rate = if (total == 0) null else (accepted.toFloat() / total * 100)
        assertEquals(75f, rate)
    }
}
