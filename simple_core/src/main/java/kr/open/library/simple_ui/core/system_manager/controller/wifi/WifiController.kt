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
 * Controller for querying and controlling WiFi state information.<br><br>
 * WiFi 상태 정보 조회 및 제어 컨트롤러입니다.<br>
 *
 * Main Features:<br>
 * - WiFi state information query<br>
 * - WiFi connection info and network scanning<br>
 * - WiFi enable/disable control<br>
 * - Network configuration management<br><br>
 * 주요 기능:<br>
 * - WiFi 상태 정보 조회<br>
 * - WiFi 연결 정보 및 네트워크 스캔<br>
 * - WiFi 켜기/끄기 제어<br>
 * - 네트워크 구성 관리<br>
 *
 * Required Permissions:<br>
 * - `android.permission.ACCESS_WIFI_STATE` (WiFi state query)<br>
 * - `android.permission.CHANGE_WIFI_STATE` (WiFi control)<br>
 * - `android.permission.ACCESS_FINE_LOCATION` (Scan results, API 23+)<br><br>
 * 필수 권한:<br>
 * - `android.permission.ACCESS_WIFI_STATE` (WiFi 상태 조회)<br>
 * - `android.permission.CHANGE_WIFI_STATE` (WiFi 제어)<br>
 * - `android.permission.ACCESS_FINE_LOCATION` (스캔 결과 조회, API 23+)<br>
 *
 * @param context The application context.<br><br>
 *                애플리케이션 컨텍스트.
 */
