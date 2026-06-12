package kr.open.library.simple_ui.compose.scroll

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlin.math.abs

// ---------------------------------------------------------------------------
// 내부 순수 함수 (unit 테스트 대상)
// Internal pure functions (subject to unit testing)
// ---------------------------------------------------------------------------

/**
 * [Orientation] 값을 기반으로 수직 여부를 판단합니다.<br>
 * Determines vertical orientation from [Orientation].<br>
 *
 * [androidx.compose.foundation.lazy.LazyListLayoutInfo.orientation] 기준:<br>
 * Per [androidx.compose.foundation.lazy.LazyListLayoutInfo.orientation]:<br>
 * [Orientation.Vertical] → `true`, [Orientation.Horizontal] → `false`.<br>
 */
internal fun isVerticalByOrientation(orientation: Orientation): Boolean =
    orientation == Orientation.Vertical

/**
 * 누적 델타와 임계값을 기반으로 스크롤 방향을 계산합니다.<br>
 * Calculates scroll direction from accumulated delta and threshold.<br>
 *
 * If the absolute value of [accumulatedDelta] is below [thresholdPx], [previousDirection] is returned as-is.<br>
 * [accumulatedDelta]의 절댓값이 [thresholdPx] 미만이면 [previousDirection]을 그대로 반환합니다.<br>
 *
 * @param accumulatedDelta Accumulated scroll delta in pixels.<br><br>
 *                         누적 스크롤 델타 (픽셀 단위).<br>
 * @param thresholdPx Minimum absolute delta required to change direction.<br><br>
 *                    방향 변경에 필요한 최소 절대 델타 값.<br>
 * @param isVertical Whether the scroll axis is vertical.<br><br>
 *                   스크롤 축이 수직인지 여부.<br>
 * @param previousDirection Direction to return when threshold is not met.<br><br>
 *                          임계값 미충족 시 유지할 직전 방향.<br>
 * @return Resolved [ScrollDirection].<br><br>
 *         결정된 [ScrollDirection].<br>
 */
internal fun resolveScrollDirection(
    accumulatedDelta: Int,
    thresholdPx: Int,
    isVertical: Boolean,
    previousDirection: ScrollDirection,
): ScrollDirection {
    if (abs(accumulatedDelta) < thresholdPx) return previousDirection
    return when {
        isVertical && accumulatedDelta > 0 -> ScrollDirection.DOWN
        isVertical && accumulatedDelta < 0 -> ScrollDirection.UP
        !isVertical && accumulatedDelta > 0 -> ScrollDirection.RIGHT
        !isVertical && accumulatedDelta < 0 -> ScrollDirection.LEFT
        else -> previousDirection
    }
}

/**
 * TOP/BOTTOM 엣지 도달 여부를 판단합니다 (수직 리스트 전용).<br>
 * Determines whether the vertical edge is reached.<br>
 *
 * [ScrollEdge.LEFT] / [ScrollEdge.RIGHT]가 전달되면 항상 `false`를 반환합니다.<br>
 * Returns `false` unconditionally for [ScrollEdge.LEFT] or [ScrollEdge.RIGHT].<br>
 *
 * @param edge Target [ScrollEdge].<br><br>
 *             대상 [ScrollEdge].<br>
 * @param firstVisibleIndex First visible item index.<br><br>
 *                          첫 번째 가시 아이템 인덱스.<br>
 * @param firstVisibleOffset First visible item scroll offset in pixels.<br><br>
 *                           첫 번째 가시 아이템 스크롤 오프셋 (픽셀 단위).<br>
 * @param canScrollForward Whether the list can still scroll toward the end.<br><br>
 *                         리스트가 끝 방향으로 더 스크롤 가능한지 여부.<br>
 * @param thresholdPx Pixel threshold for TOP edge proximity.<br><br>
 *                    TOP 엣지 근접 판정 픽셀 임계값.<br>
 * @return `true` if the specified vertical edge is reached.<br><br>
 *         지정된 수직 엣지에 도달했으면 `true`.<br>
 */
