package kr.open.library.simple_ui.xml.ui.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.xml.permissions.register.PermissionDelegate
import kr.open.library.simple_ui.xml.permissions.register.PermissionRequester

/**
 * Root Activity class providing comprehensive system bar control and permission management.<br>
 * Serves as the foundation for all Activity classes in the library with API version-aware implementations.<br><br>
 * 시스템 바 제어와 권한 관리를 종합적으로 제공하는 루트 Activity 클래스입니다.<br>
 * API 버전을 인식하는 구현으로 라이브러리의 모든 Activity 클래스의 기반이 됩니다.<br>
 *
 * Features:<br>
 * - StatusBar and NavigationBar color customization<br>
 * - System bar visibility control (show/hide/transparent)<br>
 * - Runtime permission management via PermissionDelegate<br>
 * - API 35+ Edge-to-edge support with custom background views<br>
 * - Lifecycle-aware permission state preservation<br><br>
 * 기능:<br>
 * - StatusBar와 NavigationBar 색상 커스터마이징<br>
 * - 시스템 바 가시성 제어 (표시/숨김/투명)<br>
 * - PermissionDelegate를 통한 런타임 권한 관리<br>
 * - 커스텀 배경 뷰를 사용한 API 35+ Edge-to-edge 지원<br>
 * - 생명주기 인식 권한 상태 보존<br>
 *
 * @see BaseActivity For simple layout-based Activity.<br><br>
 *      간단한 레이아웃 기반 Activity는 BaseActivity를 참조하세요.<br>
 *
 * @see BaseBindingActivity For DataBinding-enabled Activity.<br><br>
 *      DataBinding을 사용하는 Activity는 BaseBindingActivity를 참조하세요.<br>
 */
