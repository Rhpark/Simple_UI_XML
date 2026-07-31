package kr.open.library.simpleui_xml.deviceverification.xml

import android.Manifest
import android.provider.Settings
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedItem
import kr.open.library.simpleui_xml.activity_fragment.activity.BaseActivityExample
import kr.open.library.simpleui_xml.deviceverification.harness.PhysicalDeviceRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
internal class XmlPermissionIntegrationTest {
    @get:Rule
    val physicalDeviceRule = PhysicalDeviceRule()

    @Test
    fun xmlP0004_normalAndSpecialPermissionResultsAreDeliveredOnce() {
        ActivityScenario.launch(BaseActivityExample::class.java).use { scenario ->
            val normalResult = AtomicReference<List<PermissionDeniedItem>>()
            val normalLatch = CountDownLatch(1)
            scenario.onActivity { activity ->
                activity.requestPermissions(listOf(Manifest.permission.ACCESS_WIFI_STATE)) { deniedItems ->
                    normalResult.set(deniedItems)
                    normalLatch.countDown()
                }
            }
            assertTrue(normalLatch.await(3L, TimeUnit.SECONDS))
            assertTrue(normalResult.get().isEmpty())

            scenario.onActivity { activity ->
                val isGranted = Settings.canDrawOverlays(activity)
                physicalDeviceRule.requirePrecondition(
                    scenarioId = "XML-P0-004",
                    expected = "overlayPermission=false",
                    actual = "overlayPermission=$isGranted",
                    isSatisfied = !isGranted,
                )
            }
            val specialResult = AtomicReference<List<PermissionDeniedItem>>()
            val callbackCount = AtomicInteger(0)
            val specialLatch = CountDownLatch(1)
            scenario.onActivity { activity ->
                activity.requestPermissions(
                    permissions = listOf(Manifest.permission.SYSTEM_ALERT_WINDOW),
                    onDeniedResult = { deniedItems ->
                        specialResult.set(deniedItems)
                        callbackCount.incrementAndGet()
                        specialLatch.countDown()
                    },
                    onRationaleNeeded = { it.cancel() },
                    onNavigateToSettings = { it.cancel() },
                )
            }
            assertTrue(specialLatch.await(3L, TimeUnit.SECONDS))
            assertTrue(specialResult.get().any { it.permission == Manifest.permission.SYSTEM_ALERT_WINDOW })
            assertTrue(callbackCount.get() == 1)
        }
    }
}
