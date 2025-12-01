package kr.open.library.simple_ui.core.robolectric.system_manager.info.network.telephony.callback

import android.os.Build
import android.telephony.CellInfo
import android.telephony.ServiceState
import android.telephony.SignalStrength
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.callback.CommonTelephonyCallback
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.current.CurrentCellInfo
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.current.CurrentServiceState
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.current.CurrentSignalStrength
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state.TelephonyNetworkDetailType
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state.TelephonyNetworkState
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state.TelephonyNetworkType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class CommonTelephonyCallbackTest {
    private val telephonyManager = mock(TelephonyManager::class.java)
    private lateinit var callback: CommonTelephonyCallback
    private var originalSdk = Build.VERSION.SDK_INT

    @Before
    fun setUp() {
        callback = CommonTelephonyCallback(telephonyManager)
        originalSdk = Build.VERSION.SDK_INT
    }

    @After
    fun tearDown() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", originalSdk)
    }

    @Test
    fun `base telephony callback dispatches events and deduplicates updates`() {
        var dataState: Pair<Int, Int>? = null
        var serviceState: CurrentServiceState? = null
        var signalStrength: CurrentSignalStrength? = null
        var callState: Pair<Int, String?>? = null
        var displayInfo: TelephonyDisplayInfo? = null
        var activeSubId: Int? = null
        var networkEvents = 0
        var lastNetworkState: TelephonyNetworkState? = null

        callback.setOnDataConnectionState { state, type -> dataState = state to type }
        callback.setOnServiceState { serviceState = it }
        callback.setOnSignalStrength { signalStrength = it }
        callback.setOnCallState { state, number -> callState = state to number }
        callback.setOnDisplay { info -> displayInfo = info }
        callback.setOnActiveDataSubId { activeSubId = it }
        callback.setOnTelephonyNetworkType {
            networkEvents++
            lastNetworkState = it
        }
        doReturn(TelephonyManager.NETWORK_TYPE_LTE).`when`(telephonyManager).dataNetworkType

        val baseCallback = callback.baseTelephonyCallback
        baseCallback.onDataConnectionStateChanged(
            TelephonyManager.DATA_CONNECTED,
            TelephonyManager.NETWORK_TYPE_LTE,
        )
        assertEquals(TelephonyManager.DATA_CONNECTED, dataState?.first)
        assertEquals(1, networkEvents)
        assertEquals(TelephonyNetworkType.CONNECT_4G, lastNetworkState?.networkTypeState)

        // duplicate update should not trigger listeners again
        baseCallback.onDataConnectionStateChanged(
            TelephonyManager.DATA_CONNECTED,
            TelephonyManager.NETWORK_TYPE_LTE,
        )
        assertEquals(1, networkEvents)

        val service = ServiceState()
        baseCallback.onServiceStateChanged(service)
        assertSame(service, serviceState?.serviceState)

        val signal = mock(SignalStrength::class.java)
        baseCallback.onSignalStrengthsChanged(signal)
        assertSame(signal, signalStrength?.signalStrength)

        baseCallback.onCallStateChanged(TelephonyManager.CALL_STATE_RINGING)
        assertEquals(TelephonyManager.CALL_STATE_RINGING, callState?.first)
        assertNull(callState?.second)

        baseCallback.onActiveDataSubscriptionIdChanged(7)
        assertEquals(7, activeSubId)

        val telephonyDisplayInfo = mock(TelephonyDisplayInfo::class.java)
        doReturn(TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED)
            .`when`(telephonyDisplayInfo)
            .overrideNetworkType
        baseCallback.onDisplayInfoChanged(telephonyDisplayInfo)
        assertSame(telephonyDisplayInfo, displayInfo)
        assertEquals(2, networkEvents)
        assertEquals(TelephonyNetworkType.CONNECT_5G, lastNetworkState?.networkTypeState)
        assertEquals(
            TelephonyNetworkDetailType.OVERRIDE_NETWORK_TYPE_NR_ADVANCED,
            lastNetworkState?.networkTypeDetailState,
        )
    }

    @Test
    fun `base phone state listener mirrors callbacks`() {
        var activeSubId: Int? = null
        var networkState: TelephonyNetworkState? = null
        var displayInfo: TelephonyDisplayInfo? = null
        var callInfo: Pair<Int, String?>? = null
        var signal: CurrentSignalStrength? = null
        var service: CurrentServiceState? = null
        var cellInfo: CurrentCellInfo? = null

        callback.setOnActiveDataSubId { activeSubId = it }
        callback.setOnTelephonyNetworkType { networkState = it }
        callback.setOnDisplay { displayInfo = it }
        callback.setOnCallState { state, number -> callInfo = state to number }
        callback.setOnSignalStrength { signal = it }
        callback.setOnServiceState { service = it }
        callback.setOnCellInfo { cellInfo = it }

        val listener = callback.basePhoneStateListener
        doReturn(TelephonyManager.NETWORK_TYPE_HSPA).`when`(telephonyManager).dataNetworkType

        listener.onActiveDataSubscriptionIdChanged(3)
        assertEquals(3, activeSubId)

        listener.onDataConnectionStateChanged(
            TelephonyManager.DATA_CONNECTING,
            TelephonyManager.NETWORK_TYPE_HSPA,
        )
        assertEquals(TelephonyNetworkType.CONNECTING, networkState?.networkTypeState)

        val serviceState = ServiceState()
        listener.onServiceStateChanged(serviceState)
        assertSame(serviceState, service?.serviceState)

        val strength = mock(SignalStrength::class.java)
        listener.onSignalStrengthsChanged(strength)
        assertSame(strength, signal?.signalStrength)

        listener.onCallStateChanged(TelephonyManager.CALL_STATE_OFFHOOK, "010")
        assertEquals(TelephonyManager.CALL_STATE_OFFHOOK, callInfo?.first)
        assertEquals("010", callInfo?.second)

        val telephonyDisplayInfo = mock(TelephonyDisplayInfo::class.java)
        doReturn(TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA)
            .`when`(telephonyDisplayInfo)
            .overrideNetworkType
        listener.onDisplayInfoChanged(telephonyDisplayInfo)
        assertSame(telephonyDisplayInfo, displayInfo)
        assertEquals(TelephonyNetworkType.CONNECT_5G, networkState?.networkTypeState)

        val infos = mutableListOf(mock(CellInfo::class.java))
        listener.onCellInfoChanged(infos)
        assertEquals(1, cellInfo?.cellInfo?.size)
    }

    @Test
    fun `gps telephony callback delivers cell info`() {
        var delivered: CurrentCellInfo? = null
        callback.setOnCellInfo { delivered = it }

        val gpsCallback = callback.baseGpsTelephonyCallback
        val infos = mutableListOf(mock(CellInfo::class.java))
        gpsCallback.onCellInfoChanged(infos)

        assertEquals(1, delivered?.cellInfo?.size)
    }

    @Test
    fun `service state fallback promotes to 5g when sdk below r`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", Build.VERSION_CODES.Q)
        var networkState: TelephonyNetworkState? = null
        callback.setOnTelephonyNetworkType { networkState = it }
        doReturn(TelephonyManager.NETWORK_TYPE_LTE).`when`(telephonyManager).dataNetworkType
        val serviceState = mock(ServiceState::class.java)
        doReturn("nrState=CONNECTED something nsaState=5").`when`(serviceState).toString()

        val method =
            CommonTelephonyCallback::class.java.getDeclaredMethod(
                "getTelephonyServiceStateNetworkCheck",
                ServiceState::class.java,
            )
        method.isAccessible = true
        method.invoke(callback, serviceState)

        assertEquals(TelephonyNetworkType.CONNECT_5G, networkState?.networkTypeState)
        assertEquals(TelephonyNetworkDetailType.NETWORK_TYPE_NR, networkState?.networkTypeDetailState)
    }
}
