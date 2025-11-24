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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.permissions.manager.PermissionManager
import kr.open.library.simple_ui.permissions.extentions.hasPermission
import kr.open.library.simple_ui.permissions.manager.CallbackAddResult
import kr.open.library.simple_ui.permissions.vo.PermissionSpecialType

/**
 * Coordinates permission requests while surviving configuration changes.<br><br>
 * 구성 변경 동안에도 권한 요청을 안전하게 이어 주는 조정자입니다.<br>
 */
public class PermissionDelegate<T : Any>(private val contextProvider: T) {

    protected val permissionManager = PermissionManager.getInstance()
    private var currentRequestId: String? = null
    private var hasRestoredState = false
    private val KEY_REQUEST_ID = "KEY_PERMISSIONS_REQUEST_ID"

    // Self-managed CoroutineScope for calling suspend functions.<br><br>
    // suspend 함수 호출을 위한 자체 관리 CoroutineScope입니다.<br>
    private val delegateScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val specialPermissionLauncher = buildMap<String, ActivityResultLauncher<Intent>> {
        PermissionSpecialType.entries.forEach { put(it.permission, createSpecialLauncher(it.permission)) }
    }

    private val normalPermissionLauncher: ActivityResultLauncher<Array<String>> = when (contextProvider) {
        is ComponentActivity -> contextProvider.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            delegateScope.launch {
                permissionManager.result(getContext(), permissions, currentRequestId)
            }
        }

