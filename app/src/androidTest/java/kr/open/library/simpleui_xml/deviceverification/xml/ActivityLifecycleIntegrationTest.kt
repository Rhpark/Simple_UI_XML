package kr.open.library.simpleui_xml.deviceverification.xml

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.activity_fragment.activity.BaseActivityExample
import kr.open.library.simpleui_xml.activity_fragment.activity.BaseBindingActivityExample
import kr.open.library.simpleui_xml.deviceverification.harness.PhysicalDeviceRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ActivityLifecycleIntegrationTest {
    @get:Rule
    val physicalDeviceRule = PhysicalDeviceRule()

    @Test
    fun xmlP0001_baseActivitiesBindAndRecreateOnPhysicalDevice() {
        ActivityScenario.launch(BaseActivityExample::class.java).use { scenario ->
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
            scenario.onActivity { activity ->
                assertNotNull(activity.findViewById(R.id.tvStatusBarHeight))
                assertNotNull(activity.findViewById(R.id.tvNavigationBarHeight))
            }
            scenario.recreate()
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }

        ActivityScenario.launch(BaseBindingActivityExample::class.java).use { scenario ->
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
            scenario.onActivity { activity ->
                assertNotNull(activity.findViewById(R.id.tvCounter))
            }
            scenario.recreate()
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }
}
