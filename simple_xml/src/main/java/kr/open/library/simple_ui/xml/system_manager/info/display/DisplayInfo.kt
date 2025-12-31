package kr.open.library.simple_ui.xml.system_manager.info.display

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowMetrics
import androidx.annotation.RequiresApi
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.extensions.getWindowManager

/**
 * Provides display-related metrics such as physical screen size, app window size, status bar, and navigation bar sizes.<br><br>
 * 물리 화면 크기, 앱 윈도우 크기, 상태 바, 내비게이션 바 크기 등 디스플레이 정보를 제공합니다.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Android's display APIs changed significantly in API 30 (R), requiring different approaches for older and newer versions.<br>
 * - System bar sizes vary across devices, orientations, and UI modes (gesture navigation vs. button navigation).<br>
 * - Multi-window mode requires separate handling of physical screen size vs app window size.<br>
 * - Accessing display metrics requires repetitive null checks, try-catch blocks, and resource lookups.<br>
 * - This class provides a unified, safe API for retrieving display information across all Android versions.<br><br>
 * - Android의 디스플레이 API는 API 30(R)에서 크게 변경되어 이전/이후 버전에 대해 다른 접근 방식이 필요합니다.<br>
 * - 시스템 바 크기는 기기, 화면 방향, UI 모드(제스처 네비게이션 vs 버튼 네비게이션)에 따라 다릅니다.<br>
 * - 멀티윈도우 모드에서는 물리 화면 크기와 앱 윈도우 크기를 별도로 처리해야 합니다.<br>
 * - 디스플레이 정보 접근 시 반복적인 null 체크, try-catch 블록, 리소스 조회가 필요합니다.<br>
 * - 이 클래스는 모든 Android 버전에서 디스플레이 정보를 안전하게 가져올 수 있는 통합 API를 제공합니다.<br>
 *
 * **Design decisions / 설계 결정 이유:**<br>
 * - **SDK version branching**: Uses `checkSdkVersion` to handle API 30+ WindowMetrics vs legacy DisplayMetrics APIs.<br>
 * - **Nullable return types**: Returns null instead of throwing exceptions for unavailable values (e.g., gesture mode navigation bar).<br>
 * - **tryCatchSystemManager pattern**: Inherits from BaseSystemService to provide consistent error handling and logging.<br>
 * - **Resource name constants**: Hardcoded Android system resource names are extracted to companion object for maintainability.<br>
 * - **DisplayInfoBarInsets semantics**: `DisplayInfoBarInsets(top, bottom, left, right)` represents system bar insets.
 *   Status bar only uses `top` (others are 0), while navigation bar uses the direction where it appears (bottom/left/right depending on orientation and device).<br><br>
 * - **SDK 버전 분기**: API 30+ WindowMetrics와 레거시 DisplayMetrics API를 처리하기 위해 `checkSdkVersion`을 사용합니다.<br>
 * - **Nullable 반환 타입**: 사용 불가능한 값(예: 제스처 모드 내비게이션 바)에 대해 예외를 던지지 않고 null을 반환합니다.<br>
 * - **tryCatchSystemManager 패턴**: BaseSystemService를 상속하여 일관된 에러 처리 및 로깅을 제공합니다.<br>
 * - **리소스 이름 상수화**: 하드코딩된 Android 시스템 리소스 이름을 companion object로 추출하여 유지보수성을 높였습니다.<br>
 * - **DisplayInfoBarInsets 의미**: `DisplayInfoBarInsets(top, bottom, left, right)`는 시스템 바 insets를 나타냅니다.
 *   Status bar는 `top`만 사용(나머지는 0)하며, Navigation bar는 나타나는 방향(화면 방향 및 기기에 따라 bottom/left/right)을 사용합니다.<br>
 *
 * **Usage / 사용법:**<br>
 * ```kotlin
 * val displayInfo = context.getDisplayInfo()
 *
 * // Get physical and app window screen sizes
 * val physicalSize = displayInfo.getPhysicalScreenSize() // DisplayInfoSize
 * val appWindowSize = displayInfo.getAppWindowSize(activity?) // DisplayInfoSize?
 *
 * // Get system bar sizes (nullable)
 * val statusBarSize = displayInfo.getStatusBarSize() // DisplayInfoSize?
 * val navBarSize = displayInfo.getNavigationBarSize() // DisplayInfoSize?
 *
 * // Check orientation and multi-window mode
 * val isPortrait = displayInfo.isPortrait() // Boolean
 * val isLandscape = displayInfo.isLandscape() // Boolean
 * val isMultiWindow = displayInfo.isInMultiWindowMode(activity) // Boolean
 * ```<br><br>
 *
 */
