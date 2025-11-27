package kr.open.library.simple_ui.core.unit.system_manager.info.network.telephony.data.state

import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state.TelephonyNetworkDetailType
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state.TelephonyNetworkType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for TelephonyNetworkType and TelephonyNetworkDetailType enums
 */
class TelephonyNetworkTypeUnitTest {

    // ==============================================
    // TelephonyNetworkType Tests
    // ==============================================

    @Test
    fun telephonyNetworkType_hasCorrectNumberOfValues() {
        val entries = TelephonyNetworkType.entries
        assertEquals(8, entries.size)
    }

    @Test
    fun telephonyNetworkType_containsAllExpectedValues() {
        val entries = TelephonyNetworkType.entries
        assertTrue(entries.contains(TelephonyNetworkType.DISCONNECT))
        assertTrue(entries.contains(TelephonyNetworkType.CONNECTING))
        assertTrue(entries.contains(TelephonyNetworkType.UNKNOWN))
        assertTrue(entries.contains(TelephonyNetworkType.CONNECT_WIFI))
        assertTrue(entries.contains(TelephonyNetworkType.CONNECT_2G))
        assertTrue(entries.contains(TelephonyNetworkType.CONNECT_3G))
        assertTrue(entries.contains(TelephonyNetworkType.CONNECT_4G))
        assertTrue(entries.contains(TelephonyNetworkType.CONNECT_5G))
    }