        is Fragment -> contextProvider.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            delegateScope.launch {
                permissionManager.result(getContext(), permissions, currentRequestId)
            }
        }

        else -> throw IllegalArgumentException("Unsupported context provider type")
    }

    init {
        /*
         * Observes lifecycle transitions to automatically re-register or clean up delegates.<br><br>
         * 라이프사이클 변화를 감시해 Delegate를 자동으로 재등록하거나 정리합니다.<br>
         */
        val lifecycleOwner = when (contextProvider) {
            is ComponentActivity -> contextProvider.lifecycle
            is Fragment -> contextProvider.lifecycle
            else -> throw IllegalArgumentException("Unsupported context provider type")
        }
        lifecycleOwner.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        /*
                         * onRestoreInstanceState() runs between onCreate() and onStart().<br><br>
                         * onRestoreInstanceState()는 onCreate()와 onStart() 사이에 실행됩니다.<br>
                         */
                        if (hasRestoredState) {
                            attemptAutoReregistration()
                            hasRestoredState = false
                        }
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        /*
                         * Ensures pending requests are disposed when the owner is destroyed.<br><br>
                         * 소유자가 파괴될 때 진행 중인 요청을 정리합니다.<br>
                         */
                        cleanup()
                        delegateScope.cancel()
                    }
                    else -> {}
                }
            }
        })
    }


    /**
     * Releases any pending request when the host is no longer valid.<br><br>
     * 호스트가 더 이상 유효하지 않을 때 남은 요청을 정리합니다.<br>
     */
    protected fun cleanup() {
        if (shouldClearRequestId()) {
            currentRequestId?.let { requestId ->
                /*
                 * Uses an independent scope to ensure cleanup completes even after delegateScope is cancelled.<br><br>
                 * delegateScope이 취소된 후에도 정리가 완료되도록 독립적인 scope를 사용합니다.<br>
                 */
                CoroutineScope(Dispatchers.Main.immediate).launch {
                    permissionManager.cancelRequest(requestId)
                    permissionManager.unregisterDelegate(requestId)
                }
            }
            currentRequestId = null
        }
    }

    /**
     * Decides whether the stored requestId should be nulled out.<br><br>
     * 저장된 requestId를 비워야 하는지 여부를 결정합니다.<br>
     *
     * @return True if configuration is not changing and cleanup is safe.<br><br>
     *         구성 변경 중이 아니어서 정리해도 안전할 때 true 를 반환합니다.<br>
     */
    private fun shouldClearRequestId(): Boolean = when (contextProvider) {
        is ComponentActivity -> contextProvider.isFinishing && !contextProvider.isChangingConfigurations
        is Fragment -> {
            val hostActivity = contextProvider.activity
            val isChanging = hostActivity?.isChangingConfigurations ?: false
            contextProvider.isRemoving && !isChanging
        }
        else -> true
    }

    /**
     * Saves the active request identifier so it can be restored later.<br><br>
     * 이후 복원을 위해 진행 중인 요청 식별자를 저장합니다.<br>
     *
     * @param outState Bundle that will persist across process death.<br><br>
     *                 프로세스 재생성 시 유지되는 Bundle 입니다.<br>
     */
    fun onSaveInstanceState(outState: Bundle) {
        currentRequestId?.let { outState.putString(KEY_REQUEST_ID, it) }
    }

    /**
     * Restores the pending request identifier from a saved state bundle.<br><br>
     * 저장된 상태 번들에서 대기 중인 요청 식별자를 복원합니다.<br>
     *
     * @param savedState Bundle provided by the host lifecycle.<br><br>
     *                   호스트 라이프사이클에서 전달된 Bundle 입니다.<br>
     */
    fun onRestoreInstanceState(savedState: Bundle?) {
        currentRequestId = savedState?.getString(KEY_REQUEST_ID)
        hasRestoredState = true

        /*
         * Attempts eager re-registration in case ON_START already passed.<br><br>
         * ON_START 단계가 지나간 뒤라도 즉시 재등록을 시도합니다.<br>
         */
        if (currentRequestId != null) {
            attemptAutoReregistration()
        }
    }

    /**
     * Tries to rebind this delegate after a configuration change.<br><br>
     * 구성 변경 이후 Delegate를 다시 연결하려고 시도합니다.<br>
     */
    private fun attemptAutoReregistration() {
        currentRequestId?.let { requestId ->
            delegateScope.launch {
                if (permissionManager.hasActiveRequest(requestId)) {
                    permissionManager.registerDelegate(requestId, this@PermissionDelegate)
                    Logx.d("Auto-reregistered delegate for request: $requestId")
                } else {
                    Logx.w("Attempted to reregister for inactive request: $requestId. Request may have been completed or cancelled.")
                    currentRequestId = null
                }
            }
        }
    }

    /**
     * Exposes the active permission request identifier for debugging.<br><br>
     * 디버깅을 위해 현재 활성 권한 요청 ID를 제공합니다.<br>
     *
     * @return Current requestId or null when idle.<br><br>
     *         진행 중인 요청이 없다면 null 을 반환합니다.<br>
     */
    fun getCurrentRequestId(): String? = currentRequestId

    /**
     * Provides the launcher registered for a specific special permission.<br><br>
     * 특정 특수 권한에 등록된 런처를 반환합니다.<br>
     *
     * @param permission Permission key tied to the launcher.<br><br>
     *                   런처와 연결된 권한 식별자입니다.<br>
     * @return Launcher instance or null if none exists.<br><br>
     *         등록된 런처가 없으면 null 을 반환합니다.<br>
     */
    internal fun getSpecialLauncher(permission: String): ActivityResultLauncher<Intent>? {
        return specialPermissionLauncher[permission]
    }


    /**
     * Requests one or more permissions, deduplicating concurrent calls.<br><br>
     * 중복 호출을 제어하면서 하나 이상의 권한을 요청합니다.<br>
     *
     * @param permissions Ordered list of permissions to request.<br><br>
     *                    요청할 권한 순서를 유지한 목록입니다.<br>
     * @param onResult Callback invoked with denied permissions once finished.<br><br>
     *                 완료 후 거부된 권한 목록을 전달받는 콜백입니다.<br>
     */
    public fun requestPermissions(
        permissions: List<String>,
        onResult: (deniedPermissions: List<String>) -> Unit
    ) {
        delegateScope.launch {
            /*
             * Avoids duplicating requests by attaching callbacks to the in-flight one.<br><br>
             * 진행 중인 요청이 있다면 콜백만 추가해 중복 실행을 방지합니다.<br>
             */
            if (currentRequestId != null && permissionManager.hasActiveRequest(currentRequestId!!)) {
                /*
                 * Verifies whether the new request matches the existing permission set.<br><br>
                 * 새 요청이 기존 권한 집합과 동일한지 재검증합니다.<br>
                 */
                val result = permissionManager.addCallbackToRequest(
                    requestId = currentRequestId!!,
                    callback = onResult,
                    requestedPermissions = permissions
                )

                when (result) {
                    CallbackAddResult.SUCCESS -> {
                        Logx.d("Added callback to existing request: $currentRequestId (same permissions)")
                        return@launch
                    }
                    CallbackAddResult.PERMISSION_MISMATCH -> {
                        /*
                         * Ignores mismatched requests to prevent leaking stale callbacks.<br><br>
                         * 일치하지 않는 요청은 무시하여 잘못된 콜백 연결을 방지합니다.<br>
                         */
                        val existingPermissions = permissionManager.getRequestPermissions(currentRequestId!!)
                        Logx.w("Cannot add callback: permission mismatch. Existing: $existingPermissions, Requested: ${permissions.toSet()}")
                        Logx.w("Ignoring duplicate request with different permissions. Please wait for current request to complete.")
                        return@launch
                    }
                    CallbackAddResult.REQUEST_NOT_FOUND -> {
                        /*
                         * Handles the race where the previous request finished between checks.<br><br>
                         * 직전 요청이 막 끝난 레이스 컨디션을 감지해 새 요청을 준비합니다.<br>
                         */
                        Logx.d("Race condition detected: request $currentRequestId just completed. Starting new request.")
                        currentRequestId = null
                        /*
                         * Continues below to start a brand-new permission request.<br><br>
                         * 이어지는 코드에서 새로운 권한 요청을 시작합니다.<br>
                         */
                    }
                }
            }

            /*
             * Pre-registers a delegate so PermissionManager can look it up immediately.<br><br>
             * PermissionManager 가 바로 찾을 수 있도록 먼저 Delegate를 등록합니다.<br>
             */
            val tempRequestId = java.util.UUID.randomUUID().toString()
            permissionManager.registerDelegate(tempRequestId, this@PermissionDelegate)

            currentRequestId = permissionManager.request(
                context = getContext(),
                requestPermissionLauncher = normalPermissionLauncher,
                permissions = permissions,
                callback = onResult,
                /*
                 * Supplies the pre-generated ID so PermissionManager can reuse it.<br><br>
                 * PermissionManager 가 재사용할 수 있도록 선 생성한 ID를 전달합니다.<br>
                 */
                preGeneratedRequestId = tempRequestId
            )

            /*
             * Clean up the delegate if the request failed to launch and returned empty.<br><br>
             * 요청 실행이 실패해 빈 문자열을 반환하면 Delegate를 정리합니다.<br>
             */
            if (currentRequestId.isNullOrEmpty()) {
                permissionManager.unregisterDelegate(tempRequestId)
                currentRequestId = null
            }
        }
    }


    /**
     * Checks whether every permission in the list is already granted.<br><br>
     * 목록의 모든 권한이 이미 허용되었는지 확인합니다.<br>
     *
     * @param permissions Permissions to verify.<br><br>
     *                    확인할 권한 목록입니다.<br>
     * @return True if all permissions are granted.<br><br>
     *         모두 허용된 경우 true 를 반환합니다.<br>
     */
    fun arePermissionsGranted(permissions: List<String>): Boolean = permissions.all { isPermissionGranted(it) }

    /**
     * Returns the subset of permissions that remain denied.<br><br>
     * 여전히 거부 상태인 권한 목록을 반환합니다.<br>
     *
     * @param permissions Permissions to inspect.<br><br>
     *                    상태를 확인할 권한 목록입니다.<br>
     * @return Permissions that are not yet granted.<br><br>
     *         아직 허용되지 않은 권한 목록입니다.<br>
     */
    fun getDeniedPermissions(permissions: List<String>): List<String> =
        permissions.filter { !isPermissionGranted(it) }


    /**
     * Resolves the Android Context from the backing provider.<br><br>
     * 전달받은 provider 에서 Android Context 를 추출합니다.<br>
     *
     * @return Host context used for permission APIs.<br><br>
     *         권한 API 호출에 사용할 호스트 Context 입니다.<br>
     */
    private fun getContext() = when (contextProvider) {
        is ComponentActivity -> contextProvider
        is Fragment -> contextProvider.requireContext()
        else -> throw IllegalArgumentException("Unsupported context provider type")
    }

    /**
     * Checks whether a single permission has already been granted.<br><br>
     * 단일 권한의 허용 여부를 확인합니다.<br>
     *
     * @param permission Permission to check.<br><br>
     *                   확인할 권한입니다.<br>
     * @return True if the permission has been granted.<br><br>
     *         권한이 허용되었다면 true 를 반환합니다.<br>
     */
    fun isPermissionGranted(permission: String): Boolean = getContext().hasPermission(permission)


    /**
     * Registers a launcher dedicated to a special permission.<br><br>
     * 특정 특수 권한을 처리할 런처를 등록합니다.<br>
     *
     * @param permission Permission tied to the launcher.<br><br>
     *                   런처와 연결할 권한입니다.<br>
     * @return ActivityResultLauncher instance scoped to the host.<br><br>
     *         호스트 범위에 묶인 ActivityResultLauncher 를 반환합니다.<br>
     */
    protected fun createSpecialLauncher(permission: String) = when (contextProvider) {
        is ComponentActivity -> contextProvider.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            delegateScope.launch {
                permissionManager.resultSpecialPermission(getContext(), permission, currentRequestId)
            }
        }

        is Fragment -> contextProvider.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            delegateScope.launch {
                permissionManager.resultSpecialPermission(getContext(), permission, currentRequestId)
            }
        }
        else -> throw IllegalArgumentException("Unsupported context provider type")
    }
}