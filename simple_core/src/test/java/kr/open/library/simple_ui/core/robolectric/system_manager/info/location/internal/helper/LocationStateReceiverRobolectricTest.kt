package kr.open.library.simple_ui.core.robolectric.system_manager.info.location.internal.helper

import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.system_manager.info.location.internal.helper.LocationStateReceiver
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LocationStateReceiverRobolectricTest {
    private lateinit var context: Context
    private lateinit var locationManager: LocationManager
    private lateinit var receiver: LocationStateReceiver

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        locationManager = mock(LocationManager::class.java)
        receiver = LocationStateReceiver(context, locationManager) {}
    }

    @Test
    fun registerLocationListener_reappliesSettingsOnReRegister() {
        val first = receiver.registerLocationListener(LocationManager.GPS_PROVIDER, 2000L, 1f)
        val second = receiver.registerLocationListener(LocationManager.NETWORK_PROVIDER, 5000L, 10f)

        assertTrue(first)
        assertTrue(second)

        verify(locationManager, times(1)).removeUpdates(any(LocationListener::class.java))
        verify(locationManager, times(1)).requestLocationUpdates(
            eq(LocationManager.GPS_PROVIDER),
            eq(2000L),
            eq(1f),
            any(LocationListener::class.java),
        )
        verify(locationManager, times(1)).requestLocationUpdates(
            eq(LocationManager.NETWORK_PROVIDER),
            eq(5000L),
            eq(10f),
            any(LocationListener::class.java),
        )
    }
}
