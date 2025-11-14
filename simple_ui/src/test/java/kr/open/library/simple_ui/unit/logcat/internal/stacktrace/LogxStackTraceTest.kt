package kr.open.library.simple_ui.unit.logcat.internal.stacktrace

import kr.open.library.simple_ui.logcat.internal.stacktrace.LogxStackTrace
import kr.open.library.simple_ui.logcat.internal.stacktrace.LogxStackTraceMetaData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogxStackTraceTest {

    @Test
    fun `getStackTrace variants return metadata`() {
        fun capture(block: () -> LogxStackTraceMetaData): LogxStackTraceMetaData = block()

        val stack = capture { LogxStackTrace().getStackTrace() }
        val parent = capture { LogxStackTrace().getParentStackTrace() }
        val extensions = capture { LogxStackTrace().getExtensionsStackTrace() }
        val parentExt = capture { LogxStackTrace().getParentExtensionsStackTrace() }

        assertTrue(stack.fileName.isNotBlank())
        assertTrue(parent.fileName.isNotBlank())
        assertTrue(extensions.fileName.isNotBlank())
        assertTrue(parentExt.fileName.isNotBlank())
    }

    @Test
    fun `meta data falls back when fileName missing`() {
        val element = StackTraceElement(
            LogxStackTraceTest::class.java.name,
            "fakeMethod",
            null,
            123
        )
        val meta = LogxStackTraceMetaData(element)

        val normal = meta.getMsgFrontNormal()
        val parent = meta.getMsgFrontParent()
        val json = meta.getMsgFrontJson()

        assertTrue(normal.contains("fakeMethod"))
        assertTrue(parent.contains("fakeMethod"))
        assertTrue(json.contains("("))
    }

}
