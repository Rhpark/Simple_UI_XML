package kr.open.library.simpleui_xml.system_service_manager.controller.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.controller.notification.vo.NotificationStyle
import kr.open.library.simple_ui.core.system_manager.controller.notification.vo.SimpleNotificationOptionVo
import kr.open.library.simple_ui.core.system_manager.controller.notification.vo.SimpleNotificationType
import kr.open.library.simple_ui.core.system_manager.controller.notification.vo.SimpleProgressNotificationOptionVo
import kr.open.library.simple_ui.core.system_manager.extensions.getNotificationController
import kr.open.library.simple_ui.xml.extensions.view.toastShowShort
import kr.open.library.simple_ui.xml.ui.activity.BaseBindingActivity
import kr.open.library.simpleui_xml.MainActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityNotificationControllerBinding

class NotificationControllerActivity :
    BaseBindingActivity<ActivityNotificationControllerBinding>(R.layout.activity_notification_controller) {
    private val vm: NotificationControllerActivityVm by viewModels()

    private val notificationController by lazy { getNotificationController(SimpleNotificationType.ACTIVITY) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.vm = vm
        lifecycle.addObserver(vm)
        createNotificationChannels()
        requestNotificationPermission()

        // ViewModel 이벤트 수집 시작 (필요 시 직접 호출)
        // BaseBindingActivity는 eventVmCollect()를 자동 호출하지 않습니다.
        eventVmCollect()
    }

    private fun createNotificationChannels() {
        val channel =
            NotificationChannel(
                "default_channel",
                "Default Notifications",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Default notification channel for examples"
            }

        notificationController.createChannel(channel)
    }

    private fun requestNotificationPermission() {
        checkSdkVersion(Build.VERSION_CODES.TIRAMISU) {
            onRequestPermissions(listOf(Manifest.permission.POST_NOTIFICATIONS)) { deniedPermissions ->
                if (deniedPermissions.isNotEmpty()) {
                    toastShowShort("Notification permission denied")
                }
            }
        }
    }

    override fun eventVmCollect() {
        lifecycleScope.launch {
            vm.mEventVm.collect { event ->
                when (event) {
                    is NotificationControllerActivityVmEvent.ShowNotification -> {
                        showBasicNotification()
                    }
                    is NotificationControllerActivityVmEvent.ShowBigTextNotification -> {
                        showBigTextNotification()
                    }
                    is NotificationControllerActivityVmEvent.ShowProgress -> {
                        showProgressNotification()
                    }
                    is NotificationControllerActivityVmEvent.UpdateProgress -> {
                        updateProgress()
                    }
                    is NotificationControllerActivityVmEvent.CompleteProgress -> {
                        completeProgress()
                    }
                    is NotificationControllerActivityVmEvent.CancelNotification -> {
                        cancelNotification()
                    }
                    is NotificationControllerActivityVmEvent.CancelAll -> {
                        cancelAllNotifications()
                    }
                }
            }
        }
    }

    private fun showBasicNotification() {
        Logx.d()
        val option =
            SimpleNotificationOptionVo(
                notificationId = 1,
                title = "Basic Notification",
                content = "This is a basic notification example",
                smallIcon = R.drawable.ic_launcher_foreground,
                isAutoCancel = true,
                clickIntent = Intent(this, MainActivity::class.java),
            )

        notificationController.showNotification(option)
        toastShowShort("Basic notification shown")
    }

    private fun showBigTextNotification() {
        val option =
            SimpleNotificationOptionVo(
                notificationId = 2,
                title = "BigText Notification",
                content = "Short summary",
                snippet =
                    "This is a very long text that will be displayed when the notification is expanded. " +
                        "You can include multiple lines of text here. " +
                        "The BigText style allows you to show much more content than a basic notification.",
                smallIcon = R.drawable.ic_launcher_foreground,
                isAutoCancel = true,
                style = NotificationStyle.BIG_TEXT,
            )

        notificationController.showNotification(option)
        toastShowShort("BigText notification shown")
    }

    private fun showProgressNotification() {
        val option =
            SimpleProgressNotificationOptionVo(
                notificationId = 3,
                title = "Download in Progress",
                content = "Downloading file...",
                smallIcon = R.drawable.ic_launcher_foreground,
                progressPercent = 0,
                onGoing = true,
            )

        notificationController.showProgressNotification(option)
        toastShowShort("Progress notification shown")
    }

    private fun updateProgress() {
        notificationController.updateProgress(3, 50)
        toastShowShort("Progress updated to 50%")
    }

    private fun completeProgress() {
        notificationController.completeProgress(3, "Download complete!")
        toastShowShort("Progress completed")
    }

    private fun cancelNotification() {
        notificationController.cancelNotification(tag = null, notificationId = 1)
        toastShowShort("Notification (ID: 1) cancelled")
    }

    private fun cancelAllNotifications() {
        notificationController.cancelAll()
        toastShowShort("All notifications cancelled")
    }
}
