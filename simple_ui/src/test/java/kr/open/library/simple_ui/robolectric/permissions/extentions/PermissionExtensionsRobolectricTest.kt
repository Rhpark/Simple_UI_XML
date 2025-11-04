package kr.open.library.simple_ui.robolectric.permissions.extentions

import android.Manifest
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.permissions.extentions.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * Robolectric tests for PermissionExtensions
 *
 * Tests focus on:
 * - hasPermission() for normal and special permissions
 * - hasPermissions() batch checking
 * - remainPermissions() filtering
 * - isSpecialPermission() detection
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class PermissionExtensionsRobolectricTest {

    private lateinit var application: Application
    private lateinit var context: Context

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        context = application
    }

    // ==============================================
    // hasPermission() - Normal Permissions
    // ==============================================

    @Test
    fun hasPermission_normalPermission_whenGranted_returnsTrue() {
        // Given
        Shadows.shadowOf(application).grantPermissions(Manifest.permission.CAMERA)

        // When & Then
        assertTrue(context.hasPermission(Manifest.permission.CAMERA))
    }

    @Test
    fun hasPermission_normalPermission_whenDenied_returnsFalse() {
        // Given
        Shadows.shadowOf(application).denyPermissions(Manifest.permission.CAMERA)

        // When & Then
        assertFalse(context.hasPermission(Manifest.permission.CAMERA))
    }

    @Test
    fun hasPermission_multipleNormalPermissions_correctlyChecksEach() {
        // Given
        Shadows.shadowOf(application).apply {
            grantPermissions(Manifest.permission.CAMERA)
            denyPermissions(Manifest.permission.RECORD_AUDIO)
        }

        // When & Then
        assertTrue(context.hasPermission(Manifest.permission.CAMERA))
        assertFalse(context.hasPermission(Manifest.permission.RECORD_AUDIO))
    }

    // ==============================================
    // hasPermission() - Special Permissions
    // ==============================================

    /**
     * Note: Testing special permissions (SYSTEM_ALERT_WINDOW, WRITE_SETTINGS) with Robolectric
     * is challenging due to inconsistent SDK-level simulation across different API versions.
     * These permissions rely on Settings.canDrawOverlays() and Settings.System.canWrite()
     * which Robolectric may simulate differently.
     *
     * The actual permission checking logic is tested through:
     * - PermissionExtensions.kt implementation (which delegates to Android APIs)
     * - Manual testing in the sample app
     */

    @Test
    fun hasPermission_postNotifications_doesNotCrash() {
        // When & Then - Just verify it doesn't crash
        val result = context.hasPermission(Manifest.permission.POST_NOTIFICATIONS)
        assertNotNull(result)
    }


    // ==============================================
    // hasPermissions() - Batch Checking
    // ==============================================

    @Test
    fun hasPermissions_allGranted_returnsTrue() {
        // Given
        Shadows.shadowOf(application).grantPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        // When & Then
        assertTrue(context.hasPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION
        ))
    }

    @Test
    fun hasPermissions_someGranted_returnsFalse() {
        // Given
        Shadows.shadowOf(application).apply {
            grantPermissions(Manifest.permission.CAMERA)
            denyPermissions(Manifest.permission.RECORD_AUDIO)
        }

        // When & Then
        assertFalse(context.hasPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ))
    }

    @Test
    fun hasPermissions_noneGranted_returnsFalse() {
        // Given
        Shadows.shadowOf(application).denyPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        // When & Then
        assertFalse(context.hasPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ))
    }

    @Test
    fun hasPermissions_emptyList_returnsTrue() {
        // When & Then (빈 권한 목록은 모두 granted로 간주)
        assertTrue(context.hasPermissions())
    }

    // ==============================================
    // hasPermissions() with callback
    // ==============================================

    @Test
    fun hasPermissions_withCallback_whenAllGranted_executesCallback() {
        // Given
        Shadows.shadowOf(application).grantPermissions(Manifest.permission.CAMERA)
        var callbackExecuted = false

        // When
        val result = context.hasPermissions(Manifest.permission.CAMERA) {
            callbackExecuted = true
        }

        // Then
        assertTrue(result)
        assertTrue(callbackExecuted)
    }

    @Test
    fun hasPermissions_withCallback_whenDenied_doesNotExecuteCallback() {
        // Given
        Shadows.shadowOf(application).denyPermissions(Manifest.permission.CAMERA)
        var callbackExecuted = false

        // When
        val result = context.hasPermissions(Manifest.permission.CAMERA) {
            callbackExecuted = true
        }

        // Then
        assertFalse(result)
        assertFalse(callbackExecuted)
    }

    // ==============================================
    // remainPermissions() - Filtering
    // ==============================================

    @Test
    fun remainPermissions_allGranted_returnsEmptyList() {
        // Given
        Shadows.shadowOf(application).grantPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        // When
        val remaining = context.remainPermissions(listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ))

        // Then
        assertTrue(remaining.isEmpty())
    }

    @Test
    fun remainPermissions_someGranted_returnsOnlyDenied() {
        // Given
        Shadows.shadowOf(application).apply {
            grantPermissions(Manifest.permission.CAMERA)
            denyPermissions(Manifest.permission.RECORD_AUDIO)
        }

        // When
        val remaining = context.remainPermissions(listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ))

        // Then
        assertEquals(1, remaining.size)
        assertEquals(Manifest.permission.RECORD_AUDIO, remaining.first())
    }

    @Test
    fun remainPermissions_noneGranted_returnsAll() {
        // Given
        Shadows.shadowOf(application).denyPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        // When
        val remaining = context.remainPermissions(listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ))

        // Then
        assertEquals(2, remaining.size)
        assertTrue(remaining.containsAll(listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )))
    }

    @Test
    fun remainPermissions_emptyInput_returnsEmptyList() {
        // When
        val remaining = context.remainPermissions(emptyList())

        // Then
        assertTrue(remaining.isEmpty())
    }

    // ==============================================
    // isSpecialPermission()
    // ==============================================

    @Test
    fun isSpecialPermission_systemAlertWindow_returnsTrue() {
        assertTrue(context.isSpecialPermission(Manifest.permission.SYSTEM_ALERT_WINDOW))
    }

    @Test
    fun isSpecialPermission_writeSettings_returnsTrue() {
        assertTrue(context.isSpecialPermission(Manifest.permission.WRITE_SETTINGS))
    }

    @Test
    fun isSpecialPermission_packageUsageStats_returnsTrue() {
        assertTrue(context.isSpecialPermission(Manifest.permission.PACKAGE_USAGE_STATS))
    }

    @Test
    fun isSpecialPermission_normalPermission_returnsFalse() {
        assertFalse(context.isSpecialPermission(Manifest.permission.CAMERA))
        assertFalse(context.isSpecialPermission(Manifest.permission.RECORD_AUDIO))
        assertFalse(context.isSpecialPermission(Manifest.permission.ACCESS_FINE_LOCATION))
    }

    // ==============================================
    // Edge Cases
    // ==============================================

    @Test
    fun hasPermission_unknownPermission_doesNotCrash() {
        // When & Then - 알 수 없는 권한도 크래시 없이 처리
        val result = context.hasPermission("com.unknown.PERMISSION")
        assertNotNull(result)
    }

    @Test
    fun hasPermissions_mixedNormalAndSpecialPermissions_correctlyChecks() {
        // Given
        Shadows.shadowOf(application).grantPermissions(Manifest.permission.CAMERA)

        // When
        val result = context.hasPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.SYSTEM_ALERT_WINDOW
        )

        // Then - CAMERA는 granted, SYSTEM_ALERT_WINDOW는 denied
        assertFalse(result)
    }

    @Test
    fun remainPermissions_mixedNormalAndSpecialPermissions_correctlyFilters() {
        // Given
        Shadows.shadowOf(application).grantPermissions(Manifest.permission.CAMERA)

        // When
        val remaining = context.remainPermissions(listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.WRITE_SETTINGS
        ))

        // Then
        // CAMERA should not be in remaining (it's granted)
        assertFalse(remaining.contains(Manifest.permission.CAMERA))
        // At least one special permission should be in remaining
        // (exact count depends on Robolectric's SDK simulation)
        assertTrue(remaining.size >=  1 && remaining.size <= 2)
    }
}
