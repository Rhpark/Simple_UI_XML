package kr.open.library.simple_ui.xml.permissions.api

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.permissions.classifier.PermissionClassifier
import kr.open.library.simple_ui.core.permissions.classifier.PermissionType
import kr.open.library.simple_ui.core.permissions.extentions.hasPermission
import kr.open.library.simple_ui.core.permissions.handler.RolePermissionHandler
import kr.open.library.simple_ui.core.permissions.handler.SpecialPermissionHandler
import kr.open.library.simple_ui.core.permissions.model.OrphanedDeniedRequestResult
import kr.open.library.simple_ui.core.permissions.model.PermissionDecisionType
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedItem
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedType
import kr.open.library.simple_ui.core.permissions.model.PermissionRationaleRequest
import kr.open.library.simple_ui.core.permissions.model.PermissionSettingsRequest
import kr.open.library.simple_ui.core.permissions.queue.PermissionQueue
import kr.open.library.simple_ui.xml.permissions.coordinator.PermissionRequestCoordinator
import kr.open.library.simple_ui.xml.permissions.coordinator.RequestEntry
import kr.open.library.simple_ui.xml.permissions.flow.PermissionFlowProcessor
import kr.open.library.simple_ui.xml.permissions.flow.RuntimePermissionHandler
import kr.open.library.simple_ui.xml.permissions.host.PermissionHostAdapter
import kr.open.library.simple_ui.xml.permissions.result.PermissionResultAggregator
import kr.open.library.simple_ui.xml.permissions.state.PermissionStateStore
import java.util.UUID

/**
 * Coordinates runtime, special, and role permission requests through one API.<br><br>
 * 런타임/특수/Role 권한 요청을 하나의 API로 조정합니다.<br>
 *
 * @param host Host adapter supplying Activity/Fragment capabilities.<br><br>
 *             Activity/Fragment 기능을 제공하는 호스트 어댑터입니다.<br>
 */
