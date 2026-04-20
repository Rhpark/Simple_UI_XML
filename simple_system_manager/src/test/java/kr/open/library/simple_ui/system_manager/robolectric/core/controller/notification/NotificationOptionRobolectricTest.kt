package kr.open.library.simple_ui.system_manager.robolectric.core.controller.notification

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.system_manager.core.controller.notification.option.DefaultNotificationOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

/**
 * Robolectric tests for NotificationOption PendingIntent flag validation.<br><br>
 * Tests the real constructor validation logic for Android 12+ (API 31+) FLAG_IMMUTABLE/FLAG_MUTABLE requirements.<br><br>
 * NotificationOption의 PendingIntent 플래그 검증에 대한 Robolectric 테스트입니다.<br>
 * Android 12+ (API 31+)에서 요구하는 FLAG_IMMUTABLE/FLAG_MUTABLE 규칙을 실제 생성자 호출로 검증합니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class NotificationOptionRobolectricTest {
    private val application: Application = ApplicationProvider.getApplicationContext()

    private fun createIntent(): Intent = Intent(application, Application::class.java)

    @Test
    fun pendingIntentFlags_missingFlags_throwsException() {
        withSdk(Build.VERSION_CODES.TIRAMISU) {
            try {
                DefaultNotificationOption(
                    notificationId = 1,
                    smallIcon = 101,
                    title = "Title",
                    content = "Content",
                    clickIntent = createIntent(),
                    pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT,
                )
                fail("DefaultNotificationOption should throw IllegalArgumentException")
            } catch (e: IllegalArgumentException) {
                assertTrue(e.message?.contains("FLAG_IMMUTABLE or FLAG_MUTABLE") == true)
            }
        }
    }

    @Test
    fun pendingIntentFlags_bothFlags_throwsException() {
        withSdk(Build.VERSION_CODES.TIRAMISU) {
            try {
                DefaultNotificationOption(
                    notificationId = 2,
                    smallIcon = 102,
                    title = "Title",
                    content = "Content",
                    clickIntent = createIntent(),
                    pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_MUTABLE,
                )
                fail("DefaultNotificationOption should throw IllegalArgumentException")
            } catch (e: IllegalArgumentException) {
                assertTrue(e.message?.contains("must not include both") == true)
            }
        }
    }

    @Test
    fun pendingIntentFlags_immutableOnly_allowsCreation() {
        val option =
            DefaultNotificationOption(
                notificationId = 3,
                smallIcon = 103,
                title = "Title",
                content = "Content",
                clickIntent = createIntent(),
                pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE,
            )

        assertEquals(PendingIntent.FLAG_IMMUTABLE, option.pendingIntentFlags)
    }

    @Test
    fun pendingIntentFlags_mutableOnly_allowsCreation() {
        val option =
            DefaultNotificationOption(
                notificationId = 4,
                smallIcon = 104,
                title = "Title",
                content = "Content",
                clickIntent = createIntent(),
                pendingIntentFlags = PendingIntent.FLAG_MUTABLE,
            )

        assertEquals(PendingIntent.FLAG_MUTABLE, option.pendingIntentFlags)
    }

    @Test
    fun pendingIntentFlags_withUpdateCurrentAndImmutable_allowsCreation() {
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val option =
            DefaultNotificationOption(
                notificationId = 5,
                smallIcon = 105,
                title = "Title",
                content = "Content",
                clickIntent = createIntent(),
                pendingIntentFlags = flags,
            )

        assertEquals(flags, option.pendingIntentFlags)
    }

    @Test
    fun defaultPendingIntentFlags_hasImmutableFlag() {
        val option =
            DefaultNotificationOption(
                notificationId = 6,
                smallIcon = 106,
                title = "Title",
                content = "Content",
            )

        val hasImmutable = option.pendingIntentFlags and PendingIntent.FLAG_IMMUTABLE != 0
        assertTrue("Default flags should include FLAG_IMMUTABLE", hasImmutable)
    }

    private fun <T> withSdk(
        version: Int,
        block: () -> T,
    ): T {
        val original = Build.VERSION.SDK_INT
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", version)
        return try {
            block()
        } finally {
            ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", original)
        }
    }
}
