package kr.open.library.simple_ui.core.robolectric.system_manager.info.network.connectivity

import android.Manifest
import android.app.Application
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.system_manager.info.network.connectivity.NetworkConnectivityInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowConnectivityManager
import org.robolectric.shadows.ShadowNetworkCapabilities
import org.robolectric.shadows.ShadowNetworkInfo

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

    private fun setActiveWifiNetwork() {
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
        shadowCaps.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        shadowCaps.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        shadowConnectivityManager.setNetworkCapabilities(activeNetwork, caps)
        shadowConnectivityManager.setLinkProperties(
            activeNetwork,
            LinkProperties().apply { interfaceName = "wlan0" },
        )
    }
}
