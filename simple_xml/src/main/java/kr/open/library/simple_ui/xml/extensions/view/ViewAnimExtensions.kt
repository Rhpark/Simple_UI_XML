/**
 * View animation extension functions for various animation effects.<br>
 * Provides convenient methods for scale, fade, slide, rotate, pulse, and shake animations.<br><br>
 * 다양한 애니메이션 효과를 위한 View 애니메이션 확장 함수입니다.<br>
 * 스케일, 페이드, 슬라이드, 회전, 펄스 및 흔들기 애니메이션을 위한 편리한 메서드를 제공합니다.<br>
 *
 * Example usage:<br>
 * ```kotlin
 * // Scale animation
 * button.animateScale(toScale = 1.2f, duration = 150L)
 *
 * // Pulse effect
 * heartIcon.pulse(minScale = 0.9f, maxScale = 1.1f)
 * heartIcon.stopPulse()
 *
 * // Slide animations
 * panel.slideIn(SlideDirection.RIGHT, duration = 250L)
 * panel.slideOut(SlideDirection.LEFT, hideOnComplete = true)
 *
 * // Shake animation
 * errorField.shake(intensity = 15f)
 *
 * // Rotate animation
 * arrowIcon.rotate(toDegrees = 180f)
 *
 * // Fade animations
 * imageView.fadeIn(500L)
 * progressBar.fadeOut(200L, hideOnComplete = true)
 * menuView.fadeToggle(400L)
 * ```
 */
package kr.open.library.simple_ui.xml.extensions.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.view.isVisible

/**
 * Animates the view's scale with customizable parameters.<br><br>
 * 커스터마이징 가능한 매개변수로 View의 스케일을 애니메이션합니다.<br>
 *
 * @param fromScale Starting scale value (default: current scale).<br><br>
 *                  시작 스케일 값 (기본값: 현재 스케일).<br>
 *
 * @param toScale Target scale value.<br><br>
 *                대상 스케일 값.<br>
 *
 * @param duration Animation duration in milliseconds (default: 300ms).<br><br>
 *                 애니메이션 지속 시간(밀리초) (기본값: 300ms).<br>
 *
 * @param onComplete Optional callback when animation completes.<br><br>
 *                   애니메이션 완료 시 실행할 선택적 콜백.<br>
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
        ).start()
}

/**
 * Creates a pulsing animation effect.<br><br>
 * 펄스 애니메이션 효과를 생성합니다.<br>
 *
 * @param minScale Minimum scale value (default: 0.95f).<br><br>
 *                 최소 스케일 값 (기본값: 0.95f).<br>
 *
 * @param maxScale Maximum scale value (default: 1.05f).<br><br>
 *                 최대 스케일 값 (기본값: 1.05f).<br>
 *
 * @param duration Duration for one complete pulse cycle in milliseconds (default: 1000ms).<br><br>
 *                 한 번의 완전한 펄스 주기 지속 시간(밀리초) (기본값: 1000ms).<br>
 *
 * @param repeatCount Number of times to repeat (-1 for infinite, default: -1).<br><br>
 *                    반복 횟수 (무한 반복은 -1, 기본값: -1).<br>
 */
public fun View.pulse(
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f,
    duration: Long = 1000L,
    repeatCount: Int = ValueAnimator.INFINITE,
) {
    (getTag(ViewIds.FADE_ANIMATOR) as? ValueAnimator)?.cancel()

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
 * Stops any pulsing animation on this view.<br><br>
 * 이 View의 펄스 애니메이션을 중지합니다.<br>
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
 * Animates view sliding in from a specific direction.<br><br>
 * 특정 방향에서 View가 슬라이드 인되는 애니메이션을 실행합니다.<br>
 *
 * @param direction Direction to slide from (LEFT, RIGHT, TOP, BOTTOM).<br><br>
 *                  슬라이드할 방향 (LEFT, RIGHT, TOP, BOTTOM).<br>
 *
 * @param distance Distance to slide in pixels (default: view width/height).<br><br>
 *               슬라이드 거리(픽셀) (기본값: view의 너비/높이).<br>
 *
 * @param duration Animation duration in milliseconds (default: 300ms).<br><br>
 *                 애니메이션 지속 시간(밀리초) (기본값: 300ms).<br>
 *
 * @param onComplete Optional callback when animation completes.<br><br>
 *                   애니메이션 완료 시 실행할 선택적 콜백.<br>
 */
public fun View.slideIn(
    direction: SlideDirection,
    distance: Float = 0f,
    duration: Long = 300L,
    onComplete: (() -> Unit)? = null,
) {
    val actualDistance = resolveSlideDistance(direction, distance)

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
        ).start()
}

/**
 * Animates view sliding out to a specific direction.<br><br>
 * 특정 방향으로 View가 슬라이드 아웃되는 애니메이션을 실행합니다.<br>
 *
 * @param direction Direction to slide to (LEFT, RIGHT, TOP, BOTTOM).<br><br>
 *                  슬라이드할 방향 (LEFT, RIGHT, TOP, BOTTOM).<br>
 *
 * @param distance Distance to slide in pixels (default: view width/height).<br><br>
 *               슬라이드 거리(픽셀) (기본값: view의 너비/높이).<br>
 *
 * @param duration Animation duration in milliseconds (default: 300ms).<br><br>
 *                 애니메이션 지속 시간(밀리초) (기본값: 300ms).<br>
 *
 * @param hideOnComplete Whether to set visibility to GONE after animation (default: true).<br><br>
 *                       애니메이션 후 가시성을 GONE으로 설정할지 여부 (기본값: true).<br>
 *
 * @param onComplete Optional callback when animation completes.<br><br>
 *                   애니메이션 완료 시 실행할 선택적 콜백.<br>
 */
