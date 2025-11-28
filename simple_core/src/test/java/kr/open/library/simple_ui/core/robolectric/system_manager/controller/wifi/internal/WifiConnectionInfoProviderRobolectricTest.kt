package kr.open.library.simple_ui.core.robolectric.system_manager.controller.wifi.internal

import WifiNetworkDetails
import android.content.Context
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.system_manager.controller.wifi.internal.WifiConnectionInfoProvider
import kr.open.library.simple_ui.core.system_manager.controller.wifi.internal.WifiOperationGuard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowWifiInfo

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R])
class WifiConnectionInfoProviderRobolectricTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val guard = WifiOperationGuard { defaultValue, block ->
        try {
            block()
        } catch (e: Exception) {
            defaultValue
        }
    }

    private fun createWifiInfo(ssid: String): WifiInfo {
        val info = ShadowWifiInfo.newInstance()
        val shadow = org.robolectric.shadow.api.Shadow.extract<ShadowWifiInfo>(info)
        shadow.setSSID(ssid)
        shadow.setBSSID("00:11:22:33:44:55")
        shadow.setRssi(-45)
        shadow.setLinkSpeed(540)
        return info
    }

    @Test
    fun getConnectionInfo_preS_whenNetworkInfoMissing_fallsBackToWifiManager() {
        val wifiManager = mock(WifiManager::class.java)
        val connectivityManager = mock(android.net.ConnectivityManager::class.java)
        val provider = WifiConnectionInfoProvider(wifiManager, connectivityManager, guard)

        doReturn(null).`when`(connectivityManager).activeNetwork
        assertNull(provider.getConnectionInfo())

        val fallbackInfo = createWifiInfo("\"Fallback\"")
        doReturn(fallbackInfo).`when`(wifiManager).connectionInfo

        assertSame(fallbackInfo, provider.getConnectionInfo())
    }

    @Test
    fun getConnectionInfoFromNetworkCapabilities_preS_returnsWifiManagerInfo() {
        val wifiManager = mock(WifiManager::class.java)
        val connectivityManager = mock(android.net.ConnectivityManager::class.java)
        val provider = WifiConnectionInfoProvider(wifiManager, connectivityManager, guard)

        val network = mock(Network::class.java)
        val caps = mock(NetworkCapabilities::class.java)

        doReturn(network).`when`(connectivityManager).activeNetwork
        doReturn(caps).`when`(connectivityManager).getNetworkCapabilities(network)
        doReturn(true).`when`(caps).hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

        val unknownInfo = mock(WifiInfo::class.java)
        val validInfo = mock(WifiInfo::class.java)
        doReturn(WifiManager.UNKNOWN_SSID).`when`(unknownInfo).ssid
        doReturn("OfficeWifi").`when`(validInfo).ssid

        doReturn(unknownInfo, validInfo).`when`(wifiManager).connectionInfo

        assertNull(provider.getConnectionInfoFromNetworkCapabilities())
        assertSame(validInfo, provider.getConnectionInfoFromNetworkCapabilities())
    }

    @Test
    fun getConnectionInfoFromNetworkCapabilities_whenWifiManagerReturnsNull_returnsNull() {
        val wifiManager = mock(WifiManager::class.java)
        val connectivityManager = mock(android.net.ConnectivityManager::class.java)
        val provider = WifiConnectionInfoProvider(wifiManager, connectivityManager, guard)

        val network = mock(Network::class.java)
        val caps = mock(NetworkCapabilities::class.java)

        doReturn(network).`when`(connectivityManager).activeNetwork
        doReturn(caps).`when`(connectivityManager).getNetworkCapabilities(network)
        doReturn(true).`when`(caps).hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        doReturn(null).`when`(wifiManager).connectionInfo

        assertNull(provider.getConnectionInfoFromNetworkCapabilities())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun getConnectionInfo_onS_prefersTransportInfo() {
        val wifiManager = mock(WifiManager::class.java)
        val connectivityManager = mock(android.net.ConnectivityManager::class.java)
        val provider = WifiConnectionInfoProvider(wifiManager, connectivityManager, guard)

        val network = mock(Network::class.java)
        val caps = mock(NetworkCapabilities::class.java)
        val wifiInfo = mock(WifiInfo::class.java)

        doReturn(network).`when`(connectivityManager).activeNetwork
        doReturn(caps).`when`(connectivityManager).getNetworkCapabilities(network)
        doReturn(true).`when`(caps).hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        doReturn(wifiInfo).`when`(caps).transportInfo
        doReturn("\"OfficeWifi\"").`when`(wifiInfo).ssid

        val info = provider.getConnectionInfo()

        assertSame(wifiInfo, info)
        verify(wifiManager, never()).connectionInfo
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun getConnectionInfo_onS_whenTransportInfoMissing_returnsNull() {
        val wifiManager = mock(WifiManager::class.java)
        val connectivityManager = mock(android.net.ConnectivityManager::class.java)
        val provider = WifiConnectionInfoProvider(wifiManager, connectivityManager, guard)

        val network = mock(Network::class.java)
        val caps = mock(NetworkCapabilities::class.java)

        doReturn(network).`when`(connectivityManager).activeNetwork
        doReturn(caps).`when`(connectivityManager).getNetworkCapabilities(network)
        doReturn(true).`when`(caps).hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        doReturn(null).`when`(caps).transportInfo

        assertNull(provider.getConnectionInfo())
        verify(wifiManager, never()).connectionInfo
    }

    @Test
    fun getModernNetworkDetails_handlesDnsVariants() {
        val wifiManager = mock(WifiManager::class.java)
        val connectivityManager = mock(android.net.ConnectivityManager::class.java)
        val provider = WifiConnectionInfoProvider(wifiManager, connectivityManager, guard)

        val network = mock(Network::class.java)
        val caps = mock(NetworkCapabilities::class.java)
        val linkProperties = mock(LinkProperties::class.java)

        doReturn(network).`when`(connectivityManager).activeNetwork
        doReturn(caps).`when`(connectivityManager).getNetworkCapabilities(network)
        doReturn(true).`when`(caps).hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        doReturn(100).`when`(caps).linkDownstreamBandwidthKbps
        doReturn(50).`when`(caps).linkUpstreamBandwidthKbps
        doReturn(true).`when`(caps).hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        doReturn(true).`when`(caps).hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        doReturn(false).`when`(caps).hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)

        doReturn(linkProperties).`when`(connectivityManager).getLinkProperties(network)
        doReturn("wlan0").`when`(linkProperties).interfaceName
        doReturn(null).`when`(linkProperties).dnsServers
        doReturn(null).`when`(linkProperties).domains

        val withoutDns: WifiNetworkDetails? = provider.getModernNetworkDetails()
        assertNull(withoutDns?.domains)
        assertEquals(emptyList<String>(), withoutDns?.dnsServers)

        val dnsList = listOf(
            java.net.InetAddress.getByName("8.8.8.8"),
            java.net.InetAddress.getByName("1.1.1.1")
        )
        doReturn(dnsList).`when`(linkProperties).dnsServers
        doReturn("example.com").`when`(linkProperties).domains

        val withDns = provider.getModernNetworkDetails()
        assertEquals(listOf("8.8.8.8", "1.1.1.1"), withDns?.dnsServers)
        assertEquals("example.com", withDns?.domains)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun getConnectionInfo_preQ_returnsWifiManagerConnectionInfoDirectly() {
        val wifiManager = mock(WifiManager::class.java)
        val connectivityManager = mock(android.net.ConnectivityManager::class.java)
        val provider = WifiConnectionInfoProvider(wifiManager, connectivityManager, guard)

        val wifiInfo = createWifiInfo("\"TestSSID\"")
        doReturn(wifiInfo).`when`(wifiManager).connectionInfo

        val result = provider.getConnectionInfo()

        assertSame(wifiInfo, result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getConnectionInfo_onQ_whenNetworkCapabilitiesReturnsNull_fallsBackToWifiManagerWithUnknownSsid() {
        val wifiManager = mock(WifiManager::class.java)
        val connectivityManager = mock(android.net.ConnectivityManager::class.java)
        val provider = WifiConnectionInfoProvider(wifiManager, connectivityManager, guard)

        doReturn(null).`when`(connectivityManager).activeNetwork

        val unknownSsidInfo = mock(WifiInfo::class.java)
        doReturn(WifiManager.UNKNOWN_SSID).`when`(unknownSsidInfo).ssid
        doReturn(unknownSsidInfo).`when`(wifiManager).connectionInfo

        assertNull(provider.getConnectionInfo())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getConnectionInfo_onR_whenNetworkCapabilitiesReturnsNull_fallsBackToWifiManagerWithNull() {
        val wifiManager = mock(WifiManager::class.java)
        val connectivityManager = mock(android.net.ConnectivityManager::class.java)
        val provider = WifiConnectionInfoProvider(wifiManager, connectivityManager, guard)

        doReturn(null).`when`(connectivityManager).activeNetwork
        doReturn(null).`when`(wifiManager).connectionInfo

        assertNull(provider.getConnectionInfo())
    }

    @Test
    fun getConnectionInfoFromNetworkCapabilities_whenActiveNetworkIsNull_returnsNull() {
        val wifiManager = mock(WifiManager::class.java)
        val connectivityManager = mock(android.net.ConnectivityManager::class.java)
        val provider = WifiConnectionInfoProvider(wifiManager, connectivityManager, guard)

        doReturn(null).`when`(connectivityManager).activeNetwork

        assertNull(provider.getConnectionInfoFromNetworkCapabilities())
    }

    @Test
    fun getConnectionInfoFromNetworkCapabilities_whenNetworkCapabilitiesIsNull_returnsNull() {
        val wifiManager = mock(WifiManager::class.java)
        val connectivityManager = mock(android.net.ConnectivityManager::class.java)
        val provider = WifiConnectionInfoProvider(wifiManager, connectivityManager, guard)

        val network = mock(Network::class.java)
        doReturn(network).`when`(connectivityManager).activeNetwork
        doReturn(null).`when`(connectivityManager).getNetworkCapabilities(network)

        assertNull(provider.getConnectionInfoFromNetworkCapabilities())
    }

    @Test
    fun getConnectionInfoFromNetworkCapabilities_whenNotWifiTransport_returnsNull() {
        val wifiManager = mock(WifiManager::class.java)
        val connectivityManager = mock(android.net.ConnectivityManager::class.java)
        val provider = WifiConnectionInfoProvider(wifiManager, connectivityManager, guard)

        val network = mock(Network::class.java)
        val caps = mock(NetworkCapabilities::class.java)

        doReturn(network).`when`(connectivityManager).activeNetwork
        doReturn(caps).`when`(connectivityManager).getNetworkCapabilities(network)
        doReturn(false).`when`(caps).hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

        assertNull(provider.getConnectionInfoFromNetworkCapabilities())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun getConnectionInfoFromNetworkCapabilities_onS_whenTransportInfoHasUnknownSsid_returnsNull() {
        val wifiManager = mock(WifiManager::class.java)
        val connectivityManager = mock(android.net.ConnectivityManager::class.java)
        val provider = WifiConnectionInfoProvider(wifiManager, connectivityManager, guard)

        val network = mock(Network::class.java)
        val caps = mock(NetworkCapabilities::class.java)
        val wifiInfo = mock(WifiInfo::class.java)

        doReturn(network).`when`(connectivityManager).activeNetwork
        doReturn(caps).`when`(connectivityManager).getNetworkCapabilities(network)
        doReturn(true).`when`(caps).hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        doReturn(wifiInfo).`when`(caps).transportInfo
        doReturn(WifiManager.UNKNOWN_SSID).`when`(wifiInfo).ssid

        assertNull(provider.getConnectionInfoFromNetworkCapabilities())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun getModernNetworkDetails_preQ_returnsNull() {
        val wifiManager = mock(WifiManager::class.java)
        val connectivityManager = mock(android.net.ConnectivityManager::class.java)
        val provider = WifiConnectionInfoProvider(wifiManager, connectivityManager, guard)

        assertNull(provider.getModernNetworkDetails())
    }

    @Test
    fun getModernNetworkDetails_whenActiveNetworkIsNull_returnsNull() {
        val wifiManager = mock(WifiManager::class.java)
        val connectivityManager = mock(android.net.ConnectivityManager::class.java)
        val provider = WifiConnectionInfoProvider(wifiManager, connectivityManager, guard)

        doReturn(null).`when`(connectivityManager).activeNetwork

        assertNull(provider.getModernNetworkDetails())
    }

    @Test
    fun getModernNetworkDetails_whenNetworkCapabilitiesIsNull_returnsNull() {
        val wifiManager = mock(WifiManager::class.java)
        val connectivityManager = mock(android.net.ConnectivityManager::class.java)
        val provider = WifiConnectionInfoProvider(wifiManager, connectivityManager, guard)

        val network = mock(Network::class.java)
        doReturn(network).`when`(connectivityManager).activeNetwork
        doReturn(null).`when`(connectivityManager).getNetworkCapabilities(network)

        assertNull(provider.getModernNetworkDetails())
    }

    @Test
    fun getModernNetworkDetails_whenNotWifiTransport_returnsNull() {
        val wifiManager = mock(WifiManager::class.java)
        val connectivityManager = mock(android.net.ConnectivityManager::class.java)
        val provider = WifiConnectionInfoProvider(wifiManager, connectivityManager, guard)

        val network = mock(Network::class.java)
        val caps = mock(NetworkCapabilities::class.java)

        doReturn(network).`when`(connectivityManager).activeNetwork
        doReturn(caps).`when`(connectivityManager).getNetworkCapabilities(network)
        doReturn(false).`when`(caps).hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

        assertNull(provider.getModernNetworkDetails())
    }

    @Test
    fun getModernNetworkDetails_whenLinkPropertiesIsNull_returnsDetailsWithNullProperties() {
        val wifiManager = mock(WifiManager::class.java)
        val connectivityManager = mock(android.net.ConnectivityManager::class.java)
        val provider = WifiConnectionInfoProvider(wifiManager, connectivityManager, guard)

        val network = mock(Network::class.java)
        val caps = mock(NetworkCapabilities::class.java)

        doReturn(network).`when`(connectivityManager).activeNetwork
        doReturn(caps).`when`(connectivityManager).getNetworkCapabilities(network)
        doReturn(true).`when`(caps).hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        doReturn(100).`when`(caps).linkDownstreamBandwidthKbps
        doReturn(50).`when`(caps).linkUpstreamBandwidthKbps
        doReturn(true).`when`(caps).hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        doReturn(true).`when`(caps).hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        doReturn(false).`when`(caps).hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        doReturn(null).`when`(connectivityManager).getLinkProperties(network)

        val result = provider.getModernNetworkDetails()

        assertEquals(true, result?.isConnected)
        assertEquals(true, result?.hasInternet)
        assertEquals(true, result?.isValidated)
        assertEquals(true, result?.isMetered)
        assertEquals(100, result?.linkDownstreamBandwidthKbps)
        assertEquals(50, result?.linkUpstreamBandwidthKbps)
        assertNull(result?.interfaceName)
        assertEquals(emptyList<String>(), result?.dnsServers)
        assertNull(result?.domains)
    }
}
