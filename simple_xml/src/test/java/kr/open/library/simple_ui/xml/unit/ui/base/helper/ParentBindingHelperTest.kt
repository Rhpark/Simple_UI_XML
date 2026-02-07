package kr.open.library.simple_ui.xml.unit.ui.base.helper

import kr.open.library.simple_ui.xml.ui.base.helper.ParentBindingHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ParentBindingHelperTest {
    @Test
    fun startEventVmCollect_runsOnce() {
        val helper = TestHelper()
        var count = 0

        assertTrue(helper.canStartEventCollect())

        helper.startEventVmCollect { count++ }

        assertEquals(1, count)
        assertFalse(helper.canStartEventCollect())
    }

    private class TestHelper : ParentBindingHelper()
}
