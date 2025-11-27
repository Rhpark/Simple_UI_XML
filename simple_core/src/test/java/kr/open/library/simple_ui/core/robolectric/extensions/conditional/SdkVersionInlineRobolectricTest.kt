package kr.open.library.simple_ui.core.robolectric.extensions.conditional

import android.os.Build
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R])
class SdkVersionInlineRobolectricTest {

    @Test
    fun `checkSdkVersion(Unit) covers both branches`() {
        val target = Build.VERSION_CODES.R

        val executedOnOrAbove = withSdk(Build.VERSION_CODES.TIRAMISU) {
            runUnitCheck(target)
        }
        val executedBelow = withSdk(Build.VERSION_CODES.Q) {
            runUnitCheck(target)
        }

        assertTrue(executedOnOrAbove)
        assertFalse(executedBelow)
    }

    @Test
    fun `nullable checkSdkVersion covers both branches`() {
        val target = Build.VERSION_CODES.S

        val resultOnOrAbove = withSdk(Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            runNullableCheck(target)
        }
        val resultBelow = withSdk(Build.VERSION_CODES.R) {
            runNullableCheck(target)
        }

        assertEquals("allowed", resultOnOrAbove)
        assertNull(resultBelow)
    }

    @Test
    fun `positive and negative work both execute`() {
        val target = Build.VERSION_CODES.Q

        val positive = withSdk(Build.VERSION_CODES.S) {
            runBranchCheck(target)
        }
        val negative = withSdk(Build.VERSION_CODES.P) {
            runBranchCheck(target)
        }

        assertEquals("positive", positive)
        assertEquals("negative", negative)
    }

    private fun runUnitCheck(requiredSdk: Int): Boolean {
        var executed = false
        checkSdkVersion(requiredSdk) {
            executed = true
        }
        return executed
    }

    private fun runNullableCheck(requiredSdk: Int): String? =
        checkSdkVersion<String>(requiredSdk) { "allowed" }

    private fun runBranchCheck(requiredSdk: Int): String =
        checkSdkVersion(
            requiredSdk,
            positiveWork = { "positive" },
            negativeWork = { "negative" }
        )

    private fun <T> withSdk(version: Int, block: () -> T): T {
        val original = Build.VERSION.SDK_INT
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", version)
        return try {
            block()
        } finally {
            ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", original)
        }
    }
}
