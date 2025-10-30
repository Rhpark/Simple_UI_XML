package kr.open.library.simple_ui.system_manager.controller.window.vo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FloatingViewTypeTest {

    @Test
    fun floatingViewCollisionsType_hasAllEntries() {
        val types = listOf(
            FloatingViewCollisionsType.OCCURING,
            FloatingViewCollisionsType.UNCOLLISIONS,
        )

        assertEquals(2, types.toSet().size)
        types.forEach { type ->
            assertTrue(type is FloatingViewCollisionsType)
        }
    }

    @Test
    fun floatingViewTouchType_hasAllEntries() {
        val types = listOf(
            FloatingViewTouchType.TOUCH_DOWN,
            FloatingViewTouchType.TOUCH_MOVE,
            FloatingViewTouchType.TOUCH_UP,
        )

        assertEquals(3, types.toSet().size)
        types.forEach { type ->
            assertTrue(type is FloatingViewTouchType)
        }
    }
}
