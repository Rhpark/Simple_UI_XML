package kr.open.library.simple_ui.unit.system_manager.controller.window.vo

import kr.open.library.simple_ui.system_manager.controller.window.vo.FloatingViewCollisionsType
import kr.open.library.simple_ui.system_manager.controller.window.vo.FloatingViewTouchType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for FloatingViewCollisionsType and FloatingViewTouchType enums
 */
class FloatingViewTypeUnitTest {

    // FloatingViewCollisionsType Tests
    @Test
    fun floatingViewCollisionsType_hasCorrectNumberOfValues() {
        val entries = FloatingViewCollisionsType.entries
        assertEquals(2, entries.size)
    }

    @Test
    fun floatingViewCollisionsType_containsAllExpectedValues() {
        val entries = FloatingViewCollisionsType.entries
        assertTrue(entries.contains(FloatingViewCollisionsType.OCCURING))
        assertTrue(entries.contains(FloatingViewCollisionsType.UNCOLLISIONS))
    }

    @Test
    fun floatingViewCollisionsType_valueOf_returnsCorrectEnum() {
        assertEquals(FloatingViewCollisionsType.OCCURING, FloatingViewCollisionsType.valueOf("OCCURING"))
        assertEquals(FloatingViewCollisionsType.UNCOLLISIONS, FloatingViewCollisionsType.valueOf("UNCOLLISIONS"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun floatingViewCollisionsType_valueOf_throwsExceptionForInvalidName() {
        FloatingViewCollisionsType.valueOf("INVALID")
    }

    @Test
    fun floatingViewCollisionsType_enumName_matchesExpectedFormat() {
        assertEquals("OCCURING", FloatingViewCollisionsType.OCCURING.name)
        assertEquals("UNCOLLISIONS", FloatingViewCollisionsType.UNCOLLISIONS.name)
    }

    // FloatingViewTouchType Tests
    @Test
    fun floatingViewTouchType_hasCorrectNumberOfValues() {
        val entries = FloatingViewTouchType.entries
        assertEquals(3, entries.size)
    }

    @Test
    fun floatingViewTouchType_containsAllExpectedValues() {
        val entries = FloatingViewTouchType.entries
        assertTrue(entries.contains(FloatingViewTouchType.TOUCH_DOWN))
        assertTrue(entries.contains(FloatingViewTouchType.TOUCH_MOVE))
        assertTrue(entries.contains(FloatingViewTouchType.TOUCH_UP))
    }

    @Test
    fun floatingViewTouchType_valueOf_returnsCorrectEnum() {
        assertEquals(FloatingViewTouchType.TOUCH_DOWN, FloatingViewTouchType.valueOf("TOUCH_DOWN"))
        assertEquals(FloatingViewTouchType.TOUCH_MOVE, FloatingViewTouchType.valueOf("TOUCH_MOVE"))
        assertEquals(FloatingViewTouchType.TOUCH_UP, FloatingViewTouchType.valueOf("TOUCH_UP"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun floatingViewTouchType_valueOf_throwsExceptionForInvalidName() {
        FloatingViewTouchType.valueOf("INVALID_TOUCH")
    }

    @Test
    fun floatingViewTouchType_enumName_matchesExpectedFormat() {
        assertEquals("TOUCH_DOWN", FloatingViewTouchType.TOUCH_DOWN.name)
        assertEquals("TOUCH_MOVE", FloatingViewTouchType.TOUCH_MOVE.name)
        assertEquals("TOUCH_UP", FloatingViewTouchType.TOUCH_UP.name)
    }

    @Test
    fun floatingViewTouchType_enumOrder_followsTouchEventFlow() {
        val entries = FloatingViewTouchType.entries
        assertEquals(FloatingViewTouchType.TOUCH_DOWN, entries[0])
        assertEquals(FloatingViewTouchType.TOUCH_MOVE, entries[1])
        assertEquals(FloatingViewTouchType.TOUCH_UP, entries[2])
    }
}
