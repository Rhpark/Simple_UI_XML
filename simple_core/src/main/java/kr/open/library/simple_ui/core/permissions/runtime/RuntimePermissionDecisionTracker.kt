package kr.open.library.simple_ui.core.permissions.runtime

import kr.open.library.simple_ui.core.permissions.model.PermissionDecisionType

/**
 * Tracks runtime permission request history and maps dialog results to decisions.<br><br>
 * 런타임 권한 요청 이력을 추적하고 다이얼로그 결과를 결정 타입으로 매핑합니다.<br>
 *
 * UI 비의존 순수 로직 단일 출처입니다. simple_xml / simple_compose가 공유합니다.<br>
 * Single source of UI-independent pure logic shared by simple_xml / simple_compose.<br>
 *
 * @param requestedHistory Mutable set tracking previously requested permissions (owned by the caller for save/restore).<br><br>
 *                         이전 요청 이력을 추적하는 가변 집합입니다(저장/복원을 위해 호출부가 소유).<br>
 */
class RuntimePermissionDecisionTracker(
    private val requestedHistory: MutableSet<String>,
) {
    /**
     * Returns whether [permission] was requested before.<br><br>
     * [permission]이 이전에 요청되었는지 반환합니다.<br>
     */
    fun wasRequested(permission: String): Boolean = permission in requestedHistory

    /**
     * Marks [permissions] as requested in the history set.<br><br>
     * [permissions]를 요청 이력에 기록합니다.<br>
     */
    fun markRequested(permissions: List<String>) {
        requestedHistory.addAll(permissions)
    }

    /**
     * Maps a runtime permission dialog result into a [PermissionDecisionType].<br><br>
     * 런타임 권한 다이얼로그 결과를 [PermissionDecisionType]으로 매핑합니다.<br>
     *
     * Rule (SPEC): granted → GRANTED; else rationale → DENIED; else requested-before → PERMANENTLY_DENIED; else DENIED.<br>
     * [isRestored] downgrades PERMANENTLY_DENIED to DENIED because the request-history snapshot is unreliable after process restore.<br>
     * 규칙(SPEC): 승인 → GRANTED; 아니면 rationale → DENIED; 아니면 과거 요청 이력 → PERMANENTLY_DENIED; 그 외 DENIED.<br>
     * [isRestored]는 복원 후 요청 이력 스냅샷을 신뢰할 수 없어 PERMANENTLY_DENIED를 DENIED로 강등합니다.<br>
     *
     * @param permission Permission string being evaluated.<br><br>평가 중인 권한 문자열입니다.<br>
     * @param granted Whether the platform returned granted = true.<br><br>플랫폼이 granted = true로 반환했는지 여부입니다.<br>
     * @param shouldShowRationale Whether rationale UI should be shown now.<br><br>현재 설명 UI가 필요한지 여부입니다.<br>
     * @param wasRequestedBefore Whether the permission was requested before.<br><br>이전 요청 이력이 있는지 여부입니다.<br>
     * @param isRestored Whether this result belongs to a restored session.<br><br>복원된 세션의 결과인지 여부입니다.<br>
     * @return Mapped permission decision type.<br><br>매핑된 권한 결정 타입입니다.<br>
     */
    @Suppress("UNUSED_PARAMETER") // permission kept for signature/logging parity across callers
    fun mapResult(
        permission: String,
        granted: Boolean,
        shouldShowRationale: Boolean,
        wasRequestedBefore: Boolean,
        isRestored: Boolean = false,
    ): PermissionDecisionType = when {
        granted -> PermissionDecisionType.GRANTED
        shouldShowRationale -> PermissionDecisionType.DENIED
        wasRequestedBefore && !isRestored -> PermissionDecisionType.PERMANENTLY_DENIED
        else -> PermissionDecisionType.DENIED
    }
}
