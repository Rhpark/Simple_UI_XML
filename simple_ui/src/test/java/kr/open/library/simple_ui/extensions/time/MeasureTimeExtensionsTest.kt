package kr.open.library.simple_ui.extensions.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

//@Ignore("임시로 비활성화")
class MeasureTimeExtensionsTest {

    @Test
    fun measureTimeMillis_returnsResultAndNonNegativeDuration() {
        val (result, duration) = measureTimeMillis { "value" }

        assertEquals("value", result)
        assertTrue(duration >= 0)
    }

    @Test
    fun measureTimeNanos_returnsResultAndNonNegativeDuration() {
        val (result, duration) = measureTimeNanos { 42 }

        assertEquals(42, result)
        assertTrue(duration >= 0)
    }

    @Test
    fun measureTimeWithCustomProvider_returnsExpectedDuration() {
        var current = 0L
        val provider = {
            current += 5L
            current
        }

        val duration = measureTime(timeProvider = provider) { /* no-op */ }

        assertEquals(5L, duration)
    }

    @Test
    fun measureTimeWithResult_usesTimeProviderCorrectly() {
        var current = 100L
        val provider = {
            val snapshot = current
            current += 7L
            snapshot
        }

        val (result, duration) = measureTimeWithResult(timeProvider = provider) { "answer" }

        assertEquals("answer", result)
        assertEquals(7L, duration)
    }
}
