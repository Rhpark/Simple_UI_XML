package kr.open.library.simple_ui.xml.robolectric.ui.layout.base.bind.retry

import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import kr.open.library.simple_ui.xml.ui.layout.base.bind.retry.LayoutLifecycleBindRetry
import kr.open.library.simple_ui.xml.ui.layout.base.bind.retry.LayoutLifecycleBindRetryCallbacks
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import java.time.Duration

/**
 * Robolectric tests for layout lifecycle binding retries.<br><br>
 * Layout lifecycle 바인딩 재시도 흐름을 검증하는 Robolectric 테스트입니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
class LayoutLifecycleBindRetryRobolectricTest {
    @Test
    fun start_whenInitialBindingSucceeds_doesNotRetry() {
        withAttachedView { activity, view ->
            val callbacks = TrackingCallbacks(activity, successAttempt = 1)
            val retry = createRetry(view, callbacks)

            retry.start()
            idleMainLooper()

            assertEquals(1, callbacks.attemptCount)
        }
    }

    @Test
    fun start_whenOwnerBecomesAvailable_retriesUntilSuccess() {
        withAttachedView { activity, view ->
            val callbacks = TrackingCallbacks(activity, successAttempt = 3)
            val retry = createRetry(view, callbacks)

            retry.start()
            idleMainLooper()

            assertEquals(3, callbacks.attemptCount)
        }
    }

    @Test
    fun start_whenBindingAlwaysFails_respectsMaximumDelayedRetryCount() {
        withAttachedView { activity, view ->
            val callbacks = TrackingCallbacks(activity, successAttempt = null)
            val retry = createRetry(view, callbacks, maxRetry = 2)

            retry.start()
            idleMainLooper()

            assertEquals(3, callbacks.attemptCount)
        }
    }

    @Test
    fun start_whenMaximumRetryIsZero_doesNotScheduleDelayedRetry() {
        withAttachedView { activity, view ->
            val callbacks = TrackingCallbacks(activity, successAttempt = null)
            val retry = createRetry(view, callbacks, maxRetry = 0)

            retry.start()
            idleMainLooper()

            assertEquals(1, callbacks.attemptCount)
        }
    }

    @Test
    fun start_whenCalledTwice_doesNotDuplicateBindingFlow() {
        withAttachedView { activity, view ->
            val callbacks = TrackingCallbacks(activity, successAttempt = 1)
            val retry = createRetry(view, callbacks)

            retry.start()
            retry.start()
            idleMainLooper()

            assertEquals(1, callbacks.attemptCount)
        }
    }

    @Test
    fun cancel_removesPendingRetryAndAllowsRestart() {
        withAttachedView { activity, view ->
            val callbacks = TrackingCallbacks(activity, successAttempt = null)
            val retry = createRetry(view, callbacks)

            retry.start()
            retry.cancel()
            idleMainLooper()
            assertEquals(1, callbacks.attemptCount)

            retry.start()
            retry.cancel()
            assertEquals(2, callbacks.attemptCount)
        }
    }

    @Test
    fun pendingRetry_whenViewDetaches_doesNotBindAgain() {
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        try {
            val activity = activityController.get()
            val container = FrameLayout(activity)
            activity.setContentView(container)
            val view = View(activity)
            container.addView(view)
            layout(view)
            val callbacks = TrackingCallbacks(activity, successAttempt = null)
            val retry = createRetry(view, callbacks)

            retry.start()
            container.removeView(view)
            idleMainLooper()

            assertEquals(1, callbacks.attemptCount)
        } finally {
            activityController.destroy()
        }
    }

    private fun createRetry(
        view: View,
        callbacks: TrackingCallbacks,
        maxRetry: Int = 3,
    ): LayoutLifecycleBindRetry = LayoutLifecycleBindRetry(
        view = view,
        callbacks = callbacks,
        maxRetry = maxRetry,
        retryDelayMs = RETRY_DELAY_MS,
    )

    private fun withAttachedView(block: (FragmentActivity, View) -> Unit) {
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        try {
            val activity = activityController.get()
            val container = FrameLayout(activity)
            activity.setContentView(container)
            val view = View(activity)
            container.addView(view)
            layout(view)

            block(activity, view)
        } finally {
            activityController.destroy()
        }
    }

    private fun layout(view: View) {
        view.layout(0, 0, 10, 10)
        view.viewTreeObserver.dispatchOnGlobalLayout()
    }

    private fun idleMainLooper() {
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1))
    }

    private class TrackingCallbacks(
        private val owner: LifecycleOwner,
        private val successAttempt: Int?,
    ) : LayoutLifecycleBindRetryCallbacks {
        var attemptCount: Int = 0

        override fun bind(): LifecycleOwner? {
            attemptCount++
            return if (successAttempt != null && attemptCount >= successAttempt) owner else null
        }
    }

    private companion object {
        const val RETRY_DELAY_MS = 10L
    }
}
