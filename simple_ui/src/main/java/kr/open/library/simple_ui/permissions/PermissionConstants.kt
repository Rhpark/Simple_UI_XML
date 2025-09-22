package kr.open.library.simple_ui.permissions

import android.Manifest
import android.provider.Settings

/**
 * 권한 관련 상수들을 정의하는 객체
 */
internal object PermissionConstants {
    
    /**
     * 특수 권한과 해당 Settings Action의 매핑
     */
    val SPECIAL_PERMISSION_ACTIONS = mapOf(
        Manifest.permission.SYSTEM_ALERT_WINDOW to Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Manifest.permission.WRITE_SETTINGS to Settings.ACTION_MANAGE_WRITE_SETTINGS,
        Manifest.permission.PACKAGE_USAGE_STATS to Settings.ACTION_USAGE_ACCESS_SETTINGS,
        Manifest.permission.MANAGE_EXTERNAL_STORAGE to Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
        Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS to Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
        Manifest.permission.SCHEDULE_EXACT_ALARM to Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
        Manifest.permission.BIND_ACCESSIBILITY_SERVICE to Settings.ACTION_ACCESSIBILITY_SETTINGS,
        Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE to "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
    )
    
    /**
     * package URI가 필요한 특수 권한들
     */
    val PERMISSIONS_REQUIRING_PACKAGE_URI = setOf(
        Manifest.permission.SYSTEM_ALERT_WINDOW,
        Manifest.permission.WRITE_SETTINGS,
        Manifest.permission.MANAGE_EXTERNAL_STORAGE,
        Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
    )
    
    /**
     * API 레벨별 권한 요구사항
     */
    object ApiLevelRequirements {
        val ANDROID_R_PERMISSIONS = setOf(
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
        )
        
        val ANDROID_S_PERMISSIONS = setOf(
            Manifest.permission.SCHEDULE_EXACT_ALARM
        )
        
        val ANDROID_TIRAMISU_PERMISSIONS = setOf(
            Manifest.permission.POST_NOTIFICATIONS
        )
    }
    
    /**
     * 기본 설정값들
     */
    object Defaults {
        const val REQUEST_TIMEOUT_MS = 300_000L // 5분
    }
}