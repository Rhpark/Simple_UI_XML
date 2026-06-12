package kr.open.library.simple_ui.compose.unit.scroll

import androidx.compose.foundation.gestures.Orientation
import kr.open.library.simple_ui.compose.scroll.ScrollDirection
import kr.open.library.simple_ui.compose.scroll.ScrollEdge
import kr.open.library.simple_ui.compose.scroll.isHorizontalEdgeReached
import kr.open.library.simple_ui.compose.scroll.isVerticalByOrientation
import kr.open.library.simple_ui.compose.scroll.isVerticalEdgeReached
import kr.open.library.simple_ui.compose.scroll.resolveScrollDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for scroll state pure functions in ScrollStateHelpers.kt.<br>
 * ScrollStateHelpers.kt 의 스크롤 상태 순수 함수에 대한 단위 테스트입니다.<br>
 */
class ScrollDirectionCalculatorTest {
    // -----------------------------------------------------------------------
    // isVerticalByOrientation
    // -----------------------------------------------------------------------

    @Test
    fun `vertical orientation is vertical`() {
        assertTrue(isVerticalByOrientation(Orientation.Vertical))
    }

    @Test
    fun `horizontal orientation is not vertical`() {
        assertFalse(isVerticalByOrientation(Orientation.Horizontal))
    }

    // -----------------------------------------------------------------------
    // resolveScrollDirection — 수직 축
    // -----------------------------------------------------------------------

    @Test
    fun `vertical DOWN when accumulated delta exceeds threshold positively`() {
        val result = resolveScrollDirection(
            accumulatedDelta = 25,
            thresholdPx = 20,
            isVertical = true,
            previousDirection = ScrollDirection.IDLE,
        )
        assertEquals(ScrollDirection.DOWN, result)
    }

    @Test
    fun `vertical UP when accumulated delta exceeds threshold negatively`() {
        val result = resolveScrollDirection(
            accumulatedDelta = -21,
            thresholdPx = 20,
            isVertical = true,
            previousDirection = ScrollDirection.IDLE,
        )
        assertEquals(ScrollDirection.UP, result)
    }

    @Test
    fun `vertical keeps previous direction when delta below threshold`() {
        val result = resolveScrollDirection(
            accumulatedDelta = 10,
            thresholdPx = 20,
            isVertical = true,
            previousDirection = ScrollDirection.DOWN,
        )
        assertEquals(ScrollDirection.DOWN, result)
    }

    @Test
    fun `vertical keeps IDLE when delta below threshold and previous is IDLE`() {
        val result = resolveScrollDirection(
            accumulatedDelta = 5,
            thresholdPx = 20,
            isVertical = true,
            previousDirection = ScrollDirection.IDLE,
        )
        assertEquals(ScrollDirection.IDLE, result)
    }

    // -----------------------------------------------------------------------
    // resolveScrollDirection — 수평 축
    // -----------------------------------------------------------------------

    @Test
    fun `horizontal RIGHT when accumulated delta exceeds threshold positively`() {
        val result = resolveScrollDirection(
            accumulatedDelta = 25,
            thresholdPx = 20,
            isVertical = false,
            previousDirection = ScrollDirection.IDLE,
        )
        assertEquals(ScrollDirection.RIGHT, result)
    }

    @Test
    fun `horizontal LEFT when accumulated delta exceeds threshold negatively`() {
        val result = resolveScrollDirection(
            accumulatedDelta = -25,
            thresholdPx = 20,
            isVertical = false,
            previousDirection = ScrollDirection.IDLE,
        )
        assertEquals(ScrollDirection.LEFT, result)
    }

    @Test
    fun `horizontal keeps previous direction when delta below threshold`() {
        val result = resolveScrollDirection(
            accumulatedDelta = 5,
            thresholdPx = 20,
            isVertical = false,
            previousDirection = ScrollDirection.RIGHT,
        )
        assertEquals(ScrollDirection.RIGHT, result)
    }

    @Test
    fun `resolveScrollDirection at exactly threshold returns new direction`() {
        val result = resolveScrollDirection(
            accumulatedDelta = 20,
            thresholdPx = 20,
            isVertical = true,
            previousDirection = ScrollDirection.IDLE,
        )
        assertEquals(ScrollDirection.DOWN, result)
    }

    @Test
    fun `resolveScrollDirection at exactly negative threshold returns UP`() {
        val result = resolveScrollDirection(
            accumulatedDelta = -20,
            thresholdPx = 20,
            isVertical = true,
            previousDirection = ScrollDirection.IDLE,
        )
        assertEquals(ScrollDirection.UP, result)
    }

