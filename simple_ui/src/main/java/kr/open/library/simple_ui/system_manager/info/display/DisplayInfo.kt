package kr.open.library.simple_ui.system_manager.info.display

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowMetrics
import androidx.annotation.RequiresApi
import kr.open.library.simple_ui.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.system_manager.extensions.getWindowManager

/**
 * This class provides information about the display of an Android device.
 * DisplayInfo 클래스는 Android 기기의 디스플레이 정보를 제공.
 *
 * Screen Size,
 * Full Screen Size,
 * Status Bar Size,
 * Navigation Bar Size,
 *
 * @param context The application context.
 * @param context 애플리케이션 컨텍스트.
 */
public open class DisplayInfo(context: Context) : BaseSystemService(context, null) {


    public val windowManager: WindowManager by lazy { context.getWindowManager() }


    @RequiresApi(Build.VERSION_CODES.R)
    protected open fun getCurrentWindowMetricsSdkR(): WindowMetrics =
        windowManager.currentWindowMetrics

    protected fun getFullScreenSizeSdkNormal(): Point {
        val metrics = DisplayMetrics().apply {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(this)
        }
        return Point(metrics.widthPixels, metrics.heightPixels)
    }

    /**
     * Returns the full screen size(Pixel).
     * 전체 화면 크기를 반환.
     *
     * @return  The full screen size (width, height).
     * @return 전체 화면 크기 (너비, 높이)
     */
    public fun getFullScreenSize(): Point = checkSdkVersion(Build.VERSION_CODES.R,
        positiveWork = { with(getCurrentWindowMetricsSdkR().bounds) { Point(width(), height()) } },
        negativeWork = { getFullScreenSizeSdkNormal() }
    )

    /**
     * Returns the total display width in pixels.
     * 전체 화면의 가로 픽셀 크기를 반환합니다.
     */
    public fun getFullScreenWidth(): Int = getFullScreenSize().x

    /**
     * Returns the total display height in pixels.
     * 전체 화면의 세로 픽셀 크기를 반환합니다.
     */
    public fun getFullScreenHeight(): Int = getFullScreenSize().y


