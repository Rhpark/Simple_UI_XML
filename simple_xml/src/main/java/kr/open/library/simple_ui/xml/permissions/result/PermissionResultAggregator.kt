package kr.open.library.simple_ui.xml.permissions.result

import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.permissions.classifier.PermissionClassifier
import kr.open.library.simple_ui.core.permissions.model.OrphanedDeniedRequestResult
import kr.open.library.simple_ui.core.permissions.model.PermissionDecisionType
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedItem
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedType
import kr.open.library.simple_ui.core.permissions.queue.PermissionQueue
import kr.open.library.simple_ui.xml.permissions.coordinator.RequestEntry
import kr.open.library.simple_ui.xml.permissions.state.PermissionStateSnapshot
import kr.open.library.simple_ui.xml.permissions.state.PermissionStateStore

/**
 * Aggregates permission results, waiters, and persistence updates.<br><br>
 * 권한 결과 집계, 대기자 관리, 상태 저장 업데이트를 담당합니다.<br>
 *
 * @param stateStore Saved state store used for persistence.<br><br>
 *                   상태 저장에 사용하는 스토어입니다.<br>
 * @param stateSnapshot Snapshot reference for the persisted state.<br><br>
 *                      보존된 상태의 스냅샷 참조입니다.<br>
 * @param queue Queue of request identifiers.<br><br>
 *              요청 식별자 큐입니다.<br>
 * @param requests Map of active request entries.<br><br>
 *                 활성 요청 엔트리 맵입니다.<br>
 * @param inFlightWaiters Waiter map for merging permission results.<br><br>
 *                        권한 결과 병합을 위한 대기자 맵입니다.<br>
 * @param classifier Permission classifier used for logging.<br><br>
 *                   로깅에 사용하는 권한 분류기입니다.<br>
 */
