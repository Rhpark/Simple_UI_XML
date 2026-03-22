package kr.open.library.simple_ui.core.permissions.classifier

import android.Manifest
import android.content.Context
import android.content.pm.PermissionInfo
import android.os.Build
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.permissions.extensions.getPermissionBaseProtectionLevel
import kr.open.library.simple_ui.core.permissions.internal.readDeclaredManifestPermissions
import kr.open.library.simple_ui.core.permissions.vo.PermissionConstants
import kr.open.library.simple_ui.core.permissions.vo.PermissionSpecialType

/**
 * 요청 흐름에서 사용하는 권한 분류 유형입니다.<br><br>
 * Defines permission categories used by the requester.<br>
 */
enum class PermissionType {
    /**
     * `ROLE`, `SPECIAL`을 제외한 일반 권한 처리 분류입니다.
     * 이 분류에 속한 권한은 이후 단계에서 `dangerous`, `normal`, `signature` 계열로 다시 세분화될 수 있습니다.<br><br>
     * General permission flow category after excluding role and special types.
     * Entries in this group may later resolve to requestable dangerous permissions,
     * granted-by-default normal permissions, or not-supported signature-style permissions.<br>
     */
    RUNTIME,

    /**
     * 설정 화면 이동이 필요한 특수 권한 분류입니다.<br><br>
     * Special permissions that require settings navigation.<br>
     */
    SPECIAL,

    /**
     * `RoleManager`를 통해 처리되는 역할 요청 분류입니다.<br><br>
     * Role requests managed by RoleManager.<br>
     */
    ROLE,
}

/**
 * 권한이 표준 런타임 다이얼로그 흐름에 진입할 수 있는지를 나타냅니다.<br><br>
 * Represents whether a permission can enter the standard runtime dialog flow.<br>
 */
enum class RuntimePermissionRequestability {
    /**
     * 런타임 다이얼로그로 요청 가능한 `dangerous` 권한입니다.<br><br>
     * Dangerous permission that can be requested through the runtime dialog.<br>
     */
    REQUESTABLE,

    /**
     * 별도 요청 없이 기본 허용으로 간주해야 하는 `normal` 권한입니다.<br><br>
     * Normal permission that should be treated as granted by default.<br>
     */
    GRANTED_BY_DEFAULT,

    /**
     * 일반 앱 프로세스 기준으로 런타임 요청 대상으로 취급하지 않는 권한입니다.<br><br>
     * Permission that ordinary app processes should not request through runtime flow.<br>
     */
    NOT_SUPPORTED,
}

/**
 * 권한을 분류하고, SDK 지원 여부, 매니페스트 선언 여부, 런타임 요청 가능 여부를 판단합니다.<br><br>
 * Classifies permissions and validates support, manifest declarations, and runtime requestability.<br>
 *
 * @param context 패키지 권한 메타데이터를 읽기 위한 컨텍스트입니다.<br><br>
 *                Context used to read package permission metadata.<br>
 */
