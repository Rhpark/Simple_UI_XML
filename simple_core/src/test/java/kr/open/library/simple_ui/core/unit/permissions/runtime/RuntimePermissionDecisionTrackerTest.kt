package kr.open.library.simple_ui.core.unit.permissions.runtime

import kr.open.library.simple_ui.core.permissions.model.PermissionDecisionType
import kr.open.library.simple_ui.core.permissions.runtime.RuntimePermissionDecisionTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for runtime permission request history and decision mapping.<br><br>
 * 런타임 권한 요청 이력과 결정 매핑을 검증하는 단위 테스트입니다.<br>
 */
class RuntimePermissionDecisionTrackerTest {
    /**
     * Verifies that existing history is visible and newly requested permissions are added to the caller-owned set.<br><br>
     * 기존 요청 이력을 조회하고 새 권한을 호출부 소유 집합에 추가하는지 검증합니다.<br>
     */
    @Test
    fun requestHistoryShouldReadAndUpdateCallerOwnedSet() {
        val requestedHistory = mutableSetOf(CAMERA_PERMISSION)
        val tracker = RuntimePermissionDecisionTracker(requestedHistory)

        assertTrue(tracker.wasRequested(CAMERA_PERMISSION))
        assertFalse(tracker.wasRequested(MICROPHONE_PERMISSION))

        tracker.markRequested(listOf(MICROPHONE_PERMISSION, CAMERA_PERMISSION))

        assertTrue(tracker.wasRequested(MICROPHONE_PERMISSION))
        assertEquals(setOf(CAMERA_PERMISSION, MICROPHONE_PERMISSION), requestedHistory)
    }

    /**
     * Verifies that a granted platform result takes precedence over every denied-state signal.<br><br>
     * 플랫폼 승인 결과가 모든 거부 상태 신호보다 우선하는지 검증합니다.<br>
     */
    @Test
    fun mapResultShouldReturnGrantedWhenPlatformGrantsPermission() {
        val tracker = RuntimePermissionDecisionTracker(mutableSetOf())

        val result = tracker.mapResult(
            permission = CAMERA_PERMISSION,
            granted = true,
            shouldShowRationale = true,
            wasRequestedBefore = true,
            isRestored = true,
        )

        assertEquals(PermissionDecisionType.GRANTED, result)
    }

    /**
     * Verifies that a rationale signal maps a denied result to DENIED even when request history exists.<br><br>
     * 요청 이력이 있어도 설명 UI 신호가 있으면 거부 결과를 DENIED로 매핑하는지 검증합니다.<br>
     */
    @Test
    fun mapResultShouldReturnDeniedWhenRationaleIsRequired() {
        val tracker = RuntimePermissionDecisionTracker(mutableSetOf())

        val result = tracker.mapResult(
            permission = CAMERA_PERMISSION,
            granted = false,
            shouldShowRationale = true,
            wasRequestedBefore = true,
        )

        assertEquals(PermissionDecisionType.DENIED, result)
    }

    /**
     * Verifies that a previously requested denial without rationale maps to PERMANENTLY_DENIED.<br><br>
     * 이전 요청 이력이 있고 설명 UI가 필요하지 않은 거부를 PERMANENTLY_DENIED로 매핑하는지 검증합니다.<br>
     */
    @Test
    fun mapResultShouldReturnPermanentlyDeniedForRepeatedDenialWithoutRationale() {
        val tracker = RuntimePermissionDecisionTracker(mutableSetOf())

        val result = tracker.mapResult(
            permission = CAMERA_PERMISSION,
            granted = false,
            shouldShowRationale = false,
            wasRequestedBefore = true,
        )

        assertEquals(PermissionDecisionType.PERMANENTLY_DENIED, result)
    }

    /**
     * Verifies that a first denial without rationale is not classified as permanent denial.<br><br>
     * 첫 요청의 설명 UI 없는 거부를 영구 거부로 오판하지 않는지 검증합니다.<br>
     */
    @Test
    fun mapResultShouldReturnDeniedForFirstDenialWithoutRationale() {
        val tracker = RuntimePermissionDecisionTracker(mutableSetOf())

        val result = tracker.mapResult(
            permission = CAMERA_PERMISSION,
            granted = false,
            shouldShowRationale = false,
            wasRequestedBefore = false,
        )

        assertEquals(PermissionDecisionType.DENIED, result)
    }

    /**
     * Verifies that restored request history cannot produce a permanent-denial decision.<br><br>
     * 복원된 요청 이력으로 영구 거부 결정을 만들지 않고 DENIED로 강등하는지 검증합니다.<br>
     */
    @Test
    fun mapResultShouldDowngradePermanentDenialForRestoredSession() {
        val tracker = RuntimePermissionDecisionTracker(mutableSetOf())

        val result = tracker.mapResult(
            permission = CAMERA_PERMISSION,
            granted = false,
            shouldShowRationale = false,
            wasRequestedBefore = true,
            isRestored = true,
        )

        assertEquals(PermissionDecisionType.DENIED, result)
    }

    private companion object {
        const val CAMERA_PERMISSION = "android.permission.CAMERA"
        const val MICROPHONE_PERMISSION = "android.permission.RECORD_AUDIO"
    }
}
