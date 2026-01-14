package kr.open.library.simple_ui.xml.permissions.flow

import kr.open.library.simple_ui.core.permissions.model.PermissionDecisionType
import kr.open.library.simple_ui.xml.permissions.host.PermissionHostAdapter

/**
 * Handles runtime permission rationale checks and result mapping.<br><br>
 * 런타임 권한 설명 여부 확인과 결과 매핑을 처리합니다.<br>
 *
 * @param host Host adapter used for rationale checks.<br><br>
 *             설명 여부 확인에 사용하는 호스트 어댑터입니다.<br>
 * @param requestedHistory Mutable set tracking previously requested permissions.<br><br>
 *                         이전 요청 이력을 추적하는 가변 집합입니다.<br>
 */
class RuntimePermissionHandler(
    private val host: PermissionHostAdapter,
    private val requestedHistory: MutableSet<String>,
) {
    /**
     * Returns whether rationale UI should be shown for [permission].<br><br>
     * [permission]에 대해 설명 UI가 필요한지 반환합니다.<br>
     *
     * @param permission Permission string to inspect.<br><br>
     *                  확인할 권한 문자열입니다.<br>
     * @return Return value: true when rationale should be shown. Log behavior: none.<br><br>
     *         반환값: 설명이 필요하면 true. 로그 동작: 없음.<br>
     */
    fun shouldShowRationale(permission: String): Boolean =
        host.shouldShowRequestPermissionRationale(permission)

    /**
     * Returns whether [permission] was requested before.<br><br>
     * [permission]이 이전에 요청되었는지 반환합니다.<br>
     *
     * @param permission Permission string to inspect.<br><br>
     *                  확인할 권한 문자열입니다.<br>
     * @return Return value: true when requested before. Log behavior: none.<br><br>
     *         반환값: 이전 요청 이력이 있으면 true. 로그 동작: 없음.<br>
     */
    fun wasRequested(permission: String): Boolean = requestedHistory.contains(permission)

    /**
     * Marks [permissions] as requested in the history set.<br><br>
     * [permissions]를 요청 이력에 기록합니다.<br>
     *
     * @param permissions Permissions to mark as requested.<br><br>
     *                    요청 이력에 기록할 권한 목록입니다.<br>
     */
    fun markRequested(permissions: List<String>) {
        requestedHistory.addAll(permissions)
    }

    /**
     * Maps runtime permission decision into [PermissionDecisionType].<br><br>
     * 런타임 권한 결과를 [PermissionDecisionType]으로 매핑합니다.<br>
     *
     * @param permission Permission string being evaluated.<br><br>
     *                  평가 중인 권한 문자열입니다.<br>
     * @param granted Whether the platform returned granted = true.<br><br>
     *                플랫폼이 granted = true로 반환했는지 여부입니다.<br>
     * @param shouldShowRationale Whether rationale UI should be shown now.<br><br>
     *                            현재 설명 UI가 필요한지 여부입니다.<br>
     * @param wasRequestedBefore Whether the permission was requested before.<br><br>
     *                           이전 요청 이력이 있는지 여부입니다.<br>
     * @return Return value: mapped permission result type. Log behavior: none.<br><br>
     *         반환값: 매핑된 권한 결과 타입. 로그 동작: 없음.<br>
     */
    fun mapResult(
        permission: String,
        granted: Boolean,
        shouldShowRationale: Boolean,
        wasRequestedBefore: Boolean,
    ): PermissionDecisionType = when {
        granted -> PermissionDecisionType.GRANTED
        shouldShowRationale -> PermissionDecisionType.DENIED
        wasRequestedBefore -> PermissionDecisionType.PERMANENTLY_DENIED
        else -> PermissionDecisionType.DENIED
    }
}
