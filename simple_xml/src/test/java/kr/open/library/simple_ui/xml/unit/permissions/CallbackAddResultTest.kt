package kr.open.library.simple_ui.xml.unit.permissions.manager

import kr.open.library.simple_ui.xml.permissions.manager.CallbackAddResult
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class CallbackAddResultTest {
    @Test
    fun values_containsAllExpectedEntries() {
        val expected =
            arrayOf(
                CallbackAddResult.SUCCESS,
                CallbackAddResult.REQUEST_NOT_FOUND,
                CallbackAddResult.PERMISSION_MISMATCH,
            )

        assertArrayEquals(expected, CallbackAddResult.values())
    }

    @Test
    fun valueOf_returnsEnumByName() {
        assertEquals(CallbackAddResult.SUCCESS, CallbackAddResult.valueOf("SUCCESS"))
        assertEquals(CallbackAddResult.REQUEST_NOT_FOUND, CallbackAddResult.valueOf("REQUEST_NOT_FOUND"))
        assertEquals(CallbackAddResult.PERMISSION_MISMATCH, CallbackAddResult.valueOf("PERMISSION_MISMATCH"))
    }
}