internal fun isVerticalEdgeReached(
    edge: ScrollEdge,
    firstVisibleIndex: Int,
    firstVisibleOffset: Int,
    canScrollForward: Boolean,
    thresholdPx: Int,
): Boolean = when (edge) {
    ScrollEdge.TOP -> firstVisibleIndex == 0 && firstVisibleOffset <= thresholdPx
    ScrollEdge.BOTTOM -> !canScrollForward
    else -> false
}

/**
 * LEFT/RIGHT 엣지 도달 여부를 판단합니다 (수평 리스트 전용).<br>
 * Determines whether the horizontal edge is reached.<br>
 *
 * [ScrollEdge.TOP] / [ScrollEdge.BOTTOM]이 전달되면 항상 `false`를 반환합니다.<br>
 * Returns `false` unconditionally for [ScrollEdge.TOP] or [ScrollEdge.BOTTOM].<br>
 *
 * @param edge Target [ScrollEdge].<br><br>
 *             대상 [ScrollEdge].<br>
 * @param firstVisibleIndex First visible item index.<br><br>
 *                          첫 번째 가시 아이템 인덱스.<br>
 * @param firstVisibleOffset First visible item scroll offset in pixels.<br><br>
 *                           첫 번째 가시 아이템 스크롤 오프셋 (픽셀 단위).<br>
 * @param canScrollForward Whether the list can still scroll toward the end.<br><br>
 *                         리스트가 끝 방향으로 더 스크롤 가능한지 여부.<br>
 * @param thresholdPx Pixel threshold for LEFT edge proximity.<br><br>
 *                    LEFT 엣지 근접 판정 픽셀 임계값.<br>
 * @return `true` if the specified horizontal edge is reached.<br><br>
 *         지정된 수평 엣지에 도달했으면 `true`.<br>
 */
internal fun isHorizontalEdgeReached(
    edge: ScrollEdge,
    firstVisibleIndex: Int,
    firstVisibleOffset: Int,
    canScrollForward: Boolean,
    thresholdPx: Int,
): Boolean = when (edge) {
    ScrollEdge.LEFT -> firstVisibleIndex == 0 && firstVisibleOffset <= thresholdPx
    ScrollEdge.RIGHT -> !canScrollForward
    else -> false
}

/**
 * 방향 추적에 사용하는 스크롤 상태 관찰값입니다.<br>
 * Scroll state observation used for direction tracking.<br>
 *
 * @param index 첫 가시 아이템 인덱스.<br><br>
 *              First visible item index.<br>
 * @param offset 첫 가시 아이템 스크롤 오프셋 (픽셀 단위).<br><br>
 *               First visible item scroll offset in pixels.<br>
 * @param orientation 리스트 스크롤 축.<br><br>
 *                    List scroll axis.<br>
 * @param isScrollInProgress 스크롤 모션 진행 여부.<br><br>
 *                           Whether a scroll motion is in progress.<br>
 */
private data class ScrollObservation(
    val index: Int,
    val offset: Int,
    val orientation: Orientation,
    val isScrollInProgress: Boolean,
)

// ---------------------------------------------------------------------------
// 공개 Composable API
// Public Composable API
// ---------------------------------------------------------------------------

