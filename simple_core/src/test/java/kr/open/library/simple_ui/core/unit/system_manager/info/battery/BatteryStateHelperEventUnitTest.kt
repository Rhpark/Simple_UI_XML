package kr.open.library.simple_ui.core.unit.system_manager.info.battery

import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for BatteryStateEvent sealed class and its data classes
 */
class BatteryStateHelperEventUnitTest {
    // ==============================================
    // OnCapacity Tests
    // ==============================================

    @Test
    fun onCapacity_createsCorrectly() {
        val event = BatteryStateEvent.OnCapacity(75)

        assertEquals(75, event.percent)
        assertTrue(event is BatteryStateEvent)
    }

    @Test
    fun onCapacity_dataClassEquality() {
        val event1 = BatteryStateEvent.OnCapacity(50)
        val event2 = BatteryStateEvent.OnCapacity(50)
        val event3 = BatteryStateEvent.OnCapacity(75)

        assertEquals(event1, event2)
        assertNotEquals(event1, event3)
    }

    @Test
    fun onCapacity_copy() {
        val original = BatteryStateEvent.OnCapacity(80)
        val copied = original.copy(percent = 90)

        assertEquals(80, original.percent)
        assertEquals(90, copied.percent)
    }

    // ==============================================
    // OnChargeCounter Tests
    // ==============================================

    @Test
    fun onChargeCounter_createsCorrectly() {
        val event = BatteryStateEvent.OnChargeCounter(3000000)

        assertEquals(3000000, event.counter)
        assertTrue(event is BatteryStateEvent)
    }

    @Test
    fun onChargeCounter_dataClassEquality() {
        val event1 = BatteryStateEvent.OnChargeCounter(2500000)
        val event2 = BatteryStateEvent.OnChargeCounter(2500000)
        val event3 = BatteryStateEvent.OnChargeCounter(3000000)

        assertEquals(event1, event2)
        assertNotEquals(event1, event3)
    }

    // ==============================================
    // OnChargePlug Tests
    // ==============================================

    @Test
    fun onChargePlug_createsCorrectly() {
        val event = BatteryStateEvent.OnChargePlug(2) // AC

        assertEquals(2, event.type)
        assertTrue(event is BatteryStateEvent)
    }

    @Test
    fun onChargePlug_dataClassEquality() {
        val usbPlug = BatteryStateEvent.OnChargePlug(1)
        val acPlug = BatteryStateEvent.OnChargePlug(2)
        val usbPlug2 = BatteryStateEvent.OnChargePlug(1)

        assertEquals(usbPlug, usbPlug2)
        assertNotEquals(usbPlug, acPlug)
    }

    // ==============================================
    // OnTemperature Tests
    // ==============================================

    @Test
    fun onTemperature_createsCorrectly() {
        val event = BatteryStateEvent.OnTemperature(35.5)

        assertEquals(35.5, event.temperature, 0.001)
        assertTrue(event is BatteryStateEvent)
    }

    @Test
    fun onTemperature_dataClassEquality() {
        val event1 = BatteryStateEvent.OnTemperature(30.0)
        val event2 = BatteryStateEvent.OnTemperature(30.0)
        val event3 = BatteryStateEvent.OnTemperature(40.0)

        assertEquals(event1, event2)
        assertNotEquals(event1, event3)
    }

    @Test
    fun onTemperature_copy() {
        val original = BatteryStateEvent.OnTemperature(25.5)
        val copied = original.copy(temperature = 30.5)

        assertEquals(25.5, original.temperature, 0.001)
        assertEquals(30.5, copied.temperature, 0.001)
    }

    // ==============================================
    // OnVoltage Tests
    // ==============================================

    @Test
    fun onVoltage_createsCorrectly() {
        val event = BatteryStateEvent.OnVoltage(4.2)

        assertEquals(4.2, event.voltage, 0.001)
        assertTrue(event is BatteryStateEvent)
    }

    @Test
    fun onVoltage_dataClassEquality() {
        val event1 = BatteryStateEvent.OnVoltage(3.7)
        val event2 = BatteryStateEvent.OnVoltage(3.7)
        val event3 = BatteryStateEvent.OnVoltage(4.2)

        assertEquals(event1, event2)
        assertNotEquals(event1, event3)
    }

    // ==============================================
    // OnCurrentAmpere Tests
    // ==============================================

    @Test
    fun onCurrentAmpere_createsCorrectly() {
        val event = BatteryStateEvent.OnCurrentAmpere(1500000)

        assertEquals(1500000, event.current)
        assertTrue(event is BatteryStateEvent)
    }

