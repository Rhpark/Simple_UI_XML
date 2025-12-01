package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state

/**
 * Represents the current state of the telephony network.<br><br>
 * 현재 Telephony 네트워크 상태를 나타냅니다.<br>
 *
 * Combines general network type and detailed network type.<br><br>
 * 일반 네트워크 타입과 상세 네트워크 타입을 결합합니다.<br>
 *
 * @property networkTypeState General network type (e.g., 4G, 5G).<br><br>
 *                            일반 네트워크 타입 (예: 4G, 5G).
 * @property networkTypeDetailState Detailed network type (e.g., LTE, NR).<br><br>
 *                                  상세 네트워크 타입 (예: LTE, NR).
 */
public data class TelephonyNetworkState(
    public val networkTypeState: TelephonyNetworkType,
    public val networkTypeDetailState: TelephonyNetworkDetailType,
)
