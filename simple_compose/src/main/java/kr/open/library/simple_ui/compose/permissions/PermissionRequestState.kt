package kr.open.library.simple_ui.compose.permissions

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MainThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.permissions.classifier.PermissionClassifier
import kr.open.library.simple_ui.core.permissions.classifier.PermissionType
import kr.open.library.simple_ui.core.permissions.classifier.RuntimePermissionRequestability
import kr.open.library.simple_ui.core.permissions.extensions.hasPermission
import kr.open.library.simple_ui.core.permissions.handler.RolePermissionHandler
import kr.open.library.simple_ui.core.permissions.handler.SpecialPermissionHandler
import kr.open.library.simple_ui.core.permissions.model.PermissionDecisionType
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedItem
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedType
import kr.open.library.simple_ui.core.permissions.model.toDeniedTypeOrNull
import kr.open.library.simple_ui.core.permissions.queue.PermissionQueue
import kr.open.library.simple_ui.core.permissions.runtime.RuntimePermissionDecisionTracker
import kr.open.library.simple_ui.core.thread.assertMainThreadDebug

// ---------------------------------------------------------------------------
// 내부 상수
// Internal constants
// ---------------------------------------------------------------------------

private const val TAG = "PermissionRequestState"

/**
 * 빈 요청 결과에 사용하는 빈 권한 문자열입니다.<br>
 * Empty permission placeholder used for empty request results.<br>
 */
internal const val EMPTY_REQUEST_PERMISSION = ""

// ---------------------------------------------------------------------------
// 내부 순수 함수 (unit 테스트 대상)
// Internal pure functions (subject to unit testing)
// ---------------------------------------------------------------------------

/**
 * 결정 맵에서 거부 항목 목록을 요청 순서대로 생성합니다.<br>
 * Builds the denied item list from the decision map, preserving the request order.<br>
 *
 * 결정→거부 타입 변환은 simple_core의 [toDeniedTypeOrNull] 단일 출처를 사용합니다.<br>
 * The decision-to-denied-type conversion uses the single source [toDeniedTypeOrNull] in simple_core.<br>
 *
 * @param permissions 요청한 권한 목록 (순서 기준).<br><br>
 *                    Requested permission list that defines the order.<br>
 * @param results 권한별 내부 결정 상태 맵.<br><br>
 *                Map of internal decision states per permission.<br>
 * @return 거부 항목 목록. 전부 승인이면 빈 목록.<br><br>
 *         Denied item list, or an empty list when everything is granted.<br>
 */
internal fun toDeniedItems(
    permissions: List<String>,
    results: Map<String, PermissionDecisionType>,
): List<PermissionDeniedItem> = permissions.mapNotNull { permission ->
    // 미판정 권한은 xml(PermissionResultAggregator)과 동일하게 MANIFEST_UNDECLARED 기본값으로 기록한다
    // — 무음 스킵하면 거부 목록에서 빠져 승인된 것처럼 보인다
    // Unrecorded permissions default to MANIFEST_UNDECLARED, matching xml's
    // PermissionResultAggregator — silently skipping them would read as granted
    val decision = results[permission] ?: PermissionDecisionType.MANIFEST_UNDECLARED
    decision.toDeniedTypeOrNull()?.let { deniedType -> PermissionDeniedItem(permission, deniedType) }
}

// ---------------------------------------------------------------------------
// 공개 API
// Public API
// ---------------------------------------------------------------------------

/**
 * Compose 전용 권한 요청 상태입니다. 런타임/특수/Role 권한을 하나의 State 기반 API로 처리합니다.<br>
 * Compose-first permission request state that handles runtime, special, and role permissions
 * through a single State-based API.<br>
 *
 * 인스턴스는 [rememberPermissionRequestState]로만 생성합니다.<br>
 * Instances must be created via [rememberPermissionRequestState] only.<br>
 *
 * 결과 매핑 규칙(GRANTED/DENIED/PERMANENTLY_DENIED)은 simple_core의
 * [RuntimePermissionDecisionTracker] 단일 출처를 사용합니다.<br>
 * The result mapping rules (GRANTED/DENIED/PERMANENTLY_DENIED) come from the single source
 * [RuntimePermissionDecisionTracker] in simple_core.<br>
 *
 * **rationale 처리 / Rationale handling**:<br>
 * 설명 UI가 필요한 권한이 있으면 [request] 호출 시 시스템 다이얼로그를 띄우지 않고
 * [rationaleRequired] 상태만 갱신한 뒤 대기합니다.
 * 호출부는 설명 UI를 보여준 후 [continueRequest] 또는 [cancelRequest]를 호출해야 합니다.<br>
 * When some permissions need rationale UI, [request] does not launch the system dialog;
 * it only updates [rationaleRequired] and waits. The caller must show its rationale UI and
 * then invoke [continueRequest] or [cancelRequest].<br>
 *
 * **진행 중 중복 요청 / Duplicate requests while in flight**:<br>
 * 요청이 진행 중일 때 [request]를 다시 호출하면 경고 로그를 남기고 무시합니다.<br>
 * Calling [request] again while a request is in flight logs a warning and is ignored.<br>
 *
 * **스레드 계약 / Thread contract**:<br>
 * 모든 공개 메서드는 **메인 스레드**에서만 호출해야 합니다 (simple_xml PermissionRequester와 동일 계약).
 * Debug 빌드에서는 위반 시 즉시 실패합니다.<br>
 * Every public method must be called on the **main thread** (same contract as simple_xml's
 * PermissionRequester). Debug builds fail fast on violation.<br>
 *
 * **구성 변경 복원 / Configuration change restoration**:<br>
 * 요청 이력·대기 큐·진행 여부·rationale 대기 상태([rationaleRequired]와 그 대상 런타임 권한)는
 * `rememberSaveable`로 보존되어, rationale 대기 중 회전이 발생해도 흐름이 멈추지 않습니다.
 * 복원 후 도착한 결과는 콜백 없이 State([allGranted], [deniedItems])만 갱신하며,
 * [RuntimePermissionDecisionTracker.mapResult]에 `isRestored = true`로 전달되어
 * PERMANENTLY_DENIED 오판을 방지합니다.<br>
 * Request history, the pending queue, and the in-flight flag survive configuration changes via
 * `rememberSaveable`. Results delivered after restoration update only the State values
 * ([allGranted], [deniedItems]) without a callback, and are mapped with `isRestored = true`
 * to avoid PERMANENTLY_DENIED misjudgment.<br>
 */
