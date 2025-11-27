package kr.open.library.simple_ui.core.unit.system_manager.info.location

import android.location.Location
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

/**
 * Unit tests for LocationStateEvent sealed class
 */
class LocationStateEventUnitTest {

    // ==============================================
    // OnGpsEnabled Tests
    // ==============================================

    @Test
    fun onGpsEnabled_createsInstanceWithTrueValue() {
        val event = LocationStateEvent.OnGpsEnabled(isEnabled = true)

        assertTrue(event.isEnabled)
        assertTrue(event is LocationStateEvent.OnGpsEnabled)
        assertTrue(event is LocationStateEvent)
    }

    @Test
    fun onGpsEnabled_createsInstanceWithFalseValue() {
        val event = LocationStateEvent.OnGpsEnabled(isEnabled = false)

        assertFalse(event.isEnabled)
    }

    @Test
    fun onGpsEnabled_dataClassEquality() {
        val event1 = LocationStateEvent.OnGpsEnabled(isEnabled = true)
        val event2 = LocationStateEvent.OnGpsEnabled(isEnabled = true)
        val event3 = LocationStateEvent.OnGpsEnabled(isEnabled = false)

        assertEquals(event1, event2)
        assertNotEquals(event1, event3)
    }

    @Test
    fun onGpsEnabled_dataClassCopy() {
        val event = LocationStateEvent.OnGpsEnabled(isEnabled = true)
        val copied = event.copy(isEnabled = false)

        assertTrue(event.isEnabled)
        assertFalse(copied.isEnabled)
        assertNotEquals(event, copied)
    }

    // ==============================================
    // OnFusedEnabled Tests
    // ==============================================

    @Test
    fun onFusedEnabled_createsInstanceWithTrueValue() {
        val event = LocationStateEvent.OnFusedEnabled(isEnabled = true)

        assertTrue(event.isEnabled)
        assertTrue(event is LocationStateEvent.OnFusedEnabled)
        assertTrue(event is LocationStateEvent)
    }

    @Test
    fun onFusedEnabled_createsInstanceWithFalseValue() {
        val event = LocationStateEvent.OnFusedEnabled(isEnabled = false)

        assertFalse(event.isEnabled)
    }

    @Test
    fun onFusedEnabled_dataClassEquality() {
        val event1 = LocationStateEvent.OnFusedEnabled(isEnabled = true)
        val event2 = LocationStateEvent.OnFusedEnabled(isEnabled = true)
        val event3 = LocationStateEvent.OnFusedEnabled(isEnabled = false)

        assertEquals(event1, event2)
        assertNotEquals(event1, event3)
    }

    // ==============================================
    // OnNetworkEnabled Tests
    // ==============================================

    @Test
    fun onNetworkEnabled_createsInstanceWithTrueValue() {
        val event = LocationStateEvent.OnNetworkEnabled(isEnabled = true)

        assertTrue(event.isEnabled)
        assertTrue(event is LocationStateEvent.OnNetworkEnabled)
        assertTrue(event is LocationStateEvent)
    }

    @Test
    fun onNetworkEnabled_createsInstanceWithFalseValue() {
        val event = LocationStateEvent.OnNetworkEnabled(isEnabled = false)

        assertFalse(event.isEnabled)
    }

    @Test
    fun onNetworkEnabled_dataClassEquality() {
        val event1 = LocationStateEvent.OnNetworkEnabled(isEnabled = true)
        val event2 = LocationStateEvent.OnNetworkEnabled(isEnabled = true)
        val event3 = LocationStateEvent.OnNetworkEnabled(isEnabled = false)

        assertEquals(event1, event2)
        assertNotEquals(event1, event3)
    }

    // ==============================================
    // OnPassiveEnabled Tests
    // ==============================================

    @Test
    fun onPassiveEnabled_createsInstanceWithTrueValue() {
        val event = LocationStateEvent.OnPassiveEnabled(isEnabled = true)

        assertTrue(event.isEnabled)
        assertTrue(event is LocationStateEvent.OnPassiveEnabled)
        assertTrue(event is LocationStateEvent)
    }

