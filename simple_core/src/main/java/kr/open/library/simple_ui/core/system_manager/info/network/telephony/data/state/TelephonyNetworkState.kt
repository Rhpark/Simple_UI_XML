package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state

public data class TelephonyNetworkState(
    public val networkTypeState: TelephonyNetworkType,
    public val networkTypeDetailState: TelephonyNetworkDetailType
) {
}