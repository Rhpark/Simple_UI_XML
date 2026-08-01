package kr.open.library.simpleui_xml.deviceverification.systemmanager

import android.Manifest
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kr.open.library.simple_ui.system_manager.core.info.network.connectivity.NetworkConnectivityInfo
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.deviceverification.harness.PhysicalDeviceRule
import kr.open.library.simpleui_xml.system_service_manager.controller.notification.NotificationControllerActivity
import kr.open.library.simpleui_xml.system_service_manager.controller.wifi.WifiControllerActivity
import kr.open.library.simpleui_xml.system_service_manager.info.ServiceManagerInfoActivity
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class SystemManagerSampleActivityIntegrationTest {
    @get:Rule
    val physicalDeviceRule = PhysicalDeviceRule()

    private val instrumentation
        get() = InstrumentationRegistry.getInstrumentation()

    private val context
        get() = instrumentation.targetContext

    @Test
    fun sysP0002_notificationSampleBindsViewModelAndHandlesActions() {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        ensureNotificationPermission()
        physicalDeviceRule.cleanup.register("알림 샘플 상태 복구") {
            notificationManager.cancelAll()
            notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID)
        }

        notificationManager.cancelAll()
        ActivityScenario.launch(NotificationControllerActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.btnShowNotification).performClick()
            }
            awaitCondition("알림 샘플이 알림 ID 1을 생성하지 않았습니다.") {
                notificationManager.activeNotifications.any { notification ->
                    notification.id == NOTIFICATION_ID
                }
            }

            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.btnCancelAll).performClick()
            }
            awaitCondition("알림 샘플의 전체 취소가 반영되지 않았습니다.") {
                notificationManager.activeNotifications.none { notification ->
                    notification.id == NOTIFICATION_ID
                }
            }
        }
    }

    @Test
    fun sysP0005_wifiSampleBindsViewModelAndUpdatesStatus() {
        ActivityScenario.launch(WifiControllerActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.btnCheckStatus).performClick()
            }
            awaitCondition("Wi-Fi 샘플 결과가 초기 문구에서 변경되지 않았습니다.") {
                var resultText = ""
                scenario.onActivity { activity ->
                    resultText = activity.findViewById<TextView>(R.id.tvResult).text.toString()
                }
                resultText.startsWith("WiFi Status:") && resultText.contains("Enabled:")
            }
        }
    }

    @Test
    fun sysP0008_networkSampleReceivesCallbackOnMainThread() {
        val networkInfo = NetworkConnectivityInfo(context)
        val isNetworkConnected = networkInfo.isNetworkConnected()
        physicalDeviceRule.requirePrecondition(
            scenarioId = "SYS-P0-008",
            expected = "networkConnected=true",
            actual = "networkConnected=$isNetworkConnected",
            isSatisfied = isNetworkConnected,
        )

        ActivityScenario.launch(ServiceManagerInfoActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.btnNetworkRegister).performClick()
            }
            awaitCondition("현재 기본 네트워크 콜백 결과가 목록에 추가되지 않았습니다.") {
                var itemCount = 0
                scenario.onActivity { activity ->
                    itemCount = activity.findViewById<RecyclerView>(R.id.rcvResult).adapter?.itemCount ?: 0
                }
                itemCount > REGISTER_ACTION_ITEM_COUNT
            }

            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.btnNetworkUnregister).performClick()
            }
        }
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        if (!isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)) {
            instrumentation.uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        }
    }

    private fun isPermissionGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private fun awaitCondition(message: String, condition: () -> Boolean) {
        repeat(MAX_RETRIES) {
            if (condition()) return
            Thread.sleep(RETRY_INTERVAL_MILLIS)
        }
        fail(message)
    }

    private companion object {
        const val NOTIFICATION_CHANNEL_ID = "default_channel"
        const val NOTIFICATION_ID = 1
        const val REGISTER_ACTION_ITEM_COUNT = 2
        const val MAX_RETRIES = 100
        const val RETRY_INTERVAL_MILLIS = 50L
    }
}
