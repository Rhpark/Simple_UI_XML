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
 * NetworkConnectivityInfo - 순수 네트워크 연결성 관리 클래스
 * Pure Network Connectivity Management Class
 * 
 * 이 클래스는 네트워크 연결 상태와 네트워크 능력을 관리합니다.
 * This class manages network connection status and network capabilities.
 * 
 * 주요 기능 / Main Features:
 * - 네트워크 연결 상태 확인 / Network connection status check
 * - 네트워크 능력 조회 / Network capabilities inquiry
 * - 전송 타입별 연결 확인 / Transport type connection check
 * - 네트워크 콜백 관리 / Network callback management
 * - WiFi 상태 확인 / WiFi status check
 * - 링크 속성 조회 / Link properties inquiry
 * 
 * 필수 권한 / Required Permissions:
 * - android.permission.ACCESS_NETWORK_STATE (필수/Required)
 * 
 * 지원하는 전송 타입 / Supported Transport Types:
 * - WiFi, Mobile(Cellular), VPN, Bluetooth
 * - WiFi Aware, Ethernet, LowPan, USB (API level dependent)
 * 
 * 사용 예시 / Usage Example:
 * ```
 * val networkInfo = NetworkConnectivityInfo(context)
 * 
 * // 기본 연결성 확인 / Basic connectivity check
 * val isConnected = networkInfo.isNetworkConnected()
 * val capabilities = networkInfo.getNetworkCapabilities()
 * 
 * // 타입별 연결 확인 / Type-specific connection check
 * val isWifiConnected = networkInfo.isConnectedWifi()
 * val isMobileConnected = networkInfo.isConnectedMobile()
 * 
 * // 네트워크 콜백 등록 / Network callback registration
 * networkInfo.registerNetworkCallback(
 *     onNetworkAvailable = { network -> /* 네트워크 연결됨 */ },
 *     onNetworkLost = { network -> /* 네트워크 끊김 */ }
 * )
 * ```
 */
