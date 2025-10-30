package kr.open.library.simple_ui.presenter.extensions.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import kr.open.library.simple_ui.R
import kr.open.library.simple_ui.extensions.trycatch.safeCatch

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
        val currentTime = SystemClock.elapsedRealtime()
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
public fun ViewGroup.getLayoutInflater(@LayoutRes xmlRes: Int, attachToRoot: Boolean
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


/*******************
 * Basic Animation *
 *******************/

/**
 * Animates the view's scale with customizable parameters
 *
 * @param fromScale Starting scale value (default: current scale)
 * @param toScale Target scale value
 * @param duration Animation duration in milliseconds (default: 300ms)
 * @param onComplete Optional callback when animation completes
 *
 * Example:
 * ```
 * button.animateScale(fromScale = 1f, toScale = 1.2f, duration = 150L) {
 *     // Scale animation completed
 * }
 * ```
 */
public fun View.animateScale(
    fromScale: Float = scaleX,
    toScale: Float,
    duration: Long = 300L,
    onComplete: (() -> Unit)? = null,
) {
    scaleX = fromScale
    scaleY = fromScale

    animate()
        .scaleX(toScale)
        .scaleY(toScale)
        .setDuration(duration)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .setListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onComplete?.invoke()
                }
            },
        )
        .start()
}

/**
 * Creates a pulsing animation effect
 *
 * @param minScale Minimum scale value (default: 0.95f)
 * @param maxScale Maximum scale value (default: 1.05f)
 * @param duration Duration for one complete pulse cycle in milliseconds (default: 1000ms)
 * @param repeatCount Number of times to repeat (-1 for infinite, default: -1)
 *
 * Example:
 * ```
 * heartIcon.pulse(minScale = 0.9f, maxScale = 1.1f, duration = 800L)
 * ```
 */
public fun View.pulse(
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f,
    duration: Long = 1000L,
    repeatCount: Int = ValueAnimator.INFINITE,
) {
    val animator = ValueAnimator.ofFloat(minScale, maxScale, minScale)
    animator.duration = duration
    animator.repeatCount = repeatCount
    animator.interpolator = AccelerateDecelerateInterpolator()

    animator.addUpdateListener { animation ->
        val scale = animation.animatedValue as Float
        scaleX = scale
        scaleY = scale
    }

    setTag(ViewIds.FADE_ANIMATOR, animator)
    animator.start()
}

/**
 * Stops any pulsing animation on this view
 *
 * Example:
 * ```
 * heartIcon.stopPulse()
 * ```
 */
public fun View.stopPulse() {
    (getTag(ViewIds.FADE_ANIMATOR) as? ValueAnimator)?.let { animator ->
        animator.cancel()
        setTag(ViewIds.FADE_ANIMATOR, null)
        scaleX = 1f
        scaleY = 1f
    }
}

/**
 * Animates view sliding in from a specific direction
 *
 * @param direction Direction to slide from (LEFT, RIGHT, TOP, BOTTOM)
 * @param distance Distance to slide in pixels (default: view width/height)
 * @param duration Animation duration in milliseconds (default: 300ms)
 * @param onComplete Optional callback when animation completes
 *
 * Example:
 * ```
 * panel.slideIn(SlideDirection.RIGHT, duration = 250L) {
 *     // Slide animation completed
 * }
 * ```
 */
public fun View.slideIn(
    direction: SlideDirection,
    distance: Float = 0f,
    duration: Long = 300L,
    onComplete: (() -> Unit)? = null,
) {
    val actualDistance =
        if (distance == 0f) {
            when (direction) {
                SlideDirection.LEFT, SlideDirection.RIGHT -> width.toFloat()
                SlideDirection.TOP, SlideDirection.BOTTOM -> height.toFloat()
            }
        } else {
            distance
        }

    val (startX, startY) =
        when (direction) {
            SlideDirection.LEFT -> -actualDistance to 0f
            SlideDirection.RIGHT -> actualDistance to 0f
            SlideDirection.TOP -> 0f to -actualDistance
            SlideDirection.BOTTOM -> 0f to actualDistance
        }

    translationX = startX
    translationY = startY
    isVisible = true

    animate()
        .translationX(0f)
        .translationY(0f)
        .setDuration(duration)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .setListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onComplete?.invoke()
                }
            },
        )
        .start()
}

/**
 * Animates view sliding out to a specific direction
 *
 * @param direction Direction to slide to (LEFT, RIGHT, TOP, BOTTOM)
 * @param distance Distance to slide in pixels (default: view width/height)
 * @param duration Animation duration in milliseconds (default: 300ms)
 * @param hideOnComplete Whether to set visibility to GONE after animation (default: true)
 * @param onComplete Optional callback when animation completes
 *
 * Example:
 * ```
 * panel.slideOut(SlideDirection.LEFT, hideOnComplete = true) {
 *     // Panel is now hidden
 * }
 * ```
 */
