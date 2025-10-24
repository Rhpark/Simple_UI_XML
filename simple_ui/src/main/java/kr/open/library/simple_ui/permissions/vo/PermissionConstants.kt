package kr.open.library.simple_ui.permissions.vo

import android.Manifest
import android.os.Build
import android.provider.Settings
import kr.open.library.simple_ui.extensions.conditional.checkSdkVersion

/**
 * 권한 관련 상수들을 정의하는 객체
 */
internal object PermissionConstants {
    
    /**
     * 특수 권한과 해당 Settings Action의 매핑
     */

    val SPECIAL_PERMISSION_ACTIONS: Map<String, String> = buildMap {
        PermissionSpecialType.entries.forEach {
            when(it) {
                PermissionSpecialType.SYSTEM_ALERT_WINDOW           -> put(it.permission, Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                PermissionSpecialType.WRITE_SETTINGS                -> put(it.permission, Settings.ACTION_MANAGE_WRITE_SETTINGS)
                PermissionSpecialType.PACKAGE_USAGE_STATS           -> put(it.permission, Settings.ACTION_USAGE_ACCESS_SETTINGS)
                PermissionSpecialType.BIND_ACCESSIBILITY_SERVICE    -> put(it.permission, Settings.ACTION_ACCESSIBILITY_SETTINGS)
                PermissionSpecialType.REQUEST_INSTALL_PACKAGES      -> put(it.permission, Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                PermissionSpecialType.ACCESS_NOTIFICATION_POLICY    -> put(it.permission, Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                PermissionSpecialType.BIND_NOTIFICATION_LISTENER_SERVICE    -> put(it.permission, Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                PermissionSpecialType.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS  -> put(it.permission, Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)

                else -> {
                    checkSdkVersion(Build.VERSION_CODES.S) {
                        if(it == PermissionSpecialType.SCHEDULE_EXACT_ALARM) {
                            put(Manifest.permission.SCHEDULE_EXACT_ALARM, Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        }
                    }

                    checkSdkVersion(Build.VERSION_CODES.R) {
                        if(it == PermissionSpecialType.MANAGE_EXTERNAL_STORAGE) {
                            put(Manifest.permission.MANAGE_EXTERNAL_STORAGE, Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        }
                    }
                }
            }
        }
    }
    
    /**
     * package URI가 필요한 특수 권한들
     */
    val PERMISSIONS_REQUIRING_PACKAGE_URI = buildSet<String> {
        add(Manifest.permission.SYSTEM_ALERT_WINDOW)
        add(Manifest.permission.WRITE_SETTINGS)
        add(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        add(Manifest.permission.REQUEST_INSTALL_PACKAGES)
        checkSdkVersion(Build.VERSION_CODES.R) {
            add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        }
    }
    
    /**
     * API 레벨별 권한 요구사항
     */
    object ApiLevelRequirements {
        val ANDROID_R_PERMISSIONS = buildSet<String> {
            checkSdkVersion(Build.VERSION_CODES.R) {
                add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            }
        }
        
        val ANDROID_S_PERMISSIONS = buildSet<String> {
            checkSdkVersion(Build.VERSION_CODES.S) {
                add(Manifest.permission.SCHEDULE_EXACT_ALARM)
            }
        }
        
        val ANDROID_TIRAMISU_PERMISSIONS = buildSet<String> {
            checkSdkVersion(Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    /**
     * 기본 설정값들
     */
    object Defaults {
        const val REQUEST_TIMEOUT_MS = 300_000L // 5분
    }
}