package kr.open.library.simple_ui.xml.system_manager.controller.systembar

import android.graphics.Rect
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.xml.system_manager.controller.systembar.internal.helper.NavigationBarHelper
import kr.open.library.simple_ui.xml.system_manager.controller.systembar.internal.helper.StatusBarHelper

/**
 * Controller for managing system bars (StatusBar and NavigationBar) independently.<br>
 * Provides comprehensive control over system bar appearance, visibility, and size measurement.<br><br>
 * 시스템 바(StatusBar와 NavigationBar)를 독립적으로 관리하는 컨트롤러입니다.<br>
 * 시스템 바의 외관, 가시성 및 크기 측정에 대한 종합적인 제어를 제공합니다.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Works independently with only Window object (usable in Activity, Dialog, FloatingWindow, etc.)<br>
 * - Handles API 35+ edge-to-edge breaking changes with custom View overlay approach<br>
 * - Provides both stable (system-defined) and visible (current) size measurements<br>
 * - Simplifies system bar control without requiring Activity inheritance<br><br>
 * - Window 객체만으로 독립적으로 작동 (Activity, Dialog, FloatingWindow 등에서 사용 가능)<br>
 * - 커스텀 View 오버레이 방식으로 API 35+ edge-to-edge breaking change 처리<br>
 * - Stable(시스템 정의)과 Visible(현재) 크기 측정을 모두 제공<br>
 * - Activity 상속 없이 시스템 바 제어 간소화<br>
 *
 * **Design decisions / 설계 결정 이유:**<br>
 * - **Window-only dependency**: No Activity requirement enables broader use cases<br>
 * - **Rect return type**: Returns window coordinates relative to decorView (left, top, right, bottom)<br>
 * - **Stable vs Visible**: Stable returns system-defined size, Visible returns actual current size<br>
 * - **View caching**: Reuses background views for API 35+ to prevent memory churn<br>
 * - **No permissions required**: System bar control doesn't require runtime permissions<br><br>
 * - **Window만 의존**: Activity 요구사항이 없어 더 광범위한 사용 사례 가능<br>
 * - **Rect 반환 타입**: decorView 기준 윈도우 좌표 반환 (left, top, right, bottom)<br>
 * - **Stable vs Visible**: Stable은 시스템 정의 크기, Visible은 실제 현재 크기 반환<br>
 * - **View 캐싱**: API 35+에서 배경 뷰를 재사용하여 메모리 낭비 방지<br>
 * - **권한 불필요**: 시스템 바 제어는 런타임 권한이 필요 없음<br>
 * - **null vs Rect() return policy**: null means WindowInsets not ready (early initialization before view measurement),<br>
 *   Rect() means system bar is hidden or doesn't exist on device<br>
 * - **Stable vs Visible difference**: Stable is system-defined size, constant even when hidden (use for layout calculations),<br>
 *   Visible is current actual size, becomes 0 when hidden (use for padding/margin)<br>
 * - **Reset visibility policy**: resetStatusBarColor()/resetNavigationBarColor() restore visibility by default (restoreVisibility=true),<br>
 *   set restoreVisibility=false to keep current hidden state (e.g., fullscreen video player)<br>
 * - **Edge-to-edge mode policy**: Disabled by default (decorFitsSystemWindows=true), content starts below system bars.<br>
 *   Call setEdgeToEdgeMode(true) to enable edge-to-edge layout (content extends behind system bars).<br>
 *   User must manually handle WindowInsets when edge-to-edge is enabled<br><br>
 * - **null vs Rect() 반환 정책**: null은 WindowInsets 미준비 상태(뷰 측정 전 초기화 단계),<br>
 *   Rect()는 시스템 바가 숨겨졌거나 기기에 존재하지 않는 경우<br>
 * - **Stable vs Visible 차이**: Stable은 시스템 정의 크기로 숨겨져도 일정(레이아웃 계산용),<br>
 *   Visible은 현재 실제 크기로 숨기면 0이 됨(패딩/마진용)<br>
 * - **Reset 가시성 정책**: resetStatusBarColor()/resetNavigationBarColor()는 기본적으로 가시성 복원(restoreVisibility=true),<br>
 *   현재 숨김 상태 유지 필요 시 restoreVisibility=false 사용(예: 전체화면 비디오 플레이어)<br>
 * - **Edge-to-edge 모드 정책**: 기본적으로 비활성화(decorFitsSystemWindows=true), 컨텐츠는 시스템 바 아래부터 시작.<br>
 *   setEdgeToEdgeMode(true) 호출 시 edge-to-edge 레이아웃 활성화(컨텐츠가 시스템 바 뒤까지 확장).<br>
 *   edge-to-edge 활성화 시 사용자가 WindowInsets를 수동으로 처리해야 함<br>
 *
 * **Usage / 사용법:**<br>
 * ```kotlin
 * val controller = SystemBarController(window)
 *
 * // Get system bar sizes
 * val statusBarStable = controller.getStatusBarStableRect()
 * val statusBarVisible = controller.getStatusBarVisibleRect()
 *
 * // Set colors
 * controller.setStatusBarColor(Color.RED, isDarkIcon = false)
 * controller.setNavigationBarColor(Color.BLUE, isDarkIcon = true)
 *
 * // Control visibility
 * controller.setStatusBarVisible() // show
 * controller.setNavigationBarGone() // hide
 *
 * // Reset to initial state
 * controller.resetStatusBarColor()
 * controller.resetNavigationBarColor()
 *
 * // Clean up
 * controller.onDestroy()
 * ```<br><br>
 *
 * @param window The Window object to control system bars.<br><br>
 *               시스템 바를 제어할 Window 객체.<br>
 */
