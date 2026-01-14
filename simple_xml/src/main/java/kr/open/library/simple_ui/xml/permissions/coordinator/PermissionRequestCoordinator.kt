package kr.open.library.simple_ui.xml.permissions.coordinator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.core.permissions.model.PermissionDecisionType
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedItem
import kr.open.library.simple_ui.core.permissions.model.PermissionRationaleRequest
import kr.open.library.simple_ui.core.permissions.model.PermissionSettingsRequest
import kr.open.library.simple_ui.core.permissions.queue.PermissionQueue
import kr.open.library.simple_ui.xml.permissions.flow.PermissionFlowProcessor
import kr.open.library.simple_ui.xml.permissions.result.PermissionResultAggregator
import kr.open.library.simple_ui.xml.permissions.state.PermissionStateSnapshot
import kr.open.library.simple_ui.xml.permissions.state.RequestState

/**
 * Coordinates serialized permission request processing.<br><br>
 * 권한 요청 처리를 직렬화하는 조정자입니다.<br>
 *
 * @param queue Queue that stores request identifiers in order.<br><br>
 *              요청 식별자를 순서대로 보관하는 큐입니다.<br>
 * @param stateSnapshot Snapshot reference for persisted request state.<br><br>
 *                      저장된 요청 상태 스냅샷 참조입니다.<br>
 * @param requests Map of active request entries.<br><br>
 *                 활성 요청 엔트리 맵입니다.<br>
 * @param inFlightWaiters Waiter map used to merge duplicated requests.<br><br>
 *                        중복 요청 병합에 사용하는 대기자 맵입니다.<br>
 * @param scope Coroutine scope used for serialized processing.<br><br>
 *              직렬 처리에 사용하는 코루틴 스코프입니다.<br>
 * @param flowProcessor Processor that handles runtime/special/role flows.<br><br>
 *                      런타임/특수/Role 흐름을 처리하는 프로세서입니다.<br>
 * @param resultAggregator Aggregator for results and persistence updates.<br><br>
 *                         결과/상태 저장 업데이트를 담당하는 집계기입니다.<br>
 */
internal class PermissionRequestCoordinator(
    private val queue: PermissionQueue,
    private val stateSnapshot: PermissionStateSnapshot,
    private val requests: MutableMap<String, RequestEntry>,
    private val inFlightWaiters: MutableMap<String, MutableSet<String>>,
    private val scope: CoroutineScope,
    private val flowProcessor: PermissionFlowProcessor,
    private val resultAggregator: PermissionResultAggregator,
) {
    /**
     * Channel used to signal queued requests.<br><br>
     * 대기 중인 요청 처리를 알리는 채널입니다.<br>
     */
    private val requestChannel: Channel<String> = Channel(Channel.BUFFERED)

    /**
     * Starts restoring and worker processing for queued requests.<br><br>
     * 복원된 요청과 워커 처리를 시작합니다.<br>
     */
    fun start() {
        restoreRequestsFromState()
        startWorker()
        enqueueRestoredRequests()
    }

    /**
     * Enqueues a request ID and signals the worker.<br><br>
     * 요청 ID를 큐에 추가하고 워커를 깨웁니다.<br>
     *
     * @param requestId Request identifier to enqueue.<br><br>
     *                  큐에 추가할 요청 식별자입니다.<br>
     */
    fun enqueueRequest(requestId: String) {
        queue.enqueue(requestId)
        requestChannel.trySend(requestId)
    }

    /**
     * Starts the request processing worker coroutine.<br><br>
     * 요청 처리 워커 코루틴을 시작합니다.<br>
     */
    private fun startWorker() {
        scope.launch {
            for (ignored in requestChannel) {
                while (true) {
                    val nextId = queue.peek() ?: break
                    processRequestId(nextId)
                }
            }
        }
    }

    /**
     * Processes the next request identified by [requestId].<br><br>
     * [requestId]에 해당하는 다음 요청을 처리합니다.<br>
     *
     * @param requestId Request identifier to process.<br><br>
     *                  처리할 요청 식별자입니다.<br>
     */
    private suspend fun processRequestId(requestId: String) {
        val entry = requests[requestId]
        if (entry == null) {
            queue.remove(requestId)
            return
        }
        if (entry.isCompleted()) {
            resultAggregator.tryCompleteRequest(requestId)
            return
        }
        flowProcessor.process(entry)
    }

    /**
     * Enqueues restored requests for processing.<br><br>
     * 복원된 요청을 처리 큐에 다시 등록합니다.<br>
     */
    private fun enqueueRestoredRequests() {
        if (queue.isEmpty()) return
        queue.asList().forEach { requestId ->
            requestChannel.trySend(requestId)
        }
    }

    /**
     * Restores request entries and waiters from saved state.<br><br>
     * 저장된 상태에서 요청 엔트리와 대기자를 복원합니다.<br>
     */
    private fun restoreRequestsFromState() {
        if (stateSnapshot.requestStates.isEmpty()) return
        stateSnapshot.requestStates.values.forEach { state ->
            val entry = RequestEntry(
                requestId = state.requestId,
                permissions = state.permissions,
                results = state.results.toMutableMap(),
                isRestored = true,
                onDeniedResult = null,
                onRationaleNeeded = null,
                onNavigateToSettings = null,
            )
            requests[state.requestId] = entry
            entry.pendingPermissions().forEach { permission ->
                inFlightWaiters.getOrPut(permission) { mutableSetOf() }.add(state.requestId)
            }
        }
    }
}

