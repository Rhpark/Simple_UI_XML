package kr.open.library.simple_ui.core.robolectric.system_manager.controller.wifi

import android.Manifest
import android.app.Application
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.system_manager.controller.wifi.WifiController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowWifiInfo
import org.robolectric.shadows.ShadowWifiManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R])
class WifiControllerRobolectricTest {

    private lateinit var application: Application
    private lateinit var wifiManager: WifiManager
    private lateinit var shadowWifiManager: ShadowWifiManager
    private lateinit var controller: WifiController

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
        shadowWifiManager = Shadows.shadowOf(wifiManager)

        val shadowApp = Shadows.shadowOf(application)
        shadowApp.grantPermissions(
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE
        )

        controller = WifiController(application)
        controller.refreshPermissions()
    }

    @Test
    fun isWifiEnabled_returnsShadowValue() {
        shadowWifiManager.setWifiState(WifiManager.WIFI_STATE_ENABLED)
        assertTrue(controller.isWifiEnabled())

        shadowWifiManager.setWifiState(WifiManager.WIFI_STATE_DISABLED)
        assertFalse(controller.isWifiEnabled())
    }

    @Test
    fun getWifiState_returnsShadowValue() {
        shadowWifiManager.setWifiState(WifiManager.WIFI_STATE_ENABLED)
        assertEquals(WifiManager.WIFI_STATE_ENABLED, controller.getWifiState())

        shadowWifiManager.setWifiState(WifiManager.WIFI_STATE_DISABLED)
        assertEquals(WifiManager.WIFI_STATE_DISABLED, controller.getWifiState())
    }

    @Test
    fun startScan_returnsTrue() {
        assertTrue(controller.startScan())
    }

    @Test
    fun getScanResults_returnsShadowValues() {
        val fakeScanResult = ScanResult().apply {
            SSID = "TestWiFi"
            BSSID = "00:11:22:33:44:55"
            level = -50
        }
        shadowWifiManager.setScanResults(listOf(fakeScanResult))

        val results = controller.getScanResults()
        assertEquals(1, results.size)
        assertEquals("TestWiFi", results[0].SSID)
    }

    // Removed getConnectionInfo test - testConnectionInfo_properties covers this functionality
    
    @Test
    fun testConnectionInfo_properties() {
        val wifiInfo = ShadowWifiInfo.newInstance()
        val shadowWifiInfo = Shadows.shadowOf(wifiInfo)
        
        shadowWifiInfo.setSSID("MyNetwork")
        shadowWifiInfo.setBSSID("00:11:22:33:44:55")
        shadowWifiInfo.setRssi(-55)
        shadowWifiInfo.setLinkSpeed(150)
        
        shadowWifiManager.setConnectionInfo(wifiInfo)
        
        assertEquals("MyNetwork", controller.getCurrentSsid())
        assertEquals("00:11:22:33:44:55", controller.getCurrentBssid())
        assertEquals(-55, controller.getCurrentRssi())
        assertEquals(150, controller.getCurrentLinkSpeed())
    }

    @Test
    fun calculateSignalLevel_delegatesToManager() {
        val level = controller.calculateSignalLevel(-60, 5)
        assertTrue(level >= 0)
    }

    @Test
    fun compareSignalLevel_delegatesToManager() {
        val result = controller.compareSignalLevel(-60, -80)
        assertTrue(result > 0)
    }
    
    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun setWifiEnabled_api28_returnsTrue() {
        assertTrue(controller.setWifiEnabled(true))
        assertTrue(wifiManager.isWifiEnabled)
        
        assertTrue(controller.setWifiEnabled(false))
        assertFalse(wifiManager.isWifiEnabled)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun setWifiEnabled_api29_returnsFalse() {
        assertFalse(controller.setWifiEnabled(true))
    }
}
