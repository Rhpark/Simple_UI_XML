package kr.open.library.simple_ui.unit.logcat.internal.formatter

import kr.open.library.simple_ui.logcat.config.LogxConfig
import kr.open.library.simple_ui.logcat.internal.formatter.ParentLogFormatter
import kr.open.library.simple_ui.logcat.internal.stacktrace.LogxStackTrace
import kr.open.library.simple_ui.logcat.model.LogxType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import java.util.EnumSet

class ParentLogFormatterTest {

    private val baseConfig = LogxConfig(
        isDebug = true,
        debugLogTypeList = EnumSet.of(LogxType.PARENT),
        appName = "TestApp"
    )

    private val mockStackTrace = LogxStackTrace()

    @Test
    fun format_returnsFormattedDataWhenParentEnabled() {
        val formatter = ParentLogFormatter(baseConfig, mockStackTrace, isExtensions = false)
        val formatted = formatter.format(
            tag = "MyTag",
            message = "Hello",
            logType = LogxType.PARENT,
            stackInfo = "[Class#method:10] "
        )

        requireNotNull(formatted)
        assertEquals("TestApp[MyTag][PARENT]", formatted.tag)
        assertEquals("┖[Class#method:10] Hello", formatted.message)
        assertEquals(LogxType.PARENT, formatted.logType)
    }

    @Test
    fun format_returnsNullWhenLogTypeNotParent() {
        val formatter = ParentLogFormatter(baseConfig, mockStackTrace, isExtensions = false)

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
        val formatter = ParentLogFormatter(config, mockStackTrace, isExtensions = false)

        val formatted = formatter.format(
            tag = "TAG",
            message = "ignored",
            logType = LogxType.PARENT,
            stackInfo = ""
        )

        assertNull(formatted)
    }

    @Test
    fun format_returnsNullWhenParentNotInAllowedTypes() {
        val config = baseConfig.copy(
            debugLogTypeList = EnumSet.of(LogxType.DEBUG, LogxType.ERROR)
        )
        val formatter = ParentLogFormatter(config, mockStackTrace, isExtensions = false)

        val formatted = formatter.format(
            tag = "TAG",
            message = "ignored",
            logType = LogxType.PARENT,
            stackInfo = ""
        )

        assertNull(formatted)
    }

    @Test
    fun formatParentInfo_returnsFormattedDataWhenEnabled() {
        val formatter = ParentLogFormatter(baseConfig, mockStackTrace, isExtensions = false)

        val parentInfo = formatter.formatParentInfo("MyTag")

        requireNotNull(parentInfo)
        assertEquals("TestApp[MyTag][PARENT]", parentInfo.tag)
        assertTrue(parentInfo.message.startsWith("┎"))
        assertEquals(LogxType.PARENT, parentInfo.logType)
    }

    @Test
    fun formatParentInfo_returnsNullWhenDebugDisabled() {
        val config = baseConfig.copy(isDebug = false)
        val formatter = ParentLogFormatter(config, mockStackTrace, isExtensions = false)

        val parentInfo = formatter.formatParentInfo("TAG")

        assertNull(parentInfo)
    }

    @Test
    fun formatParentInfo_returnsNullWhenParentNotInAllowedTypes() {
        val config = baseConfig.copy(
            debugLogTypeList = EnumSet.of(LogxType.DEBUG)
        )
        val formatter = ParentLogFormatter(config, mockStackTrace, isExtensions = false)

        val parentInfo = formatter.formatParentInfo("TAG")

        assertNull(parentInfo)
    }

    @Test
    fun isExtensions_false_usesRegularParentTrace() {
        val formatter = ParentLogFormatter(baseConfig, mockStackTrace, isExtensions = false)

        val parentInfo = formatter.formatParentInfo("TAG")

        requireNotNull(parentInfo)
        assertTrue(parentInfo.message.startsWith("┎"))
    }

    @Test
    fun isExtensions_true_usesExtensionsParentTrace() {
        val formatter = ParentLogFormatter(baseConfig, mockStackTrace, isExtensions = true)

        val parentInfo = formatter.formatParentInfo("TAG")

        requireNotNull(parentInfo)
        assertTrue(parentInfo.message.startsWith("┎"))
    }

    @Test
    fun format_messageIncludesTreeCharacter() {
        val formatter = ParentLogFormatter(baseConfig, mockStackTrace, isExtensions = false)
        val formatted = formatter.format(
            tag = "TAG",
            message = "Test message",
            logType = LogxType.PARENT,
            stackInfo = "[StackInfo] "
        )

        requireNotNull(formatted)
        assertTrue(formatted.message.startsWith("┖"))
        assertTrue(formatted.message.contains("[StackInfo]"))
        assertTrue(formatted.message.contains("Test message"))
        assertEquals("┖[StackInfo] Test message", formatted.message)
    }

    @Test
    fun format_handlesNullMessage() {
        val formatter = ParentLogFormatter(baseConfig, mockStackTrace, isExtensions = false)
        val formatted = formatter.format(
            tag = "TAG",
            message = null,
            logType = LogxType.PARENT,
            stackInfo = "[Info] "
        )

        requireNotNull(formatted)
        assertEquals("┖[Info] ", formatted.message)
    }

    @Test
    fun format_handlesEmptyStackInfo() {
        val formatter = ParentLogFormatter(baseConfig, mockStackTrace, isExtensions = false)
        val formatted = formatter.format(
            tag = "TAG",
            message = "Message",
            logType = LogxType.PARENT,
            stackInfo = ""
        )

        requireNotNull(formatted)
        assertEquals("┖Message", formatted.message)
    }

    @Test
    fun format_tagIncludesParentSuffix() {
        val formatter = ParentLogFormatter(baseConfig, mockStackTrace, isExtensions = false)
        val formatted = formatter.format(
            tag = "CustomTag",
            message = "msg",
            logType = LogxType.PARENT,
            stackInfo = ""
        )

        requireNotNull(formatted)
        assertTrue(formatted.tag.endsWith("[PARENT]"))
        assertTrue(formatted.tag.contains("CustomTag"))
    }
}
