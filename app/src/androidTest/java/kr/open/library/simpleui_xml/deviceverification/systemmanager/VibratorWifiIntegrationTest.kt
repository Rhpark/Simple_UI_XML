package kr.open.library.simpleui_xml.deviceverification.systemmanager

import android.Manifest
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kr.open.library.simple_ui.system_manager.core.extensions.getVibratorController
import kr.open.library.simple_ui.system_manager.core.extensions.getWifiController
import kr.open.library.simpleui_xml.deviceverification.harness.PhysicalDeviceRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class VibratorWifiIntegrationTest {
    @get:Rule
    val physicalDeviceRule = PhysicalDeviceRule()

    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun sysP0004_vibratorCanRunAndBeCancelled() {
        val controller = context.getVibratorController()
        physicalDeviceRule.cleanup.register("system_manager 테스트 진동 중지") {
            controller.cancel()
        }

        assertTrue(controller.hasVibrator())
        controller.vibrate(30L).requireSuccess()
        controller.cancel().requireSuccess()
    }

    @Test
    fun sysP0005_wifiStateMatchesPlatformManager() {
        val controller = context.getWifiController()

        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        if (hasLocationPermission) {
            assertEquals(controller.wifiManager.isWifiEnabled, controller.isWifiEnabled())
            assertEquals(controller.wifiManager.wifiState, controller.getWifiState())
        } else {
            assertEquals(false, controller.isWifiEnabled())
            assertEquals(WifiManager.WIFI_STATE_UNKNOWN, controller.getWifiState())
        }
        assertEquals(controller.isWifiEnabled(), controller.isWifiEnabled())
        controller.getConnectionInfo()
        controller.getModernNetworkDetails()
    }
}
