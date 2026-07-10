package kr.open.library.simple_ui.core.permissions.model

/**
 * Represents a denied permission entry returned to callers.<br><br>
 * 호출부에 반환되는 거부 권한 항목입니다.<br>
 *
 * @param permission The requested permission string.<br><br>
 *                   요청한 권한 문자열입니다.<br>
 * @param result The denied result type for the permission.<br><br>
 *               권한의 거부 결과 타입입니다.<br>
 */
data class PermissionDeniedItem(
    val permission: String,
    val result: PermissionDeniedType,
)

/**
 * Enumerates denied/failed permission result types exposed to callers.<br><br>
 * 호출부에 노출되는 거부/실패 결과 타입입니다.<br>
 */
enum class PermissionDeniedType {
    /**
     * Indicates the permission was denied by the user.<br><br>
     * 사용자에 의해 거부되었음을 나타냅니다.<br>
     */
    DENIED,

    /**
     * Indicates the permission is permanently denied.<br><br>
     * 영구 거부 상태임을 나타냅니다.<br>
     */
    PERMANENTLY_DENIED,

    /**
     * Indicates the permission is not declared in the manifest (including empty strings).<br><br>
     * Manifest에 선언되지 않은 권한(빈 문자열 포함)임을 나타냅니다.<br>
     */
    MANIFEST_UNDECLARED,

    /**
     * Indicates the request list was empty.<br><br>
     * 요청 리스트가 비어 있음을 나타냅니다.<br>
     */
    EMPTY_REQUEST,

    /**
     * Indicates the permission is not supported on this device or OS level.<br><br>
     * 기기/OS에서 지원하지 않는 권한임을 나타냅니다.<br>
     */
    NOT_SUPPORTED,

    /**
     * Indicates the settings screen could not be launched.<br><br>
     * 설정 화면을 실행하지 못했음을 나타냅니다.<br>
     */
    FAILED_TO_LAUNCH_SETTINGS,

    /**
     * Indicates the request could not be launched due to lifecycle state.<br><br>
     * 라이프사이클 상태로 인해 요청을 실행할 수 없음을 나타냅니다.<br>
     */
    LIFECYCLE_NOT_READY,
}

/**
 * Enumerates internal permission decision states used in processing and storage.<br><br>
 * 내부 처리/저장에 사용하는 권한 결정 상태입니다.<br>
 */
enum class PermissionDecisionType {
    /**
     * Indicates the permission was granted.<br><br>
     * 권한이 승인되었음을 나타냅니다.<br>
     */
    GRANTED,

    /**
     * Indicates the permission was denied by the user.<br><br>
     * 사용자에 의해 거부되었음을 나타냅니다.<br>
     */
    DENIED,

    /**
     * Indicates the permission is permanently denied.<br><br>
     * 영구 거부 상태임을 나타냅니다.<br>
     */
    PERMANENTLY_DENIED,

    /**
     * Indicates the permission is not declared in the manifest (including empty strings).<br><br>
     * Manifest에 선언되지 않은 권한(빈 문자열 포함)임을 나타냅니다.<br>
     */
    MANIFEST_UNDECLARED,

    /**
     * Indicates the request list was empty.<br><br>
     * 요청 리스트가 비어 있음을 나타냅니다.<br>
     */
    EMPTY_REQUEST,

    /**
     * Indicates the permission is not supported on this device or OS level.<br><br>
     * 기기/OS에서 지원하지 않는 권한임을 나타냅니다.<br>
     */
    NOT_SUPPORTED,

    /**
     * Indicates the settings screen could not be launched.<br><br>
     * 설정 화면을 실행하지 못했음을 나타냅니다.<br>
     */
    FAILED_TO_LAUNCH_SETTINGS,

    /**
     * Indicates the request could not be launched due to lifecycle state.<br><br>
     * 라이프사이클 상태로 인해 요청을 실행할 수 없음을 나타냅니다.<br>
     */
    LIFECYCLE_NOT_READY,
}

