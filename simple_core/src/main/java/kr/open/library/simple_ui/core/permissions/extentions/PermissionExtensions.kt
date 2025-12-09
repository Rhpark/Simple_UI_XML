/**
 * Permission inspection helpers that unify special-case checks (usage stats, overlays, alarms, etc.).<br><br>
 * 사용량 통계·오버레이·정확 알람 등 특수 권한을 한 번에 점검할 수 있는 보조 함수 모음입니다.<br>
 */
package kr.open.library.simple_ui.core.permissions.extentions

import android.Manifest
import android.app.AlarmManager
import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.permissions.vo.PermissionSpecialType

/**
 * Evaluates whether the caller already holds [permission], including platform-specific toggles.<br><br>
 * 필요한 플랫폼 토글 여부까지 확인하여 [permission] 권한을 보유했는지 판단합니다.<br>
 *
 * @param permission Android permission string being evaluated.<br><br>
 *        확인할 Android 권한 문자열입니다.<br>
 * @return true when the permission (or equivalent toggle) is granted, otherwise false.<br><br>
 *         권한 또는 동등한 토글이 허용되면 true, 그렇지 않으면 false입니다.<br>
 */
public inline fun Context.hasPermission(permission: String): Boolean =
    when (permission) {
        Manifest.permission.SYSTEM_ALERT_WINDOW -> Settings.canDrawOverlays(this)

        Manifest.permission.WRITE_SETTINGS -> Settings.System.canWrite(this)

        Manifest.permission.PACKAGE_USAGE_STATS -> hasUsageStatsPermission()

        Manifest.permission.BIND_ACCESSIBILITY_SERVICE -> hasAccessibilityServicePermission()

        Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE -> hasNotificationListenerPermission()

        Manifest.permission.MANAGE_EXTERNAL_STORAGE -> {
            checkSdkVersion(
                Build.VERSION_CODES.R,
                positiveWork = { Environment.isExternalStorageManager() },
                negativeWork = {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                },
            )
        }

        Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            powerManager?.isIgnoringBatteryOptimizations(packageName) ?: false
        }

        Manifest.permission.SCHEDULE_EXACT_ALARM -> {
            checkSdkVersion(
                Build.VERSION_CODES.S,
                positiveWork = {
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                    alarmManager?.canScheduleExactAlarms() ?: false
                },
                negativeWork = { true },
            )
        }

        Manifest.permission.POST_NOTIFICATIONS -> {
            checkSdkVersion(
                Build.VERSION_CODES.TIRAMISU,
                positiveWork = {
                    NotificationManagerCompat.from(this).areNotificationsEnabled()
                },
                negativeWork = { true },
            )
        }

        Manifest.permission.REQUEST_INSTALL_PACKAGES -> {
            checkSdkVersion(
                Build.VERSION_CODES.O,
                positiveWork = { packageManager.canRequestPackageInstalls() },
                negativeWork = { true },
            )
        }

        Manifest.permission.ACCESS_NOTIFICATION_POLICY -> {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.isNotificationPolicyAccessGranted ?: false
        }

        else -> {
            if (getPermissionProtectionLevel(permission) == PermissionInfo.PROTECTION_DANGEROUS) {
                ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }
    }

/**
 * Returns true only when every entry in [permissions] is granted.<br><br>
 * [permissions]의 모든 항목이 허용되었을 때만 true를 반환합니다.<br>
 *
 * @param permissions List of permission strings to verify.<br><br>
 *        확인할 권한 문자열 목록입니다.<br>
 * @return true when all permissions are granted.<br><br>
 *         모든 권한이 허용되면 true입니다.<br>
 */
public inline fun Context.hasPermissions(vararg permissions: String): Boolean = permissions.all { permission -> hasPermission(permission) }

/**
 * Executes [doWork] only when every permission in [permissions] is granted.<br><br>
 * [permissions]에 포함된 권한이 모두 허용된 경우에만 [doWork]를 실행합니다.<br>
 *
 * @param permissions Permission strings to validate.<br><br>
 *        검증할 권한 문자열 목록입니다.<br>
 * @param doWork Action executed after the permissions are confirmed.<br><br>
 *        권한 확인 후 수행할 동작입니다.<br>
 * @return true when [doWork] ran because every permission was granted.<br><br>
 *         모든 권한이 허용되어 [doWork]가 실행되면 true입니다.<br>
 */