@Stable
public class PermissionRequestState internal constructor(
    private val context: Context,
    private val activity: Activity?,
    permissions: List<String>,
    private val requestedHistory: MutableSet<String> = mutableSetOf(),
    private val queueBacking: MutableList<String> = mutableListOf(),
    isRestoredInFlight: Boolean = false,
    restoredPendingRuntime: List<String> = emptyList(),
    restoredRationaleRequired: List<String> = emptyList(),
    restoredResults: Map<String, PermissionDecisionType> = emptyMap(),
    private val gateSettingsNavigation: Boolean = false,
    restoredSettingsNavigationRequired: String? = null,
) {
    /**
     * 요청 대상 권한 목록 (중복 제거, 순서 유지).<br>
     * Permissions to request (deduplicated, order preserved).<br>
     */
    public val permissions: List<String> = permissions.distinct()

    /**
     * 권한 분류기 (런타임/특수/Role, 유효성 검증).<br>
     * Permission classifier (runtime/special/role, validity check).<br>
     */
    private val classifier = PermissionClassifier(context)

    /**
     * 특수 권한 상태 확인·설정 인텐트 생성 처리기.<br>
     * Handler for special permission checks and settings intents.<br>
     */
    private val specialHandler = SpecialPermissionHandler(context)

    /**
     * Role 가용성·보유 확인·요청 인텐트 생성 처리기.<br>
     * Handler for role availability, holding checks, and request intents.<br>
     */
    private val roleHandler = RolePermissionHandler(context)

    /**
     * 런타임 권한 판정 단일 출처 (simple_core).<br>
     * Single source of runtime permission decisions (simple_core).<br>
     */
    private val decisionTracker = RuntimePermissionDecisionTracker(requestedHistory)

    /**
     * 특수/Role 권한 순차 처리용 FIFO 큐.<br>
     * FIFO queue for sequential special/role permission processing.<br>
     */
    private val queue = PermissionQueue(queueBacking)

    /**
     * 런타임 권한 요청 launcher. 컴포지션에서 주입됩니다.<br>
     * Runtime permission launcher injected from the composition.<br>
     */
    internal var runtimeLauncher: ActivityResultLauncher<Array<String>>? = null

    /**
     * 특수/Role 권한 설정 화면 launcher. 컴포지션에서 주입됩니다.<br>
     * Settings screen launcher for special/role permissions, injected from the composition.<br>
     */
    internal var settingsLauncher: ActivityResultLauncher<Intent>? = null

    /**
     * 요청 진행 중 여부 (구성 변경 시 저장·복원).<br>
     * Whether a request is in flight (saved/restored across configuration changes).<br>
     */
    private var isInFlight: Boolean = isRestoredInFlight

    /**
     * 복원된 세션에서 진행 중이던 요청인지 여부. 복원 결과 판정 시 `isRestored`로 전달됩니다.<br>
     * Whether the in-flight request was restored. Passed as `isRestored` when mapping results.<br>
     */
    private var isRestoredSession: Boolean = isRestoredInFlight

    /**
     * 현재 요청의 결과 콜백. 구성 변경 시 소실됩니다.<br>
     * Result callback of the current request. Lost on configuration change.<br>
     */
    private var onResult: ((List<PermissionDeniedItem>) -> Unit)? = null

    /**
     * 현재 요청의 권한별 내부 결정 맵 (구성 변경 시 저장·복원 — xml RequestState.results 보존과 동일 수준).<br>
     * Per-permission internal decision map of the current request (saved/restored across
     * configuration changes, matching the preservation level of xml's RequestState.results).<br>
     */
    private val results = mutableMapOf<String, PermissionDecisionType>().apply { putAll(restoredResults) }

    /**
     * 런타임 다이얼로그 디스패치 대기 권한 목록 (rationale 대기 중 구성 변경 시 저장·복원).<br>
     * Runtime permissions awaiting dialog dispatch (saved/restored when a configuration
     * change happens while rationale is pending).<br>
     */
    private var pendingRuntime: List<String> = restoredPendingRuntime

    /**
     * 런타임 launch 직전에 캡처한 권한별 사전 요청 이력 스냅샷.<br>
     * Per-permission request-history snapshot captured right before the runtime launch.<br>
     */
    private var wasRequestedBeforeSnapshot: Map<String, Boolean> = emptyMap()

    private val allGrantedState = mutableStateOf(computeAllGranted())
    private val deniedItemsState = mutableStateOf<List<PermissionDeniedItem>>(emptyList())
    private val rationaleRequiredState = mutableStateOf(restoredRationaleRequired)
    private val settingsNavigationRequiredState = mutableStateOf(restoredSettingsNavigationRequired)

    /**
     * 모든 권한이 허용된 상태인지 여부 (State 기반, 재구성 트리거).<br>
     * Whether every permission is granted (State-backed, triggers recomposition).<br>
     *
     * 초기값은 생성 시점의 권한 보유 상태로 계산되며, 요청 완료 시 재계산됩니다.<br>
     * The initial value is computed from the grant status at creation time and is
     * recomputed when a request completes.<br>
     */
    public val allGranted: Boolean
        get() = allGrantedState.value

    /**
     * 마지막 요청의 거부 항목 목록 (State 기반).<br>
     * Denied items of the last request (State-backed).<br>
     */
    public val deniedItems: List<PermissionDeniedItem>
        get() = deniedItemsState.value

    /**
     * 설명 UI가 필요한 권한 목록 (State 기반).<br>
     * Permissions that currently require rationale UI (State-backed).<br>
     *
     * 비어 있지 않으면 요청 흐름이 일시 정지된 상태이며,
     * [continueRequest] 또는 [cancelRequest] 호출로 재개/취소해야 합니다.<br>
     * When non-empty, the request flow is paused and must be resumed or cancelled via
     * [continueRequest] or [cancelRequest].<br>
     */
    public val rationaleRequired: List<String>
        get() = rationaleRequiredState.value

    /**
     * 설정 화면 이동 동의를 대기 중인 특수/Role 권한 (State 기반).<br>
     * Special/role permission awaiting consent for settings navigation (State-backed).<br>
     *
     * [rememberPermissionRequestState]의 `gateSettingsNavigation = true`일 때만 사용됩니다.
     * null이 아니면 설정 화면 이동 직전에 흐름이 일시 정지된 상태이며,
     * [continueSettingsNavigation] 또는 [cancelSettingsNavigation] 호출로 재개/취소해야 합니다.
     * simple_xml의 `onNavigateToSettings` 동의 게이트와 동일한 의미입니다.<br>
     * Used only when `gateSettingsNavigation = true` in [rememberPermissionRequestState].
     * When non-null, the flow is paused right before navigating to the settings screen and must be
     * resumed or cancelled via [continueSettingsNavigation] or [cancelSettingsNavigation].
     * This matches the semantics of simple_xml's `onNavigateToSettings` consent gate.<br>
     */
    public val settingsNavigationRequired: String?
        get() = settingsNavigationRequiredState.value

    /**
     * 권한 요청을 시작하고 거부 결과를 [onResult] 콜백으로 전달합니다.<br>
     * Starts the permission request and delivers denied results via [onResult].<br>
     *
     * 전부 승인되면 빈 목록이 전달됩니다.
     * 빈 권한 목록 요청은 [PermissionDeniedType.EMPTY_REQUEST] 한 건으로 즉시 종료됩니다.
     * 설명 UI가 필요한 권한이 있으면 [rationaleRequired]만 갱신하고 대기합니다.<br>
     * An empty denied list means everything was granted.
     * An empty permission list completes immediately with one [PermissionDeniedType.EMPTY_REQUEST] item.
     * When rationale is needed, only [rationaleRequired] is updated and the flow waits.<br>
     *
     * @param onResult 거부 항목 목록을 전달받는 콜백. 구성 변경 시 소실되며,
     *                 그 경우 State만 갱신됩니다.<br><br>
     *                 Callback receiving the denied items. It is lost on configuration change,
     *                 in which case only the State values are updated.<br>
     */
    @MainThread
    public fun request(onResult: (List<PermissionDeniedItem>) -> Unit) {
        assertMainThreadDebug("PermissionRequestState.request")
        if (isInFlight) {
            Logx.w(TAG, "request ignored: a permission request is already in flight.")
            return
        }
        if (permissions.isEmpty()) {
            val denied = listOf(PermissionDeniedItem(EMPTY_REQUEST_PERMISSION, PermissionDeniedType.EMPTY_REQUEST))
            deniedItemsState.value = denied
            safeCatch { onResult(denied) }
            return
        }

        isInFlight = true
        isRestoredSession = false
        this.onResult = onResult
        results.clear()

        val pendingRuntimeList = mutableListOf<String>()
        for (permission in permissions) {
            val decision = resolveImmediately(permission)
            if (decision != null) {
                results[permission] = decision
            } else {
                when (classifier.classify(permission)) {
                    PermissionType.RUNTIME -> pendingRuntimeList.add(permission)
                    else -> queue.enqueue(permission)
                }
            }
        }
        pendingRuntime = pendingRuntimeList

        if (pendingRuntime.isEmpty() && queue.isEmpty()) {
            complete()
            return
        }

        val needsRationale = activity
            ?.let { host ->
                pendingRuntime.filter { host.shouldShowRequestPermissionRationale(it) }
            }.orEmpty()
        if (needsRationale.isNotEmpty()) {
            rationaleRequiredState.value = needsRationale
            return
        }
        launchRuntimeOrQueue()
    }

    /**
     * 설명 UI 표시 후 권한 요청 흐름을 계속 진행합니다.<br>
     * Continues the permission request flow after the rationale UI was shown.<br>
     *
     * [rationaleRequired]가 비어 있으면 경고 로그만 남기고 무시합니다.<br>
     * Ignored with a warning log when [rationaleRequired] is empty.<br>
     */
    @MainThread
    public fun continueRequest() {
        assertMainThreadDebug("PermissionRequestState.continueRequest")
        if (rationaleRequiredState.value.isEmpty()) {
            Logx.w(TAG, "continueRequest ignored: no rationale is pending.")
            return
        }
        rationaleRequiredState.value = emptyList()
        // 사용자가 명시적으로 계속한 새 launch의 결과는 신뢰 가능한 스냅샷이 있으므로
        // 복원 보호(isRestored)를 해제한다 — 유지하면 PERMANENTLY_DENIED가 DENIED로 다운그레이드됨
        // A fresh launch explicitly continued by the user has a reliable snapshot, so the
        // restoration guard is lifted — keeping it would downgrade PERMANENTLY_DENIED to DENIED
        isRestoredSession = false
        launchRuntimeOrQueue()
    }

    /**
     * 설명 UI 단계에서 권한 요청 흐름 전체를 취소합니다.<br>
     * Cancels the whole permission request flow at the rationale stage.<br>
     *
     * 미결 권한(런타임·특수·Role)은 모두 [PermissionDeniedType.DENIED]로 처리됩니다.
     * [rationaleRequired]가 비어 있으면 경고 로그만 남기고 무시합니다.<br>
     * All pending permissions (runtime, special, role) are treated as [PermissionDeniedType.DENIED].
     * Ignored with a warning log when [rationaleRequired] is empty.<br>
     */
    @MainThread
    public fun cancelRequest() {
        assertMainThreadDebug("PermissionRequestState.cancelRequest")
        if (rationaleRequiredState.value.isEmpty()) {
            Logx.w(TAG, "cancelRequest ignored: no rationale is pending.")
            return
        }
        rationaleRequiredState.value = emptyList()
        pendingRuntime.forEach { results[it] = PermissionDecisionType.DENIED }
        pendingRuntime = emptyList()
        queue.asList().forEach { results[it] = PermissionDecisionType.DENIED }
        queue.clear()
        complete()
    }

    /**
     * 설정 화면 이동 동의 후 해당 권한의 설정 화면을 실행합니다.<br>
     * Launches the settings screen for the pending permission after the user consented.<br>
     *
     * [settingsNavigationRequired]가 null이면 경고 로그만 남기고 무시합니다.
     * 인텐트 생성/실행 실패 시 해당 권한을 실패로 기록하고 다음 큐 항목으로 진행합니다.<br>
     * Ignored with a warning log when [settingsNavigationRequired] is null.
     * On intent creation/launch failure the permission is recorded as failed and the
     * queue advances to the next entry.<br>
     */
    @MainThread
    public fun continueSettingsNavigation() {
        assertMainThreadDebug("PermissionRequestState.continueSettingsNavigation")
        val permission = settingsNavigationRequiredState.value
        if (permission == null) {
            Logx.w(TAG, "continueSettingsNavigation ignored: no settings navigation is pending.")
            return
        }
        settingsNavigationRequiredState.value = null
        if (!launchSettings(permission)) {
            // 실패가 기록되고 큐에서 제거됨 — 다음 항목 진행 (다음 항목은 다시 게이트에서 대기)
            // Failure recorded and removed from the queue — advance (the next entry gates again)
            processNextInQueue()
        }
    }

    /**
     * 설정 화면 이동을 거절하고 해당 권한만 [PermissionDeniedType.DENIED]로 처리합니다.<br>
     * Declines the settings navigation and marks only that permission as [PermissionDeniedType.DENIED].<br>
     *
     * simple_xml의 `onNavigateToSettings` cancel과 동일하게 **해당 권한만** 거부 처리하며,
     * 큐의 나머지 특수/Role 권한은 계속 진행됩니다(다음 항목은 다시 게이트에서 대기).
     * [settingsNavigationRequired]가 null이면 경고 로그만 남기고 무시합니다.<br>
     * Matching simple_xml's `onNavigateToSettings` cancel, **only that permission** is denied and
     * the remaining queued special/role permissions continue (the next entry gates again).
     * Ignored with a warning log when [settingsNavigationRequired] is null.<br>
     */
    @MainThread
    public fun cancelSettingsNavigation() {
        assertMainThreadDebug("PermissionRequestState.cancelSettingsNavigation")
        val permission = settingsNavigationRequiredState.value
        if (permission == null) {
            Logx.w(TAG, "cancelSettingsNavigation ignored: no settings navigation is pending.")
            return
        }
        settingsNavigationRequiredState.value = null
        results[permission] = PermissionDecisionType.DENIED
        queue.remove(permission)
        processNextInQueue()
    }

    /**
     * 런타임 권한 다이얼로그 결과를 처리합니다.<br>
     * Handles the runtime permission dialog result.<br>
     *
     * 판정은 [RuntimePermissionDecisionTracker.mapResult]에 위임하며,
     * 복원된 세션이면 `isRestored = true`로 전달합니다.<br>
     * Decision mapping is delegated to [RuntimePermissionDecisionTracker.mapResult];
     * `isRestored = true` is passed for restored sessions.<br>
     *
     * @param resultMap 플랫폼이 반환한 권한별 승인 여부 맵.<br><br>
     *                  Per-permission grant map returned by the platform.<br>
     */
    internal fun handleRuntimeResult(resultMap: Map<String, Boolean>) {
        if (!isInFlight) {
            // 늦은/중복 결과가 직전 요청의 deniedItems를 오염시키지 않도록 차단
            // Guard against late/duplicate results polluting the previous request's deniedItems
            Logx.w(TAG, "handleRuntimeResult ignored: no request is in flight.")
            return
        }
        val restored = isRestoredSession
        // 다이얼로그 중단 등으로 결과 맵이 비거나 일부 권한이 누락돼도, 디스패치했던 모든 권한을
        // 보유 상태 재확인으로 반드시 기록한다 (무기록 완료 방지)
        // Even when the result map is empty or partial (e.g., dialog interruption), every
        // dispatched permission is recorded by re-checking its grant status (no silent omission)
        val targets = (pendingRuntime + resultMap.keys).distinct()
        val missing = targets.filterNot { resultMap.containsKey(it) }
        if (missing.isNotEmpty()) {
            Logx.w(TAG, "runtime result is missing for $missing — re-checking grant status.")
        }
        for (permission in targets) {
            val granted = resultMap[permission] ?: context.hasPermission(permission)
            val shouldShowRationale = activity?.shouldShowRequestPermissionRationale(permission) ?: false
            val wasRequestedBefore =
                wasRequestedBeforeSnapshot[permission] ?: decisionTracker.wasRequested(permission)
            results[permission] = decisionTracker.mapResult(
                permission = permission,
                granted = granted,
                shouldShowRationale = shouldShowRationale,
                wasRequestedBefore = wasRequestedBefore,
                isRestored = restored,
            )
        }
        pendingRuntime = emptyList()
        wasRequestedBeforeSnapshot = emptyMap()
        processNextInQueue()
    }

    /**
     * 특수/Role 권한 설정 화면 복귀 결과를 처리합니다.<br>
     * Handles the result of returning from the special/role settings screen.<br>
     *
     * ActivityResult의 resultCode는 신뢰할 수 없으므로 권한 보유 상태를 재확인하여 판정합니다.<br>
     * The ActivityResult resultCode is unreliable, so the grant status is re-checked instead.<br>
     */
    internal fun handleSettingsResult() {
        if (!isInFlight) {
            // 늦은/중복 결과가 직전 요청의 deniedItems를 오염시키지 않도록 차단
            // Guard against late/duplicate results polluting the previous request's deniedItems
            Logx.w(TAG, "handleSettingsResult ignored: no request is in flight.")
            return
        }
        val permission = queue.peek()
        if (permission == null) {
            Logx.w(TAG, "handleSettingsResult: queue is empty, completing defensively.")
            complete()
            return
        }
        val granted = when (classifier.classify(permission)) {
            PermissionType.ROLE -> roleHandler.isRoleHeld(permission)
            else -> specialHandler.isGranted(permission)
        }
        results[permission] =
            if (granted) PermissionDecisionType.GRANTED else PermissionDecisionType.DENIED
        queue.remove(permission)
        processNextInQueue()
    }

    /**
     * 요청 전에 즉시 판정 가능한 권한을 해결합니다. simple_xml PermissionRequester와 동일한 의미 체계입니다.<br>
     * Resolves permissions that can be decided before dispatch, matching the semantics of
     * the simple_xml PermissionRequester.<br>
     *
     * @param permission 판정할 권한 문자열.<br><br>
     *                   Permission string to resolve.<br>
     * @return 즉시 판정된 결정, 런타임 디스패치 또는 설정 이동이 필요하면 `null`.<br><br>
     *         Immediate decision, or `null` when dialog dispatch or settings navigation is needed.<br>
     */
    private fun resolveImmediately(permission: String): PermissionDecisionType? {
        if (classifier.isInvalid(permission)) {
            Logx.w(TAG, "permission is not declared in the manifest: $permission")
            return PermissionDecisionType.MANIFEST_UNDECLARED
        }
        return when (classifier.classify(permission)) {
            PermissionType.ROLE -> when {
                !roleHandler.isRoleAvailable(permission) -> PermissionDecisionType.NOT_SUPPORTED
                roleHandler.isRoleHeld(permission) -> PermissionDecisionType.GRANTED
                else -> null
            }

            PermissionType.SPECIAL -> when {
                !classifier.isSupported(permission) -> PermissionDecisionType.NOT_SUPPORTED
                specialHandler.isGranted(permission) -> PermissionDecisionType.GRANTED
                else -> null
            }

            PermissionType.RUNTIME -> {
                if (!classifier.isSupported(permission)) return PermissionDecisionType.GRANTED
                when (classifier.getRuntimeRequestability(permission)) {
                    RuntimePermissionRequestability.GRANTED_BY_DEFAULT -> PermissionDecisionType.GRANTED
                    RuntimePermissionRequestability.NOT_SUPPORTED -> PermissionDecisionType.NOT_SUPPORTED
                    RuntimePermissionRequestability.REQUESTABLE ->
                        if (context.hasPermission(permission)) PermissionDecisionType.GRANTED else null
                }
            }
        }
    }

    /**
     * 런타임 다이얼로그를 실행하거나, 런타임 미결이 없으면 특수/Role 큐 처리로 넘어갑니다.<br>
     * Launches the runtime dialog, or moves on to the special/role queue when nothing is pending.<br>
     */
    private fun launchRuntimeOrQueue() {
        if (pendingRuntime.isEmpty()) {
            processNextInQueue()
            return
        }
        val launcher = runtimeLauncher
        wasRequestedBeforeSnapshot = pendingRuntime.associateWith { decisionTracker.wasRequested(it) }
        decisionTracker.markRequested(pendingRuntime)
        val launched = launcher != null &&
            safeCatch(defaultValue = false) {
                launcher.launch(pendingRuntime.toTypedArray())
                true
            }
        if (!launched) {
            Logx.w(TAG, "runtime permission launcher is not ready or failed to launch: $pendingRuntime")
            pendingRuntime.forEach { results[it] = PermissionDecisionType.LIFECYCLE_NOT_READY }
            pendingRuntime = emptyList()
            wasRequestedBeforeSnapshot = emptyMap()
            processNextInQueue()
        }
    }

    /**
     * 큐의 다음 특수/Role 권한에 대한 설정 인텐트를 실행합니다. 큐가 비면 요청을 완료합니다.<br>
     * Launches the settings intent for the next queued special/role permission.
     * Completes the request when the queue becomes empty.<br>
     *
     * 인텐트 생성 실패는 simple_xml과 동일하게 [PermissionDeniedType.NOT_SUPPORTED]로,
     * launcher 실행 실패는 [PermissionDeniedType.FAILED_TO_LAUNCH_SETTINGS]로 처리합니다.<br>
     * Intent creation failures are treated as [PermissionDeniedType.NOT_SUPPORTED]
     * (matching simple_xml), and launcher failures as
     * [PermissionDeniedType.FAILED_TO_LAUNCH_SETTINGS].<br>
     */
    private fun processNextInQueue() {
        while (true) {
            val permission = queue.peek() ?: break
            if (gateSettingsNavigation) {
                // 설정 화면 이동 전 동의 게이트 — simple_xml의 onNavigateToSettings와 동일 의미.
                // continueSettingsNavigation()/cancelSettingsNavigation() 호출까지 일시 정지
                // Consent gate before settings navigation — same semantics as simple_xml's
                // onNavigateToSettings. Pauses until continue/cancelSettingsNavigation() is called
                settingsNavigationRequiredState.value = permission
                return
            }
            if (launchSettings(permission)) return
            // 실패가 기록되고 큐에서 제거됨 — 다음 항목으로 진행
            // Failure recorded and removed from the queue — advance to the next entry
        }
        complete()
    }

    /**
     * [permission]의 설정/Role 인텐트를 생성해 실행합니다.<br>
     * Builds and launches the settings/role intent for [permission].<br>
     *
     * 인텐트 생성 실패는 simple_xml과 동일하게 [PermissionDeniedType.NOT_SUPPORTED]로,
     * launcher 실행 실패는 [PermissionDeniedType.FAILED_TO_LAUNCH_SETTINGS]로 기록하고
     * 큐에서 제거합니다.<br>
     * Intent creation failures are recorded as [PermissionDeniedType.NOT_SUPPORTED]
     * (matching simple_xml) and launcher failures as
     * [PermissionDeniedType.FAILED_TO_LAUNCH_SETTINGS]; both remove the entry from the queue.<br>
     *
     * @param permission 실행할 특수/Role 권한.<br><br>
     *                   Special/role permission to launch.<br>
     * @return 실행에 성공해 결과 대기 상태면 `true`, 실패로 기록·제거됐으면 `false`.<br><br>
     *         `true` when launched and awaiting the result, `false` when recorded as failed.<br>
     */
    private fun launchSettings(permission: String): Boolean {
        val intent = when (classifier.classify(permission)) {
            PermissionType.ROLE -> roleHandler.createRequestIntent(permission)
            else -> specialHandler.buildSettingsIntent(permission)
        }
        if (intent == null) {
            // xml(PermissionFlowProcessor)과 동일 의미: 인텐트를 만들 수 없는 권한은 미지원으로 판정
            // Same semantics as xml (PermissionFlowProcessor): no intent → not supported
            Logx.w(TAG, "failed to build settings intent for: $permission")
            results[permission] = PermissionDecisionType.NOT_SUPPORTED
            queue.remove(permission)
            return false
        }
        val launcher = settingsLauncher
        val launched = launcher != null &&
            safeCatch(defaultValue = false) {
                launcher.launch(intent)
                true
            }
        if (!launched) {
            Logx.w(TAG, "settings launcher is not ready or failed to launch for: $permission")
            results[permission] = PermissionDecisionType.FAILED_TO_LAUNCH_SETTINGS
            queue.remove(permission)
            return false
        }
        return true
    }

    /**
     * 요청을 완료하고 State 갱신 및 콜백 호출을 수행합니다.<br>
     * Completes the request, updating the State values and invoking the callback.<br>
     */
    private fun complete() {
        val denied = toDeniedItems(permissions, results)
        deniedItemsState.value = denied
        allGrantedState.value = computeAllGranted()
        rationaleRequiredState.value = emptyList()
        settingsNavigationRequiredState.value = null
        results.clear()
        val callback = onResult
        onResult = null
        isInFlight = false
        isRestoredSession = false
        callback?.let { safeCatch { it(denied) } }
    }

    /**
     * 권한 분류별 보유 상태를 종합하여 전체 승인 여부를 계산합니다.<br>
     * Computes whether every permission is granted, using the appropriate check per category.<br>
     *
     * 런타임은 [hasPermission], 특수는 [SpecialPermissionHandler.isGranted],
     * Role은 [RolePermissionHandler.isRoleHeld]로 판정합니다. 빈 목록은 `true`입니다.<br>
     * Runtime uses [hasPermission], special uses [SpecialPermissionHandler.isGranted],
     * and role uses [RolePermissionHandler.isRoleHeld]. An empty list yields `true`.<br>
     */
    private fun computeAllGranted(): Boolean = permissions.all { permission ->
        when (classifier.classify(permission)) {
            PermissionType.ROLE -> roleHandler.isRoleHeld(permission)
            PermissionType.SPECIAL -> specialHandler.isGranted(permission)
            PermissionType.RUNTIME -> context.hasPermission(permission)
        }
    }

    /**
     * Saver 생성용 companion 컨테이너.<br>
     * Companion container providing the Saver factory.<br>
     */
    internal companion object {
        /**
         * 요청 이력·대기 큐·진행 여부를 저장/복원하는 [Saver]를 생성합니다.<br>
         * Creates a [Saver] that persists the request history, pending queue, and in-flight flag.<br>
         *
         * @param context 복원 시 상태 생성에 사용할 컨텍스트.<br><br>
         *                Context used to recreate the state on restoration.<br>
         * @param activity 복원 시 rationale 판정에 사용할 Activity (없으면 null).<br><br>
         *                 Activity used for rationale checks on restoration, or null.<br>
         * @param permissions 요청 대상 권한 목록.<br><br>
         *                    Permissions to request.<br>
         */
        internal fun saver(
            context: Context,
            activity: Activity?,
            permissions: List<String>,
            gateSettingsNavigation: Boolean,
        ): Saver<PermissionRequestState, Any> = listSaver(
            save = { state ->
                listOf(
                    ArrayList(state.requestedHistory),
                    ArrayList(state.queueBacking),
                    state.isInFlight,
                    ArrayList(state.pendingRuntime),
                    ArrayList(state.rationaleRequiredState.value),
                    ArrayList(state.results.keys),
                    ArrayList(state.results.values.map { it.name }),
                    state.settingsNavigationRequiredState.value ?: "",
                )
            },
            restore = { saved ->
                @Suppress("UNCHECKED_CAST")
                val resultKeys = saved[5] as ArrayList<String>

                @Suppress("UNCHECKED_CAST")
                val resultNames = saved[6] as ArrayList<String>
                val restoredResults = resultKeys.indices
                    .mapNotNull { index ->
                        val name = resultNames.getOrNull(index) ?: return@mapNotNull null
                        val type = PermissionDecisionType.entries.firstOrNull { it.name == name }
                            ?: return@mapNotNull null
                        resultKeys[index] to type
                    }.toMap()

                @Suppress("UNCHECKED_CAST")
                PermissionRequestState(
                    context = context,
                    activity = activity,
                    permissions = permissions,
                    requestedHistory = (saved[0] as ArrayList<String>).toMutableSet(),
                    queueBacking = (saved[1] as ArrayList<String>).toMutableList(),
                    isRestoredInFlight = saved[2] as Boolean,
                    restoredPendingRuntime = (saved[3] as ArrayList<String>).toList(),
                    restoredRationaleRequired = (saved[4] as ArrayList<String>).toList(),
                    restoredResults = restoredResults,
                    gateSettingsNavigation = gateSettingsNavigation,
                    restoredSettingsNavigationRequired = (saved[7] as String).ifEmpty { null },
                )
            },
        )
    }
}