/**
 * Represents a completed request whose denied results lost their callback after process restore.<br><br>
 * 프로세스 복원으로 콜백을 잃은 거부 결과 요청입니다.<br>
 *
 * @param requestId The identifier of the original request.<br><br>
 *                  원본 요청 식별자입니다.<br>
 * @param deniedResults The denied results for the request.<br><br>
 *                      요청에 대한 거부 결과 목록입니다.<br>
 */
data class OrphanedDeniedRequestResult(
    val requestId: String,
    val deniedResults: List<PermissionDeniedItem>,
)

/**
 * Defines how deferred rationale/settings decisions are auto-cancelled by lifecycle changes.<br><br>
 * defer된 설명/설정 이동 결정이 라이프사이클 변화로 어떻게 자동 취소되는지 정의합니다.<br>
 */
enum class PermissionDeferredPolicy {
    /**
     * Cancels the deferred decision when the host moves to `onStop`.<br><br>
     * 호스트가 `onStop`으로 이동하면 defer된 결정을 취소합니다.<br>
     */
    CANCEL_ON_STOP,

    /**
     * Keeps the deferred decision across `onStop` and cancels only on `onDestroy`.<br><br>
     * `onStop` 동안에는 defer된 결정을 유지하고 `onDestroy`에서만 취소합니다.<br>
     */
    CANCEL_ON_DESTROY,
}

/**
 * Carries rationale UI actions for runtime permission requests.<br><br>
 * The callback must call `proceed()`, `cancel()`, or `defer(policy)` before returning; otherwise the flow is auto-cancelled.<br>
 * 콜백은 반환되기 전에 `proceed()`, `cancel()`, `defer(policy)` 중 하나를 호출해야 하며, 아무 액션 없이 반환되면 흐름은 자동 취소됩니다.<br>
 * 런타임 권한 요청 설명 UI에 필요한 액션을 전달합니다.<br>
 */
interface PermissionRationaleRequest {
    /**
     * Permissions that need rationale UI.<br><br>
     * 설명 UI가 필요한 권한 목록입니다.<br>
     */
    val permissions: List<String>

    /**
     * Continues the permission request flow.<br><br>
     * 권한 요청 흐름을 계속 진행합니다.<br>
     */
    fun proceed()

    /**
     * Cancels the permission request flow.<br><br>
     * 권한 요청 흐름을 취소합니다.<br>
     */
    fun cancel()

    /**
     * Marks that the decision will complete asynchronously later.<br><br>
     * The default policy is `CANCEL_ON_STOP`; use `CANCEL_ON_DESTROY` when the deferred UI must survive `onStop` and cancel only on host destruction.<br>
     * 기본 정책은 `CANCEL_ON_STOP`이며, `onStop`을 지나도 유지하고 host 종료 시에만 취소해야 하면 `CANCEL_ON_DESTROY`를 사용합니다.<br>
     * 결정이 이후 비동기로 완료될 예정임을 표시합니다.<br>
     *
     * @param policy Lifecycle auto-cancel policy for the deferred decision.<br><br>
     *               defer된 결정에 적용할 라이프사이클 자동 취소 정책입니다.<br>
     */
    fun defer(policy: PermissionDeferredPolicy = PermissionDeferredPolicy.CANCEL_ON_STOP)
}

/**
 * Carries settings navigation actions for special permission flows.<br><br>
 * The callback must call `proceed()`, `cancel()`, or `defer(policy)` before returning; otherwise the flow is auto-cancelled.<br>
 * 콜백은 반환되기 전에 `proceed()`, `cancel()`, `defer(policy)` 중 하나를 호출해야 하며, 아무 액션 없이 반환되면 흐름은 자동 취소됩니다.<br>
 * 특수 권한 설정 화면 이동 흐름에 필요한 액션을 전달합니다.<br>
 */
interface PermissionSettingsRequest {
    /**
     * Permission that requires settings navigation.<br><br>
     * 설정 이동이 필요한 권한입니다.<br>
     */
    val permission: String

    /**
     * Continues to the settings screen.<br><br>
     * 설정 화면 이동을 진행합니다.<br>
     */
    fun proceed()

    /**
     * Cancels the settings navigation flow.<br><br>
     * 설정 화면 이동 흐름을 취소합니다.<br>
     */
    fun cancel()

