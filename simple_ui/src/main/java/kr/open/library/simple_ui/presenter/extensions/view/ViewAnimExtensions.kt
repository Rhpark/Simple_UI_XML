package kr.open.library.simple_ui.presenter.extensions.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.view.isVisible


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
