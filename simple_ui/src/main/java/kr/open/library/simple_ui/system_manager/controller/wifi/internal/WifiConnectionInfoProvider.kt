package kr.open.library.simple_ui.system_manager.controller.wifi.internal

import android.Manifest.permission.ACCESS_NETWORK_STATE
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresPermission
import kr.open.library.simple_ui.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.system_manager.controller.wifi.WifiNetworkDetails

internal class WifiConnectionInfoProvider(
    private val wifiManager: WifiManager,
    private val connectivityManager: android.net.ConnectivityManager,
    private val guard: WifiOperationGuard
) {

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
