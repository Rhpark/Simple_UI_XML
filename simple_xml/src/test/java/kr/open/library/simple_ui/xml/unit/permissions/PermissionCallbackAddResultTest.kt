package kr.open.library.simple_ui.xml.unit.permissions.manager

import kr.open.library.simple_ui.core.permissions.manager.PermissionCallbackAddResult
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionCallbackAddResultTest {
    @Test
    fun values_containsAllExpectedEntries() {
        val expected =
            arrayOf(
                PermissionCallbackAddResult.SUCCESS,
                PermissionCallbackAddResult.REQUEST_NOT_FOUND,
                PermissionCallbackAddResult.PERMISSION_MISMATCH,
            )

        assertArrayEquals(expected, PermissionCallbackAddResult.values())
    }

    @Test
    fun valueOf_returnsEnumByName() {
        assertEquals(PermissionCallbackAddResult.SUCCESS, PermissionCallbackAddResult.valueOf("SUCCESS"))
        assertEquals(
            PermissionCallbackAddResult.REQUEST_NOT_FOUND,
            PermissionCallbackAddResult.valueOf("REQUEST_NOT_FOUND"),
        )
        assertEquals(
            PermissionCallbackAddResult.PERMISSION_MISMATCH,
            PermissionCallbackAddResult.valueOf("PERMISSION_MISMATCH"),
        )
    }
}
