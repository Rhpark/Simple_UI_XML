package kr.open.library.simple_ui.xml.unit.presenter.ui.view.recyclerview

import kr.open.library.simple_ui.xml.ui.view.recyclerview.ScrollDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScrollDirectionTest {
    @Test
    fun allDirectionsAreAccessible() {
        val directions =
            listOf(
                ScrollDirection.UP,
                ScrollDirection.DOWN,
                ScrollDirection.LEFT,
                ScrollDirection.RIGHT,
                ScrollDirection.IDLE,
            )

        assertEquals(5, directions.toSet().size)
        directions.forEach { direction ->
            assertTrue(direction is ScrollDirection)
        }
    }
}
