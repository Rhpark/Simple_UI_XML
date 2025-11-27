package kr.open.library.simple_ui.xml.unit.presenter.extensions.view

import kr.open.library.simple_ui.xml.extensions.view.SlideDirection
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class SlideDirectionTest {

    @Test
    fun values_containsAllDirections() {
        val expected = arrayOf(
            SlideDirection.LEFT,
            SlideDirection.RIGHT,
            SlideDirection.TOP,
            SlideDirection.BOTTOM,
        )

        assertArrayEquals(expected, SlideDirection.values())
    }

    @Test
    fun valueOf_returnsMatchingDirection() {
        assertEquals(SlideDirection.LEFT, SlideDirection.valueOf("LEFT"))
        assertEquals(SlideDirection.RIGHT, SlideDirection.valueOf("RIGHT"))
        assertEquals(SlideDirection.TOP, SlideDirection.valueOf("TOP"))
        assertEquals(SlideDirection.BOTTOM, SlideDirection.valueOf("BOTTOM"))
    }
}