internal class PermissionResultAggregator(
    private val stateStore: PermissionStateStore,
    private val stateSnapshot: PermissionStateSnapshot,
    private val queue: PermissionQueue,
    private val requests: MutableMap<String, RequestEntry>,
    private val inFlightWaiters: MutableMap<String, MutableSet<String>>,
    private val classifier: PermissionClassifier,
) {
    /**
     * Log tag used for permission diagnostics.<br><br>
     * 권한 진단 로그에 사용하는 태그입니다.<br>
     */
    companion object {
        /**
         * Log tag used for permission diagnostics.<br><br>
         * 권한 진단 로그에 사용하는 태그입니다.<br>
         */
        private const val LOG_TAG = "PermissionRequester"
    }

    /**
     * Returns and clears orphaned denied results after process restore.<br><br>
     * 프로세스 복원 후 orphaned 거부 결과를 반환하고 비웁니다.<br>
     *
     * @return Return value: list of orphaned denied request results. Log behavior: none.<br><br>
     *         반환값: orphaned 거부 요청 결과 목록입니다. 로그 동작: 없음.<br>
     */
    fun consumeOrphanedDeniedResults(): List<OrphanedDeniedRequestResult> {
        val results = stateSnapshot.orphanedResults.toList()
        stateStore.update { snapshot -> snapshot.orphanedResults.clear() }
        return results
    }

    /**
     * Registers a request entry and persists its state.<br><br>
     * 요청 엔트리를 등록하고 상태를 저장합니다.<br>
     *
     * @param entry Request entry to register.<br><br>
     *              등록할 요청 엔트리입니다.<br>
     */
    fun registerRequest(entry: RequestEntry) {
        requests[entry.requestId] = entry
        persistRequest(entry)
    }

    /**
     * Registers waiter mappings for the given permissions.<br><br>
     * 지정된 권한에 대한 대기자 매핑을 등록합니다.<br>
     *
     * @param requestId Request identifier to register.<br><br>
     *                  등록할 요청 식별자입니다.<br>
     * @param permissions Permissions that should wait for results.<br><br>
     *                    결과를 대기할 권한 목록입니다.<br>
     */
    fun registerWaiters(
        requestId: String,
        permissions: List<String>,
    ) {
        permissions.forEach { permission ->
            inFlightWaiters.getOrPut(permission) { mutableSetOf() }.add(requestId)
        }
    }

    /**
     * Completes waiters for [permissions] with the same [result].<br><br>
     * [permissions]의 대기자를 동일한 [result]로 완료 처리합니다.<br>
     *
     * @param permissions Permissions to complete.<br><br>
     *                    완료 처리할 권한 목록입니다.<br>
     * @param result Result to apply to all permissions.<br><br>
     *               모든 권한에 적용할 결과입니다.<br>
     */
    fun completeWaitersForPermissions(
        permissions: List<String>,
        result: PermissionDecisionType,
    ) {
        if (permissions.isEmpty()) return
        completeWaiters(permissions.associateWith { result })
    }

    /**
     * Completes waiters using a permission-to-result mapping.<br><br>
     * 권한-결과 매핑을 사용하여 대기자를 완료 처리합니다.<br>
     *
     * @param results Permission-to-result mapping.<br><br>
     *                권한-결과 매핑입니다.<br>
     */
    fun completeWaiters(results: Map<String, PermissionDecisionType>) {
        if (results.isEmpty()) return
        results.forEach { (permission, result) ->
            val waiters = inFlightWaiters.remove(permission) ?: return@forEach
            waiters.forEach { requestId ->
                updateResultsForWaiters(
                    requestId = requestId,
                    permission = permission,
                    result = result,
                )
            }
        }
    }

    /**
     * Attempts to complete a request if all results are available.<br><br>
     * 모든 결과가 준비되었으면 요청 완료를 시도합니다.<br>
     *
     * @param requestId Request identifier to complete.<br><br>
     *                  완료할 요청 식별자입니다.<br>
     */
    fun tryCompleteRequest(requestId: String) {
        val entry = requests[requestId] ?: return
        if (!entry.isCompleted()) return

        val deniedResults = entry.permissions.mapNotNull { permission ->
            val decision = entry.results[permission] ?: PermissionDecisionType.MANIFEST_UNDECLARED
            decision.toDeniedTypeOrNull()?.let { deniedType ->
                PermissionDeniedItem(permission, deniedType)
            }
        }

        if (entry.onDeniedResult != null) {
            safeCatch { entry.onDeniedResult.invoke(deniedResults) }
        } else {
            stateStore.update { snapshot ->
                snapshot.orphanedResults.add(
                    OrphanedDeniedRequestResult(
                        requestId = requestId,
                        deniedResults = deniedResults,
                    ),
                )
            }
        }

        requests.remove(requestId)
        queue.remove(requestId)
        stateStore.update { snapshot -> snapshot.requestStates.remove(requestId) }
    }

    /**
     * Logs a permission result with request metadata.<br><br>
     * 요청 메타데이터와 함께 권한 결과를 로깅합니다.<br>
     *
     * @param requestId Request identifier for the result.<br><br>
     *                  결과에 해당하는 요청 식별자입니다.<br>
     * @param permission Permission string being logged.<br><br>
     *                   로깅할 권한 문자열입니다.<br>
     * @param result Permission decision type to log.<br><br>
     *               로깅할 권한 결정 타입입니다.<br>
     */
    fun logResult(
        requestId: String,
        permission: String,
        result: PermissionDecisionType,
    ) {
        logPermissionResult(
            requestId = requestId,
            permission = permission,
            result = result,
        )
    }

    /**
     * Updates [requestId] entry with [permission] result and attempts completion.<br><br>
     * [requestId] 엔트리에 [permission] 결과를 갱신하고 완료 처리를 시도합니다.<br>
     *
     * @param requestId Request identifier to update.<br><br>
     *                  갱신할 요청 식별자입니다.<br>
     * @param permission Permission being updated.<br><br>
     *                   결과를 갱신할 권한입니다.<br>
     * @param result Result to store for the permission.<br><br>
     *               권한에 저장할 결과입니다.<br>
     */
    private fun updateResultsForWaiters(
        requestId: String,
        permission: String,
        result: PermissionDecisionType,
    ) {
        val entry = requests[requestId] ?: return
        entry.results[permission] = result
        persistRequest(entry)
        logPermissionResult(
            requestId = requestId,
            permission = permission,
            result = result,
        )
        tryCompleteRequest(requestId)
    }

    /**
     * Persists the current state of [entry] to the saved state store.<br><br>
     * [entry]의 현재 상태를 저장소에 반영합니다.<br>
     *
     * @param entry Request entry to persist.<br><br>
     *              저장할 요청 엔트리입니다.<br>
     */
    private fun persistRequest(entry: RequestEntry) {
        stateStore.update { snapshot ->
            snapshot.requestStates[entry.requestId] = entry.toState()
        }
    }

    /**
     * Logs permission denied details using [Logx].<br><br>
     * [Logx]로 권한 거부 상세를 로깅합니다.<br>
     *
     * @param requestId Request identifier for the log entry.<br><br>
     *                  로그에 포함될 요청 식별자입니다.<br>
     * @param permission Permission string being logged.<br><br>
     *                   로깅할 권한 문자열입니다.<br>
     * @param result Permission decision type to log.<br><br>
     *               로깅할 권한 결정 타입입니다.<br>
     */
    private fun logPermissionResult(
        requestId: String,
        permission: String,
        result: PermissionDecisionType,
    ) {
        val type = classifier.classify(permission)
        val deniedType = result.toDeniedTypeOrNull()?.name ?: "GRANTED"
        Logx.d("$LOG_TAG: requestId=$requestId, permission=$permission, type=$type, deniedType=$deniedType")
    }

    /**
     * Maps an internal decision type to an external denied type.<br><br>
     * 내부 결정 타입을 외부 거부 타입으로 매핑합니다.<br>
     *
     * @return Return value: denied type or null when granted. Log behavior: none.<br><br>
     *         반환값: 거부 타입 또는 승인 시 null. 로그 동작: 없음.<br>
     */
    private fun PermissionDecisionType.toDeniedTypeOrNull(): PermissionDeniedType? = when (this) {
        PermissionDecisionType.GRANTED -> null
        PermissionDecisionType.DENIED -> PermissionDeniedType.DENIED
        PermissionDecisionType.PERMANENTLY_DENIED -> PermissionDeniedType.PERMANENTLY_DENIED
        PermissionDecisionType.MANIFEST_UNDECLARED -> PermissionDeniedType.MANIFEST_UNDECLARED
        PermissionDecisionType.EMPTY_REQUEST -> PermissionDeniedType.EMPTY_REQUEST
        PermissionDecisionType.NOT_SUPPORTED -> PermissionDeniedType.NOT_SUPPORTED
        PermissionDecisionType.FAILED_TO_LAUNCH_SETTINGS -> PermissionDeniedType.FAILED_TO_LAUNCH_SETTINGS
        PermissionDecisionType.LIFECYCLE_NOT_READY -> PermissionDeniedType.LIFECYCLE_NOT_READY
    }
}
