package kr.open.library.simple_ui.system_manager.robolectric.core.info.network.connectivity

import android.Manifest
import android.app.Application
import android.net.ConnectivityManager
import android.net.LinkAddress
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.system_manager.core.info.network.connectivity.NetworkConnectivityInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowConnectivityManager
import org.robolectric.shadows.ShadowNetwork
import org.robolectric.shadows.ShadowNetworkCapabilities
import org.robolectric.shadows.ShadowNetworkInfo
import java.net.InetAddress

/**
 * Verifies public connectivity queries, callback lifecycle, and IP lookup boundaries.<br><br>
 * 공개 연결 조회, 콜백 생명주기, IP 조회 경계를 검증합니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R])
class NetworkConnectivityInfoRobolectricTest {
    private lateinit var application: Application
    private lateinit var info: NetworkConnectivityInfo
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var shadowConnectivityManager: ShadowConnectivityManager

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        val shadowApp = Shadows.shadowOf(application)
        shadowApp.grantPermissions(
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )

        info = NetworkConnectivityInfo(application)
        info.refreshPermissions()

        connectivityManager = info.connectivityManager
        shadowConnectivityManager = Shadows.shadowOf(connectivityManager)
    }

    @Test
    fun isNetworkConnected_returnsTrue_whenActiveWifiNetworkPresent() {
        setActiveWifiNetwork()

        assertTrue(info.isNetworkConnected())
        assertTrue(info.isConnectedWifi())
        assertFalse(info.isConnectedMobile())
    }

    @Test
    fun connectivityQueries_returnEmptyState_whenNoActiveNetworkExists() {
        shadowConnectivityManager.setActiveNetworkInfo(null)
        shadowConnectivityManager.clearAllNetworks()

        assertFalse(info.isNetworkConnected())
        assertNull(info.getNetworkCapabilities())
        assertNull(info.getLinkProperties())
        assertFalse(info.isConnectedWifi())
        assertFalse(info.isConnectedMobile())
        assertFalse(info.isConnectedVPN())
        assertFalse(info.isConnectedBluetooth())
        assertFalse(info.isConnectedWifiAware())
        assertFalse(info.isConnectedEthernet())
        assertFalse(info.isConnectedLowPan())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun transportQueries_returnTrue_forCapabilitiesContainingEverySupportedTransport() {
        val (capabilities, _) = setActiveNetwork(
            NetworkCapabilities.TRANSPORT_WIFI,
            NetworkCapabilities.TRANSPORT_CELLULAR,
            NetworkCapabilities.TRANSPORT_VPN,
            NetworkCapabilities.TRANSPORT_BLUETOOTH,
            NetworkCapabilities.TRANSPORT_WIFI_AWARE,
            NetworkCapabilities.TRANSPORT_ETHERNET,
            NetworkCapabilities.TRANSPORT_LOWPAN,
            NetworkCapabilities.TRANSPORT_USB,
        )

        assertTrue(info.isConnectedWifi())
        assertTrue(info.isConnectedMobile())
        assertTrue(info.isConnectedVPN())
        assertTrue(info.isConnectedBluetooth())
        assertTrue(info.isConnectedWifiAware())
        assertTrue(info.isConnectedEthernet())
        assertTrue(info.isConnectedLowPan())
        assertTrue(info.isConnectedUSB())
        assertSame(capabilities, info.getNetworkCapabilities())
    }

    @Test
    fun getNetworkConnectivitySummary_returnsCurrentSnapshot() {
        val (capabilities, linkProperties) = setActiveNetwork(NetworkCapabilities.TRANSPORT_WIFI)
        val shadowWifiManager = Shadows.shadowOf(info.wifiController.wifiManager)
        shadowWifiManager.setWifiState(WifiManager.WIFI_STATE_ENABLED)

        val summary = info.getNetworkConnectivitySummary()

        assertTrue(summary.isNetworkConnected)
        assertTrue(summary.isWifiConnected)
        assertFalse(summary.isMobileConnected)
        assertFalse(summary.isVpnConnected)
        assertTrue(summary.isWifiEnabled)
        assertSame(capabilities, summary.networkCapabilities)
        assertSame(linkProperties, summary.linkProperties)
    }

    @Test
    fun registerNetworkCallback_replacesPreviousCallback_andUnregisterIsIdempotent() {
        info.registerNetworkCallback()
        assertEquals(1, shadowConnectivityManager.networkCallbacks.size)

        info.registerNetworkCallback()
        assertEquals(1, shadowConnectivityManager.networkCallbacks.size)

        info.unregisterNetworkCallback()
        info.unregisterNetworkCallback()
        assertTrue(shadowConnectivityManager.networkCallbacks.isEmpty())
    }

    @Test
    fun registerDefaultNetworkCallback_supportsHandler_andReplacesPreviousCallback() {
        val handler = Handler(Looper.getMainLooper())

        info.registerDefaultNetworkCallback(handler = handler)
        assertEquals(1, shadowConnectivityManager.networkCallbacks.size)

        info.registerDefaultNetworkCallback(handler = handler)
        assertEquals(1, shadowConnectivityManager.networkCallbacks.size)

        info.unregisterDefaultNetworkCallback()
        assertTrue(shadowConnectivityManager.networkCallbacks.isEmpty())
    }

    @Test
    fun onDestroy_unregistersBothCallbackTypes() {
        info.registerNetworkCallback()
        info.registerDefaultNetworkCallback()
        assertEquals(2, shadowConnectivityManager.networkCallbacks.size)

        info.onDestroy()

        assertTrue(shadowConnectivityManager.networkCallbacks.isEmpty())
    }

    @Test
    fun getIPAddressByNetworkType_returnsFirstNonLoopbackIpv4Address() {
        shadowConnectivityManager.clearAllNetworks()
        addNetwork(
            netId = 101,
            transportType = NetworkCapabilities.TRANSPORT_WIFI,
            linkAddresses = listOf(
                InetAddress.getByName("127.0.0.1"),
                InetAddress.getByName("2001:db8::1"),
                InetAddress.getByName("192.0.2.10"),
            ),
        )

        assertEquals(
            "192.0.2.10",
            info.getIPAddressByNetworkType(NetworkCapabilities.TRANSPORT_WIFI),
        )
    }

    @Test
    fun getIPAddressByNetworkType_returnsNull_whenTransportDoesNotMatch() {
        shadowConnectivityManager.clearAllNetworks()
        addNetwork(
            netId = 102,
            transportType = NetworkCapabilities.TRANSPORT_ETHERNET,
            linkAddresses = listOf(InetAddress.getByName("192.0.2.20")),
        )

        assertNull(info.getIPAddressByNetworkType(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    private fun setActiveWifiNetwork() {
        setActiveNetwork(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun setActiveNetwork(vararg transportTypes: Int): Pair<NetworkCapabilities, LinkProperties> {
        shadowConnectivityManager.setActiveNetworkInfo(
            ShadowNetworkInfo.newInstance(
                NetworkInfo.DetailedState.CONNECTED,
                ConnectivityManager.TYPE_WIFI,
                ConnectivityManager.TYPE_WIFI,
                true,
                true,
            ),
        )

        val activeNetwork = checkNotNull(connectivityManager.activeNetwork)
        val caps = NetworkCapabilities()
        val shadowCaps = Shadows.shadowOf(caps) as ShadowNetworkCapabilities
        transportTypes.forEach(shadowCaps::addTransportType)
        shadowCaps.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        shadowConnectivityManager.setNetworkCapabilities(activeNetwork, caps)
        val linkProperties = LinkProperties().apply { interfaceName = "wlan0" }
        shadowConnectivityManager.setLinkProperties(activeNetwork, linkProperties)
        return caps to linkProperties
    }

    private fun addNetwork(
        netId: Int,
        transportType: Int,
        linkAddresses: List<InetAddress>,
    ) {
        val network = ShadowNetwork.newInstance(netId)
        val networkInfo = ShadowNetworkInfo.newInstance(
            NetworkInfo.DetailedState.CONNECTED,
            ConnectivityManager.TYPE_WIFI,
            ConnectivityManager.TYPE_WIFI,
            true,
            true,
        )
        val capabilities = NetworkCapabilities()
        (Shadows.shadowOf(capabilities) as ShadowNetworkCapabilities).addTransportType(transportType)
        val linkAddressMocks = linkAddresses.map { address ->
            mock(LinkAddress::class.java).also { linkAddress ->
                doReturn(address).`when`(linkAddress).address
            }
        }
        val linkProperties = mock(LinkProperties::class.java)
        doReturn(linkAddressMocks).`when`(linkProperties).linkAddresses

        shadowConnectivityManager.addNetwork(network, networkInfo)
        shadowConnectivityManager.setNetworkCapabilities(network, capabilities)
        shadowConnectivityManager.setLinkProperties(network, linkProperties)
    }
}
