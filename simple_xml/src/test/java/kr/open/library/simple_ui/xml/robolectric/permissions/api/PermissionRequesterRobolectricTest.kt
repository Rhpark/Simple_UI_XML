package kr.open.library.simple_ui.xml.robolectric.permissions.api

import android.app.Application
import android.content.pm.PermissionInfo
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kr.open.library.simple_ui.core.permissions.model.PermissionDecisionType
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedItem
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedType
import kr.open.library.simple_ui.xml.permissions.api.PermissionRequester
import kr.open.library.simple_ui.xml.permissions.state.PermissionStateStore
import kr.open.library.simple_ui.xml.permissions.state.RequestState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

/**
 * Robolectric tests for PermissionRequester behavior.<br><br>
 * PermissionRequester 동작을 검증하는 Robolectric 테스트입니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
class PermissionRequesterRobolectricTest {
    /**
     * Simple fragment used to verify pre-attach requester creation.<br><br>
     * attach 이전 요청기 생성을 검증하는 단순 Fragment입니다.<br>
     */
    class TestFragment : Fragment()

    /**
     * Verifies that an empty request returns EMPTY_REQUEST with empty permission.<br><br>
     * 빈 요청이 EMPTY_REQUEST와 빈 권한으로 반환되는지 검증합니다.<br>
     */
    @Test
    fun emptyRequestShouldReturnEmptyRequest() {
        val activityController = Robolectric.buildActivity(ComponentActivity::class.java).create()
        val activity = activityController.get()
        val requester = PermissionRequester(activity)

        var results: List<PermissionDeniedItem>? = null

        requester.requestPermissions(
            permissions = emptyList(),
            onDeniedResult = { deniedItems -> results = deniedItems },
        )

        assertNotNull(results)
        assertEquals(1, results?.size)
        assertEquals("", results?.first()?.permission)
        assertEquals(PermissionDeniedType.EMPTY_REQUEST, results?.first()?.result)
    }

    /**
     * Verifies that a fragment-bound requester can be created before attachment.<br><br>
     * Fragment attach 이전에도 fragment 바인딩 요청기를 생성할 수 있는지 검증합니다.<br>
     */
    @Test
    fun fragmentRequester_canBeCreatedBeforeAttachment() {
        val fragment = TestFragment()

        val requester = PermissionRequester(fragment)

        assertNotNull(requester)
    }

    /**
     * Verifies that restored requests are processed after the fragment becomes lifecycle-ready.<br><br>
     * 복원된 요청이 Fragment lifecycle 준비 후 자동으로 처리되는지 검증합니다.<br>
     */
    @Test
    fun restoreState_beforeFragmentAttached_processesRestoredRequestAfterAttach() {
        val fragment = TestFragment()
        val requester = PermissionRequester(fragment)
        val requestId = "restored-pending-request"
        val permission = "android.permission.CAMERA"
        val savedState = createSavedState(
            requestId = requestId,
            permissions = listOf(permission),
        )

        requester.restoreState(savedState)

        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        val activity = activityController.get()
        activity.supportFragmentManager
            .beginTransaction()
            .add(fragment, "permission-fragment")
            .commitNow()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        val orphanedResults = requester.consumeOrphanedDeniedResults()
        assertEquals(1, orphanedResults.size)
        assertEquals(requestId, orphanedResults.first().requestId)
        assertEquals(permission, orphanedResults
            .first()
            .deniedResults
            .first()
            .permission)
    }

    /**
     * Verifies that fully granted restored requests do not become orphaned denied results.<br><br>
     * 전부 승인된 복원 요청이 orphaned denied result로 저장되지 않는지 검증합니다.<br>
     */
    @Test
    fun restoreState_withGrantedCompletedRequest_doesNotStoreOrphanedDeniedResult() {
        val activityController = Robolectric.buildActivity(ComponentActivity::class.java).create()
        val activity = activityController.get()
        val requester = PermissionRequester(activity)
        val requestId = "restored-granted-request"
        val permission = "android.permission.CAMERA"
        val savedState = createSavedState(
            requestId = requestId,
            permissions = listOf(permission),
            results = mapOf(permission to PermissionDecisionType.GRANTED),
        )

        requester.restoreState(savedState)
        activityController.start().resume()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        assertTrue(requester.consumeOrphanedDeniedResults().isEmpty())
    }

    /**
     * Verifies that restored normal permissions are completed as granted by default without queueing.<br><br>
     * 복원된 normal 권한이 기본 승인으로 처리되고 요청 큐에 남지 않는지 검증합니다.<br>
     */
    @Test
    fun restoreState_withNormalPermission_completesAsGrantedByDefault() {
        val permission = "com.test.NORMAL_PERMISSION"
        registerPermission(permission, PermissionInfo.PROTECTION_NORMAL)

        val activityController = Robolectric.buildActivity(ComponentActivity::class.java).create()
        val activity = activityController.get()
        val requester = PermissionRequester(activity)
        val savedState = createSavedState(
            requestId = "restored-normal-request",
            permissions = listOf(permission),
        )

        requester.restoreState(savedState)
        activityController.start().resume()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        assertTrue(requester.consumeOrphanedDeniedResults().isEmpty())

        val outState = Bundle()
        requester.saveState(outState)
        val restoredStore = PermissionStateStore()
        restoredStore.restoreState(outState)
        assertTrue(restoredStore.getSnapshot().requestQueue.isEmpty())
        assertTrue(restoredStore.getSnapshot().requestStates.isEmpty())
    }

    /**
     * Verifies that restored signature-style permissions are marked as NOT_SUPPORTED.<br><br>
     * 복원된 signature 계열 권한이 NOT_SUPPORTED로 처리되는지 검증합니다.<br>
     */
    @Test
    fun restoreState_withSignaturePermission_storesNotSupportedDeniedResult() {
        val permission = "com.test.SIGNATURE_PERMISSION"
        registerPermission(permission, PermissionInfo.PROTECTION_SIGNATURE)

        val activityController = Robolectric.buildActivity(ComponentActivity::class.java).create()
        val activity = activityController.get()
        val requester = PermissionRequester(activity)
        val savedState = createSavedState(
            requestId = "restored-signature-request",
            permissions = listOf(permission),
        )

        requester.restoreState(savedState)
        activityController.start().resume()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        val orphanedResults = requester.consumeOrphanedDeniedResults()
        assertEquals(1, orphanedResults.size)
        assertEquals(permission, orphanedResults
            .first()
            .deniedResults
            .first()
            .permission)
        assertEquals(PermissionDeniedType.NOT_SUPPORTED, orphanedResults
            .first()
            .deniedResults
            .first()
            .result)

        val outState = Bundle()
        requester.saveState(outState)
        val restoredStore = PermissionStateStore()
        restoredStore.restoreState(outState)
        assertTrue(restoredStore.getSnapshot().requestQueue.isEmpty())
        assertTrue(restoredStore.getSnapshot().requestStates.isEmpty())
    }

    /**
     * Creates a saved state bundle containing a single permission request entry.<br><br>
     * 단일 권한 요청 엔트리를 포함한 saved state 번들을 생성합니다.<br>
     */
    private fun createSavedState(
        requestId: String,
        permissions: List<String>,
        results: Map<String, PermissionDecisionType> = emptyMap(),
    ): Bundle {
        val stateStore = PermissionStateStore()
        stateStore.update { snapshot ->
            snapshot.requestQueue.add(requestId)
            snapshot.requestStates[requestId] = RequestState(
                requestId = requestId,
                permissions = permissions,
                results = results,
            )
        }
        return Bundle().also { outState ->
            stateStore.saveState(outState)
        }
    }

    /**
     * Registers a synthetic permission in Robolectric PackageManager.<br><br>
     * Robolectric PackageManager에 테스트용 권한을 등록합니다.<br>
     */
    private fun registerPermission(
        permission: String,
        protectionLevel: Int,
    ) {
        val applicationContext = androidx.test.core.app.ApplicationProvider
            .getApplicationContext<Application>()
        val shadowPackageManager = Shadows.shadowOf(applicationContext.packageManager)
        val protectionField =
            PermissionInfo::class.java.getDeclaredField("protectionLevel").apply {
                isAccessible = true
            }

        val permissionInfo =
            PermissionInfo().apply {
                name = permission
                packageName = applicationContext.packageName
            }
        protectionField.setInt(permissionInfo, protectionLevel)
        shadowPackageManager.addPermissionInfo(permissionInfo)
    }
}
