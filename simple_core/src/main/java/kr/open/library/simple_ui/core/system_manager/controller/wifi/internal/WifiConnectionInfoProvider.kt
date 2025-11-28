package kr.open.library.simple_ui.core.system_manager.controller.wifi.internal

import WifiNetworkDetails
import android.Manifest.permission.ACCESS_NETWORK_STATE
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresPermission
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion

/**
 * Internal class for providing WiFi connection information.<br><br>
 * WiFi 연결 정보를 제공하는 내부 클래스입니다.<br>
 *
 * @param wifiManager WifiManager instance.<br><br>
 *                    WifiManager 인스턴스.
 * @param connectivityManager ConnectivityManager instance.<br><br>
 *                            ConnectivityManager 인스턴스.
 * @param guard Operation guard for safe execution.<br><br>
 *              안전한 실행을 위한 작업 가드.
 */
internal class WifiConnectionInfoProvider(
    private val wifiManager: WifiManager,
    private val connectivityManager: android.net.ConnectivityManager,
    private val guard: WifiOperationGuard
) {

    /**
     * Gets current WiFi connection information.<br>
     * Uses modern API (NetworkCapabilities) on Android Q+ and falls back to legacy API on older versions.<br><br>
     * 현재 WiFi 연결 정보를 가져옵니다.<br>
     * Android Q 이상에서는 최신 API (NetworkCapabilities)를 사용하고, 이전 버전에서는 레거시 API로 폴백합니다.<br>
     *
     * @return WifiInfo object or null if not connected.<br><br>
     *         WifiInfo 객체 또는 연결되지 않은 경우 null.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    fun getConnectionInfo(): WifiInfo? = guard.run(null) {
        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = {
                getConnectionInfoFromNetworkCapabilities()
                    ?: run {
                        checkSdkVersion(Build.VERSION_CODES.S,
                            positiveWork = { null },
                            negativeWork = {
                                @Suppress("DEPRECATION")
                                wifiManager.connectionInfo?.takeUnless { it.ssid == WifiManager.UNKNOWN_SSID }
                            }
                        )
                    }
            },
            negativeWork = {
                @Suppress("DEPRECATION")
                wifiManager.connectionInfo
            }
        )
    }

    /**
     * Gets WiFi connection information from NetworkCapabilities (modern approach).<br>
     * Available on Android Q (API 29) and higher.<br><br>
     * NetworkCapabilities에서 WiFi 연결 정보를 가져옵니다 (최신 방식).<br>
     * Android Q (API 29) 이상에서 사용 가능합니다.<br>
     *
     * @return WifiInfo object or null if not available.<br><br>
     *         WifiInfo 객체 또는 사용 불가능한 경우 null.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public fun getConnectionInfoFromNetworkCapabilities(): WifiInfo? = guard.run(null) {
        val network = connectivityManager.activeNetwork ?: return@run null
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return@run null
        if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return@run null

        checkSdkVersion(Build.VERSION_CODES.S,
            positiveWork = {
                val info = caps.transportInfo as? WifiInfo ?: return@run null
                info.takeUnless { it.ssid == WifiManager.UNKNOWN_SSID }
            },
            negativeWork = {
                @Suppress("DEPRECATION")
                wifiManager.connectionInfo?.takeUnless { it.ssid == WifiManager.UNKNOWN_SSID }
            }
        )
    }

    /**
     * Gets detailed modern WiFi network information including bandwidth, validation status, etc.<br>
     * Available on Android Q (API 29) and higher.<br><br>
     * 대역폭, 검증 상태 등을 포함한 상세한 최신 WiFi 네트워크 정보를 가져옵니다.<br>
     * Android Q (API 29) 이상에서 사용 가능합니다.<br>
     *
     * @return WifiNetworkDetails object or null if not available.<br><br>
     *         WifiNetworkDetails 객체 또는 사용 불가능한 경우 null.<br>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    fun getModernNetworkDetails(): WifiNetworkDetails? = guard.run(null) {
        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = {
                val activeNetwork: Network = connectivityManager.activeNetwork ?: return@run null
                val networkCapabilities: NetworkCapabilities =
                    connectivityManager.getNetworkCapabilities(activeNetwork) ?: return@run null

                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    val linkProperties: LinkProperties? = connectivityManager.getLinkProperties(activeNetwork)

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
                } else {
                    null
                }
            },
            negativeWork = { null }
        )
    }
}
