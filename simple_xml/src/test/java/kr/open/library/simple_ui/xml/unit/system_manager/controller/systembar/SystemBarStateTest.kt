package kr.open.library.simple_ui.xml.unit.system_manager.controller.systembar

import android.graphics.Rect
import kr.open.library.simple_ui.xml.system_manager.controller.systembar.model.SystemBarStableState
import kr.open.library.simple_ui.xml.system_manager.controller.systembar.model.SystemBarVisibleState
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for SystemBarVisibleState and SystemBarStableState sealed models.<br><br>
 * SystemBarVisibleState와 SystemBarStableState sealed 모델에 대한 단위 테스트입니다.<br>
 *
 * NOTE: local unit 환경의 mockable android.jar에서는 Rect의 equals/hashCode/toString 및 필드값이
 * 신뢰되지 않을 수 있으므로, 여기서는 "참조 전달"과 "타입 계약"만 검증합니다.<br>
 */
class SystemBarStateTest {
    // ── SystemBarVisibleState: data object singleton ─────────────

    @Test
    fun `NotReady is singleton data object`() {
        val a = SystemBarVisibleState.NotReady
        val b = SystemBarVisibleState.NotReady
        assertTrue(a === b)
    }

    @Test
    fun `NotPresent is singleton data object`() {
        val a = SystemBarVisibleState.NotPresent
        val b = SystemBarVisibleState.NotPresent
        assertTrue(a === b)
    }

    @Test
    fun `Hidden is singleton data object`() {
        val a = SystemBarVisibleState.Hidden
        val b = SystemBarVisibleState.Hidden
        assertTrue(a === b)
    }

    // ── SystemBarVisibleState: Visible data class ────────────────

    @Test
    fun `Visible stores rect reference`() {
        val rect = Rect(0, 0, 1080, 96)
        val visible = SystemBarVisibleState.Visible(rect)
        assertSame(rect, visible.rect)
    }

    @Test
    fun `Visible can hold independent rect references`() {
        val firstRect = Rect(0, 0, 1080, 96)
        val secondRect = Rect(0, 0, 1080, 96)
        val first = SystemBarVisibleState.Visible(firstRect)
        val second = SystemBarVisibleState.Visible(secondRect)

        assertSame(firstRect, first.rect)
        assertSame(secondRect, second.rect)
        assertTrue(first.rect !== second.rect)
    }

    @Test
    fun `Visible copy replaces rect reference`() {
        val originalRect = Rect(0, 0, 1080, 96)
        val copiedRect = Rect(0, 0, 720, 48)
        val original = SystemBarVisibleState.Visible(originalRect)
        val copied = original.copy(rect = copiedRect)

        assertSame(originalRect, original.rect)
        assertSame(copiedRect, copied.rect)
        assertTrue(original.rect !== copied.rect)
    }

    // ── SystemBarVisibleState: type hierarchy ────────────────────

    @Test
    fun `NotReady implements SystemBarVisibleState`() {
        val state: SystemBarVisibleState = SystemBarVisibleState.NotReady
        assertTrue(state is SystemBarVisibleState.NotReady)
    }

    @Test
    fun `NotPresent implements SystemBarVisibleState`() {
        val state: SystemBarVisibleState = SystemBarVisibleState.NotPresent
        assertTrue(state is SystemBarVisibleState.NotPresent)
    }

    @Test
    fun `Hidden implements SystemBarVisibleState`() {
        val state: SystemBarVisibleState = SystemBarVisibleState.Hidden
        assertTrue(state is SystemBarVisibleState.Hidden)
    }

    @Test
    fun `Visible implements SystemBarVisibleState`() {
        val state: SystemBarVisibleState = SystemBarVisibleState.Visible(Rect(0, 0, 100, 50))
        assertTrue(state is SystemBarVisibleState.Visible)
    }

    // ── SystemBarVisibleState: mutual exclusivity ────────────────

    @Test
    fun `NotReady is not NotPresent or Hidden or Visible`() {
        val state: SystemBarVisibleState = SystemBarVisibleState.NotReady
        assertTrue(state !is SystemBarVisibleState.NotPresent)
        assertTrue(state !is SystemBarVisibleState.Hidden)
        assertTrue(state !is SystemBarVisibleState.Visible)
    }

    @Test
    fun `Hidden is not NotReady or NotPresent or Visible`() {
        val state: SystemBarVisibleState = SystemBarVisibleState.Hidden
        assertTrue(state !is SystemBarVisibleState.NotReady)
        assertTrue(state !is SystemBarVisibleState.NotPresent)
        assertTrue(state !is SystemBarVisibleState.Visible)
    }