public fun View.slideOut(
    direction: SlideDirection,
    distance: Float = 0f,
    duration: Long = 300L,
    hideOnComplete: Boolean = true,
    onComplete: (() -> Unit)? = null,
) {
    val actualDistance = resolveSlideDistance(direction, distance)

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
        ).start()
}

/**
 * Creates a shake animation effect.<br><br>
 * 흔들기 애니메이션 효과를 생성합니다.<br>
 *
 * @param intensity Shake intensity in pixels (default: 10f).<br><br>
 *                  흔들기 강도(픽셀) (기본값: 10f).<br>
 *
 * @param duration Duration of the shake animation in milliseconds (default: 500ms).<br><br>
 *                 흔들기 애니메이션 지속 시간(밀리초) (기본값: 500ms).<br>
 *
 * @param onComplete Optional callback when animation completes.<br><br>
 *                   애니메이션 완료 시 실행할 선택적 콜백.<br>
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
                            ).start()
                    }
                }
            },
        ).start()
}

/**
 * Enum representing slide directions for slide animations.<br><br>
 * 슬라이드 애니메이션의 방향을 나타내는 열거형입니다.<br>
 */
public enum class SlideDirection {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM,
}

/**
 * Creates a rotate animation.<br><br>
 * 회전 애니메이션을 생성합니다.<br>
 *
 * @param fromDegrees Starting rotation in degrees (default: current rotation).<br><br>
 *                    시작 회전 각도(도) (기본값: 현재 회전).<br>
 *
 * @param toDegrees Target rotation in degrees.<br><br>
 *                  대상 회전 각도(도).<br>
 *
 * @param duration Animation duration in milliseconds (default: 300ms).<br><br>
 *                 애니메이션 지속 시간(밀리초) (기본값: 300ms).<br>
 *
 * @param onComplete Optional callback when animation completes.<br><br>
 *                   애니메이션 완료 시 실행할 선택적 콜백.<br>
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
        ).start()
}

/**
 * Fades in the view with animation.<br><br>
 * 애니메이션과 함께 View를 페이드 인합니다.<br>
 *
 * @param duration Animation duration in milliseconds (default: 300ms).<br><br>
 *                 애니메이션 지속 시간(밀리초) (기본값: 300ms).<br>
 *
 * @param onComplete Optional callback when animation completes.<br><br>
 *                   애니메이션 완료 시 실행할 선택적 콜백.<br>
 */
public fun View.fadeIn(
    duration: Long = 300L,
    onComplete: (() -> Unit)? = null,
) {
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
        ).start()
}

/**
 * Fades out the view with animation.<br><br>
 * 애니메이션과 함께 View를 페이드 아웃합니다.<br>
 *
 * @param duration Animation duration in milliseconds (default: 300ms).<br><br>
 *                 애니메이션 지속 시간(밀리초) (기본값: 300ms).<br>
 *
 * @param hideOnComplete Whether to set visibility to GONE after animation (default: true).<br><br>
 *                       애니메이션 후 가시성을 GONE으로 설정할지 여부 (기본값: true).<br>
 *
 * @param onComplete Optional callback when animation completes.<br><br>
 *                   애니메이션 완료 시 실행할 선택적 콜백.<br>
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
            ).start()
    }
}

/**
 * Toggles visibility with fade animation.<br><br>
 * 페이드 애니메이션과 함께 가시성을 토글합니다.<br>
 *
 * @param duration Animation duration in milliseconds (default: 300ms).<br><br>
 *                 애니메이션 지속 시간(밀리초) (기본값: 300ms).<br>
 *
 * @param onComplete Optional callback when animation completes.<br><br>
 *                   애니메이션 완료 시 실행할 선택적 콜백.<br>
 */
public fun View.fadeToggle(
    duration: Long = 300L,
    onComplete: (() -> Unit)? = null,
) {
    if (isVisible && alpha > 0f) {
        fadeOut(duration, true, onComplete)
    } else {
        fadeIn(duration, onComplete)
    }
}

private fun View.resolveSlideDistance(
    direction: SlideDirection,
    requestedDistance: Float,
): Float {
    if (requestedDistance != 0f) return requestedDistance

    val layoutDistance =
        when (direction) {
            SlideDirection.LEFT, SlideDirection.RIGHT -> width
            SlideDirection.TOP, SlideDirection.BOTTOM -> height
        }
    if (layoutDistance > 0) return layoutDistance.toFloat()

    val measuredDistance =
        when (direction) {
            SlideDirection.LEFT, SlideDirection.RIGHT -> measuredWidth
            SlideDirection.TOP, SlideDirection.BOTTOM -> measuredHeight
        }
    if (measuredDistance > 0) return measuredDistance.toFloat()

    val density = resources.displayMetrics.density
    return if (density > 0f) 56f * density else 56f
}
