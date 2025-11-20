package kr.open.library.simple_ui.presenter.ui.activity


import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import kr.open.library.simple_ui.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.permissions.register.PermissionRequester
import kr.open.library.simple_ui.permissions.register.PermissionDelegate

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
abstract class RootActivity : AppCompatActivity(), PermissionRequester {

    /**
     * Delegate for handling runtime permission requests.<br><br>
     * 런타임 권한 요청을 처리하는 델리게이트입니다.<br>
     */
    protected lateinit var permissionDelegate : PermissionDelegate<AppCompatActivity>

    /**
     * Background view for StatusBar color on API 35+.<br><br>
     * API 35+에서 StatusBar 색상을 위한 배경 뷰입니다.<br>
     */
    private var statusBarBackgroundView: View? = null

    /**
     * Background view for NavigationBar color on API 35+.<br><br>
     * API 35+에서 NavigationBar 색상을 위한 배경 뷰입니다.<br>
     */
    private var navigationBarBackgroundView: View? = null

    /**
     * Returns the current StatusBar height in pixels.<br>
     * Uses WindowInsets on API 30+ and legacy method on older versions.<br><br>
     * 현재 StatusBar 높이를 픽셀 단위로 반환합니다.<br>
     * API 30+에서는 WindowInsets를 사용하고 이전 버전에서는 레거시 메서드를 사용합니다.<br>
     */
    public val statusBarHeight: Int
        get() = checkSdkVersion(Build.VERSION_CODES.R,
            positiveWork = { window.decorView.getRootWindowInsets()?.getInsets(WindowInsets.Type.statusBars())?.top ?: 0 },
            negativeWork = { Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.top }
        )

    /**
     * Returns the current NavigationBar height in pixels.<br>
     * Uses WindowInsets on API 30+ and legacy calculation on older versions.<br><br>
     * 현재 NavigationBar 높이를 픽셀 단위로 반환합니다.<br>
     * API 30+에서는 WindowInsets를 사용하고 이전 버전에서는 레거시 계산을 사용합니다.<br>
     */
    public val navigationBarHeight: Int
        get() = checkSdkVersion(Build.VERSION_CODES.R,
            positiveWork = { window.decorView.getRootWindowInsets()?.getInsets(WindowInsets.Type.navigationBars())?.bottom ?: 0 },
            negativeWork = {
                val rootView = window.decorView.rootView
                val contentViewHeight = findViewById<View>(android.R.id.content).height
                (rootView.height - contentViewHeight) - statusBarHeight
            }
        )

