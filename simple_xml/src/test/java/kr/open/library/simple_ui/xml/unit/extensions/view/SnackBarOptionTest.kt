package kr.open.library.simple_ui.xml.unit.extensions.view

import kr.open.library.simple_ui.xml.extensions.view.SnackBarOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SnackBarOptionTest {
    @Test
    fun properties_areStoredAsProvided() {
        val option =
            SnackBarOption(
                animMode = 1,
                bgTint = 0x112233,
                bgTintStateList = null,
                textColor = 0x445566,
                textColorStateList = null,
                isGestureInsetBottomIgnored = true,
                actionTextColor = 0x778899,
                actionTextColorStateList = null,
                actionText = "Undo",
                action = {},
            )

        assertEquals(1, option.animMode)
        assertEquals(0x112233, option.bgTint)
        assertNull(option.bgTintStateList)
        assertEquals(0x445566, option.textColor)
        assertNull(option.textColorStateList)
        assertEquals(true, option.isGestureInsetBottomIgnored)
        assertEquals(0x778899, option.actionTextColor)
        assertNull(option.actionTextColorStateList)
        assertEquals("Undo", option.actionText)
        assertTrue(option.action != null)
    }

    @Test
    fun copy_updatesSelectedFields() {
        val original = SnackBarOption(actionText = "Retry")

        val copied = original.copy(actionText = "Dismiss")

        assertEquals("Retry", original.actionText)
        assertEquals("Dismiss", copied.actionText)
        assertNull(copied.bgTint)
    }
}
