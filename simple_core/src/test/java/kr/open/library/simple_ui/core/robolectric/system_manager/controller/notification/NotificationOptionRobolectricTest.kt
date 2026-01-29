package kr.open.library.simple_ui.core.robolectric.system_manager.controller.notification

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.DefaultNotificationOption
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class NotificationOptionRobolectricTest {
    private val application: Application = ApplicationProvider.getApplicationContext()

    private fun createIntent(): Intent = Intent(application, Application::class.java)

    @Test(expected = IllegalArgumentException::class)
    fun pendingIntentFlags_missingImmutableOrMutable_throwsException() {
        DefaultNotificationOption(
            notificationId = 1,
            smallIcon = 101,
            title = "Title",
            content = "Content",
            clickIntent = createIntent(),
            pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun pendingIntentFlags_containsBothImmutableAndMutable_throwsException() {
        DefaultNotificationOption(
            notificationId = 2,
            smallIcon = 102,
            title = "Title",
            content = "Content",
            clickIntent = createIntent(),
            pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_MUTABLE,
        )
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
}
