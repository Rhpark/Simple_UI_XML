package kr.open.library.simple_ui.system_manager.controller.wifi

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_NETWORK_STATE
import android.Manifest.permission.ACCESS_WIFI_STATE
import android.Manifest.permission.CHANGE_WIFI_STATE
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.DhcpInfo
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import kr.open.library.simple_ui.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.system_manager.extensions.getConnectivityManager
import kr.open.library.simple_ui.system_manager.extensions.getWifiManager


/**
 * WifiController - WiFi 상태 정보 조회 및 제어 컨트롤러
 * WiFi State Information Query and Control Controller
 * 
 * WiFi 상태 조회, 네트워크 스캔, WiFi 켜기/끄기 등의 기능을 제공합니다.
 * Provides WiFi state query, network scanning, WiFi enable/disable functionality.
 * 
 * 주요 기능 / Main Features:
 * - WiFi 상태 정보 조회 / WiFi state information query
 * - WiFi 연결 정보 및 네트워크 스캔 / WiFi connection info and network scanning  
 * - WiFi 켜기/끄기 제어 / WiFi enable/disable control
 * - 네트워크 구성 관리 / Network configuration management
 * 
 * 필수 권한 / Required Permissions:
 * - android.permission.ACCESS_WIFI_STATE (WiFi 상태 조회)
 * - android.permission.CHANGE_WIFI_STATE (WiFi 제어)
 * - android.permission.ACCESS_FINE_LOCATION (스캔 결과 조회, API 23+)
 */
public open class WifiController(context: Context) : BaseSystemService(
    context,
    listOf(ACCESS_WIFI_STATE, CHANGE_WIFI_STATE, ACCESS_FINE_LOCATION, ACCESS_NETWORK_STATE)
) {

    public val wifiManager: WifiManager by lazy { context.getWifiManager() }
    private val connectivityManager by lazy { context.getConnectivityManager() }

    /**
     * WiFi 활성화 여부를 확인합니다.
     * Checks if WiFi is enabled.
     * 
     * @return WiFi 활성화 상태 / WiFi enabled status
     */
    @RequiresPermission(ACCESS_WIFI_STATE)
    public fun isWifiEnabled(): Boolean = tryCatchSystemManager(false) {
        wifiManager.isWifiEnabled
    }


    /**
     * WiFi 상태를 가져옵니다.
     * Gets the WiFi state.
     * 
     * @return WiFi 상태 코드 (DISABLED, ENABLING, ENABLED, DISABLING, UNKNOWN)
     */
    @RequiresPermission(ACCESS_WIFI_STATE)
    public fun getWifiState(): Int = tryCatchSystemManager(WifiManager.WIFI_STATE_UNKNOWN) {
        wifiManager.wifiState
    }


    /**
     * WiFi를 활성화 또는 비활성화합니다.
     * Enables or disables WiFi.
     * 
     * @param enabled true면 활성화, false면 비활성화 / true to enable, false to disable
     * @return 설정 성공 여부 / Setting success status
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    public fun setWifiEnabled(enabled: Boolean): Boolean = tryCatchSystemManager(false) {
        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = {
                Logx.w("WiFi control deprecated on API 29+, user must enable manually")
                false
            },
            negativeWork = {
                @Suppress("DEPRECATION")
                wifiManager.setWifiEnabled(enabled)
            }
        )
    }


    /**
     * 현재 WiFi 연결 정보를 가져옵니다.
     * Gets current WiFi connection information.
     * 
     * @return WiFi 연결 정보 또는 null / WiFi connection info or null
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getConnectionInfo(): WifiInfo? = tryCatchSystemManager(null) {
        checkSdkVersion(Build.VERSION_CODES.S,
            positiveWork = {
                getConnectionInfoFromNetworkCapabilities()
            },
            negativeWork = {
                @Suppress("DEPRECATION")
                wifiManager.connectionInfo
            }
        )
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    private fun getConnectionInfoFromNetworkCapabilities(): WifiInfo? = tryCatchSystemManager(null) {
        val network = connectivityManager.activeNetwork ?: return@tryCatchSystemManager null
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return@tryCatchSystemManager null
        if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return@tryCatchSystemManager null

        // API 31+ : NetworkCapabilities.transportInfo 에서 WifiInfo 추출
        checkSdkVersion(Build.VERSION_CODES.S,
            positiveWork = {
                val info = caps.transportInfo as? WifiInfo ?: return@tryCatchSystemManager null
                // SSID가 비공개/권한 미흡 등으로 숨겨질 수 있음 → UNKNOWN 가드
                info.takeUnless { it.ssid == WifiManager.UNKNOWN_SSID }
            },
            negativeWork = {
                @Suppress("DEPRECATION")
                wifiManager.connectionInfo?.takeUnless { it.ssid == WifiManager.UNKNOWN_SSID }
            }
        )
    }



    /**
     * DHCP 정보를 가져옵니다.
     * Gets DHCP information.
     * 
     * @return DHCP 정보 또는 null / DHCP info or null
     */
    @RequiresPermission(ACCESS_WIFI_STATE)
    public fun getDhcpInfo(): DhcpInfo? = tryCatchSystemManager(null) {
        wifiManager.dhcpInfo
    }

    /**
     * WiFi 스캔을 시작합니다.
     * Starts WiFi scanning.
     * 
     * @return 스캔 시작 성공 여부 / Scan start success status
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    public fun startScan(): Boolean = tryCatchSystemManager(false) {
        @Suppress("DEPRECATION")
        wifiManager.startScan()
    }


    /**
     * WiFi 스캔 결과를 가져옵니다.
     * Gets WiFi scan results.
     * 
     * @return 스캔 결과 목록 / List of scan results
     */
    @RequiresPermission(allOf = [ACCESS_WIFI_STATE, ACCESS_FINE_LOCATION])
    public fun getScanResults(): List<ScanResult> = tryCatchSystemManager(emptyList()) {
        wifiManager.scanResults ?: emptyList()
    }


    /**
     * 설정된 네트워크 목록을 가져옵니다 (API 29 이하).
     * Gets configured networks list (API 29 and below).
     * 
     * @return 설정된 네트워크 목록 / List of configured networks
     */
    @RequiresPermission(allOf = [ACCESS_WIFI_STATE, ACCESS_FINE_LOCATION])
    public fun getConfiguredNetworks(): List<WifiConfiguration> = tryCatchSystemManager(emptyList()) {
        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = {
                Logx.w("getConfiguredNetworks deprecated on API 29+, use WiFi suggestion API")
                emptyList()
            },
            negativeWork = {
                @Suppress("DEPRECATION")
                wifiManager.configuredNetworks ?: emptyList()
            }
        )
    }


    /**
     * WiFi 연결 여부를 확인합니다.
     * Checks if WiFi is connected.
     * 
     * @return WiFi 연결 상태 / WiFi connection status
     */
    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    public fun isConnectedWifi(): Boolean = tryCatchSystemManager(false) {
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
    }


    /**
     * 신호 강도를 레벨로 변환합니다.
     * Converts signal strength to level.
     * 
     * @param rssi 신호 강도 (dBm) / Signal strength in dBm
     * @param numLevels 레벨 수 (일반적으로 4 또는 5) / Number of levels (typically 4 or 5)
     * @return 신호 레벨 (0부터 numLevels-1) / Signal level (0 to numLevels-1)
     */
