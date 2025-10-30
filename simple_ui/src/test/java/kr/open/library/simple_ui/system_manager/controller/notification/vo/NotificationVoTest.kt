package kr.open.library.simple_ui.system_manager.controller.notification.vo

import android.app.PendingIntent
import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class NotificationVoTest {

    @Test
    fun notificationStyle_hasAllEntries() {
        val styles = listOf(
            NotificationStyle.DEFAULT,
            NotificationStyle.BIG_PICTURE,
            NotificationStyle.BIG_TEXT,
            NotificationStyle.PROGRESS,
        )

        assertEquals(4, styles.toSet().size)
    }

    @Test
    fun simpleNotificationType_hasAllEntries() {
        val types = listOf(
            SimpleNotificationType.ACTIVITY,
            SimpleNotificationType.SERVICE,
            SimpleNotificationType.BROADCAST,
        )

        assertEquals(3, types.toSet().size)
    }

    @Test
    fun simpleNotificationOptionVo_defaultsAreApplied() {
        val option = SimpleNotificationOptionVo(notificationId = 1)

        assertEquals(1, option.notificationId)
        assertNull(option.title)
        assertNull(option.content)
        assertEquals(false, option.isAutoCancel)
        assertNull(option.smallIcon)
        assertNull(option.largeIcon)
        assertNull(option.clickIntent)
        assertNull(option.snippet)
        assertNull(option.actions)
        assertEquals(false, option.onGoing)
        assertEquals(NotificationStyle.DEFAULT, option.style)
    }

    @Test
    fun simpleNotificationOptionVo_allFieldsStored() {
        val option = SimpleNotificationOptionVo(
            notificationId = 2,
            title = "Title",
            content = "Content",
            isAutoCancel = true,
            smallIcon = 101,
            largeIcon = null,
            clickIntent = null,
            snippet = "Snippet",
            actions = emptyList(),
            onGoing = true,
            style = NotificationStyle.BIG_TEXT,
        )

        assertEquals(2, option.notificationId)
        assertEquals("Title", option.title)
        assertEquals("Content", option.content)
        assertEquals(true, option.isAutoCancel)
        assertEquals(101, option.smallIcon)
        assertNull(option.largeIcon)
        assertNull(option.clickIntent)
        assertEquals("Snippet", option.snippet)
        assertTrue(option.actions!!.isEmpty())
        assertEquals(true, option.onGoing)
        assertEquals(NotificationStyle.BIG_TEXT, option.style)
    }

    @Test
    fun simpleProgressNotificationOptionVo_defaultsAreApplied() {
        val option = SimpleProgressNotificationOptionVo(
            notificationId = 3,
            progressPercent = 50,
        )

        assertEquals(3, option.notificationId)
        assertNull(option.title)
        assertNull(option.content)
        assertEquals(false, option.isAutoCancel)
        assertNull(option.smallIcon)
        assertNull(option.clickIntent)
        assertNull(option.actions)
        assertEquals(50, option.progressPercent)
        assertEquals(false, option.onGoing)
        assertEquals(NotificationStyle.PROGRESS, option.style)
    }

    @Test
    fun simpleProgressNotificationOptionVo_storesValues() {
        val option = SimpleProgressNotificationOptionVo(
            notificationId = 4,
            title = "Download",
            content = "50%",
            isAutoCancel = true,
            smallIcon = 202,
            clickIntent = null,
            actions = null,
            progressPercent = 75,
            onGoing = true,
            style = NotificationStyle.BIG_PICTURE,
        )

        assertEquals(4, option.notificationId)
        assertEquals("Download", option.title)
        assertEquals("50%", option.content)
        assertEquals(true, option.isAutoCancel)
        assertEquals(202, option.smallIcon)
        assertNull(option.clickIntent)
        assertNull(option.actions)
        assertEquals(75, option.progressPercent)
        assertEquals(true, option.onGoing)
        assertEquals(NotificationStyle.BIG_PICTURE, option.style)
    }

    @Test
    fun simplePendingIntentOptionVo_defaultFlags() {
        val intent = mock(Intent::class.java)
        val option = SimplePendingIntentOptionVo(actionId = 10, clickIntent = intent)

        val expectedFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        assertEquals(10, option.actionId)
        assertEquals(intent, option.clickIntent)
        assertEquals(expectedFlags, option.flags)
    }

    @Test
    fun simplePendingIntentOptionVo_customFlags() {
        val intent = mock(Intent::class.java)
        val option = SimplePendingIntentOptionVo(actionId = 11, clickIntent = intent, flags = 0)

        assertEquals(11, option.actionId)
        assertEquals(intent, option.clickIntent)
        assertEquals(0, option.flags)
    }
}
