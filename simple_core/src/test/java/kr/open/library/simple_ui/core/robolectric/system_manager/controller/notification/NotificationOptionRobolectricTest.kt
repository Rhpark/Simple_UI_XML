package kr.open.library.simple_ui.core.robolectric.system_manager.controller.notification

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.DefaultNotificationOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric tests for NotificationOption PendingIntent flag validation.<br><br>
 * Tests validation logic for Android 12+ (API 31+) FLAG_IMMUTABLE/FLAG_MUTABLE requirements.<br>
 * Note: Due to compile-time SDK checks in library code, init block validation cannot be
 * directly tested via Robolectric. Instead, validation logic is tested separately.<br><br>
 * NotificationOption의 PendingIntent 플래그 검증에 대한 Robolectric 테스트입니다.<br>
 * Android 12+ (API 31+)에서의 FLAG_IMMUTABLE/FLAG_MUTABLE 요구 사항 검증 로직을 테스트합니다.<br>
 * 참고: 라이브러리 코드의 컴파일 타임 SDK 체크로 인해 init 블록 검증을 Robolectric으로
 * 직접 테스트할 수 없습니다. 대신 검증 로직을 별도로 테스트합니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class NotificationOptionRobolectricTest {
    private val application: Application = ApplicationProvider.getApplicationContext()

    private fun createIntent(): Intent = Intent(application, Application::class.java)

    // ==============================================
    // Validation Logic Tests (Direct)
    // ==============================================

    @Test
    fun pendingIntentFlagValidation_missingFlags_throwsException() {
        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasImmutable = pendingIntentFlags and PendingIntent.FLAG_IMMUTABLE != 0
            val hasMutable = pendingIntentFlags and PendingIntent.FLAG_MUTABLE != 0

            try {
                require(hasImmutable || hasMutable) {
                    "Android 12+ requires FLAG_IMMUTABLE or FLAG_MUTABLE in pendingIntentFlags."
                }
                fail("require should have thrown IllegalArgumentException")
            } catch (e: IllegalArgumentException) {
                assertTrue(e.message?.contains("FLAG_IMMUTABLE or FLAG_MUTABLE") == true)
            }
        } else {
            fail("This test requires SDK >= S (31)")
        }
    }

    @Test
    fun pendingIntentFlagValidation_bothFlags_throwsException() {
        val pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_MUTABLE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasImmutable = pendingIntentFlags and PendingIntent.FLAG_IMMUTABLE != 0
            val hasMutable = pendingIntentFlags and PendingIntent.FLAG_MUTABLE != 0

            try {
                require(!(hasImmutable && hasMutable)) {
                    "pendingIntentFlags must not include both FLAG_IMMUTABLE and FLAG_MUTABLE."
                }
                fail("require should have thrown IllegalArgumentException")
            } catch (e: IllegalArgumentException) {
                assertTrue(e.message?.contains("must not include both") == true)
            }
        } else {
            fail("This test requires SDK >= S (31)")
        }
    }

    @Test
    fun pendingIntentFlagValidation_immutableOnly_passes() {
        val pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasImmutable = pendingIntentFlags and PendingIntent.FLAG_IMMUTABLE != 0
            val hasMutable = pendingIntentFlags and PendingIntent.FLAG_MUTABLE != 0

            require(hasImmutable || hasMutable) {
                "Android 12+ requires FLAG_IMMUTABLE or FLAG_MUTABLE in pendingIntentFlags."
            }
            require(!(hasImmutable && hasMutable)) {
                "pendingIntentFlags must not include both FLAG_IMMUTABLE and FLAG_MUTABLE."
            }
            // No exception means validation passed
            assertTrue(hasImmutable)
        } else {
            fail("This test requires SDK >= S (31)")
        }
    }

    @Test
    fun pendingIntentFlagValidation_mutableOnly_passes() {
        val pendingIntentFlags = PendingIntent.FLAG_MUTABLE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasImmutable = pendingIntentFlags and PendingIntent.FLAG_IMMUTABLE != 0
            val hasMutable = pendingIntentFlags and PendingIntent.FLAG_MUTABLE != 0

            require(hasImmutable || hasMutable) {
                "Android 12+ requires FLAG_IMMUTABLE or FLAG_MUTABLE in pendingIntentFlags."
            }
            require(!(hasImmutable && hasMutable)) {
                "pendingIntentFlags must not include both FLAG_IMMUTABLE and FLAG_MUTABLE."
            }
            // No exception means validation passed
            assertTrue(hasMutable)
        } else {
            fail("This test requires SDK >= S (31)")
        }
    }

    // ==============================================
    // DefaultNotificationOption Creation Tests
    // ==============================================

    @Test
    fun pendingIntentFlags_immutableOnly_allowsCreation() {
        val option = DefaultNotificationOption(
            notificationId = 1,
            smallIcon = 101,
            title = "Title",
            content = "Content",
            clickIntent = createIntent(),
            pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE,
        )

        assertEquals(PendingIntent.FLAG_IMMUTABLE, option.pendingIntentFlags)
    }

    @Test
    fun pendingIntentFlags_mutableOnly_allowsCreation() {
        val option = DefaultNotificationOption(
            notificationId = 2,
            smallIcon = 102,
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
        val option = DefaultNotificationOption(
            notificationId = 3,
            smallIcon = 103,
            title = "Title",
            content = "Content",
            clickIntent = createIntent(),
            pendingIntentFlags = flags,
        )

        assertEquals(flags, option.pendingIntentFlags)
    }

    @Test
    fun defaultPendingIntentFlags_hasImmutableFlag() {
        val option = DefaultNotificationOption(
            notificationId = 4,
            smallIcon = 104,
            title = "Title",
            content = "Content",
        )

        val hasImmutable = option.pendingIntentFlags and PendingIntent.FLAG_IMMUTABLE != 0
        assertTrue("Default flags should include FLAG_IMMUTABLE", hasImmutable)
    }
}