    @Test
    fun telephonyNetworkType_valueOf_returnsCorrectEnum() {
        assertEquals(TelephonyNetworkType.DISCONNECT, TelephonyNetworkType.valueOf("DISCONNECT"))
        assertEquals(TelephonyNetworkType.CONNECTING, TelephonyNetworkType.valueOf("CONNECTING"))
        assertEquals(TelephonyNetworkType.UNKNOWN, TelephonyNetworkType.valueOf("UNKNOWN"))
        assertEquals(TelephonyNetworkType.CONNECT_WIFI, TelephonyNetworkType.valueOf("CONNECT_WIFI"))
        assertEquals(TelephonyNetworkType.CONNECT_2G, TelephonyNetworkType.valueOf("CONNECT_2G"))
        assertEquals(TelephonyNetworkType.CONNECT_3G, TelephonyNetworkType.valueOf("CONNECT_3G"))
        assertEquals(TelephonyNetworkType.CONNECT_4G, TelephonyNetworkType.valueOf("CONNECT_4G"))
        assertEquals(TelephonyNetworkType.CONNECT_5G, TelephonyNetworkType.valueOf("CONNECT_5G"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun telephonyNetworkType_valueOf_throwsExceptionForInvalidName() {
        TelephonyNetworkType.valueOf("INVALID_NETWORK")
    }

    @Test
    fun telephonyNetworkType_enumName_matchesExpectedFormat() {
        assertEquals("DISCONNECT", TelephonyNetworkType.DISCONNECT.name)
        assertEquals("CONNECTING", TelephonyNetworkType.CONNECTING.name)
        assertEquals("UNKNOWN", TelephonyNetworkType.UNKNOWN.name)
        assertEquals("CONNECT_WIFI", TelephonyNetworkType.CONNECT_WIFI.name)
        assertEquals("CONNECT_2G", TelephonyNetworkType.CONNECT_2G.name)
        assertEquals("CONNECT_3G", TelephonyNetworkType.CONNECT_3G.name)
        assertEquals("CONNECT_4G", TelephonyNetworkType.CONNECT_4G.name)
        assertEquals("CONNECT_5G", TelephonyNetworkType.CONNECT_5G.name)
    }

    @Test
    fun telephonyNetworkType_enumOrder_matchesDefinition() {
        val entries = TelephonyNetworkType.entries
        assertEquals(TelephonyNetworkType.DISCONNECT, entries[0])
        assertEquals(TelephonyNetworkType.CONNECTING, entries[1])
        assertEquals(TelephonyNetworkType.UNKNOWN, entries[2])
    }

    // ==============================================
    // TelephonyNetworkDetailType Tests
    // ==============================================

    @Test
    fun telephonyNetworkDetailType_hasCorrectNumberOfValues() {
        val entries = TelephonyNetworkDetailType.entries
        assertEquals(27, entries.size)
    }

    @Test
    fun telephonyNetworkDetailType_containsBasicConnectionStates() {
        val entries = TelephonyNetworkDetailType.entries
        assertTrue(entries.contains(TelephonyNetworkDetailType.DISCONNECT))
        assertTrue(entries.contains(TelephonyNetworkDetailType.CONNECTING))
        assertTrue(entries.contains(TelephonyNetworkDetailType.UNKNOWN))
        assertTrue(entries.contains(TelephonyNetworkDetailType.CONNECT_WIFI))
    }

    @Test
    fun telephonyNetworkDetailType_contains2GNetworkTypes() {
        val entries = TelephonyNetworkDetailType.entries
        assertTrue(entries.contains(TelephonyNetworkDetailType.NETWORK_TYPE_GPRS))
        assertTrue(entries.contains(TelephonyNetworkDetailType.NETWORK_TYPE_EDGE))
        assertTrue(entries.contains(TelephonyNetworkDetailType.NETWORK_TYPE_CDMA))
        assertTrue(entries.contains(TelephonyNetworkDetailType.NETWORK_TYPE_1xRTT))
        assertTrue(entries.contains(TelephonyNetworkDetailType.NETWORK_TYPE_IDEN))
        assertTrue(entries.contains(TelephonyNetworkDetailType.NETWORK_TYPE_GSM))
    }

    @Test
    fun telephonyNetworkDetailType_contains3GNetworkTypes() {
        val entries = TelephonyNetworkDetailType.entries
        assertTrue(entries.contains(TelephonyNetworkDetailType.NETWORK_TYPE_UMTS))
        assertTrue(entries.contains(TelephonyNetworkDetailType.NETWORK_TYPE_EVDO_0))
        assertTrue(entries.contains(TelephonyNetworkDetailType.NETWORK_TYPE_EVDO_A))
        assertTrue(entries.contains(TelephonyNetworkDetailType.NETWORK_TYPE_HSDPA))
        assertTrue(entries.contains(TelephonyNetworkDetailType.NETWORK_TYPE_HSUPA))
        assertTrue(entries.contains(TelephonyNetworkDetailType.NETWORK_TYPE_HSPA))
        assertTrue(entries.contains(TelephonyNetworkDetailType.NETWORK_TYPE_EVDO_B))
        assertTrue(entries.contains(TelephonyNetworkDetailType.NETWORK_TYPE_EHRPD))
        assertTrue(entries.contains(TelephonyNetworkDetailType.NETWORK_TYPE_HSPAP))
        assertTrue(entries.contains(TelephonyNetworkDetailType.NETWORK_TYPE_TD_SCDMA))
    }

    @Test
    fun telephonyNetworkDetailType_contains4GNetworkTypes() {
        val entries = TelephonyNetworkDetailType.entries
        assertTrue(entries.contains(TelephonyNetworkDetailType.NETWORK_TYPE_LTE))
        assertTrue(entries.contains(TelephonyNetworkDetailType.NETWORK_TYPE_IWLAN))
        assertTrue(entries.contains(TelephonyNetworkDetailType.NETWORK_TYPE_LTE_CA))
    }

    @Test
    fun telephonyNetworkDetailType_contains5GNetworkTypes() {
        val entries = TelephonyNetworkDetailType.entries
        assertTrue(entries.contains(TelephonyNetworkDetailType.NETWORK_TYPE_NR))
        assertTrue(entries.contains(TelephonyNetworkDetailType.OVERRIDE_NETWORK_TYPE_NR_NSA))
        assertTrue(entries.contains(TelephonyNetworkDetailType.OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE))
        assertTrue(entries.contains(TelephonyNetworkDetailType.OVERRIDE_NETWORK_TYPE_NR_ADVANCED))
    }

    @Test
    fun telephonyNetworkDetailType_valueOf_returnsCorrectEnum() {
        assertEquals(TelephonyNetworkDetailType.DISCONNECT, TelephonyNetworkDetailType.valueOf("DISCONNECT"))
        assertEquals(TelephonyNetworkDetailType.NETWORK_TYPE_LTE, TelephonyNetworkDetailType.valueOf("NETWORK_TYPE_LTE"))
        assertEquals(TelephonyNetworkDetailType.NETWORK_TYPE_NR, TelephonyNetworkDetailType.valueOf("NETWORK_TYPE_NR"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun telephonyNetworkDetailType_valueOf_throwsExceptionForInvalidName() {
        TelephonyNetworkDetailType.valueOf("INVALID_DETAIL_TYPE")
    }

    @Test
    fun telephonyNetworkDetailType_enumName_matchesExpectedFormat() {
        assertEquals("DISCONNECT", TelephonyNetworkDetailType.DISCONNECT.name)
        assertEquals("NETWORK_TYPE_LTE", TelephonyNetworkDetailType.NETWORK_TYPE_LTE.name)
        assertEquals("OVERRIDE_NETWORK_TYPE_NR_ADVANCED", TelephonyNetworkDetailType.OVERRIDE_NETWORK_TYPE_NR_ADVANCED.name)
    }

    @Test
    fun telephonyNetworkDetailType_enumOrder_startsWithConnectionStates() {
        val entries = TelephonyNetworkDetailType.entries
        assertEquals(TelephonyNetworkDetailType.DISCONNECT, entries[0])
        assertEquals(TelephonyNetworkDetailType.CONNECTING, entries[1])
        assertEquals(TelephonyNetworkDetailType.UNKNOWN, entries[2])
        assertEquals(TelephonyNetworkDetailType.CONNECT_WIFI, entries[3])
    }
}
