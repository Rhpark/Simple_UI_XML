package kr.open.library.simple_ui.core.permissions.classifier

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.permissions.vo.PermissionConstants
import kr.open.library.simple_ui.core.permissions.vo.PermissionSpecialType

/**
 * Defines permission categories used by the requester.<br><br>
 * 요청기에서 사용하는 권한 분류 유형입니다.<br>
 */
enum class PermissionType {
    /**
     * Runtime dangerous permissions that use the standard dialog flow.<br><br>
     * 표준 런타임 다이얼로그를 사용하는 위험 권한입니다.<br>
     */
    RUNTIME,

    /**
     * Special permissions that require settings navigation.<br><br>
     * 설정 이동이 필요한 특수 권한입니다.<br>
     */
    SPECIAL,

    /**
     * Role requests managed by RoleManager.<br><br>
     * RoleManager로 관리되는 역할 요청입니다.<br>
     */
    ROLE,
}

/**
 * Classifies permissions and validates support and manifest declarations.<br><br>
 * 권한을 분류하고 지원 여부와 매니페스트 선언을 검증합니다.<br>
 *
 * @param context Context used to read package permission metadata.<br><br>
 *                패키지 권한 메타데이터를 읽는 데 사용하는 컨텍스트입니다.<br>
 */
class PermissionClassifier(
    private val context: Context,
) {
    /**
     * Static constants for role prefix and special app access permissions.<br><br>
     * Role prefix와 특수 앱 액세스 권한 상수입니다.<br>
     */
    companion object {
        /**
         * Prefix that identifies Role strings.<br><br>
         * Role 문자열을 구분하는 접두사입니다.<br>
         */
        private const val ROLE_PREFIX = "android.app.role."

        /**
         * Permissions treated as special app access entries.<br><br>
         * 특수 앱 액세스로 취급되는 권한 목록입니다.<br>
         */
        private val SPECIAL_APP_ACCESS = setOf(
            Manifest.permission.BIND_ACCESSIBILITY_SERVICE,
            Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
        )
    }

    /**
     * Cached set of permissions declared in the manifest.<br><br>
     * 매니페스트에 선언된 권한 집합 캐시입니다.<br>
     */
    private val declaredPermissions: Set<String> by lazy { loadDeclaredPermissions() }

    /**
     * Classifies the permission into runtime, special, or role.<br><br>
     * 권한을 런타임/특수/Role로 분류합니다.<br>
     *
     * @param permission Permission string to classify.<br><br>
     *                  분류할 권한 문자열입니다.<br>
     * @return Return value: classified permission type. Log behavior: none.<br><br>
     *         반환값: 분류된 권한 타입. 로그 동작: 없음.<br>
     */
    fun classify(permission: String): PermissionType = when {
        isRole(permission) -> PermissionType.ROLE
        isSpecial(permission) -> PermissionType.SPECIAL
        else -> PermissionType.RUNTIME
    }

    /**
     * Returns whether the permission is a Role string.<br><br>
     * 권한이 Role 문자열인지 여부를 반환합니다.<br>
     *
     * @param permission Permission string to inspect.<br><br>
     *                  검사할 권한 문자열입니다.<br>
     * @return Return value: true when the string is a Role. Log behavior: none.<br><br>
     *         반환값: Role 문자열이면 true. 로그 동작: 없음.<br>
     */
    fun isRole(permission: String): Boolean = permission.startsWith(ROLE_PREFIX)

    /**
     * Returns whether the permission is treated as special.<br><br>
     * 권한이 특수 권한으로 취급되는지 반환합니다.<br>
     *
     * @param permission Permission string to inspect.<br><br>
     *                  검사할 권한 문자열입니다.<br>
     * @return Return value: true when special. Log behavior: none.<br><br>
     *         반환값: 특수 권한이면 true. 로그 동작: 없음.<br>
     */
    fun isSpecial(permission: String): Boolean =
        permission == Manifest.permission.MANAGE_MEDIA ||
            SPECIAL_APP_ACCESS.contains(permission) ||
            PermissionSpecialType.entries.any { it.permission == permission }

    /**
     * Returns whether the permission is a special app access entry.<br><br>
     * 권한이 특수 앱 액세스 항목인지 여부를 반환합니다.<br>
     *
     * @param permission Permission string to inspect.<br><br>
     *                  검사할 권한 문자열입니다.<br>
     * @return Return value: true when it is special app access. Log behavior: none.<br><br>
     *         반환값: 특수 앱 액세스이면 true. 로그 동작: 없음.<br>
     */
    fun isSpecialAppAccess(permission: String): Boolean = SPECIAL_APP_ACCESS.contains(permission)

    /**
     * Returns whether the permission is supported on this SDK level.<br><br>
     * 해당 SDK 레벨에서 권한이 지원되는지 반환합니다.<br>
     *
     * @param permission Permission string to inspect.<br><br>
     *                  검사할 권한 문자열입니다.<br>
     * @return Return value: true when supported. Log behavior: none.<br><br>
     *         반환값: 지원되면 true. 로그 동작: 없음.<br>
     */
    fun isSupported(permission: String): Boolean = when {
        permission == Manifest.permission.MANAGE_MEDIA -> false
        PermissionConstants.ApiLevelRequirements.ANDROID_R_PERMISSIONS.contains(permission) ->
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        PermissionConstants.ApiLevelRequirements.ANDROID_S_PERMISSIONS.contains(permission) ->
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        PermissionConstants.ApiLevelRequirements.ANDROID_TIRAMISU_PERMISSIONS.contains(permission) ->
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        else -> true
    }

    /**
     * Returns whether the permission should be treated as invalid.<br><br>
     * 권한이 유효하지 않은 것으로 처리되어야 하는지 반환합니다.<br>
     *
     * @param permission Permission string to validate.<br><br>
     *                  검증할 권한 문자열입니다.<br>
     * @return Return value: true when invalid. Log behavior: none.<br><br>
     *         반환값: 유효하지 않으면 true. 로그 동작: 없음.<br>
     */
    fun isInvalid(permission: String): Boolean {
        if (permission.isEmpty()) return true
        if (isRole(permission)) return false
        if (isSpecialAppAccess(permission)) return false
        return !declaredPermissions.contains(permission)
    }

    /**
     * Loads permissions declared in the app manifest.<br><br>
     * 앱 매니페스트에 선언된 권한을 로드합니다.<br>
     *
     * @return Return value: declared permissions set. Log behavior: logs a warning when empty.<br><br>
     *         반환값: 선언된 권한 집합. 로그 동작: 비어 있으면 경고 로그 기록.<br>
     */
    private fun loadDeclaredPermissions(): Set<String> = safeCatch(defaultValue = emptySet()) {
        val packageManager = context.packageManager
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_PERMISSIONS,
            )
        }
        packageInfo.requestedPermissions?.toSet() ?: emptySet()
    }.also { permissions ->
        if (permissions.isEmpty()) {
            Logx.w("PermissionClassifier: requestedPermissions is empty.")
        }
    }
}
