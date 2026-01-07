package kr.open.library.simple_ui.core.unit.system_manager.controller.notification.vo

import kr.open.library.simple_ui.core.system_manager.controller.notification.SimpleNotificationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for SimpleNotificationType enum
 */
class NotificationVoUnitTest {
    // ==============================================
    // SimpleNotificationType Tests
    // ==============================================

    @Test
    fun simpleNotificationType_hasCorrectNumberOfValues() {
        val entries = SimpleNotificationType.entries
        assertEquals(3, entries.size)
    }

    @Test
    fun simpleNotificationType_containsAllExpectedValues() {
        val entries = SimpleNotificationType.entries
        assertTrue(entries.contains(SimpleNotificationType.ACTIVITY))
        assertTrue(entries.contains(SimpleNotificationType.SERVICE))
        assertTrue(entries.contains(SimpleNotificationType.BROADCAST))
    }

    @Test
    fun simpleNotificationType_valueOf_returnsCorrectEnum() {
        assertEquals(SimpleNotificationType.ACTIVITY, SimpleNotificationType.valueOf("ACTIVITY"))
        assertEquals(SimpleNotificationType.SERVICE, SimpleNotificationType.valueOf("SERVICE"))
        assertEquals(SimpleNotificationType.BROADCAST, SimpleNotificationType.valueOf("BROADCAST"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun simpleNotificationType_valueOf_throwsExceptionForInvalidName() {
        SimpleNotificationType.valueOf("INVALID_TYPE")
    }

    @Test
    fun simpleNotificationType_enumName_matchesExpectedFormat() {
        assertEquals("ACTIVITY", SimpleNotificationType.ACTIVITY.name)
        assertEquals("SERVICE", SimpleNotificationType.SERVICE.name)
        assertEquals("BROADCAST", SimpleNotificationType.BROADCAST.name)
    }

    @Test
    fun simpleNotificationType_enumOrder_matchesDefinition() {
        val entries = SimpleNotificationType.entries
        assertEquals(SimpleNotificationType.ACTIVITY, entries[0])
        assertEquals(SimpleNotificationType.SERVICE, entries[1])
        assertEquals(SimpleNotificationType.BROADCAST, entries[2])
    }
}
