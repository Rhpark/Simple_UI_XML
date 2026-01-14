package kr.open.library.simple_ui.core.permissions.handler

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch

/**
 * Handles role availability checks and request intents.<br><br>
 * Role 사용 가능 여부 확인과 요청 인텐트를 처리합니다.<br>
 *
 * @param context Context used to access RoleManager.<br><br>
 *                RoleManager 접근에 사용하는 컨텍스트입니다.<br>
 */
class RolePermissionHandler(
    private val context: Context,
) {
    /**
     * Returns whether the role is available on this device.<br><br>
     * 해당 기기에서 Role이 사용 가능한지 반환합니다.<br>
     *
     * @param role Role name string to inspect.<br><br>
     *             확인할 Role 문자열입니다.<br>
     * @return Return value: true when available, false otherwise. Log behavior: none.<br><br>
     *         반환값: 사용 가능하면 true, 아니면 false. 로그 동작: 없음.<br>
     */
    fun isRoleAvailable(role: String): Boolean = safeCatch(defaultValue = false) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return@safeCatch false
        val roleManager = context.getSystemService(RoleManager::class.java) ?: return@safeCatch false
        roleManager.isRoleAvailable(role)
    }

    /**
     * Returns whether the app currently holds the role.<br><br>
     * 앱이 현재 Role을 보유하고 있는지 반환합니다.<br>
     *
     * @param role Role name string to inspect.<br><br>
     *             확인할 Role 문자열입니다.<br>
     * @return Return value: true when held, false otherwise. Log behavior: none.<br><br>
     *         반환값: 보유 중이면 true, 아니면 false. 로그 동작: 없음.<br>
     */
    fun isRoleHeld(role: String): Boolean = safeCatch(defaultValue = false) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return@safeCatch false
        val roleManager = context.getSystemService(RoleManager::class.java) ?: return@safeCatch false
        roleManager.isRoleHeld(role)
    }

    /**
     * Creates a role request intent when supported.<br><br>
     * 지원되는 경우 Role 요청 인텐트를 생성합니다.<br>
     *
     * @param role Role name string to request.<br><br>
     *             요청할 Role 문자열입니다.<br>
     * @return Return value: role request intent or null when unavailable. Log behavior: none.<br><br>
     *         반환값: Role 요청 인텐트 또는 미지원 시 null. 로그 동작: 없음.<br>
     */
    fun createRequestIntent(role: String): Intent? = safeCatch(defaultValue = null) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return@safeCatch null
        val roleManager = context.getSystemService(RoleManager::class.java) ?: return@safeCatch null
        roleManager.createRequestRoleIntent(role)
    }
}
