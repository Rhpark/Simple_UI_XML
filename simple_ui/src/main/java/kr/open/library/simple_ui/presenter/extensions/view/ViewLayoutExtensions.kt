package kr.open.library.simple_ui.presenter.extensions.view

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
import kr.open.library.simple_ui.extensions.trycatch.safeCatch


@SuppressLint("ResourceType")
public fun ViewGroup.getLayoutInflater(@LayoutRes xmlRes: Int, attachToRoot: Boolean
): View = LayoutInflater.from(this.context).inflate(xmlRes, this, attachToRoot)

/** View → 호스트 LifecycleOwner (Fragment의 viewLifecycleOwner 우선, 없으면 Activity) */
@MainThread
inline fun View.findHostLifecycleOwner(): LifecycleOwner? =
    findViewTreeLifecycleOwner() ?: (context as? LifecycleOwner)

/** 옵저버를 현재 Owner에 바인딩. 기존 Owner와 다르면 교체, 중복 등록 방지 */
@MainThread
fun View.bindLifecycleObserver(observer: DefaultLifecycleObserver): LifecycleOwner? {
    val current = findHostLifecycleOwner() ?: return null
    val old = getTag(ViewIds.TAG_OBSERVED_OWNER) as? LifecycleOwner
    if (old !== current) {
        old?.lifecycle?.removeObserver(observer)
        val res = safeCatch(false) {
            current.lifecycle.addObserver(observer)
            setTag(ViewIds.TAG_OBSERVED_OWNER, current)
            true
        }
        if(res == false) return null
    }
    return current
}



/** 바인딩 해제(attach 해제/재부모 전환 시 호출) */
@MainThread
fun View.unbindLifecycleObserver(observer: DefaultLifecycleObserver) {
    (getTag(ViewIds.TAG_OBSERVED_OWNER) as? LifecycleOwner)?.lifecycle?.removeObserver(observer)
    setTag(ViewIds.TAG_OBSERVED_OWNER, null)
}



/**
 * Executes a block when the view has been laid out and measured
 * Useful for getting actual view dimensions
 *
 * @param action Block to execute when view is laid out
 *
 * Example:
 * ```
 * customView.doOnLayout {
 *     val width = it.width
 *     val height = it.height
 *     // Use actual dimensions
 * }
 * ```
 */
public inline fun View.doOnLayout(crossinline action: (view: View) -> Unit) {
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
 * Gets the view's location on screen as a Pair
 *
 * @return Pair of (x, y) coordinates on screen
 *
 * Example:
 * ```
 * val (x, y) = button.getLocationOnScreen()
 * ```
 */
public fun View.getLocationOnScreen(): Pair<Int, Int> {
    val location = IntArray(2)
    getLocationOnScreen(location)
    return Pair(location[0], location[1])
}

/**************************
 * Window Insets Extensions *
 **************************/

/**
 * Applies window insets as padding to the view
 * Useful for handling system bars and keyboard
 *
 * @param left Whether to apply left inset as left padding (default: true)
 * @param top Whether to apply top inset as top padding (default: true)
 * @param right Whether to apply right inset as right padding (default: true)
 * @param bottom Whether to apply bottom inset as bottom padding (default: true)
 *
 * Example:
 * ```
 * rootView.applyWindowInsetsAsPadding(bottom = true, top = false)
 * ```
 */
public fun View.applyWindowInsetsAsPadding(
    left: Boolean = true,
    top: Boolean = true,
    right: Boolean = true,
    bottom: Boolean = true,
) {
    val initialPadding = Pair(Pair(paddingLeft, paddingTop), Pair(paddingRight, paddingBottom))

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

        view.setPadding(
            if (left) initialPadding.first.first + systemBars.left else initialPadding.first.first,
            if (top) initialPadding.first.second + systemBars.top else initialPadding.first.second,
            if (right) initialPadding.second.first + systemBars.right else initialPadding.second.first,
            if (bottom) initialPadding.second.second + systemBars.bottom else initialPadding.second.second,
        )

        insets
    }
}