    override fun onRequestPermissions(permissions: List<String>, onResult: (deniedPermissions: List<String>) -> Unit) {
        permissionDelegate.requestPermissions(permissions, onResult)
    }


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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        permissionDelegate.onSaveInstanceState(outState)
    }


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
     * ## API version behavior / API 버전별 동작 방식<br>
     *
     * ### API 28-34 (Android 9.0 ~ Android 14)<br>
     * - Sets StatusBar color directly via `window.statusBarColor`<br>
     * - `window.statusBarColor`를 직접 설정하여 StatusBar 색상 변경<br>
     *
     * ### API 35+ (Android 15+)<br>
     * - StatusBar is always transparent by system<br>
     * - Uses custom background View to simulate color<br>
     * - StatusBar가 시스템에 의해 항상 투명하게 강제됨<br>
     * - 색상을 시뮬레이션하기 위해 커스텀 배경 뷰 사용<br>
     *
     * @param color The color to set for StatusBar.<br><br>
     *              StatusBar에 설정할 색상.<br>
     *
     * @param isLightStatusBar Whether to use light status bar icons. true for dark icons (light mode), false for light icons (dark mode).<br><br>
     *                         라이트 상태 바 아이콘 사용 여부. true일 경우 어두운 아이콘(라이트 모드), false일 경우 밝은 아이콘(다크 모드).<br>
     */
    protected fun setStatusBarColor(@ColorInt color: Int, isLightStatusBar: Boolean = false) {

        checkSdkVersion(Build.VERSION_CODES.VANILLA_ICE_CREAM,
            positiveWork = { setStatusBarColorSdk35(color) },
            negativeWork = {
                // API 34 이하: 기존 방식으로 StatusBar 색상 직접 설정
                @Suppress("DEPRECATION")
                window.statusBarColor = color
            }
        )

        window.apply {
            // StatusBar 아이콘 색상 설정 (모든 API 버전 공통)
            val insetsController = WindowCompat.getInsetsController(this, decorView)
            insetsController.isAppearanceLightStatusBars = isLightStatusBar
        }
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

        checkSdkVersion(Build.VERSION_CODES.VANILLA_ICE_CREAM,
            positiveWork = { setNavigationBarColorSdk35(color) },
            negativeWork = {
                @Suppress("DEPRECATION")
                window.navigationBarColor = color
            }
        )

        window.apply {
            val insetsController = WindowCompat.getInsetsController(this, decorView)
            insetsController.isAppearanceLightNavigationBars = isLightNavigationBar
        }
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
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = isDarkIcon
        }
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
        window.apply {
            WindowCompat.getInsetsController(this, decorView).apply {
                isAppearanceLightNavigationBars = isDarkIcon
            }
        }
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
     * Removes custom background views on API 35+ and restores theme default color on older versions.<br><br>
     * 네비게이션 바를 초기 상태로 복원합니다.<br>
     * API 35+에서는 커스텀 배경 뷰를 제거하고 이전 버전에서는 테마 기본 색상을 복원합니다.<br>
     */
    protected fun navigationBarReset() {
        window.apply {
            checkSdkVersion(Build.VERSION_CODES.VANILLA_ICE_CREAM,
                positiveWork = {

                    // ContentView padding 제거
                    findViewById<ViewGroup>(android.R.id.content)?.setPadding(0, 0, 0, 0)

                    // ContentView 찾기
                    val contentView = findViewById<ViewGroup>(android.R.id.content)

                    // WindowInsets 리스너 제거 (자동 padding 방지)
                    contentView?.let {
                        ViewCompat.setOnApplyWindowInsetsListener(it, null)
                        it.setPadding(0, 0, 0, 0)
                    }

                    // API 35+ 배경 View 제거
                    navigationBarBackgroundView?.let {
                        (decorView as? FrameLayout)?.removeView(it)
                        navigationBarBackgroundView = null
                    }

                    // NavigationBar 보이기
                    val insetsController = WindowCompat.getInsetsController(this, decorView)
                    insetsController.show(WindowInsetsCompat.Type.navigationBars())
                    WindowCompat.setDecorFitsSystemWindows(this, true)
                },
                negativeWork = {
                    // 테마 기본 색상으로 복원
                    val typedValue = TypedValue()
                    context.theme.resolveAttribute(android.R.attr.navigationBarColor, typedValue, true)
                    @Suppress("DEPRECATION")
                    navigationBarColor = typedValue.data

                    // systemUiVisibility 플래그 제거
                    @Suppress("DEPRECATION")
                    decorView.systemUiVisibility = decorView.systemUiVisibility and
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv() and
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv()
                }
            )
        }
    }

    /**
     * Resets the StatusBar to its initial state.<br>
     * Removes custom background views on API 35+ and restores theme default color on older versions.<br><br>
     * StatusBar를 초기 상태로 초기화합니다.<br>
     * API 35+에서는 커스텀 배경 뷰를 제거하고 이전 버전에서는 테마 기본 색상을 복원합니다.<br>
     */
    protected fun statusBarReset() {
        window.apply {
            // StatusBar 다시 보이기
            checkSdkVersion(Build.VERSION_CODES.VANILLA_ICE_CREAM,
                positiveWork = {
                    // ContentView padding 제거
                    findViewById<ViewGroup>(android.R.id.content)?.setPadding(0, 0, 0, 0)

                    // ContentView 찾기
                    val contentView = findViewById<ViewGroup>(android.R.id.content)

                    // WindowInsets 리스너 제거 (자동 padding 방지)
                    contentView?.let {
                        ViewCompat.setOnApplyWindowInsetsListener(it, null)
                        it.setPadding(0, 0, 0, 0)
                    }

                    // API 35+ 배경 View 제거
                    statusBarBackgroundView?.let {
                        (decorView as? FrameLayout)?.removeView(it)
                        statusBarBackgroundView = null
                    }

                    // StatusBar 보이기
                    val insetsController = WindowCompat.getInsetsController(this, decorView)
                    insetsController.show(WindowInsetsCompat.Type.statusBars())
                    WindowCompat.setDecorFitsSystemWindows(this, true)
                },
                negativeWork = {
                    // 테마에 정의된 기본 statusBarColor 가져오기
                    val typedValue = TypedValue()
                    context.theme.resolveAttribute(android.R.attr.statusBarColor, typedValue, true)
                    @Suppress("DEPRECATION")
                    statusBarColor = typedValue.data
                    clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

                    @Suppress("DEPRECATION")
                    clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                }
            )
        }
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
     * Uses WindowInsetsController on API 30+ and FLAG_FULLSCREEN on older versions.<br><br>
     * StatusBar를 숨깁니다.<br>
     * API 30+에서는 WindowInsetsController를 사용하고 이전 버전에서는 FLAG_FULLSCREEN을 사용합니다.<br>
     */
    protected fun statusBarGone() {
        window.apply {
            checkSdkVersion(Build.VERSION_CODES.R,
                positiveWork = {
                    // API 30+: WindowInsetsController 사용
                    val insetsController = WindowCompat.getInsetsController(this, decorView)
                    insetsController.hide(WindowInsetsCompat.Type.statusBars())
                    WindowCompat.setDecorFitsSystemWindows(this, false)
                },
                negativeWork = {

                    // API 29 이하: StatusBar만 투명하게
                    @Suppress("DEPRECATION")
                    addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

                    // NavigationBar 색상은 유지 (기존 값 그대로 또는 명시적으로 설정)
                    // API 29 이하: FLAG_LAYOUT_NO_LIMITS 사용
//                    setFlags(
//                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//                    )
                }
            )
        }
    }

    /**
     * Hides the NavigationBar.<br>
     * Uses WindowInsetsController on API 30+ and SYSTEM_UI_FLAG on older versions.<br><br>
     * NavigationBar를 숨깁니다.<br>
     * API 30+에서는 WindowInsetsController를 사용하고 이전 버전에서는 SYSTEM_UI_FLAG를 사용합니다.<br>
     */
    protected fun navigationBarGone() {
        window.apply {
            checkSdkVersion(Build.VERSION_CODES.R,
                positiveWork = {
                    // API 30+: WindowInsetsController 사용
                    val insetsController = WindowCompat.getInsetsController(this, decorView)
                    insetsController.hide(WindowInsetsCompat.Type.navigationBars())
                    WindowCompat.setDecorFitsSystemWindows(this, false)
                },
                negativeWork = {
                    // API 29 이하: NavigationBar 숨김
                    @Suppress("DEPRECATION")
                    decorView.systemUiVisibility = decorView.systemUiVisibility or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                }
            )
        }
    }


    /**
     * Shows the StatusBar.<br>
     * Uses WindowInsetsController on API 30+ and clears FLAG_FULLSCREEN on older versions.<br><br>
     * StatusBar를 표시합니다.<br>
     * API 30+에서는 WindowInsetsController를 사용하고 이전 버전에서는 FLAG_FULLSCREEN을 해제합니다.<br>
     */
    protected fun statusBarVisible() {
        window.apply {
            checkSdkVersion(Build.VERSION_CODES.R,
                positiveWork = {
                    WindowCompat.setDecorFitsSystemWindows(this, true)
                    val insetsController = WindowCompat.getInsetsController(this, decorView)
//                    insetsController.show(WindowInsetsCompat.Type.statusBars())
                    insetsController.show(WindowInsetsCompat.Type.statusBars())
                },
                negativeWork = {
                    @Suppress("DEPRECATION")
                    clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

//                    clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                }
            )
        }
    }


    /**
     * Initializes and adds a background View for StatusBar on API 35+.<br><br>
     * API 35+에서 StatusBar 배경 뷰를 초기화하고 추가합니다.<br>
     *
     * Since StatusBar is always transparent on API 35+, this creates a custom background View
     * positioned in the StatusBar area to simulate color.<br><br>
     * API 35+에서는 StatusBar가 항상 투명하므로, StatusBar 영역에 배치될
     * 커스텀 배경 뷰를 생성하여 색상을 표현합니다.<br>
     *
     * ## How it works / 동작 방식<br>
     * 1. Remove existing StatusBar background View if present (for color changes)<br>
     *    기존에 생성된 StatusBar 배경 뷰가 있다면 제거 (색상 변경 시)<br>
     * 2. Create new View and apply specified color<br>
     *    새로운 뷰를 생성하고 지정된 색상 적용<br>
     * 3. Add to DecorView at index 0 (topmost) to cover StatusBar area<br>
     *    DecorView의 최상위(index 0)에 배치하여 StatusBar 영역을 덮음<br>
     * 4. Set explicit height (WRAP_CONTENT causes full screen coverage issue)<br>
     *    명시적으로 높이 설정 (WRAP_CONTENT는 화면 전체를 덮는 문제 발생)<br>
     * 5. Register WindowInsets listener for dynamic height updates on rotation<br>
     *    화면 회전 등의 상황에서 높이 동적 업데이트를 위해 WindowInsets 리스너 등록<br>
     *
     * @param color The color to apply to StatusBar.<br><br>
     *              StatusBar에 적용할 색상.<br>
     */
    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun setStatusBarColorSdk35(@ColorInt color: Int) {
        window.apply {
            val decorView = decorView as? FrameLayout ?: return

            // 기존 StatusBar 배경 View 제거 (색상 변경 시 중복 생성 방지)
            statusBarBackgroundView?.let { decorView.removeView(it) }

            // 새로운 StatusBar 배경 View 생성
            statusBarBackgroundView = View(this@RootActivity).apply {
                setBackgroundColor(color)
                id = View.generateViewId()
            }

            // DecorView에 추가
            // - index 0: 최상위에 배치하여 다른 View들 위에 표시
            // - height: statusBarHeight로 명시적 설정 (WRAP_CONTENT 사용 시 전체 화면을 덮는 문제 발생)
            // - gravity: TOP으로 설정하여 화면 상단에 고정
            decorView.addView(
                statusBarBackgroundView,
                0,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    statusBarHeight
                ).apply { gravity = android.view.Gravity.TOP }
            )

            // WindowInsets 리스너 등록
            // 화면 회전, 시스템 UI 변경 등의 상황에서 StatusBar 높이가 변경될 수 있으므로
            // WindowInsets를 통해 실시간으로 높이를 업데이트
            ViewCompat.setOnApplyWindowInsetsListener(statusBarBackgroundView!!) { view, insets ->
                val statusBarInsets = insets.getInsets(WindowInsets.Type.statusBars())
                view.layoutParams = (view.layoutParams as FrameLayout.LayoutParams).apply {
                    height = statusBarInsets.top
                }
                view.requestLayout()
                insets
            }

            // API 35+: Edge-to-edge 활성화
            // ContentView에 padding 추가하여 기존 레이아웃이 StatusBar와 겹치지 않도록 보장
            // setPaddingForStatusBarSdk35()
            WindowCompat.setDecorFitsSystemWindows(this, false)
        }
    }


    /**
     * Adds padding to ContentView for StatusBar area on API 35+.<br><br>
     * API 35+에서 ContentView에 StatusBar 영역만큼 padding을 추가합니다.<br>
     *
     * When Edge-to-edge layout is enabled on API 35+, ContentView extends into the StatusBar area.
     * This causes existing layouts to overlap with StatusBar, so top padding is added to ensure
     * consistent layout behavior across API versions.<br><br>
     * API 35+에서 Edge-to-edge 레이아웃이 활성화되면 ContentView가 StatusBar 영역까지 확장됩니다.
     * 이로 인해 기존 레이아웃이 StatusBar와 겹치는 문제가 발생하므로,
     * ContentView에 top padding을 추가하여 API 버전에 관계없이 일관된 레이아웃 동작을 보장합니다.<br>
     *
     * ## How it works / 동작 방식<br>
     * 1. Get ContentView (android.R.id.content)<br>
     *    ContentView(android.R.id.content)를 가져옴<br>
     * 2. Immediately set top padding to StatusBar height (WindowInsets listener is called with delay)<br>
     *    즉시 top padding을 StatusBar 높이만큼 설정 (WindowInsets 리스너는 지연 호출되므로)<br>
     * 3. Register WindowInsets listener for dynamic padding updates on rotation<br>
     *    화면 회전 등의 상황에서 padding 동적 업데이트를 위해 WindowInsets 리스너 등록<br>
     *
     * ## Design decisions / 설계 결정<br>
     * - **Why add padding**: Ensures existing user code works identically across API versions<br>
     *   **Padding 추가 이유**: 기존 사용자 코드가 API 버전에 관계없이 동일하게 동작하도록 보장<br>
     * - **Backward compatibility**: Maintains same layout behavior as API 28-34<br>
     *   **하위 호환성**: API 28-34와 동일한 레이아웃 동작 유지<br>
     */
    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun setPaddingForStatusBarSdk35() {
        val contentView = findViewById<ViewGroup>(android.R.id.content)

        // 즉시 padding 적용
        // WindowInsets 리스너는 View가 attach되고 layout이 완료된 후 호출되므로
        // 초기 렌더링 시 깜빡임을 방지하기 위해 즉시 적용
        contentView.setPadding(
            contentView.paddingLeft,
            statusBarHeight,
            contentView.paddingRight,
            contentView.paddingBottom
        )
        Logx.d("setStatusBarColor, contentView padding applied: $statusBarHeight")

        // WindowInsets 리스너로 동적 업데이트
        // 화면 회전, 시스템 UI 변경 등의 상황에서 StatusBar 높이가 변경될 수 있으므로
        // WindowInsets를 통해 실시간으로 padding을 업데이트
        ViewCompat.setOnApplyWindowInsetsListener(contentView) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsets.Type.statusBars())
            view.setPadding(
                view.paddingLeft,
                statusBarInsets.top,
                view.paddingRight,
                view.paddingBottom
            )
            insets
        }
    }

    /**
     * Initializes and adds a background View for NavigationBar on API 35+.<br><br>
     * API 35+에서 NavigationBar 배경 뷰를 초기화하고 추가합니다.<br>
     *
     * @param color The color to apply to NavigationBar.<br><br>
     *              NavigationBar에 적용할 색상.<br>
     */
    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun setNavigationBarColorSdk35(@ColorInt color: Int) {
        window.apply {
            val decorView = window.decorView as? FrameLayout ?: return

            // 기존 NavigationBar 배경 View 제거 (색상 변경 시 중복 생성 방지)
            navigationBarBackgroundView?.let { decorView.removeView(it) }

            // 새로운 NavigationBar 배경 View 생성
            navigationBarBackgroundView = View(this@RootActivity).apply {
                setBackgroundColor(color)
                id = View.generateViewId()
                elevation = 100f
            }

            // DecorView에 추가
            // - index 0: 최상위에 배치하여 다른 View들 위에 표시
            // - gravity: BOTTOM 설정하여 화면 하단에 고정
            decorView.addView(
                navigationBarBackgroundView,
                0,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    navigationBarHeight
                ).apply { gravity = android.view.Gravity.BOTTOM }
            )

            // WindowInsets 리스너 등록
            // 화면 회전, 시스템 UI 변경 등의 상황에서 NavigationBar 높이가 변경될 수 있으므로
            // WindowInsets를 통해 실시간으로 높이를 업데이트
            ViewCompat.setOnApplyWindowInsetsListener(navigationBarBackgroundView!!) { view, insets ->
                val navigationBarInsets = insets.getInsets(WindowInsets.Type.navigationBars())
                view.layoutParams = (view.layoutParams as FrameLayout.LayoutParams).apply {
                    height = navigationBarInsets.bottom
                }
                view.requestLayout()
                insets
            }

            WindowCompat.setDecorFitsSystemWindows(this, false)
        }
    }

    /**
     * Shows the NavigationBar.<br>
     * Uses WindowInsetsController on API 30+ and clears SYSTEM_UI_FLAG on older versions.<br><br>
     * NavigationBar를 표시합니다.<br>
     * API 30+에서는 WindowInsetsController를 사용하고 이전 버전에서는 SYSTEM_UI_FLAG를 해제합니다.<br>
     */
    protected fun navigationBarVisible() {
        window.apply {
            checkSdkVersion(Build.VERSION_CODES.R,
                positiveWork = {
                    // API 30+: WindowInsetsController 사용
                    WindowCompat.setDecorFitsSystemWindows(this, true)
                    val insetsController = WindowCompat.getInsetsController(this, decorView)
                    insetsController.show(WindowInsetsCompat.Type.navigationBars())
                },
                negativeWork = {
                    // API 29 이하: NavigationBar 표시
                    @Suppress("DEPRECATION")
                    decorView.systemUiVisibility = decorView.systemUiVisibility and
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv() and
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv()
                }
            )
        }
    }
}