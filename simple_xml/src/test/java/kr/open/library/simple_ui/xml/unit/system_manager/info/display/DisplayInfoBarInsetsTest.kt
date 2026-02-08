package kr.open.library.simple_ui.xml.unit.system_manager.info.display

import kr.open.library.simple_ui.xml.system_manager.info.display.DisplayInfoBarInsets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for DisplayInfoBarInsets data class.<br><br>
 * DisplayInfoBarInsets 데이터 클래스에 대한 단위 테스트입니다.<br>
 */
class DisplayInfoBarInsetsTest {
    // ==============================================
    // Constructor Tests
    // ==============================================

    @Test
    fun `DisplayInfoBarInsets stores all four insets`() {
        val insets = DisplayInfoBarInsets(top = 50, bottom = 100, left = 0, right = 0)
        assertEquals(50, insets.top)
        assertEquals(100, insets.bottom)
        assertEquals(0, insets.left)
        assertEquals(0, insets.right)
    }

    @Test
    fun `DisplayInfoBarInsets with all zeros`() {
        val insets = DisplayInfoBarInsets(0, 0, 0, 0)
        assertEquals(0, insets.top)
        assertEquals(0, insets.bottom)
        assertEquals(0, insets.left)
        assertEquals(0, insets.right)
    }

    // ==============================================
    // thickness Tests
    // ==============================================

    @Test
    fun `thickness returns bottom when bottom is largest`() {
        val insets = DisplayInfoBarInsets(top = 0, bottom = 100, left = 0, right = 0)
        assertEquals(100, insets.thickness)
    }

    @Test
    fun `thickness returns top when top is largest`() {
        val insets = DisplayInfoBarInsets(top = 50, bottom = 0, left = 0, right = 0)
        assertEquals(50, insets.thickness)
    }

    @Test
    fun `thickness returns left when left is largest`() {
        val insets = DisplayInfoBarInsets(top = 0, bottom = 0, left = 80, right = 0)
        assertEquals(80, insets.thickness)
    }

    @Test
    fun `thickness returns right when right is largest`() {
        val insets = DisplayInfoBarInsets(top = 0, bottom = 0, left = 0, right = 60)
        assertEquals(60, insets.thickness)
    }

    @Test
    fun `thickness returns zero when all insets are zero`() {
        val insets = DisplayInfoBarInsets(0, 0, 0, 0)
        assertEquals(0, insets.thickness)
    }

    @Test
    fun `thickness returns max of all directions`() {
        val insets = DisplayInfoBarInsets(top = 10, bottom = 30, left = 20, right = 5)
        assertEquals(30, insets.thickness)
    }

    // ==============================================
    // isEmpty Tests
    // ==============================================

    @Test
    fun `isEmpty returns true when all insets are zero`() {
        val insets = DisplayInfoBarInsets(0, 0, 0, 0)
        assertTrue(insets.isEmpty)
    }

    @Test
    fun `isEmpty returns false when bottom has value`() {
        val insets = DisplayInfoBarInsets(top = 0, bottom = 100, left = 0, right = 0)
        assertFalse(insets.isEmpty)
    }

    @Test
    fun `isEmpty returns false when top has value`() {
        val insets = DisplayInfoBarInsets(top = 50, bottom = 0, left = 0, right = 0)
        assertFalse(insets.isEmpty)
    }

    @Test
    fun `isEmpty returns false when left has value`() {
        val insets = DisplayInfoBarInsets(top = 0, bottom = 0, left = 80, right = 0)
        assertFalse(insets.isEmpty)
    }

    @Test
    fun `isEmpty returns false when right has value`() {
        val insets = DisplayInfoBarInsets(top = 0, bottom = 0, left = 0, right = 60)
        assertFalse(insets.isEmpty)
    }

    // ==============================================
    // Equality Tests
    // ==============================================

    @Test
    fun `same insets are equal`() {
        val a = DisplayInfoBarInsets(50, 100, 0, 0)
        val b = DisplayInfoBarInsets(50, 100, 0, 0)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `different top values are not equal`() {
        val a = DisplayInfoBarInsets(50, 100, 0, 0)
        val b = DisplayInfoBarInsets(60, 100, 0, 0)
        assertNotEquals(a, b)
    }

    @Test
    fun `different bottom values are not equal`() {
        val a = DisplayInfoBarInsets(50, 100, 0, 0)
        val b = DisplayInfoBarInsets(50, 80, 0, 0)
        assertNotEquals(a, b)
    }

    @Test
    fun `different left values are not equal`() {
        val a = DisplayInfoBarInsets(0, 0, 10, 0)
        val b = DisplayInfoBarInsets(0, 0, 20, 0)
        assertNotEquals(a, b)
    }

    @Test
    fun `different right values are not equal`() {
        val a = DisplayInfoBarInsets(0, 0, 0, 10)
        val b = DisplayInfoBarInsets(0, 0, 0, 20)
        assertNotEquals(a, b)
    }

    // ==============================================
    // Copy Tests
    // ==============================================

    @Test
    fun `copy with different bottom`() {
        val original = DisplayInfoBarInsets(50, 100, 0, 0)
        val copied = original.copy(bottom = 200)
        assertEquals(50, copied.top)
        assertEquals(200, copied.bottom)
        assertEquals(0, copied.left)
        assertEquals(0, copied.right)
    }

    @Test
    fun `copy without changes equals original`() {
        val original = DisplayInfoBarInsets(50, 100, 0, 0)
        assertEquals(original, original.copy())
    }

    // ==============================================
    // Destructuring Tests
    // ==============================================

    @Test
    fun `destructuring returns top bottom left right in order`() {
        val insets = DisplayInfoBarInsets(10, 20, 30, 40)
        val (t, b, l, r) = insets
        assertEquals(10, t)
        assertEquals(20, b)
        assertEquals(30, l)
        assertEquals(40, r)
    }

    // ==============================================
    // toString Tests
    // ==============================================

    @Test
    fun `toString contains all inset values`() {
        val insets = DisplayInfoBarInsets(10, 20, 30, 40)
        val str = insets.toString()
        assertTrue(str.contains("10"))
        assertTrue(str.contains("20"))
        assertTrue(str.contains("30"))
        assertTrue(str.contains("40"))
    }

    // ==============================================
    // Semantic Usage Tests
    // ==============================================

    @Test
    fun `status bar insets pattern has top only`() {
        val statusBarInsets = DisplayInfoBarInsets(top = 66, bottom = 0, left = 0, right = 0)
        assertEquals(66, statusBarInsets.thickness)
        assertFalse(statusBarInsets.isEmpty)
        assertEquals(66, statusBarInsets.top)
        assertEquals(0, statusBarInsets.bottom)
    }

    @Test
    fun `navigation bar bottom insets pattern`() {
        val navBarInsets = DisplayInfoBarInsets(top = 0, bottom = 126, left = 0, right = 0)
        assertEquals(126, navBarInsets.thickness)
        assertFalse(navBarInsets.isEmpty)
    }

    @Test
    fun `navigation bar side insets pattern for landscape`() {
        val navBarInsets = DisplayInfoBarInsets(top = 0, bottom = 0, left = 0, right = 84)
        assertEquals(84, navBarInsets.thickness)
        assertFalse(navBarInsets.isEmpty)
    }

    @Test
    fun `gesture navigation insets pattern is empty`() {
        val gestureInsets = DisplayInfoBarInsets(0, 0, 0, 0)
        assertEquals(0, gestureInsets.thickness)
        assertTrue(gestureInsets.isEmpty)
    }
}
