package kr.open.library.simple_ui.core.robolectric.system_manager.info.network.connectivity.callback

import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import kr.open.library.simple_ui.core.system_manager.info.network.connectivity.callback.NetworkStateCallback
import kr.open.library.simple_ui.core.system_manager.info.network.connectivity.data.NetworkCapabilitiesData
import kr.open.library.simple_ui.core.system_manager.info.network.connectivity.data.NetworkLinkPropertiesData
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state.TelephonyNetworkDetailType
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state.TelephonyNetworkState
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state.TelephonyNetworkType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NetworkStateCallbackTest {
    private val network: Network = mock(Network::class.java)
    private val networkCapabilities: NetworkCapabilities = mock(NetworkCapabilities::class.java)
    private val linkProperties: LinkProperties = mock(LinkProperties::class.java)

    @Test
    fun `onAvailable invokes listener`() {
        var captured: Network? = null
        val callback = NetworkStateCallback(onNetworkAvailable = { captured = it })

        callback.onAvailable(network)

        assertSame(network, captured)
    }

    @Test
    fun `onLosing forwards params`() {
        var capturedNetwork: Network? = null
        var capturedMs: Int? = null
        val callback =
            NetworkStateCallback(onNetworkLosing = { net, ms ->
                capturedNetwork = net
                capturedMs = ms
            })

        callback.onLosing(network, 500)

        assertSame(network, capturedNetwork)
        assertEquals(500, capturedMs)
    }

    @Test
    fun `onLost invokes both lost and telephony state callbacks`() {
        var lostNetwork: Network? = null
        var state: TelephonyNetworkState? = null
        val callback =
            NetworkStateCallback(
                onNetworkLost = { lostNetwork = it },
                onNetworkChangedState = { state = it },
            )

        callback.onLost(network)

        assertSame(network, lostNetwork)
        assertEquals(TelephonyNetworkType.DISCONNECT, state?.networkTypeState)
        assertEquals(TelephonyNetworkDetailType.DISCONNECT, state?.networkTypeDetailState)
    }

    @Test
    fun `onUnavailable triggers listener`() {
        var invoked = false
        val callback = NetworkStateCallback(onUnavailable = { invoked = true })

        callback.onUnavailable()

        assertTrue(invoked)
    }

    @Test
    fun `onCapabilitiesChanged wraps NetworkCapabilities into data object`() {
        var capturedNetwork: Network? = null
        var capturedData: NetworkCapabilitiesData? = null
        val callback =
            NetworkStateCallback(
                onNetworkCapabilitiesChanged = { net, data ->
                    capturedNetwork = net
                    capturedData = data
                },
            )

        callback.onCapabilitiesChanged(network, networkCapabilities)

        assertSame(network, capturedNetwork)
        assertSame(networkCapabilities, capturedData?.networkCapabilities)
    }

    @Test
    fun `onLinkPropertiesChanged wraps LinkProperties into data object`() {
        var capturedNetwork: Network? = null
        var capturedData: NetworkLinkPropertiesData? = null
        val callback =
            NetworkStateCallback(
                onLinkPropertiesChanged = { net, data ->
                    capturedNetwork = net
                    capturedData = data
                },
            )

        callback.onLinkPropertiesChanged(network, linkProperties)

        assertSame(network, capturedNetwork)
        assertSame(linkProperties, capturedData?.linkProperties)
    }

    @Test
    fun `onBlockedStatusChanged forwards parameters`() {
        var capturedNetwork: Network? = null
        var capturedBlocked: Boolean? = null
        val callback =
            NetworkStateCallback(onBlockedStatusChanged = { net, blocked ->
                capturedNetwork = net
                capturedBlocked = blocked
            })

        callback.onBlockedStatusChanged(network, true)

        assertSame(network, capturedNetwork)
        assertEquals(true, capturedBlocked)
    }

    @Test
    fun `onAvailable without callback does not crash`() {
        val callback = NetworkStateCallback()
        callback.onAvailable(network)
        // Success if no exception thrown
    }

    @Test
    fun `onLosing without callback does not crash`() {
        val callback = NetworkStateCallback()
        callback.onLosing(network, 500)
        // Success if no exception thrown
    }

    @Test
    fun `onLost without callback does not crash`() {
        val callback = NetworkStateCallback()
        callback.onLost(network)
        // Success if no exception thrown
    }

    @Test
    fun `onUnavailable without callback does not crash`() {
        val callback = NetworkStateCallback()
        callback.onUnavailable()
        // Success if no exception thrown
    }

    @Test
    fun `onCapabilitiesChanged without callback does not crash`() {
        val callback = NetworkStateCallback()
        callback.onCapabilitiesChanged(network, networkCapabilities)
        // Success if no exception thrown
    }

    @Test
    fun `onLinkPropertiesChanged without callback does not crash`() {
        val callback = NetworkStateCallback()
        callback.onLinkPropertiesChanged(network, linkProperties)
        // Success if no exception thrown
    }

    @Test
    fun `onBlockedStatusChanged without callback does not crash`() {
        val callback = NetworkStateCallback()
        callback.onBlockedStatusChanged(network, true)
        // Success if no exception thrown
    }
}
