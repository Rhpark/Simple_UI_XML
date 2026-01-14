package kr.open.library.simple_ui.xml.permissions.state

import android.os.Bundle
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.permissions.model.OrphanedDeniedRequestResult
import kr.open.library.simple_ui.core.permissions.model.PermissionDecisionType
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedItem
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedType

/**
 * Snapshot entry describing an individual permission request.<br><br>
 * 개별 권한 요청의 상태를 나타내는 스냅샷 항목입니다.<br>
 *
 * @param requestId Request identifier for the entry.<br><br>
 *                  해당 항목의 요청 식별자입니다.<br>
 * @param permissions Normalized permission list for the request.<br><br>
 *                    요청에 포함된 정규화 권한 목록입니다.<br>
 * @param results Mapping of permissions to their current decisions.<br><br>
 *                권한별 현재 결정 결과 매핑입니다.<br>
 */
internal data class RequestState(
    val requestId: String,
    val permissions: List<String>,
    val results: Map<String, PermissionDecisionType>,
)

/**
 * Holds the persisted state snapshot for permission requests.<br><br>
 * 권한 요청의 영속 상태 스냅샷을 보관합니다.<br>
 *
 * @param requestQueue Request ID queue for sequential processing.<br><br>
 *                     순차 처리를 위한 요청 ID 큐입니다.<br>
 * @param requestStates Map of request IDs to their states.<br><br>
 *                      요청 ID와 상태의 매핑입니다.<br>
 * @param requestedHistory Set of previously requested permissions.<br><br>
 *                         이전에 요청된 권한 집합입니다.<br>
 * @param orphanedResults Denied results that lost callbacks after process restore.<br><br>
 *                        프로세스 복원 후 콜백을 잃은 거부 결과 목록입니다.<br>
 */
internal data class PermissionStateSnapshot(
    val requestQueue: MutableList<String> = mutableListOf(),
    val requestStates: MutableMap<String, RequestState> = mutableMapOf(),
    val requestedHistory: MutableSet<String> = mutableSetOf(),
    val orphanedResults: MutableList<OrphanedDeniedRequestResult> = mutableListOf(),
) {
    /**
     * Serializes the snapshot into a Bundle.<br><br>
     * 스냅샷을 Bundle로 직렬화합니다.<br>
     *
     * @return Return value: bundle containing serialized state. Log behavior: none.<br><br>
     *         반환값: 직렬화된 상태를 담은 Bundle. 로그 동작: 없음.<br>
     */
    fun toBundle(): Bundle = Bundle().apply {
        putStringArrayList(KEY_REQUEST_QUEUE, ArrayList(requestQueue))
        putStringArrayList(KEY_REQUEST_HISTORY, ArrayList(requestedHistory))
        putParcelableArrayList(KEY_REQUEST_STATES, ArrayList(requestStates.values.map { it.toBundle() }))
        putParcelableArrayList(KEY_ORPHANED_RESULTS, ArrayList(orphanedResults.map { it.toBundle() }))
    }

    /**
     * Companion holder for snapshot bundle keys and restore logic.<br><br>
     * 스냅샷 번들 키와 복원 로직을 담는 컴패니언입니다.<br>
     */
    companion object {
        /**
         * Bundle key for request queue.<br><br>
         * 요청 큐에 대한 Bundle 키입니다.<br>
         */
        private const val KEY_REQUEST_QUEUE = "permission_requester_queue"

        /**
         * Bundle key for request history.<br><br>
         * 요청 이력에 대한 Bundle 키입니다.<br>
         */
        private const val KEY_REQUEST_HISTORY = "permission_requester_history"

        /**
         * Bundle key for request state entries.<br><br>
         * 요청 상태 항목에 대한 Bundle 키입니다.<br>
         */
        private const val KEY_REQUEST_STATES = "permission_requester_states"

        /**
         * Bundle key for orphaned results.<br><br>
         * orphaned 결과에 대한 Bundle 키입니다.<br>
         */
        private const val KEY_ORPHANED_RESULTS = "permission_requester_orphaned"

        /**
         * Restores the snapshot from a Bundle.<br><br>
         * Bundle에서 스냅샷을 복원합니다.<br>
         *
         * @param bundle Bundle containing serialized state or null.<br><br>
         *               직렬화된 상태를 담은 Bundle 또는 null입니다.<br>
         * @return Return value: restored snapshot. Log behavior: none.<br><br>
         *         반환값: 복원된 스냅샷. 로그 동작: 없음.<br>
         */
        fun fromBundle(bundle: Bundle?): PermissionStateSnapshot {
            if (bundle == null) return PermissionStateSnapshot()
            val queue = bundle.getStringArrayList(KEY_REQUEST_QUEUE)?.toMutableList() ?: mutableListOf()
            val history = bundle.getStringArrayList(KEY_REQUEST_HISTORY)?.toMutableSet() ?: mutableSetOf()
            val requestStates = mutableMapOf<String, RequestState>()
            bundle.getParcelableArrayList<Bundle>(KEY_REQUEST_STATES)?.forEach { requestBundle ->
                requestStateFromBundle(requestBundle)?.let { state ->
                    requestStates[state.requestId] = state
                }
            }
            val orphanedResults = mutableListOf<OrphanedDeniedRequestResult>()
            bundle.getParcelableArrayList<Bundle>(KEY_ORPHANED_RESULTS)?.forEach { orphanBundle ->
                orphanedDeniedRequestResultFromBundle(orphanBundle)?.let { orphanedResults.add(it) }
            }
            return PermissionStateSnapshot(
                requestQueue = queue,
                requestStates = requestStates,
                requestedHistory = history,
                orphanedResults = orphanedResults,
            )
        }
    }
}

