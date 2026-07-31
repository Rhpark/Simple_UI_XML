package kr.open.library.simple_ui.core.permissions.internal

import android.Manifest
import android.os.Build
import android.provider.Settings
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.permissions.vo.PermissionSpecialType

/**
 * 권한 분류와 특수 권한 설정 이동에 사용하는 내부 정책을 한곳에서 관리합니다.<br><br>
 * Centralizes the internal policy used for permission classification and special-permission settings navigation.<br>
 */
internal object PermissionPolicy {
    val specialPermissionActions: Map<String, String> = buildMap {
        PermissionSpecialType.entries.forEach {
            when (it) {
                PermissionSpecialType.SYSTEM_ALERT_WINDOW -> put(it.permission, Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                PermissionSpecialType.WRITE_SETTINGS -> put(it.permission, Settings.ACTION_MANAGE_WRITE_SETTINGS)
                PermissionSpecialType.PACKAGE_USAGE_STATS -> put(it.permission, Settings.ACTION_USAGE_ACCESS_SETTINGS)
                PermissionSpecialType.BIND_ACCESSIBILITY_SERVICE -> put(it.permission, Settings.ACTION_ACCESSIBILITY_SETTINGS)
                PermissionSpecialType.REQUEST_INSTALL_PACKAGES -> put(it.permission, Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                PermissionSpecialType.ACCESS_NOTIFICATION_POLICY -> put(it.permission, Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                PermissionSpecialType.BIND_NOTIFICATION_LISTENER_SERVICE -> put(
                    it.permission,
                    Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS,
                )
                PermissionSpecialType.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> put(
                    it.permission,
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                )
                else -> {
                    checkSdkVersion(Build.VERSION_CODES.S) {
                        if (it == PermissionSpecialType.SCHEDULE_EXACT_ALARM) {
                            put(Manifest.permission.SCHEDULE_EXACT_ALARM, Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        }
                    }

                    checkSdkVersion(Build.VERSION_CODES.R) {
                        if (it == PermissionSpecialType.MANAGE_EXTERNAL_STORAGE) {
                            put(Manifest.permission.MANAGE_EXTERNAL_STORAGE, Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        }
                    }
                }
            }
        }
    }

    val permissionsRequiringPackageUri: Set<String> = buildSet {
        add(Manifest.permission.SYSTEM_ALERT_WINDOW)
        add(Manifest.permission.WRITE_SETTINGS)
        add(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        add(Manifest.permission.REQUEST_INSTALL_PACKAGES)
        checkSdkVersion(Build.VERSION_CODES.R) { add(Manifest.permission.MANAGE_EXTERNAL_STORAGE) }
    }

    object ApiLevelRequirements {
        val androidRPermissions: Set<String> = setOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        val androidSPermissions: Set<String> = setOf(Manifest.permission.SCHEDULE_EXACT_ALARM)
        val androidTiramisuPermissions: Set<String> = setOf(Manifest.permission.POST_NOTIFICATIONS)
        val androidUPermissions: Set<String> = setOf(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
    }
}
