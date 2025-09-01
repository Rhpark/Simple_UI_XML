package kr.open.library.simple_ui.ui.activity

import android.Manifest
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import kr.open.library.easy_extensions.conditional.checkSdkVersion
import kr.open.library.permissions.PermissionManager

abstract class RootActivity : AppCompatActivity() {

    /************************
     *   Permission Check   *
     ************************/
    private val permissionManager = PermissionManager.getInstance()

    private var currentRequestId: String? = null

    // 일반 권한 요청 런처
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        permissionManager.result(this, permissions, currentRequestId)
    }

    // 특수 권한별 개별 런처들 (모든 지원 권한)
    private val systemAlertWindowLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        permissionManager.resultSpecialPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW, currentRequestId)
    }

    private val writeSettingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        permissionManager.resultSpecialPermission(this, Manifest.permission.WRITE_SETTINGS, currentRequestId)
    }

    private val packageUsageStatsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        permissionManager.resultSpecialPermission(this, Manifest.permission.PACKAGE_USAGE_STATS, currentRequestId)
    }

    private val manageExternalStorageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        permissionManager.resultSpecialPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE, currentRequestId)
    }

    private val requestIgnoreBatteryOptimizationsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        permissionManager.resultSpecialPermission(this, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, currentRequestId)
    }

    private val scheduleExactAlarmLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        permissionManager.resultSpecialPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM, currentRequestId)
    }

    private val bindAccessibilityServiceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        permissionManager.resultSpecialPermission(this, Manifest.permission.BIND_ACCESSIBILITY_SERVICE, currentRequestId)
    }

    private val bindNotificationListenerServiceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        permissionManager.resultSpecialPermission(this, Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE, currentRequestId)
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

    /*************************
     *  화면의 statusBar 높이  *
     *************************/
    public val statusBarHeight: Int
        get() = checkSdkVersion(Build.VERSION_CODES.R,
            positiveWork = { window.decorView.getRootWindowInsets()?.getInsets(WindowInsets.Type.statusBars())?.top ?: 0 },
            negativeWork = { Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.top }
        )

    /******************************
     *  화면의 navigationBar 높이  *
     ******************************/
    public val navigationBarHeight: Int
        get() = checkSdkVersion(Build.VERSION_CODES.R,
            positiveWork = { window.decorView.getRootWindowInsets()?.getInsets(WindowInsets.Type.navigationBars())?.bottom ?: 0 },
            negativeWork = {
                val rootView = window.decorView.rootView
                val contentViewHeight = findViewById<View>(android.R.id.content).height
                (rootView.height - contentViewHeight) - statusBarHeight
            }
        )



    protected fun requestPermissions(permissions: List<String>, onResult: ((deniedPermissions: List<String>) -> Unit)) {
        currentRequestId = permissionManager.request(
            context= this,
            requestPermissionLauncher = requestPermissionLauncher,
            specialPermissionLaunchers = specialPermissionLaunchers,
            permissions = permissions,
            callback = onResult
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        beforeOnCreated(savedInstanceState)
    }

    /**
     * Override this method to perform initialization before the standard onCreate logic.
     */
    protected open fun beforeOnCreated(savedInstanceState: Bundle?) {}


    /**
     * Sets the status bar to transparent.
     * 상태 표시줄을 투명하게 설정.
     */
    protected fun setStatusBarTransparent() {
        window.apply {
            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            checkSdkVersion(Build.VERSION_CODES.R) {
                WindowCompat.setDecorFitsSystemWindows(this, false)
            }
        }
    }


    /**
     * Sets the status bar color.
     * 상태 표시줄 색상 설정.
     *
     * @param color The color to set.
     * @param isLightStatusBar Whether to use light status bar icons.
     */
    protected fun setStatusBarColor(@ColorInt color: Int, isLightStatusBar: Boolean = false) {
        window.apply {
            @Suppress("DEPRECATION")
            statusBarColor = color
            val insetsController = WindowCompat.getInsetsController(this, decorView)
            insetsController.isAppearanceLightStatusBars = isLightStatusBar
        }
    }


    /**
     * Sets the navigation bar color.
     * 네비게이션 바 색상 설정.
     *
     * @param color The color to set.
     * @param isLightNavigationBar Whether to use light navigation bar icons.
     */
    protected fun setNavigationBarColor(
        @ColorInt color: Int,
        isLightNavigationBar: Boolean = false
    ) {
        window.apply {
            @Suppress("DEPRECATION")
            navigationBarColor = color
            val insetsController = WindowCompat.getInsetsController(this, decorView)
            insetsController.isAppearanceLightNavigationBars = isLightNavigationBar
        }
    }


    /**
     * Configures system bars (status and navigation) with the same color.
     * 시스템 바(상태 및 네비게이션)를 동일한 색상으로 설정.
     *
     * @param color The color to set for both status and navigation bars.
     * @param isLightSystemBars Whether to use light system bar icons.
     */
    protected fun setSystemBarsColor(@ColorInt color: Int, isLightSystemBars: Boolean = false) {
        setStatusBarColor(color, isLightSystemBars)
        setNavigationBarColor(color, isLightSystemBars)
    }

    /**
     * Sets the system bar icons to light or dark mode.
     * 시스템 바 아이콘을 라이트 또는 다크 모드로 설정.
     *
     * @param isLightSystemBars True for dark icons (light mode), false for light icons (dark mode).
     */
    protected fun setSystemBarsAppearance(isLightSystemBars: Boolean) {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.apply {
            isAppearanceLightStatusBars = isLightSystemBars
            isAppearanceLightNavigationBars = isLightSystemBars
        }
    }
}