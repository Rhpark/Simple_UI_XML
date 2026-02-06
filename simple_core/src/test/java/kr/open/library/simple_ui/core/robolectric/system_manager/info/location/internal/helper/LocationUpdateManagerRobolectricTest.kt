package kr.open.library.simple_ui.core.robolectric.system_manager.info.location.internal.helper

import android.Manifest
import android.app.Application
import android.content.Context
import android.location.LocationManager
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateConstants.DEFAULT_UPDATE_CYCLE_DISTANCE
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateConstants.DEFAULT_UPDATE_CYCLE_TIME
import kr.open.library.simple_ui.core.system_manager.info.location.internal.helper.LocationUpdateManager
import kr.open.library.simple_ui.core.system_manager.info.location.internal.model.LocationStateData
import org.junit.After
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
 * Robolectric test for LocationUpdateManager.<br><br>
 * Verifies coordination between LocationStateReceiver and LocationStateEmitter.<br><br>
 * LocationUpdateManager에 대한 Robolectric 테스트입니다.<br>
 * LocationStateReceiver와 LocationStateEmitter 간의 조율을 검증합니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LocationUpdateManagerRobolectricTest {
    private lateinit var context: Context
    private lateinit var locationManager: LocationManager
    private lateinit var shadowLocationManager: ShadowLocationManager
    private lateinit var updateManager: LocationUpdateManager

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

        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true)

        updateManager = LocationUpdateManager(context, locationManager) {
            LocationStateData(
                location = null,
                isGpsEnabled = true,
                isNetworkEnabled = false,
                isPassiveEnabled = false,
                isFusedEnabled = null
            )
        }
    }

    @After
    fun tearDown() {
        updateManager.destroy()
    }

    // ==============================================
    // getSfUpdate() Tests
    // ==============================================

    @Test
    fun getSfUpdate_returnsNonNullSharedFlow() {
        val sfUpdate = updateManager.getSfUpdate()
        assertNotNull(sfUpdate)
    }

    // ==============================================
    // registerReceiver() Tests
    // ==============================================

    @Test
    fun registerReceiver_returnsTrue() {
        val result = updateManager.registerReceiver()
        assertTrue(result)
    }

    // ==============================================
    // registerLocationListener() Tests
    // ==============================================

    @Test
    fun registerLocationListener_returnsTrue() {
        val result = updateManager.registerLocationListener(
            LocationManager.GPS_PROVIDER,
            DEFAULT_UPDATE_CYCLE_TIME,
            DEFAULT_UPDATE_CYCLE_DISTANCE
        )
        assertTrue(result)
    }

    // ==============================================
    // updateStart() Tests
    // ==============================================

    @Test
    fun updateStart_returnsTrue_whenReceiverRegistered() {
        updateManager.registerReceiver()
        updateManager.registerLocationListener(
            LocationManager.GPS_PROVIDER,
            DEFAULT_UPDATE_CYCLE_TIME,
            DEFAULT_UPDATE_CYCLE_DISTANCE
        )

        val scope = CoroutineScope(Job())
        val result = updateManager.updateStart(scope, DEFAULT_UPDATE_CYCLE_TIME)
        assertTrue(result)
        updateManager.destroy()
        scope.cancel()
    }

    // ==============================================
    // destroy() Tests
    // ==============================================

    @Test
    fun destroy_doesNotCrash() {
        updateManager.registerReceiver()
        updateManager.registerLocationListener(
            LocationManager.GPS_PROVIDER,
            DEFAULT_UPDATE_CYCLE_TIME,
            DEFAULT_UPDATE_CYCLE_DISTANCE
        )
        updateManager.destroy()

        // Calling destroy again should not crash
        updateManager.destroy()
    }
}
