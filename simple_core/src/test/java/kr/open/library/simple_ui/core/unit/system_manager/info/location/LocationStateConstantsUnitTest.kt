package kr.open.library.simple_ui.core.unit.system_manager.info.location

import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for LocationStateConstants.<br><br>
 * Verifies that constant values are correct and sentinel values behave as expected.<br><br>
 * LocationStateConstants에 대한 단위 테스트입니다.<br>
 * 상수 값이 올바르고 센티널 값이 예상대로 동작하는지 확인합니다.<br>
 */
class LocationStateConstantsUnitTest {
    // ==============================================
    // Time constants
    // ==============================================

    @Test
    fun defaultUpdateCycleTime_isExpectedValue() {
        assertEquals(5000L, LocationStateConstants.DEFAULT_UPDATE_CYCLE_TIME)
    }

    @Test
    fun minUpdateCycleTime_isExpectedValue() {
        assertEquals(1000L, LocationStateConstants.MIN_UPDATE_CYCLE_TIME)
    }

    @Test
    fun pollingDisabledUpdateCycleTime_isNegativeOne() {
        assertEquals(-1L, LocationStateConstants.POLLING_DISABLED_UPDATE_CYCLE_TIME)
    }

    @Suppress("DEPRECATION")
    @Test
    fun disableUpdateCycleTime_equalsPollingDisabledUpdateCycleTime() {
        assertEquals(
            LocationStateConstants.POLLING_DISABLED_UPDATE_CYCLE_TIME,
            LocationStateConstants.DISABLE_UPDATE_CYCLE_TIME
        )
    }

    @Test
    fun significantTimeDeltaMs_isExpectedValue() {
        assertEquals(10_000L, LocationStateConstants.SIGNIFICANT_TIME_DELTA_MS)
    }

    // ==============================================
    // Distance constants
    // ==============================================

    @Test
    fun defaultUpdateCycleDistance_isExpectedValue() {
        assertEquals(2.0f, LocationStateConstants.DEFAULT_UPDATE_CYCLE_DISTANCE, 0.001f)
    }

    @Test
    fun minUpdateCycleDistance_isExpectedValue() {
        assertEquals(0.1f, LocationStateConstants.MIN_UPDATE_CYCLE_DISTANCE, 0.001f)
    }

    // ==============================================
    // Accuracy constants
    // ==============================================

    @Test
    fun significantAccuracyDeltaMeters_isExpectedValue() {
        assertEquals(200, LocationStateConstants.SIGNIFICANT_ACCURACY_DELTA_METERS)
    }

    // ==============================================
    // Sentinel (error) values
    // ==============================================

    @Test
    fun locationErrorValueBoolean_isNull() {
        assertNull(LocationStateConstants.LOCATION_ERROR_VALUE_BOOLEAN)
    }

    @Test
    fun locationErrorValueLocation_isNull() {
        assertNull(LocationStateConstants.LOCATION_ERROR_VALUE_LOCATION)
    }

    // ==============================================
    // Relationship constraints
    // ==============================================

    @Test
    fun minUpdateCycleTime_isLessThanOrEqualToDefault() {
        assertTrue(LocationStateConstants.MIN_UPDATE_CYCLE_TIME <= LocationStateConstants.DEFAULT_UPDATE_CYCLE_TIME)
    }

    @Test
    fun pollingDisabledUpdateCycleTime_isLessThanMinUpdateCycleTime() {
        assertTrue(LocationStateConstants.POLLING_DISABLED_UPDATE_CYCLE_TIME < LocationStateConstants.MIN_UPDATE_CYCLE_TIME)
    }
}