    /**
     * Returns the status bar bounding box as `Point(width, height)`.
     * - Width uses the current content width (`screenWidth`), 실제 상태바 뷰 폭과는 다를 수 있습니다.
     * - Height 는 WindowInsets(R+) 또는 legacy `status_bar_height` dimen 으로 계산됩니다.
     *
     * 가로/세로 회전이나 네비게이션 위치 변화에도 실제 상태바가 차지하는 뷰 영역을 그대로 참고할 수 있습니다.
     * 단, 일부 제조사/커스터마이징에서는 상태바가 화면 전체 폭을 덮는 구조라 UI 배치 폭과 다를 수 있으니 참고용으로만 사용하세요.
     */
    @SuppressLint("InternalInsetResource")
    public open fun getStatusBarSize(): Point = checkSdkVersion(Build.VERSION_CODES.R,
        positiveWork = {
            val height =
                getCurrentWindowMetricsSdkR().windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.statusBars()).top
            val width = getFullScreenWidth()
            return Point(width, height)
        },
        negativeWork = {
            val height = context.resources.getIdentifier("status_bar_height", "dimen", "android")
                .takeIf { it > 0 }?.let { context.resources.getDimensionPixelSize(it) }
                ?: throw Resources.NotFoundException("Cannot find status bar height. Try getStatusBarSize(activity: Activity).")

            val width = getFullScreenWidth()
            return Point(width, height)
        }
    )

    /**
     * Returns the status bar height(Pixel).
     * 상태 표시줄 높이를 반환.
     *
     * @return The status bar height.
     * @return 상태 표시줄 높이.
     */
    public fun getStatusBarHeight(): Result<Int> = runCatching { getStatusBarSize().y }

    /**
     * Returns the status bar width wrapped in Result.
     * 상태바 가로 길이를 Result 로 반환합니다.
     */
    public fun getStatusBarWidth(): Result<Int> = runCatching { getStatusBarSize().x }

    @RequiresApi(Build.VERSION_CODES.R)
    protected fun getNavigationBarSizeSdkR(): Point {
        val windowMetrics = getCurrentWindowMetricsSdkR()
        val bounds = windowMetrics.bounds
        val navInsets =
            windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars())

        val size = when {
            navInsets.bottom > 0 -> Point(bounds.width(), navInsets.bottom)
            navInsets.top > 0 -> Point(bounds.width(), navInsets.top)
            navInsets.left > 0 -> Point(navInsets.left, bounds.height())
            navInsets.right > 0 -> Point(navInsets.right, bounds.height())
            else -> Point(0, 0)
        }

        if (size.x == 0 && size.y == 0) {
            throw Resources.NotFoundException("Cannot determine navigation bar size. Check navigation bar resources.")
        }
        return size
    }

    @SuppressLint("InternalInsetResource")
    private fun getNavigationBarSizeSdkNormal(): Point {
        val resources = context.resources
        fun Resources.dimensionOrNull(name: String): Int? {
            val id = getIdentifier(name, "dimen", "android")
            return id.takeIf { it > 0 }?.let { getDimensionPixelSize(it) }?.takeIf { it > 0 }
        }

        val portraitHeight = resources.dimensionOrNull("navigation_bar_height")
        val landscapeHeight = resources.dimensionOrNull("navigation_bar_height_landscape")
        val sideWidth = resources.dimensionOrNull("navigation_bar_width")

        if (portraitHeight == null && landscapeHeight == null && sideWidth == null) {
            throw Resources.NotFoundException("navigation_bar_* resources missing or zero")
        }

        val screenSize = getFullScreenSizeSdkNormal()
        val orientation = resources.configuration.orientation

        return when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                val height = portraitHeight ?: landscapeHeight
                height?.let { Point(screenSize.x, it) }
                    ?: Point(0, 0) // 실제로 내비가 없는 제스처 모드일 수 있음
            }

            Configuration.ORIENTATION_LANDSCAPE -> {
                val prefersSide =
                    sideWidth != null && (landscapeHeight == null || sideWidth >= landscapeHeight)
                if (prefersSide && sideWidth != null) {
                    Point(sideWidth, screenSize.y)
                } else {
                    val height = landscapeHeight ?: portraitHeight
                    height?.let { Point(screenSize.x, it) } ?: Point(0, 0)
                }
            }

            else -> {
                val vertical = portraitHeight ?: landscapeHeight
                when {
                    vertical != null -> Point(screenSize.x, vertical)
                    sideWidth != null -> Point(sideWidth, screenSize.y)
                    else -> Point(0, 0)
                }
            }
        }
    }


    /**
     * Returns the navigation bar size (thickness).
     * This returns the largest inset dimension where the navigation bar is located.
     * For bottom navigation bars, this is the height.
     * For side navigation bars (tablets/foldables), this is the width.
     *
     * 탐색 표시줄 크기(두께)를 반환.
     * 네비게이션 바가 위치한 방향의 가장 큰 inset 크기를 반환합니다.
     * 하단 네비게이션 바의 경우 높이, 측면 네비게이션 바의 경우 너비를 반환합니다.
     *
     * @return The navigation bar size (thickness in pixels).
     * @return 탐색 표시줄 크기 (픽셀 단위 두께).
     */
    /**
     * Returns the navigation bar bounding size (width/height) as a Point.
     * 네비게이션 바의 가로/세로 크기를 Point 로 반환합니다.
     */
    @SuppressLint("InternalInsetResource")
    public fun getNavigationBarSize(): Point = checkSdkVersion(Build.VERSION_CODES.R,
        positiveWork = { getNavigationBarSizeSdkR() },
        negativeWork = { getNavigationBarSizeSdkNormal() }
    )

    /**
     * Returns the navigation bar width component.
     * 네비게이션 바의 가로 크기를 반환합니다.
     */
    public fun getNavigationBarWidth(): Int = getNavigationBarSize().x

    /**
     * Returns the navigation bar height component.
     * 네비게이션 바의 세로 크기를 반환합니다.
     */
    public fun getNavigationBarHeight(): Int = getNavigationBarSize().y

    /**
     * Returns the available screen width after excluding navigation insets.
     * 네비게이션 영역을 제외한 실제 사용 가능한 가로 픽셀을 반환합니다.
     */
    public fun getScreenWidth(): Int {
        val navSize = getNavigationBarSize()
        val fullWidth = getFullScreenWidth()
        return if (navSize.x < fullWidth) { fullWidth - navSize.x }
        else { fullWidth }
    }

    /**
     * Returns the available screen height after excluding system bars.
     * 상태바/네비게이션 바를 제외한 실제 사용 가능한 세로 픽셀을 반환합니다.
     */
    public fun getScreenHeight(): Int {
        val navSize = getNavigationBarSize()
        val fullHeight = getFullScreenHeight()
        val statusBarHeight = getStatusBarHeight().getOrElse { 0 }
        return if (navSize.y < fullHeight) { fullHeight - navSize.y - statusBarHeight }
        else { fullHeight - statusBarHeight }
    }

    /**
     * Returns the usable screen size as Point(width, height).
     * 사용 가능한 화면 크기를 Point 로 반환합니다.
     */
    public fun getScreenSize() = Point(getScreenWidth(), getScreenHeight())

    /**
     * Indicates whether the navigation bar is hidden.
     * 네비게이션 바가 숨겨졌는지 여부를 반환합니다.
     */
    public fun isNavigationBarHided(): Boolean = getNavigationBarHeight() == 0

    /**
     * Indicates whether the status bar is hidden.
     * 상태바가 숨겨졌는지 여부를 반환합니다.
     */
    public fun isStatusBarHided(): Boolean = getStatusBarHeight().getOrElse {
        Logx.e(it)
        -1
    } == 0

    /**
     * Indicates whether the window is occupying the full screen (no insets).
     * 현재 창이 인셋 없이 전체 화면을 사용 중인지 여부를 반환합니다.
     */
    public fun isFullScreen(): Boolean = getFullScreenHeight() == getScreenHeight() && getFullScreenWidth() == getScreenWidth()
}
