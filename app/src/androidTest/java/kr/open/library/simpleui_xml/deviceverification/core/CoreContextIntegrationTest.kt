package kr.open.library.simpleui_xml.deviceverification.core

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kr.open.library.simple_ui.core.permissions.extensions.hasPermission
import kr.open.library.simple_ui.core.permissions.extensions.remainPermissions
import kr.open.library.simpleui_xml.BuildConfig
import kr.open.library.simpleui_xml.deviceverification.harness.PhysicalDeviceRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class CoreContextIntegrationTest {
    @get:Rule
    val physicalDeviceRule = PhysicalDeviceRule()

    private val targetContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun coreP0001_contextApisAreConsumableWithoutInitialization() {
        assertEquals(BuildConfig.APPLICATION_ID, targetContext.packageName)
        assertTrue(targetContext.hasPermission(Manifest.permission.ACCESS_NETWORK_STATE))
        assertFalse(targetContext.hasPermission("kr.open.library.simpleui_xml.permission.UNKNOWN"))
    }

    @Test
    fun coreP0002_dangerousPermissionMatchesPackageManagerState() {
        val permission = Manifest.permission.CAMERA
        val expected =
            ContextCompat.checkSelfPermission(targetContext, permission) ==
                PackageManager.PERMISSION_GRANTED

        assertEquals(expected, targetContext.hasPermission(permission))
        assertEquals(
            if (expected) emptyList() else listOf(permission),
            targetContext.remainPermissions(listOf(permission)),
        )
    }
}
