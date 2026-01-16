package kr.open.library.simple_ui.core.permissions.handler

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.permissions.extentions.hasPermission
import kr.open.library.simple_ui.core.permissions.vo.PermissionConstants

/**
 * Handles special permission checks and settings intents.<br><br>
 * 특수 권한의 확인과 설정 인텐트를 처리합니다.<br>
 *
 * @param context Context used to evaluate permission state and build intents.<br><br>
 *                권한 상태 확인과 인텐트 생성을 위해 사용하는 컨텍스트입니다.<br>
 */
class SpecialPermissionHandler(
    private val context: Context,
) {
    /**
     * Returns whether the special permission is currently granted.<br><br>
     * 특수 권한이 현재 허용 상태인지 반환합니다.<br>
     *
     * @param permission Special permission string to inspect.<br><br>
     *                  확인할 특수 권한 문자열입니다.<br>
     * @return Return value: true when granted, false otherwise. Log behavior: none.<br><br>
     *         반환값: 허용되면 true, 아니면 false. 로그 동작: 없음.<br>
     */
    fun isGranted(permission: String): Boolean = when (permission) {
        Manifest.permission.MANAGE_MEDIA -> false
        else -> context.hasPermission(permission)
    }

    /**
     * Builds a settings intent for the special permission when possible.<br><br>
     * 가능한 경우 특수 권한용 설정 인텐트를 생성합니다.<br>
     *
     * @param permission Special permission string to request.<br><br>
     *                  요청할 특수 권한 문자열입니다.<br>
     * @return Return value: settings intent or null when not supported. Log behavior: none.<br><br>
     *         반환값: 설정 인텐트 또는 미지원 시 null. 로그 동작: 없음.<br>
     */
    fun buildSettingsIntent(permission: String): Intent? {
        if (permission == Manifest.permission.MANAGE_MEDIA) {
            return checkSdkVersion(Build.VERSION_CODES.S,
                positiveWork = { Intent(Settings.ACTION_REQUEST_MANAGE_MEDIA) },
                negativeWork = { null }
            )
        }
        val action = PermissionConstants.SPECIAL_PERMISSION_ACTIONS[permission] ?: return null
        return Intent(action).apply {
            if (PermissionConstants.PERMISSIONS_REQUIRING_PACKAGE_URI.contains(permission)) {
                data = Uri.parse("package:${context.packageName}")
            }
        }
    }
}
