package kr.open.library.simple_ui.core.robolectric.system_manager.info.location

import android.Manifest
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateConstants.MIN_UPDATE_CYCLE_TIME
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateConstants.POLLING_DISABLED_UPDATE_CYCLE_TIME
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLocationManager

/**
 * Robolectric test for LocationStateInfo.<br><br>
 * Verifies public API delegation and parameter validation.<br><br>
 * LocationStateInfo에 대한 Robolectric 테스트입니다.<br>
 * 공개 API 위임과 파라미터 유효성 검증을 테스트합니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LocationStateInfoRobolectricTest {
    private lateinit var context: Context
    private lateinit var locationStateInfo: LocationStateInfo
    private lateinit var locationManager: LocationManager
    private lateinit var shadowLocationManager: ShadowLocationManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val shadowApp = shadowOf(context as Application)
        shadowApp.grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        locationStateInfo = LocationStateInfo(context)
        locationManager = locationStateInfo.locationManager
        shadowLocationManager = shadowOf(locationManager)
    }

    // ==============================================
    // registerStart() parameter validation Tests
    // ==============================================

    @Test(expected = IllegalArgumentException::class)
    fun registerStart_throwsException_whenUpdateCycleTimeBelowMin() {
        kotlinx.coroutines.test.runTest {
            locationStateInfo.registerStart(
                coroutineScope = this,
                locationProvider = LocationManager.GPS_PROVIDER,
                updateCycleTime = MIN_UPDATE_CYCLE_TIME - 1
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun registerStart_throwsException_whenMinDistanceNegative() {
        kotlinx.coroutines.test.runTest {
            locationStateInfo.registerStart(
                coroutineScope = this,
                locationProvider = LocationManager.GPS_PROVIDER,
                minDistanceM = -1f
            )
        }
    }

    @Test
    fun registerStart_acceptsPollingDisabledUpdateCycleTime() {
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true)

        val scope = CoroutineScope(Job())
        try {
            val result = locationStateInfo.registerStart(
                coroutineScope = scope,
                locationProvider = LocationManager.GPS_PROVIDER,
                updateCycleTime = POLLING_DISABLED_UPDATE_CYCLE_TIME
            )
            assertTrue(result)
        } finally {
            locationStateInfo.unRegister()
            scope.cancel()
        }
    }

    @Test
    fun registerStart_acceptsMinUpdateCycleTime() {
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true)

        val scope = CoroutineScope(Job())
        try {
            val result = locationStateInfo.registerStart(
                coroutineScope = scope,
                locationProvider = LocationManager.GPS_PROVIDER,
                updateCycleTime = MIN_UPDATE_CYCLE_TIME
            )
            assertTrue(result)
        } finally {
            locationStateInfo.unRegister()
            scope.cancel()
        }
    }

    // ==============================================
    // Provider delegation Tests (queryHelper)
    // ==============================================

    @Test
    fun isGpsEnabled_delegatesToQueryHelper() {
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true)
        assertTrue(locationStateInfo.isGpsEnabled())

        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, false)
        assertFalse(locationStateInfo.isGpsEnabled())
    }

    @Test
    fun isNetworkEnabled_delegatesToQueryHelper() {
        shadowLocationManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, true)
        assertTrue(locationStateInfo.isNetworkEnabled())

        shadowLocationManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, false)
        assertFalse(locationStateInfo.isNetworkEnabled())
    }

    @Test
    fun isPassiveEnabled_delegatesToQueryHelper() {
        shadowLocationManager.setProviderEnabled(LocationManager.PASSIVE_PROVIDER, true)
        assertTrue(locationStateInfo.isPassiveEnabled())

        shadowLocationManager.setProviderEnabled(LocationManager.PASSIVE_PROVIDER, false)
        assertFalse(locationStateInfo.isPassiveEnabled())
    }

    @Test
    fun isLocationEnabled_delegatesToQueryHelper() {
        shadowLocationManager.setLocationEnabled(true)
        assertTrue(locationStateInfo.isLocationEnabled())

        shadowLocationManager.setLocationEnabled(false)
        assertFalse(locationStateInfo.isLocationEnabled())
    }

    @Test
    fun isAnyEnabled_delegatesToQueryHelper() {
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true)
        assertTrue(locationStateInfo.isAnyEnabled())
    }

    // ==============================================
    // Calculator delegation Tests
    // ==============================================

    @Test
    fun calculateDistance_delegatesToCalculator() {
        val from = Location("gps").apply {
            latitude = 37.5665
            longitude = 126.9780
        }
        val to = Location("gps").apply {
            latitude = 37.5666
            longitude = 126.9781
        }
        val distance = locationStateInfo.calculateDistance(from, to)
        assertTrue(distance >= 0f)
    }

    @Test
    fun calculateBearing_delegatesToCalculator() {
        val from = Location("gps").apply {
            latitude = 37.5665
            longitude = 126.9780
        }
        val to = Location("gps").apply {
            latitude = 35.1796
            longitude = 129.0756
        }
        val bearing = locationStateInfo.calculateBearing(from, to)
        assertTrue(bearing >= -180f && bearing <= 180f)
    }

    @Test
    fun isLocationWithRadius_delegatesToCalculator() {
        val location = Location("gps").apply {
            latitude = 37.5665
            longitude = 126.9780
        }
        assertTrue(locationStateInfo.isLocationWithRadius(location, location, 100f))
    }

    // ==============================================
    // sfUpdate Tests
    // ==============================================

    @Test
    fun sfUpdate_returnsNonNullSharedFlow() {
        assertNotNull(locationStateInfo.sfUpdate)
    }
}
