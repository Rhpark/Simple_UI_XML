package kr.open.library.simple_ui.core.robolectric.system_manager.info.location.internal.helper

import android.Manifest
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.system_manager.info.location.internal.helper.LocationQueryHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLocationManager

/**
 * Robolectric test for LocationQueryHelper.<br><br>
 * Verifies provider state queries and best-location selection logic.<br><br>
 * LocationQueryHelper에 대한 Robolectric 테스트입니다.<br>
 * 제공자 상태 조회와 최적 위치 선택 로직을 검증합니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LocationQueryHelperRobolectricTest {
    private lateinit var context: Context
    private lateinit var locationManager: LocationManager
    private lateinit var shadowLocationManager: ShadowLocationManager
    private lateinit var queryHelper: LocationQueryHelper

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val shadowApp = shadowOf(context as Application)
        shadowApp.grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        shadowLocationManager = shadowOf(locationManager)
        queryHelper = LocationQueryHelper(context, locationManager)
    }

    // ==============================================
    // isGpsEnabled() Tests
    // ==============================================

    @Test
    fun isGpsEnabled_returnsTrue_whenProviderEnabled() {
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true)
        assertTrue(queryHelper.isGpsEnabled())
    }

    @Test
    fun isGpsEnabled_returnsFalse_whenProviderDisabled() {
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, false)
        assertFalse(queryHelper.isGpsEnabled())
    }

    // ==============================================
    // isNetworkEnabled() Tests
    // ==============================================

    @Test
    fun isNetworkEnabled_returnsTrue_whenProviderEnabled() {
        shadowLocationManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, true)
        assertTrue(queryHelper.isNetworkEnabled())
    }

    @Test
    fun isNetworkEnabled_returnsFalse_whenProviderDisabled() {
        shadowLocationManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, false)
        assertFalse(queryHelper.isNetworkEnabled())
    }

    // ==============================================
    // isPassiveEnabled() Tests
    // ==============================================

    @Test
    fun isPassiveEnabled_returnsTrue_whenProviderEnabled() {
        shadowLocationManager.setProviderEnabled(LocationManager.PASSIVE_PROVIDER, true)
        assertTrue(queryHelper.isPassiveEnabled())
    }

    @Test
    fun isPassiveEnabled_returnsFalse_whenProviderDisabled() {
        shadowLocationManager.setProviderEnabled(LocationManager.PASSIVE_PROVIDER, false)
        assertFalse(queryHelper.isPassiveEnabled())
    }

    // ==============================================
    // isLocationEnabled() Tests
    // ==============================================

    @Test
    fun isLocationEnabled_returnsTrue_whenLocationEnabled() {
        shadowLocationManager.setLocationEnabled(true)
        assertTrue(queryHelper.isLocationEnabled())
    }

    @Test
    fun isLocationEnabled_returnsFalse_whenLocationDisabled() {
        shadowLocationManager.setLocationEnabled(false)
        assertFalse(queryHelper.isLocationEnabled())
    }

    // ==============================================
    // isAnyEnabled() Tests
    // ==============================================

    @Test
    fun isAnyEnabled_returnsTrue_whenGpsEnabled() {
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true)
        shadowLocationManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, false)
        shadowLocationManager.setProviderEnabled(LocationManager.PASSIVE_PROVIDER, false)
        // Note: setLocationEnabled affects isProviderEnabled on API 28+, so we don't disable it

        assertTrue(queryHelper.isAnyEnabled())
    }

    @Test
    fun isAnyEnabled_returnsFalse_whenAllDisabled() {
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, false)
        shadowLocationManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, false)
        shadowLocationManager.setProviderEnabled(LocationManager.PASSIVE_PROVIDER, false)
        shadowLocationManager.setLocationEnabled(false)

        assertFalse(queryHelper.isAnyEnabled())
    }

    // ==============================================
    // getLocation() Tests
    // ==============================================

    @Test
    fun getLocation_returnsNull_whenNoProviderEnabled() {
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, false)
        shadowLocationManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, false)
        shadowLocationManager.setProviderEnabled(LocationManager.PASSIVE_PROVIDER, false)
        shadowLocationManager.setLocationEnabled(false)

        assertNull(queryHelper.getLocation())
    }

    @Test
    fun getLocation_returnsLocation_whenProviderEnabledAndHasLastKnownLocation() {
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true)
        shadowLocationManager.setProviderEnabled(LocationManager.PASSIVE_PROVIDER, true)

        val gpsLocation = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = 37.5665
            longitude = 126.9780
            time = System.currentTimeMillis()
            accuracy = 10f
        }
        shadowLocationManager.setLastKnownLocation(LocationManager.GPS_PROVIDER, gpsLocation)

        val result = queryHelper.getLocation()
        assertNotNull(result)
        assertEquals(37.5665, result!!.latitude, 0.0001)
    }

    @Test
    fun getLocation_returnsBestLocation_whenMultipleProvidersHaveLocations() {
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true)
        shadowLocationManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, true)
        shadowLocationManager.setProviderEnabled(LocationManager.PASSIVE_PROVIDER, true)

        val now = System.currentTimeMillis()

        val gpsLocation = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = 37.5665
            longitude = 126.9780
            time = now
            accuracy = 5f
        }
        val networkLocation = Location(LocationManager.NETWORK_PROVIDER).apply {
            latitude = 37.5700
            longitude = 126.9800
            time = now - 1000
            accuracy = 50f
        }

        shadowLocationManager.setLastKnownLocation(LocationManager.GPS_PROVIDER, gpsLocation)
        shadowLocationManager.setLastKnownLocation(LocationManager.NETWORK_PROVIDER, networkLocation)

        val result = queryHelper.getLocation()
        assertNotNull(result)
        assertEquals(5f, result!!.accuracy, 0.001f)
    }
}
