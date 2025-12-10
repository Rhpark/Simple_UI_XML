/**
 * Repository for managing pending permission requests and their callbacks.<br><br>
 * 대기 중인 권한 요청과 콜백을 관리하는 저장소입니다.<br>
 */

package kr.open.library.simple_ui.xml.permissions.repository

import kr.open.library.simple_ui.core.permissions.manager.PermissionCallbackAddResult
import kr.open.library.simple_ui.core.permissions.vo.PermissionConstants

/**
 * Internal repository that stores and manages pending permission requests.<br><br>
 * 대기 중인 권한 요청을 저장하고 관리하는 내부 저장소입니다.<br>
 *
 * This class is intentionally synchronous and mutex-free because the caller (PermissionManager)
 * already guarantees thread-safety through its own mutex.<br><br>
 * 이 클래스는 의도적으로 동기 함수이며 Mutex가 없습니다. 호출자(PermissionManager)가
 * 자체 Mutex를 통해 스레드 안전성을 이미 보장하기 때문입니다.<br>
 */
internal class PermissionRequestRepository {
    /**
     * Stores in-flight permission requests keyed by requestId.<br><br>
     * 진행 중인 권한 요청을 requestId 기준으로 관리합니다.<br>
     */
    private val pendingRequests = mutableMapOf<String, PermissionPendingRequest>()

    /**
     * Creates and stores a new permission request.<br><br>
     * 새로운 권한 요청을 생성하고 저장합니다.<br>
     *
     * @param requestId Unique identifier for this request.<br><br>
     *                  이 요청의 고유 식별자입니다.<br>
     * @param permissions Original permissions to request.<br><br>
     *                    요청할 원본 권한 목록입니다.<br>
     * @param callback Initial callback for this request.<br><br>
     *                 이 요청의 초기 콜백입니다.<br>
     * @return The created PermissionPendingRequest instance.<br><br>
     *         생성된 PermissionPendingRequest 인스턴스입니다.<br>
     */
    fun createRequest(
        requestId: String,
        permissions: List<String>,
        callback: (List<String>) -> Unit,
    ): PermissionPendingRequest {
        val request = PermissionPendingRequest(
            callbacks = mutableListOf(callback),
            originalPermissions = permissions,
        )
        pendingRequests[requestId] = request
        return request
    }

    /**
     * Retrieves a pending request by its ID.<br><br>
     * ID로 대기 중인 요청을 조회합니다.<br>
     *
     * @param requestId The request identifier to look up.<br><br>
     *                  조회할 요청 식별자입니다.<br>
     * @return The PermissionPendingRequest if found, null otherwise.<br><br>
     *         요청을 찾으면 PermissionPendingRequest를, 그렇지 않으면 null을 반환합니다.<br>
     */
    fun getRequest(requestId: String): PermissionPendingRequest? = pendingRequests[requestId]

    /**
     * Removes and returns a pending request.<br><br>
     * 대기 중인 요청을 제거하고 반환합니다.<br>
     *
     * @param requestId The request identifier to remove.<br><br>
     *                  제거할 요청 식별자입니다.<br>
     * @return The removed PermissionPendingRequest if found, null otherwise.<br><br>
     *         제거된 PermissionPendingRequest를 반환하거나, 없으면 null을 반환합니다.<br>
     */
    fun removeRequest(requestId: String): PermissionPendingRequest? = pendingRequests.remove(requestId)

    /**
     * Checks if a request is currently active.<br><br>
     * 요청이 현재 활성 상태인지 확인합니다.<br>
     *
     * @param requestId The request identifier to check.<br><br>
     *                  확인할 요청 식별자입니다.<br>
     * @return True if the request exists, false otherwise.<br><br>
     *         요청이 존재하면 true, 그렇지 않으면 false를 반환합니다.<br>
     */
    fun hasActiveRequest(requestId: String): Boolean = pendingRequests.containsKey(requestId)

    /**
     * Returns the original permissions for a request.<br><br>
     * 요청의 원본 권한 목록을 반환합니다.<br>
     *
     * @param requestId The request identifier to look up.<br><br>
     *                  조회할 요청 식별자입니다.<br>
     * @return Set of original permissions, or null if request not found.<br><br>
     *         원본 권한 집합을 반환하거나, 요청을 찾지 못하면 null을 반환합니다.<br>
     */
    fun getRequestPermissions(requestId: String): Set<String>? = pendingRequests[requestId]?.originalPermissions?.toSet()