    @Test
    fun onPassiveEnabled_createsInstanceWithFalseValue() {
        val event = LocationStateEvent.OnPassiveEnabled(isEnabled = false)

        assertFalse(event.isEnabled)
    }

    @Test
    fun onPassiveEnabled_dataClassEquality() {
        val event1 = LocationStateEvent.OnPassiveEnabled(isEnabled = true)
        val event2 = LocationStateEvent.OnPassiveEnabled(isEnabled = true)
        val event3 = LocationStateEvent.OnPassiveEnabled(isEnabled = false)

        assertEquals(event1, event2)
        assertNotEquals(event1, event3)
    }

    // ==============================================
    // OnLocationChanged Tests
    // ==============================================

    @Test
    fun onLocationChanged_createsInstanceWithNullLocation() {
        val event = LocationStateEvent.OnLocationChanged(location = null)

        assertNull(event.location)
        assertTrue(event is LocationStateEvent.OnLocationChanged)
        assertTrue(event is LocationStateEvent)
    }

    @Test
    fun onLocationChanged_createsInstanceWithMockLocation() {
        val mockLocation = mock(Location::class.java)
        val event = LocationStateEvent.OnLocationChanged(location = mockLocation)

        assertNotNull(event.location)
        assertEquals(mockLocation, event.location)
    }

    @Test
    fun onLocationChanged_dataClassEquality() {
        val mockLocation = mock(Location::class.java)
        val event1 = LocationStateEvent.OnLocationChanged(location = mockLocation)
        val event2 = LocationStateEvent.OnLocationChanged(location = mockLocation)
        val event3 = LocationStateEvent.OnLocationChanged(location = null)

        assertEquals(event1, event2)
        assertNotEquals(event1, event3)
    }

    // ==============================================
    // Sealed Class Behavior Tests
    // ==============================================

    @Test
    fun sealedClass_differentTypesAreNotEqual() {
        val gpsEvent = LocationStateEvent.OnGpsEnabled(isEnabled = true)
        val fusedEvent = LocationStateEvent.OnFusedEnabled(isEnabled = true)
        val networkEvent = LocationStateEvent.OnNetworkEnabled(isEnabled = true)
        val passiveEvent = LocationStateEvent.OnPassiveEnabled(isEnabled = true)
        val locationEvent = LocationStateEvent.OnLocationChanged(location = null)

        assertNotEquals(gpsEvent, fusedEvent)
        assertNotEquals(gpsEvent, networkEvent)
        assertNotEquals(gpsEvent, passiveEvent)
        assertNotEquals(gpsEvent, locationEvent)
        assertNotEquals(fusedEvent, networkEvent)
    }

    @Test
    fun sealedClass_allSubtypesAreInstanceOfParent() {
        val events: List<LocationStateEvent> = listOf(
            LocationStateEvent.OnGpsEnabled(isEnabled = true),
            LocationStateEvent.OnFusedEnabled(isEnabled = false),
            LocationStateEvent.OnNetworkEnabled(isEnabled = true),
            LocationStateEvent.OnPassiveEnabled(isEnabled = false),
            LocationStateEvent.OnLocationChanged(location = null)
        )

        events.forEach { event ->
            assertTrue(event is LocationStateEvent)
        }
    }

    @Test
    fun sealedClass_whenExpressionHandlesAllTypes() {
        val events: List<LocationStateEvent> = listOf(
            LocationStateEvent.OnGpsEnabled(isEnabled = true),
            LocationStateEvent.OnFusedEnabled(isEnabled = false),
            LocationStateEvent.OnNetworkEnabled(isEnabled = true),
            LocationStateEvent.OnPassiveEnabled(isEnabled = false),
            LocationStateEvent.OnLocationChanged(location = null)
        )

        events.forEach { event ->
            val result = when (event) {
                is LocationStateEvent.OnGpsEnabled -> "GPS"
                is LocationStateEvent.OnFusedEnabled -> "Fused"
                is LocationStateEvent.OnNetworkEnabled -> "Network"
                is LocationStateEvent.OnPassiveEnabled -> "Passive"
                is LocationStateEvent.OnLocationChanged -> "Location"
            }
            assertNotNull(result)
        }
    }
}
