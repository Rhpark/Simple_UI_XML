package kr.open.library.simple_ui.xml.unit.system_manager.controller.softkeyboard

import kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard.SoftKeyboardResizePolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for SoftKeyboardResizePolicy.<br><br>
 * SoftKeyboardResizePolicy에 대한 단위 테스트입니다.<br>
 */
class SoftKeyboardResizePolicyTest {
    // ==============================================
    // Enum Entries Tests
    // ==============================================

    @Test
    fun `all policy values are accessible`() {
        val policies = SoftKeyboardResizePolicy.entries
        assertEquals(3, policies.size)
    }

    @Test
    fun `all policy values are distinct`() {
        val policies = SoftKeyboardResizePolicy.entries.toSet()
        assertEquals(3, policies.size)
    }

    @Test
    fun `entries contain KEEP_CURRENT_WINDOW`() {
        assertTrue(SoftKeyboardResizePolicy.entries.contains(SoftKeyboardResizePolicy.KEEP_CURRENT_WINDOW))
    }

    @Test
    fun `entries contain LEGACY_ADJUST_RESIZE`() {
        assertTrue(SoftKeyboardResizePolicy.entries.contains(SoftKeyboardResizePolicy.LEGACY_ADJUST_RESIZE))
    }

    @Test
    fun `entries contain FORCE_DECOR_FITS_TRUE`() {
        assertTrue(SoftKeyboardResizePolicy.entries.contains(SoftKeyboardResizePolicy.FORCE_DECOR_FITS_TRUE))
    }

    // ==============================================
    // Name Tests
    // ==============================================

    @Test
    fun `policy names match expected values`() {
        val expectedNames = setOf(
            "KEEP_CURRENT_WINDOW",
            "LEGACY_ADJUST_RESIZE",
            "FORCE_DECOR_FITS_TRUE",
        )
        val actualNames = SoftKeyboardResizePolicy.entries.map { it.name }.toSet()
        assertEquals(expectedNames, actualNames)
    }

    // ==============================================
    // valueOf Tests
    // ==============================================

    @Test
    fun `valueOf works for all values`() {
        SoftKeyboardResizePolicy.entries.forEach { policy ->
            assertEquals(policy, SoftKeyboardResizePolicy.valueOf(policy.name))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `valueOf throws for invalid name`() {
        SoftKeyboardResizePolicy.valueOf("INVALID_POLICY")
    }

    // ==============================================
    // Ordinal Tests
    // ==============================================

    @Test
    fun `ordinal values are sequential from 0`() {
        SoftKeyboardResizePolicy.entries.forEachIndexed { index, policy ->
            assertEquals(index, policy.ordinal)
        }
    }

    @Test
    fun `KEEP_CURRENT_WINDOW is first entry`() {
        assertEquals(0, SoftKeyboardResizePolicy.KEEP_CURRENT_WINDOW.ordinal)
    }
}