/**
 * [PermissionRequestState]를 생성하고 컴포지션 생명주기에 연결합니다.<br>
 * Creates a [PermissionRequestState] and wires it into the composition lifecycle.<br>
 *
 * 초기 [PermissionRequestState.allGranted]는 [Context.hasPermission] 기반으로 계산됩니다.
 * 요청 이력·대기 큐·진행 여부는 `rememberSaveable`로 구성 변경에 걸쳐 보존됩니다.<br>
 * The initial [PermissionRequestState.allGranted] is computed via [Context.hasPermission].
 * The request history, pending queue, and in-flight flag survive configuration changes
 * through `rememberSaveable`.<br>
 *
 * **사용 예시 / Usage example**:
 * ```kotlin
 * val permissionState = rememberPermissionRequestState(
 *     listOf(Manifest.permission.CAMERA),
 * )
 * Button(onClick = { permissionState.request { denied -> /* 처리 */ } }) {
 *     Text(if (permissionState.allGranted) "Granted" else "Request")
 * }
 * if (permissionState.rationaleRequired.isNotEmpty()) {
 *     // 설명 UI 표시 후 permissionState.continueRequest() 또는 cancelRequest()
 * }
 * ```
 *
 * **설정 화면 이동 동의 게이트 / Settings navigation consent gate**:<br>
 * 기본값([gateSettingsNavigation] = false)에서는 특수/Role 권한 요청 시 설정 화면으로 **즉시 이동**합니다
 * (simple_xml에서 `onNavigateToSettings` 콜백을 전달하지 않은 경우와 동일한 동작).
 * `true`로 설정하면 이동 직전에 [PermissionRequestState.settingsNavigationRequired]가 노출되고,
 * [PermissionRequestState.continueSettingsNavigation] / [PermissionRequestState.cancelSettingsNavigation]
 * 호출까지 일시 정지합니다 (simple_xml의 `onNavigateToSettings` 동의 게이트와 동일한 의미).<br>
 * With the default ([gateSettingsNavigation] = false), special/role permission requests navigate to
 * the settings screen **immediately** (same as simple_xml without an `onNavigateToSettings` callback).
 * When `true`, [PermissionRequestState.settingsNavigationRequired] is exposed right before navigation
 * and the flow pauses until [PermissionRequestState.continueSettingsNavigation] /
 * [PermissionRequestState.cancelSettingsNavigation] is called (matching simple_xml's consent gate).<br>
 *
 * @param permissions 요청할 권한 목록. 변경되면 새 상태가 생성됩니다.<br><br>
 *                    Permissions to request. A new state is created when the list changes.<br>
 * @param gateSettingsNavigation 특수/Role 설정 화면 이동 전 동의 게이트 사용 여부. 기본값 false(즉시 이동).<br><br>
 *                               Whether to gate settings navigation behind user consent.
 *                               Defaults to false (immediate navigation).<br>
 * @return 권한 요청 상태 객체.<br><br>
 *         The permission request state.<br>
 */
@Composable
public fun rememberPermissionRequestState(
    permissions: List<String>,
    gateSettingsNavigation: Boolean = false,
): PermissionRequestState {
    val context = LocalContext.current
    val activity = LocalActivity.current

    val state = rememberSaveable(
        permissions,
        gateSettingsNavigation,
        saver = PermissionRequestState.saver(context, activity, permissions, gateSettingsNavigation),
    ) {
        PermissionRequestState(
            context = context,
            activity = activity,
            permissions = permissions,
            gateSettingsNavigation = gateSettingsNavigation,
        )
    }

    val runtimeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { resultMap ->
        state.handleRuntimeResult(resultMap)
    }
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        state.handleSettingsResult()
    }

    // launcher는 컴포지션 이탈 시 해제 — 이탈 후 launch 시도는 LIFECYCLE_NOT_READY로 처리됨
    // Launchers are detached on disposal — launches afterwards resolve to LIFECYCLE_NOT_READY
    DisposableEffect(state, runtimeLauncher, settingsLauncher) {
        state.runtimeLauncher = runtimeLauncher
        state.settingsLauncher = settingsLauncher
        onDispose {
            state.runtimeLauncher = null
            state.settingsLauncher = null
        }
    }

    return state
}