public open class DisplayInfo(
    context: Context
) : BaseSystemService(context, null) {
    private companion object {
        const val STATUS_BAR_RES = "status_bar_height"
        const val NAV_BAR_RES = "navigation_bar_height"
        const val NAV_BAR_LANDSCAPE_RES = "navigation_bar_height_landscape"
        const val NAV_BAR_WIDTH_RES = "navigation_bar_width"
        const val DISPLAY_DEF_TYPE = "dimen"
        const val DISPLAY_DEF_PACKAGE = "android"
    }

    private val windowManager: WindowManager by lazy { context.getWindowManager() }

    /**
     * Returns the physical screen size in pixels (ignores multi-window mode).<br><br>
     * 물리 화면 크기(픽셀)를 반환합니다 (멀티윈도우 모드 무시).<br>
     *
     * @return `DisplayInfoSize(width, height)` representing the physical screen size.<br><br>
     *         물리 화면의 너비와 높이를 담은 `DisplayInfoSize`입니다.<br>
     */
    public fun getPhysicalScreenSize(): DisplayInfoSize = tryCatchSystemManager(DisplayInfoSize(0, 0)) {
        checkSdkVersion(
            Build.VERSION_CODES.R,
            positiveWork = {
                // 최대 크기: 멀티윈도여도 전체(최대) 반환
                val b = windowManager.maximumWindowMetrics.bounds
                DisplayInfoSize(b.width(), b.height())
            },
            negativeWork = {
                // 28~29: 물리 크기
                @Suppress("DEPRECATION")
                val dm = DisplayMetrics().also { windowManager.defaultDisplay.getRealMetrics(it) }
                DisplayInfoSize(dm.widthPixels, dm.heightPixels)
            },
        )
    }

    /**
     * Returns the app window size in pixels (supports multi-window mode).<br><br>
     * 앱 윈도우 크기(픽셀)를 반환합니다 (멀티윈도우 모드 지원).<br>
     *
     * @param activity Activity instance required for API 28-29 to access `window.decorView` for accurate app window size measurement.
     *                 For API 30+, this parameter is ignored as `currentWindowMetrics` is used instead.
     *                 Pass null if Activity is unavailable (returns null for API 28-29, valid size for API 30+).<br><br>
     *                 API 28-29에서 정확한 앱 윈도우 크기 측정을 위해 `window.decorView` 접근에 필요한 Activity 인스턴스입니다.
     *                 API 30+에서는 `currentWindowMetrics`를 사용하므로 이 파라미터는 무시됩니다.
     *                 Activity를 사용할 수 없는 경우 null을 전달하면 API 28-29에서는 null을 반환하고, API 30+에서는 정상적인 크기를 반환합니다.<br>
     * @return `DisplayInfoSize(width, height)` representing the app window size.<br><br>
     *         앱 윈도우의 너비와 높이를 담은 `DisplayInfoSize`입니다.<br>
     */
    public fun getAppWindowSize(activity: Activity? = null): DisplayInfoSize? = tryCatchSystemManager(null) {
        checkSdkVersion(
            Build.VERSION_CODES.R,
            positiveWork = {
                // 현재 창: 멀티윈도면 작아짐(정상)
                val b = getCurrentWindowMetricsSdkR().bounds
                DisplayInfoSize(b.width(), b.height())
            },
            negativeWork = {
                // 28~29는 Activity decorView 기반이 가장 일관적(가능한 경우)
                activity?.let {
                    val decor = it.window.decorView
                    if (decor.width > 0 && decor.height > 0) {
                        DisplayInfoSize(decor.width, decor.height)
                    } else {
                        // fallback (정확도가 떨어질 수 있음)
                        val dm = context.resources.displayMetrics
                        DisplayInfoSize(dm.widthPixels, dm.heightPixels)
                    }
                }
            },
        )
    }

    /**
     * Returns the current window metrics for API 30+ (R).<br><br>
     * API 30+ (R)용 현재 윈도우 메트릭을 반환합니다.<br>
     *
     * @return Current window metrics.<br><br>
     *         현재 윈도우 메트릭입니다.<br>
     */
    @RequiresApi(Build.VERSION_CODES.R)
    protected fun getCurrentWindowMetricsSdkR(): WindowMetrics = windowManager.currentWindowMetrics

    /**
     * Returns the maximum window metrics for API 30+ (R), representing the physical screen bounds.<br><br>
     * API 30+ (R)용 최대 윈도우 메트릭을 반환하며, 물리 화면 영역을 나타냅니다.<br>
     *
     * @return Maximum window metrics (physical screen).<br><br>
     *         최대 윈도우 메트릭(물리 화면)입니다.<br>
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun getMaximumWindowMetricsSdkR(): WindowMetrics = windowManager.maximumWindowMetrics

    /**
     * Returns the status bar size based on physical screen dimensions as `DisplayInfoSize(width, height)`, or null if unavailable.<br><br>
     * 물리 화면 기준 상태 바 크기를 `DisplayInfoSize(가로, 세로)`로 반환하며, 사용 불가능한 경우 null을 반환합니다.<br>
     */
    @SuppressLint("InternalInsetResource")
    public fun getStatusBarSize(): DisplayInfoSize? = tryCatchSystemManager(null) {
        checkSdkVersion(
            Build.VERSION_CODES.R,
            positiveWork = {
                // 물리(최대) 화면 기준
                val metrics = getMaximumWindowMetricsSdkR()
                val bounds = metrics.bounds
                val status = metrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.statusBars())
                DisplayInfoSize(bounds.width(), status.top)
            },
            negativeWork = {
                // Pre-R: 물리 기준(리소스)로 높이 추정 + 물리 화면 폭
                val resId = context.resources.getIdentifier(STATUS_BAR_RES, DISPLAY_DEF_TYPE, DISPLAY_DEF_PACKAGE)
                val height = resId.takeIf { it > 0 }?.let { context.resources.getDimensionPixelSize(it) }
                height?.let { DisplayInfoSize(width = getPhysicalScreenSize().width, height = it) }
            },
        )
    }

    /**
     * Returns the navigation bar size for API 30+ (R) based on physical screen dimensions using WindowInsets.<br><br>
     * API 30+ (R)에서 WindowInsets를 사용하여 물리 화면 기준 내비게이션 바 크기를 반환합니다.<br>
     *
     * @return Navigation bar size as `DisplayInfoSize(width, height)`, or null if unavailable.<br><br>
     *         내비게이션 바 크기를 `DisplayInfoSize(가로, 세로)`로 반환하며, 사용 불가능한 경우 null을 반환합니다.<br>
     */
    @RequiresApi(Build.VERSION_CODES.R)
    protected fun getNavigationBarSizeSdkR(): DisplayInfoSize? = tryCatchSystemManager(null) {
        // 물리(최대) 화면 기준
        val metrics = getMaximumWindowMetricsSdkR()
        val bounds = metrics.bounds
        val nav = metrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars())

        when {
            nav.bottom > 0 -> DisplayInfoSize(width = bounds.width(), height = nav.bottom)
            nav.top > 0 -> DisplayInfoSize(width = bounds.width(), height = nav.top)
            nav.left > 0 -> DisplayInfoSize(width = nav.left, height = bounds.height())
            nav.right > 0 -> DisplayInfoSize(width = nav.right, height = bounds.height())
            else -> DisplayInfoSize(0, 0) // 제스처/하드키/예약영역 없음
        }
    }

    /**
     * Returns the navigation bar size for API 28-29 using resource dimensions.<br><br>
     * API 28-29에서 리소스 dimension을 사용하여 내비게이션 바 크기를 반환합니다.<br>
     *
     * @return Navigation bar size as `DisplayInfoSize(width, height)`, or null if unavailable.<br><br>
     *         내비게이션 바 크기를 `DisplayInfoSize(가로, 세로)`로 반환하며, 사용 불가능한 경우 null을 반환합니다.<br>
     */
    @SuppressLint("InternalInsetResource")
    private fun getNavigationBarSizeSdkNormal(): DisplayInfoSize? = tryCatchSystemManager(null) {
        val resources = context.resources

        fun Resources.dimensionOrNull(name: String): Int? {
            val id = getIdentifier(name, DISPLAY_DEF_TYPE, DISPLAY_DEF_PACKAGE)
            if (id <= 0) return null
            return getDimensionPixelSize(id).coerceAtLeast(0)
        }

        val portraitHeight = resources.dimensionOrNull(NAV_BAR_RES)
        val landscapeHeight = resources.dimensionOrNull(NAV_BAR_LANDSCAPE_RES)
        val sideWidth = resources.dimensionOrNull(NAV_BAR_WIDTH_RES)

        if (portraitHeight == null && landscapeHeight == null && sideWidth == null) {
            Logx.e("Cannot determine navigation bar size. Check navigation bar resources.")
            return null
        }

        val screenSize = getPhysicalScreenSize()
        when {
            isPortrait() -> {
                val height = portraitHeight ?: landscapeHeight
                when {
                    height == null -> null // 세로 높이를 알 수 없음(측정불가)
                    else -> DisplayInfoSize(screenSize.width, height)
                }
            }

            isLandscape() -> {
                val bottom = (landscapeHeight ?: portraitHeight) ?: 0
                val side = sideWidth ?: 0

                val prefersSide = side > 0 && (bottom <= 0 || side >= bottom)
                when {
                    prefersSide -> DisplayInfoSize(side, screenSize.height)
                    bottom > 0 -> DisplayInfoSize(screenSize.width, bottom)
                    else -> DisplayInfoSize(screenSize.width, 0)
                }
            }

            else -> {
                // ORIENTATION_UNDEFINED / SQUARE 등
                val vertical = (portraitHeight ?: landscapeHeight) ?: 0
                val side = sideWidth ?: 0
                when {
                    vertical > 0 -> DisplayInfoSize(screenSize.width, vertical)
                    side > 0 -> DisplayInfoSize(side, screenSize.height)
                    else -> DisplayInfoSize(0, 0) // 없음(두께 0)
                }
            }
        }
    }

    /**
     * Returns the navigation bar size as `DisplayInfoSize(width, height)`, or null if unavailable.<br><br>
     * 내비게이션 바 크기를 `DisplayInfoSize(가로, 세로)`로 반환하며, 사용 불가능한 경우 null을 반환합니다.<br>
     */
    @SuppressLint("InternalInsetResource")
    public fun getNavigationBarSize(): DisplayInfoSize? = checkSdkVersion(
        Build.VERSION_CODES.R,
        positiveWork = { getNavigationBarSizeSdkR() },
        negativeWork = { getNavigationBarSizeSdkNormal() },
    )

    /**
     * Returns true if the screen is in portrait orientation.<br><br>
     * 화면이 세로 방향인 경우 true를 반환합니다.<br>
     */
    public fun isPortrait(): Boolean =
        context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    /**
     * Returns true if the screen is in landscape orientation.<br><br>
     * 화면이 가로 방향인 경우 true를 반환합니다.<br>
     */
    public fun isLandscape(): Boolean =
        context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    /**
     * Returns true if the app is in multi-window mode.<br><br>
     * 앱이 멀티윈도우 모드인 경우 true를 반환합니다.<br>
     *
     * @param activity Activity instance required to access `isInMultiWindowMode` property.
     *                 Multi-window mode state is only available at the Activity level, not from Context.<br><br>
     *                 `isInMultiWindowMode` 속성에 접근하기 위해 필요한 Activity 인스턴스입니다.
     *                 멀티윈도우 모드 상태는 Context가 아닌 Activity 레벨에서만 확인 가능합니다.<br>
     */
    public fun isInMultiWindowMode(activity: Activity): Boolean = activity.isInMultiWindowMode

    /**
     * Returns the navigation bar stable insets.<br><br>
     * 내비게이션 바의 stable insets를 반환합니다.<br>
     *
     * **What are insets / Insets란:**<br>
     * Insets represent the space occupied by system UI elements from each screen edge.
     * For example, if navigation bar at the bottom is 100px tall, bottom inset is 100.
     * `DisplayInfoBarInsets(top, bottom, left, right)` provides insets for all four directions.<br><br>
     * Insets는 각 화면 가장자리로부터 시스템 UI 요소가 차지하는 공간을 나타냅니다.
     * 예를 들어 하단 내비게이션 바가 100px 높이면, bottom inset은 100입니다.
     * `DisplayInfoBarInsets(top, bottom, left, right)`는 네 방향 모두의 insets를 제공합니다.<br>
     *
     * **API version differences / API 버전별 차이:**<br>
     * - **API 30+ (R)**: Uses `WindowInsets.Type.navigationBars()` to get all directional insets (top, bottom, left, right).
     *   Returns actual inset values for the direction where navigation bar appears.<br>
     * - **API 28-29**: Uses `rootWindowInsets.stableInset*` properties. Sets `top` to 0 explicitly to avoid confusion with status bar area,
     *   and uses `bottom`, `left`, `right` for navigation bar position.<br><br>
     * - **API 30+ (R)**: `WindowInsets.Type.navigationBars()`를 사용하여 모든 방향의 insets(top, bottom, left, right)를 가져옵니다.
     *   내비게이션 바가 나타나는 방향에 대한 실제 inset 값을 반환합니다.<br>
     * - **API 28-29**: `rootWindowInsets.stableInset*` 속성을 사용합니다. 상태 바 영역과의 혼동을 피하기 위해 `top`을 명시적으로 0으로 설정하고,
     *   내비게이션 바 위치에 대해 `bottom`, `left`, `right`를 사용합니다.<br>
     *
     * @param activity Activity instance required for API 28-29 to access `window.decorView.rootWindowInsets` for app window-based insets.
     *                 For API 30+, this parameter is ignored as `currentWindowMetrics.windowInsets` is used instead.
     *                 Pass null if Activity is unavailable (returns null for API 28-29, valid insets for API 30+).<br><br>
     *                 API 28-29에서 앱 윈도우 기준 insets를 얻기 위해 `window.decorView.rootWindowInsets` 접근에 필요한 Activity 인스턴스입니다.
     *                 API 30+에서는 `currentWindowMetrics.windowInsets`를 사용하므로 이 파라미터는 무시됩니다.
     *                 Activity를 사용할 수 없는 경우 null을 전달하면 API 28-29에서는 null을 반환하고, API 30+에서는 정상적인 insets를 반환합니다.<br>
     * @return `DisplayInfoBarInsets(top, bottom, left, right)` or null if unavailable.<br>
     *         - `(0, 0, 0, 0)`: Gesture mode, hardware keys, or the app window doesn't reach that area (e.g., bottom split window).<br>
     *         - `null`: WindowInsets not yet received (called too early).<br><br>
     *         `DisplayInfoBarInsets(top, bottom, left, right)` 또는 사용 불가능한 경우 null을 반환합니다.<br>
     *         - `(0, 0, 0, 0)`: 제스처 모드, 하드웨어 키, 또는 앱 윈도우가 해당 영역에 닿지 않는 경우 (예: 분할 하단창).<br>
     *         - `null`: WindowInsets를 아직 받지 못한 시점 (너무 이른 호출).<br>
     */
    public fun getNavigationBarStableInsets(activity: Activity? = null): DisplayInfoBarInsets? = tryCatchSystemManager(null) {
        checkSdkVersion(
            Build.VERSION_CODES.R,
            positiveWork = {
                val nav = getCurrentWindowMetricsSdkR()
                    .windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars())
                DisplayInfoBarInsets(
                    top = nav.top,
                    bottom = nav.bottom,
                    left = nav.left,
                    right = nav.right
                )
            },
            negativeWork = {
                activity?.let {
                    val rootInsets = it.window.decorView.rootWindowInsets
                    if (rootInsets == null) {
                        null
                    } else {
                        @Suppress("DEPRECATION")
                        // Pre-R은 top이 사실상 statusbar 영역인 경우가 대부분이라 nav.top은 0으로 두는 편이 안전함
                        DisplayInfoBarInsets(
                            top = 0,
                            bottom = rootInsets.stableInsetBottom,
                            left = rootInsets.stableInsetLeft,
                            right = rootInsets.stableInsetRight,
                        )
                    }
                }
            },
        )
    }

    /**
     * Returns the status bar stable insets.<br><br>
     * 상태 바의 stable insets를 반환합니다.<br>
     *
     * **What are insets / Insets란:**<br>
     * Insets represent the space occupied by system UI elements from each screen edge.
     * For status bar at the top, if it's 50px tall, top inset is 50.
     * See [getNavigationBarStableInsets] for more details about insets concept.<br><br>
     * Insets는 각 화면 가장자리로부터 시스템 UI 요소가 차지하는 공간을 나타냅니다.
     * 상단 상태 바가 50px 높이면, top inset은 50입니다.
     * insets 개념에 대한 자세한 내용은 [getNavigationBarStableInsets]을 참조하세요.<br>
     *
     * **API version differences / API 버전별 차이:**<br>
     * - **API 30+ (R)**: Uses `WindowInsets.Type.statusBars()` to get all directional insets.
     *   In practice, only `top` has a meaningful value (status bar height), while `bottom`, `left`, `right` are typically 0.<br>
     * - **API 28-29**: Uses `rootWindowInsets.stableInsetTop` for status bar height.
     *   Only `top` is set; `bottom`, `left`, `right` are explicitly 0 since status bar only appears at the top.<br><br>
     * - **API 30+ (R)**: `WindowInsets.Type.statusBars()`를 사용하여 모든 방향의 insets를 가져옵니다.
     *   실제로는 `top`만 의미 있는 값(상태 바 높이)을 가지며, `bottom`, `left`, `right`는 일반적으로 0입니다.<br>
     * - **API 28-29**: `rootWindowInsets.stableInsetTop`을 사용하여 상태 바 높이를 가져옵니다.
     *   `top`만 설정되고 `bottom`, `left`, `right`는 명시적으로 0입니다. 상태 바는 상단에만 나타나기 때문입니다.<br>
     *
     * @param activity Activity instance required for API 28-29 to access `window.decorView.rootWindowInsets` for app window-based insets.
     *                 For API 30+, this parameter is ignored as `currentWindowMetrics.windowInsets` is used instead.
     *                 Pass null if Activity is unavailable (returns null for API 28-29, valid insets for API 30+).<br><br>
     *                 API 28-29에서 앱 윈도우 기준 insets를 얻기 위해 `window.decorView.rootWindowInsets` 접근에 필요한 Activity 인스턴스입니다.
     *                 API 30+에서는 `currentWindowMetrics.windowInsets`를 사용하므로 이 파라미터는 무시됩니다.
     *                 Activity를 사용할 수 없는 경우 null을 전달하면 API 28-29에서는 null을 반환하고, API 30+에서는 정상적인 insets를 반환합니다.<br>
     * @return `DisplayInfoBarInsets(top, bottom, left, right)` or null if unavailable.<br>
     *         - `null`: WindowInsets not yet received (called too early).<br><br>
     *         `DisplayInfoBarInsets(top, bottom, left, right)` 또는 사용 불가능한 경우 null을 반환합니다.<br>
     *         - `null`: WindowInsets를 아직 받지 못한 시점 (너무 이른 호출).<br>
     */
    public fun getStatusBarStableInsets(activity: Activity? = null): DisplayInfoBarInsets? = tryCatchSystemManager(null) {
        checkSdkVersion(
            Build.VERSION_CODES.R,
            positiveWork = {
                val s = getCurrentWindowMetricsSdkR()
                    .windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.statusBars())

                // DisplayInfoBarInsets는 (top,bottom,left,right) 순서라서 named arg로 안전하게 넣기
                DisplayInfoBarInsets(
                    top = s.top,
                    bottom = s.bottom,
                    left = s.left,
                    right = s.right,
                )
            },
            negativeWork = {
                // API 28~29: rootWindowInsets 기반 stable inset 사용 (앱 윈도우 기준 유지)
                activity?.let {
                    val root = it.window.decorView.rootWindowInsets ?: return@checkSdkVersion null
                    @Suppress("DEPRECATION")
                    DisplayInfoBarInsets(
                        top = root.stableInsetTop,
                        bottom = 0,
                        left = 0,
                        right = 0,
                    )
                }
            },
        )
    }
}
