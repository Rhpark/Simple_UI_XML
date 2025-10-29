package kr.open.library.simple_ui.logcat.internal.formatter

import kr.open.library.simple_ui.logcat.config.LogxConfig
import kr.open.library.simple_ui.logcat.model.LogxType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Ignore
import org.junit.Test
import java.util.EnumSet
@Ignore("임시로 비활성화")
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
}
