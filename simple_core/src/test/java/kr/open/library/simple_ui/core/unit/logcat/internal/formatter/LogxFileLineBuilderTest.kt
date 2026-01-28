package kr.open.library.simple_ui.core.unit.logcat.internal.formatter

import kr.open.library.simple_ui.core.logcat.config.LogType
import kr.open.library.simple_ui.core.logcat.internal.formatter.FormattedJson
import kr.open.library.simple_ui.core.logcat.internal.formatter.LogxFileLineBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LogxFileLineBuilderTest {
    private val builder = LogxFileLineBuilder()

    @Test
    fun buildLinesFormatsTimestampAndPayload() {
        val lines = builder.buildLines(LogType.DEBUG, "AppName", listOf("payload"))
        assertEquals(1, lines.size)

        val line = lines[0]
        val regex = Regex("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3} \\[D\\] AppName : payload$")
        assertTrue(regex.matches(line))
    }

    @Test
    fun buildJsonLinesPreservesBodyAndEnd() {
        val formatted = FormattedJson(
            header = "[JSON](MainActivity.kt:1).test -",
            bodyLines = listOf("{", "    \"name\": \"Lee\"", "}"),
        )

        val lines = builder.buildJsonLines("AppName", formatted)
        assertEquals(5, lines.size)

        val headerLine = lines.first()
        assertNotNull(headerLine)
        assertTrue(headerLine.contains("[J] AppName : [JSON](MainActivity.kt:1).test -"))
        assertEquals("{", lines[1])
        assertEquals("    \"name\": \"Lee\"", lines[2])
        assertEquals("}", lines[3])
        assertEquals("[End]", lines[4])
    }
}