    @Test
    fun onCurrentAmpere_negativeValue() {
        val event = BatteryStateEvent.OnCurrentAmpere(-800000) // Discharging

        assertEquals(-800000, event.current)
    }

    @Test
    fun onCurrentAmpere_dataClassEquality() {
        val event1 = BatteryStateEvent.OnCurrentAmpere(1000000)
        val event2 = BatteryStateEvent.OnCurrentAmpere(1000000)
        val event3 = BatteryStateEvent.OnCurrentAmpere(-500000)

        assertEquals(event1, event2)
        assertNotEquals(event1, event3)
    }

    // ==============================================
    // OnCurrentAverageAmpere Tests
    // ==============================================

    @Test
    fun onCurrentAverageAmpere_createsCorrectly() {
        val event = BatteryStateEvent.OnCurrentAverageAmpere(1200000)

        assertEquals(1200000, event.current)
        assertTrue(event is BatteryStateEvent)
    }

    @Test
    fun onCurrentAverageAmpere_negativeValue() {
        val event = BatteryStateEvent.OnCurrentAverageAmpere(-600000) // Discharging

        assertEquals(-600000, event.current)
    }

    @Test
    fun onCurrentAverageAmpere_dataClassEquality() {
        val event1 = BatteryStateEvent.OnCurrentAverageAmpere(900000)
        val event2 = BatteryStateEvent.OnCurrentAverageAmpere(900000)
        val event3 = BatteryStateEvent.OnCurrentAverageAmpere(1100000)

        assertEquals(event1, event2)
        assertNotEquals(event1, event3)
    }

    // ==============================================
    // OnChargeStatus Tests
    // ==============================================

    @Test
    fun onChargeStatus_createsCorrectly() {
        val event = BatteryStateEvent.OnChargeStatus(2) // CHARGING

        assertEquals(2, event.status)
        assertTrue(event is BatteryStateEvent)
    }

    @Test
    fun onChargeStatus_dataClassEquality() {
        val charging = BatteryStateEvent.OnChargeStatus(2)
        val discharging = BatteryStateEvent.OnChargeStatus(3)
        val charging2 = BatteryStateEvent.OnChargeStatus(2)

        assertEquals(charging, charging2)
        assertNotEquals(charging, discharging)
    }

    // ==============================================
    // OnHealth Tests
    // ==============================================

    @Test
    fun onHealth_createsCorrectly() {
        val event = BatteryStateEvent.OnHealth(2) // GOOD

        assertEquals(2, event.health)
        assertTrue(event is BatteryStateEvent)
    }

    @Test
    fun onHealth_dataClassEquality() {
        val good = BatteryStateEvent.OnHealth(2)
        val overheat = BatteryStateEvent.OnHealth(3)
        val good2 = BatteryStateEvent.OnHealth(2)

        assertEquals(good, good2)
        assertNotEquals(good, overheat)
    }

    // ==============================================
    // OnEnergyCounter Tests
    // ==============================================

    @Test
    fun onEnergyCounter_createsCorrectly() {
        val event = BatteryStateEvent.OnEnergyCounter(15000000000L)

        assertEquals(15000000000L, event.energy)
        assertTrue(event is BatteryStateEvent)
    }

    @Test
    fun onEnergyCounter_dataClassEquality() {
        val event1 = BatteryStateEvent.OnEnergyCounter(10000000000L)
        val event2 = BatteryStateEvent.OnEnergyCounter(10000000000L)
        val event3 = BatteryStateEvent.OnEnergyCounter(20000000000L)

        assertEquals(event1, event2)
        assertNotEquals(event1, event3)
    }

    // ==============================================
    // OnPresent Tests
    // ==============================================

    @Test
    fun onPresent_createsCorrectlyTrue() {
        val event = BatteryStateEvent.OnPresent(true)

        assertTrue(event.present)
        assertTrue(event is BatteryStateEvent)
    }

    @Test
    fun onPresent_createsCorrectlyFalse() {
        val event = BatteryStateEvent.OnPresent(false)

        assertFalse(event.present)
        assertTrue(event is BatteryStateEvent)
    }

    @Test
    fun onPresent_dataClassEquality() {
        val present1 = BatteryStateEvent.OnPresent(true)
        val present2 = BatteryStateEvent.OnPresent(true)
        val notPresent = BatteryStateEvent.OnPresent(false)

        assertEquals(present1, present2)
        assertNotEquals(present1, notPresent)
    }

