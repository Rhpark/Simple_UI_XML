/**
 * View layout and lifecycle extension functions.<br>
 * Provides convenient methods for layout inflation, lifecycle observation, and window insets handling.<br><br>
 * View 레이아웃 및 라이프사이클 확장 함수입니다.<br>
 * 레이아웃 인플레이션, 라이프사이클 관찰 및 윈도우 인셋 처리를 위한 편리한 메서드를 제공합니다.<br>
 *
 * Example usage:<br>
 * ```kotlin
 * // Layout inflation
 * val view = viewGroup.getLayoutInflater(R.layout.item_view, false)
 *
 * // Lifecycle observation
 * view.bindLifecycleObserver(observer)
 * view.unbindLifecycleObserver(observer)
 *
 * // Layout callbacks
 * view.doOnLayout { v ->
 *     val width = v.width
 *     val height = v.height
 * }
 *
 * // Window insets
 * rootView.applyWindowInsetsAsPadding(bottom = true)
 * ```
 */
package kr.open.library.simple_ui.xml.extensions.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.xml.internal.thread.assertMainThreadDebug
import kotlin.math.max

/**
 * Inflates a layout resource into this ViewGroup.<br><br>
 * 레이아웃 리소스를 이 ViewGroup에 인플레이트합니다.<br>
 *
 * @param xmlRes Layout resource ID.<br><br>
 *               레이아웃 리소스 ID.<br>
 *
 * @param attachToRoot Whether to attach the inflated layout to the root.<br><br>
 *                     인플레이트된 레이아웃을 루트에 연결할지 여부.<br>
 *
 * @return The inflated View.<br><br>
 *         인플레이트된 View.<br>
 */
@SuppressLint("ResourceType")
public fun ViewGroup.getLayoutInflater(
    @LayoutRes xmlRes: Int,
    attachToRoot: Boolean,
): View = LayoutInflater.from(this.context).inflate(xmlRes, this, attachToRoot)

/**
 * Finds the host LifecycleOwner for this View.<br>
 * Prioritizes Fragment's viewLifecycleOwner, falls back to Activity.<br><br>
 * 이 View의 호스트 LifecycleOwner를 찾습니다.<br>
 * Fragment의 viewLifecycleOwner를 우선시하고, 없으면 Activity를 사용합니다.<br>
 *
 * @return The LifecycleOwner or null if not found.<br><br>
 *         LifecycleOwner 또는 찾을 수 없으면 null.<br>
 */
@MainThread
inline fun View.findHostLifecycleOwner(): LifecycleOwner? {
    assertMainThreadDebug("View.findHostLifecycleOwner")
    return findViewTreeLifecycleOwner() ?: (context as? LifecycleOwner)
}

/**
 * Binds a lifecycle observer to the current LifecycleOwner.<br>
 * Replaces the observer if the owner changes, prevents duplicate registration.<br><br>
 * 현재 LifecycleOwner에 라이프사이클 옵저버를 바인딩합니다.<br>
 * Owner가 변경되면 옵저버를 교체하고, 중복 등록을 방지합니다.<br>
 *
 * @param observer The lifecycle observer to bind.<br><br>
 *                 바인딩할 라이프사이클 옵저버.<br>
 *
 * @return The current LifecycleOwner or null if binding failed.<br><br>
 *         현재 LifecycleOwner 또는 바인딩 실패 시 null.<br>
 */
@MainThread
fun View.bindLifecycleObserver(observer: DefaultLifecycleObserver): LifecycleOwner? {
    assertMainThreadDebug("View.bindLifecycleObserver")
    val current = findHostLifecycleOwner() ?: return null
    val bindings = getLifecycleObserverBindings()
    val oldOwner = bindings[observer]
    if (oldOwner !== current) {
        oldOwner?.lifecycle?.removeObserver(observer)
        val registered =
            safeCatch(false) {
                current.lifecycle.addObserver(observer)
                true
            }
        if (!registered) return null
        bindings[observer] = current
        setTag(ViewIds.TAG_OBSERVED_OWNER, bindings)
    }
    return current
}

/**
 * Unbinds the lifecycle observer from this View.<br>
 * Should be called when detaching the view or changing parent.<br><br>
 * 이 View에서 라이프사이클 옵저버를 언바인딩합니다.<br>
 * View를 분리하거나 부모를 변경할 때 호출해야 합니다.<br>
 *
 * @param observer The lifecycle observer to unbind.<br><br>
 *                 언바인딩할 라이프사이클 옵저버.<br>
 */
