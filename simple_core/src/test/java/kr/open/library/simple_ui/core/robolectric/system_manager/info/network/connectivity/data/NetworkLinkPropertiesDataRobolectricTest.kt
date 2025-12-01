package kr.open.library.simple_ui.core.robolectric.system_manager.info.network.connectivity.data

import android.net.LinkProperties
import android.os.Build
import kr.open.library.simple_ui.core.system_manager.info.network.connectivity.data.NetworkLinkPropertiesData
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.Inet4Address
import java.net.InetAddress

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

        assertEquals('5'.code, data.getMtu())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getDhcpServerAddress_onSdkR_returnsConfiguredAddress() {
        val address = InetAddress.getByName("192.0.2.5") as Inet4Address
        val linkProperties = LinkProperties().apply { dhcpServerAddress = address }

        val data = NetworkLinkPropertiesData(linkProperties)

        assertEquals(address, data.getDhcpServerAddress())
    }
}
