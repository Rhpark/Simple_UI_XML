package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state

/**
 * Detailed network types for telephony.<br><br>
 * Telephony를 위한 상세 네트워크 타입입니다.<br>
 *
 * Includes specific network technologies like GPRS, LTE, NR, etc.<br><br>
 * GPRS, LTE, NR 등과 같은 특정 네트워크 기술을 포함합니다.<br>
 */
public enum class TelephonyNetworkDetailType {
    DISCONNECT,
    CONNECTING,
    UNKNOWN,
    CONNECT_WIFI,
    NETWORK_TYPE_GPRS,
    NETWORK_TYPE_EDGE,
    NETWORK_TYPE_CDMA,
    NETWORK_TYPE_1xRTT,
    NETWORK_TYPE_IDEN,
    NETWORK_TYPE_GSM,
    NETWORK_TYPE_UMTS,
    NETWORK_TYPE_EVDO_0,
    NETWORK_TYPE_EVDO_A,
    NETWORK_TYPE_HSDPA,
    NETWORK_TYPE_HSUPA,
    NETWORK_TYPE_HSPA,
    NETWORK_TYPE_EVDO_B,
    NETWORK_TYPE_EHRPD,
    NETWORK_TYPE_HSPAP,
    NETWORK_TYPE_TD_SCDMA,
    NETWORK_TYPE_LTE,
    NETWORK_TYPE_IWLAN,
    NETWORK_TYPE_LTE_CA,
    NETWORK_TYPE_NR,
    OVERRIDE_NETWORK_TYPE_NR_NSA,
    OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE,
    OVERRIDE_NETWORK_TYPE_NR_ADVANCED,
}