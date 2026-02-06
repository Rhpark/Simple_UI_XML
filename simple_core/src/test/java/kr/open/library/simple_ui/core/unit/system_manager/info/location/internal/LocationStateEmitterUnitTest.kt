package kr.open.library.simple_ui.core.unit.system_manager.info.location.internal

import android.location.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateEvent
import kr.open.library.simple_ui.core.system_manager.info.location.internal.helper.LocationStateEmitter
import kr.open.library.simple_ui.core.system_manager.info.location.internal.model.LocationStateData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

/**
 * Unit tests for LocationStateEmitter internal class.<br><br>
 * Tests event emission, StateFlow caching, and reactive flow setup.<br><br>
 * LocationStateEmitter 내부 클래스에 대한 단위 테스트입니다.<br>
 * 이벤트 발행, StateFlow 캐싱, 반응형 플로우 설정을 테스트합니다.<br>
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocationStateEmitterUnitTest {
    private lateinit var emitter: LocationStateEmitter

    @Before
    fun setUp() {
        emitter = LocationStateEmitter()
    }

    // ==============================================
    // getSfUpdate() Tests
    // ==============================================

    @Test
    fun getSfUpdate_returnsNonNullSharedFlow() {
        val sfUpdate = emitter.getSfUpdate()
        assertNotNull(sfUpdate)
    }

    // ==============================================
    // updateLocationInfo() + setupDataFlows() Tests
    // ==============================================

    @Test
    fun updateLocationInfo_emitsOnGpsEnabledEvent_whenGpsStateChanges() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)

        // Setup data flows with test scope
        val flowScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + Job())
        emitter.setupDataFlows(flowScope)

        val collectedEvents = mutableListOf<LocationStateEvent>()
        val collectJob = launch(testDispatcher) {
            emitter.getSfUpdate().take(1).toList(collectedEvents)
        }

        // Update with GPS enabled
        emitter.updateLocationInfo(
            LocationStateData(
                location = null,
                isGpsEnabled = true,
                isNetworkEnabled = null,
                isPassiveEnabled = null,
                isFusedEnabled = null
            )
        )

        collectJob.join()

        assertEquals(1, collectedEvents.size)
        assertTrue(collectedEvents[0] is LocationStateEvent.OnGpsEnabled)
        assertEquals(true, (collectedEvents[0] as LocationStateEvent.OnGpsEnabled).isEnabled)
    }

    @Test
    fun updateLocationInfo_emitsOnNetworkEnabledEvent_whenNetworkStateChanges() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)

        val flowScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + Job())
        emitter.setupDataFlows(flowScope)

        val collectedEvents = mutableListOf<LocationStateEvent>()
        val collectJob = launch(testDispatcher) {
            emitter.getSfUpdate().take(1).toList(collectedEvents)
        }

        emitter.updateLocationInfo(
            LocationStateData(
                location = null,
                isGpsEnabled = null,
                isNetworkEnabled = false,
                isPassiveEnabled = null,
                isFusedEnabled = null
            )
        )

        collectJob.join()

        assertEquals(1, collectedEvents.size)
        assertTrue(collectedEvents[0] is LocationStateEvent.OnNetworkEnabled)
        assertEquals(false, (collectedEvents[0] as LocationStateEvent.OnNetworkEnabled).isEnabled)
    }

    @Test
    fun updateLocationInfo_emitsOnPassiveEnabledEvent_whenPassiveStateChanges() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)

        val flowScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + Job())
        emitter.setupDataFlows(flowScope)

        val collectedEvents = mutableListOf<LocationStateEvent>()
        val collectJob = launch(testDispatcher) {
            emitter.getSfUpdate().take(1).toList(collectedEvents)
        }

        emitter.updateLocationInfo(
            LocationStateData(
                location = null,
                isGpsEnabled = null,
                isNetworkEnabled = null,
                isPassiveEnabled = true,
                isFusedEnabled = null
            )
        )

        collectJob.join()

        assertEquals(1, collectedEvents.size)
        assertTrue(collectedEvents[0] is LocationStateEvent.OnPassiveEnabled)
        assertEquals(true, (collectedEvents[0] as LocationStateEvent.OnPassiveEnabled).isEnabled)
    }

    @Test
    fun updateLocationInfo_emitsOnFusedEnabledEvent_whenFusedStateChanges() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)

        val flowScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + Job())
        emitter.setupDataFlows(flowScope)

        val collectedEvents = mutableListOf<LocationStateEvent>()
        val collectJob = launch(testDispatcher) {
            emitter.getSfUpdate().take(1).toList(collectedEvents)
        }

        emitter.updateLocationInfo(
            LocationStateData(
                location = null,
                isGpsEnabled = null,
                isNetworkEnabled = null,
                isPassiveEnabled = null,
                isFusedEnabled = true
            )
        )

        collectJob.join()

        assertEquals(1, collectedEvents.size)
        assertTrue(collectedEvents[0] is LocationStateEvent.OnFusedEnabled)
        assertEquals(true, (collectedEvents[0] as LocationStateEvent.OnFusedEnabled).isEnabled)
    }

    @Test
    fun updateLocationInfo_emitsOnLocationChangedEvent_whenLocationChanges() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)

        val flowScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + Job())
        emitter.setupDataFlows(flowScope)

        val mockLocation = mock(Location::class.java)

        val collectedEvents = mutableListOf<LocationStateEvent>()
        val collectJob = launch(testDispatcher) {
            emitter.getSfUpdate().take(1).toList(collectedEvents)
        }

        emitter.updateLocationInfo(
            LocationStateData(
                location = mockLocation,
                isGpsEnabled = null,
                isNetworkEnabled = null,
                isPassiveEnabled = null,
                isFusedEnabled = null
            )
        )

        collectJob.join()

        assertEquals(1, collectedEvents.size)
        assertTrue(collectedEvents[0] is LocationStateEvent.OnLocationChanged)
        assertEquals(mockLocation, (collectedEvents[0] as LocationStateEvent.OnLocationChanged).location)
    }

    @Test
    fun updateLocationInfo_emitsMultipleEvents_whenMultipleStatesChange() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)

        val flowScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + Job())
        emitter.setupDataFlows(flowScope)

        val collectedEvents = mutableListOf<LocationStateEvent>()
        val collectJob = launch(testDispatcher) {
            emitter.getSfUpdate().take(3).toList(collectedEvents)
        }

        emitter.updateLocationInfo(
            LocationStateData(
                location = null,
                isGpsEnabled = true,
                isNetworkEnabled = false,
                isPassiveEnabled = true,
                isFusedEnabled = null
            )
        )

        collectJob.join()

        assertEquals(3, collectedEvents.size)

        val eventTypes = collectedEvents.map { it::class }
        assertTrue(eventTypes.contains(LocationStateEvent.OnGpsEnabled::class))
        assertTrue(eventTypes.contains(LocationStateEvent.OnNetworkEnabled::class))
        assertTrue(eventTypes.contains(LocationStateEvent.OnPassiveEnabled::class))
    }

    // ==============================================
    // Null filtering Tests (filterNotNull)
    // ==============================================

    @Test
    fun updateLocationInfo_doesNotEmitGpsEvent_whenGpsStateIsNull() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)

        val flowScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + Job())
        emitter.setupDataFlows(flowScope)

        val collectedEvents = mutableListOf<LocationStateEvent>()
        val collectJob = launch(testDispatcher) {
            emitter.getSfUpdate().take(1).toList(collectedEvents)
        }

        // First update: GPS is null (should not emit GPS event)
        emitter.updateLocationInfo(
            LocationStateData(
                location = null,
                isGpsEnabled = null,
                isNetworkEnabled = true, // This should emit
                isPassiveEnabled = null,
                isFusedEnabled = null
            )
        )

        collectJob.join()

        assertEquals(1, collectedEvents.size)
        assertTrue(collectedEvents[0] is LocationStateEvent.OnNetworkEnabled)
    }

    // ==============================================
    // StateFlow distinctUntilChanged behavior Tests
    // ==============================================

    @Test
    fun updateLocationInfo_doesNotEmitDuplicateEvents_forSameValue() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)

        val flowScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + Job())
        emitter.setupDataFlows(flowScope)

        val collectedEvents = mutableListOf<LocationStateEvent>()
        val collectJob = launch(testDispatcher) {
            emitter.getSfUpdate().take(2).toList(collectedEvents)
        }

        // First update
        emitter.updateLocationInfo(
            LocationStateData(
                location = null,
                isGpsEnabled = true,
                isNetworkEnabled = null,
                isPassiveEnabled = null,
                isFusedEnabled = null
            )
        )

        // Same value again (should not emit due to StateFlow distinctUntilChanged)
        emitter.updateLocationInfo(
            LocationStateData(
                location = null,
                isGpsEnabled = true,
                isNetworkEnabled = null,
                isPassiveEnabled = null,
                isFusedEnabled = null
            )
        )

        // Different value (should emit)
        emitter.updateLocationInfo(
            LocationStateData(
                location = null,
                isGpsEnabled = false,
                isNetworkEnabled = null,
                isPassiveEnabled = null,
                isFusedEnabled = null
            )
        )

        collectJob.join()

        assertEquals(2, collectedEvents.size)
        assertEquals(true, (collectedEvents[0] as LocationStateEvent.OnGpsEnabled).isEnabled)
        assertEquals(false, (collectedEvents[1] as LocationStateEvent.OnGpsEnabled).isEnabled)
    }

    // ==============================================
    // setupDataFlows() re-initialization Tests
    // ==============================================

    @Test
    fun setupDataFlows_cancelsExistingCollectors_whenCalledAgain() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val flowScope = CoroutineScope(testDispatcher + Job())

        // First setup: emit GPS=true
        emitter.setupDataFlows(flowScope)

        val firstCollectedEvents = mutableListOf<LocationStateEvent>()
        val firstJob = launch(testDispatcher) {
            emitter.getSfUpdate().take(1).toList(firstCollectedEvents)
        }

        emitter.updateLocationInfo(
            LocationStateData(
                location = null,
                isGpsEnabled = true,
                isNetworkEnabled = null,
                isPassiveEnabled = null,
                isFusedEnabled = null
            )
        )

        firstJob.join()
        assertEquals(1, firstCollectedEvents.size)
        assertTrue(firstCollectedEvents[0] is LocationStateEvent.OnGpsEnabled)

        // Second setup (should reset collectors and re-collect from StateFlow)
        emitter.setupDataFlows(flowScope)

        val secondCollectedEvents = mutableListOf<LocationStateEvent>()
        val secondJob = launch(testDispatcher) {
            // take(2): first = replayed cached GPS=true from StateFlow, second = new Network=false
            emitter.getSfUpdate().take(2).toList(secondCollectedEvents)
        }

        emitter.updateLocationInfo(
            LocationStateData(
                location = null,
                isGpsEnabled = null,
                isNetworkEnabled = false,
                isPassiveEnabled = null,
                isFusedEnabled = null
            )
        )

        secondJob.join()
        assertEquals(2, secondCollectedEvents.size)
        // Second event should be the new Network=false event
        assertTrue(secondCollectedEvents[1] is LocationStateEvent.OnNetworkEnabled)
        assertEquals(false, (secondCollectedEvents[1] as LocationStateEvent.OnNetworkEnabled).isEnabled)

        flowScope.cancel()
    }

    // ==============================================
    // SharedFlow replay behavior Tests
    // ==============================================

    @Test
    fun getSfUpdate_replaysLastEvent_toNewSubscribers() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val childScope = CoroutineScope(testDispatcher + Job())

        emitter.setupDataFlows(childScope)

        // Emit event before subscription
        emitter.updateLocationInfo(
            LocationStateData(
                location = null,
                isGpsEnabled = true,
                isNetworkEnabled = null,
                isPassiveEnabled = null,
                isFusedEnabled = null
            )
        )

        // Small delay to ensure emission completes
        testScheduler.advanceUntilIdle()

        // New subscriber should receive replayed event
        val event = emitter.getSfUpdate().first()
        assertTrue(event is LocationStateEvent.OnGpsEnabled)
        assertEquals(true, (event as LocationStateEvent.OnGpsEnabled).isEnabled)

        childScope.cancel()
    }
}
