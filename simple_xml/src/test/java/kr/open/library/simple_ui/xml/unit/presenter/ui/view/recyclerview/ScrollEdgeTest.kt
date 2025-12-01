package kr.open.library.simple_ui.xml.unit.presenter.ui.view.recyclerview

import kr.open.library.simple_ui.xml.ui.view.recyclerview.ScrollEdge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScrollEdgeTest {
    @Test
    fun allEdgesAreAccessible() {
        val edges =
            listOf(
                ScrollEdge.TOP,
                ScrollEdge.BOTTOM,
                ScrollEdge.LEFT,
                ScrollEdge.RIGHT,
            )

        assertEquals(4, edges.toSet().size)
        edges.forEach { edge ->
            assertTrue(edge is ScrollEdge)
        }
    }
}
