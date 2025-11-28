/**
 * Data class containing detailed WiFi network information.<br><br>
 * WiFi 네트워크의 상세 정보를 포함하는 데이터 클래스입니다.<br>
 *
 * @param isConnected Whether WiFi is currently connected.<br><br>
 *                    WiFi가 현재 연결되어 있는지 여부.
 * @param hasInternet Whether the connection has internet access.<br><br>
 *                    연결이 인터넷 접근 권한을 가지고 있는지 여부.
 * @param isValidated Whether the network connection is validated.<br><br>
 *                    네트워크 연결이 검증되었는지 여부.
 * @param isMetered Whether the connection is metered (has data limits).<br><br>
 *                  연결이 종량제인지 여부 (데이터 제한이 있는지).
 * @param linkDownstreamBandwidthKbps Downstream bandwidth in Kbps.<br><br>
 *                                    다운스트림 대역폭 (Kbps).
 * @param linkUpstreamBandwidthKbps Upstream bandwidth in Kbps.<br><br>
 *                                  업스트림 대역폭 (Kbps).
 * @param interfaceName Network interface name (e.g., "wlan0").<br><br>
 *                      네트워크 인터페이스 이름 (예: "wlan0").
 * @param dnsServers List of DNS server addresses.<br><br>
 *                   DNS 서버 주소 목록.
 * @param domains Domain search list.<br><br>
 *                도메인 검색 목록.
 */
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
