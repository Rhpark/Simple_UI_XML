package kr.open.library.simple_ui.xml.ui.view.recyclerview

import kotlin.math.abs

/**
 * Pure logic class for calculating RecyclerView scroll states.<br>
 * Designed to be unit-testable without Android dependencies.<br>
 * Handles scroll state calculation logic for RecyclerScrollStateView.<br><br>
 * RecyclerView의 스크롤 상태를 계산하는 순수 로직 클래스입니다.<br>
 * 안드로이드 의존성 없이 유닛 테스트 가능하도록 설계되었습니다.<br>
 * RecyclerScrollStateView의 스크롤 상태 계산 로직을 담당합니다.<br>
 *
 * @param edgeReachThreshold Threshold in pixels for edge reach detection.<br><br>
 *                           가장자리 도달 감지를 위한 픽셀 단위 임계값.<br>
 *
 * @param scrollDirectionThreshold Threshold in pixels for scroll direction change detection.<br><br>
 *                                  스크롤 방향 변경 감지를 위한 픽셀 단위 임계값.<br>
 */
internal class RecyclerScrollStateCalculator(
    private var edgeReachThreshold: Int,
    private var scrollDirectionThreshold: Int,
) {
    private var accumulatedDx = 0
    private var accumulatedDy = 0

    private var isAtTop = false
    private var isAtBottom = false
    private var isAtLeft = false
    private var isAtRight = false

    private var currentScrollDirection: ScrollDirection = ScrollDirection.IDLE

    /**
     * Result of vertical edge check.<br><br>
     * 수직 엣지 체크 결과.<br>
     *
     * @property topChanged Whether the top edge state changed.<br><br>
     *                      상단 가장자리 상태가 변경되었는지 여부.<br>
     *
     * @property isAtTop Whether currently at the top edge.<br><br>
     *                   현재 상단 가장자리에 있는지 여부.<br>
     *
     * @property bottomChanged Whether the bottom edge state changed.<br><br>
     *                         하단 가장자리 상태가 변경되었는지 여부.<br>
     *
     * @property isAtBottom Whether currently at the bottom edge.<br><br>
     *                      현재 하단 가장자리에 있는지 여부.<br>
     */
    data class VerticalEdgeCheckResult(
        val topChanged: Boolean,
        val isAtTop: Boolean,
        val bottomChanged: Boolean,
        val isAtBottom: Boolean,
    )

    /**
     * Result of horizontal edge check.<br><br>
     * 수평 엣지 체크 결과.<br>
     *
     * @property leftChanged Whether the left edge state changed.<br><br>
     *                       왼쪽 가장자리 상태가 변경되었는지 여부.<br>
     *
     * @property isAtLeft Whether currently at the left edge.<br><br>
     *                    현재 왼쪽 가장자리에 있는지 여부.<br>
     *
     * @property rightChanged Whether the right edge state changed.<br><br>
     *                        오른쪽 가장자리 상태가 변경되었는지 여부.<br>
     *
     * @property isAtRight Whether currently at the right edge.<br><br>
     *                     현재 오른쪽 가장자리에 있는지 여부.<br>
     */
    data class HorizontalEdgeCheckResult(
        val leftChanged: Boolean,
        val isAtLeft: Boolean,
        val rightChanged: Boolean,
        val isAtRight: Boolean,
    )

    /**
     * Result of scroll direction update.<br><br>
     * 스크롤 방향 업데이트 결과.<br>
     *
     * @property directionChanged Whether the scroll direction changed.<br><br>
     *                            스크롤 방향이 변경되었는지 여부.<br>
     *
     * @property newDirection The new scroll direction.<br><br>
     *                        새로운 스크롤 방향.<br>
     */
    data class ScrollDirectionUpdateResult(
        val directionChanged: Boolean,
        val newDirection: ScrollDirection,
    )

    /**
     * Checks vertical edges and returns the changes.<br><br>
     * 수직 엣지를 체크하고 변경사항을 반환합니다.<br>
     *
     * @param verticalScrollOffset Current vertical scroll offset.<br><br>
     *                              현재 수직 스크롤 오프셋.<br>
     *
     * @param canScrollDown Whether can scroll down.<br><br>
     *                      아래로 스크롤 가능 여부.<br>
     *
     * @param verticalScrollExtent Vertical scroll extent.<br><br>
     *                              수직 스크롤 범위.<br>
     *
     * @param verticalScrollRange Total vertical scroll range.<br><br>
     *                             전체 수직 스크롤 범위.<br>
     *
     * @return The edge change result.<br><br>
     *         엣지 변경 결과.<br>
     */
    fun checkVerticalEdges(
        verticalScrollOffset: Int,
        canScrollDown: Boolean,
        verticalScrollExtent: Int,
        verticalScrollRange: Int,
    ): VerticalEdgeCheckResult {
        val newIsAtTop = verticalScrollOffset <= edgeReachThreshold
        val topChanged = newIsAtTop != isAtTop
        isAtTop = newIsAtTop

        val isBottomReached =
            !canScrollDown &&
                verticalScrollExtent + verticalScrollOffset + edgeReachThreshold >= verticalScrollRange
        val bottomChanged = isBottomReached != isAtBottom
        isAtBottom = isBottomReached

        return VerticalEdgeCheckResult(
            topChanged = topChanged,
            isAtTop = newIsAtTop,
            bottomChanged = bottomChanged,
            isAtBottom = isBottomReached,
        )
    }

    /**
     * Checks horizontal edges and returns the changes.<br><br>
     * 수평 엣지를 체크하고 변경사항을 반환합니다.<br>
     *
     * @param horizontalScrollOffset Current horizontal scroll offset.<br><br>
     *                                현재 수평 스크롤 오프셋.<br>
     *
     * @param canScrollRight Whether can scroll right.<br><br>
     *                       오른쪽으로 스크롤 가능 여부.<br>
     *
     * @param horizontalScrollExtent Horizontal scroll extent.<br><br>
     *                                수평 스크롤 범위.<br>
     *
     * @param horizontalScrollRange Total horizontal scroll range.<br><br>
     *                               전체 수평 스크롤 범위.<br>
     *
     * @return The edge change result.<br><br>
     *         엣지 변경 결과.<br>
     */
    fun checkHorizontalEdges(
        horizontalScrollOffset: Int,
        canScrollRight: Boolean,
        horizontalScrollExtent: Int,
        horizontalScrollRange: Int,
    ): HorizontalEdgeCheckResult {
        val newIsAtLeft = horizontalScrollOffset <= edgeReachThreshold
        val leftChanged = newIsAtLeft != isAtLeft
        isAtLeft = newIsAtLeft

        val isRightReached =
            !canScrollRight &&
                horizontalScrollExtent + horizontalScrollOffset + edgeReachThreshold >= horizontalScrollRange
        val rightChanged = isRightReached != isAtRight
        isAtRight = isRightReached

        return HorizontalEdgeCheckResult(
            leftChanged = leftChanged,
            isAtLeft = newIsAtLeft,
            rightChanged = rightChanged,
            isAtRight = isRightReached,
        )
    }

    /**
     * Updates vertical scroll direction and returns the changes.<br><br>
     * 수직 스크롤 방향을 업데이트하고 변경사항을 반환합니다.<br>
     *
     * @param dy Vertical scroll delta value (positive: down, negative: up).<br><br>
     *           수직 스크롤 델타값 (양수: 아래로, 음수: 위로).<br>
     *
     * @return The scroll direction update result.<br><br>
     *         스크롤 방향 업데이트 결과.<br>
     */
    fun updateVerticalScrollDirection(dy: Int): ScrollDirectionUpdateResult {
        accumulatedDy += dy

        if (abs(accumulatedDy) >= scrollDirectionThreshold) {
            val newDirection = if (accumulatedDy > 0) ScrollDirection.DOWN else ScrollDirection.UP
            val directionChanged = newDirection != currentScrollDirection

            if (directionChanged) {
                currentScrollDirection = newDirection
            }
            accumulatedDy = 0

            return ScrollDirectionUpdateResult(
                directionChanged = directionChanged,
                newDirection = newDirection,
            )
        }

        return ScrollDirectionUpdateResult(
            directionChanged = false,
            newDirection = currentScrollDirection,
        )
    }

    /**
     * Updates horizontal scroll direction and returns the changes.<br><br>
     * 수평 스크롤 방향을 업데이트하고 변경사항을 반환합니다.<br>
     *
     * @param dx Horizontal scroll delta value (positive: right, negative: left).<br><br>
     *           수평 스크롤 델타값 (양수: 오른쪽, 음수: 왼쪽).<br>
     *
     * @return The scroll direction update result.<br><br>
     *         스크롤 방향 업데이트 결과.<br>
     */
    fun updateHorizontalScrollDirection(dx: Int): ScrollDirectionUpdateResult {
        accumulatedDx += dx

        if (abs(accumulatedDx) >= scrollDirectionThreshold) {
            val newDirection = if (accumulatedDx > 0) ScrollDirection.RIGHT else ScrollDirection.LEFT
            val directionChanged = newDirection != currentScrollDirection

            if (directionChanged) {
                currentScrollDirection = newDirection
            }
            accumulatedDx = 0

            return ScrollDirectionUpdateResult(
                directionChanged = directionChanged,
                newDirection = newDirection,
            )
        }

        return ScrollDirectionUpdateResult(
            directionChanged = false,
            newDirection = currentScrollDirection,
        )
    }

    /**
     * Called when scroll becomes IDLE state.<br>
     * Resets accumulated scroll values and changes direction to IDLE.<br><br>
     * 스크롤이 IDLE 상태가 되었을 때 호출됩니다.<br>
     * 축적된 스크롤 값을 초기화하고 방향을 IDLE로 변경합니다.<br>
     *
     * @return The scroll direction update result.<br><br>
     *         스크롤 방향 업데이트 결과.<br>
     */
    fun resetScrollAccumulation(): ScrollDirectionUpdateResult {
        accumulatedDx = 0
        accumulatedDy = 0

        val directionChanged = currentScrollDirection != ScrollDirection.IDLE
        if (directionChanged) {
            currentScrollDirection = ScrollDirection.IDLE
        }

        return ScrollDirectionUpdateResult(
            directionChanged = directionChanged,
            newDirection = ScrollDirection.IDLE,
        )
    }

    /**
     * Updates threshold values.<br><br>
     * 임계값을 업데이트합니다.<br>
     *
     * @param edgeReachThreshold Edge reach threshold (null to keep unchanged).<br><br>
     *                           엣지 도달 임계값 (null이면 변경하지 않음).<br>
     *
     * @param scrollDirectionThreshold Scroll direction threshold (null to keep unchanged).<br><br>
     *                                  스크롤 방향 임계값 (null이면 변경하지 않음).<br>
     */
    fun updateThresholds(
        edgeReachThreshold: Int? = null,
        scrollDirectionThreshold: Int? = null,
    ) {
        edgeReachThreshold?.let { this.edgeReachThreshold = it }
        scrollDirectionThreshold?.let { this.scrollDirectionThreshold = it }
    }
}
