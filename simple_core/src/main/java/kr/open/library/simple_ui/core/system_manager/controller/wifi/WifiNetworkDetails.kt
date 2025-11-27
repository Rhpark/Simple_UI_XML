package kr.open.library.simple_ui.core.system_manager.controller.wifi

public data class WifiNetworkDetails(
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