/**
 * Returns a [State] that tracks the current scroll direction of a [LazyListState].<br>
 * [LazyListState]의 현재 스크롤 방향을 추적하는 [State]를 반환합니다.<br>
 *
 * The scroll axis is automatically detected from
 * [LazyListState.layoutInfo][androidx.compose.foundation.lazy.LazyListLayoutInfo.orientation].<br>
 * 스크롤 축은 [LazyListState.layoutInfo]의 orientation 값에서 자동으로 감지됩니다.<br>
 * - Vertical list → [ScrollDirection.UP] / [ScrollDirection.DOWN]<br>
 * - Horizontal list → [ScrollDirection.LEFT] / [ScrollDirection.RIGHT]<br>
 *
 * Direction changes only when the accumulated pixel delta reaches or exceeds [thresholdPx].
 * If the delta is below the threshold the previous direction is kept.
 * The initial value is [ScrollDirection.IDLE].<br>
 * 방향은 누적 픽셀 델타가 [thresholdPx] 이상일 때만 변경됩니다.
 * 델타가 임계값 미만이면 직전 방향을 유지합니다.
 * 초기값은 [ScrollDirection.IDLE]입니다.<br>
 *
 * **Note / 주의**: Unlike simple_xml's RecyclerScrollStateView, which re-emits
 * [ScrollDirection.IDLE] when scrolling stops, this state does **not** return to
 * [ScrollDirection.IDLE] after the first direction change — it keeps the last direction.<br>
 * 스크롤 정지 시 [ScrollDirection.IDLE]을 다시 발행하는 simple_xml의 RecyclerScrollStateView와 달리,
 * 이 상태는 최초 방향 변경 이후 [ScrollDirection.IDLE]로 **복귀하지 않고** 마지막 방향을 유지합니다.<br>
 *
 * **Programmatic jumps / 프로그램적 점프**: When the first visible index changes without an
 * active scroll motion (e.g., `scrollToItem` called while idle, or list data changes shifting the
 * index), the tracking baseline is reset and **no direction is emitted** — matching the xml
 * semantics where only actual scroll motion drives the direction.<br>
 * 스크롤 모션 없이 첫 가시 인덱스가 바뀌는 경우(유휴 상태의 `scrollToItem`, 리스트 데이터 변경 등)에는
 * 기준점만 재설정하고 **방향을 발행하지 않습니다** — 실제 스크롤 모션만 방향에 반영하는 xml 의미와 동일합니다.<br>
 *
 * Internally collects scroll deltas via [snapshotFlow] inside a [LaunchedEffect],
 * so reading the returned [State] is free of side effects.<br>
 * 내부적으로 [LaunchedEffect] 안에서 [snapshotFlow]로 스크롤 델타를 수집하므로,
 * 반환된 [State]를 읽는 동작에는 부수효과가 없습니다.<br>
 *
 * @param listState The [LazyListState] to observe.<br><br>
 *                  관찰할 [LazyListState].<br>
 * @param thresholdPx Minimum accumulated pixel delta required to register a direction change.
 *                    Defaults to 20.<br><br>
 *                    방향 변경으로 등록하기 위한 최소 누적 픽셀 델타. 기본값은 20입니다.<br>
 * @return [State] holding the current [ScrollDirection].<br><br>
 *         현재 [ScrollDirection]을 보유하는 [State].<br>
 */
@Composable
public fun rememberScrollDirectionState(
    listState: LazyListState,
    thresholdPx: Int = 20,
): State<ScrollDirection> {
    val direction = remember(listState, thresholdPx) { mutableStateOf(ScrollDirection.IDLE) }

    LaunchedEffect(listState, thresholdPx) {
        var previousIndex = listState.firstVisibleItemIndex
        var previousOffset = listState.firstVisibleItemScrollOffset
        var accumulatedDelta = 0

        snapshotFlow {
            ScrollObservation(
                index = listState.firstVisibleItemIndex,
                offset = listState.firstVisibleItemScrollOffset,
                orientation = listState.layoutInfo.orientation,
                isScrollInProgress = listState.isScrollInProgress,
            )
        }.collect { observation ->
            val currentIndex = observation.index
            val currentOffset = observation.offset
            val orientation = observation.orientation
            val frameDelta = when {
                // 같은 아이템 내 오프셋 이동 — 실제 픽셀 델타 사용
                // Offset movement within the same item — use the real pixel delta
                currentIndex == previousIndex -> currentOffset - previousOffset

                // 스크롤 모션 중 아이템 경계 통과 — 방향 부호만 사용해 임계값 초과 유도
                // Crossing an item boundary during scroll motion — inject the threshold by sign
                observation.isScrollInProgress ->
                    if (currentIndex > previousIndex) thresholdPx else -thresholdPx

                // 스크롤 모션 없는 인덱스 변경(프로그램적 점프·데이터 변경) — 기준점만 재설정, 방향 미발행
                // Index change without scroll motion (programmatic jump or data change) —
                // re-baseline only, no direction emission (matching xml motion-only semantics)
                else -> {
                    accumulatedDelta = 0
                    0
                }
            }

            accumulatedDelta += frameDelta
            previousIndex = currentIndex
            previousOffset = currentOffset

            val resolved = resolveScrollDirection(
                accumulatedDelta = accumulatedDelta,
                thresholdPx = thresholdPx,
                isVertical = isVerticalByOrientation(orientation),
                previousDirection = direction.value,
            )

            // xml(RecyclerScrollStateCalculator)과 동일하게 임계값 도달 시 누적값을 항상 리셋한다.
            // 방향 변경 시에만 리셋하면 장거리 스크롤 후 역방향 감지가 누적 거리만큼 지연된다.
            // Matching the xml semantics, always reset the accumulation once the threshold is reached;
            // resetting only on direction change delays reverse detection by the accumulated distance.
            if (abs(accumulatedDelta) >= thresholdPx) {
                accumulatedDelta = 0
            }
            if (resolved != direction.value) {
                direction.value = resolved
            }
        }
    }

    return direction
}

