package kr.open.library.simple_ui.core.system_manager.base

import android.content.Context
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.permissions.extentions.remainPermissions

/**
 * Base class for system services with integrated Result pattern support.<br><br>
 * Result 패턴 지원이 통합된 시스템 서비스의 기본 클래스입니다.<br>
 *
 * This class provides:<br>
 * - Permission management<br>
 * - Common error handling with Result pattern<br>
 * - Standardized error categorization<br>
 * - Utility methods for safe operations<br><br>
 *
 * 이 클래스는 다음을 제공합니다:<br>
 * - 권한 관리<br>
 * - Result 패턴을 통한 공통 오류 처리<br>
 * - 표준화된 오류 분류<br>
 * - 안전한 작업을 위한 유틸리티 메서드<br>
 *
 * @param context The application context.<br><br>
 *                애플리케이션 컨텍스트.
 * @param requiredPermissions The list of required permissions.<br><br>
 *                            필요한 권한 목록입니다.
 */
public abstract class BaseSystemService(
    protected val context: Context,
    private val requiredPermissions: List<String>? = null,
) {
    private var remainPermissions = emptyList<String>()

    init {
        requiredPermissions?.let {
            remainPermissions = context.remainPermissions(it)
            if (remainPermissions.isEmpty()) {
                Logx.d("All required permissions granted for ${this::class.simpleName}")
            } else {
                Logx.w("Missing permissions for ${this::class.simpleName}: $remainPermissions")
            }
        }
    }

    /**
     * Gets the list of denied permissions.<br><br>
     * 거부된 권한 목록을 가져옵니다.<br>
     *
     * @return The list of permissions that have been denied.<br><br>
     *         거부된 권한 목록.<br>
     */
    protected fun getDeniedPermissionList(): List<String> {
        refreshPermissions()
        return remainPermissions
    }

    /**
     * Checks if all required permissions have been granted.<br><br>
     * 모든 필수 권한이 부여되었는지 확인합니다.<br>
     *
     * @return Returns true if all permissions have been granted, false otherwise.<br><br>
     *         모든 권한이 부여된 경우 true, 그렇지 않으면 false를 반환.<br>
     */
    protected fun isPermissionAllGranted(): Boolean = remainPermissions.isEmpty()

    /**
     * Simple safe execution with basic error handling.
     * Executes the given block if all required permissions are granted.
     * If permissions are missing, returns the default value and logs a warning.
     * If an exception occurs during execution, catches it and returns the default value.<br><br>
     *
     * 기본 오류 처리가 포함된 간단한 안전 실행입니다.
     * 모든 필수 권한이 부여된 경우 주어진 블록을 실행합니다.
     * 권한이 누락된 경우 기본값을 반환하고 경고를 로깅합니다.
     * 실행 중 예외가 발생하면 이를 포착하고 기본값을 반환합니다.<br>
     *
     * @param T The type of the return value.<br><br>
     *          반환값의 타입.
     * @param defaultValue The default value to return if permissions are missing or an error occurs.<br><br>
     *                     권한이 누락되거나 오류가 발생할 경우 반환할 기본값.
     * @param block The operation to execute if all permissions are granted.<br><br>
     *              모든 권한이 부여된 경우 실행할 작업.
     * @return Returns the result of the block execution if successful, or defaultValue if permissions are missing or an error occurs.<br><br>
     *         성공 시 블록 실행 결과를 반환하고, 권한이 누락되거나 오류 발생 시 defaultValue를 반환.<br>
     */
    protected inline fun <T> tryCatchSystemManager(defaultValue: T, block: () -> T): T {
        val deniedPermissions = getDeniedPermissionList()
        if (deniedPermissions.isNotEmpty()) {
            Logx.w("${this::class.simpleName}: Missing permissions!!! - $deniedPermissions")
            return defaultValue
        }
        return safeCatch(
            block = block,
            onCatch = { e ->
                Logx.e("${this::class.simpleName}: Error occurred : ${e.message}")
                defaultValue
            },
        )
    }

    /**
     * Refreshes the permission status. Call this after requesting permissions.<br><br>
     * 권한 상태를 새로고침합니다. 권한 요청 후 이를 호출하세요.<br>
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
     * Gets information about required permissions and their status.<br><br>
     * 필요한 권한과 그 상태에 대한 정보를 가져옵니다.<br>
     *
     * @return A map where keys are permission names and values are their granted status (true if granted, false otherwise).<br><br>
     *         권한 이름을 키로, 부여 상태를 값으로 하는 맵 (부여된 경우 true, 그렇지 않으면 false).<br>
     */
    public fun getPermissionInfo(): Map<String, Boolean> =
        requiredPermissions?.associateWith { permission -> !remainPermissions.contains(permission) } ?: emptyMap()

    /**
     * Checks if a specific permission is granted.<br><br>
     * 특정 권한이 부여되었는지 확인합니다.<br>
     *
     * @param permission The permission to check.<br><br>
     *                   확인할 권한.
     * @return Returns true if the permission is granted, false otherwise.<br><br>
     *         권한이 부여된 경우 true, 그렇지 않으면 false를 반환.<br>
     */
    public fun isPermissionGranted(permission: String): Boolean = !remainPermissions.contains(permission)

    /**
     * Called when the service is being destroyed. Override to perform cleanup.<br><br>
     * 서비스가 소멸될 때 호출됩니다. 정리 작업을 수행하려면 재정의하세요.<br>
     */
    public open fun onDestroy() {
        Logx.d("${this::class.simpleName} destroyed")
    }
}
