package kr.open.library.simple_ui.presenter.ui.view.recyclerview

import kotlin.math.abs

/**
 * RecyclerView의 스크롤 상태를 계산하는 순수 로직 클래스
 *
 * 안드로이드 의존성 없이 유닛 테스트 가능하도록 설계됨
 * RecyclerScrollStateView의 스크롤 상태 계산 로직을 담당
 */
internal class RecyclerScrollStateCalculator(
    private var edgeReachThreshold: Int,
    private var scrollDirectionThreshold: Int
) {
    private var accumulatedDx = 0
    private var accumulatedDy = 0

    private var isAtTop = false
    private var isAtBottom = false
    private var isAtLeft = false
    private var isAtRight = false

    private var currentScrollDirection: ScrollDirection = ScrollDirection.IDLE

    /**
     * 수직 엣지 체크 결과
     */
    data class VerticalEdgeCheckResult(
        val topChanged: Boolean,
        val isAtTop: Boolean,
        val bottomChanged: Boolean,
        val isAtBottom: Boolean
    )

    /**
     * 수평 엣지 체크 결과
     */
    data class HorizontalEdgeCheckResult(
        val leftChanged: Boolean,
        val isAtLeft: Boolean,
        val rightChanged: Boolean,
        val isAtRight: Boolean
    )

    /**
     * 스크롤 방향 업데이트 결과
     */
    data class ScrollDirectionUpdateResult(
        val directionChanged: Boolean,
        val newDirection: ScrollDirection
    )

    /**
     * 수직 엣지를 체크하고 변경사항을 반환
     *
     * @param verticalScrollOffset 현재 수직 스크롤 오프셋
     * @param canScrollDown 아래로 스크롤 가능 여부
     * @param verticalScrollExtent 수직 스크롤 범위
     * @param verticalScrollRange 전체 수직 스크롤 범위
     * @return 엣지 변경 결과
     */
    fun checkVerticalEdges(
        verticalScrollOffset: Int,
        canScrollDown: Boolean,
        verticalScrollExtent: Int,
        verticalScrollRange: Int
    ): VerticalEdgeCheckResult {
        val newIsAtTop = verticalScrollOffset <= edgeReachThreshold
        val topChanged = newIsAtTop != isAtTop
        isAtTop = newIsAtTop

        val isBottomReached = !canScrollDown &&
            verticalScrollExtent + verticalScrollOffset + edgeReachThreshold >= verticalScrollRange
        val bottomChanged = isBottomReached != isAtBottom
        isAtBottom = isBottomReached

        return VerticalEdgeCheckResult(
            topChanged = topChanged,
            isAtTop = newIsAtTop,
            bottomChanged = bottomChanged,
            isAtBottom = isBottomReached
        )
    }

    /**
     * 수평 엣지를 체크하고 변경사항을 반환
     *
     * @param horizontalScrollOffset 현재 수평 스크롤 오프셋
     * @param canScrollRight 오른쪽으로 스크롤 가능 여부
     * @param horizontalScrollExtent 수평 스크롤 범위
     * @param horizontalScrollRange 전체 수평 스크롤 범위
     * @return 엣지 변경 결과
     */
    fun checkHorizontalEdges(
        horizontalScrollOffset: Int,
        canScrollRight: Boolean,
        horizontalScrollExtent: Int,
        horizontalScrollRange: Int
    ): HorizontalEdgeCheckResult {
        val newIsAtLeft = horizontalScrollOffset <= edgeReachThreshold
        val leftChanged = newIsAtLeft != isAtLeft
        isAtLeft = newIsAtLeft

        val isRightReached = !canScrollRight &&
            horizontalScrollExtent + horizontalScrollOffset + edgeReachThreshold >= horizontalScrollRange
        val rightChanged = isRightReached != isAtRight
        isAtRight = isRightReached

        return HorizontalEdgeCheckResult(
            leftChanged = leftChanged,
            isAtLeft = newIsAtLeft,
            rightChanged = rightChanged,
            isAtRight = isRightReached
        )
    }

    /**
     * 수직 스크롤 방향을 업데이트하고 변경사항을 반환
     *
     * @param dy 수직 스크롤 델타값 (양수: 아래로, 음수: 위로)
     * @return 스크롤 방향 업데이트 결과
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
                newDirection = newDirection
            )
        }

        return ScrollDirectionUpdateResult(
            directionChanged = false,
            newDirection = currentScrollDirection
        )
    }

    /**
     * 수평 스크롤 방향을 업데이트하고 변경사항을 반환
     *
     * @param dx 수평 스크롤 델타값 (양수: 오른쪽, 음수: 왼쪽)
     * @return 스크롤 방향 업데이트 결과
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
                newDirection = newDirection
            )
        }

        return ScrollDirectionUpdateResult(
            directionChanged = false,
            newDirection = currentScrollDirection
        )
    }

    /**
     * 스크롤이 IDLE 상태가 되었을 때 호출
     * 축적된 스크롤 값을 초기화하고 방향을 IDLE로 변경
     *
     * @return 스크롤 방향 업데이트 결과
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
            newDirection = ScrollDirection.IDLE
        )
    }

    /**
     * 임계값 업데이트
     *
     * @param edgeReachThreshold 엣지 도달 임계값 (null이면 변경하지 않음)
     * @param scrollDirectionThreshold 스크롤 방향 임계값 (null이면 변경하지 않음)
     */
    fun updateThresholds(
        edgeReachThreshold: Int? = null,
        scrollDirectionThreshold: Int? = null
    ) {
        edgeReachThreshold?.let { this.edgeReachThreshold = it }
        scrollDirectionThreshold?.let { this.scrollDirectionThreshold = it }
    }
}
