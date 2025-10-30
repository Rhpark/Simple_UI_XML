package kr.open.library.simple_ui.unit.logcat.internal.formatter

import kr.open.library.simple_ui.logcat.config.LogxConfig
import kr.open.library.simple_ui.logcat.internal.formatter.DefaultLogFormatter
import kr.open.library.simple_ui.logcat.model.LogxType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Ignore
import org.junit.Test
import java.util.EnumSet

class DefaultLogFormatterTest {

    private val baseConfig = LogxConfig(
        isDebug = true,
        debugLogTypeList = EnumSet.allOf(LogxType::class.java),
        appName = "SampleApp"
    )

    @Test
    fun format_returnsFormattedDataWhenDebugEnabled() {
        val formatter = DefaultLogFormatter(baseConfig)
        val formatted = formatter.format(
            tag = "TAG",
            message = "Hello",
            logType = LogxType.DEBUG,
            stackInfo = "[MyClass#method:42] "
        )

        requireNotNull(formatted)
        assertEquals("SampleApp[TAG]", formatted.tag)
        assertEquals("[MyClass#method:42] Hello", formatted.message)
        assertEquals(LogxType.DEBUG, formatted.logType)
    }

    @Test
    fun format_returnsNullWhenLogTypeNotAllowed() {
        val config = baseConfig.copy(
            debugLogTypeList = EnumSet.of(LogxType.ERROR)
        )
        val formatter = DefaultLogFormatter(config)

        val formatted = formatter.format(
            tag = "TAG",
            message = "ignored",
            logType = LogxType.DEBUG,
            stackInfo = "info"
        )

        assertNull(formatted)
    }

    @Test
    fun format_returnsNullWhenDebugDisabled() {
        val formatter = DefaultLogFormatter(baseConfig.copy(isDebug = false))

        val formatted = formatter.format(
            tag = "TAG",
            message = "ignored",
            logType = LogxType.DEBUG,
            stackInfo = "info"
        )

        assertNull(formatted)
    }

    @Test
    fun format_allowsStandardLogTypes() {
        val formatter = DefaultLogFormatter(baseConfig)
        val stackInfo = "[Stack] "
        val tag = "TAG"

        listOf(
            LogxType.VERBOSE,
            LogxType.DEBUG,
            LogxType.INFO,
            LogxType.WARN,
            LogxType.ERROR
        ).forEachIndexed { index, type ->
            val message = "message-$index"
            val data = formatter.format(
                tag = tag,
                message = message,
                logType = type,
                stackInfo = stackInfo
            )

            requireNotNull(data)
            assertEquals("SampleApp[$tag]", data.tag)
            assertEquals(stackInfo + message, data.message)
            assertEquals(type, data.logType)
        }
    }

    @Test
    fun format_returnsNullForNonDefaultLogTypes() {
        val formatter = DefaultLogFormatter(
            baseConfig.copy(debugLogTypeList = EnumSet.allOf(LogxType::class.java))
        )

        listOf(LogxType.PARENT, LogxType.JSON, LogxType.THREAD_ID).forEach { type ->
            val formatted = formatter.format(
                tag = "TAG",
                message = "ignored",
                logType = type,
                stackInfo = "[stack]"
            )
            assertNull(formatted)
        }
    }

    @Test
    fun format_handlesNullMessageGracefully() {
        val formatter = DefaultLogFormatter(baseConfig)

        val formatted = formatter.format(
            tag = "TAG",
            message = null,
            logType = LogxType.INFO,
            stackInfo = "[stack]"
        )

        requireNotNull(formatted)
        assertEquals("[stack]", formatted.message)
    }

    @Test
    fun format_usesDefaultStackInfoWhenOmitted() {
        val formatter = DefaultLogFormatter(baseConfig)

        val formatted = formatter.format(
            tag = "TAG",
            message = "hello",
            logType = LogxType.DEBUG
        )

        requireNotNull(formatted)
        assertEquals("hello", formatted.message)
    }
}
