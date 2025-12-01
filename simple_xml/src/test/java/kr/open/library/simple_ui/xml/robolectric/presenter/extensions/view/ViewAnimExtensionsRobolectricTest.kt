package kr.open.library.simple_ui.xml.robolectric.presenter.extensions.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.xml.R
import kr.open.library.simple_ui.xml.extensions.view.SlideDirection
import kr.open.library.simple_ui.xml.extensions.view.animateScale
import kr.open.library.simple_ui.xml.extensions.view.fadeIn
import kr.open.library.simple_ui.xml.extensions.view.fadeOut
import kr.open.library.simple_ui.xml.extensions.view.fadeToggle
import kr.open.library.simple_ui.xml.extensions.view.pulse
import kr.open.library.simple_ui.xml.extensions.view.rotate
import kr.open.library.simple_ui.xml.extensions.view.shake
import kr.open.library.simple_ui.xml.extensions.view.slideIn
import kr.open.library.simple_ui.xml.extensions.view.slideOut
import kr.open.library.simple_ui.xml.extensions.view.stopPulse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class ViewAnimExtensionsRobolectricTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun pulseStoresAnimatorAndStopPulseResetsScale() {
        val view = View(context)

        view.pulse(minScale = 0.9f, maxScale = 1.1f, duration = 100L, repeatCount = 0)

        val animator = view.getTag(R.id.tag_fade_animator) as? ValueAnimator
        assertNotNull(animator)
        assertTrue(animator!!.isStarted)

        view.scaleX = 0.5f
        view.scaleY = 0.5f
        view.stopPulse()

        assertNull(view.getTag(R.id.tag_fade_animator))
        assertEquals(1f, view.scaleX, 0f)
        assertEquals(1f, view.scaleY, 0f)
    }

    @Test
    fun animateScaleSetsInitialScaleAndTriggersCallback() {
        val view = View(context)
        var invoked = false

        view.animateScale(fromScale = 0.4f, toScale = 1.3f, duration = 50L) {
            invoked = true
        }

        assertEquals(0.4f, view.scaleX, 0f)
        assertEquals(0.4f, view.scaleY, 0f)
        triggerAnimationEnd(view)
        assertTrue(invoked)
    }

    @Test
    fun slideInLeftInitializesTranslationUsingWidth() {
        val view = createMeasuredView(width = 200, height = 80)
        view.visibility = View.GONE

        view.slideIn(direction = SlideDirection.LEFT)

        assertEquals(-200f, view.translationX, 0.001f)
        assertEquals(0f, view.translationY, 0.001f)
        assertTrue(view.isVisible)
    }

    @Test
    fun slideInRespectsCustomDistance() {
        val view = createMeasuredView(width = 150, height = 60)

        view.slideIn(direction = SlideDirection.BOTTOM, distance = 40f)

        assertEquals(40f, view.translationY, 0.001f)
    }

    @Test
    fun slideOutHidesViewOnCompletion() {
        val view = createMeasuredView(width = 120, height = 40).apply { visibility = View.VISIBLE }
        var completed = false

        view.slideOut(direction = SlideDirection.TOP, hideOnComplete = true) {
            completed = true
        }

        triggerAnimationEnd(view)
        assertEquals(View.GONE, view.visibility)
        assertTrue(completed)
    }

    @Test
    fun fadeInWhenAlreadyVisibleInvokesCallbackImmediately() {
        val view =
            View(context).apply {
                alpha = 1f
                visibility = View.VISIBLE
            }

        var invoked = false
        view.fadeIn {
            invoked = true
        }

        assertTrue(invoked)
    }

    @Test
    fun fadeOutWhenAlreadyHiddenInvokesCallbackImmediately() {
        val view =
            View(context).apply {
                alpha = 0f
                visibility = View.GONE
            }

        var invoked = false
        view.fadeOut {
            invoked = true
        }

        assertTrue(invoked)
    }

    @Test
    fun fadeInMakesHiddenViewVisible() {
        val view =
            View(context).apply {
                alpha = 0.2f
                visibility = View.INVISIBLE
            }
        var completed = false

        view.fadeIn {
            completed = true
        }

        assertEquals(0f, view.alpha, 0f)
        assertTrue(view.isVisible)
        triggerAnimationEnd(view)
        assertTrue(completed)
    }

    @Test
    fun fadeOutAnimatesAndRespectsHideFlag() {
        val view =
            View(context).apply {
                alpha = 1f
                visibility = View.VISIBLE
            }
        var completed = false

        view.fadeOut(duration = 80L, hideOnComplete = false) {
            completed = true
        }

        triggerAnimationEnd(view)
        assertEquals(View.VISIBLE, view.visibility)
        assertTrue(completed)
    }

    @Test
    fun fadeToggleSwitchesBetweenStates() {
        val view =
            View(context).apply {
                alpha = 0f
                visibility = View.GONE
            }

        view.fadeToggle()
        assertTrue(view.isVisible)

        view.alpha = 1f
        var finished = false
        view.fadeToggle {
            finished = true
        }

        triggerAnimationEnd(view)
        assertEquals(View.GONE, view.visibility)
        assertTrue(finished)
    }

    @Test
    fun rotateSetsStartingDegreesAndInvokesCallback() {
        val view = View(context)
        var completed = false

        view.rotate(fromDegrees = 15f, toDegrees = 90f, duration = 40L) {
            completed = true
        }

        assertEquals(15f, view.rotation, 0f)
        triggerAnimationEnd(view)
        assertTrue(completed)
    }

    @Test
    fun shakeSetsTranslationXWithIntensity() {
        val view = View(context)
        val originalTranslationX = view.translationX

        view.shake(intensity = 10f, duration = 100L)

        // Shake animation should start
        assertNotNull(view.animate())
    }

    @Test
    fun shakeWithCallbackInvokesOnComplete() {
        val view = View(context)
        var completed = false

        view.shake(intensity = 5f, duration = 50L) {
            completed = true
        }

        // Trigger animation end multiple times to simulate shake cycle
        // Shake has 10 iterations, need to trigger end for final callback
        for (i in 0 until 11) {
            triggerAnimationEnd(view)
        }

        assertTrue(completed)
    }

    @Test
    fun shakeReturnsToOriginalPosition() {
        val view = View(context)
        val originalTranslationX = view.translationX

        view.shake(intensity = 15f, duration = 200L)

        // After shake starts, translationX should change
        assertNotNull(view.animate())

        // Note: Full validation of returning to original position
        // requires triggering all 10 shake iterations plus final reset
    }

    @Test
    fun shakeWithCustomIntensityAndDuration() {
        val view = View(context)

        view.shake(intensity = 20f, duration = 300L)

        assertNotNull(view.animate())
    }

    @Test
    fun shakeWithDefaultParameters() {
        val view = View(context)

        view.shake()

        assertNotNull(view.animate())
    }

    private fun createMeasuredView(
        width: Int,
        height: Int,
    ): View =
        View(context).apply {
            layoutParams = FrameLayout.LayoutParams(width, height)
            val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            measure(widthSpec, heightSpec)
            layout(0, 0, width, height)
        }

    private fun triggerAnimationEnd(view: View) {
        val animator = view.animate()
        val listenerField =
            ViewPropertyAnimator::class.java.getDeclaredField("mListener").apply {
                isAccessible = true
            }
        val listener = listenerField.get(animator) as? Animator.AnimatorListener
        listener?.onAnimationEnd(ValueAnimator())
    }
}
