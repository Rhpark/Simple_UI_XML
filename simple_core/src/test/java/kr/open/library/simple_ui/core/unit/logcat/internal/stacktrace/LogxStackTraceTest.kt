package kr.open.library.simple_ui.core.unit.logcat.internal.stacktrace

import kr.open.library.simple_ui.core.logcat.internal.stacktrace.LogxStackTrace
import kr.open.library.simple_ui.core.logcat.internal.stacktrace.LogxStackTraceMetaData
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.InvocationTargetException

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
        val element =
            StackTraceElement(
                LogxStackTraceTest::class.java.name,
                "fakeMethod",
                null,
                123,
            )
        val meta = LogxStackTraceMetaData(element)

        val normal = meta.getMsgFrontNormal()
        val parent = meta.getMsgFrontParent()
        val json = meta.getMsgFrontJson()

        assertTrue(normal.contains("fakeMethod"))
        assertTrue(parent.contains("fakeMethod"))
        assertTrue(json.contains("("))
    }

    @Test
    fun `getStackTrace throws when requested level exceeds stack size`() {
        val method = LogxStackTrace::class.java.getDeclaredMethod("getStackTrace", Int::class.javaPrimitiveType)
        method.isAccessible = true

        assertThrows(InvocationTargetException::class.java) {
            method.invoke(LogxStackTrace(), Int.MAX_VALUE)
        }
    }

    @Test
    fun `isCoroutinePath recognizes coroutine package`() {
        val method = LogxStackTrace::class.java.getDeclaredMethod("isCoroutinePath", String::class.java)
        method.isAccessible = true

        val result = method.invoke(LogxStackTrace(), "kotlinx.coroutines.JobSupport") as Boolean

        assertTrue(result)
    }

    @Test
    fun `isNormalMethod filters lambda synthesized classes`() {
        val method = LogxStackTrace::class.java.getDeclaredMethod("isNormalMethod", StackTraceElement::class.java)
        method.isAccessible = true

        val lambdaFrame = StackTraceElement("com.example.Foo\$Lambda$1", "invoke", "Foo.kt", 5)
        val normalFrame = StackTraceElement("com.example.Bar", "invoke", "Bar.kt", 6)

        val isLambdaNormal = method.invoke(LogxStackTrace(), lambdaFrame) as Boolean
        val isNormal = method.invoke(LogxStackTrace(), normalFrame) as Boolean

        assertTrue(isNormal)
        assertFalse(isLambdaNormal)
    }
}
