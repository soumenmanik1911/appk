package com.ainotif.assistant.stats

import com.ainotif.assistant.database.ActionWithCategory
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SuggestDelayTest {

    @Test
    fun testZeroSamplesReturnsNull() {
        assertNull(suggestDelayByDelay(emptyList(), "work"))
    }

    @Test
    fun testFourSamplesReturnsNull() {
        val history = listOf(
            ActionWithCategory("work", 60L, "snooze_1h"),
            ActionWithCategory("work", 120L, "tonight"),
            ActionWithCategory("work", 180L, "tomorrow"),
            ActionWithCategory("work", 240L, "important")
        )
        assertNull(suggestDelayByDelay(history, "work"))
    }

    @Test
    fun testFiveSamplesOddMedian() {
        val history = listOf(
            ActionWithCategory("work", 60L, "snooze_1h"),
            ActionWithCategory("work", 120L, "tonight"),
            ActionWithCategory("work", 180L, "tomorrow"),
            ActionWithCategory("work", 240L, "important"),
            ActionWithCategory("work", 300L, "ignore")
        )
        assertEquals(180, suggestDelayByDelay(history, "work"))
    }

    @Test
    fun testSixSamplesEvenMedian() {
        val history = listOf(
            ActionWithCategory("work", 60L, "snooze_1h"),
            ActionWithCategory("work", 120L, "tonight"),
            ActionWithCategory("work", 180L, "tomorrow"),
            ActionWithCategory("work", 240L, "important"),
            ActionWithCategory("work", 300L, "ignore"),
            ActionWithCategory("work", 360L, "snooze_1h")
        )
        assertEquals(210, suggestDelayByDelay(history, "work"))
    }

    @Test
    fun testFiltersByCategory() {
        val history = listOf(
            ActionWithCategory("work", 60L, "snooze_1h"),
            ActionWithCategory("work", 120L, "tonight"),
            ActionWithCategory("work", 180L, "tomorrow"),
            ActionWithCategory("work", 240L, "important"),
            ActionWithCategory("work", 300L, "ignore"),
            ActionWithCategory("shopping", 60L, "ignore")
        )
        assertEquals(180, suggestDelayByDelay(history, "work"))
        assertNull(suggestDelayByDelay(history, "shopping"))
    }

    @Test
    fun testExcludesDebug1min() {
        val history = listOf(
            ActionWithCategory("work", 60L, "debug_1min"),
            ActionWithCategory("work", 120L, "debug_1min"),
            ActionWithCategory("work", 180L, "debug_1min"),
            ActionWithCategory("work", 240L, "debug_1min"),
            ActionWithCategory("work", 300L, "debug_1min")
        )
        assertNull(suggestDelayByDelay(history, "work")) // all debug, 0 real samples
    }

    @Test
    fun testSuggestChoiceReturnsMode() {
        val history = listOf(
            ActionWithCategory("work", 60L, "tonight"),
            ActionWithCategory("work", 120L, "tonight"),
            ActionWithCategory("work", 180L, "ignore"),
            ActionWithCategory("work", 240L, "ignore"),
            ActionWithCategory("work", 300L, "tonight")
        )
        assertEquals("tonight", suggestChoiceByAction(history))
    }

    @Test
    fun testSuggestChoiceEmptyReturnsNull() {
        assertNull(suggestChoiceByAction(emptyList()))
    }

    @Test
    fun testSuggestChoiceIgnoresDebug() {
        val history = listOf(
            ActionWithCategory("work", 60L, "debug_1min"),
            ActionWithCategory("work", 120L, "debug_1min"),
            ActionWithCategory("work", 180L, "ignore"),
            ActionWithCategory("work", 240L, "ignore"),
            ActionWithCategory("work", 300L, "ignore")
        )
        assertEquals("ignore", suggestChoiceByAction(history))
    }

    @Test
    fun testShoppingIgnoreBecomesSuggestion() {
        val history = listOf(
            ActionWithCategory("shopping", 60L, "ignore"),
            ActionWithCategory("shopping", 120L, "ignore"),
            ActionWithCategory("shopping", 180L, "ignore"),
            ActionWithCategory("shopping", 240L, "ignore"),
            ActionWithCategory("shopping", 300L, "ignore"),
            ActionWithCategory("work", 60L, "tonight"),
            ActionWithCategory("work", 120L, "tonight"),
            ActionWithCategory("work", 180L, "tonight"),
            ActionWithCategory("work", 240L, "tonight"),
            ActionWithCategory("work", 300L, "tonight")
        )
        assertEquals("ignore", suggestChoiceByAction(history.filter { it.category == "shopping" }))
        assertEquals("tonight", suggestChoiceByAction(history.filter { it.category == "work" }))
    }
}
