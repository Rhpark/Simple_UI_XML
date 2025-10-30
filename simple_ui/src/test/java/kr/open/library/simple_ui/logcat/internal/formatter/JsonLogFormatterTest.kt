package kr.open.library.simple_ui.logcat.internal.formatter

import kr.open.library.simple_ui.logcat.config.LogxConfig
import kr.open.library.simple_ui.logcat.model.LogxType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import java.util.EnumSet

class JsonLogFormatterTest {

    private val config = LogxConfig(
        isDebug = true,
        debugLogTypeList = EnumSet.allOf(LogxType::class.java),
        appName = "JsonApp"
    )

    @Test
    fun format_prettyPrintsJson() {
        val formatter = JsonLogFormatter(config)
        val formatted = formatter.format(
            tag = "TAG",
            message = """{"key":"value","list":[1,2]}""",
            logType = LogxType.JSON,
            stackInfo = "info "
        )

        requireNotNull(formatted)
        assertEquals("JsonApp[TAG][JSON]", formatted.tag)
        assertTrue(formatted.message.contains("\n"))
        assertTrue(formatted.message.contains("\"key\""))
    }

    @Test
    fun format_returnsTrimmedStringWhenNotJson() {
        val formatter = JsonLogFormatter(config)
        val formatted = formatter.format(
            tag = "TAG",
            message = " plain text ",
            logType = LogxType.JSON,
            stackInfo = ""
        )

        requireNotNull(formatted)
        assertEquals("plain text", formatted.message)
    }

    @Test
    fun format_returnsNullForNonJsonLogType() {
        val formatter = JsonLogFormatter(config)

        val formatted = formatter.format(
            tag = "TAG",
            message = "{}",
            logType = LogxType.DEBUG,
            stackInfo = ""
        )

        assertEquals(null, formatted)
    }

    @Test
    fun format_preservesEscapedQuotesInsideStrings() {
        val formatter = JsonLogFormatter(config)
        val formatted = formatter.format(
            tag = "TAG",
            message = """{"dialog":"He said \"Hi\""}""",
            logType = LogxType.JSON,
            stackInfo = ""
        )

        requireNotNull(formatted)
        assertTrue(formatted.message.contains("""\"Hi\""""))
    }

    @Test
    fun format_removesDuplicateSpacesOutsideQuotes() {
        val formatter = JsonLogFormatter(config)
        val formatted = formatter.format(
            tag = "TAG",
            message = """{  "key"   :   "value"  }""",
            logType = LogxType.JSON,
            stackInfo = ""
        )

        requireNotNull(formatted)
        formatted.message.lines().forEach { line ->
            val core = line.trimStart()
            assertFalse("Expected no duplicate spaces in: \"$line\"", core.contains("  "))
        }
    }

    @Test
    fun format_supportsJsonArrayInput() {
        val formatter = JsonLogFormatter(config)
        val formatted = formatter.format(
            tag = "TAG",
            message = """[{"item":1},{"item":2}]""",
            logType = LogxType.JSON,
            stackInfo = ""
        )

        requireNotNull(formatted)
        assertTrue(formatted.message.startsWith("["))
        assertTrue(formatted.message.contains("\n"))
    }

    @Test
    fun formatJsonPretty_handlesLeadingWhitespaceViaReflection() {
        val formatter = JsonLogFormatter(config)
        val method = JsonLogFormatter::class.java.getDeclaredMethod("formatJsonPretty", String::class.java)
        method.isAccessible = true

        val result = method.invoke(formatter, "  {\"key\":1}") as String

        assertTrue(result.startsWith("{"))
    }
}