    // -----------------------------------------------------------------------
    // isVerticalEdgeReached
    // -----------------------------------------------------------------------

    @Test
    fun `TOP edge reached when at index 0 and offset within threshold`() {
        assertTrue(
            isVerticalEdgeReached(
                edge = ScrollEdge.TOP,
                firstVisibleIndex = 0,
                firstVisibleOffset = 5,
                canScrollForward = true,
                thresholdPx = 10,
            ),
        )
    }

    @Test
    fun `TOP edge not reached when offset exceeds threshold`() {
        assertFalse(
            isVerticalEdgeReached(
                edge = ScrollEdge.TOP,
                firstVisibleIndex = 0,
                firstVisibleOffset = 11,
                canScrollForward = true,
                thresholdPx = 10,
            ),
        )
    }

    @Test
    fun `TOP edge not reached when index is not 0`() {
        assertFalse(
            isVerticalEdgeReached(
                edge = ScrollEdge.TOP,
                firstVisibleIndex = 1,
                firstVisibleOffset = 0,
                canScrollForward = true,
                thresholdPx = 10,
            ),
        )
    }

    @Test
    fun `BOTTOM edge reached when cannot scroll forward`() {
        assertTrue(
            isVerticalEdgeReached(
                edge = ScrollEdge.BOTTOM,
                firstVisibleIndex = 10,
                firstVisibleOffset = 0,
                canScrollForward = false,
                thresholdPx = 10,
            ),
        )
    }

    @Test
    fun `BOTTOM edge not reached when can still scroll forward`() {
        assertFalse(
            isVerticalEdgeReached(
                edge = ScrollEdge.BOTTOM,
                firstVisibleIndex = 0,
                firstVisibleOffset = 0,
                canScrollForward = true,
                thresholdPx = 10,
            ),
        )
    }

    @Test
    fun `vertical function returns false for LEFT edge (axis mismatch)`() {
        assertFalse(
            isVerticalEdgeReached(
                edge = ScrollEdge.LEFT,
                firstVisibleIndex = 0,
                firstVisibleOffset = 0,
                canScrollForward = false,
                thresholdPx = 10,
            ),
        )
    }

    @Test
    fun `vertical function returns false for RIGHT edge (axis mismatch)`() {
        assertFalse(
            isVerticalEdgeReached(
                edge = ScrollEdge.RIGHT,
                firstVisibleIndex = 0,
                firstVisibleOffset = 0,
                canScrollForward = false,
                thresholdPx = 10,
            ),
        )
    }

    // -----------------------------------------------------------------------
    // isHorizontalEdgeReached
    // -----------------------------------------------------------------------

    @Test
    fun `LEFT edge reached when at index 0 and offset within threshold`() {
        assertTrue(
            isHorizontalEdgeReached(
                edge = ScrollEdge.LEFT,
                firstVisibleIndex = 0,
                firstVisibleOffset = 0,
                canScrollForward = true,
                thresholdPx = 10,
            ),
        )
    }

    @Test
    fun `LEFT edge not reached when offset exceeds threshold`() {
        assertFalse(
            isHorizontalEdgeReached(
                edge = ScrollEdge.LEFT,
                firstVisibleIndex = 0,
                firstVisibleOffset = 15,
                canScrollForward = true,
                thresholdPx = 10,
            ),
        )
    }

    @Test
    fun `RIGHT edge reached when cannot scroll forward`() {
        assertTrue(
            isHorizontalEdgeReached(
                edge = ScrollEdge.RIGHT,
                firstVisibleIndex = 5,
                firstVisibleOffset = 0,
                canScrollForward = false,
                thresholdPx = 10,
            ),
        )
    }

    @Test
    fun `RIGHT edge not reached when can still scroll forward`() {
        assertFalse(
            isHorizontalEdgeReached(
                edge = ScrollEdge.RIGHT,
                firstVisibleIndex = 0,
                firstVisibleOffset = 0,
                canScrollForward = true,
                thresholdPx = 10,
            ),
        )
    }

    @Test
    fun `horizontal function returns false for TOP edge (axis mismatch)`() {
        assertFalse(
            isHorizontalEdgeReached(
                edge = ScrollEdge.TOP,
                firstVisibleIndex = 0,
                firstVisibleOffset = 0,
                canScrollForward = false,
                thresholdPx = 10,
            ),
        )
    }

    @Test
    fun `horizontal function returns false for BOTTOM edge (axis mismatch)`() {
        assertFalse(
            isHorizontalEdgeReached(
                edge = ScrollEdge.BOTTOM,
                firstVisibleIndex = 0,
                firstVisibleOffset = 0,
                canScrollForward = false,
                thresholdPx = 10,
            ),
        )
    }
}