/**
 * Returns a [State] that tracks whether the specified [edge] of a [LazyListState] is reached.<br>
 * [LazyListState]의 지정된 [edge]에 도달했는지 추적하는 [State]를 반환합니다.<br>
 *
 * The scroll axis is automatically detected from
 * [LazyListState.layoutInfo][androidx.compose.foundation.lazy.LazyListLayoutInfo.orientation].<br>
 * 스크롤 축은 [LazyListState.layoutInfo]의 orientation 값에서 자동으로 감지됩니다.<br>
 *
 * **Axis mismatch behavior (축 불일치 동작)**:<br>
 * If the requested [edge] does not match the list orientation, this function always returns `false`.<br>
 * 요청된 [edge]가 리스트 방향과 일치하지 않으면 이 함수는 항상 `false`를 반환합니다.<br>
 * - Vertical list + [ScrollEdge.LEFT] or [ScrollEdge.RIGHT] → always `false`<br>
 * - Horizontal list + [ScrollEdge.TOP] or [ScrollEdge.BOTTOM] → always `false`<br>
 *
 * Uses [derivedStateOf] internally to minimise recompositions.<br>
 * 재구성을 최소화하기 위해 내부적으로 [derivedStateOf]를 사용합니다.<br>
 *
 * @param listState The [LazyListState] to observe.<br><br>
 *                  관찰할 [LazyListState].<br>
 * @param edge The [ScrollEdge] to watch.<br><br>
 *             감시할 [ScrollEdge].<br>
 * @param thresholdPx Pixel distance from the edge considered as "reached".
 *                    Applies only to [ScrollEdge.TOP] and [ScrollEdge.LEFT] (index == 0 check).
 *                    Defaults to 10.<br><br>
 *                    "도달"로 간주되는 엣지로부터의 픽셀 거리.
 *                    [ScrollEdge.TOP] 및 [ScrollEdge.LEFT] 엣지에만 적용됩니다.
 *                    기본값은 10입니다.<br>
 * @return [State] holding `true` when the edge is reached, `false` otherwise.<br><br>
 *         엣지에 도달했을 때 `true`, 그렇지 않으면 `false`를 보유하는 [State].<br>
 */
@Composable
public fun rememberEdgeReachedState(
    listState: LazyListState,
    edge: ScrollEdge,
    thresholdPx: Int = 10,
): State<Boolean> =
    remember(listState, edge, thresholdPx) {
        derivedStateOf {
            val isVertical = isVerticalByOrientation(listState.layoutInfo.orientation)
            val firstVisibleIndex = listState.firstVisibleItemIndex
            val firstVisibleOffset = listState.firstVisibleItemScrollOffset
            val canScrollForward = listState.canScrollForward

            when {
                isVertical && (edge == ScrollEdge.TOP || edge == ScrollEdge.BOTTOM) ->
                    isVerticalEdgeReached(
                        edge = edge,
                        firstVisibleIndex = firstVisibleIndex,
                        firstVisibleOffset = firstVisibleOffset,
                        canScrollForward = canScrollForward,
                        thresholdPx = thresholdPx,
                    )
                !isVertical && (edge == ScrollEdge.LEFT || edge == ScrollEdge.RIGHT) ->
                    isHorizontalEdgeReached(
                        edge = edge,
                        firstVisibleIndex = firstVisibleIndex,
                        firstVisibleOffset = firstVisibleOffset,
                        canScrollForward = canScrollForward,
                        thresholdPx = thresholdPx,
                    )
                // 축 불일치: 항상 false 반환
                // Axis mismatch: always return false
                else -> false
            }
        }
    }