    /**
     * Marks that the decision will complete asynchronously later.<br><br>
     * The default policy is `CANCEL_ON_STOP`; use `CANCEL_ON_DESTROY` when the deferred UI must survive `onStop` and cancel only on host destruction.<br>
     * 기본 정책은 `CANCEL_ON_STOP`이며, `onStop`을 지나도 유지하고 host 종료 시에만 취소해야 하면 `CANCEL_ON_DESTROY`를 사용합니다.<br>
     * 결정이 이후 비동기로 완료될 예정임을 표시합니다.<br>
     *
     * @param policy Lifecycle auto-cancel policy for the deferred decision.<br><br>
     *               defer된 결정에 적용할 라이프사이클 자동 취소 정책입니다.<br>
     */
    fun defer(policy: PermissionDeferredPolicy = PermissionDeferredPolicy.CANCEL_ON_STOP)
}

/**
 * Converts an internal [PermissionDecisionType] into the caller-facing [PermissionDeniedType].<br><br>
 * 내부 결정 상태 [PermissionDecisionType]을 호출부 노출용 [PermissionDeniedType]으로 변환합니다.<br>
 *
 * [PermissionDecisionType.GRANTED] is not a denied entry, so `null` is returned.<br>
 * UI 모듈(simple_xml / simple_compose)이 공유하는 단일 변환 규칙입니다.<br><br>
 * [PermissionDecisionType.GRANTED]는 거부 항목이 아니므로 `null`을 반환합니다.<br>
 * This is the single conversion rule shared by the UI modules (simple_xml / simple_compose).<br>
 *
 * @return Return value: matching denied type, or null when granted. Log behavior: none.<br><br>
 *         반환값: 대응하는 거부 타입, 승인 상태이면 null. 로그 동작: 없음.<br>
 */
fun PermissionDecisionType.toDeniedTypeOrNull(): PermissionDeniedType? = when (this) {
    PermissionDecisionType.GRANTED -> null
    PermissionDecisionType.DENIED -> PermissionDeniedType.DENIED
    PermissionDecisionType.PERMANENTLY_DENIED -> PermissionDeniedType.PERMANENTLY_DENIED
    PermissionDecisionType.MANIFEST_UNDECLARED -> PermissionDeniedType.MANIFEST_UNDECLARED
    PermissionDecisionType.EMPTY_REQUEST -> PermissionDeniedType.EMPTY_REQUEST
    PermissionDecisionType.NOT_SUPPORTED -> PermissionDeniedType.NOT_SUPPORTED
    PermissionDecisionType.FAILED_TO_LAUNCH_SETTINGS -> PermissionDeniedType.FAILED_TO_LAUNCH_SETTINGS
    PermissionDecisionType.LIFECYCLE_NOT_READY -> PermissionDeniedType.LIFECYCLE_NOT_READY
}

/**
 * 요청 순서를 유지하면서 권한 결정 맵을 호출부용 거부 목록으로 변환합니다.<br><br>
 * Converts permission decisions into caller-facing denied items while preserving request order.<br>
 *
 * 결과가 없는 권한은 승인으로 오인되지 않도록 [PermissionDecisionType.MANIFEST_UNDECLARED]로 처리합니다.<br>
 * A permission without a recorded decision is treated as [PermissionDecisionType.MANIFEST_UNDECLARED]
 * so that it cannot be mistaken for a granted permission.<br>
 *
 * @param permissions 요청한 권한 목록입니다.<br><br>
 *                    Requested permissions in caller order.<br>
 * @param results 권한별 내부 결정 상태입니다.<br><br>
 *                Internal decision state by permission.<br>
 * @return 요청 순서로 정렬된 거부 항목 목록이며, 모두 승인된 경우 빈 목록입니다.<br><br>
 *         Denied items in request order, or an empty list when all permissions are granted.<br>
 */
fun buildPermissionDeniedItems(
    permissions: List<String>,
    results: Map<String, PermissionDecisionType>,
): List<PermissionDeniedItem> = permissions.mapNotNull { permission ->
    val decision = results[permission] ?: PermissionDecisionType.MANIFEST_UNDECLARED
    decision.toDeniedTypeOrNull()?.let { deniedType -> PermissionDeniedItem(permission, deniedType) }
}
