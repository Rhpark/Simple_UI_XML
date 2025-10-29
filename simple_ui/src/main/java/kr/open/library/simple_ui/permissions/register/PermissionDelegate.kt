package kr.open.library.simple_ui.permissions.register

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.permissions.manager.PermissionManager
import kr.open.library.simple_ui.permissions.extentions.hasPermission
import kr.open.library.simple_ui.permissions.vo.PermissionSpecialType

public class PermissionDelegate<T: Any>(private val contextProvider: T) {

    protected val permissionManager = PermissionManager.getInstance()
    private var currentRequestId: String? = null
    private var hasRestoredState = false
    private val KEY_REQUEST_ID = "KEY_PERMISSIONS_REQUEST_ID"

    private val specialPermissionLauncher = buildMap<String, ActivityResultLauncher<Intent>> {
        PermissionSpecialType.entries.forEach { put(it.permission, createSpecialLauncher(it.permission)) }
    }

    private val normalPermissionLauncher: ActivityResultLauncher<Array<String>> = when (contextProvider) {
        is ComponentActivity -> contextProvider.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissionManager.result(getContext(), permissions, currentRequestId)
        }

        is Fragment -> contextProvider.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissionManager.result(getContext(), permissions, currentRequestId)
        }

        else -> throw IllegalArgumentException("Unsupported context provider type")
    }

    init {
        // Lifecycle 이벤트를 감지하여 자동으로 정리 및 재등록
        val lifecycleOwner = when(contextProvider) {
            is ComponentActivity -> contextProvider.lifecycle
            is Fragment -> contextProvider.lifecycle
            else -> throw IllegalArgumentException("Unsupported context provider type")
        }
        lifecycleOwner.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        // onRestoreInstanceState()는 onCreate()와 onStart() 사이에 호출됨
                        if (hasRestoredState) {
                            attemptAutoReregistration()
                            hasRestoredState = false
                        }
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        cleanup()
                    }
                    else -> {}
                }
            }
        })
    }


    /**
     * 리소스 정리
     */
    protected open fun cleanup() {
        if (shouldClearRequestId()) {
            currentRequestId?.let {
                permissionManager.cancelRequest(it)
                permissionManager.unregisterDelegate(it)
            }
            currentRequestId = null
        }
    }

    private fun shouldClearRequestId(): Boolean = when (contextProvider) {
        is ComponentActivity -> contextProvider.isFinishing && !contextProvider.isChangingConfigurations
        is Fragment -> {
            val hostActivity = contextProvider.activity
            val isChanging = hostActivity?.isChangingConfigurations ?: false
            contextProvider.isRemoving && !isChanging
        }
        else -> true
    }

    fun onSaveInstanceState(outState: Bundle) {
        currentRequestId?.let { outState.putString(KEY_REQUEST_ID, it) }
    }

    fun onRestoreInstanceState(savedState: Bundle?) {
        currentRequestId = savedState?.getString(KEY_REQUEST_ID)
        hasRestoredState = true

        // 즉시 재등록 시도 (ON_START가 이미 지나갔을 수도 있음)
        if (currentRequestId != null) {
            attemptAutoReregistration()
        }
    }

    /**
     * 자동 재등록 시도 (Configuration Change 후)
     */
    private fun attemptAutoReregistration() {
        currentRequestId?.let { requestId ->
            if (permissionManager.hasActiveRequest(requestId)) {
                permissionManager.registerDelegate(requestId, this)
                Logx.d("Auto-reregistered delegate for request: $requestId")
            } else {
                Logx.w("Attempted to reregister for inactive request: $requestId. Request may have been completed or cancelled.")
                currentRequestId = null
            }
        }
    }

    /**
     * 현재 권한 요청 ID 반환 (디버깅용)
     */
    fun getCurrentRequestId(): String? = currentRequestId

    /**
     * 특수 권한 런처 가져오기 (PermissionManager가 호출)
     * internal: 같은 패키지 내에서만 접근 가능
     */
    internal fun getSpecialLauncher(permission: String): ActivityResultLauncher<Intent>? {
        return specialPermissionLauncher[permission]
    }


    /**
     * 권한 요청 메인 함수
     * @param permissions 요청할 권한 목록
     * @param onResult 권한 요청 결과 콜백 (거부된 권한 목록을 반환)
     */
    public fun requestPermissions(
        permissions: List<String>,
        onResult: (deniedPermissions: List<String>) -> Unit
    ) {
        // 진행 중인 요청 확인
        if (currentRequestId != null && permissionManager.hasActiveRequest(currentRequestId!!)) {
            // 같은 권한 요청인지 확인 후 콜백 추가
            val result = permissionManager.addCallbackToRequest(
                requestId = currentRequestId!!,
                callback = onResult,
                requestedPermissions = permissions
            )

            when (result) {
                kr.open.library.simple_ui.permissions.manager.CallbackAddResult.SUCCESS -> {
                    Logx.d("Added callback to existing request: $currentRequestId (same permissions)")
                    return
                }
                kr.open.library.simple_ui.permissions.manager.CallbackAddResult.PERMISSION_MISMATCH -> {
                    // 다른 권한 요청 → 무시 (더 안전함)
                    val existingPermissions = permissionManager.getRequestPermissions(currentRequestId!!)
                    Logx.w("Cannot add callback: permission mismatch. Existing: $existingPermissions, Requested: ${permissions.toSet()}")
                    Logx.w("Ignoring duplicate request with different permissions. Please wait for current request to complete.")
                    return
                }
                kr.open.library.simple_ui.permissions.manager.CallbackAddResult.REQUEST_NOT_FOUND -> {
                    // 레이스 컨디션 감지: 요청이 막 완료됨
                    Logx.d("Race condition detected: request $currentRequestId just completed. Starting new request.")
                    currentRequestId = null
                    // 아래로 진행하여 새 요청 시작
                }
            }
        }

        // 먼저 임시 ID 생성 및 Delegate 등록 (request() 호출 전에!)
        val tempRequestId = java.util.UUID.randomUUID().toString()
        permissionManager.registerDelegate(tempRequestId, this)

        currentRequestId = permissionManager.request(
            context = getContext(),
            requestPermissionLauncher = normalPermissionLauncher,
            permissions = permissions,
            callback = onResult,
            preGeneratedRequestId = tempRequestId  // 미리 생성한 ID 전달
        )

        // request()가 실패하면 빈 문자열 반환하므로 정리 필요
        if (currentRequestId.isNullOrEmpty()) {
            permissionManager.unregisterDelegate(tempRequestId)
            currentRequestId = null
        }
    }


    /**
     * 여러 권한의 현재 상태 확인
     */
    fun arePermissionsGranted(permissions: List<String>): Boolean = permissions.all { isPermissionGranted(it) }

    /**
     * 거부된 권한들 반환
     */
    fun getDeniedPermissions(permissions: List<String>): List<String> =
        permissions.filter { !isPermissionGranted(it) }


    private fun getContext() = when (contextProvider) {
        is ComponentActivity -> contextProvider
        is Fragment -> contextProvider.requireContext()
        else -> throw IllegalArgumentException("Unsupported context provider type")
    }

    /**
     * 특정 권한의 현재 상태 확인
     */
    fun isPermissionGranted(permission: String): Boolean =  getContext().hasPermission(permission)


    /**
     * 일반 권한 결과 처리
     */
    private fun handlePermissionResult(permissions: Map<String, Boolean>) =
        permissionManager.result(getContext(), permissions, currentRequestId)

    /**
     * 특수 권한 결과 처리
     */
    protected fun handleSpecialPermissionResult(permission: String) =
        permissionManager.resultSpecialPermission(getContext(), permission, currentRequestId)


    protected fun createSpecialLauncher(permission: String) = when (contextProvider) {
        is ComponentActivity -> contextProvider.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                permissionManager.resultSpecialPermission(getContext(), permission, currentRequestId)
            }

        is Fragment ->  contextProvider.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            permissionManager.resultSpecialPermission(getContext(), permission, currentRequestId)
        }
        else -> throw IllegalArgumentException("Unsupported context provider type")
    }
}