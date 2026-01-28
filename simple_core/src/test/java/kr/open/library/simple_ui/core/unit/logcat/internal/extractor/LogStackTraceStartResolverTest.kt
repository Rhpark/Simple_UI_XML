package kr.open.library.simple_ui.core.unit.logcat.internal.extractor

import kr.open.library.simple_ui.core.logcat.internal.extractor.LogStackTraceStartResolver
import org.junit.Assert.assertEquals
import org.junit.Test

class LogStackTraceStartResolverTest {
    @Test
    fun resolveUsesFallbackWhenNoInternalPrefix() {
        val stack = Array(10) { index ->
            StackTraceElement("com.example.Test$index", "method", "Test.kt", 10)
        }

        val start = LogStackTraceStartResolver().resolve(stack)

        assertEquals(4, start)
    }

    @Test
    fun resolveUsesAfterInternalEndIndex() {
        val stack = Array(10) { index ->
            val className = if (index <= 7) {
                "kr.open.library.simple_ui.core.logcat.Internal$index"
            } else {
                "com.example.External$index"
            }
            StackTraceElement(className, "method", "Test.kt", 10)
        }

        val start = LogStackTraceStartResolver().resolve(stack)

        assertEquals(8, start)
    }
}
