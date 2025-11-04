package kr.open.library.simple_ui.robolectric.system_manager.info.network.telephony.callback

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
import android.telephony.TelephonyManager
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.system_manager.info.network.telephony.callback.CommonTelephonyCallback
import kr.open.library.simple_ui.system_manager.info.network.telephony.callback.TelephonyCallbackManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
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

        val registered = callbackManager.registerSimpleCallback(
            onSignalStrengthChanged = { emittedSignal = it },
            onServiceStateChanged = { emittedService = it }
        )
        assertTrue(registered)

        val executorCaptor = ArgumentCaptor.forClass(Executor::class.java)
        val callbackCaptor = ArgumentCaptor.forClass(TelephonyCallback::class.java)
        verify(telephonyManager).registerTelephonyCallback(executorCaptor.capture(), callbackCaptor.capture())

        val telephonyCallback = callbackCaptor.value as CommonTelephonyCallback.BaseTelephonyCallback
        telephonyCallback.onSignalStrengthsChanged(signal)
        telephonyCallback.onServiceStateChanged(serviceState)

        assertSame(signal, emittedSignal)
        assertSame(signal, callbackManager.currentSignalStrength.value)
        assertSame(serviceState, emittedService)
        assertSame(serviceState, callbackManager.currentServiceState.value)
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
}
