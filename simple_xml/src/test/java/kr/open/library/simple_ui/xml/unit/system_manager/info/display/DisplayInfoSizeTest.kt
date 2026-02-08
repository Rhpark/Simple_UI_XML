package kr.open.library.simple_ui.xml.unit.system_manager.info.display

import kr.open.library.simple_ui.xml.system_manager.info.display.DisplayInfoSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for DisplayInfoSize data class.<br><br>
 * DisplayInfoSize 데이터 클래스에 대한 단위 테스트입니다.<br>
 */
class DisplayInfoSizeTest {
    // ==============================================
    // Constructor Tests
    // ==============================================

    @Test
    fun `DisplayInfoSize stores width and height`() {
        val size = DisplayInfoSize(1080, 1920)
        assertEquals(1080, size.width)
        assertEquals(1920, size.height)
    }

    @Test
    fun `DisplayInfoSize with zero values`() {
        val size = DisplayInfoSize(0, 0)
        assertEquals(0, size.width)
        assertEquals(0, size.height)
    }

    @Test
    fun `DisplayInfoSize with negative values`() {
        val size = DisplayInfoSize(-1, -1)
        assertEquals(-1, size.width)
        assertEquals(-1, size.height)
    }

    // ==============================================
    // Equality Tests
    // ==============================================

    @Test
    fun `same width and height are equal`() {
        val a = DisplayInfoSize(1080, 1920)
        val b = DisplayInfoSize(1080, 1920)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `different width are not equal`() {
        val a = DisplayInfoSize(1080, 1920)
        val b = DisplayInfoSize(720, 1920)
        assertNotEquals(a, b)
    }

    @Test
    fun `different height are not equal`() {
        val a = DisplayInfoSize(1080, 1920)
        val b = DisplayInfoSize(1080, 2400)
        assertNotEquals(a, b)
    }

    @Test
    fun `zero size equals another zero size`() {
        assertEquals(DisplayInfoSize(0, 0), DisplayInfoSize(0, 0))
    }

    // ==============================================
    // Copy Tests
    // ==============================================

    @Test
    fun `copy with different width`() {
        val original = DisplayInfoSize(1080, 1920)
        val copied = original.copy(width = 720)
        assertEquals(720, copied.width)
        assertEquals(1920, copied.height)
    }

    @Test
    fun `copy with different height`() {
        val original = DisplayInfoSize(1080, 1920)
        val copied = original.copy(height = 2400)
        assertEquals(1080, copied.width)
        assertEquals(2400, copied.height)
    }

    @Test
    fun `copy without changes equals original`() {
        val original = DisplayInfoSize(1080, 1920)
        assertEquals(original, original.copy())
    }

    // ==============================================
    // Destructuring Tests
    // ==============================================

    @Test
    fun `destructuring returns width and height in order`() {
        val size = DisplayInfoSize(1080, 1920)
        val (w, h) = size
        assertEquals(1080, w)
        assertEquals(1920, h)
    }

    // ==============================================
    // toString Tests
    // ==============================================

    @Test
    fun `toString contains width and height`() {
        val size = DisplayInfoSize(1080, 1920)
        val str = size.toString()
        assertTrue(str.contains("1080"))
        assertTrue(str.contains("1920"))
    }
}
