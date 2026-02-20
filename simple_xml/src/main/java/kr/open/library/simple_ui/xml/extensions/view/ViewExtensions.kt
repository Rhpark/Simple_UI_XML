package kr.open.library.simple_ui.xml.extensions.view

import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import kr.open.library.simple_ui.xml.R
import kr.open.library.simple_ui.xml.internal.thread.assertMainThreadDebug

/**
 * View extension functions for visibility, sizing, margins, padding, and click handling.<br>
 * Provides convenient methods to manipulate View properties and behavior.<br><br>
 * 가시성, 크기, 여백, 패딩 및 클릭 처리를 위한 View 확장 함수입니다.<br>
 * View 속성과 동작을 조작하는 편리한 메서드를 제공합니다.<br>
 *
 * Example usage:<br>
 * ```kotlin
 * // Visibility control
 * view.setVisible()
 * view.setGone()
 * view.setInvisible()
 *
 * // Debounced click listener
 * button.setOnDebouncedClickListener(1000L) {
 *     navigateToNextScreen()
 * }
 *
 * // Size and layout
 * view.setSize(200, 100)
 * view.setMargin(16)
 * view.setPadding(12)
 * view.setWidthMatchParent()
 * view.setHeightWrapContent()
 *
 * // ViewGroup iteration
 * viewGroup.forEachChild { child ->
 *     child.setVisible()
 * }
 * ```
 */

internal object ViewIds {
    val LAST_CLICK_TIME = R.id.tag_last_click_time
    val FADE_ANIMATOR = R.id.tag_fade_animator

    /***************************
     * usefor LifeCycle*Layout
     ***************************/
    val TAG_OBSERVED_OWNER = R.id.tag_lifecycle_observer
}

/**
 * Sets the view's visibility to VISIBLE.<br><br>
 * View의 가시성을 VISIBLE로 설정합니다.<br>
 */
@MainThread
public fun View.setVisible() {
    assertMainThreadDebug("View.setVisible")
    if (this.visibility != View.VISIBLE) this.visibility = View.VISIBLE
}

/**
 * Sets the view's visibility to GONE.<br><br>
 * View의 가시성을 GONE으로 설정합니다.<br>
 */
@MainThread
public fun View.setGone() {
    assertMainThreadDebug("View.setGone")
    if (this.visibility != View.GONE) this.visibility = View.GONE
}

/**
 * Sets the view's visibility to INVISIBLE.<br><br>
 * View의 가시성을 INVISIBLE로 설정합니다.<br>
 */
@MainThread
public fun View.setInvisible() {
    assertMainThreadDebug("View.setInvisible")
    if (this.visibility != View.INVISIBLE) this.visibility = View.INVISIBLE
}

/**
 * Sets a debounced click listener on this view to prevent rapid consecutive clicks.<br>
 * Uses View's tag system to store timing information, preventing memory leaks.<br><br>
 * 연속적인 빠른 클릭을 방지하기 위해 디바운스된 클릭 리스너를 설정합니다.<br>
 * View의 tag 시스템을 사용하여 타이밍 정보를 저장하여 메모리 누수를 방지합니다.<br>
 *
 * @param debounceTime The minimum time interval between clicks in milliseconds (default: 600ms).<br><br>
 *                     클릭 간 최소 시간 간격(밀리초) (기본값: 600ms).<br>
 *
 * @param action The action to execute when a valid click occurs.<br><br>
 *               유효한 클릭이 발생했을 때 실행할 작업.<br>
 */
@MainThread
public fun View.setOnDebouncedClickListener(
    debounceTime: Long = 600L,
    action: (View) -> Unit,
) {
    assertMainThreadDebug("View.setOnDebouncedClickListener")
    setOnClickListener { view ->
        val currentTime = SystemClock.elapsedRealtime()
        val lastClickTime = (view.getTag(ViewIds.LAST_CLICK_TIME) as? Long) ?: 0L

        if (currentTime - lastClickTime >= debounceTime) {
            view.setTag(ViewIds.LAST_CLICK_TIME, currentTime)
            action(view)
        }
    }
}

/**
 * Iterates over all child views of this ViewGroup.<br><br>
 * 이 ViewGroup의 모든 자식 View를 반복합니다.<br>
 *
 * @param action The action to execute for each child view.<br><br>
 *               각 자식 View에 대해 실행할 작업.<br>
 */
