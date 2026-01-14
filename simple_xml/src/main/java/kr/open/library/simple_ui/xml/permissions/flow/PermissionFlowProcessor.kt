package kr.open.library.simple_ui.xml.permissions.flow

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.permissions.classifier.PermissionClassifier
import kr.open.library.simple_ui.core.permissions.classifier.PermissionType
import kr.open.library.simple_ui.core.permissions.extentions.hasPermission
import kr.open.library.simple_ui.core.permissions.handler.RolePermissionHandler
import kr.open.library.simple_ui.core.permissions.handler.SpecialPermissionHandler
import kr.open.library.simple_ui.core.permissions.model.PermissionRationaleRequest
import kr.open.library.simple_ui.core.permissions.model.PermissionDecisionType
import kr.open.library.simple_ui.core.permissions.model.PermissionSettingsRequest
import kr.open.library.simple_ui.xml.permissions.coordinator.RequestEntry
import kr.open.library.simple_ui.xml.permissions.host.PermissionHostAdapter
import kr.open.library.simple_ui.xml.permissions.result.PermissionResultAggregator
import kotlin.coroutines.resume

/**
 * Processes runtime, special, and role permission flows.<br><br>
 * 런타임/특수/Role 권한 흐름을 처리합니다.<br>
 *
 * @param host Host adapter supplying Activity/Fragment capabilities.<br><br>
 *             Activity/Fragment 기능을 제공하는 호스트 어댑터입니다.<br>
 * @param classifier Permission classifier for runtime/special/role decisions.<br><br>
 *                   런타임/특수/Role 판단에 사용하는 권한 분류기입니다.<br>
 * @param runtimeHandler Handler for runtime permission rationale and mapping.<br><br>
 *                       런타임 권한 설명/결과 매핑 처리기입니다.<br>
 * @param specialHandler Handler for special permission checks and intents.<br><br>
 *                       특수 권한 확인/인텐트 처리기입니다.<br>
 * @param roleHandler Handler for role availability and intents.<br><br>
 *                    Role 사용 가능 여부/인텐트 처리기입니다.<br>
 * @param resultAggregator Aggregator for result updates and completion.<br><br>
 *                         결과 갱신/완료 처리를 담당하는 집계기입니다.<br>
 */
