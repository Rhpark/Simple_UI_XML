package kr.open.library.simple_ui.core.unit.logcat.internal.formatter

import kr.open.library.simple_ui.core.logcat.internal.extractor.LogStackFrame
import kr.open.library.simple_ui.core.logcat.internal.extractor.LogStackFrames
import kr.open.library.simple_ui.core.logcat.internal.formatter.LogxFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogxFormatterTest {
    private val frame = LogStackFrame(
        fileName = "MainActivity.kt",
        lineNumber = 25,
        methodName = "onCreate",
        className = "MainActivity",
    )

    private val parentFrame = LogStackFrame(
        fileName = "BaseActivity.kt",
        lineNumber = 10,
        methodName = "onCreate",
        className = "BaseActivity",
    )

    @Test
    fun formatBasicWithoutMessageOmitsSuffix() {
        val result = LogxFormatter.formatBasic(frame, "msg", hasMessage = false)

        assertEquals("(MainActivity.kt:25).onCreate", result)
    }

    @Test
    fun formatBasicWithNullMessageUsesNullLiteral() {
        val result = LogxFormatter.formatBasic(frame, null, hasMessage = true)

        assertEquals("(MainActivity.kt:25).onCreate - null", result)
    }

    @Test
    fun formatParentIncludesParentAndCurrent() {
        val frames = LogStackFrames(current = frame, parent = parentFrame)
        val lines = LogxFormatter.formatParent(frames, "msg", hasMessage = true)

        assertEquals(2, lines.size)
        assertTrue(lines[0].contains("[PARENT]"))
        assertTrue(lines[0].contains("(BaseActivity.kt:10).onCreate"))
        assertTrue(lines[1].contains("[PARENT]"))
        assertTrue(lines[1].contains("(MainActivity.kt:25).onCreate - msg"))
    }

    @Test
    fun formatThreadIncludesThreadIdAndMeta() {
        val result = LogxFormatter.formatThread(frame, 7L, "msg", hasMessage = true)

        assertTrue(result.contains("[TID = 7]"))
        assertTrue(result.contains("(MainActivity.kt:25).onCreate"))
        assertTrue(result.contains("- msg"))
    }

    @Test
    fun formatJsonPrettyPrintsBody() {
        val formatted = LogxFormatter.formatJson(frame, "{\"name\":\"Lee\",\"items\":[1,2]}")

        assertEquals("[JSON](MainActivity.kt:25).onCreate -", formatted.header)
        assertEquals("[End]", formatted.endLine)
        assertTrue(formatted.bodyLines.first() == "{")
        assertTrue(formatted.bodyLines.contains("    \"name\": \"Lee\","))
        assertTrue(formatted.bodyLines.contains("    \"items\": ["))
        assertTrue(formatted.bodyLines.contains("        1,"))
        assertTrue(formatted.bodyLines.contains("        2"))
        assertTrue(formatted.bodyLines.last() == "}")
    }
}
