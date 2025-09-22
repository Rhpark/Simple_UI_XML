package kr.open.library.simple_ui.permissions.register

import android.Manifest
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kr.open.library.simple_ui.permissions.manager.PermissionManager
import kr.open.library.simple_ui.permissions.extentions.hasPermission

public class PermissionDelegate<T: Any>(private val contextProvider: T) {
    protected val permissionManager = PermissionManager.getInstance()
    private var currentRequestId: String? = null


    private val specialPermissionLauncher : Map<String, ActivityResultLauncher<Intent>>  by lazy {
        mapOf(
            Manifest.permission.SYSTEM_ALERT_WINDOW to getSpecialResult(Manifest.permission.SYSTEM_ALERT_WINDOW),
            Manifest.permission.WRITE_SETTINGS to getSpecialResult(Manifest.permission.WRITE_SETTINGS),
            Manifest.permission.PACKAGE_USAGE_STATS to getSpecialResult(Manifest.permission.PACKAGE_USAGE_STATS),
            Manifest.permission.MANAGE_EXTERNAL_STORAGE to getSpecialResult(Manifest.permission.MANAGE_EXTERNAL_STORAGE),
            Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS to getSpecialResult(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS),
            Manifest.permission.SCHEDULE_EXACT_ALARM to getSpecialResult(Manifest.permission.SCHEDULE_EXACT_ALARM),
            Manifest.permission.BIND_ACCESSIBILITY_SERVICE to getSpecialResult(Manifest.permission.BIND_ACCESSIBILITY_SERVICE),
            Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE to getSpecialResult(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE),
        )
    }

    init {
        // Lifecycle 이벤트를 감지하여 자동으로 정리
        val lifecycleOwner = when(contextProvider) {
            is ComponentActivity -> contextProvider.lifecycle
            is Fragment -> contextProvider.lifecycle
            else -> throw IllegalArgumentException("Unsupported context provider type")
        }
        lifecycleOwner.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    cleanup()
                }
            }
        })
    }


    /**
     * 리소스 정리
     */
    protected open fun cleanup() {
        currentRequestId = null
        // 필요시 추가 정리 작업
    }

    /**
     * 현재 권한 요청 ID 반환 (디버깅용)
     */
    fun getCurrentRequestId(): String? = currentRequestId


    /**
     * 권한 요청 메인 함수
     * @param permissions 요청할 권한 목록
     * @param onResult 권한 요청 결과 콜백 (거부된 권한 목록을 반환)
     */
    public fun requestPermissions(
        permissions: List<String>,
        onResult: (deniedPermissions: List<String>) -> Unit
    ) {
        currentRequestId = permissionManager.request(
            context = getContext(),
            requestPermissionLauncher = getNormalPermissionLauncher(),
            specialPermissionLaunchers = specialPermissionLauncher,
            permissions = permissions,
            callback = onResult
        )
    }

    private fun getNormalPermissionLauncher() = when (contextProvider) {
        is ComponentActivity -> contextProvider.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissionManager.result(getContext(), permissions, currentRequestId)
        }

        is Fragment -> contextProvider.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissionManager.result(getContext(), permissions, currentRequestId)
        }

        else -> throw IllegalArgumentException("Unsupported context provider type")
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


    protected fun getSpecialResult(permission: String) = when (contextProvider) {
        is ComponentActivity -> contextProvider.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                permissionManager.resultSpecialPermission(getContext(), permission, currentRequestId)
            }

        is Fragment ->  contextProvider.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            permissionManager.resultSpecialPermission(getContext(), permission, currentRequestId)
        }
        else -> throw IllegalArgumentException("Unsupported context provider type")
    }
}