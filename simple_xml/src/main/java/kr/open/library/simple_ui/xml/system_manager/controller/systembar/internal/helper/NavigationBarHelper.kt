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
 * Helper class for managing NavigationBar operations across different Android versions.<br>
 * Handles color changes, size measurement, position detection, and API 35+ background overlay management.<br>
 * Supports bottom, left, and right NavigationBar positions for phones, tablets, and foldables.<br><br>
 * 다양한 Android 버전에서 NavigationBar 작업을 관리하는 헬퍼 클래스입니다.<br>
 * 색상 변경, 크기 측정, 위치 감지 및 API 35+ 배경 오버레이 관리를 처리합니다.<br>
 * 휴대폰, 태블릿, 폴더블용 하단, 왼쪽, 오른쪽 NavigationBar 위치를 지원합니다.<br>
 *
 * @param decorView The window's decor view used for measuring and overlay attachment.<br><br>
 *                  측정 및 오버레이 부착에 사용되는 윈도우의 decor view.<br>
 */
internal class NavigationBarHelper(
    decorView: View
) : SystemBarHelperBase(decorView) {
    /**
     * Returns the window coordinates of the currently visible NavigationBar area.<br>
     * Uses WindowInsetsCompat with visibility detection.<br>
     * Supports bottom, left, and right NavigationBar positions.<br><br>
     * 현재 보이는 NavigationBar 영역의 윈도우 좌표를 반환합니다.<br>
     * WindowInsetsCompat과 가시성 감지를 사용합니다.<br>
     * 하단, 왼쪽, 오른쪽 NavigationBar 위치를 지원합니다.<br>
     *
     * @return Rect with window coordinates relative to decorView, or Rect() if NavigationBar is hidden, or null if view not ready.<br>
     *         - Bottom: (0, decorViewHeight-navHeight, decorViewWidth, decorViewHeight)<br>
     *         - Left: (0, 0, navWidth, decorViewHeight)<br>
     *         - Right: (decorViewWidth-navWidth, 0, decorViewWidth, decorViewHeight)<br>
     *         - Rect() when NavigationBar is hidden (all insets are 0)<br>
     *         - null when decorView is not attached to window or has invalid dimensions (width/height <= 0)<br><br>
     *         decorView 기준 윈도우 좌표를 가진 Rect, NavigationBar가 숨겨진 경우 Rect(), 뷰가 준비되지 않은 경우 null.<br>
     *         - 하단: (0, decorView높이-네비높이, decorView너비, decorView높이)<br>
     *         - 왼쪽: (0, 0, 네비너비, decorView높이)<br>
     *         - 오른쪽: (decorView너비-네비너비, 0, decorView너비, decorView높이)<br>
     *         - NavigationBar가 숨겨진 경우 (모든 insets가 0): Rect()<br>
     *         - decorView가 window에 부착되지 않았거나 크기가 유효하지 않은 경우 (width/height <= 0): null<br>
     */
    public fun getNavigationBarVisibleRect(windowInsets: WindowInsetsCompat): Rect? = safeCatch(null) {
        if (!decorView.isSystemBarRectReady()) return null
        insetsToNavigationBarRect(windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()))
    }

    /**
     * Returns the window coordinates of the system-defined (stable) NavigationBar area.<br>
     * This value remains constant even if the NavigationBar is hidden.<br>
     * Supports bottom, left, and right NavigationBar positions.<br><br>
     * 시스템 정의(stable) NavigationBar 영역의 윈도우 좌표를 반환합니다.<br>
     * 이 값은 NavigationBar가 숨겨진 경우에도 일정하게 유지됩니다.<br>
     * 하단, 왼쪽, 오른쪽 NavigationBar 위치를 지원합니다.<br>
     *
     * @return Rect with window coordinates relative to decorView, or Rect() if stable insets are 0, or null if view not ready.<br>
     *         - Bottom: (0, decorViewHeight-navHeight, decorViewWidth, decorViewHeight)<br>
     *         - Left: (0, 0, navWidth, decorViewHeight)<br>
     *         - Right: (decorViewWidth-navWidth, 0, decorViewWidth, decorViewHeight)<br>
     *         - Rect() when navigationBars stableInsets are 0 (e.g., gesture navigation mode)<br>
     *         - null when decorView is not attached to window or has invalid dimensions (width/height <= 0)<br><br>
     *         decorView 기준 윈도우 좌표를 가진 Rect, stable insets가 0인 경우 Rect(), 뷰가 준비되지 않은 경우 null.<br>
     *         - 하단: (0, decorView높이-네비높이, decorView너비, decorView높이)<br>
     *         - 왼쪽: (0, 0, 네비너비, decorView높이)<br>
     *         - 오른쪽: (decorView너비-네비너비, 0, decorView너비, decorView높이)<br>
     *         - navigationBars stableInsets가 0인 경우 (예: 제스처 내비게이션 모드): Rect()<br>
     *         - decorView가 window에 부착되지 않았거나 크기가 유효하지 않은 경우 (width/height <= 0): null<br>
     */
    public fun getNavigationBarStableRect(windowInsets: WindowInsetsCompat): Rect? = safeCatch(null) {
        if (!decorView.isSystemBarRectReady()) return null
        insetsToNavigationBarRect(windowInsets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.navigationBars()))
    }

    /**
     * Converts WindowInsets to NavigationBar Rect coordinates.<br>
     * Determines position (bottom/left/right) and calculates window coordinates.<br><br>
     * WindowInsets를 NavigationBar Rect 좌표로 변환합니다.<br>
     * 위치(하단/왼쪽/오른쪽)를 판별하고 윈도우 좌표를 계산합니다.<br>
     *
     * @param insets The Insets object containing NavigationBar dimensions.<br><br>
     *               NavigationBar 크기를 포함하는 Insets 객체.<br>
     *
     * @return Rect with window coordinates, or Rect() if no NavigationBar present.<br><br>
     *         윈도우 좌표를 가진 Rect, NavigationBar가 없는 경우 Rect().<br>
     */
    private fun insetsToNavigationBarRect(insets: androidx.core.graphics.Insets): Rect {
        val decorWidth = decorView.width.coerceAtLeast(0)
        val decorHeight = decorView.height.coerceAtLeast(0)

        return when {
            insets.bottom > 0 -> {
                val top = (decorHeight - insets.bottom).coerceAtLeast(0)
                Rect(0, top, decorWidth, decorHeight)
            }

            insets.left > 0 -> Rect(0, 0, insets.left, decorHeight)

            insets.right > 0 -> {
                val left = (decorWidth - insets.right).coerceAtLeast(0)
                Rect(left, 0, decorWidth, decorHeight)
            }

            else -> Rect()
        }
    }

    /**
     * Initializes and adds a background View for NavigationBar on API 35+.<br>
     * Supports bottom, left, and right NavigationBar positions.<br><br>
     * API 35+에서 NavigationBar 배경 뷰를 초기화하고 추가합니다.<br>
     * 하단, 왼쪽, 오른쪽 NavigationBar 위치를 지원합니다.<br>
     *
     * ## Implementation details / 구현 세부사항<br>
     *
     * **View reuse / 뷰 재사용:**<br>
     * - Reuses existing overlay view to prevent memory churn on repeated color changes<br>
     * - 반복적인 색상 변경 시 메모리 낭비 방지를 위해 기존 오버레이 뷰 재사용<br>
     *
     * **Elevation handling / Elevation 처리:**<br>
     * - Sets `elevation = 0f` to prevent touch event blocking on gesture navigation<br>
     * - Without this, overlay can block NavigationBar swipe gestures despite non-interactive flags<br>
     * - `elevation = 0f` 설정으로 제스처 내비게이션에서 터치 이벤트 차단 방지<br>
     * - 이 설정 없이는 비대화형 플래그에도 불구하고 오버레이가 NavigationBar 스와이프 제스처를 차단할 수 있음<br>
     *
     * **Position detection / 위치 감지:**<br>
     * - Detects NavigationBar position (bottom/left/right) from WindowInsets<br>
     * - Bottom: Rect.bottom == decorView.height (typical phones)<br>
     * - Left: Rect.left == 0 && Rect.height == decorView.height (landscape tablets)<br>
     * - Right: Rect.right == decorView.width && Rect.height == decorView.height (landscape foldables)<br>
     * - WindowInsets에서 NavigationBar 위치 (하단/왼쪽/오른쪽) 감지<br>
     * - 하단: Rect.bottom == decorView.height (일반 폰)<br>
     * - 왼쪽: Rect.left == 0 && Rect.height == decorView.height (가로 모드 태블릿)<br>
     * - 오른쪽: Rect.right == decorView.width && Rect.height == decorView.height (가로 모드 폴더블)<br>
     *
     * **WindowInsets listener / WindowInsets 리스너:**<br>
     * - Unlike StatusBar (fixed top), NavigationBar can change position during rotation<br>
     * - Monitors width, height, AND gravity to handle position changes<br>
     * - Only triggers layout pass when any dimension/position actually changes (performance optimization)<br>
     * - StatusBar (고정 상단)와 달리 NavigationBar는 회전 시 위치 변경 가능<br>
     * - 위치 변경을 처리하기 위해 width, height, gravity 모두 모니터링<br>
     * - 실제로 크기/위치가 변경될 때만 layout pass 트리거 (성능 최적화)<br>
     *
     * @param color The color to apply to NavigationBar.<br><br>
     *              NavigationBar에 적용할 색상.<br>
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    public fun setNavigationBarColorSdk35(windowInsets: WindowInsetsCompat, context: Context, @ColorInt color: Int) {
        val decorFrame = decorView as? FrameLayout ?: return

        val overlay = barBackgroundView ?: View(context).also {
            barBackgroundView = it
            it.id = View.generateViewId()
        }

        // 1) Configure overlay as decorative (non-interactive, non-focusable)
        overlay.configureAsSystemBarOverlay()

        // 2) Update color only (if already attached, skip re-attachment)
        overlay.setBackgroundColor(color)

        // 3) Keep elevation = 0f to prevent blocking gesture navigation
        overlay.elevation = 0f

        // Critical: Skip remove/add if already attached to decorFrame
        val alreadyAttached = overlay.parent === decorFrame
        if (!alreadyAttached) {
            // Only detach if attached to different parent
            (overlay.parent as? ViewGroup)?.removeView(overlay)

            // 초기 배치 결정 (Insets 미준비/0이면 일단 bottom + height=0으로 붙이고 리스너가 나중에 보정)
            val navRect = getNavigationBarStableRect(windowInsets) ?: Rect(0, 0, 0, 0)
            val decorWidth = decorView.width
            val decorHeight = decorView.height

            val layoutParams = when {
                navRect.bottom == decorHeight && navRect.height() > 0 -> {
                    FrameLayout.LayoutParams(MATCH_PARENT, navRect.height()).apply { gravity = Gravity.BOTTOM }
                }
                navRect.left == 0 && navRect.width() > 0 && navRect.height() == decorHeight -> {
                    FrameLayout.LayoutParams(navRect.width(), MATCH_PARENT).apply { gravity = Gravity.LEFT }
                }
                navRect.right == decorWidth && navRect.width() > 0 && navRect.height() == decorHeight -> {
                    FrameLayout.LayoutParams(navRect.width(), MATCH_PARENT).apply { gravity = Gravity.RIGHT }
                }
                else -> {
                    FrameLayout.LayoutParams(MATCH_PARENT, 0).apply { gravity = Gravity.BOTTOM }
                }
            }

            decorFrame.addView(overlay, layoutParams)

            // 리스너는 “처음 붙일 때만” 달기 (중복 세팅/할당 방지)
            overlay.attachNavigationBarInsetsListener()

            // Insets가 늦게 준비되는 타이밍을 위해 “처음 붙일 때만” 트리거 (StatusBar 패턴과 동일)
            decorView.post { ViewCompat.requestApplyInsets(decorView) }
        }
    }

    /**
     * Attaches WindowInsets listener to dynamically update NavigationBar overlay size and position.<br>
     * Monitors width, height, AND gravity since NavigationBar can change position during rotation.<br><br>
     * NavigationBar 오버레이 크기와 위치를 동적으로 업데이트하는 WindowInsets 리스너를 연결합니다.<br>
     * NavigationBar는 회전 시 위치가 변경될 수 있으므로 width, height, gravity 모두 모니터링합니다.<br>
     */
    private fun View.attachNavigationBarInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val lp = view.layoutParams as FrameLayout.LayoutParams

            // Update layout params and visibility based on current NavigationBar position
            when {
                navInsets.bottom > 0 -> {
                    // Bottom NavigationBar - only update if changed (performance optimization)
                    if (lp.width != MATCH_PARENT || lp.height != navInsets.bottom || lp.gravity != Gravity.BOTTOM) {
                        lp.width = MATCH_PARENT
                        lp.height = navInsets.bottom
                        lp.gravity = Gravity.BOTTOM
                        view.layoutParams = lp
                    }
                    view.visibility = View.VISIBLE
                }
                navInsets.left > 0 -> {
                    // Left NavigationBar
                    if (lp.width != navInsets.left || lp.height != MATCH_PARENT || lp.gravity != Gravity.LEFT) {
                        lp.width = navInsets.left
                        lp.height = MATCH_PARENT
                        lp.gravity = Gravity.LEFT
                        view.layoutParams = lp
                    }
                    view.visibility = View.VISIBLE
                }
                navInsets.right > 0 -> {
                    // Right NavigationBar
                    if (lp.width != navInsets.right || lp.height != MATCH_PARENT || lp.gravity != Gravity.RIGHT) {
                        lp.width = navInsets.right
                        lp.height = MATCH_PARENT
                        lp.gravity = Gravity.RIGHT
                        view.layoutParams = lp
                    }
                    view.visibility = View.VISIBLE
                }
                else -> {
                    // NavigationBar hidden - reset dimensions to 0
                    if (lp.width != 0 || lp.height != 0) {
                        lp.width = 0
                        lp.height = 0
                        view.layoutParams = lp
                    }
                    view.visibility = View.GONE
                }
            }
            insets
        }
    }

    /**
     * Cleans up navigation bar overlay view (API 35+).<br>
     * Removes WindowInsets listener and removes view from decorView.<br><br>
     * 네비게이션 바 오버레이 뷰를 정리합니다 (API 35+).<br>
     * WindowInsets 리스너를 제거하고 decorView에서 뷰를 제거합니다.<br>
     */
    public fun cleanupNavigationBarOverlay() = safeCatch {
        barBackgroundView?.let { view ->
            ViewCompat.setOnApplyWindowInsetsListener(view, null)
            (decorView as? FrameLayout)?.removeView(view)
            barBackgroundView = null
        }
    }
}