public open class WifiController(
    context: Context,
) : BaseSystemService(
        context,
        listOf(ACCESS_WIFI_STATE, CHANGE_WIFI_STATE, ACCESS_FINE_LOCATION, ACCESS_NETWORK_STATE),
    ) {
    /**
     * Lazy-initialized WifiManager instance.<br><br>
     * 지연 초기화된 WifiManager 인스턴스입니다.<br>
     */
    public val wifiManager: WifiManager by lazy { context.getWifiManager() }

    /**
     * Lazy-initialized ConnectivityManager instance.<br><br>
     * 지연 초기화된 ConnectivityManager 인스턴스입니다.<br>
     */
    public val connectivityManager by lazy { context.getConnectivityManager() }

    /**
     * Operation guard for safe WiFi operation execution.<br><br>
     * 안전한 WiFi 작업 실행을 위한 작업 가드입니다.<br>
     */
    private val operationGuard = WifiOperationGuard { defaultValue, block ->
        tryCatchSystemManager(defaultValue) { block() }
    }

    /**
     * Provider for WiFi connection information.<br><br>
     * WiFi 연결 정보 제공자입니다.<br>
     */
    private val connectionInfoProvider by lazy {
        WifiConnectionInfoProvider(wifiManager, connectivityManager, operationGuard)
    }

    /**
     * Checker for WiFi capability support.<br><br>
     * WiFi 기능 지원 확인자입니다.<br>
     */
    private val capabilityChecker by lazy { WifiCapabilityChecker(wifiManager, operationGuard) }

    /**
     * Checks if WiFi is enabled.<br><br>
     * WiFi 활성화 여부를 확인합니다.<br>
     *
     * @return `true` if WiFi is enabled, `false` otherwise.<br><br>
     *         WiFi가 활성화되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(ACCESS_WIFI_STATE)
    public fun isWifiEnabled(): Boolean = tryCatchSystemManager(false) { return wifiManager.isWifiEnabled }

    /**
     * Gets the current WiFi state.<br><br>
     * WiFi 상태를 가져옵니다.<br>
     *
     * @return WiFi state constant (WIFI_STATE_ENABLED, WIFI_STATE_DISABLED, etc.).<br><br>
     *         WiFi 상태 상수 (WIFI_STATE_ENABLED, WIFI_STATE_DISABLED 등).<br>
     */
    @RequiresPermission(ACCESS_WIFI_STATE)
    public fun getWifiState(): Int = tryCatchSystemManager(WifiManager.WIFI_STATE_UNKNOWN) { return wifiManager.wifiState }

    /**
     * Enables or disables WiFi.<br>
     * Note: Deprecated on Android Q (API 29+), user must enable manually.<br><br>
     * WiFi를 활성화 또는 비활성화합니다.<br>
     * 참고: Android Q (API 29+)에서 더 이상 사용되지 않으며, 사용자가 수동으로 활성화해야 합니다.<br>
     *
     * @param enabled `true` to enable WiFi, `false` to disable.<br><br>
     *                WiFi를 활성화하려면 `true`, 비활성화하려면 `false`.
     * @return `true` if operation succeeded, `false` otherwise or on API 29+.<br><br>
     *         작업 성공 시 `true`, 실패 또는 API 29+ 에서 `false`.<br>
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    public fun setWifiEnabled(enabled: Boolean): Boolean = tryCatchSystemManager(false) {
        return checkSdkVersion(
            Build.VERSION_CODES.Q,
            positiveWork = {
                Logx.w("WiFi control deprecated on API 29+, user must enable manually")
                false
            },
            negativeWork = {
                @Suppress("DEPRECATION")
                wifiManager.setWifiEnabled(enabled)
            },
        )
    }

    /**
     * Gets current WiFi connection information.<br><br>
     * 현재 WiFi 연결 정보를 가져옵니다.<br>
     *
     * @return WifiInfo object or null if not connected.<br><br>
     *         WifiInfo 객체 또는 연결되지 않은 경우 null.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getConnectionInfo(): WifiInfo? = connectionInfoProvider.getConnectionInfo()

    /**
     * Gets DHCP information.<br><br>
     * DHCP 정보를 가져옵니다.<br>
     *
     * @return DhcpInfo object or null if not available.<br><br>
     *         DhcpInfo 객체 또는 사용 불가능한 경우 null.<br>
     */
    @RequiresPermission(ACCESS_WIFI_STATE)
    public fun getDhcpInfo(): DhcpInfo? = tryCatchSystemManager(null) {
        return wifiManager.dhcpInfo
    }

    /**
     * Starts WiFi network scanning.<br><br>
     * WiFi 네트워크 스캔을 시작합니다.<br>
     *
     * @return `true` if scan was initiated successfully, `false` otherwise.<br><br>
     *         스캔이 성공적으로 시작되면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    public fun startScan(): Boolean = tryCatchSystemManager(false) {
        @Suppress("DEPRECATION")
        return wifiManager.startScan()
    }

    /**
     * Gets WiFi scan results.<br><br>
     * WiFi 스캔 결과를 가져옵니다.<br>
     *
     * @return List of ScanResult objects, empty list if none available.<br><br>
     *         ScanResult 객체 목록, 사용 불가능한 경우 빈 목록.<br>
     */
    @RequiresPermission(allOf = [ACCESS_WIFI_STATE, ACCESS_FINE_LOCATION])
    public fun getScanResults(): List<ScanResult> = tryCatchSystemManager(emptyList()) {
        return wifiManager.scanResults ?: emptyList()
    }

    /**
     * Gets list of configured (saved) networks.<br>
     * Note: Deprecated on Android Q (API 29+), use WiFi suggestion API instead.<br><br>
     * 저장된 네트워크 목록을 가져옵니다.<br>
     * 참고: Android Q (API 29+)에서 더 이상 사용되지 않으며, WiFi suggestion API를 사용하세요.<br>
     *
     * @return List of WifiConfiguration objects, empty list on API 29+ or if none available.<br><br>
     *         WifiConfiguration 객체 목록, API 29+ 또는 사용 불가능한 경우 빈 목록.<br>
     */
    @RequiresPermission(allOf = [ACCESS_WIFI_STATE, ACCESS_FINE_LOCATION])
    public fun getConfiguredNetworks(): List<WifiConfiguration> = tryCatchSystemManager(emptyList()) {
        return checkSdkVersion(
            Build.VERSION_CODES.Q,
            positiveWork = {
                Logx.w("getConfiguredNetworks deprecated on API 29+, use WiFi suggestion API")
                emptyList()
            },
            negativeWork = {
                @Suppress("DEPRECATION")
                wifiManager.configuredNetworks ?: emptyList()
            },
        )
    }

    /**
     * Checks if WiFi is currently connected.<br><br>
     * WiFi 연결 상태를 확인합니다.<br>
     *
     * @return `true` if connected to WiFi, `false` otherwise.<br><br>
     *         WiFi에 연결되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun isConnectedWifi(): Boolean = tryCatchSystemManager(false) {
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
    }

    /**
     * Calculates signal level from RSSI value.<br><br>
     * RSSI 값으로부터 신호 강도를 계산합니다.<br>
     *
     * @param rssi The RSSI value in dBm.<br><br>
     *             dBm 단위의 RSSI 값.
     * @param numLevels Number of signal levels (e.g., 5 for 0-4 range).<br><br>
     *                  신호 레벨 수 (예: 0-4 범위의 경우 5).
     * @return Signal level from 0 to numLevels-1.<br><br>
     *         0부터 numLevels-1까지의 신호 레벨.<br>
     */
    public fun calculateSignalLevel(rssi: Int, numLevels: Int): Int = tryCatchSystemManager(0) {
        return WifiManager.calculateSignalLevel(rssi, numLevels)
    }

    /**
     * Compares two signal strengths.<br><br>
     * 두 신호 강도를 비교합니다.<br>
     *
     * @param rssiA First RSSI value in dBm.<br><br>
     *              첫 번째 RSSI 값 (dBm).
     * @param rssiB Second RSSI value in dBm.<br><br>
     *              두 번째 RSSI 값 (dBm).
     * @return Negative if rssiA is weaker, positive if stronger, 0 if equal.<br><br>
     *         rssiA가 약하면 음수, 강하면 양수, 같으면 0.<br>
     */
    public fun compareSignalLevel(rssiA: Int, rssiB: Int): Int = tryCatchSystemManager(0) {
        return WifiManager.compareSignalLevel(rssiA, rssiB)
    }

    /**
     * Checks if 5GHz WiFi band is supported.<br><br>
     * 5GHz WiFi 밴드 지원 여부를 확인합니다.<br>
     *
     * @return `true` if 5GHz band is supported, `false` otherwise.<br><br>
     *         5GHz 밴드가 지원되면 `true`, 그렇지 않으면 `false`.<br>
     */
    public fun is5GHzBandSupported(): Boolean = capabilityChecker.is5GHzBandSupported()

    /**
     * Checks if 6GHz WiFi band is supported (WiFi 6E).<br>
     * Requires Android R (API 30) or higher.<br><br>
     * 6GHz WiFi 밴드 지원 여부를 확인합니다 (WiFi 6E).<br>
     * Android R (API 30) 이상이 필요합니다.<br>
     *
     * @return `true` if 6GHz band is supported, `false` otherwise.<br><br>
     *         6GHz 밴드가 지원되면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresApi(Build.VERSION_CODES.R)
    public fun is6GHzBandSupported(): Boolean = capabilityChecker.is6GHzBandSupported()

    /**
     * Checks if WPA3 SAE (Simultaneous Authentication of Equals) is supported.<br>
     * Requires Android Q (API 29) or higher.<br><br>
     * WPA3 SAE (Simultaneous Authentication of Equals) 지원 여부를 확인합니다.<br>
     * Android Q (API 29) 이상이 필요합니다.<br>
     *
     * @return `true` if WPA3 SAE is supported, `false` otherwise.<br><br>
     *         WPA3 SAE가 지원되면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    public fun isWpa3SaeSupported(): Boolean = capabilityChecker.isWpa3SaeSupported()

    /**
     * Checks if Enhanced Open (OWE) is supported.<br>
     * Requires Android Q (API 29) or higher.<br><br>
     * Enhanced Open (OWE) 지원 여부를 확인합니다.<br>
     * Android Q (API 29) 이상이 필요합니다.<br>
     *
     * @return `true` if Enhanced Open is supported, `false` otherwise.<br><br>
     *         Enhanced Open이 지원되면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    public fun isEnhancedOpenSupported(): Boolean = capabilityChecker.isEnhancedOpenSupported()

    /**
     * Attempts to reconnect to WiFi.<br><br>
     * WiFi 재연결을 시도합니다.<br>
     *
     * @return `true` if reconnect was initiated successfully, `false` otherwise.<br><br>
     *         재연결이 성공적으로 시작되면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    public fun reconnect(): Boolean = tryCatchSystemManager(false) { return wifiManager.reconnect() }

    /**
     * Attempts to reassociate with WiFi access point.<br><br>
     * WiFi 액세스 포인트와 재협상을 시도합니다.<br>
     *
     * @return `true` if reassociate was initiated successfully, `false` otherwise.<br><br>
     *         재협상이 성공적으로 시작되면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    public fun reassociate(): Boolean = tryCatchSystemManager(false) { return wifiManager.reassociate() }

    /**
     * Disconnects from current WiFi network.<br><br>
     * 현재 WiFi 네트워크로부터 연결을 해제합니다.<br>
     *
     * @return `true` if disconnect was initiated successfully, `false` otherwise.<br><br>
     *         연결 해제가 성공적으로 시작되면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    public fun disconnect(): Boolean = tryCatchSystemManager(false) { return wifiManager.disconnect() }

    /**
     * Gets the SSID of currently connected WiFi network.<br><br>
     * 현재 연결된 WiFi 네트워크의 SSID를 가져옵니다.<br>
     *
     * @return SSID string without quotes, or null if not connected.<br><br>
     *         따옴표가 제거된 SSID 문자열, 연결되지 않은 경우 null.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getCurrentSsid(): String? = tryCatchSystemManager(null) {
        return getConnectionInfo()?.ssid?.removeSurrounding("\"")
    }

    /**
     * Gets the BSSID of currently connected WiFi network.<br><br>
     * 현재 연결된 WiFi 네트워크의 BSSID를 가져옵니다.<br>
     *
     * @return BSSID string (MAC address), or null if not connected.<br><br>
     *         BSSID 문자열 (MAC 주소), 연결되지 않은 경우 null.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getCurrentBssid(): String? = tryCatchSystemManager(null) {
        return getConnectionInfo()?.bssid
    }

    /**
     * Gets the signal strength (RSSI) of currently connected WiFi.<br><br>
     * 현재 연결된 WiFi의 신호 강도(RSSI)를 가져옵니다.<br>
     *
     * @return RSSI value in dBm, or -127 if not connected.<br><br>
     *         dBm 단위의 RSSI 값, 연결되지 않은 경우 -127.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getCurrentRssi(): Int = tryCatchSystemManager(-127) {
        return getConnectionInfo()?.rssi ?: -127
    }

    /**
     * Gets the link speed of current WiFi connection.<br><br>
     * 현재 WiFi 연결의 링크 속도를 가져옵니다.<br>
     *
     * @return Link speed in Mbps, or 0 if not connected.<br><br>
     *         Mbps 단위의 링크 속도, 연결되지 않은 경우 0.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getCurrentLinkSpeed(): Int = tryCatchSystemManager(0) {
        return getConnectionInfo()?.linkSpeed ?: 0
    }

    /**
     * Gets detailed WiFi network information using modern API approach.<br>
     * Available on Android Q (API 29) and higher.<br><br>
     * 현대적인 API 접근 방식으로 WiFi 네트워크 세부 정보를 가져옵니다.<br>
     * Android Q (API 29) 이상에서 사용 가능합니다.<br>
     *
     * @return WifiNetworkDetails object or null if not available.<br><br>
     *         WifiNetworkDetails 객체 또는 사용 불가능한 경우 null.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getModernNetworkDetails(): WifiNetworkDetails? = connectionInfoProvider.getModernNetworkDetails()

    /**
     * Cleans up all resources of WiFi controller.<br>
     * Should be called when the controller is no longer needed.<br><br>
     * WiFi 컨트롤러의 모든 리소스를 정리합니다.<br>
     * 컨트롤러가 더 이상 필요하지 않을 때 호출해야 합니다.<br>
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
