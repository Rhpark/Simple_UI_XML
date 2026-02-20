package kr.open.library.simple_ui.xml.unit.ui.view.recyclerview

import kr.open.library.simple_ui.xml.ui.view.recyclerview.RecyclerScrollStateCalculator
import kr.open.library.simple_ui.xml.ui.view.recyclerview.ScrollDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * RecyclerScrollStateCalculator의 순수 로직 단위 테스트
 *
 * Android 의존성 없이 실행 가능한 빠른 단위 테스트
 */
class RecyclerScrollStateCalculatorTest {
    private lateinit var calculator: RecyclerScrollStateCalculator

    @Before
    fun setUp() {
        calculator =
            RecyclerScrollStateCalculator(
                edgeReachThreshold = 10,
                scrollDirectionThreshold = 20,
            )
    }

    // ========== Vertical Edge Tests ==========

    @Test
    fun checkVerticalEdges_atTop_returnsTopReached() {
        val result =
            calculator.checkVerticalEdges(
                verticalScrollOffset = 0,
                canScrollDown = true,
                verticalScrollExtent = 1000,
                verticalScrollRange = 5000,
            )

        assertTrue("Should be at top", result.isAtTop)
        assertTrue("Top should have changed", result.topChanged)
        assertFalse("Should not be at bottom", result.isAtBottom)
    }

    @Test
    fun checkVerticalEdges_withinTopThreshold_returnsTopReached() {
        val result =
            calculator.checkVerticalEdges(
                verticalScrollOffset = 5, // within threshold of 10
                canScrollDown = true,
                verticalScrollExtent = 1000,
                verticalScrollRange = 5000,
            )

        assertTrue("Should be at top within threshold", result.isAtTop)
        assertTrue("Top should have changed", result.topChanged)
    }

    @Test
    fun checkVerticalEdges_beyondTopThreshold_returnsNotAtTop() {
        val result =
            calculator.checkVerticalEdges(
                verticalScrollOffset = 15, // beyond threshold of 10
                canScrollDown = true,
                verticalScrollExtent = 1000,
                verticalScrollRange = 5000,
            )

        assertFalse("Should not be at top beyond threshold", result.isAtTop)
        assertFalse("Top should not have changed (initial state is false)", result.topChanged)
    }

    @Test
    fun checkVerticalEdges_atBottom_returnsBottomReached() {
        val result =
            calculator.checkVerticalEdges(
                verticalScrollOffset = 4000,
                canScrollDown = false,
                verticalScrollExtent = 1000,
                verticalScrollRange = 5000,
            )

        assertTrue("Should be at bottom", result.isAtBottom)
        assertTrue("Bottom should have changed", result.bottomChanged)
    }

    @Test
    fun checkVerticalEdges_withinBottomThreshold_returnsBottomReached() {
        val result =
            calculator.checkVerticalEdges(
                verticalScrollOffset = 3995, // 3995 + 1000 + 10 >= 5000
                canScrollDown = false,
                verticalScrollExtent = 1000,
                verticalScrollRange = 5000,
            )

        assertTrue("Should be at bottom within threshold", result.isAtBottom)
        assertTrue("Bottom should have changed", result.bottomChanged)
    }

    @Test
    fun checkVerticalEdges_middlePosition_returnsNeitherEdge() {
        val result =
            calculator.checkVerticalEdges(
                verticalScrollOffset = 2000,
                canScrollDown = true,
                verticalScrollExtent = 1000,
                verticalScrollRange = 5000,
            )

        assertFalse("Should not be at top", result.isAtTop)
        assertFalse("Should not be at bottom", result.isAtBottom)
        assertFalse("Top should not have changed (initial state is false)", result.topChanged)
        assertFalse("Bottom should not have changed (initial state is false)", result.bottomChanged)
    }

    @Test
    fun checkVerticalEdges_noChange_returnsNoChangedFlags() {
        // First call to establish state
        calculator.checkVerticalEdges(
            verticalScrollOffset = 2000,
            canScrollDown = true,
            verticalScrollExtent = 1000,
            verticalScrollRange = 5000,
        )

        // Second call with same state
        val result =
            calculator.checkVerticalEdges(
                verticalScrollOffset = 2000,
                canScrollDown = true,
                verticalScrollExtent = 1000,
                verticalScrollRange = 5000,
            )

        assertFalse("Top should not have changed", result.topChanged)
        assertFalse("Bottom should not have changed", result.bottomChanged)
    }

