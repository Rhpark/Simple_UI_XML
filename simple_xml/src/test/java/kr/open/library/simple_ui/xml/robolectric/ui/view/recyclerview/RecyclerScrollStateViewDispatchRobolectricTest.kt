package kr.open.library.simple_ui.xml.robolectric.ui.view.recyclerview

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.xml.ui.view.recyclerview.RecyclerScrollStateView
import kr.open.library.simple_ui.xml.ui.view.recyclerview.ScrollDirection
import kr.open.library.simple_ui.xml.ui.view.recyclerview.ScrollEdge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RecyclerScrollStateViewDispatchRobolectricTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun verticalScroll_dispatchesDirectionAndIdleToListenerAndFlow() {
        val view = createView(LinearLayoutManager.VERTICAL)
        val directions = mutableListOf<ScrollDirection>()
        view.setScrollDirectionThreshold(20)
        view.setOnScrollDirectionListener { direction -> directions += direction }
        val listener = getInternalScrollListener(view)

        listener.onScrolled(view, 0, 21)
        listener.onScrollStateChanged(view, RecyclerView.SCROLL_STATE_IDLE)

        assertEquals(listOf(ScrollDirection.DOWN, ScrollDirection.IDLE), directions)
        assertEquals(listOf(ScrollDirection.IDLE), view.sfScrollDirectionFlow.replayCache)
    }

    @Test
    fun horizontalScroll_dispatchesBothDirectionsToListenerAndFlow() {
        val view = createView(LinearLayoutManager.HORIZONTAL)
        val directions = mutableListOf<ScrollDirection>()
        view.setScrollDirectionThreshold(20)
        view.setOnScrollDirectionListener { direction -> directions += direction }
        val listener = getInternalScrollListener(view)

        listener.onScrolled(view, 21, 0)
        listener.onScrolled(view, -21, 0)

        assertEquals(listOf(ScrollDirection.RIGHT, ScrollDirection.LEFT), directions)
        assertEquals(listOf(ScrollDirection.LEFT), view.sfScrollDirectionFlow.replayCache)
    }

    @Test
    fun verticalEdgeChange_dispatchesToListenerAndFlow() {
        val view = createView(LinearLayoutManager.VERTICAL)
        val events = mutableListOf<Pair<ScrollEdge, Boolean>>()
        view.setOnReachEdgeListener { edge, isReached -> events += edge to isReached }

        getInternalScrollListener(view).onScrolled(view, 0, 0)

        assertTrue(events.contains(ScrollEdge.TOP to true))
        assertEquals(events.last(), view.sfEdgeReachedFlow.replayCache.single())
    }

    @Test
    fun horizontalEdgeChange_dispatchesToListenerAndFlow() {
        val view = createView(LinearLayoutManager.HORIZONTAL)
        val events = mutableListOf<Pair<ScrollEdge, Boolean>>()
        view.setOnReachEdgeListener { edge, isReached -> events += edge to isReached }

        getInternalScrollListener(view).onScrolled(view, 0, 0)

        assertTrue(events.contains(ScrollEdge.LEFT to true))
        assertEquals(events.last(), view.sfEdgeReachedFlow.replayCache.single())
    }

    private fun createView(orientation: Int): RecyclerScrollStateView = RecyclerScrollStateView(context).apply {
        layoutManager = LinearLayoutManager(context, orientation, false)
    }

    private fun getInternalScrollListener(view: RecyclerScrollStateView): RecyclerView.OnScrollListener {
        val field = RecyclerScrollStateView::class.java.getDeclaredField("scrollListener")
        field.isAccessible = true
        return field.get(view) as RecyclerView.OnScrollListener
    }
}
