/**
 * Central manager that coordinates normal and special permission requests across configuration changes.<br><br>
 * 구성 변경 상황에서도 일반·특수 권한 요청을 일괄 조율하는 중앙 관리자입니다.<br>
 */

package kr.open.library.simple_ui.xml.permissions.manager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.permissions.vo.PermissionConstants
import kr.open.library.simple_ui.core.permissions.extentions.*
import kr.open.library.simple_ui.xml.permissions.register.PermissionDelegate
import java.lang.ref.WeakReference
import java.util.UUID

/**
 * Indicates whether an additional callback could be attached to an in-flight request.<br><br>
 * 진행 중인 권한 요청에 추가 콜백을 부착할 수 있었는지를 나타냅니다.<br>
 */


enum class CallbackAddResult {
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
    PERMISSION_MISMATCH
}

/**
 * Lightweight permission orchestrator that merges callback-based requests with special flows.<br><br>
 * 콜백 기반 요청과 특수 권한 흐름을 결합한 경량 권한 오케스트레이터입니다.<br>
 *
 * Key capabilities:<br><br>
 * 주요 기능:<br>
 * - Coordinates normal and special permission flows inside a single queue.<br><br>
 *   단일 큐에서 일반·특수 권한을 순차적으로 처리합니다.<br>
 * - Tracks delegates through WeakReference to avoid memory leaks.<br><br>
 *   WeakReference로 delegate를 추적해 메모리 누수를 방지합니다.<br>
 * - Cleans up expired requests automatically on a timer.<br><br>
 *   타이머를 활용해 만료된 요청을 자동 정리합니다.<br>
 */

class PermissionManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: PermissionManager? = null

        fun getInstance(): PermissionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PermissionManager().also { INSTANCE = it }
            }
        }
    }

    // Stores in-flight permission requests keyed by requestId.<br><br>
    // 진행 중인 권한 요청을 requestId 기준으로 관리합니다.<br>
    private val pendingRequests = mutableMapOf<String, PendingRequest>()

    // Tracks delegates via WeakReference to prevent leaks after configuration changes.<br><br>
    // 설정 변경 시 참조가 오래 살아남지 않도록 WeakReference로 Delegate를 추적합니다.<br>
    private val activeDelegates = mutableMapOf<String, WeakReference<PermissionDelegate<*>>>()

    // CoroutineScope for delayed cleanup and retries on the main thread.<br><br>
    // 메인 스레드에서 정리와 재시도를 처리하는 CoroutineScope입니다.<br>
    private val managerScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    // Job for the periodic cleanup task.<br><br>
    // 주기적인 정리 작업을 위한 Job입니다.<br>
    private var cleanupJob: Job? = null

    // Mutex for thread-safe access to shared state.<br><br>
    // 공유 상태에 대한 스레드 안전 접근을 위한 Mutex입니다.<br>
    private val mutex = Mutex()

    // Internal snapshot of a pending permission request.<br><br>
    // 대기 중인 권한 요청의 내부 스냅샷입니다.<br>
    private data class PendingRequest(
        // Callbacks waiting for the permission result.<br><br>
        // 권한 결과를 기다리는 콜백 모음입니다.<br>
        val callbacks: MutableList<(List<String>) -> Unit> = mutableListOf(),
        val requestTime: Long = System.currentTimeMillis(),
        val originalPermissions: List<String> = emptyList(),
        var remainingSpecialPermissions: List<String> = emptyList()
        // Special launchers are now queried dynamically from the delegate.<br><br>
        // 특수 권한 런처는 Delegate에서 동적으로 조회합니다.<br>
    )

    /**
     * Entry point for requesting both normal and special permissions.<br><br>
     * 일반 권한과 특수 권한을 한 번에 요청하는 기본 API 입니다.<br>
     *
     * @param context Context used to validate lifecycle state before launching.<br><br>
     *                요청을 시작하기 전에 생명주기 상태를 확인할 Context 입니다.<br>
     * @param requestPermissionLauncher Launcher that handles the normal permission array.<br><br>
     *                                  일반 권한 배열을 처리하는 ActivityResultLauncher 입니다.<br>
     * @param permissions Ordered list of permissions to request.<br><br>
     *                    요청할 권한 순서를 그대로 담은 목록입니다.<br>
     * @param callback Callback receiving denied permissions after the flow finishes.<br><br>
     *                 전체 흐름이 끝난 뒤 거부된 권한 목록을 전달받는 콜백입니다.<br>
     * @param preGeneratedRequestId Optional pre-generated requestId used when a delegate already exists.<br><br>
     *                              Delegate가 이미 존재할 때 사용할 수 있는 선 생성 requestId 입니다.<br>
     * @return Request identifier that can later cancel or relink the delegate.<br><br>
     *         추후 요청을 취소하거나 Delegate를 재연결할 때 사용할 수 있는 식별자입니다.<br>
     *
     * Note: Special permission launchers are resolved dynamically from the delegate now.<br><br>
     * 참고: 특수 권한 런처는 Delegate에서 동적으로 조회됩니다.<br>
     */
    suspend fun request(
        context: Context,
        requestPermissionLauncher: ActivityResultLauncher<Array<String>>,
        permissions: List<String>,
        callback: (List<String>) -> Unit,
        preGeneratedRequestId: String? = null
    ): String = mutex.withLock {
        // Guard clause that avoids unnecessary launches when nothing is requested.<br><br>
        // 요청할 권한이 없으면 불필요한 실행을 즉시 차단합니다.<br>
        if (permissions.isEmpty()) {
            callback(emptyList())
            return@withLock ""
        }

        if (isContextDestroyed(context)) {
            callback(permissions)
            return@withLock ""
        }

        // Removes expired entries so that stale callbacks do not pile up.<br><br>
        // 오래된 요청을 미리 정리해 콜백이 쌓이지 않도록 합니다.<br>
        cleanupExpiredRequestsInternal()

        val remainingPermissions = context.remainPermissions(permissions)
        if (remainingPermissions.isEmpty()) {
            callback(emptyList())
            return@withLock ""
        }

        // Reuse pre-generated IDs when a delegate already registered the request.<br><br>
        // Delegate가 이미 등록된 경우 선 생성한 ID를 재활용합니다.<br>
        val requestId = preGeneratedRequestId ?: UUID.randomUUID().toString()
        val request = PendingRequest(
            // Seeds the first callback listener for this request.<br><br>
            // 요청에 대한 첫 번째 콜백 리스너를 등록합니다.<br>
            callbacks = mutableListOf(callback),
            originalPermissions = permissions
        )
        pendingRequests[requestId] = request

        // Ensures cleanup continues to run while we have in-flight requests.<br><br>
        // 진행 중인 요청이 있는 동안 자동 정리 작업이 계속되도록 예약합니다.<br>
        scheduleCleanup()

        val (specialPermissions, normalPermissions) = remainingPermissions.partition {
            context.isSpecialPermission(it)
        }

        // Launches the normal permission flow first when necessary.<br><br>
        // 필요 시 먼저 일반 권한 요청을 실행합니다.<br>
        if (normalPermissions.isNotEmpty()) {
            try {
                requestPermissionLauncher.launch(normalPermissions.toTypedArray())
            } catch (e: Exception) {
                Logx.e("Failed to launch permission request: $e")
                // Prevents ghost requests by clearing pendingRequests and delegates together.<br><br>
                // 유령 요청이 남지 않도록 pendingRequests와 delegate를 함께 정리합니다.<br>
                pendingRequests.remove(requestId)
                unregisterDelegateInternal(requestId)
                // Notifies callers assuming every permission remained denied.<br><br>
                // 모든 권한이 거부된 것으로 간주하고 콜백을 호출합니다.<br>
                invokeAllCallbacks(request, remainingPermissions)
                // Returns an empty id so the delegate clears its currentRequestId.<br><br>
                // 빈 문자열을 반환해 Delegate가 currentRequestId를 비우도록 합니다.<br>
                return@withLock ""
            }
        }

        // Handles special permissions sequentially.<br><br>
        // 특수 권한을 순차적으로 처리합니다.<br>
        // If normal permissions exist we wait until result() triggers.<br><br>
        // 일반 권한을 요청했다면 result() 호출 이후에 특수 권한을 진행합니다.<br>
        if (specialPermissions.isNotEmpty() && normalPermissions.isEmpty()) {
            request.remainingSpecialPermissions = specialPermissions.drop(1)
            val firstSpecialPermission = specialPermissions.first()

            // Requests the proper launcher from the delegate at runtime.<br><br>
            // Delegate로부터 적합한 런처를 동적으로 받아옵니다.<br>
            val launcher = getSpecialLauncher(requestId, firstSpecialPermission)
            if (launcher != null) {
                launchSpecialPermissionWithRetry(context, firstSpecialPermission, launcher, requestId)
            } else {
                Logx.e("No launcher found for special permission: $firstSpecialPermission (Delegate not registered?)")
                handleSpecialPermissionFailure(context, requestId, firstSpecialPermission)
            }
        } else if (specialPermissions.isNotEmpty() && normalPermissions.isNotEmpty()) {
            // When both types exist, defer special permissions until normals finish.<br><br>
            // 일반 권한과 특수 권한을 함께 요청한 경우 특수 권한은 일반 흐름 이후에 처리합니다.<br>
            request.remainingSpecialPermissions = specialPermissions
        }

        requestId
    }


    /**
     * Handles ActivityResultLauncher callbacks for normal permissions.<br><br>
     * 일반 권한 ActivityResultLauncher 콜백을 처리합니다.<br>
     *
     * @param context Context used to verify lifecycle and actual grant state.<br><br>
     *                생명주기와 실제 권한 상태를 확인할 Context 입니다.<br>
     * @param permissions Map containing permission-granted pairs from the launcher.<br><br>
     *                    런처에서 전달받은 권한과 승인 여부 쌍입니다.<br>
     * @param requestId Identifier of the pending request to resolve.<br><br>
     *                  처리할 대기 요청의 식별자입니다.<br>
     */
    suspend fun result(context: Context, permissions: Map<String, Boolean>, requestId: String?) = mutex.withLock {
        if (requestId == null) return@withLock

        if (isContextDestroyed(context)) return@withLock

        val request = pendingRequests[requestId] ?: return@withLock

        val deniedPermissions = permissions.filterNot { (permission, granted) ->
            granted || context.hasPermission(permission)
        }.keys.toList()

        // Continue with special permissions if needed; otherwise finalize the request.<br><br>
        // 특수 권한이 남아 있다면 계속 진행하고, 없다면 요청을 종료합니다.<br>
        if (request.remainingSpecialPermissions.isEmpty()) {
            pendingRequests.remove(requestId)
            unregisterDelegateInternal(requestId)

            invokeAllCallbacks(request, deniedPermissions)
        } else {
            // Picks up the next special permission in line.<br><br>
            // 다음 특수 권한을 이어서 처리합니다.<br>
            val nextPermission = request.remainingSpecialPermissions.first()
            request.remainingSpecialPermissions = request.remainingSpecialPermissions.drop(1)

            val launcher = getSpecialLauncher(requestId, nextPermission)
            if (launcher != null) {
                launchSpecialPermissionWithRetry(context, nextPermission, launcher, requestId)
            } else {
                Logx.e("No launcher available for special permission after normal permission: $nextPermission")
                handleSpecialPermissionFailure(context, requestId, nextPermission)
            }
        }
    }

    /**
     * Handles ActivityResultLauncher callbacks for special permissions.<br><br>
     * 특수 권한 ActivityResultLauncher 콜백을 처리합니다.<br>
     *
     * @param context Context reference used to check actual permission states.<br><br>
     *                특수 권한 승인 여부를 다시 검증할 Context 입니다.<br>
     * @param permission Special permission the launcher reported.<br><br>
     *                   런처에서 반환한 특수 권한입니다.<br>
     * @param requestId Identifier of the pending permission request.<br><br>
     *                  현재 진행 중인 권한 요청 식별자입니다.<br>
     */
    suspend fun resultSpecialPermission(context: Context, permission: String, requestId: String?) = mutex.withLock {
        if (requestId == null) return@withLock

        if (isContextDestroyed(context)) return@withLock

        val request = pendingRequests[requestId] ?: run {
            Logx.w("Request $requestId not found. Process may have been killed or request already completed.")
            return@withLock
        }

        // Double-checks the current permission grant state and logs it.<br><br>
        // 현재 권한 승인 상태를 다시 확인하고 로그로 남깁니다.<br>
        val isGranted = context.hasPermission(permission)
        Logx.d("Special permission $permission result: ${if (isGranted) "GRANTED" else "DENIED"}")

        // Continue to the next special permission when necessary.<br><br>
        // 다음 특수 권한이 남아 있다면 이어서 처리합니다.<br>
        if (request.remainingSpecialPermissions.isNotEmpty()) {
            val nextPermission = request.remainingSpecialPermissions.first()
            request.remainingSpecialPermissions = request.remainingSpecialPermissions.drop(1)

            // Retrieves a launcher reference from the delegate on demand.<br><br>
            // Delegate에서 런처 참조를 필요할 때마다 받아옵니다.<br>
            val launcher = getSpecialLauncher(requestId, nextPermission)
            if (launcher != null) {
                launchSpecialPermissionWithRetry(context, nextPermission, launcher, requestId)
                return@withLock
            } else {
                Logx.e("No launcher available for special permission: $nextPermission (Delegate may have been destroyed)")
                handleSpecialPermissionFailure(context, requestId, nextPermission)
                return@withLock
            }
        }

        // All special permissions have been processed, so compute the final result.<br><br>
        // 특수 권한 처리가 모두 끝났으므로 최종 결과를 산출합니다.<br>
        pendingRequests.remove(requestId)
        unregisterDelegateInternal(requestId)
        val finalDeniedPermissions = context.remainPermissions(request.originalPermissions)

        invokeAllCallbacks(request, finalDeniedPermissions)
    }

    /**
     * Cancels an in-flight permission request.<br><br>
     * 진행 중인 권한 요청을 취소합니다.<br>
     *
     * @param requestId Identifier of the request to remove.<br><br>
     *                  취소할 요청의 식별자입니다.<br>
     */
    suspend fun cancelRequest(requestId: String) = mutex.withLock {
        pendingRequests.remove(requestId)
        unregisterDelegateInternal(requestId)
    }

    /**
     * Registers the delegate that owns this permission request.<br><br>
     * 권한 요청을 소유한 Delegate를 등록합니다.<br>
     *
     * Used to reattach after configuration changes.<br><br>
     * 구성 변경 이후 자동 재등록에도 사용됩니다.<br>
     *
     * @param requestId Identifier of the pending request.<br><br>
     *                  진행 중인 요청의 식별자입니다.<br>
     * @param delegate Delegate that mediates permission launches.<br><br>
     *                 권한 실행을 중계할 Delegate 입니다.<br>
     */
    suspend fun registerDelegate(requestId: String, delegate: PermissionDelegate<*>) = mutex.withLock {
        val existingRef = activeDelegates[requestId]
        val existingDelegate = existingRef?.get()

        if (existingDelegate != null && existingDelegate !== delegate) {
            Logx.d("Replacing delegate for request: $requestId (Configuration change)")
        }

        activeDelegates[requestId] = WeakReference(delegate)
        Logx.d("Registered delegate for request: $requestId")
    }

    /**
     * Unregisters the delegate once the request finishes or gets cancelled.<br><br>
     * 요청이 완료되거나 취소되면 Delegate 등록을 해제합니다.<br>
     *
     * @param requestId Identifier whose delegate should be removed.<br><br>
     *                  참조를 제거할 요청 ID 입니다.<br>
     */
    suspend fun unregisterDelegate(requestId: String) = mutex.withLock {
        unregisterDelegateInternal(requestId)
    }

    /**
     * Internal version of unregisterDelegate without mutex lock.<br><br>
     * mutex 잠금 없이 사용하는 내부 버전의 unregisterDelegate입니다.<br>
     */
    private fun unregisterDelegateInternal(requestId: String) {
        activeDelegates.remove(requestId)
        Logx.d("Unregistered delegate for request: $requestId")
    }

    /**
     * Indicates whether a request is still active (used when restoring delegates).<br><br>
     * 요청이 여전히 활성 상태인지 확인합니다. Delegate 복구 시 사용됩니다.<br>
     *
     * @param requestId Identifier to check.<br><br>
     *                  확인할 요청 ID 입니다.<br>
     */
    suspend fun hasActiveRequest(requestId: String): Boolean = mutex.withLock {
        pendingRequests.containsKey(requestId)
    }

    /**
     * Returns the original permission set associated with the request.<br><br>
     * 요청과 연결된 원본 권한 목록을 반환합니다.<br>
     *
     * @param requestId Identifier of the pending request.<br><br>
     *                  조회할 요청 ID 입니다.<br>
     * @return Permission set or null if the request is unknown.<br><br>
     *         요청을 찾지 못하면 null 을 반환합니다.<br>
     */
    suspend fun getRequestPermissions(requestId: String): Set<String>? = mutex.withLock {
        pendingRequests[requestId]?.originalPermissions?.toSet()
    }

    /**
     * Adds an additional callback to an in-flight request (for duplicate launches).<br><br>
     * 중복 요청 상황에서 진행 중인 요청에 콜백을 추가합니다.<br>
     *
     * @param requestId Identifier of the pending request.<br><br>
     *                  대기 중인 요청 ID 입니다.<br>
     * @param callback Callback that receives denied permissions at the end.<br><br>
     *                 최종적으로 거부된 권한 목록을 전달받는 콜백입니다.<br>
     * @param requestedPermissions Permission list supplied by the caller for validation.<br><br>
     *                             검증용으로 전달된 권한 목록입니다.<br>
     * @return CallbackAddResult describing success, missing request, or mismatch.<br><br>
     *         콜백 추가 성공, 요청 없음, 권한 불일치 중 하나를 나타내는 CallbackAddResult 입니다.<br>
     */
    suspend fun addCallbackToRequest(
        requestId: String,
        callback: (List<String>) -> Unit,
        requestedPermissions: List<String>
    ): CallbackAddResult = mutex.withLock {
        // Returns immediately when the request no longer exists.<br><br>
        // 요청이 이미 완료되었거나 취소되었다면 즉시 반환합니다.<br>
        val request = pendingRequests[requestId] ?: return@withLock CallbackAddResult.REQUEST_NOT_FOUND

        // Compares permission sets regardless of order to ensure parity.<br><br>
        // 순서에 상관없이 권한 집합이 동일한지 확인합니다.<br>
        val existingPermissions = request.originalPermissions.toSet()
        val newPermissions = requestedPermissions.toSet()

        if (existingPermissions != newPermissions) {
            Logx.w("Permission mismatch! Existing: $existingPermissions, Requested: $newPermissions")
            return@withLock CallbackAddResult.PERMISSION_MISMATCH
        }

        // Appends the callback when everything matches.<br><br>
        // 조건이 일치하면 콜백을 추가합니다.<br>
        request.callbacks.add(callback)
        Logx.d("Added callback to existing request: $requestId (total callbacks: ${request.callbacks.size})")
        CallbackAddResult.SUCCESS
    }

    /**
     * Retrieves the special-permission launcher associated with a request.<br><br>
     * 요청과 연결된 특수 권한 런처를 조회합니다.<br>
     *
     * @param requestId Pending request identifier.<br><br>
     *                  조회할 요청 ID 입니다.<br>
     * @param permission Special permission key for the launcher.<br><br>
     *                   런처를 가져올 특수 권한 키입니다.<br>
     * @return Launcher instance or null if the delegate is gone.<br><br>
     *         Delegate가 없으면 null을 반환합니다.<br>
     */
    private fun getSpecialLauncher(requestId: String, permission: String): ActivityResultLauncher<Intent>? {
        val delegateRef = activeDelegates[requestId] ?: run {
            Logx.w("No delegate registered for request: $requestId")
            return null
        }

        val delegate = delegateRef.get() ?: run {
            Logx.w("Delegate was garbage collected for request: $requestId")
            activeDelegates.remove(requestId)
            return null
        }

        return delegate.getSpecialLauncher(permission)
    }

    /**
     * Invokes every registered callback with the provided denied list.<br><br>
     * 등록된 모든 콜백에 전달된 거부 목록을 전달합니다.<br>
     *
     * @param request Pending request that owns the callbacks.<br><br>
     *                콜백을 보유한 요청 객체입니다.<br>
     * @param deniedPermissions Permissions that remain denied.<br><br>
     *                          거부 상태로 남아 있는 권한 목록입니다.<br>
     */
    private fun invokeAllCallbacks(request: PendingRequest, deniedPermissions: List<String>) {
        Logx.d("Invoking ${request.callbacks.size} callback(s) with denied permissions: $deniedPermissions")
        request.callbacks.forEach { callback ->
            try {
                callback(deniedPermissions)
            } catch (e: Exception) {
                Logx.e("Error in permission callback: $e")
            }
        }
    }



    /**
     * Cleans up requests that exceeded the timeout (default 5 minutes).<br><br>
     * 기본 5분 타임아웃을 초과한 요청을 정리합니다.<br>
     *
     * Internal function for testing purposes.<br><br>
     * 테스트 목적의 internal 함수입니다.<br>
     */
    internal suspend fun cleanupExpiredRequests() = mutex.withLock {
        cleanupExpiredRequestsInternal()
    }

    /**
     * Internal version of cleanupExpiredRequests without mutex lock.<br><br>
     * mutex 잠금 없이 사용하는 내부 버전의 cleanupExpiredRequests입니다.<br>
     */
    private fun cleanupExpiredRequestsInternal() {
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

        // Removes both pending entries and delegate references to avoid leaks.<br><br>
        // pendingRequests와 delegate 참조를 함께 제거해 유령 요청을 막습니다.<br>
        expiredRequestIds.forEach { requestId ->
            pendingRequests.remove(requestId)
            // Ensures the matching activeDelegates entry is cleared as well.<br><br>
            // activeDelegates 항목도 함께 정리합니다.<br>
            unregisterDelegateInternal(requestId)
            Logx.d("Cleaned up expired request: $requestId")
        }

        // Schedule another cleanup pass only when requests remain.<br><br>
        // 요청이 남아 있을 때만 다음 정리 작업을 예약합니다.<br>
        if (pendingRequests.isNotEmpty()) {
            scheduleCleanup()
        }
    }

    /**
     * Schedules the cleanup job (default interval: 60 seconds).<br><br>
     * 정리 Job을 예약합니다. 기본 주기는 60초입니다.<br>
     */
    private fun scheduleCleanup() {
        cleanupJob?.cancel()

        cleanupJob = managerScope.launch {
            delay(60_000)
            mutex.withLock {
                cleanupExpiredRequestsInternal()
            }
        }
    }

    // Private helpers<br><br>
    // 내부 헬퍼 함수 모음입니다.<br>

    /**
     * Launches a special-permission intent and retries once on transient failures.<br><br>
     * 특수 권한 인텐트를 실행하고 일시적인 실패 시 한 번 재시도합니다.<br>
     *
     * @param context Host context for launching the intent.<br><br>
     *                인텐트를 실행할 호스트 컨텍스트입니다.<br>
     * @param permission Special permission to request.<br><br>
     *                   요청할 특수 권한입니다.<br>
     * @param launcher ActivityResultLauncher used for the special permission flow.<br><br>
     *                 특수 권한 흐름에 사용하는 ActivityResultLauncher입니다.<br>
     * @param requestId Identifier of the pending request.<br><br>
     *                  대기 중인 요청의 식별자입니다.<br>
     * @param retryCount Number of attempts already made for this permission.<br><br>
     *                   해당 권한에 대해 이미 시도한 횟수입니다.<br>
     */
    private fun launchSpecialPermissionWithRetry(
        context: Context,
        permission: String,
        launcher: ActivityResultLauncher<Intent>,
        requestId: String,
        retryCount: Int = 0
    ) {
        val intent = createSpecialPermissionIntent(context, permission)
        if (intent != null) {
            try {
                launcher.launch(intent)
            } catch (e: Exception) {
                Logx.e("Failed to launch special permission request (attempt ${retryCount + 1}) $e" )

                // Retry once asynchronously after 50ms to handle transient issues.<br><br>
                // 일시적인 문제를 대비해 50ms 후 한 번 비동기로 재시도합니다.<br>
                if (retryCount < 1) {
                    managerScope.launch {
                        delay(50)
                        try {
                            launchSpecialPermissionWithRetry(context, permission, launcher, requestId, retryCount + 1)
                        } catch (retryException: Exception) {
                            Logx.e("Retry failed for special permission request $retryException" )
                            handleSpecialPermissionFailure(context, requestId, permission)
                        }
                    }
                } else {
                    handleSpecialPermissionFailure(context, requestId, permission)
                }
            }
        } else {
            Logx.e("Cannot create intent for special permission: $permission")
            handleSpecialPermissionFailure(context, requestId, permission)
        }
    }

    /**
     * Handles failures when a special permission launcher is missing or fails to start.<br><br>
     * 특수 권한 런처가 없거나 실행에 실패했을 때의 후속 처리를 담당합니다.<br>
     *
     * @param context Context used to attempt any remaining launches.<br><br>
     *                남은 런처 실행을 시도할 때 사용할 컨텍스트입니다.<br>
     * @param requestId Identifier of the affected request.<br><br>
     *                  영향을 받는 요청의 식별자입니다.<br>
     * @param permission Special permission that triggered the failure.<br><br>
     *                   실패를 일으킨 특수 권한입니다.<br>
     */
    private fun handleSpecialPermissionFailure(context: Context?, requestId: String, permission: String) {
        val request = pendingRequests[requestId] ?: return

        val nextPermission = request.remainingSpecialPermissions.firstOrNull()
        if (nextPermission != null && context != null) {
            // Retrieves the next launcher dynamically from the delegate.<br><br>
            // Delegate에서 다음 런처를 동적으로 조회합니다.<br>
            val launcher = getSpecialLauncher(requestId, nextPermission)
            if (launcher != null) {
                request.remainingSpecialPermissions = request.remainingSpecialPermissions.drop(1)
                launchSpecialPermissionWithRetry(context, nextPermission, launcher, requestId)
                return
            } else {
                Logx.e("No launcher available for special permission: $nextPermission")
            }
        }

        val deniedPermissions = buildList {
            add(permission)
            addAll(request.remainingSpecialPermissions)
        }

        pendingRequests.remove(requestId)
        unregisterDelegateInternal(requestId)

        invokeAllCallbacks(request, deniedPermissions)
    }

    /**
     * Checks whether the provided context is no longer valid for permission launches.<br><br>
     * 전달된 컨텍스트가 권한 실행에 사용할 수 없는 상태인지 확인합니다.<br>
     *
     * @param context Context to validate.<br><br>
     *                확인할 컨텍스트입니다.<br>
     * @return True when the activity is finishing or destroyed.<br><br>
     *         액티비티가 종료 중이거나 파괴된 경우 true를 반환합니다.<br>
     */
    private fun isContextDestroyed(context: Context): Boolean {
        return if (context is Activity) {
            context.isDestroyed || context.isFinishing
        } else {
            false
        }
    }

    /**
     * Builds an intent for the given special permission after checking API-level requirements.<br><br>
     * API 레벨 요구사항을 확인한 뒤 지정된 특수 권한용 인텐트를 생성합니다.<br>
     *
     * @param context Host context used to resolve package information.<br><br>
     *                패키지 정보를 확인할 호스트 컨텍스트입니다.<br>
     * @param permission Special permission being requested.<br><br>
     *                   요청하려는 특수 권한입니다.<br>
     * @return Intent to launch the permission screen, or null if unsupported.<br><br>
     *         권한 화면을 여는 인텐트이며, 지원되지 않으면 null을 반환합니다.<br>
     */
    private fun createSpecialPermissionIntent(context: Context, permission: String): Intent? {
        // Validates API levels before creating the permission intent.<br><br>
        // 권한 인텐트를 만들기 전에 필요한 API 레벨을 확인합니다.<br>
        when (permission) {
            in PermissionConstants.ApiLevelRequirements.ANDROID_R_PERMISSIONS -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
            }
            in PermissionConstants.ApiLevelRequirements.ANDROID_S_PERMISSIONS -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
            }
            in PermissionConstants.ApiLevelRequirements.ANDROID_TIRAMISU_PERMISSIONS -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
            }
        }

        val action = PermissionConstants.SPECIAL_PERMISSION_ACTIONS[permission] ?: return null

        return if (PermissionConstants.PERMISSIONS_REQUIRING_PACKAGE_URI.contains(permission)) {
            Intent(action, Uri.parse("package:${context.packageName}"))
        } else {
            Intent(action)
        }
    }
}