    // ========== Horizontal Edge Tests ==========

    @Test
    fun checkHorizontalEdges_atLeft_returnsLeftReached() {
        val result =
            calculator.checkHorizontalEdges(
                horizontalScrollOffset = 0,
                canScrollRight = true,
                horizontalScrollExtent = 1000,
                horizontalScrollRange = 5000,
            )

        assertTrue("Should be at left", result.isAtLeft)
        assertTrue("Left should have changed", result.leftChanged)
        assertFalse("Should not be at right", result.isAtRight)
    }

    @Test
    fun checkHorizontalEdges_withinLeftThreshold_returnsLeftReached() {
        val result =
            calculator.checkHorizontalEdges(
                horizontalScrollOffset = 8, // within threshold of 10
                canScrollRight = true,
                horizontalScrollExtent = 1000,
                horizontalScrollRange = 5000,
            )

        assertTrue("Should be at left within threshold", result.isAtLeft)
        assertTrue("Left should have changed", result.leftChanged)
    }

    @Test
    fun checkHorizontalEdges_atRight_returnsRightReached() {
        val result =
            calculator.checkHorizontalEdges(
                horizontalScrollOffset = 4000,
                canScrollRight = false,
                horizontalScrollExtent = 1000,
                horizontalScrollRange = 5000,
            )

        assertTrue("Should be at right", result.isAtRight)
        assertTrue("Right should have changed", result.rightChanged)
    }

    @Test
    fun checkHorizontalEdges_middlePosition_returnsNeitherEdge() {
        val result =
            calculator.checkHorizontalEdges(
                horizontalScrollOffset = 2000,
                canScrollRight = true,
                horizontalScrollExtent = 1000,
                horizontalScrollRange = 5000,
            )

        assertFalse("Should not be at left", result.isAtLeft)
        assertFalse("Should not be at right", result.isAtRight)
        assertFalse("Left should not have changed (initial state is false)", result.leftChanged)
        assertFalse("Right should not have changed (initial state is false)", result.rightChanged)
    }

    @Test
    fun checkHorizontalEdges_noChange_returnsNoChangedFlags() {
        // First call to establish state
        calculator.checkHorizontalEdges(
            horizontalScrollOffset = 2000,
            canScrollRight = true,
            horizontalScrollExtent = 1000,
            horizontalScrollRange = 5000,
        )

        // Second call with same state
        val result =
            calculator.checkHorizontalEdges(
                horizontalScrollOffset = 2000,
                canScrollRight = true,
                horizontalScrollExtent = 1000,
                horizontalScrollRange = 5000,
            )

        assertFalse("Left should not have changed", result.leftChanged)
        assertFalse("Right should not have changed", result.rightChanged)
    }

    // ========== Vertical Scroll Direction Tests ==========

    @Test
    fun updateVerticalScrollDirection_smallDownScroll_returnsNoChange() {
        val result = calculator.updateVerticalScrollDirection(10)

        assertFalse("Direction should not have changed (below threshold)", result.directionChanged)
        assertEquals("Should remain IDLE", ScrollDirection.IDLE, result.newDirection)
    }

    @Test
    fun updateVerticalScrollDirection_largeDownScroll_returnsDown() {
        val result = calculator.updateVerticalScrollDirection(25)

        assertTrue("Direction should have changed", result.directionChanged)
        assertEquals("Should be DOWN", ScrollDirection.DOWN, result.newDirection)
    }

    @Test
    fun updateVerticalScrollDirection_largeUpScroll_returnsUp() {
        val result = calculator.updateVerticalScrollDirection(-25)

        assertTrue("Direction should have changed", result.directionChanged)
        assertEquals("Should be UP", ScrollDirection.UP, result.newDirection)
    }

    @Test
    fun updateVerticalScrollDirection_accumulatedScrollReachesThreshold_triggersChange() {
        // Accumulate small scrolls
        var result = calculator.updateVerticalScrollDirection(10)
        assertFalse("Should not change yet", result.directionChanged)

        result = calculator.updateVerticalScrollDirection(5)
        assertFalse("Should not change yet", result.directionChanged)

        result = calculator.updateVerticalScrollDirection(6) // Total = 21, exceeds threshold
        assertTrue("Should change after accumulation", result.directionChanged)
        assertEquals("Should be DOWN", ScrollDirection.DOWN, result.newDirection)
    }

