package kr.open.library.simple_ui.core.system_manager.info.network.connectivity.data

import android.net.IpPrefix
import android.net.LinkAddress
import android.net.LinkProperties
import android.net.ProxyInfo
import android.net.RouteInfo
import android.os.Build
import androidx.annotation.RequiresApi
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import java.net.InetAddress

/**
 * Data class wrapper for LinkProperties.<br><br>
 * LinkProperties를 감싸는 데이터 래퍼입니다.<br>
 *
 * Provides safe accessors and string parsing fallbacks for LinkProperties fields.<br><br>
 * LinkProperties 필드에 대한 안전한 접근자와 문자열 파싱 보조 로직을 제공합니다.<br>
 *
 * @property linkProperties The original LinkProperties object.<br><br>
 *                          원본 LinkProperties 객체입니다.<br>
 */
public data class NetworkLinkPropertiesData(
    public val linkProperties: LinkProperties,
) : NetworkBase(linkProperties) {
    /**
     * Gets the list of link addresses.<br><br>
     * 링크 주소 목록을 반환합니다.<br>
     *
     * @return List of LinkAddress.<br><br>
     *         LinkAddress 목록입니다.<br>
     */
    public fun getLinkAddresses(): MutableList<LinkAddress> = linkProperties.linkAddresses

    /**
     * Gets the MTU (Maximum Transmission Unit) size.<br><br>
     * MTU(최대 전송 단위) 크기를 반환합니다.<br>
     *
     * @return MTU size in bytes.<br><br>
     *         바이트 단위 MTU 크기입니다.<br>
     */
    public fun getMtu(): Int =
        checkSdkVersion(
            Build.VERSION_CODES.Q,
            positiveWork = { linkProperties.mtu },
            negativeWork = { splitStr("MTU: ", " ")?.let { it[0].toInt() } ?: 0 },
        )

    /**
     * Gets the list of routes.<br><br>
     * 라우트 목록을 반환합니다.<br>
     *
     * @return List of RouteInfo.<br><br>
     *         RouteInfo 목록입니다.<br>
     */
    public fun getRoutes(): MutableList<RouteInfo> = linkProperties.routes

    /**
     * Gets the domains.<br><br>
     * 도메인 정보를 반환합니다.<br>
     *
     * @return Domains string, or `null`.<br><br>
     *         도메인 문자열이며, 없으면 `null`입니다.<br>
     */
    public fun getDomains(): String? = linkProperties.domains

    /**
     * Gets the list of DNS servers.<br><br>
     * DNS 서버 목록을 반환합니다.<br>
     *
     * @return List of InetAddress.<br><br>
     *         InetAddress 목록입니다.<br>
     */
    public fun getDnsServer(): MutableList<InetAddress> = linkProperties.dnsServers

    /**
     * Gets the DHCP server address.<br><br>
     * DHCP 서버 주소를 반환합니다.<br>
     *
     * @return DHCP server address, or `null`.<br><br>
     *         DHCP 서버 주소이며, 없으면 `null`입니다.<br>
     */
    public fun getDhcpServerAddress(): InetAddress? =
        checkSdkVersion(
            Build.VERSION_CODES.R,
            positiveWork = { linkProperties.dhcpServerAddress },
            negativeWork = { InetAddress.getByName(splitStr("ServerAddress: ", " ")?.get(0)?.toString()) },
        )

    /**
     * Gets the HTTP proxy information.<br><br>
     * HTTP 프록시 정보를 반환합니다.<br>
     *
     * @return ProxyInfo, or `null`.<br><br>
     *         ProxyInfo이며, 없으면 `null`입니다.<br>
     */
    public fun getHttpProxy(): ProxyInfo? = linkProperties.httpProxy

    /**
     * Gets the interface name.<br><br>
     * 인터페이스 이름을 반환합니다.<br>
     *
     * @return Interface name, or `null`.<br><br>
     *         인터페이스 이름이며, 없으면 `null`입니다.<br>
     */
    public fun getInterfaceName(): String? = linkProperties.interfaceName

    /**
     * Checks if private DNS is active.<br><br>
     * 프라이빗 DNS가 활성화되어 있는지 확인합니다.<br>
     *
     * @return `true` if active; `false` otherwise.<br><br>
     *         활성화되어 있으면 `true`, 아니면 `false`입니다.<br>
     */
    public fun isPrivateDnsActive(): Boolean = linkProperties.isPrivateDnsActive

    /**
     * Gets the private DNS server name.<br><br>
     * 프라이빗 DNS 서버 이름을 반환합니다.<br>
     *
     * @return Private DNS server name, or `null`.<br><br>
     *         프라이빗 DNS 서버 이름이며, 없으면 `null`입니다.<br>
     */
    public fun getPrivateDnsServerName(): String? = linkProperties.privateDnsServerName

    /**
     * Gets the NAT64 prefix (API 30+).<br><br>
     * NAT64 프리픽스를 반환합니다(API 30+).<br>
     *
     * @return NAT64 prefix, or `null`.<br><br>
     *         NAT64 프리픽스이며, 없으면 `null`입니다.<br>
     */
    @RequiresApi(Build.VERSION_CODES.R)
    public fun getNat64Prefix(): IpPrefix? = linkProperties.nat64Prefix

    /**
     * Gets the TCP buffer sizes.<br><br>
     * TCP 버퍼 크기를 반환합니다.<br>
     *
     * @return Buffer sizes as strings, or `null`.<br><br>
     *         문자열 형태의 버퍼 크기 목록이며, 없으면 `null`입니다.<br>
     */
    public fun getTcpBufferSizes(): List<String>? =
        if (getResStr().contains(" TcpBufferSizes: ")) {
            getResStr().split(" TcpBufferSizes: ", " ")?.split(",")
        } else {
            null
        }

    /**
     * Converts all properties to a readable string.<br><br>
     * 주요 속성을 사람이 읽기 쉬운 문자열로 변환합니다.<br>
     *
     * @return Formatted string representation.<br><br>
     *         포맷된 문자열 표현입니다.<br>
     */
    public fun toResString(): String {
        var res =
            "getLinkAddresses ${getLinkAddresses().toList()}\n" +
                "getMtu ${getMtu()}\n" +
                "getRoutes ${getRoutes()}\n" +
                "getDomains ${getDomains()}\n" +
                "getDnsServer ${getDnsServer()}\n" +
                "getDhcpServerAddress ${getDhcpServerAddress()}\n" +
                "getHttpProxy ${getHttpProxy()}\n" +
                "getInterfaceName ${getInterfaceName()}\n" +
                "isPrivateDnsActive ${isPrivateDnsActive()}\n" +
                "getPrivateDnsServerName ${getPrivateDnsServerName()}\n" +
                "getTcpBufferSizes ${getTcpBufferSizes()}\n"

        checkSdkVersion(Build.VERSION_CODES.R) {
            res += "getNat64Prefix ${getNat64Prefix()}\n"
        }
        res += "\n\n"
        return res
    }
}
