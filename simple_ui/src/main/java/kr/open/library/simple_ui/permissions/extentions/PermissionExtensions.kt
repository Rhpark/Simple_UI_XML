package kr.open.library.simple_ui.permissions.extentions

import android.Manifest
import android.app.AppOpsManager
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
import kr.open.library.simple_ui.extensions.conditional.*
import kr.open.library.simple_ui.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.permissions.vo.PermissionSpecialType


public inline fun Context.hasPermission(permission: String): Boolean =
    when (permission) {
        Manifest.permission.SYSTEM_ALERT_WINDOW -> Settings.canDrawOverlays(this)

        Manifest.permission.WRITE_SETTINGS -> Settings.System.canWrite(this)

        Manifest.permission.PACKAGE_USAGE_STATS -> hasUsageStatsPermission()

        Manifest.permission.MANAGE_EXTERNAL_STORAGE -> {
            checkSdkVersion(Build.VERSION_CODES.R,
                positiveWork = { Environment.isExternalStorageManager() },
                negativeWork = {
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                }
            )
        }

        Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            powerManager?.isIgnoringBatteryOptimizations(packageName) ?: false
        }

        Manifest.permission.SCHEDULE_EXACT_ALARM -> {
            checkSdkVersion(Build.VERSION_CODES.S,
                positiveWork = {
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                    alarmManager?.canScheduleExactAlarms() ?: false
                },
                negativeWork = { true }
            )
        }

        Manifest.permission.POST_NOTIFICATIONS -> {
            checkSdkVersion(Build.VERSION_CODES.TIRAMISU,
                positiveWork = {
                    NotificationManagerCompat.from(this).areNotificationsEnabled()
                },
                negativeWork = { true }
            )
        }

        Manifest.permission.BIND_ACCESSIBILITY_SERVICE -> hasAccessibilityServicePermission()

        Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE -> hasNotificationListenerPermission()

        else -> {
            if(getPermissionProtectionLevel(permission) == PermissionInfo.PROTECTION_DANGEROUS) {
                ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }
    }

public inline fun Context.hasPermissions(vararg permissions: String): Boolean =
    permissions.all { permission -> hasPermission(permission) }

public inline fun Context.hasPermissions(vararg permissions: String, doWork: () -> Unit): Boolean =
    if (permissions.all { permission -> hasPermission(permission) }) {
        doWork()
        true
    } else {
        false
    }


public inline fun Context.remainPermissions(permissions: List<String>): List<String> =
    permissions.filterNot { hasPermission(it) }

public inline fun Context.hasUsageStatsPermission(): Boolean = safeCatch(defaultValue = false) {
    val appOps = getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
    appOps?.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        packageName
    ) == AppOpsManager.MODE_ALLOWED
}

public inline fun Context.hasAccessibilityServicePermission(): Boolean =
    safeCatch(defaultValue = false) {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        enabledServices?.contains(packageName) == true
    }

public inline fun Context.hasNotificationListenerPermission(): Boolean = safeCatch(defaultValue = false) {
    val enabledListeners = Settings.Secure.getString(
        contentResolver,
        "enabled_notification_listeners"
    )
    enabledListeners?.contains(packageName) == true
}

public inline fun Context.isSpecialPermission(permission: String): Boolean {

    var isContain = false
    PermissionSpecialType.entries.forEach {
        if (permission == it.permission) return true
    }
    return isContain
}

public inline fun Context.getPermissionProtectionLevel(permission: String): Int =
    safeCatch(defaultValue = PermissionInfo.PROTECTION_DANGEROUS) {
        packageManager.getPermissionInfo(permission, 0).protection
    }
