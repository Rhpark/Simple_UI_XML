package kr.open.library.simple_ui.system_manager.robolectric.core.info.network.connectivity.data

import android.net.NetworkCapabilities
import android.net.NetworkSpecifier
import android.net.TransportInfo
import android.os.Build
import kr.open.library.simple_ui.system_manager.core.info.network.connectivity.data.NetworkCapabilitiesData
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies SDK-aware capability accessors and documented string fallbacks.<br><br>
 * SDK별 네트워크 기능 접근자와 문서화된 문자열 폴백을 검증합니다.<br>
 */
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

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getCapabilities_onPreS_mapsAllKnownNames_andKeepsUnknownAsMinusOne() {
        val names = listOf(
            "MMS",
            "SUPL",
            "DUN",
            "FOTA",
            "IMS",
            "CBS",
            "WIFI_P2P",
            "IA",
            "RCS",
            "XCAP",
            "EIMS",
            "NOT_METERED",
            "INTERNET",
            "NOT_RESTRICTED",
            "TRUSTED",
            "NOT_VPN",
            "VALIDATED",
            "CAPTIVE_PORTAL",
            "NOT_ROAMING",
            "FOREGROUND",
            "NOT_CONGESTED",
            "NOT_SUSPENDED",
            "OEM_PAID",
            "MCX",
            "PARTIAL_CONNECTIVITY",
            "TEMPORARILY_NOT_METERED",
            "OEM_PRIVATE",
            "VEHICLE_INTERNAL",
            "NOT_VCN_MANAGED",
            "ENTERPRISE",
            "VSIM",
            "BIP",
            "HEAD_UNIT",
            "MMTEL",
            "PRIORITIZE_LATENCY",
            "PRIORITIZE_BANDWIDTH",
            "LOCAL_NETWORK",
            "UNKNOWN_CAPABILITY",
        )
        val capabilities = mock(NetworkCapabilities::class.java)
        doReturn("Capabilities: ${names.joinToString("&")} LinkUpBandwidth")
            .`when`(capabilities)
            .toString()

        val result = NetworkCapabilitiesData(capabilities).getCapabilities()

        assertArrayEquals((0..36).toList().plus(-1).toIntArray(), result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun publicAccessors_returnFrameworkValues() {
        val capabilities = mock(NetworkCapabilities::class.java)
        val specifier = mock(NetworkSpecifier::class.java)
        val transportInfo = mock(TransportInfo::class.java)
        doReturn(1200).`when`(capabilities).linkUpstreamBandwidthKbps
        doReturn(2400).`when`(capabilities).linkDownstreamBandwidthKbps
        doReturn(-55).`when`(capabilities).signalStrength
        doReturn(specifier).`when`(capabilities).networkSpecifier
        doReturn(10001).`when`(capabilities).ownerUid
        doReturn(transportInfo).`when`(capabilities).transportInfo

        val data = NetworkCapabilitiesData(capabilities)

        assertEquals(1200, data.getLinkUpstreamBandwidthKbps())
        assertEquals(2400, data.getLinkDownstreamBandwidthKbps())
        assertEquals(-55, data.getSignalStrength())
        assertSame(specifier, data.getNetworkSpecifier())
        assertEquals(10001, data.getOwnerUid())
        assertSame(transportInfo, data.getTransportInfo())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun legacyAccessors_parseSignalStrengthAndSubscriptionIds() {
        val capabilities = mock(NetworkCapabilities::class.java)
        doReturn("SignalStrength: -70 SubscriptionIds: {1,2}").`when`(capabilities).toString()

        val data = NetworkCapabilitiesData(capabilities)

        assertEquals(-70, data.getSignalStrength())
        assertEquals(listOf(1, 2), data.getSubscriptionIds())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun getEnterpriseIds_returnsFrameworkValues() {
        val capabilities = mock(NetworkCapabilities::class.java)
        doReturn(intArrayOf(1, 3, 5)).`when`(capabilities).enterpriseIds

        assertArrayEquals(
            intArrayOf(1, 3, 5),
            NetworkCapabilitiesData(capabilities).getEnterpriseIds(),
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun transportInfoAccessors_parsePublishedFields() {
        val transportInfo = mock(TransportInfo::class.java)
        doReturn(
            "SSID: test, BSSID: 00:11:22:33:44:55, MAC: 66:77:88:99:AA:BB, " +
                "IP: 192.0.2.1, Security type: WPA2, Supplicant state: COMPLETED, " +
                "Wi-Fi standard: 6, RSSI: -45, Link speed: 866Mbps, Tx Link speed: 500Mbps, " +
                "Max Supported Tx Link speed: 1200Mbps, Rx Link speed: 600Mbps, " +
                "Max Supported Rx Link speed: 1200Mbps, Frequency: 5200MHz, Net ID: 7, " +
                "Metered hint: true, score: 60, SubscriptionId: 1, IsPrimary: true, " +
                "isUsable: true, CarrierMerged: true, Trusted: true, Restricted: true, " +
                "Ephemeral: true, OEM paid: true, OEM private: true, OSU AP: true, end",
        ).`when`(transportInfo).toString()
        val capabilities = mock(NetworkCapabilities::class.java)
        doReturn(transportInfo).`when`(capabilities).transportInfo

        val data = NetworkCapabilitiesData(capabilities)

        assertEquals("00:11:22:33:44:55", data.getBssidInTransportInfo())
        assertEquals("66:77:88:99:AA:BB", data.getMacInTransportInfo())
        assertEquals("192.0.2.1", data.getIpTransportInfo())
        assertEquals("WPA2", data.getSecurityTypeTransportInfo())
        assertEquals("COMPLETED", data.getSupplicantStateTransportInfo())
        assertEquals("6", data.getWifiStandardTransportInfo())
        assertEquals("-45", data.getRssiTransportInfo())
        assertEquals("866Mbps", data.getLinkSpeedTransportInfo())
        assertEquals("500Mbps", data.getTxLinkSpeedTransportInfo())
        assertEquals("1200Mbps", data.getMaxSupportedTxLinkSpeedTransportInfo())
        assertEquals("600Mbps", data.getRxLinkSpeedTransportInfo())
        assertEquals("1200Mbps", data.getMaxRxSupportedLinkSpeedTransportInfo())
        assertEquals("5200MHz", data.getFrequencyTransportInfo())
        assertEquals("7", data.getNetIdTransportInfo())
        assertTrue(data.isMeteredHintTransportInfo())
        assertEquals("60", data.getScoreTransportInfo())
        assertEquals("1", data.getSubscriptionIdTransportInfo())
        assertEquals("true", data.getIsPrimaryTransportInfo())
        assertTrue(data.isUsableTransportInfo())
        assertTrue(data.isCarrierMergedTransportInfo())
        assertTrue(data.isTrustedTransportInfo())
        assertTrue(data.isRestrictedTransportInfo())
        assertTrue(data.isEphemeralTransportInfo())
        assertTrue(data.isOemPaidTransportInfo())
        assertTrue(data.isOemPrivateTransportInfo())
        assertTrue(data.isOsuApTransportInfo())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun transportInfoAccessors_returnFallbacks_whenTransportInfoIsMissing() {
        val data = NetworkCapabilitiesData(mock(NetworkCapabilities::class.java))

        assertNull(data.getBssidInTransportInfo())
        assertFalse(data.isMeteredHintTransportInfo())
        assertFalse(data.isUsableTransportInfo())
        assertFalse(data.isCarrierMergedTransportInfo())
        assertFalse(data.isTrustedTransportInfo())
        assertFalse(data.isRestrictedTransportInfo())
        assertFalse(data.isEphemeralTransportInfo())
        assertFalse(data.isOemPaidTransportInfo())
        assertFalse(data.isOemPrivateTransportInfo())
        assertFalse(data.isOsuApTransportInfo())
    }
}