class PermissionClassifier(
    private val context: Context,
) {
    /**
     * Role 접두사와 특수 앱 접근 권한 목록을 담은 정적 상수입니다.<br><br>
     * Static constants for role prefix and special app access permissions.<br>
     */
    companion object {
        /**
         * Role 문자열을 식별하는 접두사입니다.<br><br>
         * Prefix that identifies Role strings.<br>
         */
        private const val ROLE_PREFIX = "android.app.role."

        /**
         * 특수 앱 접근 항목으로 취급하는 권한 목록입니다.<br><br>
         * Permissions treated as special app access entries.<br>
         */
        private val SPECIAL_APP_ACCESS = setOf(
            Manifest.permission.BIND_ACCESSIBILITY_SERVICE,
            Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
        )
    }

    /**
     * 매니페스트에 선언된 권한 집합의 캐시입니다.<br><br>
     * Cached set of permissions declared in the manifest.<br>
     */
    private val declaredPermissions: Set<String> by lazy { loadDeclaredPermissions() }

    /**
     * 권한을 `RUNTIME`, `SPECIAL`, `ROLE` 중 하나로 분류합니다.<br><br>
     * Classifies the permission into runtime, special, or role.<br>
     *
     * @param permission 분류할 권한 문자열입니다.<br><br>
     *                  Permission string to classify.<br>
     * @return 분류된 권한 유형입니다. 이 메서드는 로그를 남기지 않습니다.<br><br>
     *         Return value: classified permission type. Log behavior: none.<br>
     */
    fun classify(permission: String): PermissionType = when {
        isRole(permission) -> PermissionType.ROLE
        isSpecial(permission) -> PermissionType.SPECIAL
        else -> PermissionType.RUNTIME
    }

    /**
     * 권한 문자열이 `Role` 형식인지 여부를 반환합니다.<br><br>
     * Returns whether the permission is a Role string.<br>
     *
     * @param permission 확인할 권한 문자열입니다.<br><br>
     *                  Permission string to inspect.<br>
     * @return Role 문자열이면 `true`를 반환합니다. 이 메서드는 로그를 남기지 않습니다.<br><br>
     *         Return value: true when the string is a Role. Log behavior: none.<br>
     */
    fun isRole(permission: String): Boolean = permission.startsWith(ROLE_PREFIX)

    /**
     * 권한을 특수 권한으로 취급해야 하는지 반환합니다.<br><br>
     * Returns whether the permission is treated as special.<br>
     *
     * `MANAGE_MEDIA`는 일반 런타임 흐름에서 제외하기 위해 `SPECIAL`로 분류되지만,
     * 이 라이브러리에서는 일반 앱 요청 대상으로는 계속 지원하지 않는 권한으로 취급합니다.<br><br>
     * `MANAGE_MEDIA` is classified as `SPECIAL` so it is excluded from the general runtime flow,
     * but this library still treats it as unsupported for ordinary app requests.<br>
     *
     * @param permission 확인할 권한 문자열입니다.<br><br>
     *                  Permission string to inspect.<br>
     * @return 특수 권한이면 `true`를 반환합니다. 이 메서드는 로그를 남기지 않습니다.<br><br>
     *         Return value: true when special. Log behavior: none.<br>
     */
    fun isSpecial(permission: String): Boolean =
        permission == Manifest.permission.MANAGE_MEDIA ||
            SPECIAL_APP_ACCESS.contains(permission) ||
            PermissionSpecialType.entries.any { it.permission == permission }

    /**
     * 권한이 특수 앱 접근 항목인지 여부를 반환합니다.<br><br>
     * Returns whether the permission is a special app access entry.<br>
     *
     * @param permission 확인할 권한 문자열입니다.<br><br>
     *                  Permission string to inspect.<br>
     * @return 특수 앱 접근 항목이면 `true`를 반환합니다. 이 메서드는 로그를 남기지 않습니다.<br><br>
     *         Return value: true when it is special app access. Log behavior: none.<br>
     */
    fun isSpecialAppAccess(permission: String): Boolean = SPECIAL_APP_ACCESS.contains(permission)

    /**
     * 현재 SDK에서 이 권한을 지원하는지 여부를 반환합니다.<br><br>
     * Returns whether the permission is supported on this SDK level.<br>
     *
     * `MANAGE_MEDIA`는 OS 버전상 존재하더라도 일반 앱 요청 대상으로는 노출하지 않는
     * 라이브러리 정책 때문에 의도적으로 `false`를 반환합니다.<br><br>
     * `MANAGE_MEDIA` is intentionally returned as unsupported even on supported OS versions,
     * because the library policy does not expose it as a requestable permission for ordinary apps.<br>
     *
     * @param permission 확인할 권한 문자열입니다.<br><br>
     *                  Permission string to inspect.<br>
     * @return 현재 SDK에서 지원하면 `true`를 반환합니다. 이 메서드는 로그를 남기지 않습니다.<br><br>
     *         Return value: true when supported. Log behavior: none.<br>
     *
     * **Note / 주의:** The `else → true` branch means any permission not listed in
     * [PermissionConstants.ApiLevelRequirements] is treated as universally supported.
     * When a new Android version introduces new API-level-gated permissions, add them to the
     * corresponding set in [PermissionConstants.ApiLevelRequirements] and add a matching branch here.<br><br>
     * `else → true` 분기로 인해 [PermissionConstants.ApiLevelRequirements]에 없는 권한은
     * 모든 API 레벨에서 지원되는 것으로 처리됩니다.
     * 신규 Android 버전에서 API 레벨 제한 권한이 추가되면 해당 집합과 분기를 반드시 추가하세요.<br>
     */
    fun isSupported(permission: String): Boolean = when {
        permission == Manifest.permission.MANAGE_MEDIA -> false
        PermissionConstants.ApiLevelRequirements.ANDROID_R_PERMISSIONS.contains(permission) ->
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        PermissionConstants.ApiLevelRequirements.ANDROID_S_PERMISSIONS.contains(permission) ->
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        PermissionConstants.ApiLevelRequirements.ANDROID_TIRAMISU_PERMISSIONS.contains(permission) ->
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        PermissionConstants.ApiLevelRequirements.ANDROID_U_PERMISSIONS.contains(permission) ->
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
        else -> true
    }

    /**
     * 권한이 런타임 다이얼로그 흐름에 진입 가능한지 반환합니다.<br><br>
     * Returns whether the permission can enter the runtime dialog flow.<br>
     *
     * 이 메서드는 호출부가 [isInvalid]를 통해 빈 문자열이나 매니페스트 미선언 권한 같은
     * 잘못된 입력을 먼저 걸렀다고 가정합니다.
     * 즉, 이미 유효성 검증이 끝난 일반 권한에 대해서만 요청 가능성을 판정합니다.<br><br>
     * This method assumes the caller already filtered out invalid inputs such as blank strings
     * or manifest-undeclared permissions via [isInvalid]. It only classifies requestability for
     * already-validated general permissions.<br>
     *
     * @param permission 확인할 권한 문자열입니다.<br><br>
     *                  Permission string to inspect.<br>
     * @return 런타임 요청 가능성 분류입니다. 이 메서드는 로그를 남기지 않습니다.<br><br>
     *         Return value: runtime requestability classification. Log behavior: none.<br>
     */
    fun getRuntimeRequestability(permission: String): RuntimePermissionRequestability {
        if (classify(permission) != PermissionType.RUNTIME) {
            return RuntimePermissionRequestability.NOT_SUPPORTED
        }

        return when (context.getPermissionBaseProtectionLevel(permission)) {
            PermissionInfo.PROTECTION_DANGEROUS -> RuntimePermissionRequestability.REQUESTABLE
            PermissionInfo.PROTECTION_NORMAL -> RuntimePermissionRequestability.GRANTED_BY_DEFAULT
            else -> RuntimePermissionRequestability.NOT_SUPPORTED
        }
    }

    /**
     * 권한을 잘못된 입력으로 취급해야 하는지 반환합니다.<br><br>
     * Returns whether the permission should be treated as invalid.<br>
     *
     * @param permission 검증할 권한 문자열입니다.<br><br>
     *                  Permission string to validate.<br>
     * @return 잘못된 입력이면 `true`를 반환합니다. 이 메서드는 로그를 남기지 않습니다.<br><br>
     *         Return value: true when invalid. Log behavior: none.<br>
     */
    fun isInvalid(permission: String): Boolean {
        if (permission.isEmpty()) return true
        if (isRole(permission)) return false
        if (isSpecialAppAccess(permission)) return false
        return !declaredPermissions.contains(permission)
    }

    /**
     * 앱 매니페스트에 선언된 권한 목록을 읽어 옵니다.<br><br>
     * Loads permissions declared in the app manifest.<br>
     *
     * @return 선언된 권한 집합입니다. 권한 목록이 비어 있으면 경고 로그를 남깁니다.<br><br>
     *         Return value: declared permissions set. Log behavior: logs a warning when empty.<br>
     */
    private fun loadDeclaredPermissions(): Set<String> = context.readDeclaredManifestPermissions().also { permissions ->
        if (permissions.isEmpty()) {
            Logx.w("PermissionClassifier: requestedPermissions is empty.")
        }
    }
}
