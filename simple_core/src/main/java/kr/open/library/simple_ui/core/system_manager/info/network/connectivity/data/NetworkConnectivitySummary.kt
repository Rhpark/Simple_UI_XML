package kr.open.library.simple_ui.core.system_manager.info.network.connectivity.data

import android.net.LinkProperties
import android.net.NetworkCapabilities

// =================================================
// Data Classes / 데이터 클래스
// =================================================

/**
 * Network connectivity summary information.<br><br>
 * 네트워크 연결성 요약 정보입니다.<br>
 *
 * @property isNetworkConnected Indicates whether the network is connected.<br><br>
 *                              네트워크가 연결되어 있는지 여부.<br>
 * @property isWifiConnected Indicates whether WiFi is connected.<br><br>
 *                           WiFi가 연결되어 있는지 여부.<br>
 * @property isMobileConnected Indicates whether mobile network is connected.<br><br>
 *                             모바일 네트워크가 연결되어 있는지 여부.<br>
 * @property isVpnConnected Indicates whether VPN is connected.<br><br>
 *                          VPN이 연결되어 있는지 여부.<br>
 * @property isWifiEnabled Indicates whether WiFi is enabled.<br><br>
 *                         WiFi가 활성화되어 있는지 여부.<br>
 * @property networkCapabilities Current network capabilities, or null if unavailable.<br><br>
 *                               현재 네트워크 능력, 사용할 수 없는 경우 null.<br>
 * @property linkProperties Current link properties, or null if unavailable.<br><br>
 *                          현재 링크 속성, 사용할 수 없는 경우 null.<br>
 */
public data class NetworkConnectivitySummary(
    val isNetworkConnected: Boolean,
    val isWifiConnected: Boolean,
    val isMobileConnected: Boolean,
    val isVpnConnected: Boolean,
    val isWifiEnabled: Boolean,
    val networkCapabilities: NetworkCapabilities?,
    val linkProperties: LinkProperties?,
)
