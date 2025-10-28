package kr.open.library.simple_ui.logcat.internal.formatter

import kr.open.library.simple_ui.logcat.config.LogxConfig
import kr.open.library.simple_ui.logcat.model.LogxType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
}