@MainThread
public fun ViewGroup.forEachChild(action: (View) -> Unit) {
    assertMainThreadDebug("ViewGroup.forEachChild")
    for (i in 0 until childCount) {
        action(getChildAt(i))
    }
}

/**
 * Sets all margin values at once.<br><br>
 * 모든 여백 값을 한 번에 설정합니다.<br>
 *
 * @param left Left margin in pixels.<br><br>
 *             왼쪽 여백(픽셀 단위).<br>
 *
 * @param top Top margin in pixels.<br><br>
 *            상단 여백(픽셀 단위).<br>
 *
 * @param right Right margin in pixels.<br><br>
 *              오른쪽 여백(픽셀 단위).<br>
 *
 * @param bottom Bottom margin in pixels.<br><br>
 *               하단 여백(픽셀 단위).<br>
 */
@MainThread
public fun View.setMargins(
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
) {
    assertMainThreadDebug("View.setMargins")
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        val params = layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(left, top, right, bottom)
        layoutParams = params
    }
}

/**
 * Sets uniform margin for all sides.<br><br>
 * 모든 면에 대해 동일한 여백을 설정합니다.<br>
 *
 * @param margin Margin value in pixels for all sides.<br><br>
 *               모든 면의 여백 값(픽셀 단위).<br>
 */
@MainThread
public fun View.setMargin(margin: Int) {
    assertMainThreadDebug("View.setMargin")
    setMargins(margin, margin, margin, margin)
}

/**
 * Sets uniform padding for all sides.<br><br>
 * 모든 면에 대해 동일한 패딩을 설정합니다.<br>
 *
 * @param padding Padding value in pixels for all sides.<br><br>
 *                모든 면의 패딩 값(픽셀 단위).<br>
 */
@MainThread
public fun View.setPadding(padding: Int) {
    assertMainThreadDebug("View.setPadding")
    setPadding(padding, padding, padding, padding)
}

/**
 * Sets the width of the view.<br><br>
 * View의 너비를 설정합니다.<br>
 *
 * @param width Width in pixels.<br><br>
 *              너비(픽셀 단위).<br>
 */
@MainThread
public fun View.setWidth(width: Int) {
    assertMainThreadDebug("View.setWidth")
    layoutParams?.let { params ->
        params.width = width
        layoutParams = params
    }
}

/**
 * Sets the height of the view.<br><br>
 * View의 높이를 설정합니다.<br>
 *
 * @param height Height in pixels.<br><br>
 *               높이(픽셀 단위).<br>
 */
@MainThread
public fun View.setHeight(height: Int) {
    assertMainThreadDebug("View.setHeight")
    layoutParams?.let { params ->
        params.height = height
        layoutParams = params
    }
}

/**
 * Sets both width and height of the view.<br><br>
 * View의 너비와 높이를 모두 설정합니다.<br>
 *
 * @param width Width in pixels.<br><br>
 *              너비(픽셀 단위).<br>
 *
 * @param height Height in pixels.<br><br>
 *               높이(픽셀 단위).<br>
 */
@MainThread
public fun View.setSize(
    width: Int,
    height: Int,
) {
    assertMainThreadDebug("View.setSize")
    layoutParams?.let { params ->
        params.width = width
        params.height = height
        layoutParams = params
    }
}

/**
 * Sets the view width to match parent.<br><br>
 * View의 너비를 부모에 맞춥니다.<br>
 */
@MainThread
public fun View.setWidthMatchParent() {
    assertMainThreadDebug("View.setWidthMatchParent")
    setWidth(ViewGroup.LayoutParams.MATCH_PARENT)
}

/**
 * Sets the view height to match parent.<br><br>
 * View의 높이를 부모에 맞춥니다.<br>
 */
@MainThread
public fun View.setHeightMatchParent() {
    assertMainThreadDebug("View.setHeightMatchParent")
    setHeight(ViewGroup.LayoutParams.MATCH_PARENT)
}

/**
 * Sets the view width to wrap content.<br><br>
 * View의 너비를 내용에 맞춥니다.<br>
 */
@MainThread
public fun View.setWidthWrapContent() {
    assertMainThreadDebug("View.setWidthWrapContent")
    setWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
}

/**
 * Sets the view height to wrap content.<br><br>
 * View의 높이를 내용에 맞춥니다.<br>
 */
@MainThread
public fun View.setHeightWrapContent() {
    assertMainThreadDebug("View.setHeightWrapContent")
    setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
}
