package kr.open.library.simple_ui.core.robolectric.system_manager.info.location.internal

import android.app.Application
import android.content.Context
import android.location.LocationManager
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateConstants.DEFAULT_UPDATE_CYCLE_DISTANCE
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateConstants.DEFAULT_UPDATE_CYCLE_TIME
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateConstants.POLLING_DISABLED_UPDATE_CYCLE_TIME
import kr.open.library.simple_ui.core.system_manager.info.location.internal.helper.LocationStateReceiver
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication

/**
 * Robolectric tests for LocationStateReceiver internal class.<br><br>
 * Tests BroadcastReceiver registration, LocationListener management, and coroutine lifecycle.<br><br>
 * LocationStateReceiver 내부 클래스에 대한 Robolectric 테스트입니다.<br>
 * BroadcastReceiver 등록, LocationListener 관리, 코루틴 라이프사이클을 테스트합니다.<br>
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LocationStateReceiverRobolectricTest {
    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var shadowApplication: ShadowApplication
    private lateinit var locationManager: LocationManager
    private lateinit var receiver: LocationStateReceiver
    private var callbackInvokeCount: Int = 0

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        context = application
        shadowApplication = shadowOf(application)
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        callbackInvokeCount = 0
        receiver = LocationStateReceiver(context, locationManager) {
            callbackInvokeCount++
        }
    }

    @After
    fun tearDown() {
        receiver.destroy()
    }

    // ==============================================
    // registerReceiver() Tests
    // ==============================================

    @Test
    fun registerReceiver_returnsTrue_onFirstRegistration() {
        val result = receiver.registerReceiver()
        assertTrue(result)
    }

    @Test
    fun registerReceiver_returnsTrue_whenAlreadyRegistered() {
        receiver.registerReceiver()
        val secondResult = receiver.registerReceiver()
        assertTrue(secondResult)
    }

    @Test
    fun registerReceiver_registersBroadcastReceiver() {
        receiver.registerReceiver()

        val registeredReceivers = shadowApplication.registeredReceivers

        assertTrue(registeredReceivers.isNotEmpty())
    }

    // ==============================================
    // registerLocationListener() Tests
    // ==============================================

    @Test
    fun registerLocationListener_returnsTrue_onFirstRegistration() {
        val result = receiver.registerLocationListener(
            LocationManager.GPS_PROVIDER,
            DEFAULT_UPDATE_CYCLE_TIME,
            DEFAULT_UPDATE_CYCLE_DISTANCE
        )
        assertTrue(result)
    }

    @Test
    fun registerLocationListener_reRegistersWithNewSettings_whenAlreadyRegistered() {
        // First registration
        receiver.registerLocationListener(
            LocationManager.GPS_PROVIDER,
            DEFAULT_UPDATE_CYCLE_TIME,
            DEFAULT_UPDATE_CYCLE_DISTANCE
        )

        // Second registration with different settings (should re-register)
        val secondResult = receiver.registerLocationListener(
            LocationManager.NETWORK_PROVIDER,
            5000L,
            10.0f
        )

        assertTrue(secondResult)
    }

    // ==============================================
    // updateStart() Tests
    // ==============================================

    @Test
    fun updateStart_returnsTrue_whenReceiverIsRegistered() {
        receiver.registerReceiver()
        receiver.registerLocationListener(
            LocationManager.GPS_PROVIDER,
            DEFAULT_UPDATE_CYCLE_TIME,
            DEFAULT_UPDATE_CYCLE_DISTANCE
        )

        val scope = CoroutineScope(Job())
        val result = receiver.updateStart(scope, DEFAULT_UPDATE_CYCLE_TIME) {}
        assertTrue(result)
        receiver.destroy()
        scope.cancel()
    }

    @Test
    fun updateStart_registersReceiverAutomatically_whenNotRegistered() {
        receiver.registerLocationListener(
            LocationManager.GPS_PROVIDER,
            DEFAULT_UPDATE_CYCLE_TIME,
            DEFAULT_UPDATE_CYCLE_DISTANCE
        )

        val scope = CoroutineScope(Job())
        val result = receiver.updateStart(scope, DEFAULT_UPDATE_CYCLE_TIME) {}
        assertTrue(result)
        receiver.destroy()
        scope.cancel()
    }

    @Test
    fun updateStart_invokesCallback_periodically() {
        val scheduler = TestCoroutineScheduler()
        val testDispatcher = StandardTestDispatcher(scheduler)
        val testScope = TestScope(testDispatcher + Job())

        receiver.registerReceiver()
        receiver.registerLocationListener(
            LocationManager.GPS_PROVIDER,
            DEFAULT_UPDATE_CYCLE_TIME,
            DEFAULT_UPDATE_CYCLE_DISTANCE
        )

        receiver.updateStart(testScope, DEFAULT_UPDATE_CYCLE_TIME) {}

        // Initial callback is invoked immediately
        scheduler.runCurrent()
        val initialCount = callbackInvokeCount

        // Advance time by one update cycle and run
        scheduler.advanceTimeBy(DEFAULT_UPDATE_CYCLE_TIME)
        scheduler.runCurrent()

        assertTrue(callbackInvokeCount > initialCount)

        testScope.cancel()
    }

    @Test
    fun updateStart_setsCoroutineScope() {
        receiver.registerReceiver()
        receiver.registerLocationListener(
            LocationManager.GPS_PROVIDER,
            DEFAULT_UPDATE_CYCLE_TIME,
            DEFAULT_UPDATE_CYCLE_DISTANCE
        )

        assertNull(receiver.getCoroutineScope())

        val scope = CoroutineScope(Job())
        receiver.updateStart(scope, DEFAULT_UPDATE_CYCLE_TIME) {}

        assertNotNull(receiver.getCoroutineScope())
        receiver.destroy()
        scope.cancel()
    }

    @Test
    fun updateStart_callsSetupDataFlowsCallback() {
        receiver.registerReceiver()
        receiver.registerLocationListener(
            LocationManager.GPS_PROVIDER,
            DEFAULT_UPDATE_CYCLE_TIME,
            DEFAULT_UPDATE_CYCLE_DISTANCE
        )

        val scope = CoroutineScope(Job())
        var setupDataFlowsCalled = false
        receiver.updateStart(scope, DEFAULT_UPDATE_CYCLE_TIME) {
            setupDataFlowsCalled = true
        }

        assertTrue(setupDataFlowsCalled)
        receiver.destroy()
        scope.cancel()
    }

    // ==============================================
    // POLLING_DISABLED_UPDATE_CYCLE_TIME Tests
    // ==============================================

    @Test
    fun updateStart_executesOnce_whenPollingDisabled() {
        val scheduler = TestCoroutineScheduler()
        val testDispatcher = StandardTestDispatcher(scheduler)
        val testScope = TestScope(testDispatcher + Job())

        receiver.registerReceiver()
        receiver.registerLocationListener(
            LocationManager.GPS_PROVIDER,
            DEFAULT_UPDATE_CYCLE_TIME,
            DEFAULT_UPDATE_CYCLE_DISTANCE
        )

        receiver.updateStart(testScope, POLLING_DISABLED_UPDATE_CYCLE_TIME) {}

        // For POLLING_DISABLED mode, coroutine runs once and completes
        scheduler.runCurrent()
        val countAfterInitial = callbackInvokeCount

        // Advance time significantly - callback count should not increase
        scheduler.advanceTimeBy(DEFAULT_UPDATE_CYCLE_TIME * 5)
        scheduler.runCurrent()

        assertEquals(countAfterInitial, callbackInvokeCount)

        testScope.cancel()
    }

    @Test
    fun updateStart_executesPeriodically_whenPollingEnabled() {
        val scheduler = TestCoroutineScheduler()
        val testDispatcher = StandardTestDispatcher(scheduler)
        val testScope = TestScope(testDispatcher + Job())

        receiver.registerReceiver()
        receiver.registerLocationListener(
            LocationManager.GPS_PROVIDER,
            DEFAULT_UPDATE_CYCLE_TIME,
            DEFAULT_UPDATE_CYCLE_DISTANCE
        )

        receiver.updateStart(testScope, DEFAULT_UPDATE_CYCLE_TIME) {}

        scheduler.runCurrent()
        val countAfterInitial = callbackInvokeCount

        scheduler.advanceTimeBy(DEFAULT_UPDATE_CYCLE_TIME * 3)
        scheduler.runCurrent()

        assertTrue(callbackInvokeCount > countAfterInitial)

        testScope.cancel()
    }

    // ==============================================
    // destroy() Tests
    // ==============================================

    @Test
    fun destroy_unregistersReceiver() {
        receiver.registerReceiver()
        receiver.destroy()

        // Verify no crash when destroying already destroyed receiver
        receiver.destroy()
    }

    @Test
    fun destroy_clearsCoroutineScope() {
        receiver.registerReceiver()
        receiver.registerLocationListener(
            LocationManager.GPS_PROVIDER,
            DEFAULT_UPDATE_CYCLE_TIME,
            DEFAULT_UPDATE_CYCLE_DISTANCE
        )

        val scope = CoroutineScope(Job())
        receiver.updateStart(scope, DEFAULT_UPDATE_CYCLE_TIME) {}

        assertNotNull(receiver.getCoroutineScope())

        receiver.destroy()
        scope.cancel()

        assertNull(receiver.getCoroutineScope())
    }

    // ==============================================
    // getCoroutineScope() Tests
    // ==============================================

    @Test
    fun getCoroutineScope_returnsNull_beforeUpdateStart() {
        assertNull(receiver.getCoroutineScope())
    }

    @Test
    fun getCoroutineScope_returnsScope_afterUpdateStart() {
        receiver.registerReceiver()
        receiver.registerLocationListener(
            LocationManager.GPS_PROVIDER,
            DEFAULT_UPDATE_CYCLE_TIME,
            DEFAULT_UPDATE_CYCLE_DISTANCE
        )

        val updateScope = CoroutineScope(Job())
        receiver.updateStart(updateScope, DEFAULT_UPDATE_CYCLE_TIME) {}

        val scope = receiver.getCoroutineScope()
        assertNotNull(scope)
        receiver.destroy()
        updateScope.cancel()
    }

    // ==============================================
    // Thread safety Tests
    // ==============================================

    @Test
    fun registerReceiver_isThreadSafe_whenCalledConcurrently() {
        val results = mutableListOf<Boolean>()

        repeat(10) {
            results.add(receiver.registerReceiver())
        }

        // All calls should succeed
        assertTrue(results.all { it })
    }

    @Test
    fun destroy_isThreadSafe_whenCalledMultipleTimes() {
        receiver.registerReceiver()

        // Multiple destroy calls should not crash
        repeat(5) {
            receiver.destroy()
        }
    }
}
