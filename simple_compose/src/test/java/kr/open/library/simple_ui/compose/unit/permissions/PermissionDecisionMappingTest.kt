package kr.open.library.simple_ui.compose.unit.permissions

import kr.open.library.simple_ui.compose.permissions.EMPTY_REQUEST_PERMISSION
import kr.open.library.simple_ui.compose.permissions.toDeniedItems
import kr.open.library.simple_ui.core.permissions.model.PermissionDecisionType
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedItem
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedType
import kr.open.library.simple_ui.core.permissions.model.toDeniedTypeOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 권한 결정 매핑 순수 함수 단위 테스트입니다 — core 단일 출처 변환([toDeniedTypeOrNull])과
 * compose의 거부 목록 생성([toDeniedItems])을 검증합니다.<br>
 * Unit tests for the permission decision mapping pure functions — the single-source conversion
 * in core ([toDeniedTypeOrNull]) and the denied list building in compose ([toDeniedItems]).<br>
 */
class PermissionDecisionMappingTest {
    // -----------------------------------------------------------------------
    // toDeniedTypeOrNull — 결정 상태 → 거부 타입 변환 (simple_core 단일 출처)
    // toDeniedTypeOrNull — decision to denied type conversion (single source in simple_core)
    // -----------------------------------------------------------------------

    @Test
    fun `GRANTED maps to null because it is not a denied entry`() {
        assertNull(PermissionDecisionType.GRANTED.toDeniedTypeOrNull())
    }

    @Test
    fun `DENIED maps to DENIED`() {
        assertEquals(PermissionDeniedType.DENIED, PermissionDecisionType.DENIED.toDeniedTypeOrNull())
    }

    @Test
    fun `PERMANENTLY_DENIED maps to PERMANENTLY_DENIED`() {
        assertEquals(
            PermissionDeniedType.PERMANENTLY_DENIED,
            PermissionDecisionType.PERMANENTLY_DENIED.toDeniedTypeOrNull(),
        )
    }

    @Test
    fun `MANIFEST_UNDECLARED maps to MANIFEST_UNDECLARED`() {
        assertEquals(
            PermissionDeniedType.MANIFEST_UNDECLARED,
            PermissionDecisionType.MANIFEST_UNDECLARED.toDeniedTypeOrNull(),
        )
    }

    @Test
    fun `EMPTY_REQUEST maps to EMPTY_REQUEST`() {
        assertEquals(
            PermissionDeniedType.EMPTY_REQUEST,
            PermissionDecisionType.EMPTY_REQUEST.toDeniedTypeOrNull(),
        )
    }

    @Test
    fun `NOT_SUPPORTED maps to NOT_SUPPORTED`() {
        assertEquals(
            PermissionDeniedType.NOT_SUPPORTED,
            PermissionDecisionType.NOT_SUPPORTED.toDeniedTypeOrNull(),
        )
    }

    @Test
    fun `FAILED_TO_LAUNCH_SETTINGS maps to FAILED_TO_LAUNCH_SETTINGS`() {
        assertEquals(
            PermissionDeniedType.FAILED_TO_LAUNCH_SETTINGS,
            PermissionDecisionType.FAILED_TO_LAUNCH_SETTINGS.toDeniedTypeOrNull(),
        )
    }

    @Test
    fun `LIFECYCLE_NOT_READY maps to LIFECYCLE_NOT_READY`() {
        assertEquals(
            PermissionDeniedType.LIFECYCLE_NOT_READY,
            PermissionDecisionType.LIFECYCLE_NOT_READY.toDeniedTypeOrNull(),
        )
    }

    // -----------------------------------------------------------------------
    // toDeniedItems — 거부 목록 생성
    // toDeniedItems — denied list building
    // -----------------------------------------------------------------------

    @Test
    fun `all granted results produce empty denied list`() {
        val permissions = listOf("android.permission.CAMERA", "android.permission.RECORD_AUDIO")
        val results = permissions.associateWith { PermissionDecisionType.GRANTED }

        assertTrue(toDeniedItems(permissions, results).isEmpty())
    }

    @Test
    fun `denied list preserves the request order`() {
        val permissions = listOf("a.permission.FIRST", "a.permission.SECOND", "a.permission.THIRD")
        // 결과 맵의 삽입 순서를 요청 순서와 다르게 구성
        // Insertion order of the result map intentionally differs from the request order
        val results = linkedMapOf(
            "a.permission.THIRD" to PermissionDecisionType.DENIED,
            "a.permission.FIRST" to PermissionDecisionType.PERMANENTLY_DENIED,
            "a.permission.SECOND" to PermissionDecisionType.GRANTED,
        )

        val denied = toDeniedItems(permissions, results)

        assertEquals(
            listOf(
                PermissionDeniedItem("a.permission.FIRST", PermissionDeniedType.PERMANENTLY_DENIED),
                PermissionDeniedItem("a.permission.THIRD", PermissionDeniedType.DENIED),
            ),
            denied,
        )
    }

    @Test
    fun `permissions missing from the result map default to MANIFEST_UNDECLARED`() {
        // xml(PermissionResultAggregator)과 동일 기본값 — 무음 스킵 시 승인으로 오인됨
        // Same default as xml's PermissionResultAggregator — silent skipping reads as granted
        val permissions = listOf("a.permission.KNOWN", "a.permission.UNKNOWN")
        val results = mapOf("a.permission.KNOWN" to PermissionDecisionType.DENIED)

        val denied = toDeniedItems(permissions, results)

        assertEquals(
            listOf(
                PermissionDeniedItem("a.permission.KNOWN", PermissionDeniedType.DENIED),
                PermissionDeniedItem("a.permission.UNKNOWN", PermissionDeniedType.MANIFEST_UNDECLARED),
            ),
            denied,
        )
    }

    @Test
    fun `empty request permission constant is an empty string`() {
        assertEquals("", EMPTY_REQUEST_PERMISSION)
    }
}
