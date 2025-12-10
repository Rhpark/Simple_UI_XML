/**
 * Factory for creating special permission intents.<br><br>
 * 특수 권한 인텐트를 생성하는 팩토리입니다.<br>
 */

package kr.open.library.simple_ui.xml.permissions.factory

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import kr.open.library.simple_ui.core.permissions.vo.PermissionConstants

/**
 * Factory object for creating special permission intents and validating context state.<br><br>
 * 특수 권한 인텐트 생성 및 컨텍스트 상태 검증을 위한 팩토리 객체입니다.<br>
 *
 * This factory is intentionally stateless and provides pure functions for intent creation.<br><br>
 * 이 팩토리는 의도적으로 상태를 가지지 않으며 인텐트 생성을 위한 순수 함수를 제공합니다.<br>
 */
internal object SpecialPermissionIntentFactory {
    /**
     * Builds an intent for the given special permission after checking API-level requirements.<br><br>
     * API 레벨 요구사항을 확인한 뒤 지정된 특수 권한용 인텐트를 생성합니다.<br>
     *
     * @param context Host context used to resolve package information.<br><br>
     *                패키지 정보를 확인할 호스트 컨텍스트입니다.<br>
     * @param permission Special permission being requested.<br><br>
     *                   요청하려는 특수 권한입니다.<br>
     * @return Intent to launch the permission screen, or null if unsupported.<br><br>
     *         권한 화면을 여는 인텐트이며, 지원되지 않으면 null을 반환합니다.<br>
     */
    fun createSpecialPermissionIntent(context: Context, permission: String): Intent? {
        // Validates API levels before creating the permission intent.<br><br>
        // 권한 인텐트를 만들기 전에 필요한 API 레벨을 확인합니다.<br>
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
