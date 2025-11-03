package kr.open.library.simple_ui.system_manager.controller.wifi

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_NETWORK_STATE
import android.Manifest.permission.ACCESS_WIFI_STATE
import android.Manifest.permission.CHANGE_WIFI_STATE
import android.content.Context
import android.net.DhcpInfo
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.system_manager.controller.wifi.internal.WifiCapabilityChecker
import kr.open.library.simple_ui.system_manager.controller.wifi.internal.WifiConnectionCommander
import kr.open.library.simple_ui.system_manager.controller.wifi.internal.WifiConnectionInfoProvider
import kr.open.library.simple_ui.system_manager.controller.wifi.internal.WifiOperationGuard
import kr.open.library.simple_ui.system_manager.controller.wifi.internal.WifiStateController
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

    private val operationGuard = WifiOperationGuard { defaultValue, block ->
        guard(defaultValue, block)
    }
    private val stateController by lazy { WifiStateController(wifiManager, operationGuard) }
    private val connectionCommander by lazy { WifiConnectionCommander(wifiManager, operationGuard) }
    private val connectionInfoProvider by lazy {
        WifiConnectionInfoProvider(wifiManager, connectivityManager, operationGuard)
    }
    private val capabilityChecker by lazy { WifiCapabilityChecker(wifiManager, operationGuard) }

    private fun <T> guard(defaultValue: T, block: () -> T): T =
        tryCatchSystemManager(defaultValue, block)

    /**
     * WiFi 활성화 여부를 확인합니다.
     * Checks if WiFi is enabled.
     */
    @RequiresPermission(ACCESS_WIFI_STATE)
    public fun isWifiEnabled(): Boolean = stateController.isWifiEnabled()

    /**
     * WiFi 상태를 가져옵니다.
     * Gets the WiFi state.
     */
    @RequiresPermission(ACCESS_WIFI_STATE)
    public fun getWifiState(): Int = stateController.getWifiState()

    /**
     * WiFi를 활성화 또는 비활성화합니다.
     * Enables or disables WiFi.
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    public fun setWifiEnabled(enabled: Boolean): Boolean = stateController.setWifiEnabled(enabled)

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
    public fun getDhcpInfo(): DhcpInfo? = stateController.getDhcpInfo()

    /**
     * WiFi 스캔을 시작합니다.
     * Starts WiFi scanning.
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    public fun startScan(): Boolean = stateController.startScan()

    /**
     * WiFi 스캔 결과를 가져옵니다.
     * Gets WiFi scan results.
     */
    @RequiresPermission(allOf = [ACCESS_WIFI_STATE, ACCESS_FINE_LOCATION])
    public fun getScanResults(): List<ScanResult> = stateController.getScanResults()

    /**
     * 저장된 네트워크 목록을 가져옵니다 (API 29 이하).
     * Gets configured networks list (API 29 and below).
     */
    @RequiresPermission(allOf = [ACCESS_WIFI_STATE, ACCESS_FINE_LOCATION])
    public fun getConfiguredNetworks(): List<WifiConfiguration> = stateController.getConfiguredNetworks()

    /**
     * WiFi 연결 상태를 확인합니다.
     * Checks if WiFi is connected.
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedWifi(): Boolean = connectionInfoProvider.isConnectedWifi()

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
    public fun reconnect(): Boolean = connectionCommander.reconnect()

    /**
     * WiFi 재협상을 시도합니다.
     * Attempts to reassociate WiFi.
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    public fun reassociate(): Boolean = connectionCommander.reassociate()

    /**
     * WiFi 연결을 해제합니다.
     * Disconnects WiFi.
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    public fun disconnect(): Boolean = connectionCommander.disconnect()

    /**
     * 현재 연결된 WiFi의 SSID를 가져옵니다.
     * Gets the SSID of currently connected WiFi.
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getCurrentSsid(): String? = connectionInfoProvider.getCurrentSsid()

    /**
     * 현재 연결된 WiFi의 BSSID를 가져옵니다.
     * Gets the BSSID of currently connected WiFi.
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getCurrentBssid(): String? = connectionInfoProvider.getCurrentBssid()

    /**
     * 현재 연결된 WiFi의 신호 강도를 가져옵니다.
     * Gets the signal strength of currently connected WiFi.
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getCurrentRssi(): Int = connectionInfoProvider.getCurrentRssi()

    /**
     * 현재 연결된 WiFi의 링크 속도를 가져옵니다.
     * Gets the link speed of current WiFi connection.
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getCurrentLinkSpeed(): Int = connectionInfoProvider.getCurrentLinkSpeed()

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
