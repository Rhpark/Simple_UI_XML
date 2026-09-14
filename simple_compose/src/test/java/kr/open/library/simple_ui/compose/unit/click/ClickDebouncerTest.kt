package kr.open.library.simple_ui.compose.unit.click

import kr.open.library.simple_ui.compose.click.ClickDebouncer
import kr.open.library.simple_ui.compose.click.isClickAccepted
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 클릭 차단 순수 로직 검증 테스트입니다.<br>
 * Unit tests for the pure click debounce logic.<br>
 */
class ClickDebouncerTest {
    @Test
    fun `첫 클릭은 항상 허용된다`() {
        val debouncer = ClickDebouncer(debounceMs = 500L)

        assertTrue(debouncer.tryAccept(nowMs = 1_000L))
    }

    @Test
    fun `차단 간격 이내의 연속 클릭은 무시된다`() {
        val debouncer = ClickDebouncer(debounceMs = 500L)
        debouncer.tryAccept(nowMs = 1_000L)

        assertFalse(debouncer.tryAccept(nowMs = 1_100L))
        assertFalse(debouncer.tryAccept(nowMs = 1_499L))
    }

    @Test
    fun `차단 간격이 지난 클릭은 다시 허용된다`() {
        val debouncer = ClickDebouncer(debounceMs = 500L)
        debouncer.tryAccept(nowMs = 1_000L)

        assertTrue(debouncer.tryAccept(nowMs = 1_500L))
    }

    @Test
    fun `무시된 클릭은 기준 시각을 갱신하지 않는다`() {
        val debouncer = ClickDebouncer(debounceMs = 500L)
        debouncer.tryAccept(nowMs = 1_000L)
        debouncer.tryAccept(nowMs = 1_400L)

        // 기준은 여전히 1_000L 이므로 1_500L 은 허용된다.
        assertTrue(debouncer.tryAccept(nowMs = 1_500L))
    }

    @Test
    fun `차단 간격이 0이면 모든 클릭이 허용된다`() {
        val debouncer = ClickDebouncer(debounceMs = 0L)

        assertTrue(debouncer.tryAccept(nowMs = 1_000L))
        assertTrue(debouncer.tryAccept(nowMs = 1_000L))
    }

    @Test
    fun `경계값 판정은 간격 이상일 때 허용된다`() {
        assertFalse(isClickAccepted(lastClickTimeMs = 1_000L, nowMs = 1_499L, debounceMs = 500L))
        assertTrue(isClickAccepted(lastClickTimeMs = 1_000L, nowMs = 1_500L, debounceMs = 500L))
    }
}
