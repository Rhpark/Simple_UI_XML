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
 * Carries rationale UI callbacks for runtime permission requests.<br><br>
 * 런타임 권한 요청 설명 UI 콜백을 전달합니다.<br>
 *
 * @param permissions The permissions that need rationale UI.<br><br>
 *                    설명 UI가 필요한 권한 목록입니다.<br>
 * @param proceed Continues the permission request flow.<br><br>
 *                권한 요청 흐름을 계속 진행합니다.<br>
 * @param cancel Cancels the permission request flow.<br><br>
 *               권한 요청 흐름을 취소합니다.<br>
 */
data class PermissionRationaleRequest(
    val permissions: List<String>,
    val proceed: () -> Unit,
    val cancel: () -> Unit,
)

/**
 * Carries navigation callbacks for special permission settings flows.<br><br>
 * 특수 권한 설정 화면 이동 콜백을 전달합니다.<br>
 *
 * @param permission The permission that requires settings navigation.<br><br>
 *                   설정 이동이 필요한 권한입니다.<br>
 * @param proceed Continues to the settings screen.<br><br>
 *                설정 화면 이동을 진행합니다.<br>
 * @param cancel Cancels the settings navigation.<br><br>
 *               설정 화면 이동을 취소합니다.<br>
 */
data class PermissionSettingsRequest(
    val permission: String,
    val proceed: () -> Unit,
    val cancel: () -> Unit,
)
