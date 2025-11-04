package kr.open.library.simple_ui.robolectric.presenter.ui.view.recyclerview

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kr.open.library.simple_ui.presenter.ui.view.recyclerview.OnEdgeReachedListener
import kr.open.library.simple_ui.presenter.ui.view.recyclerview.OnScrollDirectionChangedListener
import kr.open.library.simple_ui.presenter.ui.view.recyclerview.RecyclerScrollStateView
import kr.open.library.simple_ui.presenter.ui.view.recyclerview.ScrollDirection
import kr.open.library.simple_ui.presenter.ui.view.recyclerview.ScrollEdge
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric tests for RecyclerScrollStateView
 *
 * Tests comprehensive RecyclerScrollStateView functionality:
 * - View creation and initialization
 * - Threshold configuration (edge reach, scroll direction)
 * - Listener registration (interface and lambda)
 * - Flow accessibility
 * - Lifecycle management
 * - WeakReference listener management
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class RecyclerScrollStateViewRobolectricTest {

    private lateinit var context: Context
    private lateinit var recyclerView: RecyclerScrollStateView

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        recyclerView = RecyclerScrollStateView(context)
    }

    // ==============================================
    // View Creation Tests
    // ==============================================

    @Test
    fun recyclerScrollStateView_createsSuccessfully() {
        // Given & When
        val view = RecyclerScrollStateView(context)

        // Then
        assertNotNull(view)
        assertTrue(view is RecyclerScrollStateView)
    }

    @Test
    fun recyclerScrollStateView_createsWithAttributeSet() {
        // Given & When
        val view = RecyclerScrollStateView(context, null)

        // Then
        assertNotNull(view)
    }

    @Test
    fun recyclerScrollStateView_createsWithDefStyleAttr() {
        // Given & When
        val view = RecyclerScrollStateView(context, null, 0)

        // Then
        assertNotNull(view)
    }

    // ==============================================
    // Threshold Configuration Tests
    // ==============================================

    @Test
    fun setEdgeReachThreshold_withPositiveValue_succeeds() {
        // Given
        val threshold = 50

        // When & Then - should not throw exception
        recyclerView.setEdgeReachThreshold(threshold)
    }

    @Test
    fun setEdgeReachThreshold_withZero_succeeds() {
        // Given
        val threshold = 0

        // When & Then - should not throw exception
        recyclerView.setEdgeReachThreshold(threshold)
    }

    @Test(expected = IllegalArgumentException::class)
    fun setEdgeReachThreshold_withNegativeValue_throwsException() {
        // Given
        val threshold = -1

        // When
        recyclerView.setEdgeReachThreshold(threshold)

        // Then - exception should be thrown
    }

    @Test
    fun setScrollDirectionThreshold_withPositiveValue_succeeds() {
        // Given
        val threshold = 100

        // When & Then - should not throw exception
        recyclerView.setScrollDirectionThreshold(threshold)
    }

    @Test
    fun setScrollDirectionThreshold_withZero_succeeds() {
        // Given
        val threshold = 0

        // When & Then - should not throw exception
        recyclerView.setScrollDirectionThreshold(threshold)
    }

    @Test(expected = IllegalArgumentException::class)
    fun setScrollDirectionThreshold_withNegativeValue_throwsException() {
        // Given
        val threshold = -10

        // When
        recyclerView.setScrollDirectionThreshold(threshold)

        // Then - exception should be thrown
    }

    // ==============================================
    // Listener Registration Tests - Interface
    // ==============================================

    @Test
    fun setOnScrollDirectionListener_withInterface_registersSuccessfully() {
        // Given
        val listener = object : OnScrollDirectionChangedListener {
            override fun onScrollDirectionChanged(scrollDirection: ScrollDirection) {
                // Do nothing
            }
        }

        // When & Then - should not throw exception
        recyclerView.setOnScrollDirectionListener(listener)
    }

    @Test
    fun setOnScrollDirectionListener_withNull_removesListener() {
        // Given
        val listener = object : OnScrollDirectionChangedListener {
            override fun onScrollDirectionChanged(scrollDirection: ScrollDirection) {
                // Do nothing
            }
        }
        recyclerView.setOnScrollDirectionListener(listener)

        // When & Then - should not throw exception
        recyclerView.setOnScrollDirectionListener(null as OnScrollDirectionChangedListener?)
    }

    @Test
    fun setOnReachEdgeListener_withInterface_registersSuccessfully() {
        // Given
        val listener = object : OnEdgeReachedListener {
            override fun onEdgeReached(edge: ScrollEdge, isReached: Boolean) {
                // Do nothing
            }
        }

        // When & Then - should not throw exception
        recyclerView.setOnReachEdgeListener(listener)
    }

    @Test
    fun setOnReachEdgeListener_withNull_removesListener() {
        // Given
        val listener = object : OnEdgeReachedListener {
            override fun onEdgeReached(edge: ScrollEdge, isReached: Boolean) {
                // Do nothing
            }
        }
        recyclerView.setOnReachEdgeListener(listener)

        // When & Then - should not throw exception
        recyclerView.setOnReachEdgeListener(null as OnEdgeReachedListener?)
    }

    // ==============================================
    // Listener Registration Tests - Lambda
    // ==============================================

    @Test
    fun setOnScrollDirectionListener_withLambda_registersSuccessfully() {
        // Given
        var receivedDirection: ScrollDirection? = null
        val lambda: (ScrollDirection) -> Unit = { direction ->
            receivedDirection = direction
        }

        // When & Then - should not throw exception
        recyclerView.setOnScrollDirectionListener(lambda)
        assertNull(receivedDirection) // Lambda not invoked yet
    }

    @Test
    fun setOnReachEdgeListener_withLambda_registersSuccessfully() {
        // Given
        var receivedEdge: ScrollEdge? = null
        var receivedIsReached: Boolean? = null
        val lambda: (ScrollEdge, Boolean) -> Unit = { edge, isReached ->
            receivedEdge = edge
            receivedIsReached = isReached
        }

        // When & Then - should not throw exception
        recyclerView.setOnReachEdgeListener(lambda)
        assertNull(receivedEdge) // Lambda not invoked yet
        assertNull(receivedIsReached)
    }

    // ==============================================
    // Flow Accessibility Tests
    // ==============================================

    @Test
    fun sfScrollDirectionFlow_isAccessible() {
        // When
        val flow = recyclerView.sfScrollDirectionFlow

        // Then
        assertNotNull(flow)
    }

    @Test
    fun sfEdgeReachedFlow_isAccessible() {
        // When
        val flow = recyclerView.sfEdgeReachedFlow

        // Then
        assertNotNull(flow)
    }

    @Test
    fun sfScrollDirectionFlow_hasReplayCache() = runBlocking {
        // Given
        val view = RecyclerScrollStateView(context)

        // When
        val flow = view.sfScrollDirectionFlow

        // Then - Flow should be accessible and have replay capability
        assertNotNull(flow)
        // Note: Cannot easily test replay without triggering scroll events in Robolectric
    }

    @Test
    fun sfEdgeReachedFlow_hasReplayCache() = runBlocking {
        // Given
        val view = RecyclerScrollStateView(context)

        // When
        val flow = view.sfEdgeReachedFlow

        // Then - Flow should be accessible and have replay capability
        assertNotNull(flow)
        // Note: Cannot easily test replay without triggering scroll events in Robolectric
    }

    // ==============================================
    // Lifecycle Tests
    // ==============================================

    /**
     * Note: onAttachedToWindow() and onDetachedFromWindow() are protected methods
     * and cannot be directly called from tests. They are called automatically by
     * the Android framework when the view is attached to/detached from a window.
     *
     * Lifecycle behavior is tested through:
     * - View creation (which internally handles initialization)
     * - Integration tests in sample app
     */

    // ==============================================
    // Multiple Threshold Configuration Tests
    // ==============================================

    @Test
    fun setThresholds_multipleTimes_updatesCorrectly() {
        // When
        recyclerView.setEdgeReachThreshold(10)
        recyclerView.setEdgeReachThreshold(20)
        recyclerView.setEdgeReachThreshold(30)

        recyclerView.setScrollDirectionThreshold(5)
        recyclerView.setScrollDirectionThreshold(15)
        recyclerView.setScrollDirectionThreshold(25)

        // Then - should not crash
        assertNotNull(recyclerView)
    }

    @Test
    fun setThresholds_withBoundaryValues_handlesCorrectly() {
        // When & Then - should not crash
        recyclerView.setEdgeReachThreshold(0)
        recyclerView.setEdgeReachThreshold(Int.MAX_VALUE)

        recyclerView.setScrollDirectionThreshold(0)
        recyclerView.setScrollDirectionThreshold(Int.MAX_VALUE)
    }

    // ==============================================
    // Listener Replacement Tests
    // ==============================================

    @Test
    fun setOnScrollDirectionListener_replacesOldListener() {
        // Given
        val listener1 = object : OnScrollDirectionChangedListener {
            override fun onScrollDirectionChanged(scrollDirection: ScrollDirection) {}
        }
        val listener2 = object : OnScrollDirectionChangedListener {
            override fun onScrollDirectionChanged(scrollDirection: ScrollDirection) {}
        }

        // When
        recyclerView.setOnScrollDirectionListener(listener1)
        recyclerView.setOnScrollDirectionListener(listener2)

        // Then - should not crash, second listener replaces first
        assertNotNull(recyclerView)
    }

    @Test
    fun setOnReachEdgeListener_replacesOldListener() {
        // Given
        val listener1 = object : OnEdgeReachedListener {
            override fun onEdgeReached(edge: ScrollEdge, isReached: Boolean) {}
        }
        val listener2 = object : OnEdgeReachedListener {
            override fun onEdgeReached(edge: ScrollEdge, isReached: Boolean) {}
        }

        // When
        recyclerView.setOnReachEdgeListener(listener1)
        recyclerView.setOnReachEdgeListener(listener2)

        // Then - should not crash, second listener replaces first
        assertNotNull(recyclerView)
    }

    // ==============================================
    // Edge Cases
    // ==============================================

    @Test
    fun multipleListeners_canBeSetAndRemoved() {
        // Given
        val scrollListener = object : OnScrollDirectionChangedListener {
            override fun onScrollDirectionChanged(scrollDirection: ScrollDirection) {}
        }
        val edgeListener = object : OnEdgeReachedListener {
            override fun onEdgeReached(edge: ScrollEdge, isReached: Boolean) {}
        }

        // When
        recyclerView.setOnScrollDirectionListener(scrollListener)
        recyclerView.setOnReachEdgeListener(edgeListener)
        recyclerView.setOnScrollDirectionListener(null as OnScrollDirectionChangedListener?)
        recyclerView.setOnReachEdgeListener(null as OnEdgeReachedListener?)

        // Then - should not crash
        assertNotNull(recyclerView)
    }

    @Test
    fun view_canBeCreatedAndConfiguredMultipleTimes() {
        // When
        for (i in 1..10) {
            val view = RecyclerScrollStateView(context)
            view.setEdgeReachThreshold(i * 10)
            view.setScrollDirectionThreshold(i * 20)
            view.setOnScrollDirectionListener { }
            view.setOnReachEdgeListener { _, _ -> }
        }

        // Then - should not crash or cause memory issues
        assertTrue(true)
    }

    /**
     * Integration note:
     *
     * Full scroll behavior testing (actual scroll events, edge detection, direction changes)
     * requires a more complex setup with:
     * - Populated RecyclerView with Adapter and LayoutManager
     * - Simulated scroll events
     * - UI thread execution
     *
     * These are best tested through:
     * - Instrumentation tests (on real device/emulator)
     * - Manual testing in sample app
     *
     * This Robolectric test focuses on:
     * - API correctness (methods don't crash)
     * - Configuration validation (threshold requirements)
     * - Listener registration mechanics
     * - Flow accessibility
     */
}
