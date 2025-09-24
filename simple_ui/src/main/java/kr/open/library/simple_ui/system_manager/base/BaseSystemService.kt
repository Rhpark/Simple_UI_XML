package kr.open.library.simple_ui.system_manager.base

import android.content.Context
import kr.open.library.simple_ui.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.permissions.extentions.remainPermissions


/**
 * Base class for system services with integrated Result pattern support.
 * Result 패턴 지원이 통합된 시스템 서비스의 기본 클래스입니다.
 *
 * This class provides:
 * 이 클래스는 다음을 제공합니다:
 * - Permission management / 권한 관리
 * - Common error handling with Result pattern / Result 패턴을 통한 공통 오류 처리
 * - Standardized error categorization / 표준화된 오류 분류
 * - Utility methods for safe operations / 안전한 작업을 위한 유틸리티 메서드
 *
 * @param context The application context.
 * @param context 애플리케이션 컨텍스트.
 * @param requiredPermissions The list of required permissions.
 * @param requiredPermissions 필요한 권한 목록입니다.
 */
public abstract class BaseSystemService(
    protected val context: Context, 
    private val requiredPermissions: List<String>? = null
) {

    private var remainPermissions = emptyList<String>()

    init {
        requiredPermissions?.let {
            remainPermissions = context.remainPermissions(it)
            if(remainPermissions.isEmpty()) {
                Logx.d("All required permissions granted for ${this::class.simpleName}")
            } else {
                Logx.w("Missing permissions for ${this::class.simpleName}: $remainPermissions")
            }
        }
    }

    /**
     * Gets the list of denied permissions.
     * 거부된 권한 목록을 가져옵니다.
     */
    protected fun getDeniedPermissionList(): List<String> = remainPermissions

    /**
     * Checks if all required permissions have been granted.
     * 모든 필수 권한이 부여되었는지 확인합니다.
     *
     * @return Returns true if all permissions have been granted, false otherwise.
     * @return 모든 권한이 부여된 경우 true, 그렇지 않으면 false를 반환.
     */
    protected fun isPermissionAllGranted(): Boolean = remainPermissions.isEmpty()


    /**
     * Simple safe execution with basic error handling.
     * 기본 오류 처리가 포함된 간단한 안전 실행입니다.
     */
    protected inline fun <T> tryCatchSystemManager(defaultValue: T, block: () -> T): T {

        val deniedPermissions = getDeniedPermissionList()
        if(deniedPermissions.isNotEmpty()) {
            Logx.w("${this::class.simpleName}: Missing permissions!!! - $deniedPermissions")
        }
        return safeCatch(
            block = block,
            onCatch = { e->
                Logx.e("${this::class.simpleName}: Error occurred : ${e.message}")
                defaultValue
            }
        )
    }




    /**
     * Refreshes the permission status. Call this after requesting permissions.
     * 권한 상태를 새로고침합니다. 권한 요청 후 이를 호출하세요.
     */
    public fun refreshPermissions() {
        requiredPermissions?.let {
            remainPermissions = context.remainPermissions(it)
            if (remainPermissions.isEmpty()) {
                Logx.d("All permissions granted after refresh for ${this::class.simpleName}")
            }
        }
    }

    /**
     * Gets information about required permissions and their status.
     * 필요한 권한과 그 상태에 대한 정보를 가져옵니다.
     */
    public fun getPermissionInfo(): Map<String, Boolean> {
        return requiredPermissions?.associateWith { permission ->
            !remainPermissions.contains(permission)
        } ?: emptyMap()
    }

    /**
     * Checks if a specific permission is granted.
     * 특정 권한이 부여되었는지 확인합니다.
     */
    public fun isPermissionGranted(permission: String): Boolean {
        return !remainPermissions.contains(permission)
    }

    /**
     * Called when the service is being destroyed. Override to perform cleanup.
     * 서비스가 소멸될 때 호출됩니다. 정리 작업을 수행하려면 재정의하세요.
     */
    public open fun onDestroy() {
        Logx.d("${this::class.simpleName} destroyed")
    }
}