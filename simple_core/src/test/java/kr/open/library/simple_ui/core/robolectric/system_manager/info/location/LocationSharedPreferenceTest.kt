package kr.open.library.simple_ui.core.robolectric.system_manager.info.location

import android.location.Location
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.system_manager.info.location.LocationSharedPreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R])
class LocationSharedPreferenceTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun `loadLocation returns null when nothing saved`() {
        val prefs = LocationSharedPreference(context)

        assertNull(prefs.loadLocation())
    }

    @Test
    fun `saveApplyLocation persists coordinates`() {
        val prefs = LocationSharedPreference(context)
        val location =
            Location("gps").apply {
                latitude = 37.5
                longitude = 127.0
                accuracy = 12.3f
                time = 12345L
                provider = "gps"
            }

        prefs.saveApplyLocation(location)

        val restored = prefs.loadLocation()
        requireNotNull(restored)
        assertEquals(location.latitude, restored.latitude, 0.0)
        assertEquals(location.longitude, restored.longitude, 0.0)
        assertEquals(location.accuracy, restored.accuracy, 0.0f)
        assertEquals(location.time, restored.time)
        assertEquals(location.provider, restored.provider)
    }

    @Test
    fun `removeApply clears saved coordinates`() {
        val prefs = LocationSharedPreference(context)
        val location =
            Location("gps").apply {
                latitude = 1.0
                longitude = 2.0
                accuracy = 3f
                time = 999L
                provider = "gps"
            }

        prefs.saveApplyLocation(location)
        prefs.removeApply()

        assertNull(prefs.loadLocation())
    }
}
