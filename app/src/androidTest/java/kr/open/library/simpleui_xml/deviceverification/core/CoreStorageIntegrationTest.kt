package kr.open.library.simpleui_xml.deviceverification.core

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kr.open.library.simple_ui.core.local.base.BaseSharedPreference
import kr.open.library.simpleui_xml.deviceverification.harness.PhysicalDeviceRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class CoreStorageIntegrationTest {
    @get:Rule
    val physicalDeviceRule = PhysicalDeviceRule()

    private val targetContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun coreP0004_preferencesPersistAcrossWrapperRecreationAndCanBeRemoved() {
        val preference = DeviceVerificationPreference(targetContext)
        physicalDeviceRule.cleanup.register("core 테스트 SharedPreferences 삭제") {
            DeviceVerificationPreference(targetContext).clear()
        }

        preference.clear()
        assertNull(preference.text)
        assertEquals(0, preference.count)
        assertFalse(preference.enabled)

        preference.text = "physical-device"
        preference.count = 31
        preference.enabled = true

        val recreated = DeviceVerificationPreference(targetContext)
        assertEquals("physical-device", recreated.text)
        assertEquals(31, recreated.count)
        assertTrue(recreated.enabled)

        recreated.clear()
        val cleared = DeviceVerificationPreference(targetContext)
        assertNull(cleared.text)
        assertEquals(0, cleared.count)
        assertFalse(cleared.enabled)
    }

    private class DeviceVerificationPreference(
        context: Context,
    ) : BaseSharedPreference(context, GROUP_KEY) {
        var text: String? by stringPref(KEY_TEXT)
        var count: Int by intPref(KEY_COUNT, 0)
        var enabled: Boolean by booleanPref(KEY_ENABLED, false)

        fun clear() {
            removeAllApply()
        }

        private companion object {
            const val GROUP_KEY = "device_verification_core"
            const val KEY_TEXT = "text"
            const val KEY_COUNT = "count"
            const val KEY_ENABLED = "enabled"
        }
    }
}