public class SystemBarController(
    private val window: Window,
) : BaseSystemService(window.context, null) {
    /**
     * Helper instance for managing StatusBar operations.<br>
     * Lazily initialized when first accessed.<br><br>
     * StatusBar 작업을 관리하는 헬퍼 인스턴스입니다.<br>
     * 최초 접근 시 지연 초기화됩니다.<br>
     */
    private val statusBarHelper: StatusBarHelper by lazy { StatusBarHelper(window.decorView) }

    /**
     * Helper instance for managing NavigationBar operations.<br>
     * Lazily initialized when first accessed.<br><br>
     * NavigationBar 작업을 관리하는 헬퍼 인스턴스입니다.<br>
     * 최초 접근 시 지연 초기화됩니다.<br>
     */
    private val navigationBarHelper: NavigationBarHelper by lazy { NavigationBarHelper(window.decorView) }

    /**
     * Tracks whether edge-to-edge mode is currently enabled.<br><br>
     * edge-to-edge 모드가 현재 활성화되어 있는지 추적합니다.<br>
     */
    private var isEdgeToEdge: Boolean = false

    /**
     * Retrieves the root WindowInsetsCompat from the window's decorView.<br>
     * Returns null if WindowInsets are not yet available (early initialization).<br><br>
     * 윈도우의 decorView에서 루트 WindowInsetsCompat을 가져옵니다.<br>
     * WindowInsets가 아직 사용 불가능한 경우(초기화 초기 단계) null을 반환합니다.<br>
     *
     * @return WindowInsetsCompat if available, null otherwise.<br><br>
     *         사용 가능한 경우 WindowInsetsCompat, 아니면 null.<br>
     */
    private fun getRootWindowInsetsCompat(): WindowInsetsCompat? = tryCatchSystemManager(null) {
        ViewCompat.getRootWindowInsets(window.decorView)
    }

    /**
     * Gets the WindowInsetsController for managing system bar visibility and appearance.<br>
     * Configures BEHAVIOR_DEFAULT for smooth animations when showing/hiding system bars.<br><br>
     * 시스템 바 가시성 및 모양을 관리하기 위한 WindowInsetsController를 가져옵니다.<br>
     * 시스템 바 표시/숨김 시 부드러운 애니메이션을 위해 BEHAVIOR_DEFAULT를 설정합니다.<br>
     *
     * @return WindowInsetsControllerCompat instance.<br><br>
     *         WindowInsetsControllerCompat 인스턴스.<br>
     */
    private fun getWindowInsetController() = WindowCompat.getInsetsController(window, window.decorView).apply {
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    }

    /**
     * Returns the window coordinates of the currently visible StatusBar area.<br>
     * Uses WindowInsetsCompat with visibility detection. Returns empty Rect if StatusBar is hidden.<br><br>
     * 현재 보이는 StatusBar 영역의 윈도우 좌표를 반환합니다.<br>
     * WindowInsetsCompat과 가시성 감지를 사용합니다. StatusBar가 숨겨진 경우 빈 Rect를 반환합니다.<br>
     *
     * @return Rect with window coordinates relative to decorView (left=0, top=0, right=decorViewWidth, bottom=statusBarHeight),
     *         or null if WindowInsets not ready (early initialization before view measurement).<br><br>
     *         decorView 기준 윈도우 좌표를 가진 Rect (left=0, top=0, right=decorView너비, bottom=상태바높이),
     *         WindowInsets 미준비 시 (뷰 측정 전 초기화 단계) null.<br>
     */
    public fun getStatusBarVisibleRect(): Rect? = getRootWindowInsetsCompat()?.let { statusBarHelper.getStatusBarVisibleRect(it) }

    /**
     * Returns the window coordinates of the system-defined (stable) StatusBar area.<br>
     * This value remains constant even if the StatusBar is hidden.<br><br>
     * 시스템 정의(stable) StatusBar 영역의 윈도우 좌표를 반환합니다.<br>
     * 이 값은 StatusBar가 숨겨진 경우에도 일정하게 유지됩니다.<br>
     *
     * @return Rect with window coordinates relative to decorView (left=0, top=0, right=decorViewWidth, bottom=statusBarHeight),
     *         or null if WindowInsets not ready (early initialization before view measurement).<br><br>
     *         decorView 기준 윈도우 좌표를 가진 Rect (left=0, top=0, right=decorView너비, bottom=상태바높이),
     *         WindowInsets 미준비 시 (뷰 측정 전 초기화 단계) null.<br>
     */
    public fun getStatusBarStableRect(): Rect? = getRootWindowInsetsCompat()?.let { statusBarHelper.getStatusBarStableRect(it) }

    /**
     * Returns the window coordinates of the currently visible NavigationBar area.<br>
     * Uses WindowInsetsCompat with visibility detection. Returns empty Rect if NavigationBar is hidden.<br>
     * Supports bottom, left, and right NavigationBar positions.<br><br>
     * 현재 보이는 NavigationBar 영역의 윈도우 좌표를 반환합니다.<br>
     * WindowInsetsCompat과 가시성 감지를 사용합니다. NavigationBar가 숨겨진 경우 빈 Rect를 반환합니다.<br>
     * 하단, 왼쪽, 오른쪽 NavigationBar 위치를 지원합니다.<br>
     *
     * @return Rect with window coordinates relative to decorView. Bottom: (0, decorViewHeight-navHeight, decorViewWidth, decorViewHeight),
     *         Left: (0, 0, navWidth, decorViewHeight), Right: (decorViewWidth-navWidth, 0, decorViewWidth, decorViewHeight),
     *         or null if WindowInsets not ready (early initialization before view measurement).<br><br>
     *         decorView 기준 윈도우 좌표를 가진 Rect. 하단: (0, decorView높이-네비높이, decorView너비, decorView높이),
     *         왼쪽: (0, 0, 네비너비, decorView높이), 오른쪽: (decorView너비-네비너비, 0, decorView너비, decorView높이),
     *         WindowInsets 미준비 시 (뷰 측정 전 초기화 단계) null.<br>
     */
    public fun getNavigationBarVisibleRect(): Rect? =
        getRootWindowInsetsCompat()?.let { navigationBarHelper.getNavigationBarVisibleRect(it) }

    /**
     * Returns the window coordinates of the system-defined (stable) NavigationBar area.<br>
     * This value remains constant even if the NavigationBar is hidden.<br>
     * Supports bottom, left, and right NavigationBar positions.<br><br>
     * 시스템 정의(stable) NavigationBar 영역의 윈도우 좌표를 반환합니다.<br>
     * 이 값은 NavigationBar가 숨겨진 경우에도 일정하게 유지됩니다.<br>
     * 하단, 왼쪽, 오른쪽 NavigationBar 위치를 지원합니다.<br>
     *
     * @return Rect with window coordinates relative to decorView. Bottom: (0, decorViewHeight-navHeight, decorViewWidth, decorViewHeight),
     *         Left: (0, 0, navWidth, decorViewHeight), Right: (decorViewWidth-navWidth, 0, decorViewWidth, decorViewHeight),
     *         or null if WindowInsets not ready (early initialization before view measurement).<br><br>
     *         decorView 기준 윈도우 좌표를 가진 Rect. 하단: (0, decorView높이-네비높이, decorView너비, decorView높이),
     *         왼쪽: (0, 0, 네비너비, decorView높이), 오른쪽: (decorView너비-네비너비, 0, decorView너비, decorView높이),
     *         WindowInsets 미준비 시 (뷰 측정 전 초기화 단계) null.<br>
     */
    public fun getNavigationBarStableRect(): Rect? = getRootWindowInsetsCompat()?.let { navigationBarHelper.getNavigationBarStableRect(it) }

    /**
     * Sets the StatusBar color.<br><br>
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
     * @param isDarkIcon True for dark icons (light mode), false for light icons (dark mode).<br><br>
     *                   어두운 아이콘인 경우 true (라이트 모드), 밝은 아이콘인 경우 false (다크 모드).<br>
     */
    public fun setStatusBarColor(@ColorInt color: Int, isDarkIcon: Boolean = false) {
        checkSdkVersion(
            Build.VERSION_CODES.VANILLA_ICE_CREAM,
            positiveWork = {
                val insets = getRootWindowInsetsCompat() ?: WindowInsetsCompat.CONSUMED
                statusBarHelper.setStatusBarColorSdk35(insets, window.context, color)
            },
            negativeWork = {
                @Suppress("DEPRECATION")
                window.statusBarColor = color
            },
        )
        setStatusBarDarkIcon(isDarkIcon)
    }

    /**
     * Sets the NavigationBar color.<br><br>
     * 네비게이션 바 색상을 설정합니다.<br>
     *
     * ## API version behavior / API 버전별 동작 방식<br>
     *
     * ### API 28-34 (Android 9.0 ~ Android 14)<br>
     * - Sets NavigationBar color directly via `window.navigationBarColor`<br>
     * - `window.navigationBarColor`를 직접 설정하여 NavigationBar 색상 변경<br>
     *
     * ### API 35+ (Android 15+)<br>
     * - NavigationBar is always transparent by system<br>
     * - Uses custom background View to simulate color<br>
     * - Supports bottom, left, and right positions (tablets, foldables)<br>
     * - NavigationBar가 시스템에 의해 항상 투명하게 강제됨<br>
     * - 색상을 시뮬레이션하기 위해 커스텀 배경 뷰 사용<br>
     * - 하단, 왼쪽, 오른쪽 위치 지원 (태블릿, 폴더블)<br>
     *
     * @param color The color to set for NavigationBar.<br><br>
     *              NavigationBar에 설정할 색상.<br>
     *
     * @param isDarkIcon True for dark icons (light mode), false for light icons (dark mode).<br><br>
     *                   어두운 아이콘인 경우 true (라이트 모드), 밝은 아이콘인 경우 false (다크 모드).<br>
     */
    public fun setNavigationBarColor(@ColorInt color: Int, isDarkIcon: Boolean = false) {
        checkSdkVersion(
            Build.VERSION_CODES.VANILLA_ICE_CREAM,
            positiveWork = {
                val insets = getRootWindowInsetsCompat() ?: WindowInsetsCompat.CONSUMED
                navigationBarHelper.setNavigationBarColorSdk35(insets, window.context, color)
            },
            negativeWork = {
                @Suppress("DEPRECATION")
                window.navigationBarColor = color
            },
        )
        setNavigationBarDarkIcon(isDarkIcon)
    }

    /**
     * Sets the StatusBar icon appearance mode (Dark or Light).<br>
     * It may not work on certain devices.<br><br>
     * StatusBar 아이콘 모드를 설정합니다 (Dark 또는 Light).<br>
     * 특정 장치에서는 작동하지 않을 수 있습니다.<br>
     *
     * @param isDarkIcon true for dark icons, false for light icons.<br><br>
     *               어두운 아이콘은 true, 밝은 아이콘은 false.<br>
     */
    public fun setStatusBarDarkIcon(isDarkIcon: Boolean) {
        getWindowInsetController().apply { isAppearanceLightStatusBars = isDarkIcon }
    }

    /**
     * Sets the NavigationBar icon appearance mode (Dark or Light).<br>
     * It may not work on certain devices.<br><br>
     * NavigationBar 아이콘 모드를 설정합니다 (Dark 또는 Light).<br>
     * 특정 장치에서는 작동하지 않을 수 있습니다.<br>
     *
     * @param isDarkIcon true for dark icons, false for light icons.<br><br>
     *               어두운 아이콘은 true, 밝은 아이콘은 false.<br>
     */
    public fun setNavigationBarDarkIcon(isDarkIcon: Boolean) {
        getWindowInsetController().apply { isAppearanceLightNavigationBars = isDarkIcon }
    }

    /**
     * Shows the StatusBar.<br>
     * Uses WindowInsetsController on API 30+ and FLAG_FULLSCREEN on older versions.<br><br>
     * StatusBar를 표시합니다.<br>
     * API 30+에서는 WindowInsetsController를 사용하고 이전 버전에서는 FLAG_FULLSCREEN을 사용합니다.<br>
     *
     * **Important / 주의사항:**<br>
     * - Rapidly toggling visibility (calling setStatusBarVisible()/setStatusBarGone() multiple times within 1500ms)
     *   may cause screen flickering on certain Android versions (e.g., Android 12).<br>
     * - Recommended to debounce calls or add delay between visibility changes if flickering occurs.<br><br>
     * - 짧은 시간(1500ms 이내)에 가시성을 여러 번 토글하는 경우 (setStatusBarVisible()/setStatusBarGone() 반복 호출)
     *   특정 Android 버전(예: Android 12)에서 화면 깜빡임이 발생할 수 있습니다.<br>
     * - 깜빡임이 발생하는 경우 호출을 디바운스하거나 가시성 변경 사이에 지연을 추가하는 것을 권장합니다.<br>
     */
    public fun setStatusBarVisible() {
        window.apply {
            checkSdkVersion(
                Build.VERSION_CODES.R,
                positiveWork = { getWindowInsetController().show(WindowInsetsCompat.Type.statusBars()) },
                negativeWork = {
                    @Suppress("DEPRECATION")
                    clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                },
            )
        }
    }

    /**
     * Hides the StatusBar.<br>
     * Uses WindowInsetsController on API 30+ and FLAG_FULLSCREEN on older versions.<br><br>
     * StatusBar를 숨깁니다.<br>
     * API 30+에서는 WindowInsetsController를 사용하고 이전 버전에서는 FLAG_FULLSCREEN을 사용합니다.<br>
     *
     * **Important / 주의사항:**<br>
     * - Rapidly toggling visibility (calling setStatusBarVisible()/setStatusBarGone() multiple times within 1500ms)
     *   may cause screen flickering on certain Android versions (e.g., Android 12).<br>
     * - Recommended to debounce calls or add delay between visibility changes if flickering occurs.<br><br>
     * - 짧은 시간(1500ms 이내)에 가시성을 여러 번 토글하는 경우 (setStatusBarVisible()/setStatusBarGone() 반복 호출)
     *   특정 Android 버전(예: Android 12)에서 화면 깜빡임이 발생할 수 있습니다.<br>
     * - 깜빡임이 발생하는 경우 호출을 디바운스하거나 가시성 변경 사이에 지연을 추가하는 것을 권장합니다.<br>
     */
    public fun setStatusBarGone() {
        window.apply {
            checkSdkVersion(
                Build.VERSION_CODES.R,
                positiveWork = { getWindowInsetController().hide(WindowInsetsCompat.Type.statusBars()) },
                negativeWork = {
                    @Suppress("DEPRECATION")
                    addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                },
            )
        }
    }

    /**
     * Shows the NavigationBar.<br>
     * Uses WindowInsetsController on API 30+ and SYSTEM_UI_FLAG on older versions.<br><br>
     * NavigationBar를 표시합니다.<br>
     * API 30+에서는 WindowInsetsController를 사용하고 이전 버전에서는 SYSTEM_UI_FLAG를 사용합니다.<br>
     */
    public fun setNavigationBarVisible() {
        window.apply {
            checkSdkVersion(
                Build.VERSION_CODES.R,
                positiveWork = { getWindowInsetController().show(WindowInsetsCompat.Type.navigationBars()) },
                negativeWork = {
                    @Suppress("DEPRECATION")
                    decorView.systemUiVisibility = decorView.systemUiVisibility and
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv() and
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv()
                },
            )
        }
    }

    /**
     * Hides the NavigationBar.<br>
     * Uses WindowInsetsController on API 30+ and SYSTEM_UI_FLAG on older versions.<br><br>
     * NavigationBar를 숨깁니다.<br>
     * API 30+에서는 WindowInsetsController를 사용하고 이전 버전에서는 SYSTEM_UI_FLAG를 사용합니다.<br>
     */
    public fun setNavigationBarGone() {
        window.apply {
            checkSdkVersion(
                Build.VERSION_CODES.R,
                positiveWork = { getWindowInsetController().hide(WindowInsetsCompat.Type.navigationBars()) },
                negativeWork = {
                    @Suppress("DEPRECATION")
                    decorView.systemUiVisibility = decorView.systemUiVisibility or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                },
            )
        }
    }

    /**
     * Resets the StatusBar to its initial state.<br>
     * Removes custom background views on API 35+ and restores theme default color on older versions.<br><br>
     * StatusBar를 초기 상태로 초기화합니다.<br>
     * API 35+에서는 커스텀 배경 뷰를 제거하고 이전 버전에서는 테마 기본 색상을 복원합니다.<br>
     *
     * @param restoreVisibility If true, also calls setStatusBarVisible() to ensure StatusBar is visible after reset.
     *                          Set to false if you want to keep current visibility state (e.g., when StatusBar is intentionally hidden).<br><br>
     *                          true인 경우 리셋 후 StatusBar가 보이도록 setStatusBarVisible()도 호출합니다.
     *                          현재 가시성 상태를 유지하려면 false로 설정하세요 (예: StatusBar를 의도적으로 숨긴 경우).<br>
     */
    public fun resetStatusBarColor(restoreVisibility: Boolean = true) {
        window.apply {
            checkSdkVersion(
                Build.VERSION_CODES.VANILLA_ICE_CREAM,
                positiveWork = { cleanupStatusBarOverlay() },
                negativeWork = {
                    val typedValue = TypedValue()
                    @Suppress("DEPRECATION")
                    context.theme.resolveAttribute(android.R.attr.statusBarColor, typedValue, true)
                    @Suppress("DEPRECATION")
                    statusBarColor = typedValue.data
                },
            )
            if (restoreVisibility) setStatusBarVisible()
        }
    }

    /**
     * Resets the NavigationBar to its initial state.<br>
     * Removes custom background views on API 35+ and restores theme default color on older versions.<br><br>
     * 네비게이션 바를 초기 상태로 복원합니다.<br>
     * API 35+에서는 커스텀 배경 뷰를 제거하고 이전 버전에서는 테마 기본 색상을 복원합니다.<br>
     *
     * @param restoreVisibility If true, also calls setNavigationBarVisible() to ensure NavigationBar is visible after reset.
     *                          Set to false if you want to keep current visibility state (e.g., when NavigationBar is intentionally hidden).<br><br>
     *                          true인 경우 리셋 후 NavigationBar가 보이도록 setNavigationBarVisible()도 호출합니다.
     *                          현재 가시성 상태를 유지하려면 false로 설정하세요 (예: NavigationBar를 의도적으로 숨긴 경우).<br>
     */
    public fun resetNavigationBarColor(restoreVisibility: Boolean = true) {
        window.apply {
            checkSdkVersion(
                Build.VERSION_CODES.VANILLA_ICE_CREAM,
                positiveWork = { cleanupNavigationBarOverlay() },
                negativeWork = {
                    val typedValue = TypedValue()
                    @Suppress("DEPRECATION")
                    context.theme.resolveAttribute(android.R.attr.navigationBarColor, typedValue, true)
                    @Suppress("DEPRECATION")
                    navigationBarColor = typedValue.data
                },
            )
            if (restoreVisibility) setNavigationBarVisible()
        }
    }

    /**
     * Enables or disables edge-to-edge mode.<br>
     * When enabled, the layout extends behind system bars (StatusBar and NavigationBar).<br>
     * When disabled, the layout starts below system bars (default behavior).<br><br>
     * edge-to-edge 모드를 활성화하거나 비활성화합니다.<br>
     * 활성화 시 레이아웃이 시스템 바(StatusBar와 NavigationBar) 뒤까지 확장됩니다.<br>
     * 비활성화 시 레이아웃은 시스템 바 아래부터 시작합니다 (기본 동작).<br>
     *
     * **Important / 주의사항:**<br>
     * - When edge-to-edge is enabled, you must manually handle WindowInsets to avoid content being drawn behind system bars.<br>
     * - Use ViewCompat.setOnApplyWindowInsetsListener() or apply padding/margin based on WindowInsets.<br>
     * - When edge-to-edge is disabled, system automatically adds padding for system bars (default Android behavior).<br><br>
     * - edge-to-edge 활성화 시, 컨텐츠가 시스템 바 뒤에 그려지는 것을 방지하기 위해 WindowInsets를 수동으로 처리해야 합니다.<br>
     * - ViewCompat.setOnApplyWindowInsetsListener()를 사용하거나 WindowInsets 기반으로 패딩/마진을 적용하세요.<br>
     * - edge-to-edge 비활성화 시, 시스템이 자동으로 시스템 바에 대한 패딩을 추가합니다 (기본 Android 동작).<br>
     *
     * **Usage / 사용법:**<br>
     * ```kotlin
     * // Enable edge-to-edge mode
     * controller.setEdgeToEdgeMode(true)
     *
     * // Now handle WindowInsets in your layout
     * ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
     *     val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
     *     view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
     *     insets
     * }
     *
     * // Disable edge-to-edge mode (return to default)
     * controller.setEdgeToEdgeMode(false)
     * ```<br><br>
     * ```kotlin
     * // edge-to-edge 모드 활성화
     * controller.setEdgeToEdgeMode(true)
     *
     * // 이제 레이아웃에서 WindowInsets 처리
     * ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
     *     val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
     *     view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
     *     insets
     * }
     *
     * // edge-to-edge 모드 비활성화 (기본값으로 복귀)
     * controller.setEdgeToEdgeMode(false)
     * ```<br>
     *
     * @param enabled true to enable edge-to-edge mode, false to disable (default mode).<br><br>
     *                edge-to-edge 모드를 활성화하려면 true, 비활성화(기본 모드)하려면 false.<br>
     */
    public fun setEdgeToEdgeMode(enabled: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(window, !enabled)
        isEdgeToEdge = enabled
    }

    /**
     * Checks whether edge-to-edge mode is currently enabled.<br><br>
     * edge-to-edge 모드가 현재 활성화되어 있는지 확인합니다.<br>
     *
     * @return true if edge-to-edge mode is enabled, false otherwise.<br><br>
     *         edge-to-edge 모드가 활성화된 경우 true, 그렇지 않으면 false.<br>
     */
    public fun isEdgeToEdgeEnabled(): Boolean = isEdgeToEdge

    /**
     * Cleans up status bar overlay view (API 35+).<br>
     * Removes WindowInsets listener and removes view from decorView.<br><br>
     * 상태바 오버레이 뷰를 정리합니다 (API 35+).<br>
     * WindowInsets 리스너를 제거하고 decorView에서 뷰를 제거합니다.<br>
     */
    private fun cleanupStatusBarOverlay() {
        statusBarHelper.cleanupStatusBarOverlay()
    }

    /**
     * Cleans up navigation bar overlay view (API 35+).<br>
     * Removes WindowInsets listener and removes view from decorView.<br><br>
     * 네비게이션 바 오버레이 뷰를 정리합니다 (API 35+).<br>
     * WindowInsets 리스너를 제거하고 decorView에서 뷰를 제거합니다.<br>
     */
    private fun cleanupNavigationBarOverlay() {
        navigationBarHelper.cleanupNavigationBarOverlay()
    }

    /**
     * Cleans up all resources of SystemBarController.<br>
     * Removes custom background views and clears references.<br><br>
     * SystemBarController의 모든 리소스를 정리합니다.<br>
     * 커스텀 배경 뷰를 제거하고 참조를 해제합니다.<br>
     */
    override fun onDestroy() {
        cleanupStatusBarOverlay()
        cleanupNavigationBarOverlay()
        super.onDestroy()
    }
}
