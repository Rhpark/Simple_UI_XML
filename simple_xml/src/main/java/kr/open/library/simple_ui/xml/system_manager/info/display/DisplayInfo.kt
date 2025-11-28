package kr.open.library.simple_ui.xml.system_manager.info.display

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
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.extensions.getWindowManager

/**
 * Provides display-related metrics such as full screen, status bar, and navigation bar sizes.<br><br>
 * 전체 화면, 상태 바, 내비게이션 바 크기 등 디스플레이 정보를 제공합니다.<br>
 *
 * @param context Application context used to access system services.<br><br>
 *                시스템 서비스를 사용하기 위한 애플리케이션 컨텍스트입니다.<br>
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
     * Returns the full screen size in pixels.<br><br>
     * 전체 화면 크기(픽셀)를 반환합니다.<br>
     *
     * @return `Point(width, height)` representing the full screen size.<br><br>
     *         전체 화면 너비와 높이를 담은 `Point`입니다.<br>
     */
    public fun getFullScreenSize(): Point = checkSdkVersion(Build.VERSION_CODES.R,
        positiveWork = { with(getCurrentWindowMetricsSdkR().bounds) { Point(width(), height()) } },
        negativeWork = { getFullScreenSizeSdkNormal() }
    )

    /**
     * Returns the total display width in pixels.<br><br>
     * 전체 화면의 가로 픽셀 크기를 반환합니다.<br>
     */
    public fun getFullScreenWidth(): Int = getFullScreenSize().x

    /**
     * Returns the total display height in pixels.<br><br>
     * 전체 화면의 세로 픽셀 크기를 반환합니다.<br>
     */
    public fun getFullScreenHeight(): Int = getFullScreenSize().y


    /**
     * Returns the status bar bounding box as `Point(width, height)`; width uses the current screen width.<br><br>
     * 상태 바 크기를 `Point(가로, 세로)`로 반환하며, 가로 값은 현재 화면 너비를 사용합니다.<br>
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
     * Returns the status bar height wrapped in `Result`.<br><br>
     * 상태 표시줄 높이를 `Result`로 반환합니다.<br>
     */
    public fun getStatusBarHeight(): Result<Int> = runCatching { getStatusBarSize().y }

    /**
     * Returns the status bar width wrapped in `Result`.<br><br>
     * 상태 표시줄 너비를 `Result`로 반환합니다.<br>
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
     * Returns the navigation bar bounding size as `Point(width, height)`.<br><br>
     * 내비게이션 바의 가로·세로 크기를 `Point`로 반환합니다.<br>
     */
    @SuppressLint("InternalInsetResource")
    public fun getNavigationBarSize(): Point = checkSdkVersion(Build.VERSION_CODES.R,
        positiveWork = { getNavigationBarSizeSdkR() },
        negativeWork = { getNavigationBarSizeSdkNormal() }
    )

    /**
     * Returns the navigation bar width component.<br><br>
     * 내비게이션 바의 가로 크기를 반환합니다.<br>
     */
    public fun getNavigationBarWidth(): Int = getNavigationBarSize().x

    /**
     * Returns the navigation bar height component.<br><br>
     * 내비게이션 바의 세로 크기를 반환합니다.<br>
     */
    public fun getNavigationBarHeight(): Int = getNavigationBarSize().y

    /**
     * Returns the available screen width after excluding navigation insets.<br><br>
     * 내비게이션 영역을 제외한 가용 가로 픽셀 크기를 반환합니다.<br>
     */
    public fun getScreenWidth(): Int {
        val navSize = getNavigationBarSize()
        val fullWidth = getFullScreenWidth()
        return if (navSize.x < fullWidth) { fullWidth - navSize.x }
        else { fullWidth }
    }

    /**
     * Returns the available screen height after excluding system bars.<br><br>
     * 상태바와 내비게이션 바를 제외한 가용 세로 픽셀 크기를 반환합니다.<br>
     */
    public fun getScreenHeight(): Int {
        val navSize = getNavigationBarSize()
        val fullHeight = getFullScreenHeight()
        val statusBarHeight = getStatusBarHeight().getOrElse { 0 }
        return if (navSize.y < fullHeight) { fullHeight - navSize.y - statusBarHeight }
        else { fullHeight - statusBarHeight }
    }

    /**
     * Returns the usable screen size as `Point(width, height)`.<br><br>
     * 사용 가능한 화면 크기를 `Point(가로, 세로)`로 반환합니다.<br>
     */
    public fun getScreenSize() = Point(getScreenWidth(), getScreenHeight())

    /**
     * Indicates whether the navigation bar is hidden.<br><br>
     * 내비게이션 바가 숨김 상태인지 여부를 반환합니다.<br>
     */
    public fun isNavigationBarHided(): Boolean = getNavigationBarHeight() == 0

    /**
     * Indicates whether the status bar is hidden.<br><br>
     * 상태 표시줄이 숨김 상태인지 여부를 반환합니다.<br>
     */
    public fun isStatusBarHided(): Boolean = getStatusBarHeight().getOrElse {
        Logx.e(it)
        -1
    } == 0

    /**
     * Indicates whether the window uses the full screen with no insets.<br><br>
     * 현재 창이 인셋 없이 전체 화면을 사용하는지 여부를 반환합니다.<br>
     */
    public fun isFullScreen(): Boolean = getFullScreenHeight() == getScreenHeight() && getFullScreenWidth() == getScreenWidth()
}