    @Test
    fun updateVerticalScrollDirection_accumulationResetsAfterThreshold() {
        // First large scroll
        calculator.updateVerticalScrollDirection(25)

        // Small scroll should not trigger change (accumulation was reset)
        val result = calculator.updateVerticalScrollDirection(10)
        assertFalse("Should not change (accumulation reset)", result.directionChanged)
    }

    @Test
    fun updateVerticalScrollDirection_changeFromDownToUp_detectsChange() {
        // First scroll down
        calculator.updateVerticalScrollDirection(25)

        // Then scroll up
        val result = calculator.updateVerticalScrollDirection(-25)
        assertTrue("Direction should have changed", result.directionChanged)
        assertEquals("Should be UP", ScrollDirection.UP, result.newDirection)
    }

    @Test
    fun updateVerticalScrollDirection_sameDirectionContinues_noChange() {
        // First scroll down
        calculator.updateVerticalScrollDirection(25)

        // Continue scrolling down
        val result = calculator.updateVerticalScrollDirection(25)
        assertFalse("Direction should not change (same direction)", result.directionChanged)
        assertEquals("Should still be DOWN", ScrollDirection.DOWN, result.newDirection)
    }

    // ========== Horizontal Scroll Direction Tests ==========

    @Test
    fun updateHorizontalScrollDirection_smallRightScroll_returnsNoChange() {
        val result = calculator.updateHorizontalScrollDirection(10)

        assertFalse("Direction should not have changed (below threshold)", result.directionChanged)
        assertEquals("Should remain IDLE", ScrollDirection.IDLE, result.newDirection)
    }

    @Test
    fun updateHorizontalScrollDirection_largeRightScroll_returnsRight() {
        val result = calculator.updateHorizontalScrollDirection(25)

        assertTrue("Direction should have changed", result.directionChanged)
        assertEquals("Should be RIGHT", ScrollDirection.RIGHT, result.newDirection)
    }

    @Test
    fun updateHorizontalScrollDirection_largeLeftScroll_returnsLeft() {
        val result = calculator.updateHorizontalScrollDirection(-25)

        assertTrue("Direction should have changed", result.directionChanged)
        assertEquals("Should be LEFT", ScrollDirection.LEFT, result.newDirection)
    }

    @Test
    fun updateHorizontalScrollDirection_accumulatedScrollReachesThreshold_triggersChange() {
        // Accumulate small scrolls
        var result = calculator.updateHorizontalScrollDirection(10)
        assertFalse("Should not change yet", result.directionChanged)

        result = calculator.updateHorizontalScrollDirection(5)
        assertFalse("Should not change yet", result.directionChanged)

        result = calculator.updateHorizontalScrollDirection(6) // Total = 21
        assertTrue("Should change after accumulation", result.directionChanged)
        assertEquals("Should be RIGHT", ScrollDirection.RIGHT, result.newDirection)
    }

    @Test
    fun updateHorizontalScrollDirection_changeFromRightToLeft_detectsChange() {
        // First scroll right
        calculator.updateHorizontalScrollDirection(25)

        // Then scroll left
        val result = calculator.updateHorizontalScrollDirection(-25)
        assertTrue("Direction should have changed", result.directionChanged)
        assertEquals("Should be LEFT", ScrollDirection.LEFT, result.newDirection)
    }

    // ========== Reset Tests ==========

    @Test
    fun resetScrollAccumulation_fromIdle_returnsNoChange() {
        val result = calculator.resetScrollAccumulation()

        assertFalse("Direction should not have changed (already IDLE)", result.directionChanged)
        assertEquals("Should be IDLE", ScrollDirection.IDLE, result.newDirection)
    }

    @Test
    fun resetScrollAccumulation_fromDown_returnsChange() {
        // Set direction to DOWN
        calculator.updateVerticalScrollDirection(25)

        val result = calculator.resetScrollAccumulation()

        assertTrue("Direction should have changed to IDLE", result.directionChanged)
        assertEquals("Should be IDLE", ScrollDirection.IDLE, result.newDirection)
    }

