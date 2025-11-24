package kr.open.library.simple_ui.robolectric.permissions.manager

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import kr.open.library.simple_ui.permissions.extentions.hasPermission
import kr.open.library.simple_ui.permissions.extentions.remainPermissions
import kr.open.library.simple_ui.permissions.manager.CallbackAddResult
import kr.open.library.simple_ui.permissions.manager.PermissionManager
import kr.open.library.simple_ui.permissions.register.PermissionDelegate
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.lang.ref.WeakReference

/**
 * Robolectric tests for PermissionManager
 *
 * Tests comprehensive permission management functionality:
 * - Singleton pattern
 * - Normal permission requests
 * - Special permission requests
 * - Multiple callback support
 * - Request cancellation
 * - Delegate management
 * - Expired request cleanup
 * - Memory management (WeakReference)
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class PermissionManagerRobolectricTest {

    private lateinit var application: Application
    private lateinit var context: Context
    private lateinit var permissionManager: PermissionManager
    private lateinit var mockLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var mockSpecialLauncher: ActivityResultLauncher<Intent>
    private lateinit var mockDelegate: PermissionDelegate<*>

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        context = application
        permissionManager = PermissionManager.getInstance()

        // Mock launchers
        @Suppress("UNCHECKED_CAST")
        mockLauncher = mock(ActivityResultLauncher::class.java) as ActivityResultLauncher<Array<String>>

        @Suppress("UNCHECKED_CAST")
        mockSpecialLauncher = mock(ActivityResultLauncher::class.java) as ActivityResultLauncher<Intent>

        // Mock delegate
        @Suppress("UNCHECKED_CAST")
        mockDelegate = mock(PermissionDelegate::class.java) as PermissionDelegate<*>
        `when`(mockDelegate.getSpecialLauncher(anyString())).thenReturn(mockSpecialLauncher)
    }

    // ==============================================
    // Singleton Pattern Tests
    // ==============================================

    @Test
    fun getInstance_returnsSameInstance() {
        // When
        val instance1 = PermissionManager.getInstance()
        val instance2 = PermissionManager.getInstance()

        // Then
        assertSame(instance1, instance2)
    }

    // ==============================================
    // Basic Request Tests
    // ==============================================

    @Test
    fun request_withEmptyPermissions_callsCallbackImmediately() = runTest {
        // Given
        var callbackInvoked = false
        var deniedPermissions: List<String>? = null

        // When
        val requestId = permissionManager.request(
            context = context,
            requestPermissionLauncher = mockLauncher,
            permissions = emptyList(),
            callback = { denied ->
                callbackInvoked = true
                deniedPermissions = denied
            }
        )

        // Then
        assertTrue(callbackInvoked)
        assertTrue(deniedPermissions!!.isEmpty())
        assertEquals("", requestId)
        verify(mockLauncher, never()).launch(any())
    }

    @Test
    fun request_withAllGrantedPermissions_callsCallbackImmediately() = runTest {
        // Given
        Shadows.shadowOf(application).grantPermissions(Manifest.permission.CAMERA)
        var callbackInvoked = false
        var deniedPermissions: List<String>? = null

        // When
        val requestId = permissionManager.request(
            context = context,
            requestPermissionLauncher = mockLauncher,
            permissions = listOf(Manifest.permission.CAMERA),
            callback = { denied ->
                callbackInvoked = true
                deniedPermissions = denied
            }
        )

        // Then
        assertTrue(callbackInvoked)
        assertTrue(deniedPermissions!!.isEmpty())
        assertEquals("", requestId)
        verify(mockLauncher, never()).launch(any())
    }

    @Test
    fun request_withDeniedPermissions_launchesPermissionRequest() = runTest {
        // Given
        Shadows.shadowOf(application).denyPermissions(Manifest.permission.CAMERA)

        // When
        val requestId = permissionManager.request(
            context = context,
            requestPermissionLauncher = mockLauncher,
            permissions = listOf(Manifest.permission.CAMERA),
            callback = { }
        )

        // Then
        assertNotEquals("", requestId)
        verify(mockLauncher).launch(arrayOf(Manifest.permission.CAMERA))
    }

    @Test
    fun request_withMultiplePermissions_launchesOnlyDenied() = runTest {
        // Given
        Shadows.shadowOf(application).apply {
            grantPermissions(Manifest.permission.CAMERA)
            denyPermissions(Manifest.permission.RECORD_AUDIO)
        }

        // When
        permissionManager.request(
            context = context,
            requestPermissionLauncher = mockLauncher,
            permissions = listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ),
            callback = { }
        )

        // Then
        verify(mockLauncher).launch(arrayOf(Manifest.permission.RECORD_AUDIO))
    }

    // ==============================================
    // Result Handling Tests
    // ==============================================

    @Test
    fun result_withAllGranted_invokesCallbackWithEmptyList() = runTest {
        // Given
        Shadows.shadowOf(application).denyPermissions(Manifest.permission.CAMERA)
        var deniedPermissions: List<String>? = null

        val requestId = permissionManager.request(
            context = context,
            requestPermissionLauncher = mockLauncher,
            permissions = listOf(Manifest.permission.CAMERA),
            callback = { denied -> deniedPermissions = denied }
        )

        // Grant permission before result callback
        Shadows.shadowOf(application).grantPermissions(Manifest.permission.CAMERA)

        // When
        permissionManager.result(
            context = context,
            permissions = mapOf(Manifest.permission.CAMERA to true),
            requestId = requestId
        )

        // Then
        assertNotNull(deniedPermissions)
        assertTrue(deniedPermissions!!.isEmpty())
    }

    @Test
    fun result_withSomeDenied_invokesCallbackWithDeniedList() = runTest {
        // Given
        Shadows.shadowOf(application).denyPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        var deniedPermissions: List<String>? = null

        val requestId = permissionManager.request(
            context = context,
            requestPermissionLauncher = mockLauncher,
            permissions = listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ),
            callback = { denied -> deniedPermissions = denied }
        )

        // Grant only CAMERA
        Shadows.shadowOf(application).grantPermissions(Manifest.permission.CAMERA)

        // When
        permissionManager.result(
            context = context,
            permissions = mapOf(
                Manifest.permission.CAMERA to true,
                Manifest.permission.RECORD_AUDIO to false
            ),
            requestId = requestId
        )

        // Then
        assertNotNull(deniedPermissions)
        assertEquals(1, deniedPermissions!!.size)
        assertEquals(Manifest.permission.RECORD_AUDIO, deniedPermissions!!.first())
    }

    @Test
    fun result_withNullRequestId_doesNotCrash() = runTest {
        // When & Then - should not crash
        permissionManager.result(
            context = context,
            permissions = mapOf(Manifest.permission.CAMERA to true),
            requestId = null
        )
    }

    @Test
    fun result_withUnknownRequestId_doesNotCrash() = runTest {
        // When & Then - should not crash
        permissionManager.result(
            context = context,
            permissions = mapOf(Manifest.permission.CAMERA to true),
            requestId = "unknown-request-id"
        )
    }

    // ==============================================
    // Special Permission Tests
    // ==============================================

    /**
     * Note: Special permission tests require a properly initialized PermissionDelegate with
     * ActivityResultLauncher. These are challenging to test with Robolectric due to Activity
     * lifecycle timing issues. The special permission logic is tested through:
     * - PermissionExtensions tests (permission checking)
     * - Integration/Manual testing in sample app
     */

    @Test
    fun resultSpecialPermission_withNullRequestId_doesNotCrash() = runTest {
        // When & Then - should not crash
        permissionManager.resultSpecialPermission(
            context = context,
            permission = Manifest.permission.WRITE_SETTINGS,
            requestId = null
        )
    }

    @Test
    fun resultSpecialPermission_withUnknownRequestId_doesNotCrash() = runTest {
        // When & Then - should not crash
        permissionManager.resultSpecialPermission(
            context = context,
            permission = Manifest.permission.WRITE_SETTINGS,
            requestId = "unknown-request-id"
        )
    }

    // ==============================================
    // Multiple Callback Tests
    // ==============================================

    @Test
    fun addCallbackToRequest_whenRequestExists_returnsSuccess() = runTest {
        // Given
        Shadows.shadowOf(application).denyPermissions(Manifest.permission.CAMERA)
        val requestId = permissionManager.request(
            context = context,
            requestPermissionLauncher = mockLauncher,
            permissions = listOf(Manifest.permission.CAMERA),
            callback = { }
        )

        // When
        val result = permissionManager.addCallbackToRequest(
            requestId = requestId,
            callback = { },
            requestedPermissions = listOf(Manifest.permission.CAMERA)
        )

        // Then
        assertEquals(CallbackAddResult.SUCCESS, result)
    }

    @Test
    fun addCallbackToRequest_whenRequestNotFound_returnsRequestNotFound() = runTest {
        // When
        val result = permissionManager.addCallbackToRequest(
            requestId = "unknown-request-id",
            callback = { },
            requestedPermissions = listOf(Manifest.permission.CAMERA)
        )

        // Then
        assertEquals(CallbackAddResult.REQUEST_NOT_FOUND, result)
    }

    @Test
    fun addCallbackToRequest_whenPermissionMismatch_returnsPermissionMismatch() = runTest {
        // Given
        Shadows.shadowOf(application).denyPermissions(Manifest.permission.CAMERA)
        val requestId = permissionManager.request(
            context = context,
            requestPermissionLauncher = mockLauncher,
            permissions = listOf(Manifest.permission.CAMERA),
            callback = { }
        )

        // When - requesting different permission
        val result = permissionManager.addCallbackToRequest(
            requestId = requestId,
            callback = { },
            requestedPermissions = listOf(Manifest.permission.RECORD_AUDIO)
        )

        // Then
        assertEquals(CallbackAddResult.PERMISSION_MISMATCH, result)
    }

    @Test
    fun result_withMultipleCallbacks_invokesAllCallbacks() = runTest {
        // Given
        Shadows.shadowOf(application).denyPermissions(Manifest.permission.CAMERA)
        var callback1Invoked = false
        var callback2Invoked = false

        val requestId = permissionManager.request(
            context = context,
            requestPermissionLauncher = mockLauncher,
            permissions = listOf(Manifest.permission.CAMERA),
            callback = { callback1Invoked = true }
        )

        permissionManager.addCallbackToRequest(
            requestId = requestId,
            callback = { callback2Invoked = true },
            requestedPermissions = listOf(Manifest.permission.CAMERA)
        )

        Shadows.shadowOf(application).grantPermissions(Manifest.permission.CAMERA)

        // When
        permissionManager.result(
            context = context,
            permissions = mapOf(Manifest.permission.CAMERA to true),
            requestId = requestId
        )

        // Then
        assertTrue(callback1Invoked)
        assertTrue(callback2Invoked)
    }

    // ==============================================
    // Request Cancellation Tests
    // ==============================================

    @Test
    fun cancelRequest_removesRequest() = runTest {
        // Given
        Shadows.shadowOf(application).denyPermissions(Manifest.permission.CAMERA)
        val requestId = permissionManager.request(
            context = context,
            requestPermissionLauncher = mockLauncher,
            permissions = listOf(Manifest.permission.CAMERA),
            callback = { }
        )

        // When
        permissionManager.cancelRequest(requestId)

        // Then - adding callback should fail as request is removed
        val result = permissionManager.addCallbackToRequest(
            requestId = requestId,
            callback = { },
            requestedPermissions = listOf(Manifest.permission.CAMERA)
        )
        assertEquals(CallbackAddResult.REQUEST_NOT_FOUND, result)
    }

    @Test
    fun cancelRequest_withUnknownRequestId_doesNotCrash() = runTest {
        // When & Then - should not crash
        permissionManager.cancelRequest("unknown-request-id")
    }

    // ==============================================
    // Delegate Management Tests
    // ==============================================

    @Test
    fun registerDelegate_storesDelegate() = runTest {
        // Given
        val requestId = "test-request-id"

        // When
        permissionManager.registerDelegate(requestId, mockDelegate)

        // Then - should not crash when unregistering
        permissionManager.unregisterDelegate(requestId)
    }

    @Test
    fun hasActiveRequest_whenRequestExists_returnsTrue() = runTest {
        // Given
        Shadows.shadowOf(application).denyPermissions(Manifest.permission.CAMERA)
        val requestId = permissionManager.request(
            context = context,
            requestPermissionLauncher = mockLauncher,
            permissions = listOf(Manifest.permission.CAMERA),
            callback = { }
        )

        // When & Then
        assertTrue(permissionManager.hasActiveRequest(requestId))
    }

    @Test
    fun hasActiveRequest_whenRequestNotExists_returnsFalse() = runTest {
        // When & Then
        assertFalse(permissionManager.hasActiveRequest("unknown-request-id"))
    }

    @Test
    fun getRequestPermissions_whenRequestExists_returnsPermissions() = runTest {
        // Given
        Shadows.shadowOf(application).denyPermissions(Manifest.permission.CAMERA)
        val requestId = permissionManager.request(
            context = context,
            requestPermissionLauncher = mockLauncher,
            permissions = listOf(Manifest.permission.CAMERA),
            callback = { }
        )

        // When
        val permissions = permissionManager.getRequestPermissions(requestId)

        // Then
        assertNotNull(permissions)
        assertEquals(1, permissions!!.size)
        assertTrue(permissions.contains(Manifest.permission.CAMERA))
    }

    @Test
    fun getRequestPermissions_whenRequestNotExists_returnsNull() = runTest {
        // When
        val permissions = permissionManager.getRequestPermissions("unknown-request-id")

        // Then
        assertNull(permissions)
    }

    // ==============================================
    // Cleanup Tests
    // ==============================================

    @Test
    fun cleanupExpiredRequests_doesNotCrashOnEmptyRequests() = runTest {
        // When & Then - should not crash
        permissionManager.cleanupExpiredRequests()
    }

    @Test
    fun cleanupExpiredRequests_doesNotRemoveRecentRequests() = runTest {
        // Given
        Shadows.shadowOf(application).denyPermissions(Manifest.permission.CAMERA)
        val requestId = permissionManager.request(
            context = context,
            requestPermissionLauncher = mockLauncher,
            permissions = listOf(Manifest.permission.CAMERA),
            callback = { }
        )

        // When
        permissionManager.cleanupExpiredRequests()

        // Then - request should still exist
        assertTrue(permissionManager.hasActiveRequest(requestId))
    }

    // ==============================================
    // Edge Cases
    // ==============================================

    @Test
    fun request_withDuplicatePermissions_handlesCorrectly() = runTest {
        // Given
        Shadows.shadowOf(application).denyPermissions(Manifest.permission.CAMERA)

        // When
        val requestId = permissionManager.request(
            context = context,
            requestPermissionLauncher = mockLauncher,
            permissions = listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.CAMERA,
                Manifest.permission.CAMERA
            ),
            callback = { }
        )

        // Then - should only launch once with unique permissions
        assertNotEquals("", requestId)
        verify(mockLauncher).launch(any())
    }


    @Test
    fun unregisterDelegate_withUnknownRequestId_doesNotCrash() = runTest {
        // When & Then - should not crash
        permissionManager.unregisterDelegate("unknown-request-id")
    }

    // ==============================================
    // Context Destroyed Tests
    // ==============================================

    @Test
    fun request_withDestroyedActivity_returnsEmptyAndCallsCallback() = runTest {
        // Given
        val mockActivity = mock(android.app.Activity::class.java)
        `when`(mockActivity.isDestroyed).thenReturn(true)
        var deniedPermissions: List<String>? = null

        // When
        val requestId = permissionManager.request(
            context = mockActivity,
            requestPermissionLauncher = mockLauncher,
            permissions = listOf(Manifest.permission.CAMERA),
            callback = { denied -> deniedPermissions = denied }
        )

        // Then
        assertEquals("", requestId)
        assertNotNull(deniedPermissions)
        assertEquals(1, deniedPermissions!!.size)
        verify(mockLauncher, never()).launch(any())
    }

    @Test
    fun request_withFinishingActivity_returnsEmptyAndCallsCallback() = runTest {
        // Given
        val mockActivity = mock(android.app.Activity::class.java)
        `when`(mockActivity.isDestroyed).thenReturn(false)
        `when`(mockActivity.isFinishing).thenReturn(true)
        var deniedPermissions: List<String>? = null

        // When
        val requestId = permissionManager.request(
            context = mockActivity,
            requestPermissionLauncher = mockLauncher,
            permissions = listOf(Manifest.permission.CAMERA),
            callback = { denied -> deniedPermissions = denied }
        )

        // Then
        assertEquals("", requestId)
        assertNotNull(deniedPermissions)
        verify(mockLauncher, never()).launch(any())
    }

    @Test
    fun result_withDestroyedActivity_doesNotInvokeCallback() = runTest {
        // Given
        Shadows.shadowOf(application).denyPermissions(Manifest.permission.CAMERA)
        var callbackInvoked = false

        val requestId = permissionManager.request(
            context = context,
            requestPermissionLauncher = mockLauncher,
            permissions = listOf(Manifest.permission.CAMERA),
            callback = { callbackInvoked = true }
        )

        val mockActivity = mock(android.app.Activity::class.java)
        `when`(mockActivity.isDestroyed).thenReturn(true)

        // When
        permissionManager.result(
            context = mockActivity,
            permissions = mapOf(Manifest.permission.CAMERA to true),
            requestId = requestId
        )

        // Then
        assertFalse(callbackInvoked)
    }

    // ==============================================
    // Launcher Exception Tests
    // ==============================================

    @Test
    fun request_whenLauncherThrows_handlesGracefully() = runTest {
        // Given
        Shadows.shadowOf(application).denyPermissions(Manifest.permission.CAMERA)
        `when`(mockLauncher.launch(any())).thenThrow(RuntimeException("Launcher failed"))
        var deniedPermissions: List<String>? = null

        // When
        val requestId = permissionManager.request(
            context = context,
            requestPermissionLauncher = mockLauncher,
            permissions = listOf(Manifest.permission.CAMERA),
            callback = { denied -> deniedPermissions = denied }
        )

        // Then - should call callback with all permissions denied
        assertEquals("", requestId)
        assertNotNull(deniedPermissions)
        assertEquals(1, deniedPermissions!!.size)
        assertEquals(Manifest.permission.CAMERA, deniedPermissions!!.first())
    }

    // ==============================================
    // Callback Exception Tests
    // ==============================================

    @Test
    fun result_whenCallbackThrows_continuesWithOtherCallbacks() = runTest {
        // Given
        Shadows.shadowOf(application).denyPermissions(Manifest.permission.CAMERA)
        var callback2Invoked = false

        val requestId = permissionManager.request(
            context = context,
            requestPermissionLauncher = mockLauncher,
            permissions = listOf(Manifest.permission.CAMERA),
            callback = { throw RuntimeException("Callback failed") }
        )

        permissionManager.addCallbackToRequest(
            requestId = requestId,
            callback = { callback2Invoked = true },
            requestedPermissions = listOf(Manifest.permission.CAMERA)
        )

        Shadows.shadowOf(application).grantPermissions(Manifest.permission.CAMERA)

        // When
        permissionManager.result(
            context = context,
            permissions = mapOf(Manifest.permission.CAMERA to true),
            requestId = requestId
        )

        // Then - second callback should still be invoked
        assertTrue(callback2Invoked)
    }

    // ==============================================
    // Delegate Replacement Tests
    // ==============================================

    @Test
    fun registerDelegate_whenAlreadyRegistered_replacesDelegate() = runTest {
        // Given
        val requestId = "test-request-id"
        @Suppress("UNCHECKED_CAST")
        val mockDelegate2 = mock(PermissionDelegate::class.java) as PermissionDelegate<*>

        // When
        permissionManager.registerDelegate(requestId, mockDelegate)
        permissionManager.registerDelegate(requestId, mockDelegate2)

        // Then - should not crash (delegate replaced)
        permissionManager.unregisterDelegate(requestId)
    }

    // ==============================================
    // PreGenerated RequestId Tests
    // ==============================================

    @Test
    fun request_withPreGeneratedRequestId_usesProvidedId() = runTest {
        // Given
        Shadows.shadowOf(application).denyPermissions(Manifest.permission.CAMERA)
        val preGeneratedId = "my-custom-request-id"

        // When
        val requestId = permissionManager.request(
            context = context,
            requestPermissionLauncher = mockLauncher,
            permissions = listOf(Manifest.permission.CAMERA),
            callback = { },
            preGeneratedRequestId = preGeneratedId
        )

        // Then
        assertEquals(preGeneratedId, requestId)
        assertTrue(permissionManager.hasActiveRequest(preGeneratedId))
    }

    // ==============================================
    // Special Permission Intent Helpers
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createSpecialPermissionIntent_returnsNullWhenApiLevelTooLow() {
        val method = PermissionManager::class.java.getDeclaredMethod(
            "createSpecialPermissionIntent",
            Context::class.java,
            String::class.java
        ).apply { isAccessible = true }

        val result = method.invoke(
            permissionManager,
            context,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
        )

        assertNull(result)
    }

    @Test
    fun createSpecialPermissionIntent_returnsPackageUriIntentWhenRequired() {
        val method = PermissionManager::class.java.getDeclaredMethod(
            "createSpecialPermissionIntent",
            Context::class.java,
            String::class.java
        ).apply { isAccessible = true }

        val permission = Manifest.permission.REQUEST_INSTALL_PACKAGES
        val result = method.invoke(permissionManager, context, permission) as Intent

        assertEquals("package:${context.packageName}", result.dataString)
    }
}
