package kr.open.library.simple_ui.core.unit.system_manager.controller.notification.vo

import kr.open.library.simple_ui.core.system_manager.controller.notification.vo.NotificationStyle
import kr.open.library.simple_ui.core.system_manager.controller.notification.vo.SimpleNotificationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for NotificationStyle and SimpleNotificationType enums
 */
class NotificationVoUnitTest {
    // ==============================================
    // NotificationStyle Tests
    // ==============================================

    @Test
    fun notificationStyle_hasCorrectNumberOfValues() {
        val entries = NotificationStyle.entries
        assertEquals(4, entries.size)
    }

    @Test
    fun notificationStyle_containsAllExpectedValues() {
        val entries = NotificationStyle.entries
        assertTrue(entries.contains(NotificationStyle.DEFAULT))
        assertTrue(entries.contains(NotificationStyle.BIG_PICTURE))
        assertTrue(entries.contains(NotificationStyle.BIG_TEXT))
        assertTrue(entries.contains(NotificationStyle.PROGRESS))
    }

    @Test
    fun notificationStyle_valueOf_returnsCorrectEnum() {
        assertEquals(NotificationStyle.DEFAULT, NotificationStyle.valueOf("DEFAULT"))
        assertEquals(NotificationStyle.BIG_PICTURE, NotificationStyle.valueOf("BIG_PICTURE"))
        assertEquals(NotificationStyle.BIG_TEXT, NotificationStyle.valueOf("BIG_TEXT"))
        assertEquals(NotificationStyle.PROGRESS, NotificationStyle.valueOf("PROGRESS"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun notificationStyle_valueOf_throwsExceptionForInvalidName() {
        NotificationStyle.valueOf("INVALID_STYLE")
    }

    @Test
    fun notificationStyle_enumName_matchesExpectedFormat() {
        assertEquals("DEFAULT", NotificationStyle.DEFAULT.name)
        assertEquals("BIG_PICTURE", NotificationStyle.BIG_PICTURE.name)
        assertEquals("BIG_TEXT", NotificationStyle.BIG_TEXT.name)
        assertEquals("PROGRESS", NotificationStyle.PROGRESS.name)
    }

    @Test
    fun notificationStyle_enumOrder_matchesDefinition() {
        val entries = NotificationStyle.entries
        assertEquals(NotificationStyle.DEFAULT, entries[0])
        assertEquals(NotificationStyle.BIG_PICTURE, entries[1])
        assertEquals(NotificationStyle.BIG_TEXT, entries[2])
        assertEquals(NotificationStyle.PROGRESS, entries[3])
    }

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
