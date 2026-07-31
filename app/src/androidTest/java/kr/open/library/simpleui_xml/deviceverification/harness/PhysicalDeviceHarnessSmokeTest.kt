package kr.open.library.simpleui_xml.deviceverification.harness

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kr.open.library.simpleui_xml.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class PhysicalDeviceHarnessSmokeTest {
    @get:Rule
    val physicalDeviceRule = PhysicalDeviceRule()

    @Test
    fun harnessP0001_targetContextAndEnvironmentAreAvailable() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

        assertEquals(BuildConfig.APPLICATION_ID, targetContext.packageName)
        assertTrue(physicalDeviceRule.environment.model.isNotBlank())
        assertTrue(physicalDeviceRule.environment.abi.isNotBlank())
        assertTrue(physicalDeviceRule.environment.apiLevel >= 28)
    }
}
