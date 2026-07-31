package kr.open.library.simple_ui.system_manager.robolectric.core.controller.notification

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import kr.open.library.simple_ui.system_manager.core.controller.notification.option.BigPictureNotificationOption
import kr.open.library.simple_ui.system_manager.core.controller.notification.option.BigTextNotificationOption
import kr.open.library.simple_ui.system_manager.core.controller.notification.option.DefaultNotificationOption
import kr.open.library.simple_ui.system_manager.core.controller.notification.option.ProgressNotificationOption
import kr.open.library.simple_ui.system_manager.core.controller.notification.option.SimpleNotificationOptionBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric tests for NotificationOption PendingIntent flag validation.<br><br>
 * Tests the constructor validation logic for Android 12+ (API 31+) FLAG_IMMUTABLE/FLAG_MUTABLE requirements.<br><br>
 * NotificationOption의 PendingIntent 플래그 검증에 대한 Robolectric 테스트입니다.<br>
 * Android 12+ (API 31+)에서 요구하는 FLAG_IMMUTABLE/FLAG_MUTABLE 규칙을 생성자 호출로 검증합니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
class NotificationOptionRobolectricTest {
    private fun optionFactories(
        pendingIntentFlags: Int,
    ): List<Pair<String, () -> SimpleNotificationOptionBase>> =
        listOf(
            "DefaultNotificationOption" to {
                DefaultNotificationOption(
                    notificationId = 1,
                    smallIcon = 101,
                    title = "Title",
                    content = "Content",
                    clickIntent = Intent(),
                    pendingIntentFlags = pendingIntentFlags,
                )
            },
            "BigTextNotificationOption" to {
                BigTextNotificationOption(
                    notificationId = 2,
                    smallIcon = 102,
                    title = "Title",
                    content = "Content",
                    clickIntent = Intent(),
                    pendingIntentFlags = pendingIntentFlags,
                    snippet = "Snippet",
                )
            },
            "BigPictureNotificationOption" to {
                BigPictureNotificationOption(
                    notificationId = 3,
                    smallIcon = 103,
                    title = "Title",
                    content = "Content",
                    clickIntent = Intent(),
                    pendingIntentFlags = pendingIntentFlags,
                    bigPicture = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),
                )
            },
            "ProgressNotificationOption" to {
                ProgressNotificationOption(
                    notificationId = 4,
                    smallIcon = 104,
                    title = "Title",
                    content = "Content",
                    clickIntent = Intent(),
                    pendingIntentFlags = pendingIntentFlags,
                    progressPercent = 50,
                )
            },
        )

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun pendingIntentFlags_missingFlags_throwsException() {
        optionFactories(PendingIntent.FLAG_UPDATE_CURRENT).forEach { (optionName, createOption) ->
            val exception = assertThrows(
                "$optionName should throw IllegalArgumentException",
                IllegalArgumentException::class.java,
            ) {
                createOption()
            }
            assertTrue(exception.message?.contains("FLAG_IMMUTABLE or FLAG_MUTABLE") == true)
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun pendingIntentFlags_bothFlags_throwsException() {
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_MUTABLE
        optionFactories(flags).forEach { (optionName, createOption) ->
            val exception = assertThrows(
                "$optionName should throw IllegalArgumentException",
                IllegalArgumentException::class.java,
            ) {
                createOption()
            }
            assertTrue(exception.message?.contains("must not include both") == true)
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun pendingIntentFlags_immutableOnly_allowsCreation() {
        val option = DefaultNotificationOption(
            notificationId = 3,
            smallIcon = 103,
            title = "Title",
            content = "Content",
            clickIntent = Intent(),
            pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE,
        )
        assertEquals(PendingIntent.FLAG_IMMUTABLE, option.pendingIntentFlags)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun pendingIntentFlags_mutableOnly_allowsCreation() {
        val option = DefaultNotificationOption(
            notificationId = 4,
            smallIcon = 104,
            title = "Title",
            content = "Content",
            clickIntent = Intent(),
            pendingIntentFlags = PendingIntent.FLAG_MUTABLE,
        )
        assertEquals(PendingIntent.FLAG_MUTABLE, option.pendingIntentFlags)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun pendingIntentFlags_withUpdateCurrentAndImmutable_allowsCreation() {
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val option = DefaultNotificationOption(
            notificationId = 5,
            smallIcon = 105,
            title = "Title",
            content = "Content",
            clickIntent = Intent(),
            pendingIntentFlags = flags,
        )
        assertEquals(flags, option.pendingIntentFlags)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun pendingIntentFlags_belowS_noValidation() {
        val option = DefaultNotificationOption(
            notificationId = 6,
            smallIcon = 106,
            title = "Title",
            content = "Content",
            clickIntent = Intent(),
            pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT,
        )
        assertEquals(PendingIntent.FLAG_UPDATE_CURRENT, option.pendingIntentFlags)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun pendingIntentFlags_withoutClickIntent_noValidation() {
        val option = DefaultNotificationOption(
            notificationId = 7,
            smallIcon = 107,
            title = "Title",
            content = "Content",
            clickIntent = null,
            pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT,
        )
        assertEquals(PendingIntent.FLAG_UPDATE_CURRENT, option.pendingIntentFlags)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun copy_withMissingFlags_throwsException() {
        val option = DefaultNotificationOption(
            notificationId = 8,
            smallIcon = 108,
            title = "Title",
            content = "Content",
            clickIntent = Intent(),
            pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE,
        )

        assertThrows(IllegalArgumentException::class.java) {
            option.copy(pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun defaultPendingIntentFlags_hasImmutableFlag() {
        val option = DefaultNotificationOption(
            notificationId = 9,
            smallIcon = 109,
            title = "Title",
            content = "Content",
        )
        val hasImmutable = option.pendingIntentFlags and PendingIntent.FLAG_IMMUTABLE != 0
        assertTrue("Default flags should include FLAG_IMMUTABLE", hasImmutable)
    }
}
