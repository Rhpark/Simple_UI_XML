package kr.open.library.simple_ui.core.permissions.manager

/**
 * Indicates whether an additional callback could be attached to an in-flight request.<br><br>
 * 진행 중인 권한 요청에 추가 콜백을 부착할 수 있었는지를 나타냅니다.<br>
 */

enum class PermissionCallbackAddResult {
    /**
     * Additional callbacks were attached successfully.<br><br>
     * 추가 콜백이 정상적으로 연결되었습니다.<br>
     */
    SUCCESS,

    /**
     * The request could not be found (completed or cancelled already).<br><br>
     * 요청을 찾을 수 없어 이미 완료되었거나 취소된 상태입니다.<br>
     */
    REQUEST_NOT_FOUND,

    /**
     * The permission set does not match the existing pending request.<br><br>
     * 새로 전달된 권한 집합이 기존 대기 요청과 일치하지 않습니다.<br>
     */
    PERMISSION_MISMATCH,
}
