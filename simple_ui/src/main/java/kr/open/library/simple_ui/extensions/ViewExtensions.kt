package kr.open.library.simple_ui.extensions

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import kr.open.library.simple_ui.R

/********
 * View *
 ********/


internal object ViewIds {
    val LAST_CLICK_TIME = R.id.tag_last_click_time
    val FADE_ANIMATOR = R.id.tag_fade_animator


    /***************************
     * usefor LifeCycle*Layout
     ***************************/
    val TAG_OBSERVED_OWNER = R.id.tag_lifecycle_observer
}




public fun View.setVisible() {
    if (this.visibility != View.VISIBLE) this.visibility = View.VISIBLE
}


public fun View.setGone() {
    if (this.visibility != View.GONE) this.visibility = View.GONE
}


public fun View.setInvisible() {
    if (this.visibility != View.INVISIBLE) this.visibility = View.INVISIBLE
}


/**
 * Sets a debounced click listener on this view to prevent rapid consecutive clicks
 * Uses View's tag system to store timing information, preventing memory leaks
 *
 * @param debounceTime The minimum time interval between clicks in milliseconds (default: 600ms)
 * @param action The action to execute when a valid click occurs
 *
 * Example:
 * ```
 * button.setOnDebouncedClickListener(1000L) { view ->
 *     // This will only execute once per second maximum
 *     navigateToNextScreen()
 * }
 * ```
 */
public fun View.setOnDebouncedClickListener(
    debounceTime: Long = 600L,
    action: (View) -> Unit,
) {
    setOnClickListener { view ->
        val currentTime = System.currentTimeMillis()
        val lastClickTime = (view.getTag(ViewIds.LAST_CLICK_TIME) as? Long) ?: 0L

        if (currentTime - lastClickTime >= debounceTime) {
            view.setTag(ViewIds.LAST_CLICK_TIME, currentTime)
            action(view)
        }
    }
}


/*************
 * ViewGroup *
 *************/
public fun ViewGroup.forEachChild(action: (View) -> Unit) {
    for (i in 0 until childCount) {
        action(getChildAt(i))
    }
}


@SuppressLint("ResourceType")
public fun ViewGroup.getLayoutInflater(
    @LayoutRes xmlRes: Int,
    attachToRoot: Boolean,
): View = LayoutInflater.from(this.context).inflate(xmlRes, this, attachToRoot)


/**
 * Sets all margin values at once
 *
 * @param left Left margin in pixels
 * @param top Top margin in pixels
 * @param right Right margin in pixels
 * @param bottom Bottom margin in pixels
 *
 * Example:
 * ```
 * view.setMargins(16, 8, 16, 8)
 * ```
 */
public fun View.setMargins(
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        val params = layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(left, top, right, bottom)
        layoutParams = params
    }
}


/**
 * Sets uniform margin for all sides
 *
 * @param margin Margin value in pixels for all sides
 *
 * Example:
 * ```
 * view.setMargin(16)
 * ```
 */
public fun View.setMargin(margin: Int) {
    setMargins(margin, margin, margin, margin)
}


/**
 * Sets uniform padding for all sides
 *
 * @param padding Padding value in pixels for all sides
 *
 * Example:
 * ```
 * view.setPadding(12)
 * ```
 */
public fun View.setPadding(padding: Int) {
    setPadding(padding, padding, padding, padding)
}


/**
 * Sets the width of the view
 *
 * @param width Width in pixels
 *
 * Example:
 * ```
 * view.setWidth(200)
 * ```
 */
public fun View.setWidth(width: Int) {
    layoutParams?.let { params ->
        params.width = width
        layoutParams = params
    }
}


/**
 * Sets the height of the view
 *
 * @param height Height in pixels
 *
 * Example:
 * ```
 * view.setHeight(100)
 * ```
 */
public fun View.setHeight(height: Int) {
    layoutParams?.let { params ->
        params.height = height
        layoutParams = params
    }
}


/**
 * Sets both width and height of the view
 *
 * @param width Width in pixels
 * @param height Height in pixels
 *
 * Example:
 * ```
 * view.setSize(200, 100)
 * ```
 */
public fun View.setSize(width: Int, height: Int) {
    layoutParams?.let { params ->
        params.width = width
        params.height = height
        layoutParams = params
    }
}


/**
 * Sets the view width to match parent
 *
 * Example:
 * ```
 * view.setWidthMatchParent()
 * ```
 */
public fun View.setWidthMatchParent() {
    setWidth(ViewGroup.LayoutParams.MATCH_PARENT)
}


/**
 * Sets the view height to match parent
 *
 * Example:
 * ```
 * view.setHeightMatchParent()
 * ```
 */
public fun View.setHeightMatchParent() {
    setHeight(ViewGroup.LayoutParams.MATCH_PARENT)
}


/**
 * Sets the view width to wrap content
 *
 * Example:
 * ```
 * view.setWidthWrapContent()
 * ```
 */
public fun View.setWidthWrapContent() {
    setWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
}


/**
 * Sets the view height to wrap content
 *
 * Example:
 * ```
 * view.setHeightWrapContent()
 * ```
 */
public fun View.setHeightWrapContent() {
    setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
}


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
        try {
            current.lifecycle.addObserver(observer)
            setTag(ViewIds.TAG_OBSERVED_OWNER, current)
        } catch (e:Exception) { return null }
    }
    return current
}


/** 바인딩 해제(attach 해제/재부모 전환 시 호출) */
@MainThread
fun View.unbindLifecycleObserver(observer: DefaultLifecycleObserver) {
    (getTag(ViewIds.TAG_OBSERVED_OWNER) as? LifecycleOwner)?.lifecycle?.removeObserver(observer)
    setTag(ViewIds.TAG_OBSERVED_OWNER, null)
}