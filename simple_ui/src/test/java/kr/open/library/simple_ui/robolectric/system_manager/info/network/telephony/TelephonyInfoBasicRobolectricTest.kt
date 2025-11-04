package kr.open.library.simple_ui.robolectric.system_manager.info.network.telephony

import android.Manifest
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.ServiceState
import android.telephony.SignalStrength
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.telephony.TelephonyDisplayInfo
import androidx.test.core.app.ApplicationProvider
import java.util.concurrent.Executor
import kr.open.library.simple_ui.system_manager.info.network.telephony.TelephonyInfo
import kr.open.library.simple_ui.system_manager.info.network.telephony.callback.TelephonyCallbackManager
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.current.CurrentCellInfo
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.current.CurrentServiceState
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.current.CurrentSignalStrength
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.state.TelephonyNetworkState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * Focused Robolectric tests covering TelephonyInfo's basic getter behaviour.
 *
 * Mirrors the scenarios previously validated against TelephonyBasicInfo.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class TelephonyInfoBasicRobolectricTest {

    private lateinit var application: Application
    private lateinit var context: Context
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var subscriptionManager: SubscriptionManager
    private lateinit var telephonyInfo: TelephonyInfo
    private lateinit var callbackManager: TelephonyCallbackManager

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        context = application
        telephonyManager = mock(TelephonyManager::class.java)
        subscriptionManager = mock(SubscriptionManager::class.java)

        val shadowApp = Shadows.shadowOf(application)
        shadowApp.grantPermissions(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_NUMBERS
        )
        shadowApp.setSystemService(Context.TELEPHONY_SERVICE, telephonyManager)
        shadowApp.setSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE, subscriptionManager)

        telephonyInfo = TelephonyInfo(context)
        telephonyInfo.refreshPermissions()

        callbackManager = mock(TelephonyCallbackManager::class.java)
        val field = TelephonyInfo::class.java.getDeclaredField("callbackManager")
        field.isAccessible = true
        field.set(telephonyInfo, callbackManager)
    }

    // Carrier info
    @Test
    fun getCarrierName_returnsValueWhenNonBlank() {
        doReturn("CarrierX").`when`(telephonyManager).networkOperatorName
        assertEquals("CarrierX", telephonyInfo.getCarrierName())
    }

    @Test
    fun getCarrierName_returnsNullWhenBlank() {
        doReturn(" ").`when`(telephonyManager).networkOperatorName
        assertNull(telephonyInfo.getCarrierName())
    }

    @Test
    fun getMobileCountryCode_returnsFirstThreeDigits() {
        doReturn("310260").`when`(telephonyManager).networkOperator
        assertEquals("310", telephonyInfo.getMobileCountryCode())
    }

    @Test
    fun getMobileCountryCode_returnsNullWhenOperatorTooShort() {
        doReturn("31").`when`(telephonyManager).networkOperator
        assertNull(telephonyInfo.getMobileCountryCode())
    }

    @Test
    fun getMobileNetworkCode_returnsSuffixWhenLengthValid() {
        doReturn("310260").`when`(telephonyManager).networkOperator
        assertEquals("260", telephonyInfo.getMobileNetworkCode())
    }

    @Test
    fun getMobileNetworkCode_returnsNullWhenLengthInvalid() {
        doReturn("3102").`when`(telephonyManager).networkOperator
        assertNull(telephonyInfo.getMobileNetworkCode())
    }

    // SIM info
    @Test
    fun getSimState_and_isSimReady_reflectTelephonyManagerState() {
        doReturn(TelephonyManager.SIM_STATE_READY).`when`(telephonyManager).simState
        assertEquals(TelephonyManager.SIM_STATE_READY, telephonyInfo.getSimState())
        assertTrue(telephonyInfo.isSimReady())

        doReturn(TelephonyManager.SIM_STATE_PIN_REQUIRED).`when`(telephonyManager).simState
        assertFalse(telephonyInfo.isSimReady())
    }

    @Test
    fun getSimOperatorName_returnsValue() {
        doReturn("OperatorY").`when`(telephonyManager).simOperatorName
        assertEquals("OperatorY", telephonyInfo.getSimOperatorName())
    }

    @Test
    fun getSimCountryIso_returnsNullWhenBlank() {
        doReturn("   ").`when`(telephonyManager).simCountryIso
        assertNull(telephonyInfo.getSimCountryIso())
    }

    @Test
    fun getSimCountryIso_returnsValueWhenNonBlank() {
        doReturn("kr").`when`(telephonyManager).simCountryIso
        assertEquals("kr", telephonyInfo.getSimCountryIso())
    }

    // Phone info
    @Test
    fun getPhoneNumber_returnsNullWhenBlank() {
        @Suppress("DEPRECATION")
        doReturn("").`when`(telephonyManager).line1Number
        assertNull(telephonyInfo.getPhoneNumber())
    }

    @Test
    fun getPhoneNumber_returnsValueWhenNonBlank() {
        @Suppress("DEPRECATION")
        doReturn("01012345678").`when`(telephonyManager).line1Number
        assertEquals("01012345678", telephonyInfo.getPhoneNumber())
    }

    @Test
    fun getCallState_returnsTelephonyValue() {
        doReturn(TelephonyManager.CALL_STATE_RINGING).`when`(telephonyManager).callState
        assertEquals(TelephonyManager.CALL_STATE_RINGING, telephonyInfo.getCallState())
    }

    // Network info
    @Test
    fun getNetworkType_onRPlus_returnsDataNetworkType() {
        doReturn(TelephonyManager.NETWORK_TYPE_NR).`when`(telephonyManager).dataNetworkType
        assertEquals(TelephonyManager.NETWORK_TYPE_NR, telephonyInfo.getNetworkType())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun getNetworkType_onPreR_usesLegacyNetworkType() {
        setUp()
        @Suppress("DEPRECATION")
        doReturn(TelephonyManager.NETWORK_TYPE_LTE).`when`(telephonyManager).networkType
        assertEquals(TelephonyManager.NETWORK_TYPE_LTE, telephonyInfo.getNetworkType())
    }

    @Test
    fun getDataNetworkType_returnsTelephonyValue() {
        doReturn(TelephonyManager.NETWORK_TYPE_LTE).`when`(telephonyManager).dataNetworkType
        assertEquals(TelephonyManager.NETWORK_TYPE_LTE, telephonyInfo.getDataNetworkType())
    }

    @Test
    fun isNetworkRoaming_reflectsTelephonyManager() {
        doReturn(true).`when`(telephonyManager).isNetworkRoaming
        assertTrue(telephonyInfo.isNetworkRoaming())

        doReturn(false).`when`(telephonyManager).isNetworkRoaming
        assertFalse(telephonyInfo.isNetworkRoaming())
    }

    // Multi SIM
    @Test
    fun getActiveSimCount_returnsSubscriptionCount() {
        doReturn(2).`when`(subscriptionManager).activeSubscriptionInfoCount
        assertEquals(2, telephonyInfo.getActiveSimCount())
    }

    @Test
    fun getActiveSubscriptionInfoList_returnsEmptyWhenNull() {
        doReturn(null).`when`(subscriptionManager).activeSubscriptionInfoList
        assertTrue(telephonyInfo.getActiveSubscriptionInfoList().isEmpty())
    }

    @Test
    fun getActiveSubscriptionInfoList_returnsProvidedList() {
        val info = mock(SubscriptionInfo::class.java)
        doReturn(listOf(info)).`when`(subscriptionManager).activeSubscriptionInfoList
        val list = telephonyInfo.getActiveSubscriptionInfoList()
        assertEquals(1, list.size)
        assertSame(info, list.first())
    }

    @Test
    fun getDefaultDataSubscriptionInfo_onRPlus_returnsSubscription() {
        val info = mock(SubscriptionInfo::class.java)
        doReturn(5).`when`(telephonyManager).subscriptionId
        doReturn(info).`when`(subscriptionManager).getActiveSubscriptionInfo(5)
        assertSame(info, telephonyInfo.getDefaultDataSubscriptionInfo())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getDefaultDataSubscriptionInfo_onPreR_usesFirstActiveSubscription() {
        setUp()
        val info = mock(SubscriptionInfo::class.java)
        doReturn(7).`when`(info).subscriptionId
        doReturn(listOf(info)).`when`(subscriptionManager).activeSubscriptionInfoList
        doReturn(info).`when`(subscriptionManager).getActiveSubscriptionInfo(7)
        assertSame(info, telephonyInfo.getDefaultDataSubscriptionInfo())
    }

    // Utility mapping coverage
    @Test
    fun getNetworkTypeString_coversAllMappings() {
        val cases = listOf(
            TelephonyManager.NETWORK_TYPE_GPRS to "GPRS",
            TelephonyManager.NETWORK_TYPE_EDGE to "EDGE",
            TelephonyManager.NETWORK_TYPE_UMTS to "UMTS",
            TelephonyManager.NETWORK_TYPE_HSDPA to "HSDPA",
            TelephonyManager.NETWORK_TYPE_HSUPA to "HSUPA",
            TelephonyManager.NETWORK_TYPE_HSPA to "HSPA",
            TelephonyManager.NETWORK_TYPE_CDMA to "CDMA",
            TelephonyManager.NETWORK_TYPE_EVDO_0 to "EVDO_0",
            TelephonyManager.NETWORK_TYPE_EVDO_A to "EVDO_A",
            TelephonyManager.NETWORK_TYPE_EVDO_B to "EVDO_B",
            TelephonyManager.NETWORK_TYPE_1xRTT to "1xRTT",
            TelephonyManager.NETWORK_TYPE_IDEN to "IDEN",
            TelephonyManager.NETWORK_TYPE_EHRPD to "EHRPD",
            TelephonyManager.NETWORK_TYPE_HSPAP to "HSPA+",
            TelephonyManager.NETWORK_TYPE_GSM to "GSM",
            TelephonyManager.NETWORK_TYPE_TD_SCDMA to "TD_SCDMA",
            TelephonyManager.NETWORK_TYPE_IWLAN to "IWLAN",
            19 to "LTE_CA",
            20 to "5G NR",
            TelephonyManager.NETWORK_TYPE_UNKNOWN to "UNKNOWN"
        )
        cases.forEach { (type, label) ->
            doReturn(type).`when`(telephonyManager).dataNetworkType
            assertEquals(label, telephonyInfo.getNetworkTypeString())
        }
    }

    @Test
    fun getSimStateString_coversAllMappings() {
        val cases = listOf(
            TelephonyManager.SIM_STATE_UNKNOWN to "UNKNOWN",
            TelephonyManager.SIM_STATE_ABSENT to "ABSENT",
            TelephonyManager.SIM_STATE_PIN_REQUIRED to "PIN_REQUIRED",
            TelephonyManager.SIM_STATE_PUK_REQUIRED to "PUK_REQUIRED",
            TelephonyManager.SIM_STATE_NETWORK_LOCKED to "NETWORK_LOCKED",
            TelephonyManager.SIM_STATE_READY to "READY",
            TelephonyManager.SIM_STATE_NOT_READY to "NOT_READY",
            TelephonyManager.SIM_STATE_PERM_DISABLED to "PERM_DISABLED",
            TelephonyManager.SIM_STATE_CARD_IO_ERROR to "CARD_IO_ERROR",
            TelephonyManager.SIM_STATE_CARD_RESTRICTED to "CARD_RESTRICTED",
            999 to "UNKNOWN"
        )
        cases.forEach { (state, label) ->
            doReturn(state).`when`(telephonyManager).simState
            assertEquals(label, telephonyInfo.getSimStateString())
        }
    }

    // Callback delegation
    @Test
    fun currentSignalStrength_returnsFlowFromCallbackManager() {
        // Default Mockito behaviour returns null; accessing the property ensures TelephonyInfo forwards the call.
        assertNull(telephonyInfo.currentSignalStrength)
    }

    @Test
    fun currentServiceState_returnsFlowFromCallbackManager() {
        assertNull(telephonyInfo.currentServiceState)
    }

    @Test
    fun currentNetworkState_returnsFlowFromCallbackManager() {
        assertNull(telephonyInfo.currentNetworkState)
    }

    @Test
    fun getCurrentSignalStrength_delegatesToCallbackManager() {
        doReturn(null).`when`(callbackManager).getCurrentSignalStrength()
        telephonyInfo.getCurrentSignalStrength()
        verify(callbackManager).getCurrentSignalStrength()
    }

    @Test
    fun getCurrentServiceState_delegatesToCallbackManager() {
        doReturn(null).`when`(callbackManager).getCurrentServiceState()
        telephonyInfo.getCurrentServiceState()
        verify(callbackManager).getCurrentServiceState()
    }

    @Test
    fun registerCallback_delegatesToCallbackManager() {
        val handler = Handler(Looper.getMainLooper())
        val signal: (SignalStrength) -> Unit = { }
        val service: (ServiceState) -> Unit = { }
        val network: (TelephonyNetworkState) -> Unit = { }

        telephonyInfo.registerCallback(handler, signal, service, network)

        verify(callbackManager).registerSimpleCallback(handler, signal, service, network)
    }

    @Test
    fun unregisterCallback_delegatesToCallbackManager() {
        telephonyInfo.unregisterCallback()
        verify(callbackManager).unregisterSimpleCallback()
    }

    @Test
    fun registerTelephonyCallBackFromDefaultUSim_delegatesAllParameters() {
        val executor = Executor { command -> command.run() }
        val onActiveDataSubId: (Int) -> Unit = { }
        val onDataConnectionState: (Int, Int) -> Unit = { _, _ -> }
        val onCellInfo: (CurrentCellInfo) -> Unit = { }
        val onSignalStrength: (CurrentSignalStrength) -> Unit = { }
        val onServiceState: (CurrentServiceState) -> Unit = { }
        val onCallState: (Int, String?) -> Unit = { _, _ -> }
        val onDisplayInfo: (TelephonyDisplayInfo) -> Unit = { }
        val onTelephonyNetworkState: (TelephonyNetworkState) -> Unit = { }

        telephonyInfo.registerTelephonyCallBackFromDefaultUSim(
            executor,
            true,
            onActiveDataSubId,
            onDataConnectionState,
            onCellInfo,
            onSignalStrength,
            onServiceState,
            onCallState,
            onDisplayInfo,
            onTelephonyNetworkState
        )

        verify(callbackManager).registerAdvancedCallbackFromDefaultUSim(
            executor,
            true,
            onActiveDataSubId,
            onDataConnectionState,
            onCellInfo,
            onSignalStrength,
            onServiceState,
            onCallState,
            onDisplayInfo,
            onTelephonyNetworkState
        )
    }

    @Test
    fun registerTelephonyCallBack_delegatesAllParameters() {
        val executor = Executor { command -> command.run() }
        val onActiveDataSubId: (Int) -> Unit = { }
        val onDataConnectionState: (Int, Int) -> Unit = { _, _ -> }
        val onCellInfo: (CurrentCellInfo) -> Unit = { }
        val onSignalStrength: (CurrentSignalStrength) -> Unit = { }
        val onServiceState: (CurrentServiceState) -> Unit = { }
        val onCallState: (Int, String?) -> Unit = { _, _ -> }
        val onDisplayInfo: (TelephonyDisplayInfo) -> Unit = { }
        val onTelephonyNetworkState: (TelephonyNetworkState) -> Unit = { }
        val slotIndex = 1

        telephonyInfo.registerTelephonyCallBack(
            slotIndex,
            executor,
            false,
            onActiveDataSubId,
            onDataConnectionState,
            onCellInfo,
            onSignalStrength,
            onServiceState,
            onCallState,
            onDisplayInfo,
            onTelephonyNetworkState
        )

        verify(callbackManager).registerAdvancedCallback(
            slotIndex,
            executor,
            false,
            onActiveDataSubId,
            onDataConnectionState,
            onCellInfo,
            onSignalStrength,
            onServiceState,
            onCallState,
            onDisplayInfo,
            onTelephonyNetworkState
        )
    }

    @Test
    fun unregisterCallBack_delegatesToCallbackManager() {
        val slotIndex = 2
        telephonyInfo.unregisterCallBack(slotIndex)
        verify(callbackManager).unregisterAdvancedCallback(slotIndex)
    }

    @Test
    fun setOnSignalStrength_delegatesToCallbackManager() {
        val slotIndex = 3
        val listener: (CurrentSignalStrength) -> Unit = { }
        telephonyInfo.setOnSignalStrength(slotIndex, listener)
        verify(callbackManager).setOnSignalStrength(slotIndex, listener)
    }

    @Test
    fun setOnServiceState_delegatesToCallbackManager() {
        val slotIndex = 4
        val listener: (CurrentServiceState) -> Unit = { }
        telephonyInfo.setOnServiceState(slotIndex, listener)
        verify(callbackManager).setOnServiceState(slotIndex, listener)
    }

    @Test
    fun setOnActiveDataSubId_delegatesToCallbackManager() {
        val slotIndex = 5
        val listener: (Int) -> Unit = { }
        telephonyInfo.setOnActiveDataSubId(slotIndex, listener)
        verify(callbackManager).setOnActiveDataSubId(slotIndex, listener)
    }

    @Test
    fun setOnDataConnectionState_delegatesToCallbackManager() {
        val slotIndex = 6
        val listener: (Int, Int) -> Unit = { _, _ -> }
        telephonyInfo.setOnDataConnectionState(slotIndex, listener)
        verify(callbackManager).setOnDataConnectionState(slotIndex, listener)
    }

    @Test
    fun setOnCellInfo_delegatesToCallbackManager() {
        val slotIndex = 7
        val listener: (CurrentCellInfo) -> Unit = { }
        telephonyInfo.setOnCellInfo(slotIndex, listener)
        verify(callbackManager).setOnCellInfo(slotIndex, listener)
    }

    @Test
    fun setOnCallState_delegatesToCallbackManager() {
        val slotIndex = 8
        val listener: (Int, String?) -> Unit = { _, _ -> }
        telephonyInfo.setOnCallState(slotIndex, listener)
        verify(callbackManager).setOnCallState(slotIndex, listener)
    }

    @Test
    fun setOnDisplayState_delegatesToCallbackManager() {
        val slotIndex = 9
        val listener: (TelephonyDisplayInfo) -> Unit = { }
        telephonyInfo.setOnDisplayState(slotIndex, listener)
        verify(callbackManager).setOnDisplayState(slotIndex, listener)
    }

    @Test
    fun setOnTelephonyNetworkType_delegatesToCallbackManager() {
        val slotIndex = 10
        val listener: (TelephonyNetworkState) -> Unit = { }
        telephonyInfo.setOnTelephonyNetworkType(slotIndex, listener)
        verify(callbackManager).setOnTelephonyNetworkType(slotIndex, listener)
    }

    @Test
    fun isRegistered_delegatesToCallbackManager() {
        doReturn(true).`when`(callbackManager).isRegistered(0)
        assertTrue(telephonyInfo.isRegistered(0))
        verify(callbackManager).isRegistered(0)
    }

    @Test
    fun getTelephonyManagerFromUSim_delegatesToCallbackManager() {
        val expected = mock(TelephonyManager::class.java)
        doReturn(expected).`when`(callbackManager).getTelephonyManagerFromUSim(1)
        assertSame(expected, telephonyInfo.getTelephonyManagerFromUSim(1))
        verify(callbackManager).getTelephonyManagerFromUSim(1)
    }

    @Test
    fun onDestroy_callsCallbackManagerBeforeCleanup() {
        telephonyInfo.onDestroy()
        verify(callbackManager).onDestroy()
    }

    // Accessors
    @Test
    fun telephonyManager_isAccessible() {
        assertNotNull(telephonyInfo.telephonyManager)
    }

    @Test
    fun subscriptionManager_isAccessible() {
        assertNotNull(telephonyInfo.subscriptionManager)
    }
}