@MainThread
fun View.unbindLifecycleObserver(observer: DefaultLifecycleObserver) {
    assertMainThreadDebug("View.unbindLifecycleObserver")
    @Suppress("UNCHECKED_CAST")
    val bindings =
        getTag(ViewIds.TAG_OBSERVED_OWNER) as? MutableMap<DefaultLifecycleObserver, LifecycleOwner>
            ?: return

    bindings.remove(observer)?.lifecycle?.removeObserver(observer)

    if (bindings.isEmpty()) {
        setTag(ViewIds.TAG_OBSERVED_OWNER, null)
    } else {
        setTag(ViewIds.TAG_OBSERVED_OWNER, bindings)
    }
}

@MainThread
private fun View.getLifecycleObserverBindings(): MutableMap<DefaultLifecycleObserver, LifecycleOwner> {
    assertMainThreadDebug("View.getLifecycleObserverBindings")
    @Suppress("UNCHECKED_CAST")
    val existing =
        getTag(ViewIds.TAG_OBSERVED_OWNER) as? MutableMap<DefaultLifecycleObserver, LifecycleOwner>
    if (existing != null) return existing

    return mutableMapOf<DefaultLifecycleObserver, LifecycleOwner>().also {
        setTag(ViewIds.TAG_OBSERVED_OWNER, it)
    }
}

/**
 * Executes a block when the view has been laid out and measured.<br>
 * Useful for getting actual view dimensions.<br><br>
 * View가 레이아웃되고 측정된 후 블록을 실행합니다.<br>
 * 실제 View 크기를 얻는 데 유용합니다.<br>
 *
 * @param action Block to execute when view is laid out.<br><br>
 *               View가 레이아웃될 때 실행할 블록.<br>
 */
@MainThread
public inline fun View.doOnLayout(crossinline action: (view: View) -> Unit) {
    assertMainThreadDebug("View.doOnLayout")
    if (isLaidOut && !isLayoutRequested) {
        action(this)
    } else {
        viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    action(this@doOnLayout)
                }
            },
        )
    }
}

/**
 * Gets the view's location on screen as a Pair.<br><br>
 * View의 화면상 위치를 Pair로 가져옵니다.<br>
 *
 * @return Pair of (x, y) coordinates on screen.<br><br>
 *         화면상 (x, y) 좌표의 Pair.<br>
 */
@MainThread
public fun View.getLocationOnScreen(): Pair<Int, Int> {
    assertMainThreadDebug("View.getLocationOnScreen")
    val location = IntArray(2)
    getLocationOnScreen(location)
    return Pair(location[0], location[1])
}

/**
 * Applies window insets as padding to the view.<br>
 * Useful for handling system bars and keyboard.<br><br>
 * 윈도우 인셋을 View의 패딩으로 적용합니다.<br>
 * 시스템 바 및 키보드 처리에 유용합니다.<br>
 *
 * @param left Whether to apply left inset as left padding (default: true).<br><br>
 *             왼쪽 인셋을 왼쪽 패딩으로 적용할지 여부 (기본값: true).<br>
 *
 * @param top Whether to apply top inset as top padding (default: true).<br><br>
 *            상단 인셋을 상단 패딩으로 적용할지 여부 (기본값: true).<br>
 *
 * @param right Whether to apply right inset as right padding (default: true).<br><br>
 *              오른쪽 인셋을 오른쪽 패딩으로 적용할지 여부 (기본값: true).<br>
 *
 * @param bottom Whether to apply bottom inset as bottom padding (default: true).<br><br>
 *               하단 인셋을 하단 패딩으로 적용할지 여부 (기본값: true).<br>
 */
@MainThread
public fun View.applyWindowInsetsAsPadding(
    left: Boolean = true,
    top: Boolean = true,
    right: Boolean = true,
    bottom: Boolean = true,
) {
    assertMainThreadDebug("View.applyWindowInsetsAsPadding")
    val initialPadding = Pair(Pair(paddingLeft, paddingTop), Pair(paddingRight, paddingBottom))

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
        val bottomInset = max(systemBars.bottom, imeInsets.bottom)

        view.setPadding(
            if (left) initialPadding.first.first + systemBars.left else initialPadding.first.first,
            if (top) initialPadding.first.second + systemBars.top else initialPadding.first.second,
            if (right) initialPadding.second.first + systemBars.right else initialPadding.second.first,
            if (bottom) initialPadding.second.second + bottomInset else initialPadding.second.second,
        )

        insets
    }
}