    @Test
    fun resetScrollAccumulation_clearsAccumulatedValues() {
        // Accumulate some scroll
        calculator.updateVerticalScrollDirection(10)
        calculator.updateHorizontalScrollDirection(15)

        // Reset
        calculator.resetScrollAccumulation()

        // Small new scroll should not trigger direction change
        var result = calculator.updateVerticalScrollDirection(5)
        assertFalse("Should not trigger (accumulation was cleared)", result.directionChanged)

        result = calculator.updateHorizontalScrollDirection(5)
        assertFalse("Should not trigger (accumulation was cleared)", result.directionChanged)
    }

    // ========== Threshold Update Tests ==========

    @Test
    fun updateThresholds_edgeReachThreshold_affectsEdgeDetection() {
        calculator.updateThresholds(edgeReachThreshold = 50)

        // Offset of 40 should now be within threshold
        val result =
            calculator.checkVerticalEdges(
                verticalScrollOffset = 40,
                canScrollDown = true,
                verticalScrollExtent = 1000,
                verticalScrollRange = 5000,
            )

        assertTrue("Should be at top with new threshold", result.isAtTop)
    }

    @Test
    fun updateThresholds_scrollDirectionThreshold_affectsDirectionChange() {
        calculator.updateThresholds(scrollDirectionThreshold = 50)

        // Scroll of 30 should not trigger with new threshold
        var result = calculator.updateVerticalScrollDirection(30)
        assertFalse("Should not change (below new threshold)", result.directionChanged)

        // Scroll of 25 more should trigger (total 55)
        result = calculator.updateVerticalScrollDirection(25)
        assertTrue("Should change (exceeds new threshold)", result.directionChanged)
    }

    @Test
    fun updateThresholds_onlyEdgeReachThreshold_keepsScrollDirectionThreshold() {
        calculator.updateThresholds(edgeReachThreshold = 50)

        // Original scroll direction threshold (20) should still apply
        val result = calculator.updateVerticalScrollDirection(25)
        assertTrue("Should change with original scroll threshold", result.directionChanged)
    }

    @Test
    fun updateThresholds_onlyScrollDirectionThreshold_keepsEdgeReachThreshold() {
        calculator.updateThresholds(scrollDirectionThreshold = 50)

        // Original edge reach threshold (10) should still apply
        val result =
            calculator.checkVerticalEdges(
                verticalScrollOffset = 5,
                canScrollDown = true,
                verticalScrollExtent = 1000,
                verticalScrollRange = 5000,
            )

        assertTrue("Should be at top with original edge threshold", result.isAtTop)
    }

    @Test
    fun updateThresholds_bothThresholds_updatesBoth() {
        calculator.updateThresholds(
            edgeReachThreshold = 30,
            scrollDirectionThreshold = 100,
        )

        val edgeResult =
            calculator.checkVerticalEdges(
                verticalScrollOffset = 25,
                canScrollDown = true,
                verticalScrollExtent = 1000,
                verticalScrollRange = 5000,
            )
        assertTrue("Should use new edge threshold", edgeResult.isAtTop)

        val scrollResult = calculator.updateVerticalScrollDirection(50)
        assertFalse("Should use new scroll threshold", scrollResult.directionChanged)
    }

    // ========== Edge Case Tests ==========

    @Test
    fun checkVerticalEdges_exactlyAtThreshold_returnsAtEdge() {
        val result =
            calculator.checkVerticalEdges(
                verticalScrollOffset = 10, // exactly at threshold
                canScrollDown = true,
                verticalScrollExtent = 1000,
                verticalScrollRange = 5000,
            )

        assertTrue("Should be at top at exact threshold", result.isAtTop)
    }

    @Test
    fun updateVerticalScrollDirection_exactlyAtThreshold_triggersChange() {
        val result = calculator.updateVerticalScrollDirection(20) // exactly at threshold

        assertTrue("Should change at exact threshold", result.directionChanged)
        assertEquals("Should be DOWN", ScrollDirection.DOWN, result.newDirection)
    }

    @Test
    fun updateVerticalScrollDirection_negativeToPositive_changesDirection() {
        calculator.updateVerticalScrollDirection(-25) // UP

        val result = calculator.updateVerticalScrollDirection(25) // DOWN
        assertTrue("Should change direction", result.directionChanged)
        assertEquals("Should be DOWN", ScrollDirection.DOWN, result.newDirection)
    }
}
