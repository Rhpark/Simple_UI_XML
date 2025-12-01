package kr.open.library.simple_ui.core.unit.system_manager.info.network.telephony.data.state

import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state.TelephonyNetworkDetailType
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state.TelephonyNetworkState
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state.TelephonyNetworkType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TelephonyNetworkStateTest {
    @Test
    fun telephonyNetworkType_hasAllEntries() {
        val types =
            listOf(
                TelephonyNetworkType.DISCONNECT,
                TelephonyNetworkType.CONNECTING,
                TelephonyNetworkType.UNKNOWN,
                TelephonyNetworkType.CONNECT_WIFI,
                TelephonyNetworkType.CONNECT_2G,
                TelephonyNetworkType.CONNECT_3G,
                TelephonyNetworkType.CONNECT_4G,
                TelephonyNetworkType.CONNECT_5G,
            )

        assertEquals(8, types.toSet().size)
    }

    @Test
    fun telephonyNetworkDetailType_hasAllEntries() {
        val detailTypes = TelephonyNetworkDetailType.values()

        assertTrue(detailTypes.isNotEmpty())
        assertEquals(detailTypes.size, detailTypes.toSet().size)
        assertTrue(detailTypes.contains(TelephonyNetworkDetailType.NETWORK_TYPE_NR))
    }

    @Test
    fun telephonyNetworkState_storesValues() {
        val state =
            TelephonyNetworkState(
                networkTypeState = TelephonyNetworkType.CONNECT_5G,
                networkTypeDetailState = TelephonyNetworkDetailType.NETWORK_TYPE_NR,
            )

        assertEquals(TelephonyNetworkType.CONNECT_5G, state.networkTypeState)
        assertEquals(TelephonyNetworkDetailType.NETWORK_TYPE_NR, state.networkTypeDetailState)
    }
}
