package kr.open.library.simple_ui.unit.logcat.internal.formatter

import kr.open.library.simple_ui.logcat.config.LogxConfig
import kr.open.library.simple_ui.logcat.internal.formatter.ThreadIdLogFormatter
import kr.open.library.simple_ui.logcat.model.LogxType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import java.util.EnumSet

class ThreadIdLogFormatterTest {

    private val baseConfig = LogxConfig(
        isDebug = true,
        debugLogTypeList = EnumSet.of(LogxType.THREAD_ID),
        appName = "TestApp"
    )

    @Test
    fun format_returnsFormattedDataWithThreadId() {
        val formatter = ThreadIdLogFormatter(baseConfig)
        val formatted = formatter.format(
            tag = "MyTag",
            message = "Hello",
            logType = LogxType.THREAD_ID,
            stackInfo = "[Class#method:10] "
        )

        requireNotNull(formatted)
        assertEquals("TestApp[MyTag][T_ID]", formatted.tag)
        assertTrue(formatted.message.contains("[${Thread.currentThread().id}]"))
        assertTrue(formatted.message.contains("[Class#method:10]"))
        assertTrue(formatted.message.contains("Hello"))
        assertEquals(LogxType.THREAD_ID, formatted.logType)
    }

    @Test
    fun format_returnsNullWhenLogTypeNotThreadId() {
        val formatter = ThreadIdLogFormatter(baseConfig)

        val formatted = formatter.format(
            tag = "TAG",
            message = "ignored",
            logType = LogxType.DEBUG,
            stackInfo = ""
        )

        assertNull(formatted)
    }

    @Test
    fun format_returnsNullWhenDebugDisabled() {
        val config = baseConfig.copy(isDebug = false)
        val formatter = ThreadIdLogFormatter(config)

        val formatted = formatter.format(
            tag = "TAG",
            message = "ignored",
            logType = LogxType.THREAD_ID,
            stackInfo = ""
        )

        assertNull(formatted)
    }

    @Test
    fun format_returnsNullWhenThreadIdNotInAllowedTypes() {
        val config = baseConfig.copy(
            debugLogTypeList = EnumSet.of(LogxType.DEBUG, LogxType.ERROR)
        )
        val formatter = ThreadIdLogFormatter(config)

        val formatted = formatter.format(
            tag = "TAG",
            message = "ignored",
            logType = LogxType.THREAD_ID,
            stackInfo = ""
        )

        assertNull(formatted)
    }

    @Test
    fun format_includesThreadIdInMessage() {
        val formatter = ThreadIdLogFormatter(baseConfig)
        val formatted = formatter.format(
            tag = "TAG",
            message = "Test message",
            logType = LogxType.THREAD_ID,
            stackInfo = ""
        )

        requireNotNull(formatted)
        assertTrue(formatted.message.startsWith("[${Thread.currentThread().id}]"))
        assertTrue(formatted.message.endsWith("Test message"))
    }

    @Test
    fun format_handlesNullMessage() {
        val formatter = ThreadIdLogFormatter(baseConfig)
        val formatted = formatter.format(
            tag = "TAG",
            message = null,
            logType = LogxType.THREAD_ID,
            stackInfo = "[Info] "
        )

        requireNotNull(formatted)
        assertTrue(formatted.message.contains("[${Thread.currentThread().id}]"))
        assertTrue(formatted.message.contains("[Info]"))
    }

    @Test
    fun format_handlesEmptyStackInfo() {
        val formatter = ThreadIdLogFormatter(baseConfig)
        val formatted = formatter.format(
            tag = "TAG",
            message = "Message",
            logType = LogxType.THREAD_ID,
            stackInfo = ""
        )

        requireNotNull(formatted)
        assertEquals("[${Thread.currentThread().id}]Message", formatted.message)
    }

    @Test
    fun format_tagIncludesTIdSuffix() {
        val formatter = ThreadIdLogFormatter(baseConfig)
        val formatted = formatter.format(
            tag = "CustomTag",
            message = "msg",
            logType = LogxType.THREAD_ID,
            stackInfo = ""
        )

        requireNotNull(formatted)
        assertTrue(formatted.tag.endsWith("[T_ID]"))
        assertTrue(formatted.tag.contains("CustomTag"))
    }
}
