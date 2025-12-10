package kr.open.library.simple_ui.xml.permissions.repository

/**
 * Internal snapshot of a pending permission request.<br><br>
 * 대기 중인 권한 요청의 내부 스냅샷입니다.<br>
 */
internal data class PermissionPendingRequest(
    /**
     * Callbacks waiting for the permission result.<br><br>
     * 권한 결과를 기다리는 콜백 모음입니다.<br>
     */
    val callbacks: MutableList<(List<String>) -> Unit> = mutableListOf(),
    /**
     * Timestamp when the request was created.<br><br>
     * 요청이 생성된 시간입니다.<br>
     */
    val requestTime: Long = System.currentTimeMillis(),
    /**
     * Original permissions requested by the caller.<br><br>
     * 호출자가 요청한 원본 권한 목록입니다.<br>
     */
    val originalPermissions: List<String> = emptyList(),
    /**
     * Special permissions that still need to be processed.<br><br>
     * 아직 처리해야 할 특수 권한 목록입니다.<br>
     */
    var remainingSpecialPermissions: List<String> = emptyList(),
)
