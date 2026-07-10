package kr.open.library.simple_ui.compose.robolectric.scroll

import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.compose.scroll.ScrollDirection
import kr.open.library.simple_ui.compose.scroll.ScrollEdge
import kr.open.library.simple_ui.compose.scroll.rememberEdgeReachedState
import kr.open.library.simple_ui.compose.scroll.rememberScrollDirectionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric + createComposeRule 기반 스크롤 상태 헬퍼 통합 테스트.<br>
 * Integration tests for scroll state helpers using Robolectric and createComposeRule.<br>
 *
 * 이 테스트 클래스는 ui-test-junit4 + Robolectric 조합 동작을 검증하는 게이트 역할을 합니다.<br>
 * This test class acts as a gate validating the ui-test-junit4 + Robolectric combination.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ScrollStateHelpersRobolectricTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // -----------------------------------------------------------------------
    // rememberScrollDirectionState — 초기 상태
    // rememberScrollDirectionState — initial state
    // -----------------------------------------------------------------------

    @Test
    fun `rememberScrollDirectionState initial value is IDLE for vertical list`() {
        var direction: ScrollDirection = ScrollDirection.DOWN

        composeTestRule.setContent {
            val listState = rememberLazyListState()
            val directionState by rememberScrollDirectionState(listState)
            direction = directionState
            LazyColumn(state = listState) {
                items(5) {}
            }
        }

        composeTestRule.waitForIdle()
        assertEquals(ScrollDirection.IDLE, direction)
    }

    @Test
    fun `rememberScrollDirectionState initial value is IDLE for horizontal list`() {
        var direction: ScrollDirection = ScrollDirection.RIGHT

        composeTestRule.setContent {
            val listState = rememberLazyListState()
            val directionState by rememberScrollDirectionState(listState)
            direction = directionState
            LazyRow(state = listState) {
                items(5) {}
            }
        }

        composeTestRule.waitForIdle()
        assertEquals(ScrollDirection.IDLE, direction)
    }

    // -----------------------------------------------------------------------
    // rememberEdgeReachedState — 초기 상태
    // rememberEdgeReachedState — initial state
    // -----------------------------------------------------------------------

    @Test
    fun `rememberEdgeReachedState TOP is true at initial position for vertical list`() {
        var isTopReached = false

        composeTestRule.setContent {
            val listState = rememberLazyListState()
            val edgeState by rememberEdgeReachedState(listState, ScrollEdge.TOP)
            isTopReached = edgeState
            LazyColumn(state = listState) {
                items(20) {
                    Box(modifier = Modifier.fillMaxWidth().height(50.dp))
                }
            }
        }

        composeTestRule.waitForIdle()
        assertTrue(isTopReached)
    }

    @Test
    fun `rememberEdgeReachedState BOTTOM is false when list has more items`() {
        var isBottomReached = true

        composeTestRule.setContent {
            val listState = rememberLazyListState()
            val edgeState by rememberEdgeReachedState(listState, ScrollEdge.BOTTOM)
            isBottomReached = edgeState
            // 아이템이 충분히 많아 스크롤 가능한 상태
            // Enough items to be scrollable
            LazyColumn(state = listState) {
                items(100) {
                    Box(modifier = Modifier.fillMaxWidth().height(50.dp))
                }
            }
        }

        composeTestRule.waitForIdle()
        assertFalse(isBottomReached)
    }

    // -----------------------------------------------------------------------
    // rememberEdgeReachedState — 축 불일치 동작 검증
    // rememberEdgeReachedState — axis mismatch behavior
    // -----------------------------------------------------------------------

    @Test
    fun `rememberEdgeReachedState returns false for LEFT edge on vertical list (axis mismatch)`() {
        var isLeftReached = true

        composeTestRule.setContent {
            val listState = rememberLazyListState()
            val edgeState by rememberEdgeReachedState(listState, ScrollEdge.LEFT)
            isLeftReached = edgeState
            LazyColumn(state = listState) {
                items(5) {
                    Box(modifier = Modifier.fillMaxWidth().height(50.dp))
                }
            }
        }

        composeTestRule.waitForIdle()
        assertFalse("수직 리스트에서 LEFT 엣지는 항상 false여야 합니다", isLeftReached)
    }

    @Test
    fun `rememberEdgeReachedState returns false for RIGHT edge on vertical list (axis mismatch)`() {
        var isRightReached = true

        composeTestRule.setContent {
            val listState = rememberLazyListState()
            val edgeState by rememberEdgeReachedState(listState, ScrollEdge.RIGHT)
            isRightReached = edgeState
            LazyColumn(state = listState) {
                items(5) {
                    Box(modifier = Modifier.fillMaxWidth().height(50.dp))
                }
            }
        }

        composeTestRule.waitForIdle()
        assertFalse("수직 리스트에서 RIGHT 엣지는 항상 false여야 합니다", isRightReached)
    }

    @Test
    fun `rememberEdgeReachedState returns false for TOP edge on horizontal list (axis mismatch)`() {
        var isTopReached = true

        composeTestRule.setContent {
            val listState = rememberLazyListState()
            val edgeState by rememberEdgeReachedState(listState, ScrollEdge.TOP)
            isTopReached = edgeState
            LazyRow(state = listState) {
                items(5) {
                    Box(modifier = Modifier.height(50.dp))
                }
            }
        }

        composeTestRule.waitForIdle()
        assertFalse("수평 리스트에서 TOP 엣지는 항상 false여야 합니다", isTopReached)
    }

    @Test
    fun `rememberEdgeReachedState returns false for BOTTOM edge on horizontal list (axis mismatch)`() {
        var isBottomReached = true

        composeTestRule.setContent {
            val listState = rememberLazyListState()
            val edgeState by rememberEdgeReachedState(listState, ScrollEdge.BOTTOM)
            isBottomReached = edgeState
            LazyRow(state = listState) {
                items(5) {
                    Box(modifier = Modifier.height(50.dp))
                }
            }
        }

        composeTestRule.waitForIdle()
        assertFalse("수평 리스트에서 BOTTOM 엣지는 항상 false여야 합니다", isBottomReached)
    }

    // -----------------------------------------------------------------------
    // rememberEdgeReachedState — 수평 초기 상태
    // rememberEdgeReachedState — horizontal initial state
    // -----------------------------------------------------------------------

    @Test
    fun `rememberEdgeReachedState LEFT is true at initial position for horizontal list`() {
        var isLeftReached = false

        composeTestRule.setContent {
            val listState = rememberLazyListState()
            val edgeState by rememberEdgeReachedState(listState, ScrollEdge.LEFT)
            isLeftReached = edgeState
            LazyRow(state = listState) {
                items(20) {
                    Box(modifier = Modifier.height(50.dp))
                }
            }
        }

        composeTestRule.waitForIdle()
        assertTrue(isLeftReached)
    }

    // -----------------------------------------------------------------------
    // rememberScrollDirectionState — 방향 전환 (실스크롤 기반)
    // rememberScrollDirectionState — direction change with actual scrolling
    // -----------------------------------------------------------------------

    /**
     * 수직 방향 전환 테스트용 콘텐츠를 구성하고 (방향 읽기 함수, listState)를 반환합니다.<br>
     * Sets up vertical direction-test content and returns (direction reader, listState).<br>
     */
    private fun setVerticalDirectionContent(itemHeightDp: Int = 1000): Pair<() -> ScrollDirection, () -> LazyListState> {
        var direction: ScrollDirection = ScrollDirection.IDLE
        lateinit var listState: LazyListState

        composeTestRule.setContent {
            listState = rememberLazyListState()
            val directionState by rememberScrollDirectionState(listState)
            direction = directionState
            // 아이템을 크게 잡아 dispatchRawDelta가 같은 아이템 내 오프셋 이동(실제 델타 경로)으로 처리되게 한다
            // Large items keep dispatchRawDelta within the same item (real-delta path)
            LazyColumn(state = listState, modifier = Modifier.height(200.dp)) {
                items(50) {
                    Box(modifier = Modifier.fillMaxWidth().height(itemHeightDp.dp))
                }
            }
        }
        composeTestRule.waitForIdle()
        return Pair({ direction }, { listState })
    }

    @Test
    fun `direction becomes DOWN after scrolling forward beyond threshold`() {
        val (direction, listState) = setVerticalDirectionContent()

        composeTestRule.runOnIdle { listState().dispatchRawDelta(30f) }
        composeTestRule.waitForIdle()

        assertEquals(ScrollDirection.DOWN, direction())
    }

    @Test
    fun `direction returns to IDLE after an animated scroll session ends`() {
        val observedDirections = mutableListOf<ScrollDirection>()
        lateinit var listState: LazyListState
        lateinit var scroll: () -> Unit

        composeTestRule.setContent {
            listState = rememberLazyListState()
            val scope = rememberCoroutineScope()
            val directionState = rememberScrollDirectionState(listState)
            scroll = {
                scope.launch {
                    listState.animateScrollBy(
                        value = 100f,
                        animationSpec = tween(durationMillis = 200),
                    )
                }
            }
            LaunchedEffect(directionState) {
                snapshotFlow { directionState.value }.collect { observedDirections += it }
            }
            LazyColumn(state = listState, modifier = Modifier.height(200.dp)) {
                items(50) {
                    Box(modifier = Modifier.fillMaxWidth().height(1000.dp))
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle { scroll() }
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            observedDirections.contains(ScrollDirection.DOWN) &&
                observedDirections.lastOrNull() == ScrollDirection.IDLE
        }
    }

    @Test
    fun `direction stays IDLE when scroll is below threshold`() {
        val (direction, listState) = setVerticalDirectionContent()

        composeTestRule.runOnIdle { listState().dispatchRawDelta(10f) }
        composeTestRule.waitForIdle()

        assertEquals(ScrollDirection.IDLE, direction())
    }

    @Test
    fun `direction becomes UP right after small reverse scroll following long forward scroll`() {
        // HIGH 회귀 테스트: 장거리 정방향 스크롤 후 임계값만큼만 되돌려도 즉시 UP이 감지되어야 한다
        // HIGH regression test: after a long forward scroll, reversing by just the threshold must yield UP
        val (direction, listState) = setVerticalDirectionContent()

        // 장거리 정방향 스크롤 (여러 프레임으로 분할 — 누적 리셋 경로 검증)
        // Long forward scroll split across frames to exercise the accumulation-reset path
        repeat(5) {
            composeTestRule.runOnIdle { listState().dispatchRawDelta(100f) }
            composeTestRule.waitForIdle()
        }
        assertEquals(ScrollDirection.DOWN, direction())

        composeTestRule.runOnIdle { listState().dispatchRawDelta(-20f) }
        composeTestRule.waitForIdle()

        assertEquals(
            "장거리 스크롤 후 임계값(20px)만큼만 되돌려도 UP이 감지되어야 합니다",
            ScrollDirection.UP,
            direction(),
        )
    }

    @Test
    fun `horizontal direction becomes RIGHT then LEFT with reverse scroll`() {
        var direction: ScrollDirection = ScrollDirection.IDLE
        lateinit var listState: LazyListState

        composeTestRule.setContent {
            listState = rememberLazyListState()
            val directionState by rememberScrollDirectionState(listState)
            direction = directionState
            LazyRow(state = listState, modifier = Modifier.width(200.dp)) {
                items(50) {
                    Box(modifier = Modifier.width(1000.dp).height(50.dp))
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle { listState.dispatchRawDelta(100f) }
        composeTestRule.waitForIdle()
        assertEquals(ScrollDirection.RIGHT, direction)

        composeTestRule.runOnIdle { listState.dispatchRawDelta(-20f) }
        composeTestRule.waitForIdle()
        assertEquals(
            "수평 리스트에서 임계값만큼 되돌리면 LEFT가 감지되어야 합니다",
            ScrollDirection.LEFT,
            direction,
        )
    }

    @Test
    fun `index jump without scroll motion does not emit a direction`() {
        // 적대적 리뷰 회귀 테스트: 스크롤 모션 없는 인덱스 점프(프로그램적 이동·데이터 변경)는
        // 방향을 발행하지 않아야 한다 — xml은 스크롤 모션 델타만 방향에 반영
        // Adversarial-review regression: an index jump without scroll motion (programmatic move
        // or data change) must not emit a direction — xml reflects only scroll-motion deltas
        val (direction, listState) = setVerticalDirectionContent(itemHeightDp = 50)

        // 작은 아이템(50dp) 위에서 대형 델타 → 유휴 상태 인덱스 점프
        // Large delta over small (50dp) items → an idle index jump
        composeTestRule.runOnIdle { listState().dispatchRawDelta(500f) }
        composeTestRule.waitForIdle()

        assertEquals(
            "스크롤 모션 없는 인덱스 점프는 방향을 바꾸지 않아야 합니다",
            ScrollDirection.IDLE,
            direction(),
        )
    }
}
