package kr.open.library.simpleui_xml.deviceverification.core

import android.Manifest
import android.provider.Settings
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kr.open.library.simple_ui.core.permissions.extensions.hasPermission
import kr.open.library.simpleui_xml.deviceverification.harness.PhysicalDeviceRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class CorePermissionIntegrationTest {
    @get:Rule
    val physicalDeviceRule = PhysicalDeviceRule()

    @Test
    fun coreP0003_overlayPermissionMatchesPlatformState() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

        assertEquals(
            Settings.canDrawOverlays(targetContext),
            targetContext.hasPermission(Manifest.permission.SYSTEM_ALERT_WINDOW),
        )
    }
}
