package kr.open.library.simple_ui.core.unit.permissions.model

import kr.open.library.simple_ui.core.permissions.model.OrphanedDeniedRequestResult
import kr.open.library.simple_ui.core.permissions.model.PermissionDecisionType
import kr.open.library.simple_ui.core.permissions.model.PermissionDeferredPolicy
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedItem
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedType
import kr.open.library.simple_ui.core.permissions.model.PermissionRationaleRequest
import kr.open.library.simple_ui.core.permissions.model.PermissionSettingsRequest
import kr.open.library.simple_ui.core.permissions.model.buildPermissionDeniedItems
import kr.open.library.simple_ui.core.permissions.model.toDeniedTypeOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for caller-facing permission result conversion.<br><br>
 * 호출부에 전달되는 권한 결과 변환을 검증하는 단위 테스트입니다.<br>
 */
class PermissionModelsTest {
    /**
     * Verifies every denied decision maps to the matching caller-facing denied type.<br><br>
     * 모든 거부 결정이 동일한 의미의 호출부용 거부 타입으로 변환되는지 검증합니다.<br>
     */
    @Test
    fun toDeniedTypeOrNullShouldMapEveryDeniedDecision() {
        val expectedMappings = mapOf(
            PermissionDecisionType.DENIED to PermissionDeniedType.DENIED,
            PermissionDecisionType.PERMANENTLY_DENIED to PermissionDeniedType.PERMANENTLY_DENIED,
            PermissionDecisionType.MANIFEST_UNDECLARED to PermissionDeniedType.MANIFEST_UNDECLARED,
            PermissionDecisionType.EMPTY_REQUEST to PermissionDeniedType.EMPTY_REQUEST,
            PermissionDecisionType.NOT_SUPPORTED to PermissionDeniedType.NOT_SUPPORTED,
            PermissionDecisionType.FAILED_TO_LAUNCH_SETTINGS to PermissionDeniedType.FAILED_TO_LAUNCH_SETTINGS,
            PermissionDecisionType.LIFECYCLE_NOT_READY to PermissionDeniedType.LIFECYCLE_NOT_READY,
        )

        expectedMappings.forEach { (decision, expected) ->
            assertEquals(expected, decision.toDeniedTypeOrNull())
        }
    }

    /**
     * Verifies that GRANTED is excluded from caller-facing denied results.<br><br>
     * GRANTED 결정이 호출부용 거부 결과에서 제외되는지 검증합니다.<br>
     */
    @Test
    fun toDeniedTypeOrNullShouldReturnNullForGrantedDecision() {
        assertNull(PermissionDecisionType.GRANTED.toDeniedTypeOrNull())
    }

    /**
     * Verifies that denied items preserve caller request order while granted permissions are omitted.<br><br>
     * 승인 권한을 제외하면서 거부 항목이 호출부 요청 순서를 유지하는지 검증합니다.<br>
     */
    @Test
    fun buildPermissionDeniedItemsShouldPreserveOrderAndExcludeGrantedPermissions() {
        val permissions = listOf(CAMERA_PERMISSION, MICROPHONE_PERMISSION, LOCATION_PERMISSION)
        val results = mapOf(
            LOCATION_PERMISSION to PermissionDecisionType.NOT_SUPPORTED,
            CAMERA_PERMISSION to PermissionDecisionType.DENIED,
            MICROPHONE_PERMISSION to PermissionDecisionType.GRANTED,
        )

        val deniedItems = buildPermissionDeniedItems(permissions, results)

        assertEquals(
            listOf(
                PermissionDeniedItem(CAMERA_PERMISSION, PermissionDeniedType.DENIED),
                PermissionDeniedItem(LOCATION_PERMISSION, PermissionDeniedType.NOT_SUPPORTED),
            ),
            deniedItems,
        )
    }

    /**
     * Verifies that a missing decision is reported as MANIFEST_UNDECLARED instead of being treated as granted.<br><br>
     * 결정이 누락된 권한을 승인으로 오인하지 않고 MANIFEST_UNDECLARED로 보고하는지 검증합니다.<br>
     */
    @Test
    fun buildPermissionDeniedItemsShouldTreatMissingDecisionAsManifestUndeclared() {
        val deniedItems = buildPermissionDeniedItems(
            permissions = listOf(CAMERA_PERMISSION),
            results = emptyMap(),
        )

        assertEquals(
            listOf(PermissionDeniedItem(CAMERA_PERMISSION, PermissionDeniedType.MANIFEST_UNDECLARED)),
            deniedItems,
        )
    }

    /**
     * Verifies that duplicate request entries remain duplicated and ordered in the caller-facing result.<br><br>
     * 중복 요청 항목이 호출부 결과에서도 요청 순서대로 유지되는지 검증합니다.<br>
     */
    @Test
    fun buildPermissionDeniedItemsShouldPreserveDuplicateRequestEntries() {
        val deniedItems = buildPermissionDeniedItems(
            permissions = listOf(CAMERA_PERMISSION, CAMERA_PERMISSION),
            results = mapOf(CAMERA_PERMISSION to PermissionDecisionType.DENIED),
        )

        assertEquals(
            listOf(
                PermissionDeniedItem(CAMERA_PERMISSION, PermissionDeniedType.DENIED),
                PermissionDeniedItem(CAMERA_PERMISSION, PermissionDeniedType.DENIED),
            ),
            deniedItems,
        )
    }

