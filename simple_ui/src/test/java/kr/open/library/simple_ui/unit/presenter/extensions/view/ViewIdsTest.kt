package kr.open.library.simple_ui.unit.presenter.extensions.view

import kr.open.library.simple_ui.presenter.extensions.view.ViewIds
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ViewIdsTest {

    @Test
    fun ids_areUniqueAndPositive() {
        val ids = listOf(
            ViewIds.LAST_CLICK_TIME,
            ViewIds.FADE_ANIMATOR,
            ViewIds.TAG_OBSERVED_OWNER,
        )

        ids.forEachIndexed { index, id ->
            assertTrue("ID should be positive", id > 0)
            ids.drop(index + 1).forEach { other ->
                assertNotEquals("IDs should be unique", id, other)
            }
        }
    }
}
