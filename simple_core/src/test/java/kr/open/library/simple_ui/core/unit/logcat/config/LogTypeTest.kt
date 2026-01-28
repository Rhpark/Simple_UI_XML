package kr.open.library.simple_ui.core.unit.logcat.config

import kr.open.library.simple_ui.core.logcat.config.LogType
import org.junit.Assert.assertEquals
import org.junit.Test

class LogTypeTest {
    @Test
    fun outputCharsMatchSpec() {
        assertEquals('V', LogType.VERBOSE.outputChar)
        assertEquals('D', LogType.DEBUG.outputChar)
        assertEquals('I', LogType.INFO.outputChar)
        assertEquals('W', LogType.WARN.outputChar)
        assertEquals('E', LogType.ERROR.outputChar)
        assertEquals('P', LogType.PARENT.outputChar)
        assertEquals('J', LogType.JSON.outputChar)
        assertEquals('T', LogType.THREAD.outputChar)
    }
}