    /**
     * Verifies that empty requests and all-granted requests produce an empty denied list.<br><br>
     * 빈 요청과 모두 승인된 요청이 빈 거부 목록을 반환하는지 검증합니다.<br>
     */
    @Test
    fun buildPermissionDeniedItemsShouldReturnEmptyListWhenNothingIsDenied() {
        val emptyRequest = buildPermissionDeniedItems(emptyList(), emptyMap())
        val allGranted = buildPermissionDeniedItems(
            permissions = listOf(CAMERA_PERMISSION, MICROPHONE_PERMISSION),
            results = mapOf(
                CAMERA_PERMISSION to PermissionDecisionType.GRANTED,
                MICROPHONE_PERMISSION to PermissionDecisionType.GRANTED,
            ),
        )

        assertTrue(emptyRequest.isEmpty())
        assertTrue(allGranted.isEmpty())
    }

    /**
     * Verifies that an orphaned result preserves its request identifier and denied results.<br><br>
     * 콜백을 잃은 복원 결과가 요청 식별자와 거부 결과를 그대로 보존하는지 검증합니다.<br>
     */
    @Test
    fun orphanedDeniedRequestResultShouldPreserveRequestData() {
        val deniedResults = listOf(
            PermissionDeniedItem(CAMERA_PERMISSION, PermissionDeniedType.PERMANENTLY_DENIED),
        )

        val result = OrphanedDeniedRequestResult(REQUEST_ID, deniedResults)

        assertEquals(REQUEST_ID, result.requestId)
        assertEquals(deniedResults, result.deniedResults)
    }

    /**
     * Verifies that every documented deferred lifecycle policy remains available.<br><br>
     * 문서화된 defer 라이프사이클 정책값이 모두 유지되는지 검증합니다.<br>
     */
    @Test
    fun permissionDeferredPolicyShouldExposeBothLifecyclePolicies() {
        assertEquals(
            listOf(
                PermissionDeferredPolicy.CANCEL_ON_STOP,
                PermissionDeferredPolicy.CANCEL_ON_DESTROY,
            ),
            enumValues<PermissionDeferredPolicy>().toList(),
        )
    }

    /**
     * Verifies that rationale defer uses CANCEL_ON_STOP when the policy argument is omitted.<br><br>
     * 설명 요청의 defer 정책을 생략하면 CANCEL_ON_STOP을 사용하는지 검증합니다.<br>
     */
    @Test
    fun rationaleRequestDeferShouldUseCancelOnStopByDefault() {
        val recorder = RationaleRequestRecorder()
        val request: PermissionRationaleRequest = recorder

        request.defer()

        assertEquals(PermissionDeferredPolicy.CANCEL_ON_STOP, recorder.deferredPolicy)
    }

    /**
     * Verifies that settings defer uses CANCEL_ON_STOP when the policy argument is omitted.<br><br>
     * 설정 요청의 defer 정책을 생략하면 CANCEL_ON_STOP을 사용하는지 검증합니다.<br>
     */
    @Test
    fun settingsRequestDeferShouldUseCancelOnStopByDefault() {
        val recorder = SettingsRequestRecorder()
        val request: PermissionSettingsRequest = recorder

        request.defer()

        assertEquals(PermissionDeferredPolicy.CANCEL_ON_STOP, recorder.deferredPolicy)
    }

    /**
     * Verifies that both request contracts preserve an explicitly supplied deferred policy.<br><br>
     * 두 요청 계약이 명시적으로 전달된 defer 정책을 그대로 보존하는지 검증합니다.<br>
     */
    @Test
    fun requestDeferShouldPreserveExplicitPolicy() {
        val rationaleRecorder = RationaleRequestRecorder()
        val settingsRecorder = SettingsRequestRecorder()

        rationaleRecorder.defer(PermissionDeferredPolicy.CANCEL_ON_DESTROY)
        settingsRecorder.defer(PermissionDeferredPolicy.CANCEL_ON_DESTROY)

        assertEquals(PermissionDeferredPolicy.CANCEL_ON_DESTROY, rationaleRecorder.deferredPolicy)
        assertEquals(PermissionDeferredPolicy.CANCEL_ON_DESTROY, settingsRecorder.deferredPolicy)
    }

    private class RationaleRequestRecorder : PermissionRationaleRequest {
        override val permissions: List<String> = listOf(CAMERA_PERMISSION)
        var deferredPolicy: PermissionDeferredPolicy? = null

        override fun proceed() = Unit

        override fun cancel() = Unit

        override fun defer(policy: PermissionDeferredPolicy) {
            deferredPolicy = policy
        }
    }

    private class SettingsRequestRecorder : PermissionSettingsRequest {
        override val permission: String = CAMERA_PERMISSION
        var deferredPolicy: PermissionDeferredPolicy? = null

        override fun proceed() = Unit

        override fun cancel() = Unit

        override fun defer(policy: PermissionDeferredPolicy) {
            deferredPolicy = policy
        }
    }

    private companion object {
        const val REQUEST_ID = "request-1"
        const val CAMERA_PERMISSION = "android.permission.CAMERA"
        const val MICROPHONE_PERMISSION = "android.permission.RECORD_AUDIO"
        const val LOCATION_PERMISSION = "android.permission.ACCESS_FINE_LOCATION"
    }
}
