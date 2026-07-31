package kr.open.library.simpleui_xml.deviceverification.systemmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kr.open.library.simple_ui.system_manager.core.extensions.getAlarmController
import kr.open.library.simple_ui.system_manager.core.extensions.getBatteryManager
import kr.open.library.simple_ui.system_manager.core.extensions.getBatteryStateInfo
import kr.open.library.simple_ui.system_manager.core.extensions.getConnectivityManager
import kr.open.library.simple_ui.system_manager.core.extensions.getLocationManager
import kr.open.library.simple_ui.system_manager.core.extensions.getLocationStateInfo
import kr.open.library.simple_ui.system_manager.core.extensions.getNotificationController
import kr.open.library.simple_ui.system_manager.core.extensions.getVibratorController
import kr.open.library.simple_ui.system_manager.core.extensions.getWifiController
import kr.open.library.simpleui_xml.deviceverification.harness.PhysicalDeviceRule
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class SystemServiceBaseIntegrationTest {
    @get:Rule
    val physicalDeviceRule = PhysicalDeviceRule()

    @Test
    fun sysP0001_supportedServicesAndControllersAreAvailable() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val channel =
            NotificationChannel(
                "device_verification_service_probe",
                "Device verification service probe",
                NotificationManager.IMPORTANCE_LOW,
            )

        assertNotNull(context.getBatteryManager())
        assertNotNull(context.getConnectivityManager())
        assertNotNull(context.getLocationManager())
        assertNotNull(context.getAlarmController())
        assertNotNull(context.getNotificationController(channel))
        assertNotNull(context.getVibratorController())
        assertNotNull(context.getWifiController())
        assertNotNull(context.getBatteryStateInfo())
        assertNotNull(context.getLocationStateInfo())
    }
}