    @Test
    fun onPresent_copy() {
        val original = BatteryStateEvent.OnPresent(true)
        val copied = original.copy(present = false)

        assertTrue(original.present)
        assertFalse(copied.present)
    }

    // ==============================================
    // Sealed Class Hierarchy Tests
    // ==============================================

    @Test
    fun allEvents_areInstancesOfBatteryStateEvent() {
        val events: List<BatteryStateEvent> =
            listOf(
                BatteryStateEvent.OnCapacity(50),
                BatteryStateEvent.OnChargeCounter(2000000),
                BatteryStateEvent.OnChargePlug(1),
                BatteryStateEvent.OnTemperature(30.0),
                BatteryStateEvent.OnVoltage(3.8),
                BatteryStateEvent.OnCurrentAmpere(1000000),
                BatteryStateEvent.OnCurrentAverageAmpere(950000),
                BatteryStateEvent.OnChargeStatus(2),
                BatteryStateEvent.OnHealth(2),
                BatteryStateEvent.OnEnergyCounter(12000000000L),
                BatteryStateEvent.OnPresent(true),
            )

        events.forEach { event ->
            assertTrue(event is BatteryStateEvent)
        }
    }

    @Test
    fun whenExpression_coversAllEventTypes() {
        val events: List<BatteryStateEvent> =
            listOf(
                BatteryStateEvent.OnCapacity(75),
                BatteryStateEvent.OnChargeCounter(3000000),
                BatteryStateEvent.OnChargePlug(2),
                BatteryStateEvent.OnTemperature(35.0),
                BatteryStateEvent.OnVoltage(4.1),
                BatteryStateEvent.OnCurrentAmpere(1500000),
                BatteryStateEvent.OnCurrentAverageAmpere(1400000),
                BatteryStateEvent.OnChargeStatus(2),
                BatteryStateEvent.OnHealth(2),
                BatteryStateEvent.OnEnergyCounter(15000000000L),
                BatteryStateEvent.OnPresent(true),
            )

        events.forEach { event ->
            val eventType =
                when (event) {
                    is BatteryStateEvent.OnCapacity -> "Capacity"
                    is BatteryStateEvent.OnChargeCounter -> "ChargeCounter"
                    is BatteryStateEvent.OnChargePlug -> "ChargePlug"
                    is BatteryStateEvent.OnTemperature -> "Temperature"
                    is BatteryStateEvent.OnVoltage -> "Voltage"
                    is BatteryStateEvent.OnCurrentAmpere -> "CurrentAmpere"
                    is BatteryStateEvent.OnCurrentAverageAmpere -> "CurrentAverageAmpere"
                    is BatteryStateEvent.OnChargeStatus -> "ChargeStatus"
                    is BatteryStateEvent.OnHealth -> "Health"
                    is BatteryStateEvent.OnEnergyCounter -> "EnergyCounter"
                    is BatteryStateEvent.OnPresent -> "Present"
                }
            assertTrue(eventType.isNotEmpty())
        }
    }

    // ==============================================
    // Data Class Component Tests
    // ==============================================

    @Test
    fun onCapacity_componentFunctions() {
        val event = BatteryStateEvent.OnCapacity(80)
        val (percent) = event

        assertEquals(80, percent)
    }

    @Test
    fun onTemperature_componentFunctions() {
        val event = BatteryStateEvent.OnTemperature(32.5)
        val (temperature) = event

        assertEquals(32.5, temperature, 0.001)
    }

    @Test
    fun onPresent_componentFunctions() {
        val event = BatteryStateEvent.OnPresent(true)
        val (present) = event

        assertTrue(present)
    }

    // ==============================================
    // HashCode and ToString Tests
    // ==============================================

    @Test
    fun dataClasses_haveConsistentHashCode() {
        val event1 = BatteryStateEvent.OnCapacity(60)
        val event2 = BatteryStateEvent.OnCapacity(60)

        assertEquals(event1.hashCode(), event2.hashCode())
    }

    @Test
    fun dataClasses_haveDescriptiveToString() {
        val event = BatteryStateEvent.OnCapacity(85)
        val toString = event.toString()

        assertTrue(toString.contains("OnCapacity"))
        assertTrue(toString.contains("85"))
    }

    @Test
    fun onVoltage_toStringContainsValue() {
        val event = BatteryStateEvent.OnVoltage(4.15)
        val toString = event.toString()

        assertTrue(toString.contains("OnVoltage"))
        assertTrue(toString.contains("4.15"))
    }
}