/**
 * Internal container describing a permission request and its callbacks.<br><br>
 * 권한 요청과 콜백을 보관하는 내부 컨테이너입니다.<br>
 *
 * @param requestId Unique identifier for the request.<br><br>
 *                  요청을 구분하는 고유 식별자입니다.<br>
 * @param permissions Normalized permission list for the request.<br><br>
 *                    요청에 포함된 정규화 권한 목록입니다.<br>
 * @param results Mutable map of permission decisions.<br><br>
 *                권한 결정 결과를 보관하는 가변 맵입니다.<br>
 * @param isRestored Indicates whether the entry was restored without callbacks.<br><br>
 *                   콜백 없이 복원된 엔트리인지 여부를 나타냅니다.<br>
 * @param onDeniedResult Callback invoked with denied results when the request completes.<br><br>
 *                       요청 완료 시 거부 결과를 전달하는 콜백입니다.<br>
 * @param onRationaleNeeded Callback invoked for runtime rationale UI.<br><br>
 *                          런타임 설명 UI 콜백입니다.<br>
 * @param onNavigateToSettings Callback invoked for settings navigation UI.<br><br>
 *                             설정 이동 안내 콜백입니다.<br>
 */
internal data class RequestEntry(
    /**
     * Unique identifier for the request.<br><br>
     * 요청을 구분하는 고유 식별자입니다.<br>
     */
    val requestId: String,
    /**
     * Normalized permission list for the request.<br><br>
     * 요청에 포함된 정규화 권한 목록입니다.<br>
     */
    val permissions: List<String>,
    /**
     * Mutable map of permission decisions.<br><br>
     * 권한 결정 결과를 보관하는 가변 맵입니다.<br>
     */
    val results: MutableMap<String, PermissionDecisionType>,
    /**
     * Indicates whether the entry was restored without callbacks.<br><br>
     * 콜백 없이 복원된 엔트리인지 여부를 나타냅니다.<br>
     */
    val isRestored: Boolean,
    /**
     * Callback invoked with denied results when the request completes.<br><br>
     * 요청 완료 시 거부 결과를 전달하는 콜백입니다.<br>
     */
    val onDeniedResult: ((List<PermissionDeniedItem>) -> Unit)?,
    /**
     * Callback invoked for runtime rationale UI.<br><br>
     * 런타임 설명 UI 콜백입니다.<br>
     */
    val onRationaleNeeded: ((PermissionRationaleRequest) -> Unit)?,
    /**
     * Callback invoked for settings navigation UI.<br><br>
     * 설정 이동 안내 콜백입니다.<br>
     */
    val onNavigateToSettings: ((PermissionSettingsRequest) -> Unit)?,
) {
    /**
     * Returns whether all permissions have resolved results.<br><br>
     * 모든 권한 결과가 확정되었는지 여부를 반환합니다.<br>
     *
     * @return Return value: true when completed, false otherwise. Log behavior: none.<br><br>
     *         반환값: 완료 시 true, 아니면 false입니다. 로그 동작: 없음.<br>
     */
    fun isCompleted(): Boolean = results.size >= permissions.size

    /**
     * Returns the list of permissions that are still pending.<br><br>
     * 아직 결과가 없는 권한 목록을 반환합니다.<br>
     *
     * @return Return value: pending permissions list. Log behavior: none.<br><br>
     *         반환값: 대기 중인 권한 목록입니다. 로그 동작: 없음.<br>
     */
    fun pendingPermissions(): List<String> = permissions.filterNot { results.containsKey(it) }

    /**
     * Converts the entry into a persisted [RequestState].<br><br>
     * 엔트리를 저장용 [RequestState]로 변환합니다.<br>
     *
     * @return Return value: request state snapshot. Log behavior: none.<br><br>
     *         반환값: 요청 상태 스냅샷입니다. 로그 동작: 없음.<br>
     */
    fun toState(): RequestState = RequestState(
        requestId = requestId,
        permissions = permissions,
        results = results.toMap(),
    )
}
