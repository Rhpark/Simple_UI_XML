package kr.open.library.simple_ui.ui.fragment

import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import kr.open.library.permissions.PermissionManager

abstract class RootFragment: Fragment() {

    /************************
     *   Permission Check   *
     ************************/
    private val permissionManager = PermissionManager.getInstance()

    private var currentRequestId: String? = null

    // 일반 권한 요청 런처
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        permissionManager.result(requireContext(), permissions, currentRequestId)
    }

    // 특수 권한별 개별 런처들 (모든 지원 권한)
    private val systemAlertWindowLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        permissionManager.resultSpecialPermission(requireContext(), Manifest.permission.SYSTEM_ALERT_WINDOW, currentRequestId)
    }

    private val writeSettingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        permissionManager.resultSpecialPermission(requireContext(), Manifest.permission.WRITE_SETTINGS, currentRequestId)
    }

    private val packageUsageStatsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        permissionManager.resultSpecialPermission(requireContext(), Manifest.permission.PACKAGE_USAGE_STATS, currentRequestId)
    }

    private val manageExternalStorageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        permissionManager.resultSpecialPermission(requireContext(), Manifest.permission.MANAGE_EXTERNAL_STORAGE, currentRequestId)
    }

    private val requestIgnoreBatteryOptimizationsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        permissionManager.resultSpecialPermission(requireContext(), Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, currentRequestId)
    }

    private val scheduleExactAlarmLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        permissionManager.resultSpecialPermission(requireContext(), Manifest.permission.SCHEDULE_EXACT_ALARM, currentRequestId)
    }

    private val bindAccessibilityServiceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        permissionManager.resultSpecialPermission(requireContext(), Manifest.permission.BIND_ACCESSIBILITY_SERVICE, currentRequestId)
    }

    private val bindNotificationListenerServiceLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        permissionManager.resultSpecialPermission(requireContext(), Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE, currentRequestId)
    }

    // 모든 특수 권한별 런처 맵 생성
    private val specialPermissionLaunchers = mapOf(
        Manifest.permission.SYSTEM_ALERT_WINDOW to systemAlertWindowLauncher,
        Manifest.permission.WRITE_SETTINGS to writeSettingsLauncher,
        Manifest.permission.PACKAGE_USAGE_STATS to packageUsageStatsLauncher,
        Manifest.permission.MANAGE_EXTERNAL_STORAGE to manageExternalStorageLauncher,
        Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS to requestIgnoreBatteryOptimizationsLauncher,
        Manifest.permission.SCHEDULE_EXACT_ALARM to scheduleExactAlarmLauncher,
        Manifest.permission.BIND_ACCESSIBILITY_SERVICE to bindAccessibilityServiceLauncher,
        Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE to bindNotificationListenerServiceLauncher
    )

    protected fun requestPermissions(permissions: List<String>, onResult: ((deniedPermissions: List<String>) -> Unit)) {
        currentRequestId = permissionManager.request(
            context= requireContext(),
            requestPermissionLauncher = requestPermissionLauncher,
            specialPermissionLaunchers = specialPermissionLaunchers,
            permissions = permissions,
            callback = onResult
        )
    }
}