class PermissionRequester private constructor(
    private val host: PermissionHostAdapter,
) {
    /**
     * Creates a requester bound to an Activity host.<br><br>
     * Activity 호스트에 바인딩된 요청기를 생성합니다.<br>
     *
     * @param activity Activity host used for permission requests.<br><br>
     *                 권한 요청에 사용되는 Activity 호스트입니다.<br>
     */
    constructor(activity: ComponentActivity) : this(PermissionHostAdapter.ActivityHost(activity))

    /**
     * Creates a requester bound to a Fragment host.<br><br>
     * Fragment 호스트에 바인딩된 요청기를 생성합니다.<br>
     *
     * @param fragment Fragment host used for permission requests.<br><br>
     *                 권한 요청에 사용되는 Fragment 호스트입니다.<br>
     */
    constructor(fragment: Fragment) : this(PermissionHostAdapter.FragmentHost(fragment))

    /**
     * Companion container for internal constants.<br><br>
     * 내부 상수를 보관하는 companion 컨테이너입니다.<br>
     */
    companion object {
        /**
         * Empty permission placeholder used for empty request results.<br><br>
         * 빈 요청 결과에 사용하는 빈 권한 문자열입니다.<br>
         */
        private const val EMPTY_REQUEST_PERMISSION = ""
    }

    /**
     * State store used for Bundle snapshot persistence.<br><br>
     * Bundle 스냅샷 기반 상태 보존에 사용하는 스토어입니다.<br>
     */
    private val stateStore = PermissionStateStore()

    /**
     * Snapshot reference for persisted permission state.<br><br>
     * 보존된 권한 상태의 스냅샷 참조입니다.<br>
     */
    private val stateSnapshot = stateStore.getSnapshot()

    /**
     * Queue wrapper for request IDs.<br><br>
     * 요청 ID를 관리하는 큐 래퍼입니다.<br>
     */
    private val queue = PermissionQueue(stateSnapshot.requestQueue)

    /**
     * Permission classifier for runtime/special/role categorization.<br><br>
     * 런타임/특수/Role 분류를 위한 권한 분류기입니다.<br>
     */
    private val classifier: PermissionClassifier = PermissionClassifier(host.context)

    /**
     * Runtime permission handler for rationale and result mapping.<br><br>
     * 런타임 권한의 설명 및 결과 매핑 처리기입니다.<br>
     */
    private val runtimeHandler: RuntimePermissionHandler =
        RuntimePermissionHandler(host, stateSnapshot.requestedHistory)

    /**
     * Special permission handler for settings intents and checks.<br><br>
     * 특수 권한의 설정 인텐트/상태 확인 처리기입니다.<br>
     */
    private val specialHandler: SpecialPermissionHandler = SpecialPermissionHandler(host.context)

    /**
     * Role permission handler for RoleManager checks and intents.<br><br>
     * RoleManager 기반 역할 권한 처리기입니다.<br>
     */
    private val roleHandler: RolePermissionHandler = RolePermissionHandler(host.context)

    /**
     * Active request entries keyed by request ID.<br><br>
     * requestId를 키로 하는 활성 요청 엔트리 맵입니다.<br>
     */
    private val requests: MutableMap<String, RequestEntry> = mutableMapOf()

    /**
     * Permission waiters for merging duplicate permission requests.<br><br>
     * 중복 권한 요청 병합을 위한 대기자 맵입니다.<br>
     */
    private val inFlightWaiters: MutableMap<String, MutableSet<String>> = mutableMapOf()

    /**
     * Aggregator for request results and persistence updates.<br><br>
     * 요청 결과와 상태 저장 업데이트를 집계하는 객체입니다.<br>
     */
    private val resultAggregator = PermissionResultAggregator(
        stateStore = stateStore,
        stateSnapshot = stateSnapshot,
        queue = queue,
        requests = requests,
        inFlightWaiters = inFlightWaiters,
        classifier = classifier,
    )

    /**
     * Processor that handles runtime/special/role permission flows.<br><br>
     * 런타임/특수/Role 권한 흐름을 처리하는 프로세서입니다.<br>
     */
    private val flowProcessor = PermissionFlowProcessor(
        host = host,
        classifier = classifier,
        runtimeHandler = runtimeHandler,
        specialHandler = specialHandler,
        roleHandler = roleHandler,
        resultAggregator = resultAggregator,
    )

    /**
     * Coordinator that serializes request processing and restoration.<br><br>
     * 요청 처리/복원을 직렬화하는 조정자입니다.<br>
     */
    private val coordinator = PermissionRequestCoordinator(
        queue = queue,
        stateSnapshot = stateSnapshot,
        requests = requests,
        inFlightWaiters = inFlightWaiters,
        scope = host.lifecycleOwner.lifecycleScope,
        flowProcessor = flowProcessor,
        resultAggregator = resultAggregator,
    )

    /**
     * Flag indicating restoreState was already applied.<br><br>
     * restoreState가 이미 적용되었는지 나타내는 플래그입니다.<br>
     */
    private var hasRestoredState: Boolean = false

    /**
     * Flag indicating request processing has started.<br><br>
     * 요청 처리가 시작되었는지 나타내는 플래그입니다.<br>
     */
    private var hasRequestStarted: Boolean = false

    /**
     * Flag indicating the coordinator worker has started.<br><br>
     * 코디네이터 워커가 시작되었는지 나타내는 플래그입니다.<br>
     */
    private var isCoordinatorStarted: Boolean = false

    /**
     * Restores internal state from [savedInstanceState].<br><br>
     * [savedInstanceState]에서 내부 상태를 복원합니다.<br>
     *
     * @param savedInstanceState Bundle containing saved state or null.<br><br>
     *                           저장된 상태를 담은 Bundle 또는 null입니다.<br>
     */
    fun restoreState(savedInstanceState: Bundle?) {
        if (hasRequestStarted) {
            Logx.w("PermissionRequester: restoreState ignored because requests already started.")
            return
        }
        if (hasRestoredState) {
            Logx.w("PermissionRequester: restoreState ignored because it was already applied.")
            return
        }
        hasRestoredState = true
        stateStore.restoreState(savedInstanceState)
        ensureCoordinatorStarted()
    }

    /**
     * Saves internal state into [outState].<br><br>
     * [outState]에 내부 상태를 저장합니다.<br>
     *
     * @param outState Bundle that receives the saved state.<br><br>
     *                 저장 상태를 기록할 Bundle입니다.<br>
     */
    fun saveState(outState: Bundle) {
        stateStore.saveState(outState)
    }

    /**
     * Requests a single permission and returns denied results via callback.<br><br>
     * 단일 권한을 요청하고 거부 결과를 콜백으로 반환합니다.<br>
     *
     * @param permission Permission string to request.<br><br>
     *                  요청할 권한 문자열입니다.<br>
     * @param onDeniedResult Callback invoked with denied items.<br><br>
     *                       거부 항목을 전달받는 콜백입니다.<br>
     * @param onRationaleNeeded Callback for rationale UI when needed.<br><br>
     *                          필요 시 설명 UI를 제공하는 콜백입니다.<br>
     * @param onNavigateToSettings Callback for settings navigation when needed.<br><br>
     *                             필요 시 설정 이동을 안내하는 콜백입니다.<br>
     */
    fun requestPermission(
        permission: String,
        onDeniedResult: (List<PermissionDeniedItem>) -> Unit,
        onRationaleNeeded: ((PermissionRationaleRequest) -> Unit)? = null,
        onNavigateToSettings: ((PermissionSettingsRequest) -> Unit)? = null,
    ) {
        requestPermissions(
            permissions = listOf(permission),
            onDeniedResult = onDeniedResult,
            onRationaleNeeded = onRationaleNeeded,
            onNavigateToSettings = onNavigateToSettings,
        )
    }

    /**
     * Requests multiple permissions and returns denied results via callback.<br><br>
     * 여러 권한을 요청하고 거부 결과를 콜백으로 반환합니다.<br>
     *
     * @param permissions Permissions to request.<br><br>
     *                    요청할 권한 목록입니다.<br>
     * @param onDeniedResult Callback invoked with denied items.<br><br>
     *                       거부 항목을 전달받는 콜백입니다.<br>
     * @param onRationaleNeeded Callback for rationale UI when needed.<br><br>
     *                          필요 시 설명 UI를 제공하는 콜백입니다.<br>
     * @param onNavigateToSettings Callback for settings navigation when needed.<br><br>
     *                             필요 시 설정 이동을 안내하는 콜백입니다.<br>
     */
    fun requestPermissions(
        permissions: List<String>,
        onDeniedResult: (List<PermissionDeniedItem>) -> Unit,
        onRationaleNeeded: ((PermissionRationaleRequest) -> Unit)? = null,
        onNavigateToSettings: ((PermissionSettingsRequest) -> Unit)? = null,
    ) {
        if (permissions.isEmpty()) {
            safeCatch {
                onDeniedResult(
                    listOf(
                        PermissionDeniedItem(
                            EMPTY_REQUEST_PERMISSION,
                            PermissionDeniedType.EMPTY_REQUEST,
                        ),
                    ),
                )
            }
            return
        }

        val normalizedPermissions = normalizePermissions(permissions)
        val invalidPermissions = normalizedPermissions.filter { classifier.isInvalid(it) }.toSet()
        if (!flowProcessor.isLifecycleRequestAllowed()) {
            safeCatch {
                onDeniedResult(
                    normalizedPermissions.map { permission ->
                        if (invalidPermissions.contains(permission)) {
                            PermissionDeniedItem(permission, PermissionDeniedType.MANIFEST_UNDECLARED)
                        } else {
                            PermissionDeniedItem(permission, PermissionDeniedType.LIFECYCLE_NOT_READY)
                        }
                    },
                )
            }
            return
        }

        hasRequestStarted = true
        ensureCoordinatorStarted()

        val requestId = UUID.randomUUID().toString()
        val results = mutableMapOf<String, PermissionDecisionType>()
        val pendingPermissions = mutableListOf<String>()

        for (permission in normalizedPermissions) {
            val type = classifier.classify(permission)
            if (classifier.isInvalid(permission)) {
                results[permission] = PermissionDecisionType.MANIFEST_UNDECLARED
                resultAggregator.logResult(
                    requestId = requestId,
                    permission = permission,
                    result = PermissionDecisionType.MANIFEST_UNDECLARED,
                )
                continue
            }
            when (type) {
                PermissionType.ROLE -> {
                    if (!roleHandler.isRoleAvailable(permission)) {
                        results[permission] = PermissionDecisionType.NOT_SUPPORTED
                        resultAggregator.logResult(
                            requestId = requestId,
                            permission = permission,
                            result = PermissionDecisionType.NOT_SUPPORTED,
                        )
                    } else if (roleHandler.isRoleHeld(permission)) {
                        results[permission] = PermissionDecisionType.GRANTED
                        resultAggregator.logResult(
                            requestId = requestId,
                            permission = permission,
                            result = PermissionDecisionType.GRANTED,
                        )
                    } else {
                        pendingPermissions.add(permission)
                    }
                }

                PermissionType.SPECIAL -> {
                    if (!classifier.isSupported(permission)) {
                        results[permission] = PermissionDecisionType.NOT_SUPPORTED
                        resultAggregator.logResult(
                            requestId = requestId,
                            permission = permission,
                            result = PermissionDecisionType.NOT_SUPPORTED,
                        )
                    } else if (specialHandler.isGranted(permission)) {
                        results[permission] = PermissionDecisionType.GRANTED
                        resultAggregator.logResult(
                            requestId = requestId,
                            permission = permission,
                            result = PermissionDecisionType.GRANTED,
                        )
                    } else {
                        pendingPermissions.add(permission)
                    }
                }

                PermissionType.RUNTIME -> {
                    if (!classifier.isSupported(permission)) {
                        results[permission] = PermissionDecisionType.GRANTED
                        resultAggregator.logResult(
                            requestId = requestId,
                            permission = permission,
                            result = PermissionDecisionType.GRANTED,
                        )
                    } else if (host.context.hasPermission(permission)) {
                        results[permission] = PermissionDecisionType.GRANTED
                        resultAggregator.logResult(
                            requestId = requestId,
                            permission = permission,
                            result = PermissionDecisionType.GRANTED,
                        )
                    } else {
                        pendingPermissions.add(permission)
                    }
                }
            }
        }

        val entry = RequestEntry(
            requestId = requestId,
            permissions = normalizedPermissions,
            results = results,
            isRestored = false,
            onDeniedResult = onDeniedResult,
            onRationaleNeeded = onRationaleNeeded,
            onNavigateToSettings = onNavigateToSettings,
        )

        resultAggregator.registerRequest(entry)

        if (pendingPermissions.isNotEmpty()) {
            resultAggregator.registerWaiters(requestId, pendingPermissions)
            coordinator.enqueueRequest(requestId)
        }

        resultAggregator.tryCompleteRequest(requestId)
    }

    /**
     * Returns and clears orphaned denied results after process restore.<br><br>
     * 프로세스 복원 후 orphaned 거부 결과를 반환하고 비웁니다.<br>
     *
     * @return Return value: list of orphaned denied request results. Log behavior: none.<br><br>
     *         반환값: orphaned 거부 요청 결과 목록입니다. 로그 동작: 없음.<br>
     */
    fun consumeOrphanedDeniedResults(): List<OrphanedDeniedRequestResult> =
        resultAggregator.consumeOrphanedDeniedResults()

    /**
     * Ensures the coordinator worker is started once.<br><br>
     * 코디네이터 워커가 한 번만 시작되도록 보장합니다.<br>
     */
    private fun ensureCoordinatorStarted() {
        if (isCoordinatorStarted) return
        coordinator.start()
        isCoordinatorStarted = true
    }

    /**
     * Normalizes permission list by removing duplicates while keeping order.<br><br>
     * 권한 목록의 중복을 제거하면서 순서를 유지합니다.<br>
     *
     * @param permissions Permissions to normalize.<br><br>
     *                    정규화할 권한 목록입니다.<br>
     * @return Return value: normalized permission list. Log behavior: none.<br><br>
     *         반환값: 정규화된 권한 목록입니다. 로그 동작: 없음.<br>
     */
    private fun normalizePermissions(permissions: List<String>): List<String> =
        permissions.distinct()
}