/**
 * Persists permission requester state using Bundle snapshots.<br><br>
 * Bundle 스냅샷을 사용해 권한 요청 상태를 보존합니다.<br>
 */
internal class PermissionStateStore {
    /**
     * Bundle key for saved state storage.<br><br>
     * 저장 상태에 사용하는 Bundle 키입니다.<br>
     */
    companion object {
        private const val STATE_KEY = "permission_requester_state_store"
    }

    /**
     * Mutable snapshot of the current permission request state.<br><br>
     * 현재 권한 요청 상태의 가변 스냅샷입니다.<br>
     */
    private var snapshot: PermissionStateSnapshot = PermissionStateSnapshot()

    /**
     * Returns the current snapshot reference.<br><br>
     * 현재 스냅샷 참조를 반환합니다.<br>
     *
     * @return Return value: current snapshot reference. Log behavior: none.<br><br>
     *         반환값: 현재 스냅샷 참조. 로그 동작: 없음.<br>
     */
    fun getSnapshot(): PermissionStateSnapshot = snapshot

    /**
     * Restores the snapshot from [savedInstanceState].<br><br>
     * [savedInstanceState]에서 스냅샷을 복원합니다.<br>
     *
     * @param savedInstanceState Bundle containing saved state or null.<br><br>
     *                           저장된 상태를 담은 Bundle 또는 null입니다.<br>
     */
    fun restoreState(savedInstanceState: Bundle?) {
        val restored = PermissionStateSnapshot.fromBundle(savedInstanceState?.getBundle(STATE_KEY))
        applySnapshot(restored)
    }

    /**
     * Saves the snapshot into [outState].<br><br>
     * [outState]에 스냅샷을 저장합니다.<br>
     *
     * @param outState Bundle that receives the saved state.<br><br>
     *                 저장 상태를 기록할 Bundle입니다.<br>
     */
    fun saveState(outState: Bundle) {
        outState.putBundle(STATE_KEY, snapshot.toBundle())
    }

    /**
     * Applies [block] to mutate the snapshot.<br><br>
     * [block]을 적용해 스냅샷을 변경합니다.<br>
     *
     * @param block Mutation block to apply to the snapshot.<br><br>
     *              스냅샷에 적용할 변경 블록입니다.<br>
     */
    fun update(block: (PermissionStateSnapshot) -> Unit) {
        block(snapshot)
    }

    /**
     * Applies [source] snapshot values to the current snapshot instance.<br><br>
     * [source] 스냅샷 값을 현재 스냅샷 인스턴스에 적용합니다.<br>
     *
     * @param source Snapshot to apply.<br><br>
     *               적용할 스냅샷입니다.<br>
     */
    private fun applySnapshot(source: PermissionStateSnapshot) {
        snapshot.requestQueue.clear()
        snapshot.requestQueue.addAll(source.requestQueue)
        snapshot.requestStates.clear()
        snapshot.requestStates.putAll(source.requestStates)
        snapshot.requestedHistory.clear()
        snapshot.requestedHistory.addAll(source.requestedHistory)
        snapshot.orphanedResults.clear()
        snapshot.orphanedResults.addAll(source.orphanedResults)
    }
}

/**
 * Bundle key for request ID in serialized state.<br><br>
 * 직렬화 상태에서 요청 ID에 대한 Bundle 키입니다.<br>
 */
private const val KEY_REQUEST_ID = "request_id"

/**
 * Bundle key for permission list in serialized state.<br><br>
 * 직렬화 상태에서 권한 목록에 대한 Bundle 키입니다.<br>
 */
private const val KEY_PERMISSIONS = "permissions"

/**
 * Bundle key for result permission list in serialized state.<br><br>
 * 직렬화 상태에서 결과 권한 목록에 대한 Bundle 키입니다.<br>
 */
private const val KEY_RESULT_PERMISSIONS = "result_permissions"