public class NetworkConnectivityInfo(context: Context) : BaseSystemService(
    context,
    listOf(ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE)
) {

    // =================================================
    // Core System Services
    // =================================================
    
    /**
     * ConnectivityManager for network operations
     * 네트워크 작업을 위한 ConnectivityManager
     */
    public val connectivityManager: ConnectivityManager by lazy { context.getConnectivityManager() }
    
    /**
     * WifiController for WiFi-specific operations
     * WiFi 전용 작업을 위한 WifiController
     */
    public val wifiController: WifiController by lazy { WifiController(context) }

    // =================================================
    // Network Callback Management
    // =================================================
    
    /**
     * 일반 네트워크 상태 콜백
     * General network state callback
     */
    private var networkCallBack: NetworkStateCallback? = null

    /**
     * 기본 네트워크 상태 콜백  
     * Default network state callback
     */
    private var networkDefaultCallback: NetworkStateCallback? = null

    // =================================================
    // Basic Network Connectivity / 기본 네트워크 연결성
    // =================================================
    
    /**
     * 네트워크 연결 여부 확인
     * Check if network is connected
     * 
     * @return Boolean - 네트워크가 연결되어 있으면 true
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
     * 현재 네트워크의 NetworkCapabilities 반환
     * Get NetworkCapabilities of current network
     * 
     * @return NetworkCapabilities? - 현재 활성 네트워크의 능력 또는 null
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getNetworkCapabilities(): NetworkCapabilities? = safeCatch(defaultValue = null) {
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    }

    /**
     * 현재 네트워크의 LinkProperties 반환
     * Get LinkProperties of current network
     * 
     * @return LinkProperties? - 현재 활성 네트워크의 링크 속성 또는 null
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getLinkProperties(): LinkProperties? = safeCatch(defaultValue = null) {
        connectivityManager.getLinkProperties(connectivityManager.activeNetwork)
    }

    // =================================================
    // Transport Type Connectivity / 전송 타입별 연결성
    // =================================================
    
    /**
     * WiFi 연결 여부 확인
     * Check if connected via WiFi
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedWifi(): Boolean = 
        getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false

    /**
     * 모바일 네트워크 연결 여부 확인
     * Check if connected via mobile network
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedMobile(): Boolean = 
        getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false

    /**
     * VPN 연결 여부 확인
     * Check if connected via VPN
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedVPN(): Boolean = 
        getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false

    /**
     * 블루투스 연결 여부 확인
     * Check if connected via Bluetooth
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedBluetooth(): Boolean = 
        getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) ?: false

    /**
     * WiFi Aware 연결 여부 확인
     * Check if connected via WiFi Aware
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedWifiAware(): Boolean = 
        getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE) ?: false

    /**
     * 이더넷 연결 여부 확인
     * Check if connected via Ethernet
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedEthernet(): Boolean = 
        getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ?: false

    /**
     * LowPan 연결 여부 확인
     * Check if connected via LowPan
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedLowPan(): Boolean = 
        getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN) ?: false

    /**
     * USB 연결 여부 확인 (API 31+)
     * Check if connected via USB (API 31+)
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    @RequiresApi(Build.VERSION_CODES.S)
    public fun isConnectedUSB(): Boolean = 
        getNetworkCapabilities()?.hasTransport(NetworkCapabilities.TRANSPORT_USB) ?: false

    // =================================================
    // WiFi State / WiFi 상태
    // =================================================
    
    /**
     * WiFi 활성화 여부 확인
     * Check if WiFi is enabled
     */
    @RequiresPermission(Manifest.permission.ACCESS_WIFI_STATE)
    public fun isWifiEnabled(): Boolean = wifiController.isWifiEnabled()

    // =================================================
    // Network Callback Management / 네트워크 콜백 관리
    // =================================================
    
    /**
     * 일반 네트워크 상태 콜백 등록
     * Register general network state callback
     * 
     * @param handler 콜백 실행을 위한 핸들러 (null이면 메인 스레드)
     * @param onNetworkAvailable 네트워크 연결 시 호출
     * @param onNetworkLosing 네트워크 끊어질 예정 시 호출
     * @param onNetworkLost 네트워크 끊어짐 시 호출
     * @param onUnavailable 네트워크 사용 불가 시 호출
     * @param onNetworkCapabilitiesChanged 네트워크 능력 변경 시 호출
     * @param onLinkPropertiesChanged 링크 속성 변경 시 호출
     * @param onBlockedStatusChanged 차단 상태 변경 시 호출
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
     * 기본 네트워크 상태 콜백 등록
     * Register default network state callback
     * 
     * @param handler 콜백 실행을 위한 핸들러 (null이면 메인 스레드)
     * @param onNetworkAvailable 네트워크 연결 시 호출
     * @param onNetworkLosing 네트워크 끊어질 예정 시 호출
     * @param onNetworkLost 네트워크 끊어짐 시 호출
     * @param onUnavailable 네트워크 사용 불가 시 호출
     * @param onNetworkCapabilitiesChanged 네트워크 능력 변경 시 호출
     * @param onLinkPropertiesChanged 링크 속성 변경 시 호출
     * @param onBlockedStatusChanged 차단 상태 변경 시 호출
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
     * 일반 네트워크 콜백 해제
     * Unregister general network callback
     */
    public fun unregisterNetworkCallback() {
        networkCallBack?.let {
            connectivityManager.unregisterNetworkCallback(it)
        }
        networkCallBack = null
    }

    /**
     * 기본 네트워크 콜백 해제
     * Unregister default network callback
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
     * 네트워크 연결 상태 요약 정보 반환
     * Get network connectivity summary
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
     * 네트워크 연결성 요약 정보
     * Network connectivity summary information
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
     * 네트워크 타입별 IP 주소 가져오기
     *
     * 사용 법
     *
     * getIPAddressByNetworkType(NetworkCapabilities.TRANSPORT_ETHERNET)
     * getIPAddressByNetworkType(NetworkCapabilities.TRANSPORT_WIFI)
     * getIPAddressByNetworkType(NetworkCapabilities.TRANSPORT_CELLULAR)
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    fun getIPAddressByNetworkType(type:Int): String? {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        capabilities?.let {
            return if(it.hasTransport(type)) {
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
            } else null
        }?: return null
    }
}