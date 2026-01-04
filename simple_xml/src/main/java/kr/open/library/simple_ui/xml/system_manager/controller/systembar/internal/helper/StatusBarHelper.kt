package kr.open.library.simple_ui.xml.system_manager.controller.systembar.internal.helper

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.xml.system_manager.controller.systembar.internal.helper.base.SystemBarHelperBase

/**
 * Helper class for managing StatusBar operations across different Android versions.<br>
 * Handles color changes, size measurement, and API 35+ background overlay management.<br><br>
 * 다양한 Android 버전에서 StatusBar 작업을 관리하는 헬퍼 클래스입니다.<br>
 * 색상 변경, 크기 측정 및 API 35+ 배경 오버레이 관리를 처리합니다.<br>
 *
 * @param decorView The window's decor view used for measuring and overlay attachment.<br><br>
 *                  측정 및 오버레이 부착에 사용되는 윈도우의 decor view.<br>
 */
internal class StatusBarHelper(
    private val decorView: View
) : SystemBarHelperBase() {
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
    public fun getStatusBarVisibleRect(windowInsets: WindowInsetsCompat): Rect? = safeCatch(null) {
        val top = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        if (top == 0) return@safeCatch Rect() // Return empty Rect when hidden
        val width = decorView.width.coerceAtLeast(0)
        Rect(0, 0, width, top)
    }

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
    public fun getStatusBarStableRect(windowInsets: WindowInsetsCompat): Rect? = safeCatch(null) {
        val stableInsets = windowInsets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars())
        val width = decorView.width.coerceAtLeast(0)
        Rect(0, 0, width, stableInsets.top)
    }

    /**
     * Initializes and adds a background View for StatusBar on API 35+.<br><br>
     * API 35+에서 StatusBar 배경 뷰를 초기화하고 추가합니다.<br>
     *
     * ## Implementation details / 구현 세부사항<br>
     *
     * **View reuse / 뷰 재사용:**<br>
     * - Reuses existing overlay view to prevent memory churn on repeated color changes<br>
     * - 반복적인 색상 변경 시 메모리 낭비 방지를 위해 기존 오버레이 뷰 재사용<br>
     *
     * **WindowInsets listener / WindowInsets 리스너:**<br>
     * - Updates height dynamically when StatusBar size changes (e.g., during rotation)<br>
     * - Only triggers layout pass when height actually changes (performance optimization)<br>
     * - StatusBar position is always top, so only height needs monitoring<br>
     * - StatusBar 크기 변경 시 (예: 회전) 동적으로 높이 업데이트<br>
     * - 높이가 실제로 변경될 때만 layout pass 트리거 (성능 최적화)<br>
     * - StatusBar 위치는 항상 상단이므로 높이만 모니터링하면 충분<br>
     *
     * @param color The color to apply to StatusBar.<br><br>
     *              StatusBar에 적용할 색상.<br>
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    public fun setStatusBarColorSdk35(windowInsets: WindowInsetsCompat, context: Context, @ColorInt color: Int) {
        val decorFrame = decorView as? FrameLayout ?: return

        val overlay = barBackgroundView ?: View(context).also {
            barBackgroundView = it
            it.id = View.generateViewId()
        }

        overlay.configureAsSystemBarOverlay()
        overlay.setBackgroundColor(color)

        val alreadyAttached = overlay.parent === decorFrame
        if (!alreadyAttached) {
            (overlay.parent as? ViewGroup)?.removeView(overlay)

            val initialHeight = getStatusBarStableRect(windowInsets)?.height() ?: 0
            decorFrame.addView(
                overlay,
                FrameLayout.LayoutParams(MATCH_PARENT, initialHeight).apply { gravity = Gravity.TOP }
            )

            overlay.attachStatusBarInsetsListener()

            // Trigger insets only when first attached
            decorView.post { ViewCompat.requestApplyInsets(decorView) }
        }
    }

    /**
     * Attaches WindowInsets listener to dynamically update StatusBar overlay height.<br>
     * Only monitors height changes since StatusBar position is always top.<br><br>
     * StatusBar 오버레이 높이를 동적으로 업데이트하는 WindowInsets 리스너를 연결합니다.<br>
     * StatusBar 위치는 항상 상단이므로 높이 변경만 모니터링합니다.<br>
     */
    private fun View.attachStatusBarInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val lp = view.layoutParams as FrameLayout.LayoutParams
            // Conditional update: only trigger layout pass if height changed (performance optimization)
            if (lp.height != top) {
                lp.height = top
                view.layoutParams = lp
            }
            view.visibility = if (top == 0) View.GONE else View.VISIBLE
            insets
        }
    }

    /**
     * Cleans up status bar overlay view (API 35+).<br>
     * Removes WindowInsets listener and removes view from decorView.<br><br>
     * 상태바 오버레이 뷰를 정리합니다 (API 35+).<br>
     * WindowInsets 리스너를 제거하고 decorView에서 뷰를 제거합니다.<br>
     */
    public fun cleanupStatusBarOverlay() = safeCatch {
        barBackgroundView?.let { view ->
            ViewCompat.setOnApplyWindowInsetsListener(view, null)
            (decorView as? FrameLayout)?.removeView(view)
            barBackgroundView = null
        }
    }
}
