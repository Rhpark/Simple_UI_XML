package kr.open.library.simple_ui.core.unit.logcat.model

import kr.open.library.simple_ui.core.logcat.model.LogxType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for LogxType enum
 */
class LogxTypeUnitTest {
    @Test
    fun logxType_hasCorrectNumberOfValues() {
        val entries = LogxType.entries
        assertEquals(8, entries.size)
    }

    @Test
    fun logxType_containsAllExpectedValues() {
        val entries = LogxType.entries
        assertTrue(entries.contains(LogxType.VERBOSE))
        assertTrue(entries.contains(LogxType.DEBUG))
        assertTrue(entries.contains(LogxType.INFO))
        assertTrue(entries.contains(LogxType.WARN))
        assertTrue(entries.contains(LogxType.ERROR))
        assertTrue(entries.contains(LogxType.PARENT))
        assertTrue(entries.contains(LogxType.JSON))
        assertTrue(entries.contains(LogxType.THREAD_ID))
    }

    @Test
    fun logxType_hasCorrectLogTypeStrings() {
        assertEquals("V", LogxType.VERBOSE.logTypeString)
        assertEquals("D", LogxType.DEBUG.logTypeString)
        assertEquals("I", LogxType.INFO.logTypeString)
        assertEquals("W", LogxType.WARN.logTypeString)
        assertEquals("E", LogxType.ERROR.logTypeString)
        assertEquals("P", LogxType.PARENT.logTypeString)
        assertEquals("J", LogxType.JSON.logTypeString)
        assertEquals("T", LogxType.THREAD_ID.logTypeString)
    }

    @Test
    fun logxType_valueOf_returnsCorrectEnum() {
        assertEquals(LogxType.VERBOSE, LogxType.valueOf("VERBOSE"))
        assertEquals(LogxType.DEBUG, LogxType.valueOf("DEBUG"))
        assertEquals(LogxType.INFO, LogxType.valueOf("INFO"))
        assertEquals(LogxType.WARN, LogxType.valueOf("WARN"))
        assertEquals(LogxType.ERROR, LogxType.valueOf("ERROR"))
        assertEquals(LogxType.PARENT, LogxType.valueOf("PARENT"))
        assertEquals(LogxType.JSON, LogxType.valueOf("JSON"))
        assertEquals(LogxType.THREAD_ID, LogxType.valueOf("THREAD_ID"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun logxType_valueOf_throwsExceptionForInvalidName() {
        LogxType.valueOf("INVALID_LOG_TYPE")
    }

    @Test
    fun logxType_enumName_matchesExpectedFormat() {
        assertEquals("VERBOSE", LogxType.VERBOSE.name)
        assertEquals("DEBUG", LogxType.DEBUG.name)
        assertEquals("INFO", LogxType.INFO.name)
        assertEquals("WARN", LogxType.WARN.name)
        assertEquals("ERROR", LogxType.ERROR.name)
        assertEquals("PARENT", LogxType.PARENT.name)
        assertEquals("JSON", LogxType.JSON.name)
        assertEquals("THREAD_ID", LogxType.THREAD_ID.name)
    }

    @Test
    fun logxType_enumOrder_matchesLogPriority() {
        val entries = LogxType.entries
        assertEquals(LogxType.VERBOSE, entries[0])
        assertEquals(LogxType.DEBUG, entries[1])
        assertEquals(LogxType.INFO, entries[2])
        assertEquals(LogxType.WARN, entries[3])
        assertEquals(LogxType.ERROR, entries[4])
    }
}
