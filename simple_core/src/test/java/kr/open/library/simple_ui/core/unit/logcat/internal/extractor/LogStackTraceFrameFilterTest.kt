package kr.open.library.simple_ui.core.unit.logcat.internal.extractor

import kr.open.library.simple_ui.core.logcat.internal.extractor.LogStackTraceFrameFilter
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogStackTraceFrameFilterTest {
    @Test
    fun isSkippedMatchesPrefix() {
        val filter = LogStackTraceFrameFilter(setOf("com.example."))
        val skipped = StackTraceElement("com.example.Test", "method", "Test.kt", 10)
        val allowed = StackTraceElement("com.other.Test", "method", "Test.kt", 10)

        assertTrue(filter.isSkipped(skipped))
        assertFalse(filter.isSkipped(allowed))
    }

    @Test
    fun isSyntheticDetectsInvalidFrames() {
        val filter = LogStackTraceFrameFilter(emptySet())
        val unknown = StackTraceElement("com.example.Test", "method", null, 10)
        val unknownName = StackTraceElement("com.example.Test", "method", "Unknown", 10)
        val zeroLine = StackTraceElement("com.example.Test", "method", "Test.kt", 0)
        val d8 = StackTraceElement("D8${'$'}${'$'}SyntheticClass", "method", "Test.kt", 10)
        val access = StackTraceElement("com.example.Test", "access${'$'}123", "Test.kt", 10)
        val normal = StackTraceElement("com.example.Test", "method", "Test.kt", 10)

        assertTrue(filter.isSynthetic(unknown))
        assertTrue(filter.isSynthetic(unknownName))
        assertTrue(filter.isSynthetic(zeroLine))
        assertTrue(filter.isSynthetic(d8))
        assertTrue(filter.isSynthetic(access))
        assertFalse(filter.isSynthetic(normal))
    }
}
