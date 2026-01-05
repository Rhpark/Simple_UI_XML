package kr.open.library.simple_ui.core.system_manager.info.network.connectivity

import android.Manifest.permission.ACCESS_NETWORK_STATE
import android.Manifest.permission.ACCESS_WIFI_STATE
import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.controller.wifi.WifiController
import kr.open.library.simple_ui.core.system_manager.extensions.getConnectivityManager
import kr.open.library.simple_ui.core.system_manager.info.network.connectivity.callback.NetworkStateCallback
import kr.open.library.simple_ui.core.system_manager.info.network.connectivity.data.NetworkCapabilitiesData
import kr.open.library.simple_ui.core.system_manager.info.network.connectivity.data.NetworkConnectivitySummary
import kr.open.library.simple_ui.core.system_manager.info.network.connectivity.data.NetworkLinkPropertiesData
import java.net.Inet4Address

/**
 * Pure network connectivity management class.<br><br>
 * 순수 네트워크 연결성 관리 클래스입니다.<br>
 *
 * This class manages network connection status and network capabilities.<br><br>
 * 이 클래스는 네트워크 연결 상태와 네트워크 능력을 관리합니다.<br>
 *
 * Main Features:<br>
 * - Network connection status check<br>
 * - Network capabilities inquiry<br>
 * - Transport type connection check<br>
 * - Network callback management<br>
 * - WiFi status check<br>
 * - Link properties inquiry<br><br>
 * 주요 기능:<br>
 * - 네트워크 연결 상태 확인<br>
 * - 네트워크 능력 조회<br>
 * - 전송 타입별 연결 확인<br>
 * - 네트워크 콜백 관리<br>
 * - WiFi 상태 확인<br>
 * - 링크 속성 조회<br>
 *
 * Required Permissions:<br>
 * - `android.permission.ACCESS_NETWORK_STATE` (Required for network connectivity checks)<br>
 * - `android.permission.ACCESS_WIFI_STATE` (Required for WiFi status checks)<br><br>
 * 필요한 권한:<br>
 * - `android.permission.ACCESS_NETWORK_STATE` (네트워크 연결성 확인에 필요)<br>
 * - `android.permission.ACCESS_WIFI_STATE` (WiFi 상태 확인에 필요)<br><br>
 *
 * Supported Transport Types:<br>
 * - WiFi, Mobile(Cellular), VPN, Bluetooth<br>
 * - WiFi Aware, Ethernet, LowPan, USB (API level dependent)<br><br>
 *
 * Usage Example:<br>
 * ```kotlin
 * val networkInfo = NetworkConnectivityInfo(context)
 *
 * // Basic connectivity check
 * val isConnected = networkInfo.isNetworkConnected()
 * val capabilities = networkInfo.getNetworkCapabilities()
 *
 * // Type-specific connection check
 * val isWifiConnected = networkInfo.isConnectedWifi()
 * val isMobileConnected = networkInfo.isConnectedMobile()
 *
 * // WiFi status check (requires ACCESS_WIFI_STATE)
 * val wifiEnabled = networkInfo.isWifiEnabled()
 *
 * // Network callback registration
 * networkInfo.registerNetworkCallback(
 *     onNetworkAvailable = { network -> /* Network connected */ },
 *     onNetworkLost = { network -> /* Network lost */ }
 * )
 * ```
 *
 * @param context The application context.<br><br>
 *                애플리케이션 컨텍스트.<br>
 */
public class NetworkConnectivityInfo(
    context: Context,
) : BaseSystemService(
        context,
        listOf(ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE)
    ) {
    /**
     * ConnectivityManager for network operations.<br><br>
     * 네트워크 작업을 위한 ConnectivityManager입니다.<br>
     */
    public val connectivityManager: ConnectivityManager by lazy { context.getConnectivityManager() }

    /**
     * WifiController for WiFi-specific operations.<br><br>
     * WiFi 전용 작업을 위한 WifiController입니다.<br>
     */
    public val wifiController: WifiController by lazy { WifiController(context) }

    /**
     * General network state callback.<br><br>
     * 일반 네트워크 상태 콜백입니다.<br>
     */
    private var networkCallBack: NetworkStateCallback? = null

    /**
     * Default network state callback.<br><br>
     * 기본 네트워크 상태 콜백입니다.<br>
     */
    private var networkDefaultCallback: NetworkStateCallback? = null

    /**
     * Checks if network is connected.<br><br>
     * 네트워크가 연결되어 있는지 확인합니다.<br>
     *
     * @return `true` if network is connected, `false` otherwise.<br><br>
     *         네트워크가 연결되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isNetworkConnected(): Boolean = safeCatch(false) {
        val caps = getNetworkCapabilities()
        val linkProperties = getLinkProperties()
        (caps != null) && (linkProperties != null)
    }

    /**
     * Gets NetworkCapabilities of current network.<br><br>
     * 현재 네트워크의 NetworkCapabilities를 반환합니다.<br>
     *
     * @return NetworkCapabilities of the active network, or null if unavailable.<br><br>
     *         현재 활성 네트워크의 능력, 사용할 수 없는 경우 null.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getNetworkCapabilities(): NetworkCapabilities? = tryCatchSystemManager(null) {
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    }

    /**
     * Gets LinkProperties of current network.<br><br>
     * 현재 네트워크의 LinkProperties를 반환합니다.<br>
     *
     * @return LinkProperties of the active network, or null if unavailable.<br><br>
     *         현재 활성 네트워크의 링크 속성, 사용할 수 없는 경우 null.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getLinkProperties(): LinkProperties? = tryCatchSystemManager(defaultValue = null) {
        connectivityManager.getLinkProperties(connectivityManager.activeNetwork)
    }

    /**
     * Checks if connected via WiFi.<br><br>
     * WiFi로 연결되어 있는지 확인합니다.<br>
     *
     * @return `true` if connected via WiFi, `false` otherwise.<br><br>
     *         WiFi로 연결되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedWifi(): Boolean = getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false

    /**
     * Checks if connected via mobile network.<br><br>
     * 모바일 네트워크로 연결되어 있는지 확인합니다.<br>
     *
     * @return `true` if connected via mobile network, `false` otherwise.<br><br>
     *         모바일 네트워크로 연결되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedMobile(): Boolean = getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false

    /**
     * Checks if connected via VPN.<br><br>
     * VPN으로 연결되어 있는지 확인합니다.<br>
     *
     * @return `true` if connected via VPN, `false` otherwise.<br><br>
     *         VPN으로 연결되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedVPN(): Boolean = getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false

    /**
     * Checks if connected via Bluetooth.<br><br>
     * 블루투스로 연결되어 있는지 확인합니다.<br>
     *
     * @return `true` if connected via Bluetooth, `false` otherwise.<br><br>
     *         블루투스로 연결되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedBluetooth(): Boolean = getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) ?: false

    /**
     * Checks if connected via WiFi Aware.<br><br>
     * WiFi Aware로 연결되어 있는지 확인합니다.<br>
     *
     * @return `true` if connected via WiFi Aware, `false` otherwise.<br><br>
     *         WiFi Aware로 연결되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedWifiAware(): Boolean = getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE) ?: false

    /**
     * Checks if connected via Ethernet.<br><br>
     * 이더넷으로 연결되어 있는지 확인합니다.<br>
     *
     * @return `true` if connected via Ethernet, `false` otherwise.<br><br>
     *         이더넷으로 연결되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedEthernet(): Boolean = getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ?: false

    /**
     * Checks if connected via LowPan.<br><br>
     * LowPan으로 연결되어 있는지 확인합니다.<br>
     *
     * @return `true` if connected via LowPan, `false` otherwise.<br><br>
     *         LowPan으로 연결되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedLowPan(): Boolean = getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN) ?: false

    /**
     * Checks if connected via USB (API 31+).<br><br>
     * USB로 연결되어 있는지 확인합니다 (API 31+).<br>
     *
     * @return `true` if connected via USB, `false` otherwise.<br><br>
     *         USB로 연결되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    @RequiresApi(Build.VERSION_CODES.S)
    public fun isConnectedUSB(): Boolean = getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_USB) ?: false

    /**
     * Checks if WiFi is enabled.<br><br>
     * WiFi가 활성화되어 있는지 확인합니다.<br>
     *
     * @return `true` if WiFi is enabled, `false` otherwise.<br><br>
     *         WiFi가 활성화되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(ACCESS_WIFI_STATE)
    public fun isWifiEnabled(): Boolean = wifiController.isWifiEnabled()

    /**
     * Registers general network state callback.<br><br>
     * 일반 네트워크 상태 콜백을 등록합니다.<br>
     *
     * @param handler Handler for callback execution (null for main thread).<br><br>
     *                콜백 실행을 위한 핸들러 (null이면 메인 스레드).<br>
     * @param onNetworkAvailable Called when network is connected.<br><br>
     *                           네트워크 연결 시 호출.<br>
     * @param onNetworkLosing Called when network is about to be lost.<br><br>
     *                        네트워크 끊어질 예정 시 호출.<br>
     * @param onNetworkLost Called when network is lost.<br><br>
     *                      네트워크 끊어짐 시 호출.<br>
     * @param onUnavailable Called when network is unavailable.<br><br>
     *                      네트워크 사용 불가 시 호출.<br>
     * @param onNetworkCapabilitiesChanged Called when network capabilities change.<br><br>
     *                                     네트워크 능력 변경 시 호출.<br>
     * @param onLinkPropertiesChanged Called when link properties change.<br><br>
     *                                링크 속성 변경 시 호출.<br>
     * @param onBlockedStatusChanged Called when blocked status changes.<br><br>
     *                               차단 상태 변경 시 호출.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun registerNetworkCallback(
        handler: Handler? = null,
        onNetworkAvailable: ((Network) -> Unit)? = null,
        onNetworkLosing: ((Network, Int) -> Unit)? = null,
        onNetworkLost: ((Network) -> Unit)? = null,
        onUnavailable: (() -> Unit)? = null,
        onNetworkCapabilitiesChanged: ((Network, NetworkCapabilitiesData) -> Unit)? = null,
        onLinkPropertiesChanged: ((Network, NetworkLinkPropertiesData) -> Unit)? = null,
        onBlockedStatusChanged: ((Network, Boolean) -> Unit)? = null,
    ) {
        unregisterNetworkCallback()
        networkCallBack =
            NetworkStateCallback(
                onNetworkAvailable,
                onNetworkLosing,
                onNetworkLost,
                onUnavailable,
                onNetworkCapabilitiesChanged,
                onLinkPropertiesChanged,
                onBlockedStatusChanged,
            )

        val networkRequest = NetworkRequest.Builder().build()
        networkCallBack?.let { callback ->
            handler?.let {
                connectivityManager.registerNetworkCallback(networkRequest, callback, it)
            } ?: connectivityManager.registerNetworkCallback(networkRequest, callback)
        }
    }

    /**
     * Registers default network state callback.<br><br>
     * 기본 네트워크 상태 콜백을 등록합니다.<br>
     *
     * @param handler Handler for callback execution (null for main thread).<br><br>
     *                콜백 실행을 위한 핸들러 (null이면 메인 스레드).<br>
     * @param onNetworkAvailable Called when network is connected.<br><br>
     *                           네트워크 연결 시 호출.<br>
     * @param onNetworkLosing Called when network is about to be lost.<br><br>
     *                        네트워크 끊어질 예정 시 호출.<br>
     * @param onNetworkLost Called when network is lost.<br><br>
     *                      네트워크 끊어짐 시 호출.<br>
     * @param onUnavailable Called when network is unavailable.<br><br>
     *                      네트워크 사용 불가 시 호출.<br>
     * @param onNetworkCapabilitiesChanged Called when network capabilities change.<br><br>
     *                                     네트워크 능력 변경 시 호출.<br>
     * @param onLinkPropertiesChanged Called when link properties change.<br><br>
     *                                링크 속성 변경 시 호출.<br>
     * @param onBlockedStatusChanged Called when blocked status changes.<br><br>
     *                               차단 상태 변경 시 호출.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun registerDefaultNetworkCallback(
        handler: Handler? = null,
        onNetworkAvailable: ((Network) -> Unit)? = null,
        onNetworkLosing: ((Network, Int) -> Unit)? = null,
        onNetworkLost: ((Network) -> Unit)? = null,
        onUnavailable: (() -> Unit)? = null,
        onNetworkCapabilitiesChanged: ((Network, NetworkCapabilitiesData) -> Unit)? = null,
        onLinkPropertiesChanged: ((Network, NetworkLinkPropertiesData) -> Unit)? = null,
        onBlockedStatusChanged: ((Network, Boolean) -> Unit)? = null,
    ) {
        unregisterDefaultNetworkCallback()

        networkDefaultCallback = NetworkStateCallback(
            onNetworkAvailable,
            onNetworkLosing,
            onNetworkLost,
            onUnavailable,
            onNetworkCapabilitiesChanged,
            onLinkPropertiesChanged,
            onBlockedStatusChanged,
        )

        networkDefaultCallback?.let { callback ->
            handler?.let {
                connectivityManager.registerDefaultNetworkCallback(callback, it)
            } ?: connectivityManager.registerDefaultNetworkCallback(callback)
        }
    }

    /**
     * Unregisters general network callback.<br><br>
     * 일반 네트워크 콜백을 해제합니다.<br>
     */
    public fun unregisterNetworkCallback() {
        safeCatch {
            networkCallBack?.let { connectivityManager.unregisterNetworkCallback(it) }
        }
        networkCallBack = null
    }

    /**
     * Unregisters default network callback.<br><br>
     * 기본 네트워크 콜백을 해제합니다.<br>
     */
    public fun unregisterDefaultNetworkCallback() {
        safeCatch {
            networkDefaultCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
        }
        networkDefaultCallback = null
    }

    /**
     * Gets network connectivity summary.<br><br>
     * 네트워크 연결 상태 요약 정보를 반환합니다.<br>
     *
     * @return NetworkConnectivitySummary containing connection status.<br><br>
     *         연결 상태를 포함하는 NetworkConnectivitySummary.
     */
    @RequiresPermission(allOf = [ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE])
    public fun getNetworkConnectivitySummary(): NetworkConnectivitySummary = NetworkConnectivitySummary(
        isNetworkConnected = isNetworkConnected(),
        isWifiConnected = isConnectedWifi(),
        isMobileConnected = isConnectedMobile(),
        isVpnConnected = isConnectedVPN(),
        isWifiEnabled = isWifiEnabled(),
        networkCapabilities = getNetworkCapabilities(),
        linkProperties = getLinkProperties(),
    )

    /**
     * Cleans up resources and unregisters callbacks.<br><br>
     * 리소스를 정리하고 콜백을 해제합니다.<br>
     */
    override fun onDestroy() {
        unregisterNetworkCallback()
        unregisterDefaultNetworkCallback()
        wifiController.onDestroy()
        super.onDestroy()
    }

    /**
     * Gets IP address by network type.<br><br>
     * 네트워크 타입별 IP 주소를 가져옵니다.<br>
     *
     * Usage:<br>
     * `getIPAddressByNetworkType(NetworkCapabilities.TRANSPORT_ETHERNET)`<br>
     * `getIPAddressByNetworkType(NetworkCapabilities.TRANSPORT_WIFI)`<br>
     * `getIPAddressByNetworkType(NetworkCapabilities.TRANSPORT_CELLULAR)`<br>
     *
     * @param type Network transport type (e.g., TRANSPORT_WIFI).<br><br>
     *             네트워크 전송 타입 (예: TRANSPORT_WIFI).<br>
     * @return IP address string (IPv4), or null if unavailable.<br><br>
     *         IP 주소 문자열 (IPv4), 사용할 수 없는 경우 null.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    fun getIPAddressByNetworkType(type: Int): String? = tryCatchSystemManager(null) {
        // 모든 네트워크를 순회하여 해당 타입 찾기
        val allNetworks = connectivityManager.allNetworks

        for (network in allNetworks) {
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: continue

            // 요청한 transport type을 가진 네트워크인지 확인
            if (!capabilities.hasTransport(type)) {
                continue
            }

            // 해당 네트워크의 LinkProperties에서 IPv4 주소 찾기
            val linkProperties = connectivityManager.getLinkProperties(network) ?: continue

            linkProperties.linkAddresses.forEach { linkAddress ->
                val address = linkAddress.address
                // IPv4이고 loopback이 아닌 주소 반환
                if (address is Inet4Address && !address.isLoopbackAddress) {
                    return address.hostAddress
                }
            }
        }

        // 해당 타입의 네트워크를 찾지 못함
        Logx.d("No network found with transport type: $type")
        null
    }
}
