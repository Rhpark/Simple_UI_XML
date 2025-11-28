package kr.open.library.simple_ui.core.system_manager.info.network.connectivity.callback

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import kr.open.library.simple_ui.core.system_manager.info.network.connectivity.data.NetworkCapabilitiesData
import kr.open.library.simple_ui.core.system_manager.info.network.connectivity.data.NetworkLinkPropertiesData
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state.TelephonyNetworkDetailType
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state.TelephonyNetworkState
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state.TelephonyNetworkType

/**
 * Internal callback class for monitoring network state changes.<br><br>
 * 네트워크 상태 변경을 모니터링하기 위한 내부 콜백 클래스입니다.<br>
 *
 * This class extends ConnectivityManager.NetworkCallback to provide lambda-based callbacks.<br><br>
 * 이 클래스는 ConnectivityManager.NetworkCallback을 상속받아 람다 기반의 콜백을 제공합니다.<br>
 *
 * @param onNetworkAvailable Called when network becomes available.<br><br>
 *                           네트워크를 사용할 수 있게 되었을 때 호출됩니다.
 * @param onNetworkLosing Called when network is about to be lost.<br><br>
 *                        네트워크가 곧 끊어질 때 호출됩니다.
 * @param onNetworkLost Called when network is lost.<br><br>
 *                      네트워크가 끊어졌을 때 호출됩니다.
 * @param onUnavailable Called when network is unavailable.<br><br>
 *                      네트워크를 사용할 수 없을 때 호출됩니다.
 * @param onNetworkCapabilitiesChanged Called when network capabilities change.<br><br>
 *                                     네트워크 기능이 변경되었을 때 호출됩니다.
 * @param onLinkPropertiesChanged Called when link properties change.<br><br>
 *                                링크 속성이 변경되었을 때 호출됩니다.
 * @param onBlockedStatusChanged Called when network blocked status changes.<br><br>
 *                               네트워크 차단 상태가 변경되었을 때 호출됩니다.
 * @param onNetworkChangedState Called when network state changes (for internal state tracking).<br><br>
 *                              네트워크 상태가 변경되었을 때 호출됩니다 (내부 상태 추적용).
 */
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
    /**
     * Called when the framework connects to a new network.<br><br>
     * 프레임워크가 새 네트워크에 연결될 때 호출됩니다.<br>
     */
    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        onNetworkAvailable?.invoke(network)
    }

    /**
     * Called when the network is about to be disconnected.<br><br>
     * 네트워크가 곧 연결 해제될 때 호출됩니다.<br>
     */
    override fun onLosing(network: Network, maxMsToLive: Int) {
        super.onLosing(network, maxMsToLive)
        onNetworkLosing?.invoke(network, maxMsToLive)
    }

    /**
     * Called when the network is disconnected.<br><br>
     * 네트워크 연결이 끊어졌을 때 호출됩니다.<br>
     */
    override fun onLost(network: Network) {
        super.onLost(network)
        onNetworkLost?.invoke(network)
        onNetworkChangedState?.invoke(TelephonyNetworkState(TelephonyNetworkType.DISCONNECT, TelephonyNetworkDetailType.DISCONNECT))
    }

    /**
     * Called when the network is unavailable.<br><br>
     * 네트워크를 사용할 수 없을 때 호출됩니다.<br>
     */
    override fun onUnavailable() {
        super.onUnavailable()
        onUnavailable?.invoke()
    }

    /**
     * Called when the network capabilities have changed.<br><br>
     * 네트워크 기능이 변경되었을 때 호출됩니다.<br>
     */
    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        onNetworkCapabilitiesChanged?.invoke(network, NetworkCapabilitiesData((networkCapabilities)))
    }

    /**
     * Called when the link properties have changed.<br><br>
     * 링크 속성이 변경되었을 때 호출됩니다.<br>
     */
    override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
        super.onLinkPropertiesChanged(network, linkProperties)
        onLinkPropertiesChanged?.invoke(network, NetworkLinkPropertiesData((linkProperties)))
    }

    /**
     * Called when the blocked status of the network has changed.<br><br>
     * 네트워크의 차단 상태가 변경되었을 때 호출됩니다.<br>
     */
    override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
        super.onBlockedStatusChanged(network, blocked)
        onBlockedStatusChanged?.invoke(network,blocked)
    }
}