package kr.open.library.simple_ui.robolectric.system_manager.controller.wifi

import android.Manifest
import android.app.Application
import android.content.Context
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.system_manager.controller.wifi.WifiController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.net.InetAddress

/**
 * Robolectric tests for [WifiController].
 *
 * These tests focus on behaviour that depends on Android SDK versions and
 * framework service interactions. Real hardware specific scenarios are
 * intentionally excluded because they need instrumentation/device tests.
 */
@RunWith(RobolectricTestRunner::class)
class WifiControllerRobolectricTest {

    private lateinit var application: Application
    private lateinit var context: Context
    private lateinit var wifiManager: WifiManager
    private lateinit var connectivityManager: android.net.ConnectivityManager

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        // Grant required permissions so BaseSystemService passes checks.
        Shadows.shadowOf(application).grantPermissions(
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE
        )

        context = spy(application)
        wifiManager = mock(WifiManager::class.java)
        connectivityManager = mock(android.net.ConnectivityManager::class.java)

        doReturn(wifiManager).`when`(context).getSystemService(Context.WIFI_SERVICE)
        doReturn(connectivityManager).`when`(context).getSystemService(Context.CONNECTIVITY_SERVICE)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun setWifiEnabled_preQ_delegatesToWifiManager() {
        val controller = WifiController(context)
        controller.refreshPermissions()

        doReturn(true).`when`(wifiManager).setWifiEnabled(true)

        val result = controller.setWifiEnabled(true)

        assertTrue(result)
        verify(wifiManager).setWifiEnabled(true)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun setWifiEnabled_qAndAbove_returnsFalseWithoutCallingManager() {
        val controller = WifiController(context)
        controller.refreshPermissions()

        val result = controller.setWifiEnabled(true)

        assertFalse(result)
        verify(wifiManager, never()).setWifiEnabled(anyBoolean())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun startScan_preQ_returnsWifiManagerResult() {
        val controller = WifiController(context)
        controller.refreshPermissions()

        doReturn(true).`when`(wifiManager).startScan()

        val started = controller.startScan()

        assertTrue(started)
        verify(wifiManager).startScan()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun getScanResults_whenManagerReturnsNull_providesEmptyList() {
        val controller = WifiController(context)
        controller.refreshPermissions()

        doReturn(null).`when`(wifiManager).scanResults

        val results = controller.getScanResults()

        assertTrue(results.isEmpty())
        verify(wifiManager).scanResults
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getConnectionInfo_preS_returnsWifiManagerInfo() {
        val controller = WifiController(context)
        controller.refreshPermissions()

        val expectedInfo = mock(WifiInfo::class.java)
        doReturn(expectedInfo).`when`(wifiManager).connectionInfo

        val result = controller.getConnectionInfo()

        assertSame(expectedInfo, result)
        verify(wifiManager).connectionInfo
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun getModernNetworkDetails_onS_returnsDetailedInfoWhenConnectedToWifi() {
        val controller = WifiController(context)
        controller.refreshPermissions()

        val network = mock(Network::class.java)
        val networkCapabilities = mock(NetworkCapabilities::class.java)
        val linkProperties = mock(LinkProperties::class.java)
        val wifiInfo = mock(WifiInfo::class.java)

        doReturn(network).`when`(connectivityManager).activeNetwork
        doReturn(networkCapabilities).`when`(connectivityManager).getNetworkCapabilities(network)
        doReturn(true).`when`(networkCapabilities).hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        doReturn(wifiInfo).`when`(networkCapabilities).transportInfo

        doAnswer { invocation ->
            when (invocation.getArgument<Int>(0)) {
                NetworkCapabilities.NET_CAPABILITY_INTERNET -> true
                NetworkCapabilities.NET_CAPABILITY_VALIDATED -> true
                NetworkCapabilities.NET_CAPABILITY_NOT_METERED -> false
                else -> false
            }
        }.`when`(networkCapabilities).hasCapability(anyInt())

        doReturn(150).`when`(networkCapabilities).linkDownstreamBandwidthKbps
        doReturn(75).`when`(networkCapabilities).linkUpstreamBandwidthKbps

        val dns = listOf(InetAddress.getByName("8.8.8.8"))
        doReturn(linkProperties).`when`(connectivityManager).getLinkProperties(network)
        doReturn("wlan0").`when`(linkProperties).interfaceName
        doReturn(dns).`when`(linkProperties).dnsServers
        doReturn("example.com").`when`(linkProperties).domains

        val details = controller.getModernNetworkDetails()

        requireNotNull(details)
        assertTrue(details.isConnected)
        assertTrue(details.hasInternet)
        assertTrue(details.isValidated)
        assertTrue(details.isMetered)
        assertEquals(150, details.linkDownstreamBandwidthKbps)
        assertEquals(75, details.linkUpstreamBandwidthKbps)
        assertEquals("wlan0", details.interfaceName)
        assertEquals(listOf("8.8.8.8"), details.dnsServers)
        assertEquals("example.com", details.domains)

        // When using API 31+ path, WifiManager.connectionInfo should not be queried.
        verify(wifiManager, never()).connectionInfo
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun getModernNetworkDetails_whenNotWifiTransport_returnsNull() {
        val controller = WifiController(context)
        controller.refreshPermissions()

        val network = mock(Network::class.java)
        val networkCapabilities = mock(NetworkCapabilities::class.java)

        doReturn(network).`when`(connectivityManager).activeNetwork
        doReturn(networkCapabilities).`when`(connectivityManager).getNetworkCapabilities(network)
        doReturn(false).`when`(networkCapabilities).hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

        val details = controller.getModernNetworkDetails()

        assertNull(details)
        verify(connectivityManager, never()).getLinkProperties(network)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun getConfiguredNetworks_preQ_returnsDelegateList() {
        val controller = WifiController(context)
        controller.refreshPermissions()

        val configuration = mock(android.net.wifi.WifiConfiguration::class.java)
        doReturn(listOf(configuration)).`when`(wifiManager).configuredNetworks

        val configured = controller.getConfiguredNetworks()

        assertEquals(1, configured.size)
        assertSame(configuration, configured.first())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getConfiguredNetworks_qAndAbove_returnsEmptyList() {
        val controller = WifiController(context)
        controller.refreshPermissions()

        val configured = controller.getConfiguredNetworks()

        assertTrue(configured.isEmpty())
        verify(wifiManager, never()).configuredNetworks
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun isConnectedWifi_returnsFalseWhenNotWifiTransport() {
        val controller = WifiController(context)
        controller.refreshPermissions()

        val network = mock(Network::class.java)
        val networkCapabilities = mock(NetworkCapabilities::class.java)

        doReturn(network).`when`(connectivityManager).activeNetwork
        doReturn(networkCapabilities).`when`(connectivityManager).getNetworkCapabilities(network)
        doReturn(false).`when`(networkCapabilities).hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

        val connected = controller.isConnectedWifi()

        assertFalse(connected)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getCurrentSsid_returnsValueWithoutQuotes() {
        val controller = WifiController(context)
        controller.refreshPermissions()

        val wifiInfo = mock(WifiInfo::class.java)
        doReturn("\"MyWifi\"").`when`(wifiInfo).ssid
        doReturn(wifiInfo).`when`(wifiManager).connectionInfo

        val ssid = controller.getCurrentSsid()

        assertEquals("MyWifi", ssid)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun is5GHzBandSupported_delegatesToWifiManager() {
        val controller = WifiController(context)
        controller.refreshPermissions()

        doReturn(true).`when`(wifiManager).is5GHzBandSupported

        assertTrue(controller.is5GHzBandSupported())
        verify(wifiManager).is5GHzBandSupported
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun is6GHzBandSupported_preR_returnsFalse() {
        val controller = WifiController(context)
        controller.refreshPermissions()

        val supported = controller.is6GHzBandSupported()

        assertFalse(supported)
        verify(wifiManager, never()).is6GHzBandSupported
    }
}
