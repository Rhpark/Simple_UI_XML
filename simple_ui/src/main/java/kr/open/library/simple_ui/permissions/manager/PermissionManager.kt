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
import java.util.UUID

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
    
    
    // 메인 스레드 핸들러 (재시도용)
    private val mainHandler = Handler(Looper.getMainLooper())
    
    // 자동 정리 작업
    private var cleanupRunnable: Runnable? = null
    
    // 내부 요청 정보
    private data class PendingRequest(
        val callback: (List<String>) -> Unit,
        val requestTime: Long = System.currentTimeMillis(),
        val originalPermissions: List<String> = emptyList(),
        var remainingSpecialPermissions: List<String> = emptyList(),
        val specialPermissionLaunchers: Map<String, ActivityResultLauncher<Intent>> = emptyMap()
    )

    /**
     * 권한 요청 (메인 API)
     * 
     * @param context 컨텍스트
     * @param requestPermissionLauncher 일반 권한 요청 런처
     * @param specialPermissionLaunchers 특수 권한별 런처 맵 (권한 -> 런처)
     * @param permissions 요청할 권한 목록
     * @param callback 결과 콜백 (거부된 권한 목록)
     * @return 요청 ID (취소 시 사용)
     */
    @Synchronized
    fun request(
        context: Context,
        requestPermissionLauncher: ActivityResultLauncher<Array<String>>,
        specialPermissionLaunchers: Map<String, ActivityResultLauncher<Intent>>,
        permissions: List<String>,
        callback: (List<String>) -> Unit
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
        
        val requestId = UUID.randomUUID().toString()
        val request = PendingRequest(
            callback = callback,
            originalPermissions = permissions,
            specialPermissionLaunchers = specialPermissionLaunchers
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
                Logx.e("Failed to launch permission request $e" )
                pendingRequests.remove(requestId)
                callback(remainingPermissions)
                return requestId
            }
        }
        
        // 특수 권한 순차 처리
        if (specialPermissions.isNotEmpty()) {
            request.remainingSpecialPermissions = specialPermissions.drop(1)
            val firstSpecialPermission = specialPermissions.first()
            val launcher = specialPermissionLaunchers[firstSpecialPermission]
            if (launcher != null) {
                launchSpecialPermissionWithRetry(context, firstSpecialPermission, launcher, requestId)
            } else {
                Logx.e("No launcher found for special permission: $firstSpecialPermission")
                handleSpecialPermissionFailure(context, requestId, firstSpecialPermission)
            }
        }
        
        return requestId
    }


    /**
     * 일반 권한 결과 처리 (ActivityResultLauncher 콜백용)
     */
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
            request.callback(deniedPermissions)
        }
        // 특수 권한이 있는 경우는 특수 권한 처리 완료 후 최종 결과 계산
    }

    /**
     * 특수 권한 결과 처리 (ActivityResultLauncher 콜백용)
     */
    fun resultSpecialPermission(context: Context, permission: String, requestId: String?) {
        if (requestId == null) return
        
        if (isContextDestroyed(context)) return

        val request = pendingRequests[requestId] ?: return
        
        // 현재 특수 권한 승인 상태 확인 및 로깅
        val isGranted = context.hasPermission(permission)
        Logx.d("Special permission $permission result: ${if (isGranted) "GRANTED" else "DENIED"}")
        
        // 다음 특수 권한이 있으면 계속 처리
        if (request.remainingSpecialPermissions.isNotEmpty()) {
            val nextPermission = request.remainingSpecialPermissions.first()
            request.remainingSpecialPermissions = request.remainingSpecialPermissions.drop(1)
            
            // 다음 특수 권한 요청 - 해당 권한의 런처 사용
            val launcher = request.specialPermissionLaunchers[nextPermission]
            if (launcher != null) {
                launchSpecialPermissionWithRetry(context, nextPermission, launcher, requestId)
                return
            } else {
                Logx.e("No launcher found for special permission: $nextPermission")
                handleSpecialPermissionFailure(context, requestId, nextPermission)
                return
            }
        }
        
        // 모든 특수 권한 처리 완료 - 최종 결과 계산
        pendingRequests.remove(requestId)
        val finalDeniedPermissions = context.remainPermissions(request.originalPermissions)
        request.callback(finalDeniedPermissions)
    }

    /**
     * 요청 취소
     */
    fun cancelRequest(requestId: String) {
        pendingRequests.remove(requestId)
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
                // 만료된 요청에 대한 콜백 호출
                try {
                    request.callback(request.originalPermissions)
                } catch (e: Exception) {
                    Logx.e("Error in expired request callback $e")
                }
            }
        }
        
        // 만료된 요청 제거
        expiredRequestIds.forEach { requestId ->
            pendingRequests.remove(requestId)
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
            val launcher = request.specialPermissionLaunchers[nextPermission]
            if (launcher != null) {
                request.remainingSpecialPermissions = request.remainingSpecialPermissions.drop(1)
                launchSpecialPermissionWithRetry(context, nextPermission, launcher, requestId)
                return
            } else {
                Logx.e("No launcher found for special permission: $nextPermission")
            }
        }

        val deniedPermissions = buildList {
            add(permission)
            addAll(request.remainingSpecialPermissions)
        }

        pendingRequests.remove(requestId)
        try {
            request.callback(deniedPermissions)
        } catch (e: Exception) {
            Logx.e("Error in permission failure callback $e")
        }
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