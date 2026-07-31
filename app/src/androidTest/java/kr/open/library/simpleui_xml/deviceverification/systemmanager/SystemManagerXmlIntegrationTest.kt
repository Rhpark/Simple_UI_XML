package kr.open.library.simpleui_xml.deviceverification.systemmanager

import android.provider.Settings
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import kr.open.library.simple_ui.system_manager.core.base.SystemResult
import kr.open.library.simple_ui.system_manager.xml.extensions.destroySystemBarControllerCache
import kr.open.library.simple_ui.system_manager.xml.extensions.getFloatingViewController
import kr.open.library.simple_ui.system_manager.xml.extensions.getSoftKeyboardController
import kr.open.library.simple_ui.system_manager.xml.extensions.getSystemBarController
import kr.open.library.simpleui_xml.deviceverification.harness.PhysicalDeviceRule
import kr.open.library.simpleui_xml.system_service_manager.controller.ServiceManagerControllerActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class SystemManagerXmlIntegrationTest {
    @get:Rule
    val physicalDeviceRule = PhysicalDeviceRule()

    @Test
    fun sysP0010_systemBarStateCanBeAppliedAndRestored() {
        ActivityScenario.launch(ServiceManagerControllerActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val controller = activity.window.getSystemBarController()
                val original = controller.isEdgeToEdgeEnabled()

                try {
                    controller.setEdgeToEdgeMode(!original)
                    assertEquals(!original, controller.isEdgeToEdgeEnabled())
                    controller.setEdgeToEdgeMode(original)
                    assertEquals(original, controller.isEdgeToEdgeEnabled())
                } finally {
                    activity.window.destroySystemBarControllerCache()
                }
            }
        }
    }

    @Test
    fun sysP0011_softKeyboardControllerCanSafelyHide() {
        ActivityScenario.launch(ServiceManagerControllerActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.getSoftKeyboardController().hide(activity.window.decorView).requireSuccess()
            }
        }
    }

    @Test
    fun sysP0012_floatingControllerCanRemoveEmptyState() {
        ActivityScenario.launch(ServiceManagerControllerActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val controller = activity.getFloatingViewController()
                val result = controller.removeAllFloatingView()
                if (Settings.canDrawOverlays(activity)) {
                    result.requireSuccess()
                } else {
                    assertTrue(result is SystemResult.PermissionDenied)
                }
                assertEquals(null, controller.getFloatingFixedView())
            }
        }
    }
}