abstract class RootActivity :
    AppCompatActivity(),
    PermissionRequester {
    /**
     * Delegate for handling runtime permission requests.<br><br>
     * 런타임 권한 요청을 처리하는 델리게이트입니다.<br>
     */
    protected lateinit var permissionDelegate: PermissionDelegate<AppCompatActivity>

    private val activityWindow: RootActivityWindow by lazy { RootActivityWindow(this.window) }

    /**
     * Requests runtime permissions and returns the result via callback.
     * Delegates to PermissionDelegate for actual permission handling.<br><br>
     * 런타임 권한을 요청하고 콜백을 통해 결과를 반환합니다.<br>
     * 실제 권한 처리는 PermissionDelegate에 위임합니다.<br>
     *
     * @param permissions The list of permissions to request.<br><br>
     *                    요청할 권한 목록.
     * @param onResult Callback invoked with the list of denied permissions after the request completes.<br><br>
     *                 요청 완료 후 거부된 권한 목록과 함께 호출되는 콜백.
     */
    override fun onRequestPermissions(
        permissions: List<String>,
        onResult: (deniedPermissions: List<String>) -> Unit,
    ) {
        permissionDelegate.requestPermissions(permissions, onResult)
    }

    /**
     * Called when the activity is starting.
     * Initializes PermissionDelegate and restores permission state from savedInstanceState.<br><br>
     * 액티비티가 시작될 때 호출됩니다.<br>
     * PermissionDelegate를 초기화하고 savedInstanceState에서 권한 상태를 복원합니다.<br>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState.<br><br>
     *                           액티비티가 이전에 종료된 후 다시 초기화되는 경우,
     *                           이 Bundle에는 onSaveInstanceState에서 가장 최근에 제공된 데이터가 포함됩니다.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionDelegate = PermissionDelegate(this)
        permissionDelegate.onRestoreInstanceState(savedInstanceState)
        beforeOnCreated(savedInstanceState)
    }

    /**
     * Override this method to perform initialization before the standard onCreate logic.<br>
     * Called after super.onCreate() but before any child class initialization.<br><br>
     * 표준 onCreate 로직 전에 초기화를 수행하려면 이 메서드를 오버라이드하세요.<br>
     * super.onCreate() 후 자식 클래스 초기화 전에 호출됩니다.<br>
     *
     * @param savedInstanceState The saved instance state bundle, if any.<br><br>
     *                           저장된 인스턴스 상태 번들 (있는 경우).<br>
     */
    protected open fun beforeOnCreated(savedInstanceState: Bundle?) {}

    /**
     * Called to retrieve per-instance state from an activity before being killed.
     * Saves the current permission state to the Bundle.<br><br>
     * 액티비티가 종료되기 전에 인스턴스별 상태를 검색하기 위해 호출됩니다.<br>
     * 현재 권한 상태를 Bundle에 저장합니다.<br>
     *
     * @param outState Bundle in which to place your saved state.<br><br>
     *                 저장된 상태를 배치할 Bundle.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        permissionDelegate.onSaveInstanceState(outState)
    }

    /**
     * Returns the current StatusBar height in pixels.<br>
     * 현재 StatusBar 높이를 픽셀 단위로 반환합니다.<br>
     *
     * @return The StatusBar height in pixels.<br><br>
     *         StatusBar 높이 (픽셀 단위).<br>
     */
    public fun getStatusBarHeight(): Int = activityWindow.getStatusBarHeight()

    /**
     * Returns the current NavigationBar height in pixels.<br>
     * 현재 NavigationBar 높이를 픽셀 단위로 반환합니다.<br>
     *
     * @return The NavigationBar height in pixels.<br><br>
     *         NavigationBar 높이 (픽셀 단위).<br>
     */
    public fun getNavigationBarHeight(): Int = activityWindow.getNavigationBarHeight(findViewById<View>(android.R.id.content).height)

    /**
     * Configures system bars (status and navigation) with the same color.<br><br>
     * 시스템 바(상태 및 네비게이션)를 동일한 색상으로 설정합니다.<br>
     *
     * @param color The color to set for both status and navigation bars.<br><br>
     *              상태 및 네비게이션 바에 설정할 색상.<br>
     *
     * @param isLightSystemBars Whether to use light system bar icons (dark icons for light backgrounds).<br><br>
     *                          라이트 시스템 바 아이콘 사용 여부 (밝은 배경에는 어두운 아이콘).<br>
     */
    protected fun setSystemBarsColor(@ColorInt color: Int, isLightSystemBars: Boolean = false) {
        setStatusBarColor(color, isLightSystemBars)
        setNavigationBarColor(color, isLightSystemBars)
    }

    /**
     * Sets the status bar color.<br><br>
     * 상태 표시줄 색상을 설정합니다.<br>
     *
     * @param color The color to set for StatusBar.<br><br>
     *              StatusBar에 설정할 색상.<br>
     *
     * @param isLightStatusBar Whether to use light status bar icons. true for dark icons (light mode), false for light icons (dark mode).<br><br>
     *                         라이트 상태 바 아이콘 사용 여부. true일 경우 어두운 아이콘(라이트 모드), false일 경우 밝은 아이콘(다크 모드).<br>
     */
    protected fun setStatusBarColor(@ColorInt color: Int, isLightStatusBar: Boolean = false) {
        activityWindow.setStatusBarColor(this@RootActivity, color, isLightStatusBar)
    }

    /**
     * Sets the navigation bar color.<br><br>
     * 네비게이션 바 색상을 설정합니다.<br>
     *
     * @param color The color to set for NavigationBar.<br><br>
     *              NavigationBar에 설정할 색상.<br>
     *
     * @param isLightNavigationBar Whether to use light navigation bar icons (dark icons for light backgrounds).<br><br>
     *                             라이트 네비게이션 바 아이콘 사용 여부 (밝은 배경에는 어두운 아이콘).<br>
     */
    protected fun setNavigationBarColor(@ColorInt color: Int, isLightNavigationBar: Boolean = false) {
        val contentViewHeight =
            checkSdkVersion(
                Build.VERSION_CODES.VANILLA_ICE_CREAM,
                positiveWork = { findViewById<View>(android.R.id.content).height },
                negativeWork = { 0 },
            )

        activityWindow.setNavigationBarColor(
            this@RootActivity,
            color,
            contentViewHeight,
            isLightNavigationBar,
        )
    }

    /**
     * Sets the system bar icons mode (StatusBar + NavigationBar).<br><br>
     * 시스템 바 아이콘 밝기 모드를 설정합니다 (light 또는 dark).<br>
     *
     * @param isDarkIcon True for dark icons (light mode), false for light icons (dark mode).<br><br>
     *                   어두운 아이콘은 true (라이트 모드), 밝은 아이콘은 false (다크 모드).<br>
     */
    protected fun setSystemBarsAppearance(isDarkIcon: Boolean) {
        setStatusBarAppearance(isDarkIcon)
        setNavigationBarAppearance(isDarkIcon)
    }

    /**
     * Sets the StatusBar icon mode (Dark or Light).<br>
     * It may not work on certain devices.<br><br>
     * StatusBar 아이콘 모드를 설정합니다 (Dark 또는 Light).<br>
     * 특정 장치에서는 작동하지 않을 수 있습니다.<br>
     *
     * @param isDarkIcon true for dark icons, false for light icons.<br><br>
     *                   어두운 아이콘은 true, 밝은 아이콘은 false.<br>
     */
    protected fun setStatusBarAppearance(isDarkIcon: Boolean) {
        activityWindow.setStatusBarAppearance(isDarkIcon)
    }

    /**
     * Sets the NavigationBar icon mode (Dark or Light).<br>
     * It may not work on certain devices.<br><br>
     * NavigationBar 아이콘 모드를 설정합니다 (Dark 또는 Light).<br>
     * 특정 장치에서는 작동하지 않을 수 있습니다.<br>
     *
     * @param isDarkIcon true for dark icons, false for light icons.<br><br>
     *                   어두운 아이콘은 true, 밝은 아이콘은 false.<br>
     */
    protected fun setNavigationBarAppearance(isDarkIcon: Boolean) {
        activityWindow.setNavigationBarAppearance(isDarkIcon)
    }

    /**
     * Resets system bars (StatusBar and NavigationBar) to their initial state.<br>
     * Restores default colors and visibility mode.<br><br>
     * 시스템 바(StatusBar와 NavigationBar)를 초기 상태로 초기화합니다.<br>
     * 기본 색상과 가시성 모드를 복원합니다.<br>
     */
    protected fun setSystemBarsReset() {
        statusBarReset()
        navigationBarReset()
    }

    /**
     * Resets the navigation bar to its initial state.<br>
     * 네비게이션 바를 초기 상태로 복원합니다.<br>
     */
    protected fun navigationBarReset() {
        activityWindow.navigationBarReset()
    }

    /**
     * Resets the StatusBar to its initial state.<br>
     * Removes custom background views on API 35+ and restores theme default color on older versions.<br><br>
     * StatusBar를 초기 상태로 초기화합니다.<br>
     * API 35+에서는 커스텀 배경 뷰를 제거하고 이전 버전에서는 테마 기본 색상을 복원합니다.<br>
     */
    protected fun statusBarReset() {
        activityWindow.statusBarReset()
    }

    /**
     * Hides both StatusBar and NavigationBar.<br><br>
     * StatusBar와 NavigationBar를 모두 숨깁니다.<br>
     */
    protected fun setSystemBarsGone() {
        statusBarGone()
        navigationBarGone()
    }

    /**
     * Hides the StatusBar.<br>
     * StatusBar를 숨깁니다.<br>
     */
    protected fun statusBarGone() {
        activityWindow.statusBarGone()
    }

    /**
     * Hides the NavigationBar.<br>
     * NavigationBar를 숨깁니다.<br>
     */
    protected fun navigationBarGone() {
        activityWindow.navigationBarGone()
    }

    /**
     * Shows the StatusBar.<br>
     * StatusBar를 표시합니다.<br>
     */
    protected fun statusBarVisible() {
        activityWindow.statusBarVisible()
    }

    /**
     * Shows the NavigationBar.<br>
     * NavigationBar를 표시합니다.<br>
     */
    protected fun navigationBarVisible() {
        activityWindow.navigationBarVisible()
    }
}
