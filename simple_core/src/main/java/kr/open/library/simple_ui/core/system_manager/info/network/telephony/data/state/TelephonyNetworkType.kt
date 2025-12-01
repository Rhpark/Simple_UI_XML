package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state

/**
 * General network types for telephony.<br><br>
 * Telephony를 위한 일반 네트워크 타입입니다.<br>
 *
 * Categorizes networks into generations (2G, 3G, 4G, 5G) or connection states.<br><br>
 * 네트워크를 세대(2G, 3G, 4G, 5G) 또는 연결 상태로 분류합니다.<br>
 */
public enum class TelephonyNetworkType {
    DISCONNECT,
    CONNECTING,
    UNKNOWN,
    CONNECT_WIFI,
    CONNECT_2G,
    CONNECT_3G,
    CONNECT_4G,
    CONNECT_5G,
}
