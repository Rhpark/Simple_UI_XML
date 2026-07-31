package kr.open.library.simple_ui.system_manager.robolectric.core.info.network.connectivity.data

import android.net.IpPrefix
import android.net.LinkAddress
import android.net.LinkProperties
import android.net.ProxyInfo
import android.net.RouteInfo
import android.os.Build
import kr.open.library.simple_ui.system_manager.core.info.network.connectivity.data.NetworkLinkPropertiesData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.Inet4Address
import java.net.InetAddress

/**
 * Verifies LinkProperties accessors and SDK-specific string fallbacks.<br><br>
 * LinkProperties 접근자와 SDK별 문자열 폴백을 검증합니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
class NetworkLinkPropertiesDataRobolectricTest {
    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getMtu_onSdkQ_returnsConfiguredValue() {
        val linkProperties = LinkProperties().apply { mtu = 1700 }

        val data = NetworkLinkPropertiesData(linkProperties)

        assertEquals(1700, data.getMtu())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun getMtu_onPreQ_parsesFromToString() {
        val mocked = mock(LinkProperties::class.java)
        doReturn("MTU: 5 ").`when`(mocked).toString()

        val data = NetworkLinkPropertiesData(mocked)

        assertEquals(5, data.getMtu())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getDhcpServerAddress_onSdkR_returnsConfiguredAddress() {
        val address = InetAddress.getByName("192.0.2.5") as Inet4Address
        val linkProperties = LinkProperties().apply { dhcpServerAddress = address }

        val data = NetworkLinkPropertiesData(linkProperties)

        assertEquals(address, data.getDhcpServerAddress())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun getDhcpServerAddress_onPreR_parsesAddressWithoutSlash() {
        val linkProperties = mock(LinkProperties::class.java)
        doReturn("DhcpServerAddress: /192.0.2.6 ").`when`(linkProperties).toString()

        assertEquals(
            InetAddress.getByName("192.0.2.6"),
            NetworkLinkPropertiesData(linkProperties).getDhcpServerAddress(),
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun legacyParsers_returnFallbacks_whenValuesAreMissingOrMalformed() {
        val linkProperties = mock(LinkProperties::class.java)
        doReturn("DhcpServerAddress: / TcpBufferSizes: 1,2,3 end")
            .`when`(linkProperties)
            .toString()

        val data = NetworkLinkPropertiesData(linkProperties)

        assertNull(data.getDhcpServerAddress())
        assertEquals(listOf("1", "2", "3"), data.getTcpBufferSizes())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun publicAccessors_returnFrameworkValues() {
        val linkAddress = mock(LinkAddress::class.java)
        val route = mock(RouteInfo::class.java)
        val dnsServer = InetAddress.getByName("192.0.2.53")
        val proxy = mock(ProxyInfo::class.java)
        val nat64Prefix = mock(IpPrefix::class.java)
        val linkProperties = mock(LinkProperties::class.java)
        doReturn(mutableListOf(linkAddress)).`when`(linkProperties).linkAddresses
        doReturn(mutableListOf(route)).`when`(linkProperties).routes
        doReturn("example.test").`when`(linkProperties).domains
        doReturn(mutableListOf(dnsServer)).`when`(linkProperties).dnsServers
        doReturn(proxy).`when`(linkProperties).httpProxy
        doReturn("wlan0").`when`(linkProperties).interfaceName
        doReturn(true).`when`(linkProperties).isPrivateDnsActive
        doReturn("dns.example.test").`when`(linkProperties).privateDnsServerName
        doReturn(nat64Prefix).`when`(linkProperties).nat64Prefix

        val data = NetworkLinkPropertiesData(linkProperties)

        assertEquals(listOf(linkAddress), data.getLinkAddresses())
        assertEquals(listOf(route), data.getRoutes())
        assertEquals("example.test", data.getDomains())
        assertEquals(listOf(dnsServer), data.getDnsServer())
        assertSame(proxy, data.getHttpProxy())
        assertEquals("wlan0", data.getInterfaceName())
        assertTrue(data.isPrivateDnsActive())
        assertEquals("dns.example.test", data.getPrivateDnsServerName())
        assertSame(nat64Prefix, data.getNat64Prefix())
    }

    @Test
    fun getTcpBufferSizes_returnsNull_whenMarkerIsMissing() {
        val linkProperties = mock(LinkProperties::class.java)
        doReturn("LinkProperties without TCP buffer information").`when`(linkProperties).toString()

        assertNull(NetworkLinkPropertiesData(linkProperties).getTcpBufferSizes())
    }
}
