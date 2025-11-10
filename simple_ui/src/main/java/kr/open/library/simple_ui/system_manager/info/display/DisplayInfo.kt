package kr.open.library.simple_ui.system_manager.info.display

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Insets
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowMetrics
import androidx.annotation.RequiresApi
import kr.open.library.simple_ui.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.system_manager.extensions.getWindowManager

/**
 * This class provides information about the display of an Android device.
 * DisplayInfo 클래스는 Android 기기의 디스플레이 정보를 제공.
 *
 * This class offers both traditional exception-based methods and safer Result-based alternatives.
 * 이 클래스는 전통적인 예외 기반 메서드와 더 안전한 Result 기반 대안을 모두 제공합니다.
 *
 * Example usage / 사용 예제:
 * ```kotlin
 * val displayInfo = DisplayInfo(context)
 * 
 * // Traditional approach (may throw exceptions)
 * // 전통적인 방식 (예외 발생 가능)
 * try {
 *     val height = displayInfo.getStatusBarHeight()
 *     // Use height...
 * } catch (e: Resources.NotFoundException) {
 *     // Handle error...
 * }
 *
 * // Safe approach with Result pattern
 * displayInfo.getStatusBarHeightSafe().fold(
 *     onSuccess = { height ->
 *         // Use height safely
 *     },
 *     onFailure = { error ->
 *         // Handle error gracefully
 *     }
 * )
 *
 * // Convenient approach with default values
 * val heightWithDefault = displayInfo.getStatusBarHeightOrDefault(60) // Uses 60px if unable to determine
 *
 * ```
 *
 * @param context The application context.
 * @param context 애플리케이션 컨텍스트.
 */
public open class DisplayInfo(context: Context) : BaseSystemService(context, null) {


    public val windowManager: WindowManager by lazy { context.getWindowManager() }

    /**
     * Returns the full screen size.
     * 전체 화면 크기를 반환.
     *
     * @return  The full screen size (width, height).
     * @return 전체 화면 크기 (너비, 높이)
     */
    public fun getFullScreenSize(): Point = checkSdkVersion(Build.VERSION_CODES.R,
        positiveWork = { with(getCurrentWindowMetricsCompat().bounds) { Point(width(), height()) } },
        negativeWork = { getLegacyRealScreenSize() }
    )

    @RequiresApi(Build.VERSION_CODES.R)
    protected open fun getCurrentWindowMetricsCompat(): WindowMetrics = windowManager.currentWindowMetrics

