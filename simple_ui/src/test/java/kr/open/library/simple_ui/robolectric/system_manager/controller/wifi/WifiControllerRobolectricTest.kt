package kr.open.library.simple_ui.robolectric.system_manager.controller.wifi

import android.Manifest
import android.app.Application
import android.content.Context
import android.net.DhcpInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.system_manager.controller.wifi.WifiController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R])
class WifiControllerRobolectricTest {

    private lateinit var application: Application
    private lateinit var wifiManager: WifiManager
    private lateinit var connectivityManager: android.net.ConnectivityManager
    private lateinit var controller: WifiController

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        wifiManager = mock(WifiManager::class.java)
        connectivityManager = mock(android.net.ConnectivityManager::class.java)

        val shadowApp = Shadows.shadowOf(application)
        shadowApp.grantPermissions(
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE
        )
        shadowApp.setSystemService(Context.WIFI_SERVICE, wifiManager)
        shadowApp.setSystemService(Context.CONNECTIVITY_SERVICE, connectivityManager)

        controller = WifiController(application)
        controller.refreshPermissions()
    }

    @Test
    fun isWifiEnabled_returnsManagerValue() {
        doReturn(true).`when`(wifiManager).isWifiEnabled

        assertTrue(controller.isWifiEnabled())
    }

    @Test
    fun isWifiEnabled_whenManagerThrows_returnsFalse() {
        doThrow(RuntimeException("boom")).`when`(wifiManager).isWifiEnabled

        assertFalse(controller.isWifiEnabled())
    }

    @Test
    fun getWifiState_returnsManagerValue() {
        doReturn(WifiManager.WIFI_STATE_ENABLED).`when`(wifiManager).wifiState

        assertEquals(WifiManager.WIFI_STATE_ENABLED, controller.getWifiState())
    }

    @Test
    fun getWifiState_whenManagerThrows_returnsUnknown() {
        doThrow(RuntimeException("boom")).`when`(wifiManager).wifiState

        assertEquals(WifiManager.WIFI_STATE_UNKNOWN, controller.getWifiState())
    }

    @Test
    fun startScan_returnsManagerValue() {
        doReturn(true).`when`(wifiManager).startScan()

        assertTrue(controller.startScan())
    }

    @Test
    fun startScan_whenManagerThrows_returnsFalse() {
        doThrow(RuntimeException("boom")).`when`(wifiManager).startScan()

        assertFalse(controller.startScan())
    }

    @Test
    fun getDhcpInfo_returnsManagerValue() {
        val info = DhcpInfo()
        doReturn(info).`when`(wifiManager).dhcpInfo

        assertEquals(info, controller.getDhcpInfo())
    }

    @Test
    fun getDhcpInfo_whenManagerThrows_returnsNull() {
        doThrow(RuntimeException("boom")).`when`(wifiManager).dhcpInfo

        assertNull(controller.getDhcpInfo())
    }
}
