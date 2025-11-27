package kr.open.library.simple_ui.core.system_manager.controller.wifi

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_NETWORK_STATE
import android.Manifest.permission.ACCESS_WIFI_STATE
import android.Manifest.permission.CHANGE_WIFI_STATE
import android.content.Context
import android.net.DhcpInfo
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.controller.wifi.internal.WifiCapabilityChecker
import kr.open.library.simple_ui.core.system_manager.controller.wifi.internal.WifiConnectionInfoProvider
import kr.open.library.simple_ui.core.system_manager.controller.wifi.internal.WifiOperationGuard
import kr.open.library.simple_ui.core.system_manager.extensions.getConnectivityManager
import kr.open.library.simple_ui.core.system_manager.extensions.getWifiManager

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
    public val connectivityManager by lazy { context.getConnectivityManager() }

    private val operationGuard = WifiOperationGuard { defaultValue, block -> tryCatchSystemManager(defaultValue) { block() } }

    private val connectionInfoProvider by lazy {
        WifiConnectionInfoProvider(wifiManager, connectivityManager, operationGuard)
    }
    private val capabilityChecker by lazy { WifiCapabilityChecker(wifiManager, operationGuard) }

    /**
     * WiFi 활성화 여부를 확인합니다.
     * Checks if WiFi is enabled.
     */
    @RequiresPermission(ACCESS_WIFI_STATE)
    public fun isWifiEnabled(): Boolean = tryCatchSystemManager(false) { wifiManager.isWifiEnabled }

    /**
     * WiFi 상태를 가져옵니다.
     * Gets the WiFi state.
     */
    @RequiresPermission(ACCESS_WIFI_STATE)
    public fun getWifiState(): Int = tryCatchSystemManager(WifiManager.WIFI_STATE_UNKNOWN) { wifiManager.wifiState }

    /**
     * WiFi를 활성화 또는 비활성화합니다.
     * Enables or disables WiFi.
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
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getConnectionInfo(): WifiInfo? = connectionInfoProvider.getConnectionInfo()

    /**
     * DHCP 정보를 가져옵니다.
     * Gets DHCP information.
     */
    @RequiresPermission(ACCESS_WIFI_STATE)
    public fun getDhcpInfo(): DhcpInfo? = tryCatchSystemManager(null) {
        wifiManager.dhcpInfo
    }

    /**
     * WiFi 스캔을 시작합니다.
     * Starts WiFi scanning.
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    public fun startScan(): Boolean = tryCatchSystemManager(false) {
        @Suppress("DEPRECATION")
        wifiManager.startScan()
    }

    /**
     * WiFi 스캔 결과를 가져옵니다.
     * Gets WiFi scan results.
     */
    @RequiresPermission(allOf = [ACCESS_WIFI_STATE, ACCESS_FINE_LOCATION])
    public fun getScanResults(): List<ScanResult> = tryCatchSystemManager(emptyList()) {
        wifiManager.scanResults ?: emptyList()
    }

    /**
     * 저장된 네트워크 목록을 가져옵니다 (API 29 이하).
     * Gets configured networks list (API 29 and below).
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
     * WiFi 연결 상태를 확인합니다.
     * Checks if WiFi is connected.
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedWifi(): Boolean = tryCatchSystemManager(false) {
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
    }

    /**
     * 신호 강도를 계산합니다.
     * Calculates signal level.
     */
    public fun calculateSignalLevel(rssi: Int, numLevels: Int): Int =
        tryCatchSystemManager(0) { WifiManager.calculateSignalLevel(rssi, numLevels) }

    /**
     * 두 신호 강도를 비교합니다.
     * Compares two signal strengths.
     */
    public fun compareSignalLevel(rssiA: Int, rssiB: Int): Int =
        tryCatchSystemManager(0) { WifiManager.compareSignalLevel(rssiA, rssiB) }

    /**
     * 5GHz 밴드 지원 여부를 확인합니다.
     * Checks if 5GHz band is supported.
     */
    public fun is5GHzBandSupported(): Boolean = capabilityChecker.is5GHzBandSupported()

    /**
     * 6GHz 밴드 지원 여부를 확인합니다 (WiFi 6E).
     * Checks if 6GHz band is supported (WiFi 6E).
     */
    @RequiresApi(Build.VERSION_CODES.R)
    public fun is6GHzBandSupported(): Boolean = capabilityChecker.is6GHzBandSupported()

    /**
     * WPA3 SAE 지원 여부를 확인합니다.
     * Checks if WPA3 SAE is supported.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    public fun isWpa3SaeSupported(): Boolean = capabilityChecker.isWpa3SaeSupported()

    /**
     * Enhanced Open 지원 여부를 확인합니다.
     * Checks if Enhanced Open is supported.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    public fun isEnhancedOpenSupported(): Boolean = capabilityChecker.isEnhancedOpenSupported()

    /**
     * WiFi 재연결을 시도합니다.
     * Attempts to reconnect WiFi.
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    public fun reconnect(): Boolean = tryCatchSystemManager(false) { wifiManager.reconnect() }

    /**
     * WiFi 재협상을 시도합니다.
     * Attempts to reassociate WiFi.
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    public fun reassociate(): Boolean = tryCatchSystemManager(false) { wifiManager.reassociate() }

    /**
     * WiFi 연결을 해제합니다.
     * Disconnects WiFi.
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    public fun disconnect(): Boolean = tryCatchSystemManager(false) { wifiManager.disconnect() }

    /**
     * 현재 연결된 WiFi의 SSID를 가져옵니다.
     * Gets the SSID of currently connected WiFi.
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getCurrentSsid(): String? = tryCatchSystemManager(null) {
        getConnectionInfo()?.ssid?.removeSurrounding("\"")
    }

    /**
     * 현재 연결된 WiFi의 BSSID를 가져옵니다.
     * Gets the BSSID of currently connected WiFi.
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getCurrentBssid(): String? = tryCatchSystemManager(null) {
        getConnectionInfo()?.bssid
    }

    /**
     * 현재 연결된 WiFi의 신호 강도를 가져옵니다.
     * Gets the signal strength of currently connected WiFi.
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getCurrentRssi(): Int = tryCatchSystemManager(-127) {
        getConnectionInfo()?.rssi ?: -127
    }

    /**
     * 현재 연결된 WiFi의 링크 속도를 가져옵니다.
     * Gets the link speed of current WiFi connection.
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getCurrentLinkSpeed(): Int = tryCatchSystemManager(0) {
        getConnectionInfo()?.linkSpeed ?: 0
    }

    /**
     * 현대적인 접근 방식으로 WiFi 네트워크 세부 정보를 가져옵니다.
     * Gets detailed WiFi network information using modern approach.
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getModernNetworkDetails(): WifiNetworkDetails? =
        connectionInfoProvider.getModernNetworkDetails()

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
