package kr.open.library.simple_ui.core.robolectric.system_manager.info.location.internal.helper

import android.location.Location
import kr.open.library.simple_ui.core.system_manager.info.location.internal.helper.LocationCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric test for LocationCalculator.<br><br>
 * Verifies distance, bearing, and radius calculations using real Location objects.<br><br>
 * LocationCalculator에 대한 Robolectric 테스트입니다.<br>
 * 실제 Location 객체를 사용하여 거리, 방위각, 반경 계산을 검증합니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LocationCalculatorRobolectricTest {
    // ==============================================
    // calculateDistance() Tests
    // ==============================================

    @Test
    fun calculateDistance_returnsZero_whenSameLocation() {
        val location = Location("gps").apply {
            latitude = 37.5665
            longitude = 126.9780
        }
        val distance = LocationCalculator.calculateDistance(location, location)
        assertEquals(0f, distance, 0.001f)
    }

    @Test
    fun calculateDistance_returnsPositive_whenDifferentLocations() {
        val from = Location("gps").apply {
            latitude = 37.5665
            longitude = 126.9780
        }
        val to = Location("gps").apply {
            latitude = 35.1796
            longitude = 129.0756
        }
        val distance = LocationCalculator.calculateDistance(from, to)
        assertTrue(distance > 0f)
    }

    // ==============================================
    // calculateBearing() Tests
    // ==============================================

    @Test
    fun calculateBearing_returnsValue_betweenLocations() {
        val from = Location("gps").apply {
            latitude = 37.5665
            longitude = 126.9780
        }
        val to = Location("gps").apply {
            latitude = 35.1796
            longitude = 129.0756
        }
        val bearing = LocationCalculator.calculateBearing(from, to)
        assertTrue(bearing >= -180f && bearing <= 180f)
    }

    @Test
    fun calculateBearing_returnsZero_whenSameLocation() {
        val location = Location("gps").apply {
            latitude = 37.5665
            longitude = 126.9780
        }
        val bearing = LocationCalculator.calculateBearing(location, location)
        assertEquals(0f, bearing, 0.001f)
    }

    // ==============================================
    // isLocationWithRadius() Tests
    // ==============================================

    @Test
    fun isLocationWithRadius_returnsTrue_whenWithinRadius() {
        val from = Location("gps").apply {
            latitude = 37.5665
            longitude = 126.9780
        }
        val to = Location("gps").apply {
            latitude = 37.5666
            longitude = 126.9781
        }
        val distance = LocationCalculator.calculateDistance(from, to)
        val largeRadius = distance + 100f

        assertTrue(LocationCalculator.isLocationWithRadius(from, to, largeRadius))
    }

    @Test
    fun isLocationWithRadius_returnsFalse_whenOutsideRadius() {
        val from = Location("gps").apply {
            latitude = 37.5665
            longitude = 126.9780
        }
        val to = Location("gps").apply {
            latitude = 35.1796
            longitude = 129.0756
        }
        assertFalse(LocationCalculator.isLocationWithRadius(from, to, 1f))
    }

    @Test
    fun isLocationWithRadius_returnsTrue_whenExactlyOnBoundary() {
        val from = Location("gps").apply {
            latitude = 37.5665
            longitude = 126.9780
        }
        val to = Location("gps").apply {
            latitude = 37.5666
            longitude = 126.9781
        }
        val exactDistance = LocationCalculator.calculateDistance(from, to)

        assertTrue(LocationCalculator.isLocationWithRadius(from, to, exactDistance))
    }

    @Test
    fun isLocationWithRadius_returnsTrue_whenSameLocation() {
        val location = Location("gps").apply {
            latitude = 37.5665
            longitude = 126.9780
        }
        assertTrue(LocationCalculator.isLocationWithRadius(location, location, 0f))
    }
}
