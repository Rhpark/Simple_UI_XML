package kr.open.library.simpleui_xml.deviceverification.systemmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kr.open.library.simple_ui.system_manager.core.extensions.getNotificationController
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.deviceverification.harness.PhysicalDeviceRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class NotificationIntegrationTest {
    @get:Rule
    val physicalDeviceRule = PhysicalDeviceRule()

    @Test
    fun sysP0002_notificationCanBeShownAndCancelled() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                "Device verification",
                NotificationManager.IMPORTANCE_LOW,
            )
        val controller = context.getNotificationController(channel)
        physicalDeviceRule.cleanup.register("system_manager 테스트 알림 채널 삭제") {
            controller.cancelAll()
            controller.notificationManager.deleteNotificationChannel(CHANNEL_ID)
            controller.cleanup()
        }

        controller.createChannel(channel)
        val notification =
            Notification
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("SYS-P0-002")
                .setContentText("실기기 알림 자동 검증")
                .setAutoCancel(true)
                .build()

        controller.notify(NOTIFICATION_ID, notification).requireSuccess()
        controller.cancelNotification(notificationId = NOTIFICATION_ID).requireSuccess()
    }

    private companion object {
        const val CHANNEL_ID = "device_verification_notification"
        const val NOTIFICATION_ID = 91002
    }
}