public fun View.slideOut(
    direction: SlideDirection,
    distance: Float = 0f,
    duration: Long = 300L,
    hideOnComplete: Boolean = true,
    onComplete: (() -> Unit)? = null,
) {
    val actualDistance =
        if (distance == 0f) {
            when (direction) {
                SlideDirection.LEFT, SlideDirection.RIGHT -> width.toFloat()
                SlideDirection.TOP, SlideDirection.BOTTOM -> height.toFloat()
            }
        } else {
            distance
        }

    val (endX, endY) =
        when (direction) {
            SlideDirection.LEFT -> -actualDistance to 0f
            SlideDirection.RIGHT -> actualDistance to 0f
            SlideDirection.TOP -> 0f to -actualDistance
            SlideDirection.BOTTOM -> 0f to actualDistance
        }

    animate()
        .translationX(endX)
        .translationY(endY)
        .setDuration(duration)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .setListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (hideOnComplete) {
                        visibility = View.GONE
                    }
                    onComplete?.invoke()
                }
            },
        )
        .start()
}

/**
 * Creates a shake animation effect
 *
 * @param intensity Shake intensity in pixels (default: 10f)
 * @param duration Duration of the shake animation in milliseconds (default: 500ms)
 * @param onComplete Optional callback when animation completes
 *
 * Example:
 * ```
 * errorField.shake(intensity = 15f) {
 *     // Shake animation completed
 * }
 * ```
 */
public fun View.shake(
    intensity: Float = 10f,
    duration: Long = 500L,
    onComplete: (() -> Unit)? = null,
) {
    val originalX = translationX

    animate()
        .translationX(originalX + intensity)
        .setDuration(duration / 10)
        .setInterpolator(LinearInterpolator())
        .setListener(
            object : AnimatorListenerAdapter() {
                private var shakeCount = 0
                private val maxShakes = 10

                override fun onAnimationEnd(animation: Animator) {
                    shakeCount++
                    if (shakeCount < maxShakes) {
                        val direction = if (shakeCount % 2 == 0) 1 else -1
                        val currentIntensity = intensity * (1f - shakeCount.toFloat() / maxShakes)
                        animate()
                            .translationX(originalX + (currentIntensity * direction))
                            .setDuration(duration / 10)
                            .setListener(this)
                            .start()
                    } else {
                        animate()
                            .translationX(originalX)
                            .setDuration(duration / 20)
                            .setListener(
                                object : AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator) {
                                        onComplete?.invoke()
                                    }
                                },
                            )
                            .start()
                    }
                }
            },
        )
        .start()
}

/**
 * Enum representing slide directions for slide animations
 */
public enum class SlideDirection {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM,
}

/**
 * Creates a rotate animation
 *
 * @param fromDegrees Starting rotation in degrees (default: current rotation)
 * @param toDegrees Target rotation in degrees
 * @param duration Animation duration in milliseconds (default: 300ms)
 * @param onComplete Optional callback when animation completes
 *
 * Example:
 * ```
 * arrowIcon.rotate(toDegrees = 180f, duration = 200L)
 * ```
 */
public fun View.rotate(
    fromDegrees: Float = rotation,
    toDegrees: Float,
    duration: Long = 300L,
    onComplete: (() -> Unit)? = null,
) {
    rotation = fromDegrees

    animate()
        .rotation(toDegrees)
        .setDuration(duration)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .setListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onComplete?.invoke()
                }
            },
        )
        .start()
}


/**
 * Fades in the view with animation
 *
 * @param duration Animation duration in milliseconds (default: 300ms)
 * @param onComplete Optional callback when animation completes
 *
 * Example:
 * ```
 * imageView.fadeIn(500L) {
 *     // Animation completed
 * }
 * ```
 */
public fun View.fadeIn(duration: Long = 300L, onComplete: (() -> Unit)? = null) {
    if (alpha == 1f && isVisible) {
        onComplete?.invoke()
        return
    }

    alpha = 0f
    isVisible = true
    animate()
        .alpha(1f)
        .setDuration(duration)
        .setListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onComplete?.invoke()
                }
            },
        )
        .start()
}

/**
 * Fades out the view with animation
 *
 * @param duration Animation duration in milliseconds (default: 300ms)
 * @param hideOnComplete Whether to set visibility to GONE after animation (default: true)
 * @param onComplete Optional callback when animation completes
 *
 * Example:
 * ```
 * progressBar.fadeOut(200L, hideOnComplete = true) {
 *     // View is now hidden
 * }
 * ```
 */
public fun View.fadeOut(
    duration: Long = 300L,
    hideOnComplete: Boolean = true,
    onComplete: (() -> Unit)? = null,
) {
    if (alpha == 0f || !isVisible) {
        onComplete?.invoke()
    } else {
        animate()
            .alpha(0f)
            .setDuration(duration)
            .setListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (hideOnComplete) {
                            visibility = View.GONE
                        }
                        onComplete?.invoke()
                    }
                },
            )
            .start()
    }
}

/**
 * Toggles visibility with fade animation
 *
 * @param duration Animation duration in milliseconds (default: 300ms)
 * @param onComplete Optional callback when animation completes
 *
 * Example:
 * ```
 * menuView.fadeToggle(400L) {
 *     // Toggle animation completed
 * }
 * ```
 */
public fun View.fadeToggle(duration: Long = 300L, onComplete: (() -> Unit)? = null) {
    if (isVisible && alpha > 0f) {
        fadeOut(duration, true, onComplete)
    } else {
        fadeIn(duration, onComplete)
    }
}