public inline fun Context.hasPermissions(
    vararg permissions: String,
    doWork: () -> Unit,
): Boolean =
    if (permissions.all { permission -> hasPermission(permission) }) {
        doWork()
        true
    } else {
        false
    }

/**
 * Produces a list of permissions from [permissions] that are still missing.<br><br>
 * [permissions] 중 아직 허용되지 않은 권한 목록을 반환합니다.<br>
 *
 * @param permissions Requested permission list.<br><br>
 *        요청할 권한 목록입니다.<br>
 * @return Permissions that have not been granted yet.<br><br>
 *         아직 허용되지 않은 권한 목록입니다.<br>
 */
public inline fun Context.remainPermissions(permissions: List<String>): List<String> = permissions.filterNot { hasPermission(it) }

/**
 * Checks whether usage stats access is enabled for this app.<br><br>
 * 앱에 사용량 통계 권한이 허용되었는지 확인합니다.<br>
 *
 * @return true when usage stats access is granted, otherwise false.<br><br>
 *         사용량 통계 권한이 허용되면 true, 아니면 false입니다.<br>
 */
public inline fun Context.hasUsageStatsPermission(): Boolean =
    safeCatch(defaultValue = false) {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
        appOps?.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName,
        ) == AppOpsManager.MODE_ALLOWED
    }

/**
 * Checks whether at least one accessibility service from this package is enabled.<br><br>
 * 이 패키지의 접근성 서비스가 활성화되어 있는지 확인합니다.<br>
 *
 * @return true when the package appears in enabled accessibility services.<br><br>
 *         접근성 서비스 활성 목록에 패키지가 있으면 true입니다.<br>
 */
public inline fun Context.hasAccessibilityServicePermission(): Boolean =
    safeCatch(defaultValue = false) {
        val enabledServices =
            Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            )
        enabledServices?.contains(packageName) == true
    }

/**
 * Checks whether the app is listed as a notification listener.<br><br>
 * 앱이 알림 리스너로 등록되어 있는지 확인합니다.<br>
 *
 * @return true when the package is present in the enabled listener list.<br><br>
 *         활성 알림 리스너 목록에 패키지가 있으면 true입니다.<br>
 */
public inline fun Context.hasNotificationListenerPermission(): Boolean =
    safeCatch(defaultValue = false) {
        val enabledListeners =
            Settings.Secure.getString(
                contentResolver,
                "enabled_notification_listeners",
            )
        enabledListeners?.contains(packageName) == true
    }

/**
 * Determines whether [permission] is one of the framework-defined special cases.<br><br>
 * [permission]이 프레임워크에서 정의한 특수 권한인지 판별합니다.<br>
 *
 * @param permission Permission string to classify.<br><br>
 *        분류할 권한 문자열입니다.<br>
 * @return true when the permission matches [PermissionSpecialType].<br><br>
 *         [PermissionSpecialType] 목록 중 하나와 일치하면 true입니다.<br>
 */
public inline fun isSpecialPermission(permission: String): Boolean {
    PermissionSpecialType.entries.forEach {
        if (permission == it.permission) return true
    }
    return false
}

/**
 * Reads the platform protection level for [permission].<br><br>
 * [permission] 권한의 플랫폼 보호 수준을 조회합니다.<br>
 *
 * @param permission Permission string to query.<br><br>
 *        조회할 권한 문자열입니다.<br>
 * @return Protection level constant, defaulting to [PermissionInfo.PROTECTION_DANGEROUS].<br><br>
 *         보호 수준 상수이며 기본값은 [PermissionInfo.PROTECTION_DANGEROUS]입니다.<br>
 */
public inline fun Context.getPermissionProtectionLevel(permission: String): Int =
    safeCatch(defaultValue = PermissionInfo.PROTECTION_DANGEROUS) {
        packageManager.getPermissionInfo(permission, 0).protection
    }
