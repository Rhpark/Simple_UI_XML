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

abstract class RootActivity : AppCompatActivity(), PermissionRequester {

    /************************
     *   Permission Check   *
     ************************/
    protected lateinit var permissionDelegate : PermissionDelegate<AppCompatActivity>

    /************************************
     *  API 35+ StatusBar 배경 View 관리  *
     ************************************/
    private var statusBarBackgroundView: View? = null
    private var navigationBarBackgroundView: View? = null


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
     * Override this method to perform initialization before the standard onCreate logic.
     */
    protected open fun beforeOnCreated(savedInstanceState: Bundle?) {}

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        permissionDelegate.onSaveInstanceState(outState)
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
     * Sets the status bar color.
     * 상태 표시줄 색상 설정.
     *
     * ## API 버전별 동작 방식
     *
     * ### API 28-34 (Android 9.0 ~ Android 14)
     * - `window.statusBarColor`를 직접 설정하여 StatusBar 색상 변경
     * - 기존 방식으로 안정적으로 동작
     *
     * ### API 35+ (Android 15+)
     * - StatusBar가 항상 투명하게 강제됨
     * 색 변경시
     *
     * @param color The color to set. StatusBar 색상
     * @param isLightStatusBar Whether to use light status bar icons. true일 경우 어두운 아이콘(라이트 모드), false일 경우 밝은 아이콘(다크 모드)
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
     * Sets the navigation bar color.
     * 네비게이션 바 색상 설정.
     *
     * @param color The color to set.
     * @param isLightNavigationBar Whether to use light navigation bar icons.
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
     * Sets the system bar icons mode(StatusBar + NavigationBar).
     * 시스템 바 아이콘을 밝기 모드 설정(light 또는 dark).
     *
     * @param isDarkIcon True for dark icons (light mode), false for light icons (dark mode).
     */
    protected fun setSystemBarsAppearance(isDarkIcon: Boolean) {
        setStatusBarAppearance(isDarkIcon)
        setNavigationBarAppearance(isDarkIcon)
    }

    /**
     *  StatusBar Icon Mode(Dark or Light)
     *
     *  It may not work on certain devices.
     *  특정 장치에서는 작동하지 않을 수 있습니다.
     *
     *  @param isDarkIcon true is Dark, false is Bright
     */
    protected fun setStatusBarAppearance(isDarkIcon: Boolean) {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = isDarkIcon
        }
    }

    /**
     *  NavigationBar Icon Mode(Dark or Light)
     *
     *  It may not work on certain devices.
     *  특정 장치에서는 작동하지 않을 수 있습니다.
     *
     *  @param isDarkIcon true is Dark, false is Bright
     */
    protected fun setNavigationBarAppearance(isDarkIcon: Boolean) {
        window.apply {
            WindowCompat.getInsetsController(this, decorView).apply {
                isAppearanceLightNavigationBars = isDarkIcon
            }
        }
    }


    /**
     * SystemBar Reset(Color, VisibleMode).
     * 시스템 바 초기화(색상, 가시성 모드).
     */
    protected fun setSystemBarsReset() {
        statusBarReset()
        navigationBarReset()
    }

    /**
     * Resets the navigation bar to its initial state.
     * 네비게이션 바를 초기 상태로 복원.
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
     * StatusBar Reset
     * 상태 표시줄 초기화.
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
     * SystemBar Gone.
     * 시스템 바 Gone.
     */
    protected fun setSystemBarsGone() {
        statusBarGone()
        navigationBarGone()
    }

    /**
     * Sets the status bar to transparent.
     * 상태 표시줄을 투명하게 설정.
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
     * Hides the navigation bar.
     * 네비게이션 바를 숨김.
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
     * API 35+ StatusBar 배경 View 초기화 및 추가
     *
     * API 35+에서는 StatusBar가 항상 투명하므로, StatusBar 영역에 배치될
     * 커스텀 배경 View를 생성하여 색상을 표현합니다.
     *
     * ## 동작 방식
     * 1. 기존에 생성된 StatusBar 배경 View가 있다면 제거 (색상 변경 시)
     * 2. 새로운 View를 생성하고 지정된 색상 적용
     * 3. DecorView의 최상위(index 0)에 배치하여 StatusBar 영역을 덮음
     * 4. 초기 높이를 명시적으로 설정 (WRAP_CONTENT는 화면 전체를 덮는 문제 발생)
     * 5. WindowInsets 리스너를 등록하여 화면 회전 등의 상황에서 높이 동적 업데이트
     *
     * @param color 적용할 StatusBar 색상
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
     * API 35+ ContentView에 StatusBar 영역만큼 padding 추가
     *
     * API 35+에서 Edge-to-edge 레이아웃이 활성화되면 ContentView가 StatusBar 영역까지 확장됩니다.
     * 이로 인해 기존 레이아웃이 StatusBar와 겹치는 문제가 발생하므로,
     * ContentView에 top padding을 추가하여 API 버전에 관계없이 일관된 레이아웃 동작을 보장합니다.
     *
     * ## 동작 방식
     * 1. ContentView(android.R.id.content)를 가져옴
     * 2. 즉시 top padding을 StatusBar 높이만큼 설정 (WindowInsets 리스너는 지연 호출되므로)
     * 3. WindowInsets 리스너를 등록하여 화면 회전 등의 상황에서 padding 동적 업데이트
     *
     * ## 설계 결정
     * - **Padding 추가 이유**: 기존 사용자 코드가 API 버전에 관계없이 동일하게 동작하도록 보장
     * - **하위 호환성**: API 28-34와 동일한 레이아웃 동작 유지
     * - **대안**: Edge-to-edge 디자인이 필요한 경우 `setStatusBarTransparent()` 사용
     *
     * @param statusBarHeight StatusBar 높이 (픽셀 단위)
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
     * Shows the navigation bar.
     * 네비게이션 바를 표시.
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