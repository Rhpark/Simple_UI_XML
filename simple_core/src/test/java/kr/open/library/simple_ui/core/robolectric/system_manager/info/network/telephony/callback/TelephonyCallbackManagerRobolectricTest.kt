package kr.open.library.simple_ui.core.robolectric.system_manager.info.network.telephony.callback

import android.Manifest
import android.app.Application
import android.content.Context
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.SignalStrength
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyCallback
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import android.util.SparseArray
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.callback.CommonTelephonyCallback
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.callback.TelephonyCallbackManager
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.current.CurrentCellInfo
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.current.CurrentServiceState
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.current.CurrentSignalStrength
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state.TelephonyNetworkState
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state.TelephonyNetworkType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.util.concurrent.Executor

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class TelephonyCallbackManagerRobolectricTest {

    private lateinit var application: Application
    private lateinit var context: Context
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var subscriptionManager: SubscriptionManager
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

        callbackManager = TelephonyCallbackManager(context)
        callbackManager.refreshPermissions()
    }

    @Test
    fun registerSimpleCallback_onS_updatesStateFlowsAndInvokesLambda() {
        val signal = mock(SignalStrength::class.java)
        val serviceState = ServiceState()
        var emittedSignal: SignalStrength? = null
        var emittedService: ServiceState? = null
        var emittedNetwork: TelephonyNetworkState? = null

        val registered = callbackManager.registerSimpleCallback(
            onSignalStrengthChanged = { emittedSignal = it },
            onServiceStateChanged = { emittedService = it },
            onNetworkStateChanged = { emittedNetwork = it }
        )
        assertTrue(registered)

        val executorCaptor = ArgumentCaptor.forClass(Executor::class.java)
        val callbackCaptor = ArgumentCaptor.forClass(TelephonyCallback::class.java)
        verify(telephonyManager).registerTelephonyCallback(executorCaptor.capture(), callbackCaptor.capture())

        val telephonyCallback = callbackCaptor.value as CommonTelephonyCallback.BaseTelephonyCallback
        telephonyCallback.onSignalStrengthsChanged(signal)
        telephonyCallback.onServiceStateChanged(serviceState)
        val displayInfo = mock(TelephonyDisplayInfo::class.java)
        doReturn(TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED)
            .`when`(displayInfo).overrideNetworkType
        telephonyCallback.onDisplayInfoChanged(displayInfo)

        assertSame(signal, emittedSignal)
        assertSame(signal, callbackManager.currentSignalStrength.value)
        assertSame(serviceState, emittedService)
        assertSame(serviceState, callbackManager.currentServiceState.value)
        assertEquals(TelephonyNetworkType.CONNECT_5G, emittedNetwork?.networkTypeState)
        assertEquals(TelephonyNetworkType.CONNECT_5G, callbackManager.currentNetworkState.value?.networkTypeState)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun registerSimpleCallback_onPreS_usesLegacyPhoneStateListener() {
        // Re-run setup under pre-S configuration
        setUp()

        val registered = callbackManager.registerSimpleCallback()
        assertTrue(registered)

        val listenerCaptor = ArgumentCaptor.forClass(PhoneStateListener::class.java)
        val eventsCaptor = ArgumentCaptor.forClass(Int::class.java)
        verify(telephonyManager).listen(listenerCaptor.capture(), eventsCaptor.capture())

        val expectedEvents =
            PhoneStateListener.LISTEN_SIGNAL_STRENGTHS or
                    PhoneStateListener.LISTEN_SERVICE_STATE or
                    PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
        assertEquals(expectedEvents, eventsCaptor.value.toInt())
        assertTrue(listenerCaptor.value is PhoneStateListener)
    }

    @Test
    fun unregisterSimpleCallback_withoutRegistration_returnsFalse() {
        assertFalse(callbackManager.unregisterSimpleCallback())
    }

    @Test
    fun unregisterSimpleCallback_onS_callsTelephonyManagerUnregister() {
        callbackManager.registerSimpleCallback()

        val result = callbackManager.unregisterSimpleCallback()

        assertTrue(result)
        assertFalse(callbackManager.unregisterSimpleCallback())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun unregisterSimpleCallback_onPreS_stopsLegacyListener() {
        setUp()
        callbackManager.registerSimpleCallback()

        callbackManager.unregisterSimpleCallback()

        verify(telephonyManager).listen(any(PhoneStateListener::class.java), eq(PhoneStateListener.LISTEN_NONE))
    }

    @Test
    fun registerAdvancedCallbackFromDefaultUSim_registersSlotManagers() {
        val subscriptionInfo = mock(SubscriptionInfo::class.java)
        val slotTelephonyManager = mock(TelephonyManager::class.java)
        doReturn(0).`when`(subscriptionInfo).simSlotIndex
        doReturn(5).`when`(subscriptionInfo).subscriptionId
        doReturn(listOf(subscriptionInfo)).`when`(subscriptionManager).activeSubscriptionInfoList
        doReturn(slotTelephonyManager).`when`(telephonyManager).createForSubscriptionId(5)

        var collectedSubId = -1
        val executor = Executor { runnable -> runnable.run() }

        callbackManager.clearMultiSimState()
        callbackManager.forceUpdateMultiSimData()

        val registered = callbackManager.registerAdvancedCallbackFromDefaultUSim(
            executor = executor,
            isGpsOn = false,
            onActiveDataSubId = { collectedSubId = it }
        )
        assertTrue(registered)

        val callbackCaptor = ArgumentCaptor.forClass(TelephonyCallback::class.java)
        verify(slotTelephonyManager).registerTelephonyCallback(eq(executor), callbackCaptor.capture())

        val slotCallback = callbackCaptor.value as CommonTelephonyCallback.BaseTelephonyCallback
        slotCallback.onActiveDataSubscriptionIdChanged(7)
        assertEquals(7, collectedSubId)

        assertTrue(callbackManager.isRegistered(0))
        assertSame(slotTelephonyManager, callbackManager.getTelephonyManagerFromUSim(0))

        callbackManager.unregisterAdvancedCallback(0)
        assertFalse(callbackManager.isRegistered(0))
    }

    @Test
    fun registerAdvancedCallbackFromDefaultUSim_withGps_usesGpsCallback() {
        val subscriptionInfo = mock(SubscriptionInfo::class.java)
        val slotTelephonyManager = mock(TelephonyManager::class.java)
        doReturn(0).`when`(subscriptionInfo).simSlotIndex
        doReturn(9).`when`(subscriptionInfo).subscriptionId
        doReturn(listOf(subscriptionInfo)).`when`(subscriptionManager).activeSubscriptionInfoList
        doReturn(slotTelephonyManager).`when`(telephonyManager).createForSubscriptionId(9)

        val executor = Executor { it.run() }
        callbackManager.clearMultiSimState()
        callbackManager.forceUpdateMultiSimData()

        val registered = callbackManager.registerAdvancedCallbackFromDefaultUSim(
            executor = executor,
            isGpsOn = true
        )
        assertTrue(registered)

        val callbackCaptor = ArgumentCaptor.forClass(TelephonyCallback::class.java)
        verify(slotTelephonyManager).registerTelephonyCallback(eq(executor), callbackCaptor.capture())
        val callbackClass = callbackCaptor.value::class.qualifiedName
        assertTrue(callbackClass?.contains("BaseGpsTelephonyCallback") == true)
    }

    @Test
    fun registerAdvancedCallbackFromDefaultUSim_whenNoSim_returnsFalse() {
        doReturn(emptyList<SubscriptionInfo>()).`when`(subscriptionManager).activeSubscriptionInfoList
        callbackManager.clearMultiSimState()
        callbackManager.forceUpdateMultiSimData()
        assertFalse(
            callbackManager.registerAdvancedCallbackFromDefaultUSim(
                executor = Executor { it.run() },
                isGpsOn = false
            )
        )
    }

    @Test(expected = IllegalStateException::class)
    fun registerAdvancedCallback_whenSlotMissing_throws() {
        callbackManager.clearMultiSimState()
        callbackManager.registerAdvancedCallback(
            simSlotIndex = 10,
            executor = Executor { it.run() },
            isGpsOn = false
        )
    }

    @Test
    fun unregisterAdvancedCallback_whenSlotMissing_doesNotThrow() {
        callbackManager.unregisterAdvancedCallback(99)
    }

    @Test
    fun setCallbacks_whenSlotMissing_doesNothing() {
        callbackManager.setOnSignalStrength(42) { }
        callbackManager.setOnServiceState(42) { }
        callbackManager.setOnActiveDataSubId(42) { }
        callbackManager.setOnDataConnectionState(42) { _, _ -> }
        callbackManager.setOnCellInfo(42) { }
        callbackManager.setOnCallState(42) { _, _ -> }
        callbackManager.setOnDisplayState(42) { }
        callbackManager.setOnTelephonyNetworkType(42) { }
    }

    @Test
    fun setCallbacks_whenSlotPresent_delegatesToCommonCallback() {
        val slotIndex = 3
        val slotTelephonyManager = mock(TelephonyManager::class.java)
        val callbackSpy = spy(CommonTelephonyCallback(slotTelephonyManager))
        callbackManager.injectSlot(slotIndex, callbackSpy, slotTelephonyManager)

        val signalListener: (CurrentSignalStrength) -> Unit = {}
        val serviceListener: (CurrentServiceState) -> Unit = {}
        val activeSubListener: (Int) -> Unit = {}
        val dataConnectionListener: (Int, Int) -> Unit = { _, _ -> }
        val cellInfoListener: (CurrentCellInfo) -> Unit = {}
        val callStateListener: (Int, String?) -> Unit = { _, _ -> }
        val displayListener: (TelephonyDisplayInfo) -> Unit = {}
        val telephonyStateListener: (TelephonyNetworkState) -> Unit = {}

        callbackManager.setOnSignalStrength(slotIndex, signalListener)
        callbackManager.setOnServiceState(slotIndex, serviceListener)
        callbackManager.setOnActiveDataSubId(slotIndex, activeSubListener)
        callbackManager.setOnDataConnectionState(slotIndex, dataConnectionListener)
        callbackManager.setOnCellInfo(slotIndex, cellInfoListener)
        callbackManager.setOnCallState(slotIndex, callStateListener)
        callbackManager.setOnDisplayState(slotIndex, displayListener)
        callbackManager.setOnTelephonyNetworkType(slotIndex, telephonyStateListener)

        verify(callbackSpy).setOnSignalStrength(signalListener)
        verify(callbackSpy).setOnServiceState(serviceListener)
        verify(callbackSpy).setOnActiveDataSubId(activeSubListener)
        verify(callbackSpy).setOnDataConnectionState(dataConnectionListener)
        verify(callbackSpy).setOnCellInfo(cellInfoListener)
        verify(callbackSpy).setOnCallState(callStateListener)
        verify(callbackSpy).setOnDisplay(displayListener)
        verify(callbackSpy).setOnTelephonyNetworkType(telephonyStateListener)
    }

    @Test
    fun registerAdvancedCallback_usesBaseCallbackWhenGpsOff() {
        val slotIndex = 4
        val slotTelephonyManager = mock(TelephonyManager::class.java)
        val callbackSpy = spy(CommonTelephonyCallback(slotTelephonyManager))
        callbackManager.injectSlot(slotIndex, callbackSpy, slotTelephonyManager)

        val executor = Executor { it.run() }
        callbackManager.registerAdvancedCallback(
            simSlotIndex = slotIndex,
            executor = executor,
            isGpsOn = false,
            onActiveDataSubId = {},
            onDataConnectionState = { _, _ -> },
            onCellInfo = {},
            onSignalStrength = {},
            onServiceState = {},
            onCallState = { _, _ -> },
            onDisplayInfo = {},
            onTelephonyNetworkState = {}
        )

        val callbackCaptor = ArgumentCaptor.forClass(TelephonyCallback::class.java)
        verify(slotTelephonyManager).registerTelephonyCallback(any(Executor::class.java), callbackCaptor.capture())
        assertSame(callbackSpy.baseTelephonyCallback, callbackCaptor.value)
        assertTrue(callbackManager.isRegistered(slotIndex))
    }

    @Test
    fun registerAdvancedCallback_usesGpsCallbackWhenGpsOn() {
        val slotIndex = 6
        val slotTelephonyManager = mock(TelephonyManager::class.java)
        val callbackSpy = spy(CommonTelephonyCallback(slotTelephonyManager))
        callbackManager.injectSlot(slotIndex, callbackSpy, slotTelephonyManager)

        val executor = Executor { it.run() }
        callbackManager.registerAdvancedCallback(
            simSlotIndex = slotIndex,
            executor = executor,
            isGpsOn = true,
            onActiveDataSubId = {},
            onDataConnectionState = { _, _ -> },
            onCellInfo = {},
            onSignalStrength = {},
            onServiceState = {},
            onCallState = { _, _ -> },
            onDisplayInfo = {},
            onTelephonyNetworkState = {}
        )

        val gpsCallbackCaptor = ArgumentCaptor.forClass(TelephonyCallback::class.java)
        verify(slotTelephonyManager).registerTelephonyCallback(any(Executor::class.java), gpsCallbackCaptor.capture())
        assertSame(callbackSpy.baseGpsTelephonyCallback, gpsCallbackCaptor.value)
        assertTrue(callbackManager.isRegistered(slotIndex))
    }

    @Test
    fun onDestroy_unregistersCallbacksSafely() {
        val subscriptionInfo = mock(SubscriptionInfo::class.java)
        val slotTelephonyManager = mock(TelephonyManager::class.java)
        doReturn(0).`when`(subscriptionInfo).simSlotIndex
        doReturn(11).`when`(subscriptionInfo).subscriptionId
        doReturn(listOf(subscriptionInfo)).`when`(subscriptionManager).activeSubscriptionInfoList
        doReturn(slotTelephonyManager).`when`(telephonyManager).createForSubscriptionId(11)

        callbackManager.forceUpdateMultiSimData()
        callbackManager.registerSimpleCallback()
        callbackManager.registerAdvancedCallbackFromDefaultUSim(Executor { it.run() }, isGpsOn = false)

        callbackManager.onDestroy()
        assertFalse(callbackManager.isRegistered(0))
    }

    @Test
    fun initializeMultiSimSupport_withoutPermission_doesNotPopulate() {
        val shadowApp = Shadows.shadowOf(application)
        shadowApp.denyPermissions(Manifest.permission.READ_PHONE_STATE)

        val manager = TelephonyCallbackManager(application)
        manager.refreshPermissions()
        assertFalse(manager.isRegistered(0))

        shadowApp.grantPermissions(Manifest.permission.READ_PHONE_STATE)
    }

    @Test
    fun initializeMultiSimSupport_whenCreateForSubscriptionThrows_securityHandled() {
        val subscriptionInfo = mock(SubscriptionInfo::class.java)
        doReturn(0).`when`(subscriptionInfo).simSlotIndex
        doReturn(33).`when`(subscriptionInfo).subscriptionId
        doReturn(listOf(subscriptionInfo)).`when`(subscriptionManager).activeSubscriptionInfoList
        doThrow(SecurityException("denied")).`when`(telephonyManager).createForSubscriptionId(33)

        val manager = TelephonyCallbackManager(context)
        manager.refreshPermissions()

        assertNull(manager.getTelephonyManagerFromUSim(0))
    }

    @Test(expected = IllegalStateException::class)
    fun registerAdvancedCallback_whenCallbackMissing_throws() {
        prepareMultiSimSlot()
        callbackManager.removeCallbackEntry(0)

        callbackManager.registerAdvancedCallback(
            simSlotIndex = 0,
            executor = Executor { it.run() },
            isGpsOn = false
        )
    }

    @Test
    fun unregisterAdvancedCallback_handlesSecurityThenIllegalExceptions() {
        val slotTelephonyManager = prepareMultiSimSlot(subscriptionId = 40)
        callbackManager.registerAdvancedCallback(0, Executor { it.run() }, isGpsOn = false)

        doThrow(SecurityException("base")).doThrow(IllegalArgumentException("gps"))
            .`when`(slotTelephonyManager).unregisterTelephonyCallback(any())

        callbackManager.unregisterAdvancedCallback(0)
    }

    @Test
    fun unregisterAdvancedCallback_handlesIllegalThenSecurityExceptions() {
        val slotTelephonyManager = prepareMultiSimSlot(subscriptionId = 41)
        callbackManager.registerAdvancedCallback(0, Executor { it.run() }, isGpsOn = true)

        doThrow(IllegalArgumentException("base")).doThrow(SecurityException("gps"))
            .`when`(slotTelephonyManager).unregisterTelephonyCallback(any())

        callbackManager.unregisterAdvancedCallback(0)
    }

    private fun prepareMultiSimSlot(slotIndex: Int = 0, subscriptionId: Int = 1): TelephonyManager {
        val subscriptionInfo = mock(SubscriptionInfo::class.java)
        val slotTelephonyManager = mock(TelephonyManager::class.java)
        doReturn(slotIndex).`when`(subscriptionInfo).simSlotIndex
        doReturn(subscriptionId).`when`(subscriptionInfo).subscriptionId
        doReturn(listOf(subscriptionInfo)).`when`(subscriptionManager).activeSubscriptionInfoList
        doReturn(slotTelephonyManager).`when`(telephonyManager).createForSubscriptionId(subscriptionId)
        callbackManager.clearMultiSimState()
        callbackManager.forceUpdateMultiSimData()
        return slotTelephonyManager
    }

    private fun TelephonyCallbackManager.removeCallbackEntry(slotIndex: Int) {
        val callbackField = TelephonyCallbackManager::class.java.getDeclaredField("uSimTelephonyCallbackList")
        callbackField.isAccessible = true
        val array = callbackField.get(this) as SparseArray<*>
        array.remove(slotIndex)
    }

    private fun TelephonyCallbackManager.forceUpdateMultiSimData() {
        val method = TelephonyCallbackManager::class.java.getDeclaredMethod("updateUSimTelephonyManagerList")
        method.isAccessible = true
        method.invoke(this)
    }

    private fun TelephonyCallbackManager.clearMultiSimState() {
        val managerField = TelephonyCallbackManager::class.java.getDeclaredField("uSimTelephonyManagerList")
        managerField.isAccessible = true
        val callbackField = TelephonyCallbackManager::class.java.getDeclaredField("uSimTelephonyCallbackList")
        callbackField.isAccessible = true
        val registeredField = TelephonyCallbackManager::class.java.getDeclaredField("isRegistered")
        registeredField.isAccessible = true

        (managerField.get(this) as android.util.SparseArray<*>).clear()
        (callbackField.get(this) as android.util.SparseArray<*>).clear()
        (registeredField.get(this) as android.util.SparseBooleanArray).clear()
    }

    private fun TelephonyCallbackManager.injectSlot(
        slotIndex: Int,
        callback: CommonTelephonyCallback,
        manager: TelephonyManager
    ) {
        val managerField = TelephonyCallbackManager::class.java.getDeclaredField("uSimTelephonyManagerList")
        managerField.isAccessible = true
        val callbackField = TelephonyCallbackManager::class.java.getDeclaredField("uSimTelephonyCallbackList")
        callbackField.isAccessible = true
        val registeredField = TelephonyCallbackManager::class.java.getDeclaredField("isRegistered")
        registeredField.isAccessible = true

        (managerField.get(this) as android.util.SparseArray<TelephonyManager>).put(slotIndex, manager)
        (callbackField.get(this) as android.util.SparseArray<CommonTelephonyCallback>).put(slotIndex, callback)
        (registeredField.get(this) as android.util.SparseBooleanArray).put(slotIndex, false)
    }
}