/**
 * Bundle key for result type list in serialized state.<br><br>
 * 직렬화 상태에서 결과 타입 목록에 대한 Bundle 키입니다.<br>
 */
private const val KEY_RESULT_TYPES = "result_types"

/**
 * Converts [RequestState] into a Bundle representation.<br><br>
 * [RequestState]를 Bundle 형태로 변환합니다.<br>
 *
 * @return Return value: bundle representation. Log behavior: none.<br><br>
 *         반환값: Bundle 표현. 로그 동작: 없음.<br>
 */
private fun RequestState.toBundle(): Bundle = Bundle().apply {
    putString(KEY_REQUEST_ID, requestId)
    putStringArrayList(KEY_PERMISSIONS, ArrayList(permissions))
    putStringArrayList(KEY_RESULT_PERMISSIONS, ArrayList(results.keys))
    putIntArray(KEY_RESULT_TYPES, results.values.map { it.ordinal }.toIntArray())
}

/**
 * Restores [RequestState] from a Bundle.<br><br>
 * Bundle에서 [RequestState]를 복원합니다.<br>
 *
 * @param bundle Bundle containing request state data.<br><br>
 *               요청 상태 데이터를 담은 Bundle입니다.<br>
 * @return Return value: restored RequestState or null when invalid. Log behavior: none.<br><br>
 *         반환값: 복원된 RequestState 또는 유효하지 않으면 null. 로그 동작: 없음.<br>
 */
private fun requestStateFromBundle(bundle: Bundle): RequestState? = safeCatch(defaultValue = null) {
    val requestId = bundle.getString(KEY_REQUEST_ID) ?: return@safeCatch null
    val permissions = bundle.getStringArrayList(KEY_PERMISSIONS)?.toList() ?: emptyList()
    val resultPermissions = bundle.getStringArrayList(KEY_RESULT_PERMISSIONS) ?: arrayListOf()
    val resultTypes = bundle.getIntArray(KEY_RESULT_TYPES) ?: intArrayOf()
    val results = mutableMapOf<String, PermissionDecisionType>()
    for (index in resultPermissions.indices) {
        val permission = resultPermissions[index]
        val typeOrdinal = resultTypes.getOrNull(index) ?: continue
        val type = PermissionDecisionType.entries.getOrNull(typeOrdinal) ?: continue
        results[permission] = type
    }
    RequestState(
        requestId = requestId,
        permissions = permissions,
        results = results,
    )
}

/**
 * Converts [OrphanedDeniedRequestResult] into a Bundle representation.<br><br>
 * [OrphanedDeniedRequestResult]를 Bundle 형태로 변환합니다.<br>
 *
 * @return Return value: bundle representation. Log behavior: none.<br><br>
 *         반환값: Bundle 표현. 로그 동작: 없음.<br>
 */
private fun OrphanedDeniedRequestResult.toBundle(): Bundle = Bundle().apply {
    putString(KEY_REQUEST_ID, requestId)
    putStringArrayList(KEY_RESULT_PERMISSIONS, ArrayList(deniedResults.map { it.permission }))
    putIntArray(KEY_RESULT_TYPES, deniedResults.map { it.result.ordinal }.toIntArray())
}

/**
 * Restores [OrphanedDeniedRequestResult] from a Bundle.<br><br>
 * Bundle에서 [OrphanedDeniedRequestResult]를 복원합니다.<br>
 *
 * @param bundle Bundle containing orphaned denied result data.<br><br>
 *               orphaned 거부 결과 데이터를 담은 Bundle입니다.<br>
 * @return Return value: restored OrphanedDeniedRequestResult or null when invalid. Log behavior: none.<br><br>
 *         반환값: 복원된 OrphanedDeniedRequestResult 또는 유효하지 않으면 null. 로그 동작: 없음.<br>
 */
private fun orphanedDeniedRequestResultFromBundle(bundle: Bundle): OrphanedDeniedRequestResult? =
    safeCatch(defaultValue = null) {
        val requestId = bundle.getString(KEY_REQUEST_ID) ?: return@safeCatch null
        val resultPermissions = bundle.getStringArrayList(KEY_RESULT_PERMISSIONS) ?: arrayListOf()
        val resultTypes = bundle.getIntArray(KEY_RESULT_TYPES) ?: intArrayOf()
        val deniedResults = mutableListOf<PermissionDeniedItem>()
        for (index in resultPermissions.indices) {
            val permission = resultPermissions[index]
            val typeOrdinal = resultTypes.getOrNull(index) ?: continue
            val type = PermissionDeniedType.entries.getOrNull(typeOrdinal) ?: continue
            deniedResults.add(PermissionDeniedItem(permission, type))
        }
        OrphanedDeniedRequestResult(
            requestId = requestId,
            deniedResults = deniedResults,
        )
    }
