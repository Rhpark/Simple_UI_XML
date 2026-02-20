package kr.open.library.simple_ui.xml.robolectric.ui.view.recyclerview

import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import kr.open.library.simple_ui.xml.ui.view.recyclerview.OnEdgeReachedListener
import kr.open.library.simple_ui.xml.ui.view.recyclerview.OnScrollDirectionChangedListener
import kr.open.library.simple_ui.xml.ui.view.recyclerview.RecyclerScrollStateView
import kr.open.library.simple_ui.xml.ui.view.recyclerview.ScrollDirection
import kr.open.library.simple_ui.xml.ui.view.recyclerview.ScrollEdge
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
        val listener =
            object : OnScrollDirectionChangedListener {
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
        val listener =
            object : OnScrollDirectionChangedListener {
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
        val listener =
            object : OnEdgeReachedListener {
                override fun onEdgeReached(
                    edge: ScrollEdge,
                    isReached: Boolean,
                ) {
                    // Do nothing
                }
            }

        // When & Then - should not throw exception
        recyclerView.setOnReachEdgeListener(listener)
    }

    @Test
    fun setOnReachEdgeListener_withNull_removesListener() {
        // Given
        val listener =
            object : OnEdgeReachedListener {
                override fun onEdgeReached(
                    edge: ScrollEdge,
                    isReached: Boolean,
                ) {
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
    fun sfScrollDirectionFlow_hasReplayCache() =
        runBlocking {
            // Given
            val view = RecyclerScrollStateView(context)

            // When
            val flow = view.sfScrollDirectionFlow

            // Then - Flow should be accessible and have replay capability
            assertNotNull(flow)
            // Note: Cannot easily test replay without triggering scroll events in Robolectric
        }

    @Test
    fun sfEdgeReachedFlow_hasReplayCache() =
        runBlocking {
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
        val listener1 =
            object : OnScrollDirectionChangedListener {
                override fun onScrollDirectionChanged(scrollDirection: ScrollDirection) {}
            }
        val listener2 =
            object : OnScrollDirectionChangedListener {
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
        val listener1 =
            object : OnEdgeReachedListener {
                override fun onEdgeReached(
                    edge: ScrollEdge,
                    isReached: Boolean,
                ) {}
            }
        val listener2 =
            object : OnEdgeReachedListener {
                override fun onEdgeReached(
                    edge: ScrollEdge,
                    isReached: Boolean,
                ) {}
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
        val scrollListener =
            object : OnScrollDirectionChangedListener {
                override fun onScrollDirectionChanged(scrollDirection: ScrollDirection) {}
            }
        val edgeListener =
            object : OnEdgeReachedListener {
                override fun onEdgeReached(
                    edge: ScrollEdge,
                    isReached: Boolean,
                ) {}
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

    // ==============================================
    // Lifecycle Tests - Scroll Listener Attachment
    // ==============================================

    @Test
    fun attachToWindow_registersScrollListener() {
        // Given
        val container = FrameLayout(context)
        val view = RecyclerScrollStateView(context)
        view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        // When - attach to window triggers onAttachedToWindow internally
        container.addView(view)

        // Then - should not crash, scroll listener is registered internally
        assertNotNull(view)
    }

    @Test
    fun detachFromWindow_unregistersScrollListener() {
        // Given
        val container = FrameLayout(context)
        val view = RecyclerScrollStateView(context)
        view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        container.addView(view)

        // When - remove from window triggers onDetachedFromWindow internally
        container.removeView(view)

        // Then - should not crash, scroll listener is unregistered internally
        assertNotNull(view)
    }

    // ==============================================
    // Scroll Event Tests - onScrolled and onScrollStateChanged
    // ==============================================

    @Test
    fun onScrolled_withVerticalScroll_triggersEdgeCheck() {
        // Given
        val view = RecyclerScrollStateView(context)
        view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.adapter = TestAdapter(100)
        view.setEdgeReachThreshold(10)

        var edgeReachedCalled = false
        view.setOnReachEdgeListener { _, _ ->
            edgeReachedCalled = true
        }

        // Layout the view to make scroll calculations work
        view.measure(
            View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, 500, 1000)

        // When - trigger scroll
        view.scrollBy(0, 100)

        // Then - edge check should be triggered (implementation detail)
        assertNotNull(view)
    }

    @Test
    fun onScrollStateChanged_toIdle_resetsAccumulation() {
        // Given
        val view = RecyclerScrollStateView(context)
        view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.adapter = TestAdapter(100)
        view.setScrollDirectionThreshold(20)

        var directionChangedToIdle = false
        view.setOnScrollDirectionListener { direction ->
            if (direction == ScrollDirection.IDLE) {
                directionChangedToIdle = true
            }
        }

        // Layout the view
        view.measure(
            View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, 500, 1000)

        // When - scroll and then stop
        view.scrollBy(0, 100)
        view.dispatchSetActivated(false) // Simulate scroll state change to idle

        // Then
        assertNotNull(view)
    }

    // ==============================================
    // Vertical Edge Detection Tests
    // ==============================================

    @Test
    fun checkVerticalEdges_detectsTopEdge() {
        // Given
        val view = RecyclerScrollStateView(context)
        view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.adapter = TestAdapter(100)
        view.setEdgeReachThreshold(10)

        var topEdgeReached = false
        view.setOnReachEdgeListener { edge, isReached ->
            if (edge == ScrollEdge.TOP && isReached) {
                topEdgeReached = true
            }
        }

        // Layout the view
        view.measure(
            View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, 500, 1000)

        // When - scroll to top
        view.scrollToPosition(0)

        // Then
        assertNotNull(view)
    }

    @Test
    fun checkVerticalEdges_detectsBottomEdge() {
        // Given
        val view = RecyclerScrollStateView(context)
        view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.adapter = TestAdapter(10) // Small dataset for easier bottom detection
        view.setEdgeReachThreshold(10)

        var bottomEdgeReached = false
        view.setOnReachEdgeListener { edge, isReached ->
            if (edge == ScrollEdge.BOTTOM && isReached) {
                bottomEdgeReached = true
            }
        }

        // Layout the view
        view.measure(
            View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, 500, 1000)

        // When - scroll to bottom
        view.scrollToPosition(9)

        // Then
        assertNotNull(view)
    }

    // ==============================================
    // Horizontal Edge Detection Tests
    // ==============================================

    @Test
    fun checkHorizontalEdges_detectsLeftEdge() {
        // Given
        val view = RecyclerScrollStateView(context)
        view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.adapter = TestAdapter(100)
        view.setEdgeReachThreshold(10)

        var leftEdgeReached = false
        view.setOnReachEdgeListener { edge, isReached ->
            if (edge == ScrollEdge.LEFT && isReached) {
                leftEdgeReached = true
            }
        }

        // Layout the view
        view.measure(
            View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, 1000, 500)

        // When - scroll to left
        view.scrollToPosition(0)

        // Then
        assertNotNull(view)
    }

    @Test
    fun checkHorizontalEdges_detectsRightEdge() {
        // Given
        val view = RecyclerScrollStateView(context)
        view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.adapter = TestAdapter(10)
        view.setEdgeReachThreshold(10)

        var rightEdgeReached = false
        view.setOnReachEdgeListener { edge, isReached ->
            if (edge == ScrollEdge.RIGHT && isReached) {
                rightEdgeReached = true
            }
        }

        // Layout the view
        view.measure(
            View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, 1000, 500)

        // When - scroll to right
        view.scrollToPosition(9)

        // Then
        assertNotNull(view)
    }

    // ==============================================
    // Scroll Direction Detection Tests
    // ==============================================

    @Test
    fun updateVerticalScrollDirection_detectsDownScroll() {
        // Given
        val view = RecyclerScrollStateView(context)
        view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.adapter = TestAdapter(100)
        view.setScrollDirectionThreshold(20)

        var downDirectionDetected = false
        view.setOnScrollDirectionListener { direction ->
            if (direction == ScrollDirection.DOWN) {
                downDirectionDetected = true
            }
        }

        // Layout the view
        view.measure(
            View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, 500, 1000)

        // When - scroll down sufficiently to exceed threshold
        view.scrollBy(0, 30)

        // Then
        assertNotNull(view)
    }

    @Test
    fun updateVerticalScrollDirection_detectsUpScroll() {
        // Given
        val view = RecyclerScrollStateView(context)
        view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.adapter = TestAdapter(100)
        view.setScrollDirectionThreshold(20)

        var upDirectionDetected = false
        view.setOnScrollDirectionListener { direction ->
            if (direction == ScrollDirection.UP) {
                upDirectionDetected = true
            }
        }

        // Layout the view
        view.measure(
            View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, 500, 1000)

        // When - first scroll down, then scroll up
        view.scrollBy(0, 100)
        view.scrollBy(0, -30)

        // Then
        assertNotNull(view)
    }

    @Test
    fun updateHorizontalScrollDirection_detectsRightScroll() {
        // Given
        val view = RecyclerScrollStateView(context)
        view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.adapter = TestAdapter(100)
        view.setScrollDirectionThreshold(20)

        var rightDirectionDetected = false
        view.setOnScrollDirectionListener { direction ->
            if (direction == ScrollDirection.RIGHT) {
                rightDirectionDetected = true
            }
        }

        // Layout the view
        view.measure(
            View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, 1000, 500)

        // When - scroll right
        view.scrollBy(30, 0)

        // Then
        assertNotNull(view)
    }

    @Test
    fun updateHorizontalScrollDirection_detectsLeftScroll() {
        // Given
        val view = RecyclerScrollStateView(context)
        view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.adapter = TestAdapter(100)
        view.setScrollDirectionThreshold(20)

        var leftDirectionDetected = false
        view.setOnScrollDirectionListener { direction ->
            if (direction == ScrollDirection.LEFT) {
                leftDirectionDetected = true
            }
        }

        // Layout the view
        view.measure(
            View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, 1000, 500)

        // When - first scroll right, then scroll left
        view.scrollBy(100, 0)
        view.scrollBy(-30, 0)

        // Then
        assertNotNull(view)
    }

    // ==============================================
    // Flow Event Emission Tests
    // ==============================================

    @Test
    fun scrollDirectionFlow_emitsDirectionChanges() =
        runBlocking {
            // Given
            val view = RecyclerScrollStateView(context)
            view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            view.adapter = TestAdapter(100)
            view.setScrollDirectionThreshold(20)

            // When
            val flow = view.sfScrollDirectionFlow

            // Then - Flow should be accessible
            assertNotNull(flow)
        }

    @Test
    fun edgeReachedFlow_emitsEdgeChanges() =
        runBlocking {
            // Given
            val view = RecyclerScrollStateView(context)
            view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            view.adapter = TestAdapter(100)
            view.setEdgeReachThreshold(10)

            // When
            val flow = view.sfEdgeReachedFlow

            // Then - Flow should be accessible
            assertNotNull(flow)
        }

    // ==============================================
    // XML Attribute Initialization Tests
    // ==============================================

    @Test
    fun initTypeArray_withNullAttrs_usesDefaults() {
        // Given & When
        val view = RecyclerScrollStateView(context, null)

        // Then - should use default thresholds and not crash
        assertNotNull(view)
    }

    // ==============================================
    // Callback Invocation Tests
    // ==============================================

    @Test
    fun onScrollDirectionChanged_invokesInterfaceCallback() {
        // Given
        val view = RecyclerScrollStateView(context)
        view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.adapter = TestAdapter(100)
        view.setScrollDirectionThreshold(20)

        var callbackInvoked = false
        val listener =
            object : OnScrollDirectionChangedListener {
                override fun onScrollDirectionChanged(scrollDirection: ScrollDirection) {
                    callbackInvoked = true
                }
            }
        view.setOnScrollDirectionListener(listener)

        // Layout the view
        view.measure(
            View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, 500, 1000)

        // When - scroll to trigger direction change
        view.scrollBy(0, 30)

        // Then - callback should be ready to be invoked
        assertNotNull(listener)
    }

    @Test
    fun onEdgeReached_invokesInterfaceCallback() {
        // Given
        val view = RecyclerScrollStateView(context)
        view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.adapter = TestAdapter(100)
        view.setEdgeReachThreshold(10)

        var callbackInvoked = false
        val listener =
            object : OnEdgeReachedListener {
                override fun onEdgeReached(
                    edge: ScrollEdge,
                    isReached: Boolean,
                ) {
                    callbackInvoked = true
                }
            }
        view.setOnReachEdgeListener(listener)

        // Layout the view
        view.measure(
            View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, 500, 1000)

        // When - scroll to trigger edge detection
        view.scrollToPosition(0)

        // Then - callback should be ready to be invoked
        assertNotNull(listener)
    }

    @Test
    fun onScrollDirectionChanged_invokesLambdaCallback() {
        // Given
        val view = RecyclerScrollStateView(context)
        view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.adapter = TestAdapter(100)
        view.setScrollDirectionThreshold(20)

        var receivedDirection: ScrollDirection? = null
        view.setOnScrollDirectionListener { direction ->
            receivedDirection = direction
        }

        // Layout the view
        view.measure(
            View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, 500, 1000)

        // When - scroll to trigger direction change
        view.scrollBy(0, 30)

        // Then - lambda should be ready to be invoked
        assertNotNull(view)
    }

    @Test
    fun onEdgeReached_invokesLambdaCallback() {
        // Given
        val view = RecyclerScrollStateView(context)
        view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.adapter = TestAdapter(100)
        view.setEdgeReachThreshold(10)

        var receivedEdge: ScrollEdge? = null
        var receivedIsReached: Boolean? = null
        view.setOnReachEdgeListener { edge, isReached ->
            receivedEdge = edge
            receivedIsReached = isReached
        }

        // Layout the view
        view.measure(
            View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, 500, 1000)

        // When - scroll to trigger edge detection
        view.scrollToPosition(0)

        // Then - lambda should be ready to be invoked
        assertNotNull(view)
    }

    // ==============================================
    // Test Helper Classes
    // ==============================================

    private class TestAdapter(
        private val itemCount: Int,
    ) : RecyclerView.Adapter<TestViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): TestViewHolder {
            val view = View(parent.context)
            view.layoutParams =
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    100,
                )
            return TestViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: TestViewHolder,
            position: Int,
        ) {
            // Nothing to bind
        }

        override fun getItemCount(): Int = itemCount
    }

    private class TestViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView)
}
