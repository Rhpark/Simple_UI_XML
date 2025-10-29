package kr.open.library.simple_ui.permissions.manager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.permissions.vo.PermissionConstants
import kr.open.library.simple_ui.permissions.extentions.*
import kr.open.library.simple_ui.permissions.register.PermissionDelegate
import java.lang.ref.WeakReference
import java.util.UUID

/**
 * 콜백 추가 결과
 */
enum class CallbackAddResult {
    SUCCESS,              // 콜백이 성공적으로 추가됨
    REQUEST_NOT_FOUND,    // 요청을 찾을 수 없음 (이미 완료되었거나 취소됨)
    PERMISSION_MISMATCH   // 요청한 권한 목록이 기존 요청과 다름
}

/**
 * 단순하고 안전한 권한 관리자
 * 
 * 특징:
 * - 콜백 기반의 간단한 API
 * - 일반 권한과 특수 권한 통합 처리
 * - 메모리 안전을 위한 WeakReference 사용
 * - 자동 요청 정리
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

    // 진행 중인 요청 관리
    private val pendingRequests = mutableMapOf<String, PendingRequest>()

    // Delegate 참조 관리 (WeakReference로 메모리 누수 방지)
    private val activeDelegates = mutableMapOf<String, WeakReference<PermissionDelegate<*>>>()

    // 메인 스레드 핸들러 (재시도용)
    private val mainHandler = Handler(Looper.getMainLooper())

    // 자동 정리 작업
    private var cleanupRunnable: Runnable? = null
    
    // 내부 요청 정보
    private data class PendingRequest(
        val callbacks: MutableList<(List<String>) -> Unit> = mutableListOf(),  // 콜백 리스트로 변경
        val requestTime: Long = System.currentTimeMillis(),
        val originalPermissions: List<String> = emptyList(),
        var remainingSpecialPermissions: List<String> = emptyList()
        // specialPermissionLaunchers 제거: 이제 Delegate에서 동적으로 조회
    )

    /**
     * 권한 요청 (메인 API)
     *
     * @param context 컨텍스트
     * @param requestPermissionLauncher 일반 권한 요청 런처
     * @param permissions 요청할 권한 목록
     * @param callback 결과 콜백 (거부된 권한 목록)
     * @param preGeneratedRequestId 미리 생성된 요청 ID (Delegate 등록용)
     * @return 요청 ID (취소 시 사용)
     *
     * 참고: specialPermissionLaunchers는 이제 Delegate에서 동적으로 조회됨
     */
    @Synchronized
    fun request(
        context: Context,
        requestPermissionLauncher: ActivityResultLauncher<Array<String>>,
        permissions: List<String>,
        callback: (List<String>) -> Unit,
        preGeneratedRequestId: String? = null
    ): String {
        // 입력 검증
        if (permissions.isEmpty()) {
            callback(emptyList())
            return ""
        }

        if (isContextDestroyed(context)) {
            callback(permissions)
            return ""
        }

        // 만료된 요청 정리
        cleanupExpiredRequests()

        val remainingPermissions = context.remainPermissions(permissions)
        if (remainingPermissions.isEmpty()) {
            callback(emptyList())
            return ""
        }

        // 미리 생성된 ID 사용 (Delegate가 이미 등록되어 있음)
        val requestId = preGeneratedRequestId ?: UUID.randomUUID().toString()
        val request = PendingRequest(
            callbacks = mutableListOf(callback),  // 첫 번째 콜백 추가
            originalPermissions = permissions
        )
        pendingRequests[requestId] = request
        
        // 자동 정리 스케줄링
        scheduleCleanup()
        
        val (specialPermissions, normalPermissions) = remainingPermissions.partition {
            context.isSpecialPermission(it)
        }
        
        // 일반 권한 요청
        if (normalPermissions.isNotEmpty()) {
            try {
                requestPermissionLauncher.launch(normalPermissions.toTypedArray())
            } catch (e: Exception) {
                Logx.e("Failed to launch permission request: $e")
                // 유령 요청 방지: pendingRequests, activeDelegates 모두 정리
                pendingRequests.remove(requestId)
                unregisterDelegate(requestId)
                // 콜백 호출 (모든 권한 거부로 처리)
                invokeAllCallbacks(request, remainingPermissions)
                // 빈 문자열 반환하여 Delegate가 currentRequestId를 null로 설정하도록 함
                return ""
            }
        }

        // 특수 권한 순차 처리
        // 일반 권한이 있으면 일반 권한 완료 후 result()에서 처리됨
        if (specialPermissions.isNotEmpty() && normalPermissions.isEmpty()) {
            request.remainingSpecialPermissions = specialPermissions.drop(1)
            val firstSpecialPermission = specialPermissions.first()

            // Delegate에서 런처 동적 조회
            val launcher = getSpecialLauncher(requestId, firstSpecialPermission)
            if (launcher != null) {
                launchSpecialPermissionWithRetry(context, firstSpecialPermission, launcher, requestId)
            } else {
                Logx.e("No launcher found for special permission: $firstSpecialPermission (Delegate not registered?)")
                handleSpecialPermissionFailure(context, requestId, firstSpecialPermission)
            }
        } else if (specialPermissions.isNotEmpty() && normalPermissions.isNotEmpty()) {
            // 일반 권한과 특수 권한이 모두 있는 경우, 특수 권한은 나중에 처리
            request.remainingSpecialPermissions = specialPermissions
        }
        
        return requestId
    }


    /**
     * 일반 권한 결과 처리 (ActivityResultLauncher 콜백용)
     */
    @Synchronized
    fun result(context: Context, permissions: Map<String, Boolean>, requestId: String?) {
        if (requestId == null) return

        if (isContextDestroyed(context)) return

        val request = pendingRequests[requestId] ?: return

        val deniedPermissions = permissions.filterNot { (permission, granted) ->
            granted || context.hasPermission(permission)
        }.keys.toList()

        // 특수 권한이 있으면 계속 처리, 없으면 요청 완료
        if (request.remainingSpecialPermissions.isEmpty()) {
            pendingRequests.remove(requestId)
            unregisterDelegate(requestId)

            invokeAllCallbacks(request, deniedPermissions)
        } else {
            // 특수 권한이 남아있는 경우 다음 특수 권한 처리
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
     * 특수 권한 결과 처리 (ActivityResultLauncher 콜백용)
     */
    @Synchronized
    fun resultSpecialPermission(context: Context, permission: String, requestId: String?) {
        if (requestId == null) return

        if (isContextDestroyed(context)) return

        val request = pendingRequests[requestId] ?: run {
            Logx.w("Request $requestId not found. Process may have been killed or request already completed.")
            return
        }
        
        // 현재 특수 권한 승인 상태 확인 및 로깅
        val isGranted = context.hasPermission(permission)
        Logx.d("Special permission $permission result: ${if (isGranted) "GRANTED" else "DENIED"}")
        
        // 다음 특수 권한이 있으면 계속 처리
        if (request.remainingSpecialPermissions.isNotEmpty()) {
            val nextPermission = request.remainingSpecialPermissions.first()
            request.remainingSpecialPermissions = request.remainingSpecialPermissions.drop(1)

            // Delegate에서 런처 동적 조회
            val launcher = getSpecialLauncher(requestId, nextPermission)
            if (launcher != null) {
                launchSpecialPermissionWithRetry(context, nextPermission, launcher, requestId)
                return
            } else {
                Logx.e("No launcher available for special permission: $nextPermission (Delegate may have been destroyed)")
                handleSpecialPermissionFailure(context, requestId, nextPermission)
                return
            }
        }

        // 모든 특수 권한 처리 완료 - 최종 결과 계산
        pendingRequests.remove(requestId)
        unregisterDelegate(requestId)
        val finalDeniedPermissions = context.remainPermissions(request.originalPermissions)

        invokeAllCallbacks(request, finalDeniedPermissions)
    }

    /**
     * 요청 취소
     */
    @Synchronized
    fun cancelRequest(requestId: String) {
        pendingRequests.remove(requestId)
        unregisterDelegate(requestId)
    }

    /**
     * Delegate 등록 (권한 요청 시작 시 호출)
     * Configuration change 후 자동 재등록에도 사용됨
     */
    @Synchronized
    fun registerDelegate(requestId: String, delegate: PermissionDelegate<*>) {
        val existingRef = activeDelegates[requestId]
        val existingDelegate = existingRef?.get()

        if (existingDelegate != null && existingDelegate !== delegate) {
            Logx.d("Replacing delegate for request: $requestId (Configuration change)")
        }

        activeDelegates[requestId] = WeakReference(delegate)
        Logx.d("Registered delegate for request: $requestId")
    }

    /**
     * Delegate 등록 해제 (요청 완료 또는 취소 시)
     */
    @Synchronized
    fun unregisterDelegate(requestId: String) {
        activeDelegates.remove(requestId)
        Logx.d("Unregistered delegate for request: $requestId")
    }

    /**
     * 활성 요청 확인 (Delegate 자동 재등록 시 사용)
     */
    @Synchronized
    fun hasActiveRequest(requestId: String): Boolean {
        return pendingRequests.containsKey(requestId)
    }

    /**
     * 요청의 원본 권한 목록 조회
     */
    @Synchronized
    fun getRequestPermissions(requestId: String): Set<String>? {
        return pendingRequests[requestId]?.originalPermissions?.toSet()
    }

    /**
     * 진행 중인 요청에 콜백 추가 (중복 요청 시 사용)
     * @param requestId 요청 ID
     * @param callback 추가할 콜백
     * @param requestedPermissions 요청한 권한 목록 (검증용)
     * @return CallbackAddResult - 성공, 요청 없음, 권한 불일치 중 하나
     */
    @Synchronized
    fun addCallbackToRequest(
        requestId: String,
        callback: (List<String>) -> Unit,
        requestedPermissions: List<String>
    ): CallbackAddResult {
        // 요청을 찾을 수 없음 - 이미 완료되었거나 취소됨
        val request = pendingRequests[requestId] ?: return CallbackAddResult.REQUEST_NOT_FOUND

        // 권한 목록 검증 (순서 무관, 내용만 비교)
        val existingPermissions = request.originalPermissions.toSet()
        val newPermissions = requestedPermissions.toSet()

        if (existingPermissions != newPermissions) {
            Logx.w("Permission mismatch! Existing: $existingPermissions, Requested: $newPermissions")
            return CallbackAddResult.PERMISSION_MISMATCH
        }

        // 콜백 추가 성공
        request.callbacks.add(callback)
        Logx.d("Added callback to existing request: $requestId (total callbacks: ${request.callbacks.size})")
        return CallbackAddResult.SUCCESS
    }

    /**
     * 현재 활성화된 런처 가져오기 (내부용)
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
     * 모든 콜백 호출 (헬퍼 메서드)
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
     * 만료된 요청 정리 (5분 경과)
     */
    fun cleanupExpiredRequests() {
        val currentTime = System.currentTimeMillis()
        val expiredRequestIds = mutableListOf<String>()

        // 만료된 요청 찾기
        for ((requestId, request) in pendingRequests) {
            if (currentTime - request.requestTime > PermissionConstants.Defaults.REQUEST_TIMEOUT_MS) {
                expiredRequestIds.add(requestId)
                // 만료된 요청에 대한 모든 콜백 호출
                invokeAllCallbacks(request, request.originalPermissions)
            }
        }

        // 만료된 요청 및 Delegate 참조 모두 제거 (유령 요청 방지)
        expiredRequestIds.forEach { requestId ->
            pendingRequests.remove(requestId)
            unregisterDelegate(requestId)  // activeDelegates도 함께 정리
            Logx.d("Cleaned up expired request: $requestId")
        }

        // 다음 자동 정리 스케줄링 (요청이 남아있는 경우에만)
        if (pendingRequests.isNotEmpty()) {
            scheduleCleanup()
        }
    }
    
    /**
     * 자동 정리 스케줄링 (1분마다)
     */
    private fun scheduleCleanup() {
        cleanupRunnable?.let { mainHandler.removeCallbacks(it) }
        
        cleanupRunnable = Runnable {
            cleanupExpiredRequests()
        }
        
        mainHandler.postDelayed(cleanupRunnable!!, 60_000) // 1분 후 실행
    }

    // Private Functions
    
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
                
                // 재시도 로직 (최대 1회) - 50ms 후 비동기 재시도
                if (retryCount < 1) {
                    mainHandler.postDelayed({
                        try {
                            launchSpecialPermissionWithRetry(context, permission, launcher, requestId, retryCount + 1)
                        } catch (retryException: Exception) {
                            Logx.e("Retry failed for special permission request $retryException" )
                            handleSpecialPermissionFailure(context, requestId, permission)
                        }
                    }, 50)
                } else {
                    handleSpecialPermissionFailure(context, requestId, permission)
                }
            }
        } else {
            Logx.e("Cannot create intent for special permission: $permission")
            handleSpecialPermissionFailure(context, requestId, permission)
        }
    }
    
    private fun handleSpecialPermissionFailure(context: Context?, requestId: String, permission: String) {
        val request = pendingRequests[requestId] ?: return

        val nextPermission = request.remainingSpecialPermissions.firstOrNull()
        if (nextPermission != null && context != null) {
            // Delegate에서 런처 동적 조회
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
        unregisterDelegate(requestId)

        invokeAllCallbacks(request, deniedPermissions)
    }

    private fun isContextDestroyed(context: Context): Boolean {
        return if (context is Activity) {
            context.isDestroyed || context.isFinishing
        } else {
            false
        }
    }

    private fun createSpecialPermissionIntent(context: Context, permission: String): Intent? {
        // API 레벨 체크
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