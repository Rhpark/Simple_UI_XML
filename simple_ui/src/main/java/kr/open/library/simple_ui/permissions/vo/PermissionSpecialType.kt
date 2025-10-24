package kr.open.library.simple_ui.permissions.vo

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi


enum class PermissionSpecialType(val permission: String) {
    SYSTEM_ALERT_WINDOW(Manifest.permission.SYSTEM_ALERT_WINDOW),
    WRITE_SETTINGS(Manifest.permission.WRITE_SETTINGS),
    PACKAGE_USAGE_STATS(Manifest.permission.PACKAGE_USAGE_STATS),

    REQUEST_IGNORE_BATTERY_OPTIMIZATIONS(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS),

    BIND_ACCESSIBILITY_SERVICE(Manifest.permission.BIND_ACCESSIBILITY_SERVICE),
    BIND_NOTIFICATION_LISTENER_SERVICE(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE),

    REQUEST_INSTALL_PACKAGES(Manifest.permission.REQUEST_INSTALL_PACKAGES),
    ACCESS_NOTIFICATION_POLICY(Manifest.permission.ACCESS_NOTIFICATION_POLICY),

    @RequiresApi(Build.VERSION_CODES.R)
    MANAGE_EXTERNAL_STORAGE(Manifest.permission.MANAGE_EXTERNAL_STORAGE),

    @RequiresApi(Build.VERSION_CODES.S)
    SCHEDULE_EXACT_ALARM(Manifest.permission.SCHEDULE_EXACT_ALARM),
}