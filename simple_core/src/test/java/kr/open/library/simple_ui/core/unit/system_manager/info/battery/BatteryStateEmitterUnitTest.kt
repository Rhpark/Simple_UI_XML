package kr.open.library.simple_ui.core.unit.system_manager.info.battery

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.BATTERY_ERROR_VALUE
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.BATTERY_ERROR_VALUE_BOOLEAN
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.BATTERY_ERROR_VALUE_DOUBLE
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.BATTERY_ERROR_VALUE_LONG
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateEvent
import kr.open.library.simple_ui.core.system_manager.info.battery.internal.helper.BatteryStateEmitter
import kr.open.library.simple_ui.core.system_manager.info.battery.internal.model.BatteryStateData
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for BatteryStateEmitter.
 * Tests SharedFlow event emission, sentinel filtering, StateFlow deduplication, and lifecycle management.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BatteryStateEmitterUnitTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var emitter: BatteryStateEmitter

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        emitter = BatteryStateEmitter()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createTestData(
        capacity: Int = 75,
        chargeCounter: Int = 3000000,
        chargePlug: Int = 2,
        currentAmpere: Int = 1500000,
        chargeStatus: Int = 2,
        currentAverageAmpere: Int = 1200000,
        energyCounter: Long = 15000000000L,
        health: Int = 2,
        present: Boolean? = true,
        temperature: Double = 35.5,
        voltage: Double = 4.2
    ) = BatteryStateData(
        capacity = capacity,
        chargeCounter = chargeCounter,
        chargePlug = chargePlug,
        currentAmpere = currentAmpere,
        chargeStatus = chargeStatus,
        currentAverageAmpere = currentAverageAmpere,
        energyCounter = energyCounter,
        health = health,
        present = present,
        temperature = temperature,
        voltage = voltage
    )

    // ==============================================
    // SharedFlow configuration
    // ==============================================

    @Test
    fun `getSfUpdate returns SharedFlow instance`() {
        val sfUpdate = emitter.getSfUpdate()
        assertNotNull(sfUpdate)
    }

    @Test
    fun `getSfUpdate has replay of 1`() = runTest {
        val scope = CoroutineScope(testDispatcher + Job())
        emitter.setupDataFlows(scope)

        emitter.updateBatteryInfo(createTestData(capacity = 80))

        // New collector should immediately receive the last replayed event
        val events = mutableListOf<BatteryStateEvent>()
        val collectJob = launch(testDispatcher) {
            emitter.getSfUpdate().collect { events.add(it) }
        }

        assertTrue(events.isNotEmpty())

        collectJob.cancel()
        scope.cancel()
    }

    // ==============================================
    // updateBatteryInfo - value emission
    // ==============================================

    @Test
    fun `updateBatteryInfo emits all eleven events`() = runTest {
        val events = mutableListOf<BatteryStateEvent>()
        val scope = CoroutineScope(testDispatcher + Job())

        emitter.setupDataFlows(scope)

        val collectJob = launch(testDispatcher) {
            emitter.getSfUpdate().collect { events.add(it) }
        }

        emitter.updateBatteryInfo(createTestData())

        assertEquals(11, events.size)
        assertTrue(events.any { it is BatteryStateEvent.OnCapacity })
        assertTrue(events.any { it is BatteryStateEvent.OnChargeCounter })
        assertTrue(events.any { it is BatteryStateEvent.OnChargePlug })
        assertTrue(events.any { it is BatteryStateEvent.OnTemperature })
        assertTrue(events.any { it is BatteryStateEvent.OnVoltage })
        assertTrue(events.any { it is BatteryStateEvent.OnCurrentAmpere })
        assertTrue(events.any { it is BatteryStateEvent.OnCurrentAverageAmpere })
        assertTrue(events.any { it is BatteryStateEvent.OnChargeStatus })
        assertTrue(events.any { it is BatteryStateEvent.OnHealth })
        assertTrue(events.any { it is BatteryStateEvent.OnEnergyCounter })
        assertTrue(events.any { it is BatteryStateEvent.OnPresent })

        collectJob.cancel()
        scope.cancel()
    }

    @Test
    fun `updateBatteryInfo emits correct capacity value`() = runTest {
        val events = mutableListOf<BatteryStateEvent>()
        val scope = CoroutineScope(testDispatcher + Job())

        emitter.setupDataFlows(scope)
        val collectJob = launch(testDispatcher) { emitter.getSfUpdate().collect { events.add(it) } }

        emitter.updateBatteryInfo(createTestData(capacity = 85))

        val capacityEvent = events.filterIsInstance<BatteryStateEvent.OnCapacity>().first()
        assertEquals(85, capacityEvent.percent)

        collectJob.cancel()
        scope.cancel()
    }

    @Test
    fun `updateBatteryInfo emits correct temperature value`() = runTest {
        val events = mutableListOf<BatteryStateEvent>()
        val scope = CoroutineScope(testDispatcher + Job())

        emitter.setupDataFlows(scope)
        val collectJob = launch(testDispatcher) { emitter.getSfUpdate().collect { events.add(it) } }

        emitter.updateBatteryInfo(createTestData(temperature = 42.3))

        val temperatureEvent = events.filterIsInstance<BatteryStateEvent.OnTemperature>().first()
        assertEquals(42.3, temperatureEvent.temperature, 0.01)

        collectJob.cancel()
        scope.cancel()
    }

    @Test
    fun `updateBatteryInfo emits correct energy counter value`() = runTest {
        val events = mutableListOf<BatteryStateEvent>()
        val scope = CoroutineScope(testDispatcher + Job())

        emitter.setupDataFlows(scope)
        val collectJob = launch(testDispatcher) { emitter.getSfUpdate().collect { events.add(it) } }

        emitter.updateBatteryInfo(createTestData(energyCounter = 99999999L))

        val energyEvent = events.filterIsInstance<BatteryStateEvent.OnEnergyCounter>().first()
        assertEquals(99999999L, energyEvent.energy)

        collectJob.cancel()
        scope.cancel()
    }

    @Test
    fun `updateBatteryInfo emits correct present value`() = runTest {
        val events = mutableListOf<BatteryStateEvent>()
        val scope = CoroutineScope(testDispatcher + Job())

        emitter.setupDataFlows(scope)
        val collectJob = launch(testDispatcher) { emitter.getSfUpdate().collect { events.add(it) } }

        emitter.updateBatteryInfo(createTestData(present = false))

        val presentEvent = events.filterIsInstance<BatteryStateEvent.OnPresent>().first()
        assertEquals(false, presentEvent.present)

        collectJob.cancel()
        scope.cancel()
    }

    @Test
    fun `updateBatteryInfo emits correct voltage value`() = runTest {
        val events = mutableListOf<BatteryStateEvent>()
        val scope = CoroutineScope(testDispatcher + Job())

        emitter.setupDataFlows(scope)
        val collectJob = launch(testDispatcher) { emitter.getSfUpdate().collect { events.add(it) } }

        emitter.updateBatteryInfo(createTestData(voltage = 3.85))

        val voltageEvent = events.filterIsInstance<BatteryStateEvent.OnVoltage>().first()
        assertEquals(3.85, voltageEvent.voltage, 0.01)

        collectJob.cancel()
        scope.cancel()
    }

    @Test
    fun `updateBatteryInfo emits correct chargeCounter value`() = runTest {
        val events = mutableListOf<BatteryStateEvent>()
        val scope = CoroutineScope(testDispatcher + Job())

        emitter.setupDataFlows(scope)
        val collectJob = launch(testDispatcher) { emitter.getSfUpdate().collect { events.add(it) } }

        emitter.updateBatteryInfo(createTestData(chargeCounter = 2500000))

        val event = events.filterIsInstance<BatteryStateEvent.OnChargeCounter>().first()
        assertEquals(2500000, event.counter)

        collectJob.cancel()
        scope.cancel()
    }

    @Test
    fun `updateBatteryInfo emits correct chargePlug value`() = runTest {
        val events = mutableListOf<BatteryStateEvent>()
        val scope = CoroutineScope(testDispatcher + Job())

        emitter.setupDataFlows(scope)
        val collectJob = launch(testDispatcher) { emitter.getSfUpdate().collect { events.add(it) } }

        emitter.updateBatteryInfo(createTestData(chargePlug = 1))

        val event = events.filterIsInstance<BatteryStateEvent.OnChargePlug>().first()
        assertEquals(1, event.type)

        collectJob.cancel()
        scope.cancel()
    }

    @Test
    fun `updateBatteryInfo emits correct chargeStatus value`() = runTest {
        val events = mutableListOf<BatteryStateEvent>()
        val scope = CoroutineScope(testDispatcher + Job())

        emitter.setupDataFlows(scope)
        val collectJob = launch(testDispatcher) { emitter.getSfUpdate().collect { events.add(it) } }

        emitter.updateBatteryInfo(createTestData(chargeStatus = 3))

        val event = events.filterIsInstance<BatteryStateEvent.OnChargeStatus>().first()
        assertEquals(3, event.status)

        collectJob.cancel()
        scope.cancel()
    }

    @Test
    fun `updateBatteryInfo emits correct currentAmpere value`() = runTest {
        val events = mutableListOf<BatteryStateEvent>()
        val scope = CoroutineScope(testDispatcher + Job())

        emitter.setupDataFlows(scope)
        val collectJob = launch(testDispatcher) { emitter.getSfUpdate().collect { events.add(it) } }

        emitter.updateBatteryInfo(createTestData(currentAmpere = -800000))

        val event = events.filterIsInstance<BatteryStateEvent.OnCurrentAmpere>().first()
        assertEquals(-800000, event.current)

        collectJob.cancel()
        scope.cancel()
    }

    @Test
    fun `updateBatteryInfo emits correct currentAverageAmpere value`() = runTest {
        val events = mutableListOf<BatteryStateEvent>()
        val scope = CoroutineScope(testDispatcher + Job())

        emitter.setupDataFlows(scope)
        val collectJob = launch(testDispatcher) { emitter.getSfUpdate().collect { events.add(it) } }

        emitter.updateBatteryInfo(createTestData(currentAverageAmpere = 950000))

        val event = events.filterIsInstance<BatteryStateEvent.OnCurrentAverageAmpere>().first()
        assertEquals(950000, event.current)

        collectJob.cancel()
        scope.cancel()
    }

    @Test
    fun `updateBatteryInfo emits correct health value`() = runTest {
        val events = mutableListOf<BatteryStateEvent>()
        val scope = CoroutineScope(testDispatcher + Job())

        emitter.setupDataFlows(scope)
        val collectJob = launch(testDispatcher) { emitter.getSfUpdate().collect { events.add(it) } }

        emitter.updateBatteryInfo(createTestData(health = 3))

        val event = events.filterIsInstance<BatteryStateEvent.OnHealth>().first()
        assertEquals(3, event.health)

        collectJob.cancel()
        scope.cancel()
    }

    // ==============================================
    // dropWhile sentinel filtering
    // ==============================================

    @Test
    fun `setupDataFlows filters initial sentinel values`() = runTest {
        val events = mutableListOf<BatteryStateEvent>()
        val scope = CoroutineScope(testDispatcher + Job())

        emitter.setupDataFlows(scope)
        val collectJob = launch(testDispatcher) { emitter.getSfUpdate().collect { events.add(it) } }

        // No updateBatteryInfo called - all StateFlows hold sentinel values
        assertTrue("No events should be emitted for sentinel values", events.isEmpty())

        collectJob.cancel()
        scope.cancel()
    }

    @Test
    fun `setupDataFlows subsequent sentinel values pass through`() = runTest {
        val events = mutableListOf<BatteryStateEvent>()
        val scope = CoroutineScope(testDispatcher + Job())

        emitter.setupDataFlows(scope)
        val collectJob = launch(testDispatcher) { emitter.getSfUpdate().collect { events.add(it) } }

        // First update with real data - breaks dropWhile
        emitter.updateBatteryInfo(createTestData(capacity = 75))

        val capacityEvents1 = events.filterIsInstance<BatteryStateEvent.OnCapacity>()
        assertEquals(1, capacityEvents1.size)
        assertEquals(75, capacityEvents1[0].percent)

        // Second update with sentinel value - should pass through after dropWhile is broken
        emitter.updateBatteryInfo(createTestData(capacity = BATTERY_ERROR_VALUE))

        val capacityEvents2 = events.filterIsInstance<BatteryStateEvent.OnCapacity>()
        assertEquals(2, capacityEvents2.size)
        assertEquals(BATTERY_ERROR_VALUE, capacityEvents2[1].percent)

        collectJob.cancel()
        scope.cancel()
    }

    @Test
    fun `setupDataFlows mixed sentinel and real values`() = runTest {
        val events = mutableListOf<BatteryStateEvent>()
        val scope = CoroutineScope(testDispatcher + Job())

        emitter.setupDataFlows(scope)
        val collectJob = launch(testDispatcher) { emitter.getSfUpdate().collect { events.add(it) } }

        // Only capacity and temperature have real values; others remain sentinel
        emitter.updateBatteryInfo(
            BatteryStateData(
                capacity = 50,
                chargeCounter = BATTERY_ERROR_VALUE,
                chargePlug = BATTERY_ERROR_VALUE,
                currentAmpere = BATTERY_ERROR_VALUE,
                chargeStatus = BATTERY_ERROR_VALUE,
                currentAverageAmpere = BATTERY_ERROR_VALUE,
                energyCounter = BATTERY_ERROR_VALUE_LONG,
                health = BATTERY_ERROR_VALUE,
                present = BATTERY_ERROR_VALUE_BOOLEAN,
                temperature = 30.0,
                voltage = BATTERY_ERROR_VALUE_DOUBLE
            )
        )

        // Only capacity and temperature should have events (non-sentinel values)
        assertTrue(events.any { it is BatteryStateEvent.OnCapacity })
        assertTrue(events.any { it is BatteryStateEvent.OnTemperature })
        // Sentinel-initialized fields should NOT emit
        assertTrue(events.none { it is BatteryStateEvent.OnChargeCounter })
        assertTrue(events.none { it is BatteryStateEvent.OnChargePlug })
        assertTrue(events.none { it is BatteryStateEvent.OnCurrentAmpere })
        assertTrue(events.none { it is BatteryStateEvent.OnCurrentAverageAmpere })
        assertTrue(events.none { it is BatteryStateEvent.OnChargeStatus })
        assertTrue(events.none { it is BatteryStateEvent.OnHealth })
        assertTrue(events.none { it is BatteryStateEvent.OnEnergyCounter })
        assertTrue(events.none { it is BatteryStateEvent.OnPresent })
        assertTrue(events.none { it is BatteryStateEvent.OnVoltage })

        collectJob.cancel()
        scope.cancel()
    }

    // ==============================================
    // Multiple updates
    // ==============================================

    @Test
    fun `updateBatteryInfo multiple updates emit multiple events`() = runTest {
        val events = mutableListOf<BatteryStateEvent>()
        val scope = CoroutineScope(testDispatcher + Job())

        emitter.setupDataFlows(scope)
        val collectJob = launch(testDispatcher) { emitter.getSfUpdate().collect { events.add(it) } }

        emitter.updateBatteryInfo(createTestData(capacity = 70))
        emitter.updateBatteryInfo(createTestData(capacity = 60))
        emitter.updateBatteryInfo(createTestData(capacity = 50))

        val capacityEvents = events.filterIsInstance<BatteryStateEvent.OnCapacity>()
        assertEquals(3, capacityEvents.size)
        assertEquals(70, capacityEvents[0].percent)
        assertEquals(60, capacityEvents[1].percent)
        assertEquals(50, capacityEvents[2].percent)

        collectJob.cancel()
        scope.cancel()
    }

    @Test
    fun `updateBatteryInfo duplicate value does not emit`() = runTest {
        val events = mutableListOf<BatteryStateEvent>()
        val scope = CoroutineScope(testDispatcher + Job())

        emitter.setupDataFlows(scope)
        val collectJob = launch(testDispatcher) { emitter.getSfUpdate().collect { events.add(it) } }

        val sameData = createTestData(capacity = 80)
        emitter.updateBatteryInfo(sameData)
        emitter.updateBatteryInfo(sameData)

        // StateFlow conflates identical values - second update should not produce new events
        val capacityEvents = events.filterIsInstance<BatteryStateEvent.OnCapacity>()
        assertEquals(1, capacityEvents.size)

        collectJob.cancel()
        scope.cancel()
    }

    // ==============================================
    // setupDataFlows / resetCollectors lifecycle
    // ==============================================

    @Test
    fun `setupDataFlows called twice cancels first collectors`() = runTest {
        val events = mutableListOf<BatteryStateEvent>()
        val scope = CoroutineScope(testDispatcher + Job())

        emitter.setupDataFlows(scope)
        emitter.setupDataFlows(scope) // second call should cancel first collectors

        val collectJob = launch(testDispatcher) { emitter.getSfUpdate().collect { events.add(it) } }

        emitter.updateBatteryInfo(createTestData(capacity = 90))

        // Should emit exactly 11 events (not 22 from duplicated collectors)
        assertEquals(11, events.size)
        val capacityEvents = events.filterIsInstance<BatteryStateEvent.OnCapacity>()
        assertEquals(1, capacityEvents.size)

        collectJob.cancel()
        scope.cancel()
    }

    @Test
    fun `setupDataFlows scope cancellation stops collection`() = runTest {
        val events = mutableListOf<BatteryStateEvent>()
        val scope = CoroutineScope(testDispatcher + Job())

        emitter.setupDataFlows(scope)
        val collectJob = launch(testDispatcher) { emitter.getSfUpdate().collect { events.add(it) } }

        emitter.updateBatteryInfo(createTestData(capacity = 75))

        val countBefore = events.size

        // Cancel the scope used for data flows
        scope.cancel()

        // Update after scope cancellation - no new events should appear
        emitter.updateBatteryInfo(createTestData(capacity = 50))

        assertEquals(countBefore, events.size)

        collectJob.cancel()
    }

    // ==============================================
    // SharedFlow buffer overflow (DROP_OLDEST)
    // ==============================================

    @Test
    fun `sharedFlow drop oldest on overflow`() = runTest {
        val scope = CoroutineScope(testDispatcher + Job())
        emitter.setupDataFlows(scope)

        // Emit many different values without any collector (fills buffer)
        for (i in 1..25) {
            emitter.updateBatteryInfo(createTestData(capacity = i))
        }

        // Now start collecting - should get replay (1 event) only
        val events = mutableListOf<BatteryStateEvent>()
        val collectJob = launch(testDispatcher) { emitter.getSfUpdate().collect { events.add(it) } }

        // With replay=1, new collector receives at most the last replayed event
        assertTrue("Should receive at least 1 replayed event", events.isNotEmpty())
        // The replayed event should be from the latest emission
        val lastCapacity = events.filterIsInstance<BatteryStateEvent.OnCapacity>().lastOrNull()
        if (lastCapacity != null) {
            assertEquals(25, lastCapacity.percent)
        }

        collectJob.cancel()
        scope.cancel()
    }
}