//    @Deprecated("_DEPRECATED_")
    public fun calculateSignalLevel(rssi: Int, numLevels: Int): Int = tryCatchSystemManager(0) {
        WifiManager.calculateSignalLevel(rssi, numLevels)
    }

    /**
     * 두 신호 강도를 비교합니다.
     * Compares two signal strengths.
     * 
     * @param rssiA 첫 번째 신호 강도 / First signal strength
     * @param rssiB 두 번째 신호 강도 / Second signal strength  
     * @return 비교 결과 (-1, 0, 1) / Comparison result (-1, 0, 1)
     */
    public fun compareSignalLevel(rssiA: Int, rssiB: Int): Int = tryCatchSystemManager(0) {
        WifiManager.compareSignalLevel(rssiA, rssiB)
    }

    /**
     * 5GHz 대역 지원 여부를 확인합니다.
     * Checks if 5GHz band is supported.
     * 
     * @return 5GHz 대역 지원 여부 / 5GHz band support status
     */
    public fun is5GHzBandSupported(): Boolean = tryCatchSystemManager(false) {
        wifiManager.is5GHzBandSupported
    }

    /**
     * 6GHz 대역 지원 여부를 확인합니다 (WiFi 6E).
     * Checks if 6GHz band is supported (WiFi 6E).
     * 
     * @return 6GHz 대역 지원 여부 / 6GHz band support status
     */
    @RequiresApi(Build.VERSION_CODES.R)
    public fun is6GHzBandSupported(): Boolean = tryCatchSystemManager(false) {
        checkSdkVersion(Build.VERSION_CODES.R,
            positiveWork = { wifiManager.is6GHzBandSupported },
            negativeWork = { false }
        )
    }

    /**
     * WPA3 SAE 지원 여부를 확인합니다.
     * Checks if WPA3 SAE is supported.
     * 
     * @return WPA3 SAE 지원 여부 / WPA3 SAE support status
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    public fun isWpa3SaeSupported(): Boolean = tryCatchSystemManager(false) {
        checkSdkVersion(Build.VERSION_CODES.R,
            positiveWork = { wifiManager.isWpa3SaeSupported },
            negativeWork = { false }
        )
    }

    /**
     * Enhanced Open 지원 여부를 확인합니다.
     * Checks if Enhanced Open is supported.
     * 
     * @return Enhanced Open 지원 여부 / Enhanced Open support status
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    public fun isEnhancedOpenSupported(): Boolean = tryCatchSystemManager(false) {
        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = { wifiManager.isEnhancedOpenSupported },
            negativeWork = { false }
        )
    }

    /**
     * WiFi 재연결을 시도합니다.
     * Attempts to reconnect WiFi.
     * 
     * @return 재연결 시도 성공 여부 / Reconnection attempt success status
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    public fun reconnect(): Boolean = tryCatchSystemManager(false) {
        wifiManager.reconnect()
    }

    /**
     * WiFi 재결합을 시도합니다.
     * Attempts to reassociate WiFi.
     * 
     * @return 재결합 시도 성공 여부 / Reassociation attempt success status
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    public fun reassociate(): Boolean = tryCatchSystemManager(false) {
        wifiManager.reassociate()
    }

    /**
     * WiFi 연결을 해제합니다.
     * Disconnects WiFi.
     * 
     * @return 연결 해제 성공 여부 / Disconnection success status
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    public fun disconnect(): Boolean = tryCatchSystemManager(false) {
        wifiManager.disconnect()
    }

    /**
     * 현재 연결된 WiFi의 SSID를 가져옵니다.
     * Gets the SSID of currently connected WiFi.
     * 
     * @return WiFi SSID 또는 null / WiFi SSID or null
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getCurrentSsid(): String? = tryCatchSystemManager(null) {
        val wifiInfo = getConnectionInfo()
        wifiInfo?.ssid?.removeSurrounding("\"")
    }

    /**
     * 현재 연결된 WiFi의 BSSID를 가져옵니다.
     * Gets the BSSID of currently connected WiFi.
     * 
     * @return WiFi BSSID 또는 null / WiFi BSSID or null
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getCurrentBssid(): String? = tryCatchSystemManager(null) {
        getConnectionInfo()?.bssid
    }

    /**
     * 현재 연결된 WiFi의 신호 강도를 가져옵니다.
     * Gets the signal strength of currently connected WiFi.
     * 
     * @return 신호 강도 (dBm) / Signal strength in dBm
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getCurrentRssi(): Int = tryCatchSystemManager(-127) {
        getConnectionInfo()?.rssi ?: -127
    }

    /**
     * 현재 WiFi 연결의 링크 속도를 가져옵니다.
     * Gets the link speed of current WiFi connection.
     * 
     * @return 링크 속도 (Mbps) / Link speed in Mbps
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getCurrentLinkSpeed(): Int = tryCatchSystemManager(0) {
        getConnectionInfo()?.linkSpeed ?: 0
    }

    /**
     * 현대적 방식으로 WiFi 네트워크 상세 정보를 가져옵니다.
     * Gets detailed WiFi network information using modern approach.
     * 
     * @return WifiNetworkDetails? WiFi 네트워크 상세 정보 / WiFi network details
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getModernNetworkDetails(): WifiNetworkDetails? = tryCatchSystemManager(null) {

        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork =  {
                val activeNetwork = connectivityManager.activeNetwork
                val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

                if (networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                    val linkProperties = connectivityManager.getLinkProperties(activeNetwork)

                    WifiNetworkDetails(
                        isConnected = true,
                        hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
                        isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
                        isMetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED).not(),
                        linkDownstreamBandwidthKbps = networkCapabilities.linkDownstreamBandwidthKbps,
                        linkUpstreamBandwidthKbps = networkCapabilities.linkUpstreamBandwidthKbps,
                        interfaceName = linkProperties?.interfaceName,
                        dnsServers = linkProperties?.dnsServers?.mapNotNull { it.hostAddress } ?: emptyList(),
                        domains = linkProperties?.domains
                    )
                } else { null }
            },
            negativeWork = { null }
        )
    }
    
    
    /**
     * WiFi 네트워크 상세 정보를 나타내는 데이터 클래스 (현대적 접근용)
     * Data class representing detailed WiFi network information (for modern approach)
     */
    data class WifiNetworkDetails(
        val isConnected: Boolean,
        val hasInternet: Boolean,
        val isValidated: Boolean,
        val isMetered: Boolean,
        val linkDownstreamBandwidthKbps: Int,
        val linkUpstreamBandwidthKbps: Int,
        val interfaceName: String?,
        val dnsServers: List<String>,
        val domains: String?
    )


    /**
     * WiFi 컨트롤러의 모든 리소스를 정리합니다.
     * Cleans up all resources of WiFi controller.
     */
    override fun onDestroy() {
        try {
            Logx.d("WifiController resources cleaned up")
        } catch (e: Exception) {
            Logx.e("Error during WifiController cleanup: ${e.message}")
        } finally {
            super.onDestroy()
        }
    }
}