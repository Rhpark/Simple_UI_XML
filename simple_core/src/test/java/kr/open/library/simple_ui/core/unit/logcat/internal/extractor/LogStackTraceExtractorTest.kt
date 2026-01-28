package kr.open.library.simple_ui.core.unit.logcat.internal.extractor

import kr.open.library.simple_ui.core.logcat.internal.extractor.LogStackTraceExtractor
import org.junit.Assert.assertTrue
import org.junit.Test

class LogStackTraceExtractorTest {
    @Test
    fun extractReturnsCurrentFrame() {
        val frames = LogStackTraceExtractor(emptySet()).extract()

        assertTrue(frames.current.fileName.isNotBlank())
        assertTrue(frames.current.methodName.isNotBlank())
        assertTrue(frames.current.lineNumber >= 0)
    }
}