internal class PermissionFlowProcessor(
    private val host: PermissionHostAdapter,
    private val classifier: PermissionClassifier,
    private val runtimeHandler: RuntimePermissionHandler,
    private val specialHandler: SpecialPermissionHandler,
    private val roleHandler: RolePermissionHandler,
    private val resultAggregator: PermissionResultAggregator,
) {
    /**
     * Snapshot of "requested before" flags for the current runtime request.<br><br>
     * 현재 런타임 요청에 대한 이전 요청 여부 스냅샷입니다.<br>
     */
    private var runtimeRequestedBefore: Map<String, Boolean> = emptyMap()

    /**
     * Tracks the currently launched settings/role activity action.<br><br>
     * 현재 실행 중인 설정/Role 액션을 추적합니다.<br>
     */
    private var currentActivityAction: InFlightActivityAction? = null

    /**
     * Flag indicating a special permission result should be checked on resume.<br><br>
     * onResume 시 특수 권한 결과 재확인이 필요한지 나타내는 플래그입니다.<br>
     */
    private var awaitingSpecialReturn: Boolean = false

    /**
     * Flag indicating the host actually left the screen for special permission settings.<br><br>
     * 특수 권한 설정 화면으로 실제 이탈했는지 여부를 나타내는 플래그입니다.<br>
     */
    private var didLeaveForSpecial: Boolean = false

    /**
     * Deferred completion for runtime permission requests.<br><br>
     * 런타임 권한 요청 완료를 기다리는 디퍼드입니다.<br>
     */
    private var runtimeCompletion: CompletableDeferred<Unit>? = null

    /**
     * Deferred completion for special permission requests.<br><br>
     * 특수 권한 요청 완료를 기다리는 디퍼드입니다.<br>
     */
    private var specialCompletion: CompletableDeferred<Unit>? = null

    /**
     * Deferred completion for role permission requests.<br><br>
     * Role 권한 요청 완료를 기다리는 디퍼드입니다.<br>
     */
    private var roleCompletion: CompletableDeferred<Unit>? = null

    /**
     * Lifecycle observer that rechecks special permissions on resume.<br><br>
     * onResume 시 특수 권한을 재확인하는 라이프사이클 옵저버입니다.<br>
     */
    private val resumeObserver: DefaultLifecycleObserver =
        object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                if (awaitingSpecialReturn && didLeaveForSpecial) {
                    handleSpecialPermissionReturn()
                }
            }

            override fun onStop(owner: LifecycleOwner) {
                if (awaitingSpecialReturn) {
                    didLeaveForSpecial = true
                }
            }
        }

    /**
     * ActivityResult launcher for runtime permission dialogs.<br><br>
     * 런타임 권한 다이얼로그 실행용 ActivityResult 런처입니다.<br>
     */
    private val runtimePermissionsLauncher: ActivityResultLauncher<Array<String>> =
        host.activityResultCaller.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { results ->
            handleRuntimePermissionsResult(results)
        }

    /**
     * ActivityResult launcher for settings/role intents.<br><br>
     * 설정/Role 인텐트 실행용 ActivityResult 런처입니다.<br>
     */
    private val activityLauncher: ActivityResultLauncher<Intent> =
        host.activityResultCaller.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) {
            handleActivityResultReturn()
        }

    /**
     * Initializes lifecycle observers for special permission returns.<br><br>
     * 특수 권한 복귀 처리를 위한 라이프사이클 옵저버를 초기화합니다.<br>
     */
    init {
        host.lifecycleOwner.lifecycle.addObserver(resumeObserver)
    }

    /**
     * Returns whether the lifecycle is ready to accept new requests.<br><br>
     * 라이프사이클이 새 요청을 수용할 준비가 되었는지 반환합니다.<br>
     *
     * @return Return value: true when lifecycle is at least CREATED. Log behavior: none.<br><br>
     *         반환값: CREATED 이상이면 true입니다. 로그 동작: 없음.<br>
     */
    fun isLifecycleRequestAllowed(): Boolean =
        host.lifecycleOwner.lifecycle.currentState
            .isAtLeast(Lifecycle.State.CREATED)

    /**
     * Processes runtime, special, and role permissions for [entry].<br><br>
     * [entry]의 런타임/특수/Role 권한을 순차 처리합니다.<br>
     *
     * @param entry Request entry to process.<br><br>
     *              처리할 요청 엔트리입니다.<br>
     */
    suspend fun process(entry: RequestEntry) {
        if (entry.isCompleted()) {
            resultAggregator.tryCompleteRequest(entry.requestId)
            return
        }
        processRequest(entry)
        resultAggregator.tryCompleteRequest(entry.requestId)
    }

    /**
     * Handles the runtime/special/role permission pipeline for [entry].<br><br>
     * [entry]의 런타임/특수/Role 권한 파이프라인을 처리합니다.<br>
     *
     * @param entry Request entry to process.<br><br>
     *              처리할 요청 엔트리입니다.<br>
     */
    private suspend fun processRequest(entry: RequestEntry) {
        var pendingPermissions = entry.pendingPermissions()
        if (pendingPermissions.isEmpty()) return

        val runtimePermissions = pendingPermissions.filter {
            classifier.classify(it) == PermissionType.RUNTIME
        }
        if (runtimePermissions.isNotEmpty()) {
            processRuntimePermissions(entry, runtimePermissions)
        }

        pendingPermissions = entry.pendingPermissions()
        val specialPermissions = pendingPermissions.filter {
            classifier.classify(it) == PermissionType.SPECIAL
        }
        for (permission in specialPermissions) {
            processSpecialPermission(entry, permission)
        }

        pendingPermissions = entry.pendingPermissions()
        val rolePermissions = pendingPermissions.filter {
            classifier.classify(it) == PermissionType.ROLE
        }
        for (permission in rolePermissions) {
            processRolePermission(entry, permission)
        }
    }

    /**
     * Handles runtime permission flow for [permissions].<br><br>
     * [permissions]에 대한 런타임 권한 흐름을 처리합니다.<br>
     *
     * @param entry Request entry owning the runtime permissions.<br><br>
     *              런타임 권한을 보유한 요청 엔트리입니다.<br>
     * @param permissions Runtime permissions to process.<br><br>
     *                    처리할 런타임 권한 목록입니다.<br>
     */
    private suspend fun processRuntimePermissions(
        entry: RequestEntry,
        permissions: List<String>,
    ) {
        if (permissions.isEmpty()) return

        if (entry.isRestored) {
            val restoredResults = permissions.associateWith { permission ->
                when {
                    !isPlatformPermissionAvailable(permission, PermissionType.RUNTIME) ->
                        PermissionDecisionType.GRANTED
                    host.context.hasPermission(permission) ->
                        PermissionDecisionType.GRANTED
                    else -> {
                        val shouldShowRationale = runtimeHandler.shouldShowRationale(permission)
                        val wasRequestedBefore = runtimeHandler.wasRequested(permission)
                        runtimeHandler.mapResult(
                            permission = permission,
                            granted = false,
                            shouldShowRationale = shouldShowRationale,
                            wasRequestedBefore = wasRequestedBefore,
                        )
                    }
                }
            }
            resultAggregator.completeWaiters(restoredResults)
            return
        }

        val supportedPermissions = permissions.filter {
            isPlatformPermissionAvailable(it, PermissionType.RUNTIME)
        }
        val unsupportedPermissions = permissions.filterNot {
            isPlatformPermissionAvailable(it, PermissionType.RUNTIME)
        }
        if (unsupportedPermissions.isNotEmpty()) {
            resultAggregator.completeWaitersForPermissions(
                unsupportedPermissions,
                PermissionDecisionType.GRANTED,
            )
        }

        val grantedPermissions = supportedPermissions.filter { host.context.hasPermission(it) }
        if (grantedPermissions.isNotEmpty()) {
            resultAggregator.completeWaitersForPermissions(
                grantedPermissions,
                PermissionDecisionType.GRANTED,
            )
        }

        val requestablePermissions = supportedPermissions.filterNot {
            host.context.hasPermission(it)
        }
        if (requestablePermissions.isEmpty()) return

        val readyForLaunch = awaitHostStarted()
        if (!readyForLaunch) {
            resultAggregator.completeWaitersForPermissions(
                requestablePermissions,
                PermissionDecisionType.LIFECYCLE_NOT_READY,
            )
            return
        }

        val rationalePermissions = requestablePermissions.filter {
            runtimeHandler.shouldShowRationale(it)
        }
        if (rationalePermissions.isNotEmpty()) {
            val proceed = awaitRationaleDecision(
                permissions = rationalePermissions,
                onRationaleNeeded = entry.onRationaleNeeded,
            )
            if (!proceed) {
                resultAggregator.completeWaitersForPermissions(
                    requestablePermissions,
                    PermissionDecisionType.DENIED,
                )
                return
            }
        }

        runtimeRequestedBefore = requestablePermissions.associateWith {
            runtimeHandler.wasRequested(it)
        }
        runtimeHandler.markRequested(requestablePermissions)
        launchRuntimeRequest(requestablePermissions)
        runtimeCompletion?.await()
    }

    /**
     * Launches the runtime permission request dialog.<br><br>
     * 런타임 권한 요청 다이얼로그를 실행합니다.<br>
     *
     * @param permissions Permissions to request from the system.<br><br>
     *                    시스템에 요청할 권한 목록입니다.<br>
     */
    private fun launchRuntimeRequest(permissions: List<String>) {
        if (permissions.isEmpty()) return
        runtimeCompletion = CompletableDeferred()
        val launched = safeCatch(defaultValue = false) {
            runtimePermissionsLauncher.launch(permissions.toTypedArray())
            true
        }
        if (!launched) {
            Logx.e("Failed to launch runtime permission request.")
            resultAggregator.completeWaitersForPermissions(
                permissions,
                PermissionDecisionType.DENIED,
            )
            runtimeCompletion?.complete(Unit)
            runtimeCompletion = null
        }
    }

    /**
     * Handles a special permission flow for [permission].<br><br>
     * [permission]에 대한 특수 권한 흐름을 처리합니다.<br>
     *
     * @param entry Request entry owning the special permission.<br><br>
     *              특수 권한을 보유한 요청 엔트리입니다.<br>
     * @param permission Special permission string to process.<br><br>
     *                   처리할 특수 권한 문자열입니다.<br>
     */
    private suspend fun processSpecialPermission(
        entry: RequestEntry,
        permission: String,
    ) {
        if (entry.isRestored) {
            val result = when {
                !isPlatformPermissionAvailable(permission, PermissionType.SPECIAL) ->
                    PermissionDecisionType.NOT_SUPPORTED
                specialHandler.isGranted(permission) ->
                    PermissionDecisionType.GRANTED
                else -> PermissionDecisionType.DENIED
            }
            resultAggregator.completeWaitersForPermissions(
                listOf(permission),
                result,
            )
            return
        }
        if (!isPlatformPermissionAvailable(permission, PermissionType.SPECIAL)) {
            resultAggregator.completeWaitersForPermissions(
                listOf(permission),
                PermissionDecisionType.NOT_SUPPORTED,
            )
            return
        }
        if (specialHandler.isGranted(permission)) {
            resultAggregator.completeWaitersForPermissions(
                listOf(permission),
                PermissionDecisionType.GRANTED,
            )
            return
        }
        val intent = specialHandler.buildSettingsIntent(permission)
        if (intent == null) {
            resultAggregator.completeWaitersForPermissions(
                listOf(permission),
                PermissionDecisionType.NOT_SUPPORTED,
            )
            return
        }
        val readyForLaunch = awaitHostStarted()
        if (!readyForLaunch) {
            resultAggregator.completeWaitersForPermissions(
                listOf(permission),
                PermissionDecisionType.LIFECYCLE_NOT_READY,
            )
            return
        }
        val proceed = awaitSettingsDecision(
            permission = permission,
            onNavigateToSettings = entry.onNavigateToSettings,
        )
        if (!proceed) {
            resultAggregator.completeWaitersForPermissions(
                listOf(permission),
                PermissionDecisionType.DENIED,
            )
            return
        }
        launchSpecialPermission(permission, intent)
        specialCompletion?.await()
    }

    /**
     * Launches the settings activity for [permission].<br><br>
     * [permission]에 대한 설정 화면을 실행합니다.<br>
     *
     * @param permission Special permission being requested.<br><br>
     *                   요청 중인 특수 권한입니다.<br>
     * @param intent Intent pointing to the settings screen.<br><br>
     *               설정 화면을 가리키는 인텐트입니다.<br>
     */
    private fun launchSpecialPermission(
        permission: String,
        intent: Intent,
    ) {
        specialCompletion = CompletableDeferred()
        currentActivityAction = InFlightActivityAction.Special(permission)
        awaitingSpecialReturn = true
        didLeaveForSpecial = false
        val launched = safeCatch(defaultValue = false) {
            activityLauncher.launch(intent)
            true
        }
        if (!launched) {
            Logx.e("Failed to launch settings for permission=$permission")
            awaitingSpecialReturn = false
            didLeaveForSpecial = false
            currentActivityAction = null
            resultAggregator.completeWaitersForPermissions(
                listOf(permission),
                PermissionDecisionType.FAILED_TO_LAUNCH_SETTINGS,
            )
            specialCompletion?.complete(Unit)
            specialCompletion = null
        }
    }

    /**
     * Handles a role permission flow for [permission].<br><br>
     * [permission]에 대한 Role 권한 흐름을 처리합니다.<br>
     *
     * @param entry Request entry owning the role permission.<br><br>
     *              Role 권한을 보유한 요청 엔트리입니다.<br>
     * @param permission Role permission string to process.<br><br>
     *                   처리할 Role 권한 문자열입니다.<br>
     */
    private suspend fun processRolePermission(
        entry: RequestEntry,
        permission: String,
    ) {
        if (entry.isRestored) {
            val result = when {
                !roleHandler.isRoleAvailable(permission) ->
                    PermissionDecisionType.NOT_SUPPORTED
                roleHandler.isRoleHeld(permission) ->
                    PermissionDecisionType.GRANTED
                else -> PermissionDecisionType.DENIED
            }
            resultAggregator.completeWaitersForPermissions(
                listOf(permission),
                result,
            )
            return
        }
        if (!roleHandler.isRoleAvailable(permission)) {
            resultAggregator.completeWaitersForPermissions(
                listOf(permission),
                PermissionDecisionType.NOT_SUPPORTED,
            )
            return
        }
        if (roleHandler.isRoleHeld(permission)) {
            resultAggregator.completeWaitersForPermissions(
                listOf(permission),
                PermissionDecisionType.GRANTED,
            )
            return
        }
        val intent = roleHandler.createRequestIntent(permission)
        if (intent == null) {
            resultAggregator.completeWaitersForPermissions(
                listOf(permission),
                PermissionDecisionType.NOT_SUPPORTED,
            )
            return
        }
        val readyForLaunch = awaitHostStarted()
        if (!readyForLaunch) {
            resultAggregator.completeWaitersForPermissions(
                listOf(permission),
                PermissionDecisionType.LIFECYCLE_NOT_READY,
            )
            return
        }
        launchRolePermission(permission, intent)
        roleCompletion?.await()
    }

    /**
     * Launches the role request activity for [permission].<br><br>
     * [permission]에 대한 Role 요청 액티비티를 실행합니다.<br>
     *
     * @param permission Role permission being requested.<br><br>
     *                   요청 중인 Role 권한입니다.<br>
     * @param intent Role request intent to launch.<br><br>
     *               실행할 Role 요청 인텐트입니다.<br>
     */
    private fun launchRolePermission(
        permission: String,
        intent: Intent,
    ) {
        roleCompletion = CompletableDeferred()
        currentActivityAction = InFlightActivityAction.Role(permission)
        val launched = safeCatch(defaultValue = false) {
            activityLauncher.launch(intent)
            true
        }
        if (!launched) {
            Logx.e("Failed to launch role request for permission=$permission")
            currentActivityAction = null
            resultAggregator.completeWaitersForPermissions(
                listOf(permission),
                PermissionDecisionType.FAILED_TO_LAUNCH_SETTINGS,
            )
            roleCompletion?.complete(Unit)
            roleCompletion = null
        }
    }

    /**
     * Awaits the rationale decision from [onRationaleNeeded].<br><br>
     * [onRationaleNeeded]의 설명 결정 결과를 대기합니다.<br>
     *
     * @param permissions Permissions requiring rationale UI.<br><br>
     *                    설명 UI가 필요한 권한 목록입니다.<br>
     * @param onRationaleNeeded Callback invoked for rationale UI.<br><br>
     *                          설명 UI를 제공하는 콜백입니다.<br>
     * @return Return value: true when proceeding, false when canceled. Log behavior: logs on callback error.<br><br>
     *         반환값: 진행 시 true, 취소 시 false입니다. 로그 동작: 콜백 오류 시 로깅합니다.<br>
     */
    private suspend fun awaitRationaleDecision(
        permissions: List<String>,
        onRationaleNeeded: ((PermissionRationaleRequest) -> Unit)?,
    ): Boolean {
        if (permissions.isEmpty()) return true
        val callback = onRationaleNeeded ?: return true
        val decision = CompletableDeferred<Boolean>()
        val dispatched = safeCatch(defaultValue = false) {
            callback.invoke(
                PermissionRationaleRequest(
                    permissions = permissions,
                    proceed = { decision.complete(true) },
                    cancel = { decision.complete(false) },
                ),
            )
            true
        }
        if (!dispatched) {
            decision.complete(false)
        }
        return decision.await()
    }

    /**
     * Awaits the settings navigation decision from [onNavigateToSettings].<br><br>
     * [onNavigateToSettings]의 설정 이동 결정 결과를 대기합니다.<br>
     *
     * @param permission Special permission requiring settings navigation.<br><br>
     *                   설정 이동이 필요한 특수 권한입니다.<br>
     * @param onNavigateToSettings Callback invoked for settings navigation UI.<br><br>
     *                             설정 이동 안내 콜백입니다.<br>
     * @return Return value: true when proceeding, false when canceled. Log behavior: logs on callback error.<br><br>
     *         반환값: 진행 시 true, 취소 시 false입니다. 로그 동작: 콜백 오류 시 로깅합니다.<br>
     */
    private suspend fun awaitSettingsDecision(
        permission: String,
        onNavigateToSettings: ((PermissionSettingsRequest) -> Unit)?,
    ): Boolean {
        val callback = onNavigateToSettings ?: return true
        val decision = CompletableDeferred<Boolean>()
        val dispatched = safeCatch(defaultValue = false) {
            callback.invoke(
                PermissionSettingsRequest(
                    permission = permission,
                    proceed = { decision.complete(true) },
                    cancel = { decision.complete(false) },
                ),
            )
            true
        }
        if (!dispatched) {
            decision.complete(false)
        }
        return decision.await()
    }

    /**
     * Handles the runtime permission result map from the system dialog.<br><br>
     * 시스템 다이얼로그의 런타임 권한 결과를 처리합니다.<br>
     *
     * @param results Map of permission to granted flag.<br><br>
     *                권한과 허용 여부의 매핑입니다.<br>
     */
    private fun handleRuntimePermissionsResult(results: Map<String, Boolean>) {
        val completion = runtimeCompletion ?: return
        val mappedResults = results.mapValues { (permission, granted) ->
            val shouldShowRationale = runtimeHandler.shouldShowRationale(permission)
            val wasRequestedBefore = runtimeRequestedBefore[permission]
                ?: runtimeHandler.wasRequested(permission)
            runtimeHandler.mapResult(
                permission = permission,
                granted = granted,
                shouldShowRationale = shouldShowRationale,
                wasRequestedBefore = wasRequestedBefore,
            )
        }
        resultAggregator.completeWaiters(mappedResults)
        runtimeRequestedBefore = emptyMap()
        completion.complete(Unit)
        runtimeCompletion = null
    }

    /**
     * Dispatches activity result callbacks to role handlers only.<br><br>
     * ActivityResult 콜백은 Role 처리에만 전달합니다.<br>
     *
     * Special permissions are confirmed on resume to avoid premature completion.<br><br>
     * 특수 권한은 조기 완료를 피하기 위해 onResume에서만 확정합니다.<br>
     */
    private fun handleActivityResultReturn() {
        when (currentActivityAction) {
            is InFlightActivityAction.Role -> handleRolePermissionReturn()
            is InFlightActivityAction.Special,
            null,
            -> Unit
        }
    }

    /**
     * Rechecks special permission state after returning from settings.<br><br>
     * 설정 화면 복귀 후 특수 권한 상태를 재확인합니다.<br>
     */
    private fun handleSpecialPermissionReturn() {
        val action = currentActivityAction as? InFlightActivityAction.Special ?: return
        if (!awaitingSpecialReturn) return
        awaitingSpecialReturn = false
        didLeaveForSpecial = false
        currentActivityAction = null
        val granted = specialHandler.isGranted(action.permission)
        val result = if (granted) PermissionDecisionType.GRANTED else PermissionDecisionType.DENIED
        resultAggregator.completeWaitersForPermissions(
            listOf(action.permission),
            result,
        )
        specialCompletion?.complete(Unit)
        specialCompletion = null
    }

    /**
     * Rechecks role state after returning from role request activity.<br><br>
     * Role 요청 액티비티 복귀 후 Role 보유 상태를 재확인합니다.<br>
     */
    private fun handleRolePermissionReturn() {
        val action = currentActivityAction as? InFlightActivityAction.Role ?: return
        currentActivityAction = null
        val granted = roleHandler.isRoleHeld(action.role)
        val result = if (granted) PermissionDecisionType.GRANTED else PermissionDecisionType.DENIED
        resultAggregator.completeWaitersForPermissions(
            listOf(action.role),
            result,
        )
        roleCompletion?.complete(Unit)
        roleCompletion = null
    }

    /**
     * Returns whether the permission is supported on the current platform.<br><br>
     * 현재 플랫폼에서 권한이 지원되는지 여부를 반환합니다.<br>
     *
     * @param permission Permission string to inspect.<br><br>
     *                  확인할 권한 문자열입니다.<br>
     * @param type Classified permission type.<br><br>
     *             분류된 권한 타입입니다.<br>
     * @return Return value: true when the permission is supported. Log behavior: none.<br><br>
     *         반환값: 지원되면 true입니다. 로그 동작: 없음.<br>
     */
    private fun isPlatformPermissionAvailable(
        permission: String,
        type: PermissionType,
    ): Boolean = when (type) {
        PermissionType.RUNTIME -> classifier.isSupported(permission)
        PermissionType.SPECIAL -> classifier.isSupported(permission)
        PermissionType.ROLE -> true
    }

    /**
     * Suspends until the host reaches STARTED or returns false on destroy.<br><br>
     * 호스트가 STARTED가 될 때까지 대기하거나 destroy 시 false를 반환합니다.<br>
     *
     * @return Return value: true when STARTED is reached, false when destroyed. Log behavior: none.<br><br>
     *         반환값: STARTED 도달 시 true, destroy 시 false입니다. 로그 동작: 없음.<br>
     */
    private suspend fun awaitHostStarted(): Boolean {
        val lifecycle = host.lifecycleOwner.lifecycle
        val currentState = lifecycle.currentState
        if (currentState == Lifecycle.State.DESTROYED) return false
        if (currentState.isAtLeast(Lifecycle.State.STARTED)) return true
        return suspendCancellableCoroutine { continuation ->
            val observer = object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    lifecycle.removeObserver(this)
                    if (continuation.isActive) {
                        continuation.resume(true)
                    }
                }

                override fun onDestroy(owner: LifecycleOwner) {
                    lifecycle.removeObserver(this)
                    if (continuation.isActive) {
                        continuation.resume(false)
                    }
                }
            }
            lifecycle.addObserver(observer)
            continuation.invokeOnCancellation { lifecycle.removeObserver(observer) }
        }
    }

    /**
     * Represents the current activity action in flight.<br><br>
     * 현재 실행 중인 액티비티 액션을 나타냅니다.<br>
     */
    private sealed class InFlightActivityAction {
        /**
         * Action representing a special permission settings flow.<br><br>
         * 특수 권한 설정 흐름을 나타내는 액션입니다.<br>
         *
         * @param permission Special permission string.<br><br>
         *                   특수 권한 문자열입니다.<br>
         */
        data class Special(
            val permission: String,
        ) : InFlightActivityAction()

        /**
         * Action representing a role request flow.<br><br>
         * Role 요청 흐름을 나타내는 액션입니다.<br>
         *
         * @param role Role string requested via RoleManager.<br><br>
         *             RoleManager로 요청하는 Role 문자열입니다.<br>
         */
        data class Role(
            val role: String,
        ) : InFlightActivityAction()
    }
}