    @Test
    fun `when expression covers all VisibleState subtypes`() {
        val states = listOf(
            SystemBarVisibleState.NotReady,
            SystemBarVisibleState.NotPresent,
            SystemBarVisibleState.Hidden,
            SystemBarVisibleState.Visible(Rect(0, 0, 100, 50)),
        )

        states.forEach { state ->
            val label = when (state) {
                SystemBarVisibleState.NotReady -> "not_ready"
                SystemBarVisibleState.NotPresent -> "not_present"
                SystemBarVisibleState.Hidden -> "hidden"
                is SystemBarVisibleState.Visible -> "visible"
            }
            assertTrue(label.isNotEmpty())
        }
    }

    // ── SystemBarStableState: data object singleton ──────────────

    @Test
    fun `Stable NotReady is singleton data object`() {
        val a = SystemBarStableState.NotReady
        val b = SystemBarStableState.NotReady
        assertTrue(a === b)
    }

    @Test
    fun `Stable NotPresent is singleton data object`() {
        val a = SystemBarStableState.NotPresent
        val b = SystemBarStableState.NotPresent
        assertTrue(a === b)
    }

    // ── SystemBarStableState: Stable data class ──────────────────

    @Test
    fun `Stable stores rect reference`() {
        val rect = Rect(0, 0, 1080, 96)
        val stable = SystemBarStableState.Stable(rect)
        assertSame(rect, stable.rect)
    }

    @Test
    fun `Stable can hold independent rect references`() {
        val firstRect = Rect(0, 0, 1080, 96)
        val secondRect = Rect(0, 0, 1080, 96)
        val first = SystemBarStableState.Stable(firstRect)
        val second = SystemBarStableState.Stable(secondRect)

        assertSame(firstRect, first.rect)
        assertSame(secondRect, second.rect)
        assertTrue(first.rect !== second.rect)
    }

    @Test
    fun `Stable copy replaces rect reference`() {
        val originalRect = Rect(0, 0, 1080, 96)
        val copiedRect = Rect(0, 0, 720, 48)
        val original = SystemBarStableState.Stable(originalRect)
        val copied = original.copy(rect = copiedRect)

        assertSame(originalRect, original.rect)
        assertSame(copiedRect, copied.rect)
        assertTrue(original.rect !== copied.rect)
    }

    // ── SystemBarStableState: type hierarchy ─────────────────────

    @Test
    fun `Stable NotReady implements SystemBarStableState`() {
        val state: SystemBarStableState = SystemBarStableState.NotReady
        assertTrue(state is SystemBarStableState.NotReady)
    }

    @Test
    fun `Stable NotPresent implements SystemBarStableState`() {
        val state: SystemBarStableState = SystemBarStableState.NotPresent
        assertTrue(state is SystemBarStableState.NotPresent)
    }

    @Test
    fun `Stable implements SystemBarStableState`() {
        val state: SystemBarStableState = SystemBarStableState.Stable(Rect(0, 0, 100, 50))
        assertTrue(state is SystemBarStableState.Stable)
    }

    // ── SystemBarStableState: mutual exclusivity ─────────────────

    @Test
    fun `Stable NotReady is not NotPresent or Stable`() {
        val state: SystemBarStableState = SystemBarStableState.NotReady
        assertTrue(state !is SystemBarStableState.NotPresent)
        assertTrue(state !is SystemBarStableState.Stable)
    }

    @Test
    fun `when expression covers all StableState subtypes`() {
        val states = listOf(
            SystemBarStableState.NotReady,
            SystemBarStableState.NotPresent,
            SystemBarStableState.Stable(Rect(0, 0, 100, 50)),
        )

        states.forEach { state ->
            val label = when (state) {
                SystemBarStableState.NotReady -> "not_ready"
                SystemBarStableState.NotPresent -> "not_present"
                is SystemBarStableState.Stable -> "stable"
            }
            assertTrue(label.isNotEmpty())
        }
    }

    // ── Cross-type: VisibleState and StableState are independent ─

    @Test
    fun `VisibleState and StableState are different types`() {
        val visible: Any = SystemBarVisibleState.NotReady
        val stable: Any = SystemBarStableState.NotReady
        assertTrue(visible !is SystemBarStableState)
        assertTrue(stable !is SystemBarVisibleState)
    }
}