    /**
     * Returns the screen size excluding the status bar and navigation bar.
     * 상태 표시줄과 네비게이션 바를 제외한 화면 크기를 반환.
     *
     * If the desired result is not obtained,
     * be used getScreenWithStatusBar() - getNavigationBarHeight(activity: Activity)
     *
     * @return The screen size (width, height).
     * @return 화면 크기 (너비, 높이).
     */
    public fun getScreen(): Point = checkSdkVersion(Build.VERSION_CODES.R,
        positiveWork = {
            val windowMetrics = getCurrentWindowMetricsCompat()
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())

            val width = windowMetrics.bounds.width() - (insets.left + insets.right)
            val height = windowMetrics.bounds.height() - (insets.bottom + insets.top)
            Point(width, height)
        }, negativeWork = {
            val fullSize = getLegacyRealScreenSize()
            val statusBarHeight = runCatching { getStatusBarHeight() }.getOrDefault(0)
            val legacyInsets = getLegacyNavigationBarInsetsCompat()

            Point(
                (fullSize.x - legacyInsets.horizontal).coerceAtLeast(0),
                (fullSize.y - statusBarHeight - legacyInsets.vertical).coerceAtLeast(0)
            )
        }
    )


    /**
     * Returns the screen size excluding the navigation bar.
     * 탐색 표시줄을 제외한 화면 크기를 반환.
     *
     * @return The screen size (width, height).
     * @return 화면 크기 (너비, 높이)
     */
    public fun getScreenWithStatusBar(): Point = checkSdkVersion(Build.VERSION_CODES.R,
        positiveWork = {
            val windowMetrics = getCurrentWindowMetricsCompat()
            val navInsets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars())
            val width = windowMetrics.bounds.width() - navInsets.horizontal()
            val height = windowMetrics.bounds.height() - navInsets.bottom
            Point(width, height)
        },
        negativeWork = {
            val fullSize = getLegacyRealScreenSize()
            val legacyInsets = getLegacyNavigationBarInsetsCompat()
            Point(
                (fullSize.x - legacyInsets.horizontal).coerceAtLeast(0),
                (fullSize.y - legacyInsets.vertical).coerceAtLeast(0)
            )
        }
    )

    /**
     * Returns the status bar height.
     * 상태 표시줄 높이를 반환.
     *
     * @return The status bar height.
     * @return 상태 표시줄 높이.
     */
    @SuppressLint("InternalInsetResource")
    public open fun getStatusBarHeight(): Int = checkSdkVersion(Build.VERSION_CODES.R,
        positiveWork = {
            getCurrentWindowMetricsCompat().windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.statusBars()).top
        },
        negativeWork = {
            context.resources.getIdentifier("status_bar_height", "dimen", "android")
                .takeIf { it > 0 }?.let { context.resources.getDimensionPixelSize(it) }
                ?: throw Resources.NotFoundException("Cannot find status bar height. Try getStatusBarHeight(activity: Activity).")
        }
    )

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
    @SuppressLint("InternalInsetResource")
    public open fun getNavigationBarSize(): Int = checkSdkVersion(Build.VERSION_CODES.R,
        positiveWork = {
            val navInsets = getCurrentWindowMetricsCompat().windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars())
            when {
                navInsets.bottom > 0 -> navInsets.bottom
                navInsets.top > 0 -> navInsets.top
                else -> maxOf(navInsets.left, navInsets.right)
            }
        },
        negativeWork = {
            val legacyInsets = getLegacyNavigationBarInsetsCompat()
            val size = maxOf(legacyInsets.horizontal, legacyInsets.vertical)
            if (size > 0) size else throw Resources.NotFoundException("Cannot determine navigation bar size. Check navigation bar resources.")
        }
    )

    /**
     * Result 기반 안전 메서드. 상태 표시줄 높이를 Result로 제공.
     */
    public fun getStatusBarHeightSafe(): Result<Int> = runCatching { getStatusBarHeight() }

    /**
     * Result 기반 안전 메서드. 탐색 표시줄 크기를 Result로 제공.
     */
    public fun getNavigationBarSizeSafe(): Result<Int> = runCatching { getNavigationBarSize() }

    /**
     * 상태 표시줄 높이를 구하고 실패 시 기본값을 사용한다.
     */
    public fun getStatusBarHeightOrDefault(defaultValue: Int): Int =
        getStatusBarHeightSafe().getOrElse { defaultValue }

    /**
     * 탐색 표시줄 크기를 구하고 실패 시 기본값을 사용한다.
     */
    public fun getNavigationBarSizeOrDefault(defaultValue: Int): Int =
        getNavigationBarSizeSafe().getOrElse { defaultValue }

    private fun getLegacyRealScreenSize(): Point {
        val metrics = DisplayMetrics().apply {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(this)
        }
        return Point(metrics.widthPixels, metrics.heightPixels)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected data class LegacyNavigationBarInsets(
        val horizontal: Int,
        val vertical: Int,
    )

    protected open fun getLegacyNavigationBarInsetsCompat(): LegacyNavigationBarInsets {
        val resources = context.resources
        fun Resources.dimensionOrZero(name: String): Int {
            val resId = getIdentifier(name, "dimen", "android")
            return resId.takeIf { it > 0 }?.let { getDimensionPixelSize(it) } ?: 0
        }

        val portraitHeight = resources.dimensionOrZero("navigation_bar_height")
        val landscapeHeight = resources.dimensionOrZero("navigation_bar_height_landscape")
        val width = resources.dimensionOrZero("navigation_bar_width")
        val orientation = resources.configuration.orientation

        val maxVertical = maxOf(portraitHeight, landscapeHeight, 0)

        return when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                val vertical = if (portraitHeight > 0) portraitHeight else maxVertical
                LegacyNavigationBarInsets(horizontal = 0, vertical = vertical)
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                val prefersSideBar = width > 0 && width >= landscapeHeight
                if (prefersSideBar) {
                    LegacyNavigationBarInsets(horizontal = width, vertical = 0)
                } else {
                    val vertical = if (landscapeHeight > 0) landscapeHeight else maxVertical
                    LegacyNavigationBarInsets(horizontal = 0, vertical = vertical)
                }
            }
            else -> {
                if (width > maxVertical) {
                    LegacyNavigationBarInsets(horizontal = width, vertical = 0)
                } else {
                    LegacyNavigationBarInsets(horizontal = 0, vertical = maxVertical)
                }
            }
        }
    }

    private fun Insets.horizontal(): Int = left + right
    private fun Insets.vertical(): Int = top + bottom
}
