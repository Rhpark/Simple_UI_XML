package kr.open.library.simple_ui.system_manager.info.network.connectivity.callback

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import kr.open.library.simple_ui.system_manager.info.network.connectivity.data.NetworkCapabilitiesData
import kr.open.library.simple_ui.system_manager.info.network.connectivity.data.NetworkLinkPropertiesData
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.state.TelephonyNetworkDetailType
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.state.TelephonyNetworkState
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.state.TelephonyNetworkType

internal class NetworkStateCallback(
    private var onNetworkAvailable: ((Network) -> Unit)? = null,
    private var onNetworkLosing: ((Network, Int) -> Unit)? = null,
    private var onNetworkLost: ((Network) -> Unit)? = null,
    private var onUnavailable: (() -> Unit)? = null,
    private var onNetworkCapabilitiesChanged: ((Network, NetworkCapabilitiesData) -> Unit)? = null,
    private var onLinkPropertiesChanged: ((Network, NetworkLinkPropertiesData) -> Unit)? = null,
    private var onBlockedStatusChanged: ((Network, Boolean) -> Unit)? = null,
    private var onNetworkChangedState: ((TelephonyNetworkState) -> Unit)? = null
) : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        onNetworkAvailable?.invoke(network)
    }

    override fun onLosing(network: Network, maxMsToLive: Int) {
        super.onLosing(network, maxMsToLive)
        onNetworkLosing?.invoke(network, maxMsToLive)
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        onNetworkLost?.invoke(network)
        onNetworkChangedState?.invoke(TelephonyNetworkState(TelephonyNetworkType.DISCONNECT, TelephonyNetworkDetailType.DISCONNECT))
    }

    override fun onUnavailable() {
        super.onUnavailable()
        onUnavailable?.invoke()
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        onNetworkCapabilitiesChanged?.invoke(network, NetworkCapabilitiesData((networkCapabilities)))
    }

    override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
        super.onLinkPropertiesChanged(network, linkProperties)
        onLinkPropertiesChanged?.invoke(network, NetworkLinkPropertiesData((linkProperties)))
    }

    override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
        super.onBlockedStatusChanged(network, blocked)
        onBlockedStatusChanged?.invoke(network,blocked)
    }
}