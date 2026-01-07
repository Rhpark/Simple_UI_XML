package kr.open.library.simple_ui.core.unit.system_manager.controller.notification.vo

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.BigPictureNotificationOption
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.BigTextNotificationOption
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.DefaultNotificationOption
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.ProgressNotificationOption
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.SimplePendingIntentOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class NotificationVoTest {
    @Test
    fun defaultNotificationOption_storesValues() {
        val option =
            DefaultNotificationOption(
                notificationId = 1,
                smallIcon = 101,
                title = "Title",
                content = "Content",
                isAutoCancel = true,
                onGoing = true,
                clickIntent = null,
                actions = emptyList(),
            )

        assertEquals(1, option.notificationId)
        assertEquals(101, option.smallIcon)
        assertEquals("Title", option.title)
        assertEquals("Content", option.content)
        assertEquals(true, option.isAutoCancel)
        assertEquals(true, option.onGoing)
        assertNull(option.clickIntent)
        assertTrue(option.actions!!.isEmpty())
    }

    @Test
    fun bigTextNotificationOption_storesValues() {
        val option =
            BigTextNotificationOption(
                notificationId = 2,
                smallIcon = 201,
                title = "Big Text",
                content = "Short",
                isAutoCancel = false,
                onGoing = false,
                clickIntent = null,
                actions = null,
                snippet = "Long description",
            )

        assertEquals(2, option.notificationId)
        assertEquals(201, option.smallIcon)
        assertEquals("Big Text", option.title)
        assertEquals("Short", option.content)
        assertEquals(false, option.isAutoCancel)
        assertEquals(false, option.onGoing)
        assertNull(option.clickIntent)
        assertNull(option.actions)
        assertEquals("Long description", option.snippet)
    }

    @Test
    fun bigPictureNotificationOption_storesValues() {
        val bitmap = mock(Bitmap::class.java)
        val option =
            BigPictureNotificationOption(
                notificationId = 3,
                smallIcon = 301,
                title = "Big Picture",
                content = null,
                isAutoCancel = true,
                onGoing = false,
                clickIntent = null,
                actions = null,
                bigPicture = bitmap,
            )

        assertEquals(3, option.notificationId)
        assertEquals(301, option.smallIcon)
        assertEquals("Big Picture", option.title)
        assertNull(option.content)
        assertEquals(true, option.isAutoCancel)
        assertEquals(false, option.onGoing)
        assertNull(option.clickIntent)
        assertNull(option.actions)
        assertEquals(bitmap, option.bigPicture)
    }

    @Test
    fun progressNotificationOption_storesValues() {
        val option =
            ProgressNotificationOption(
                notificationId = 4,
                smallIcon = 401,
                title = "Download",
                content = "50%",
                isAutoCancel = false,
                onGoing = true,
                clickIntent = null,
                actions = null,
                progressPercent = 75,
            )

        assertEquals(4, option.notificationId)
        assertEquals(401, option.smallIcon)
        assertEquals("Download", option.title)
        assertEquals("50%", option.content)
        assertEquals(false, option.isAutoCancel)
        assertEquals(true, option.onGoing)
        assertNull(option.clickIntent)
        assertNull(option.actions)
        assertEquals(75, option.progressPercent)
    }

    @Test(expected = IllegalArgumentException::class)
    fun progressNotificationOption_invalidProgress_throwsException() {
        ProgressNotificationOption(
            notificationId = 5,
            smallIcon = 501,
            title = "Invalid",
            content = null,
            isAutoCancel = true,
            onGoing = false,
            clickIntent = null,
            actions = null,
            progressPercent = 200,
        )
    }

    @Test
    fun simplePendingIntentOptionVo_defaultFlags() {
        val intent = mock(Intent::class.java)
        val option = SimplePendingIntentOption(actionId = 10, clickIntent = intent)

        val expectedFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        assertEquals(10, option.actionId)
        assertEquals(intent, option.clickIntent)
        assertEquals(expectedFlags, option.flags)
    }

    @Test
    fun simplePendingIntentOptionVo_customFlags() {
        val intent = mock(Intent::class.java)
        val option = SimplePendingIntentOption(actionId = 11, clickIntent = intent, flags = 0)

        assertEquals(11, option.actionId)
        assertEquals(intent, option.clickIntent)
        assertEquals(0, option.flags)
    }
}
