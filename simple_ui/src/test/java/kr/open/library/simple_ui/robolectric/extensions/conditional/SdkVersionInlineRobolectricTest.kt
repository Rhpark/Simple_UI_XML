package kr.open.library.simple_ui.robolectric.extensions.conditional

import android.os.Build
import kr.open.library.simple_ui.extensions.conditional.checkSdkVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric tests for SDK version checking inline functions
 */
@RunWith(RobolectricTestRunner::class)
class SdkVersionInlineRobolectricTest {

    // ==============================================
    // checkSdkVersion(ver, doWork) - Unit type
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R]) // API 30
    fun checkSdkVersion_unit_executesWorkWhenSdkMatches() {
        var executed = false

        checkSdkVersion(Build.VERSION_CODES.R) {
            executed = true
        }

        assertTrue("doWork should be executed when SDK_INT >= ver", executed)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R]) // API 30
    fun checkSdkVersion_unit_executesWorkWhenSdkHigher() {
        var executed = false

        checkSdkVersion(Build.VERSION_CODES.Q) { // API 29 < 30
            executed = true
        }

        assertTrue("doWork should be executed when SDK_INT > ver", executed)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q]) // API 29
    fun checkSdkVersion_unit_doesNotExecuteWorkWhenSdkLower() {
        var executed = false

        checkSdkVersion(Build.VERSION_CODES.R) { // API 30 > 29
            executed = true
        }

        assertFalse("doWork should not be executed when SDK_INT < ver", executed)
    }

    // ==============================================
    // checkSdkVersion(ver, doWork) - Nullable return
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R]) // API 30
    fun checkSdkVersion_nullable_returnsResultWhenSdkMatches() {
        val result = checkSdkVersion<String>(Build.VERSION_CODES.R) {
            "Success"
        }

        assertEquals("Success", result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R]) // API 30
    fun checkSdkVersion_nullable_returnsResultWhenSdkHigher() {
        val result = checkSdkVersion<Int>(Build.VERSION_CODES.Q) { // API 29 < 30
            42
        }

        assertEquals(42, result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q]) // API 29
    fun checkSdkVersion_nullable_returnsNullWhenSdkLower() {
        val result = checkSdkVersion<String>(Build.VERSION_CODES.R) { // API 30 > 29
            "Should not return"
        }

        assertNull("Should return null when SDK_INT < ver", result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU]) // API 33
    fun checkSdkVersion_nullable_worksWithComplexTypes() {
        data class TestData(val value: String, val number: Int)

        val result = checkSdkVersion<TestData>(Build.VERSION_CODES.R) {
            TestData("test", 123)
        }

        assertEquals(TestData("test", 123), result)
    }

    // ==============================================
    // checkSdkVersion(ver, positiveWork, negativeWork)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R]) // API 30
    fun checkSdkVersion_branching_executesPositiveWorkWhenSdkMatches() {
        val result = checkSdkVersion(
            Build.VERSION_CODES.R,
            positiveWork = { "Positive" },
            negativeWork = { "Negative" }
        )

        assertEquals("Positive", result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R]) // API 30
    fun checkSdkVersion_branching_executesPositiveWorkWhenSdkHigher() {
        val result = checkSdkVersion(
            Build.VERSION_CODES.Q, // API 29 < 30
            positiveWork = { "Positive" },
            negativeWork = { "Negative" }
        )

        assertEquals("Positive", result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q]) // API 29
    fun checkSdkVersion_branching_executesNegativeWorkWhenSdkLower() {
        val result = checkSdkVersion(
            Build.VERSION_CODES.R, // API 30 > 29
            positiveWork = { "Positive" },
            negativeWork = { "Negative" }
        )

        assertEquals("Negative", result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU]) // API 33
    fun checkSdkVersion_branching_worksWithDifferentReturnTypes() {
        val result = checkSdkVersion(
            Build.VERSION_CODES.S,
            positiveWork = { 100 },
            negativeWork = { 0 }
        )

        assertEquals(100, result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q]) // API 29
    fun checkSdkVersion_branching_executesNegativeWorkWithDifferentReturnTypes() {
        val result = checkSdkVersion(
            Build.VERSION_CODES.R, // API 30 > 29
            positiveWork = { 100 },
            negativeWork = { 0 }
        )

        assertEquals(0, result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU]) // API 33
    fun checkSdkVersion_branching_worksWithComplexTypesPositive() {
        data class ComplexData(val name: String, val value: Int, val flag: Boolean)

        val result = checkSdkVersion(
            Build.VERSION_CODES.R,
            positiveWork = { ComplexData("positive", 100, true) },
            negativeWork = { ComplexData("negative", 0, false) }
        )

        assertEquals(ComplexData("positive", 100, true), result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q]) // API 29
    fun checkSdkVersion_branching_worksWithComplexTypesNegative() {
        data class ComplexData(val name: String, val value: Int, val flag: Boolean)

        val result = checkSdkVersion(
            Build.VERSION_CODES.R, // API 30 > 29
            positiveWork = { ComplexData("positive", 100, true) },
            negativeWork = { ComplexData("negative", 0, false) }
        )

        assertEquals(ComplexData("negative", 0, false), result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU]) // API 33
    fun checkSdkVersion_branching_worksWithBooleanPositive() {
        val result = checkSdkVersion(
            Build.VERSION_CODES.R,
            positiveWork = { true },
            negativeWork = { false }
        )

        assertTrue(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q]) // API 29
    fun checkSdkVersion_branching_worksWithBooleanNegative() {
        val result = checkSdkVersion(
            Build.VERSION_CODES.R, // API 30 > 29
            positiveWork = { true },
            negativeWork = { false }
        )

        assertFalse(result)
    }

    // ==============================================
    // Edge Cases
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.P]) // API 28 (minSdk for this library)
    fun checkSdkVersion_worksWithLowestSdk() {
        val result = checkSdkVersion(
            Build.VERSION_CODES.P,
            positiveWork = { "MinSdk" },
            negativeWork = { "Never" }
        )

        assertEquals("MinSdk", result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU]) // API 33
    fun checkSdkVersion_worksWithHighSdk() {
        val result = checkSdkVersion(
            Build.VERSION_CODES.TIRAMISU,
            positiveWork = { "Tiramisu" },
            negativeWork = { "Lower" }
        )

        assertEquals("Tiramisu", result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R]) // API 30
    fun checkSdkVersion_exactBoundary() {
        // Test exact boundary condition
        val result = checkSdkVersion(
            Build.VERSION_CODES.R, // Exactly API 30
            positiveWork = { true },
            negativeWork = { false }
        )

        assertTrue("Should execute positiveWork when SDK_INT == ver", result)
    }

    // ==============================================
    // Real-world scenarios
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.S]) // API 31
    fun checkSdkVersion_realWorld_featureFlag() {
        // Simulate feature availability check
        val isFeatureAvailable: Boolean = checkSdkVersion<Boolean>(Build.VERSION_CODES.R) {
            true
        } ?: false

        assertTrue("Feature should be available on API 31", isFeatureAvailable)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q]) // API 29
    fun checkSdkVersion_realWorld_featureFlagNotAvailable() {
        // Simulate feature availability check
        val isFeatureAvailable: Boolean = checkSdkVersion<Boolean>(Build.VERSION_CODES.R) {
            true
        } ?: false

        assertFalse("Feature should not be available on API 29", isFeatureAvailable)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R]) // API 30
    fun checkSdkVersion_realWorld_apiSelection() {
        // Simulate API selection based on SDK version
        val apiEndpoint = checkSdkVersion(
            Build.VERSION_CODES.R,
            positiveWork = { "api/v2/endpoint" },
            negativeWork = { "api/v1/endpoint" }
        )

        assertEquals("api/v2/endpoint", apiEndpoint)
    }
}
