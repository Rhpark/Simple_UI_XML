package kr.open.library.simple_ui.core.system_manager.info.network.connectivity

import android.Manifest
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
import kr.open.library.simple_ui.core.system_manager.info.network.connectivity.data.NetworkLinkPropertiesData
import java.net.NetworkInterface
import java.util.Collections

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
 * - `android.permission.ACCESS_NETWORK_STATE` (Required)<br><br>
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
 * // Network callback registration
 * networkInfo.registerNetworkCallback(
 *     onNetworkAvailable = { network -> /* Network connected */ },
 *     onNetworkLost = { network -> /* Network lost */ }
 * )
 * ```
 *
 * @param context The application context.<br><br>
 *                애플리케이션 컨텍스트.
 */
public class NetworkConnectivityInfo(context: Context) : BaseSystemService(
    context,
    listOf(ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE)
) {

    // =================================================
    // Core System Services
    // =================================================
    
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

    // =================================================
    // Network Callback Management
    // =================================================
    
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

    // =================================================
    // Basic Network Connectivity / 기본 네트워크 연결성
    // =================================================
    
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


    // =================================================
    // Network Capabilities / 네트워크 능력
    // =================================================
    
    /**
     * Gets NetworkCapabilities of current network.<br><br>
     * 현재 네트워크의 NetworkCapabilities를 반환합니다.<br>
     * 
     * @return NetworkCapabilities of the active network, or null if unavailable.<br><br>
     *         현재 활성 네트워크의 능력, 사용할 수 없는 경우 null.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getNetworkCapabilities(): NetworkCapabilities? = safeCatch(defaultValue = null) {
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
    public fun getLinkProperties(): LinkProperties? = safeCatch(defaultValue = null) {
        connectivityManager.getLinkProperties(connectivityManager.activeNetwork)
    }

    // =================================================
    // Transport Type Connectivity / 전송 타입별 연결성
    // =================================================
    
    /**
     * Checks if connected via WiFi.<br><br>
     * WiFi로 연결되어 있는지 확인합니다.<br>
     *
     * @return `true` if connected via WiFi, `false` otherwise.<br><br>
     *         WiFi로 연결되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedWifi(): Boolean = 
        getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false

    /**
     * Checks if connected via mobile network.<br><br>
     * 모바일 네트워크로 연결되어 있는지 확인합니다.<br>
     *
     * @return `true` if connected via mobile network, `false` otherwise.<br><br>
     *         모바일 네트워크로 연결되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedMobile(): Boolean = 
        getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false

    /**
     * Checks if connected via VPN.<br><br>
     * VPN으로 연결되어 있는지 확인합니다.<br>
     *
     * @return `true` if connected via VPN, `false` otherwise.<br><br>
     *         VPN으로 연결되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedVPN(): Boolean = 
        getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false

    /**
     * Checks if connected via Bluetooth.<br><br>
     * 블루투스로 연결되어 있는지 확인합니다.<br>
     *
     * @return `true` if connected via Bluetooth, `false` otherwise.<br><br>
     *         블루투스로 연결되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedBluetooth(): Boolean = 
        getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) ?: false

    /**
     * Checks if connected via WiFi Aware.<br><br>
     * WiFi Aware로 연결되어 있는지 확인합니다.<br>
     *
     * @return `true` if connected via WiFi Aware, `false` otherwise.<br><br>
     *         WiFi Aware로 연결되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedWifiAware(): Boolean = 
        getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE) ?: false

    /**
     * Checks if connected via Ethernet.<br><br>
     * 이더넷으로 연결되어 있는지 확인합니다.<br>
     *
     * @return `true` if connected via Ethernet, `false` otherwise.<br><br>
     *         이더넷으로 연결되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedEthernet(): Boolean = 
        getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ?: false

    /**
     * Checks if connected via LowPan.<br><br>
     * LowPan으로 연결되어 있는지 확인합니다.<br>
     *
     * @return `true` if connected via LowPan, `false` otherwise.<br><br>
     *         LowPan으로 연결되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedLowPan(): Boolean = 
        getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN) ?: false

    /**
     * Checks if connected via USB (API 31+).<br><br>
     * USB로 연결되어 있는지 확인합니다 (API 31+).<br>
     *
     * @return `true` if connected via USB, `false` otherwise.<br><br>
     *         USB로 연결되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    @RequiresApi(Build.VERSION_CODES.S)
    public fun isConnectedUSB(): Boolean = 
        getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_USB) ?: false

    // =================================================
    // WiFi State / WiFi 상태
    // =================================================
    
    /**
     * Checks if WiFi is enabled.<br><br>
     * WiFi가 활성화되어 있는지 확인합니다.<br>
     *
     * @return `true` if WiFi is enabled, `false` otherwise.<br><br>
     *         WiFi가 활성화되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(Manifest.permission.ACCESS_WIFI_STATE)
    public fun isWifiEnabled(): Boolean = wifiController.isWifiEnabled()

    // =================================================
    // Network Callback Management / 네트워크 콜백 관리
    // =================================================
    
    /**
     * Registers general network state callback.<br><br>
     * 일반 네트워크 상태 콜백을 등록합니다.<br>
     * 
     * @param handler Handler for callback execution (null for main thread).<br><br>
     *                콜백 실행을 위한 핸들러 (null이면 메인 스레드).
     * @param onNetworkAvailable Called when network is connected.<br><br>
     *                           네트워크 연결 시 호출.
     * @param onNetworkLosing Called when network is about to be lost.<br><br>
     *                        네트워크 끊어질 예정 시 호출.
     * @param onNetworkLost Called when network is lost.<br><br>
     *                      네트워크 끊어짐 시 호출.
     * @param onUnavailable Called when network is unavailable.<br><br>
     *                      네트워크 사용 불가 시 호출.
     * @param onNetworkCapabilitiesChanged Called when network capabilities change.<br><br>
     *                                     네트워크 능력 변경 시 호출.
     * @param onLinkPropertiesChanged Called when link properties change.<br><br>
     *                                링크 속성 변경 시 호출.
     * @param onBlockedStatusChanged Called when blocked status changes.<br><br>
     *                               차단 상태 변경 시 호출.
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
        networkCallBack = NetworkStateCallback(
            onNetworkAvailable, onNetworkLosing, onNetworkLost, onUnavailable,
            onNetworkCapabilitiesChanged, onLinkPropertiesChanged, onBlockedStatusChanged
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
     *                콜백 실행을 위한 핸들러 (null이면 메인 스레드).
     * @param onNetworkAvailable Called when network is connected.<br><br>
     *                           네트워크 연결 시 호출.
     * @param onNetworkLosing Called when network is about to be lost.<br><br>
     *                        네트워크 끊어질 예정 시 호출.
     * @param onNetworkLost Called when network is lost.<br><br>
     *                      네트워크 끊어짐 시 호출.
     * @param onUnavailable Called when network is unavailable.<br><br>
     *                      네트워크 사용 불가 시 호출.
     * @param onNetworkCapabilitiesChanged Called when network capabilities change.<br><br>
     *                                     네트워크 능력 변경 시 호출.
     * @param onLinkPropertiesChanged Called when link properties change.<br><br>
     *                                링크 속성 변경 시 호출.
     * @param onBlockedStatusChanged Called when blocked status changes.<br><br>
     *                               차단 상태 변경 시 호출.
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
            onNetworkAvailable, onNetworkLosing, onNetworkLost, onUnavailable,
            onNetworkCapabilitiesChanged, onLinkPropertiesChanged, onBlockedStatusChanged
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
        networkCallBack?.let {
            connectivityManager.unregisterNetworkCallback(it)
        }
        networkCallBack = null
    }

    /**
     * Unregisters default network callback.<br><br>
     * 기본 네트워크 콜백을 해제합니다.<br>
     */
    public fun unregisterDefaultNetworkCallback() {
        networkDefaultCallback?.let {
            connectivityManager.unregisterNetworkCallback(it)
        }
        networkDefaultCallback = null
    }

    // =================================================
    // Cross-Reference Helper Methods / 상호참조 헬퍼 메서드
    // =================================================
    
    /**
     * Gets network connectivity summary.<br><br>
     * 네트워크 연결 상태 요약 정보를 반환합니다.<br>
     *
     * @return NetworkConnectivitySummary containing connection status.<br><br>
     *         연결 상태를 포함하는 NetworkConnectivitySummary.
     */
    @RequiresPermission(allOf = [ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE])
    public fun getNetworkConnectivitySummary(): NetworkConnectivitySummary {
        return NetworkConnectivitySummary(
            isNetworkConnected = isNetworkConnected(),
            isWifiConnected = isConnectedWifi(),
            isMobileConnected = isConnectedMobile(),
            isVpnConnected = isConnectedVPN(),
            isWifiEnabled = isWifiEnabled(),
            networkCapabilities = getNetworkCapabilities(),
            linkProperties = getLinkProperties()
        )
    }

    // =================================================
    // Data Classes / 데이터 클래스
    // =================================================
    
    /**
     * Network connectivity summary information.<br><br>
     * 네트워크 연결성 요약 정보입니다.<br>
     */
    public data class NetworkConnectivitySummary(
        val isNetworkConnected: Boolean,
        val isWifiConnected: Boolean,
        val isMobileConnected: Boolean,
        val isVpnConnected: Boolean,
        val isWifiEnabled: Boolean,
        val networkCapabilities: NetworkCapabilities?,
        val linkProperties: LinkProperties?
    )

    // =================================================
    // Cleanup / 정리
    // =================================================
    
    /**
     * Cleans up resources and unregisters callbacks.<br><br>
     * 리소스를 정리하고 콜백을 해제합니다.<br>
     */
    override fun onDestroy() {
        try {
            unregisterNetworkCallback()
            unregisterDefaultNetworkCallback()
            wifiController.onDestroy()
            Logx.d("NetworkConnectivityInfo destroyed")
        } catch (e: Exception) {
            Logx.e("Error during NetworkConnectivityInfo cleanup: ${e.message}")
        } finally {
            super.onDestroy()
        }
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
     *             네트워크 전송 타입 (예: TRANSPORT_WIFI).
     * @return IP address string (IPv4), or null if unavailable.<br><br>
     *         IP 주소 문자열 (IPv4), 사용할 수 없는 경우 null.
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    fun getIPAddressByNetworkType(type:Int): String? {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        if(capabilities == null) return null

        val transport = capabilities.hasTransport(type)

        if(transport == null) return null

        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val hostAddress = addr.hostAddress
                        // IPv4 주소만 반환
                        val isIPv4 = hostAddress?.indexOf(':') ?: -1 < 0
                        if (isIPv4) {
                            return hostAddress
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}