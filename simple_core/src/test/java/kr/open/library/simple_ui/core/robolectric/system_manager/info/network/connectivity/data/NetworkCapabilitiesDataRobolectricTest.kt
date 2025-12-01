package kr.open.library.simple_ui.core.robolectric.system_manager.info.network.connectivity.data

import android.net.NetworkCapabilities
import android.os.Build
import kr.open.library.simple_ui.core.system_manager.info.network.connectivity.data.NetworkCapabilitiesData
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class NetworkCapabilitiesDataRobolectricTest {
    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun getCapabilities_onSdkS_returnsCapabilitiesArray() {
        val capabilities = mock(NetworkCapabilities::class.java)
        doReturn(
            intArrayOf(
                NetworkCapabilities.NET_CAPABILITY_INTERNET,
                NetworkCapabilities.NET_CAPABILITY_MMS,
            ),
        ).`when`(capabilities).capabilities

        val data = NetworkCapabilitiesData(capabilities)

        assertArrayEquals(
            intArrayOf(
                NetworkCapabilities.NET_CAPABILITY_INTERNET,
                NetworkCapabilities.NET_CAPABILITY_MMS,
            ),
            data.getCapabilities(),
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getCapabilities_onPreS_parsesFromToString() {
        val mockedCapabilities = mock(NetworkCapabilities::class.java)
        doReturn("Capabilities: INTERNET&MMS LinkUpBandwidth").`when`(mockedCapabilities).toString()

        val data = NetworkCapabilitiesData(mockedCapabilities)

        assertArrayEquals(
            intArrayOf(
                NetworkCapabilities.NET_CAPABILITY_INTERNET,
                NetworkCapabilities.NET_CAPABILITY_MMS,
            ),
            data.getCapabilities(),
        )
    }
}
