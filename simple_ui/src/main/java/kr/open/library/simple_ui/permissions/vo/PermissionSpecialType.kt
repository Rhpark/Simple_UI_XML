package kr.open.library.simple_ui.permissions.vo

import android.Manifest


enum class PermissionSpecialType(val permission: String) {
    SYSTEM_ALERT_WINDOW(Manifest.permission.SYSTEM_ALERT_WINDOW),
    WRITE_SETTINGS(Manifest.permission.WRITE_SETTINGS),
    PACKAGE_USAGE_STATS(Manifest.permission.PACKAGE_USAGE_STATS),
    MANAGE_EXTERNAL_STORAGE(Manifest.permission.MANAGE_EXTERNAL_STORAGE),
    REQUEST_IGNORE_BATTERY_OPTIMIZATIONS(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS),
    SCHEDULE_EXACT_ALARM(Manifest.permission.SCHEDULE_EXACT_ALARM),
    BIND_ACCESSIBILITY_SERVICE(Manifest.permission.BIND_ACCESSIBILITY_SERVICE),
    BIND_NOTIFICATION_LISTENER_SERVICE(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE),
}