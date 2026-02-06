package kr.open.library.simple_ui.core.robolectric.system_manager.info.location.internal.helper

import android.location.Location
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateConstants.SIGNIFICANT_ACCURACY_DELTA_METERS
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateConstants.SIGNIFICANT_TIME_DELTA_MS
import kr.open.library.simple_ui.core.system_manager.info.location.internal.helper.LocationQuality
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric test for LocationQuality.<br><br>
 * Verifies isBetter() heuristic across all branches (null, time, accuracy, provider).<br><br>
 * LocationQuality에 대한 Robolectric 테스트입니다.<br>
 * isBetter() 휴리스틱의 모든 분기(null, 시간, 정확도, 제공자)를 검증합니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LocationQualityRobolectricTest {
    // ==============================================
    // Early-return branches
    // ==============================================

    @Test
    fun isBetter_returnsTrue_whenCurrentBestIsNull() {
        val candidate = Location("gps")
        assertTrue(LocationQuality.isBetter(candidate, null))
    }

    @Test
    fun isBetter_returnsTrue_whenSignificantlyNewer() {
        val current = Location("gps").apply {
            time = 1_000L
            accuracy = 30f
        }
        val candidate = Location("gps").apply {
            time = 1_000L + SIGNIFICANT_TIME_DELTA_MS + 1
            accuracy = 100f
        }
        assertTrue(LocationQuality.isBetter(candidate, current))
    }

    @Test
    fun isBetter_returnsFalse_whenSignificantlyOlder() {
        val current = Location("gps").apply {
            time = 10_000L
            accuracy = 30f
        }
        val candidate = Location("gps").apply {
            time = 10_000L - SIGNIFICANT_TIME_DELTA_MS - 1
            accuracy = 10f
        }
        assertFalse(LocationQuality.isBetter(candidate, current))
    }

    // ==============================================
    // when-branch: isMoreAccurate -> true
    // ==============================================

    @Test
    fun isBetter_returnsTrue_whenMoreAccurate() {
        val current = Location("gps").apply {
            time = 10_000L
            accuracy = 50f
        }
        val candidate = Location("gps").apply {
            time = 10_000L
            accuracy = 30f
        }
        assertTrue(LocationQuality.isBetter(candidate, current))
    }

    // ==============================================
    // when-branch: isNewer && !isLessAccurate -> true
    // ==============================================

    @Test
    fun isBetter_returnsTrue_whenNewerAndNotLessAccurate() {
        val current = Location("gps").apply {
            time = 10_000L
            accuracy = 50f
        }
        val candidate = Location("gps").apply {
            time = 11_000L
            accuracy = 50f
        }
        assertTrue(LocationQuality.isBetter(candidate, current))
    }

    // ==============================================
    // when-branch: isNewer && !isSignificantlyLessAccurate && isFromSameProvider -> true
    // ==============================================

    @Test
    fun isBetter_returnsTrue_whenNewerAndNotSignificantlyLessAccurateFromSameProvider() {
        val current = Location("gps").apply {
            time = 10_000L
            accuracy = 50f
        }
        val candidate = Location("gps").apply {
            time = 11_000L
            accuracy = 50f + SIGNIFICANT_ACCURACY_DELTA_METERS
        }
        assertTrue(LocationQuality.isBetter(candidate, current))
    }

    // ==============================================
    // when-branch else: false cases
    // ==============================================

    @Test
    fun isBetter_returnsFalse_whenNewerButSignificantlyLessAccurate() {
        val current = Location("gps").apply {
            time = 10_000L
            accuracy = 10f
        }
        val candidate = Location("gps").apply {
            time = 11_000L
            accuracy = 10f + SIGNIFICANT_ACCURACY_DELTA_METERS + 1
        }
        assertFalse(LocationQuality.isBetter(candidate, current))
    }

    @Test
    fun isBetter_returnsFalse_whenOlderAndLessAccurate() {
        val current = Location("gps").apply {
            time = 10_000L
            accuracy = 30f
        }
        val candidate = Location("gps").apply {
            time = 9_000L
            accuracy = 50f
        }
        assertFalse(LocationQuality.isBetter(candidate, current))
    }

    @Test
    fun isBetter_returnsFalse_whenSameTimeSameAccuracy() {
        val current = Location("gps").apply {
            time = 10_000L
            accuracy = 30f
        }
        val candidate = Location("gps").apply {
            time = 10_000L
            accuracy = 30f
        }
        assertFalse(LocationQuality.isBetter(candidate, current))
    }

    @Test
    fun isBetter_returnsFalse_whenNewerButLessAccurateFromDifferentProvider() {
        val current = Location("gps").apply {
            time = 10_000L
            accuracy = 50f
        }
        val candidate = Location("network").apply {
            time = 11_000L
            accuracy = 50f + SIGNIFICANT_ACCURACY_DELTA_METERS
        }
        assertFalse(LocationQuality.isBetter(candidate, current))
    }
}