    /**
     * Adds an additional callback to an existing request.<br><br>
     * 기존 요청에 추가 콜백을 등록합니다.<br>
     *
     * Used when duplicate permission requests are detected.<br><br>
     * 중복된 권한 요청이 감지되었을 때 사용됩니다.<br>
     *
     * @param requestId The request identifier to add callback to.<br><br>
     *                  콜백을 추가할 요청 식별자입니다.<br>
     * @param callback The callback to add.<br><br>
     *                 추가할 콜백입니다.<br>
     * @param requestedPermissions Permissions to validate against existing request.<br><br>
     *                             기존 요청과 비교할 권한 목록입니다.<br>
     * @return Result indicating success, request not found, or permission mismatch.<br><br>
     *         성공, 요청 없음, 또는 권한 불일치를 나타내는 결과입니다.<br>
     */
    fun addCallbackToRequest(
        requestId: String,
        callback: (List<String>) -> Unit,
        requestedPermissions: List<String>,
    ): PermissionCallbackAddResult {
        // Returns immediately when the request no longer exists.<br><br>
        // 요청이 이미 완료되었거나 취소되었다면 즉시 반환합니다.<br>
        val request = pendingRequests[requestId] ?: return PermissionCallbackAddResult.REQUEST_NOT_FOUND

        // Compares permission sets regardless of order to ensure parity.<br><br>
        // 순서에 상관없이 권한 집합이 동일한지 확인합니다.<br>
        val existingPermissions = request.originalPermissions.toSet()
        val newPermissions = requestedPermissions.toSet()

        if (existingPermissions != newPermissions) {
            return PermissionCallbackAddResult.PERMISSION_MISMATCH
        }

        // Appends the callback when everything matches.<br><br>
        // 조건이 일치하면 콜백을 추가합니다.<br>
        request.callbacks.add(callback)
        return PermissionCallbackAddResult.SUCCESS
    }

    /**
     * Invokes all callbacks registered for a request with the provided denied permissions.<br><br>
     * 요청에 등록된 모든 콜백을 거부된 권한 목록과 함께 호출합니다.<br>
     *
     * @param request The pending request containing callbacks to invoke.<br><br>
     *                호출할 콜백들을 포함한 대기 요청입니다.<br>
     * @param deniedPermissions Permissions that remain denied after the request.<br><br>
     *                          요청 후에도 거부된 상태로 남아 있는 권한 목록입니다.<br>
     */
    fun invokeAllCallbacks(request: PermissionPendingRequest, deniedPermissions: List<String>) {
        request.callbacks.forEach { callback ->
            try {
                callback(deniedPermissions)
            } catch (e: Exception) {
                // Print stack trace without using Logx to maintain unit testability.<br><br>
                // Unit 테스트 가능성을 유지하기 위해 Logx 없이 스택 트레이스만 출력합니다.<br>
                e.printStackTrace()
                // Silently catch exceptions to prevent one failing callback from affecting others.<br><br>
                // 하나의 콜백 실패가 다른 콜백에 영향을 주지 않도록 예외를 조용히 처리합니다.<br>
            }
        }
    }

    /**
     * Removes and invokes callbacks for requests that exceeded the timeout.<br><br>
     * 타임아웃을 초과한 요청들을 제거하고 콜백을 호출합니다.<br>
     *
     * @return List of expired request IDs that were cleaned up.<br><br>
     *         정리된 만료 요청 ID 목록입니다.<br>
     */
    fun cleanupExpiredRequests(): List<String> {
        val currentTime = System.currentTimeMillis()
        val expiredRequestIds = mutableListOf<String>()

        // Identifies requests that lingered beyond the maximum window.<br><br>
        // 허용 시간을 초과한 요청을 찾아냅니다.<br>
        for ((requestId, request) in pendingRequests) {
            if (currentTime - request.requestTime > PermissionConstants.Defaults.REQUEST_TIMEOUT_MS) {
                expiredRequestIds.add(requestId)
                // Notifies callbacks immediately to prevent silent abandonment.<br><br>
                // 조용히 방치되지 않도록 콜백을 즉시 호출합니다.<br>
                invokeAllCallbacks(request, request.originalPermissions)
            }
        }

        // Removes expired entries from the map.<br><br>
        // 만료된 항목들을 맵에서 제거합니다.<br>
        expiredRequestIds.forEach { requestId -> pendingRequests.remove(requestId) }

        return expiredRequestIds
    }

    /**
     * Returns the current number of pending requests.<br><br>
     * 현재 대기 중인 요청의 수를 반환합니다.<br>
     *
     * @return Count of active requests.<br><br>
     *         활성 요청의 개수입니다.<br>
     */
    fun getPendingRequestCount(): Int = pendingRequests.size

    /**
     * Removes all pending requests without invoking callbacks.<br><br>
     * 콜백을 호출하지 않고 모든 대기 요청을 제거합니다.<br>
     *
     * This method is primarily intended for testing purposes.<br><br>
     * 이 메서드는 주로 테스트 목적으로 사용됩니다.<br>
     */
    fun clearAllRequests() {
        pendingRequests.clear()
    }
}
