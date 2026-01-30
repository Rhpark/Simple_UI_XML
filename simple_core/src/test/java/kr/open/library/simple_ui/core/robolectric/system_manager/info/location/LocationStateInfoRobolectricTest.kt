package kr.open.library.simple_ui.core.robolectric.system_manager.info.location

import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.reflect.Method

/**
 * Robolectric test for LocationStateInfo.
 * Specifically validates the 'isBetterLocation' logic including the 10-second freshness rule.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LocationStateInfoRobolectricTest {
    private lateinit var context: Context
    private lateinit var locationStateInfo: LocationStateInfo
    private lateinit var isBetterLocationMethod: Method

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        locationStateInfo = LocationStateInfo(context)

        // Access private method isBetterLocation using reflection
        val method = LocationStateInfo::class.java.getDeclaredMethod(
            "isBetterLocation",
            Location::class.java,
            Location::class.java
        )
        method.isAccessible = true
        isBetterLocationMethod = method
    }

    private fun invokeIsBetterLocation(newLocation: Location, currentBest: Location?): Boolean = isBetterLocationMethod
        .invoke(locationStateInfo, newLocation, currentBest) as Boolean

    @Test
    fun isBetterLocation_returnsTrue_whenCurrentBestIsNull() {
        val newLocation = Location("gps")
        assertTrue(invokeIsBetterLocation(newLocation, null))
    }

    @Test
    fun isBetterLocation_returnsTrue_whenSignificantlyNewer() {
        // 10초(10000ms) 초과 차이
        val current = Location("gps").apply { time = 10000 }
        val newer = Location("gps").apply { time = 20001 } // +10001ms

        assertTrue(invokeIsBetterLocation(newer, current))
    }

    @Test
    fun isBetterLocation_returnsFalse_whenSignificantlyOlder() {
        // 10초(10000ms) 초과 차이 (과거)
        val current = Location("gps").apply { time = 20000 }
        val older = Location("gps").apply { time = 9999 } // -10001ms

        assertFalse(invokeIsBetterLocation(older, current))
    }

    @Test
    fun isBetterLocation_returnsTrue_whenMoreAccurate() {
        val current = Location("gps").apply {
            time = 10000
            accuracy = 50f
        }
        val accurate = Location("gps").apply {
            time = 10000
            accuracy = 30f // Delta < 0
        }

        assertTrue(invokeIsBetterLocation(accurate, current))
    }

    @Test
    fun isBetterLocation_returnsTrue_whenNewerAndNotLessAccurate() {
        val current = Location("gps").apply {
            time = 10000
            accuracy = 50f
        }
        val newer = Location("gps").apply {
            time = 11000 // +1000ms (Newer)
            accuracy = 50f // Not less accurate
        }

        assertTrue(invokeIsBetterLocation(newer, current))
    }

    @Test
    fun isBetterLocation_returnsTrue_whenNewerAndNotSignificantlyLessAccurate_fromSameProvider() {
        val current = Location("gps").apply {
            time = 10000
            accuracy = 50f
        }
        val newer = Location("gps").apply {
            time = 11000 // +1000ms (Newer)
            accuracy = 100f // +50 (Less accurate, but not significantly > 200)
        }

        assertTrue(invokeIsBetterLocation(newer, current))
    }

    @Test
    fun isBetterLocation_returnsFalse_whenNewerButSignificantlyLessAccurate() {
        val current = Location("gps").apply {
            time = 10000
            accuracy = 50f
        }
        val newer = Location("gps").apply {
            time = 11000 // +1000ms (Newer)
            accuracy = 300f // +250 (Significantly less accurate > 200)
        }

        assertFalse(invokeIsBetterLocation(newer, current))
    }
}
