package kr.open.library.simple_ui.xml.unit.permissions.flow

import kr.open.library.simple_ui.core.permissions.model.PermissionDecisionType
import kr.open.library.simple_ui.xml.permissions.flow.RuntimePermissionHandler
import kr.open.library.simple_ui.xml.permissions.host.PermissionHostAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class RuntimePermissionHandlerTest {
    private val host: PermissionHostAdapter = mock(PermissionHostAdapter::class.java)
    private val requestedHistory = mutableSetOf<String>()
    private val handler = RuntimePermissionHandler(
        host = host,
        requestedHistory = requestedHistory,
    )

    @Test
    fun `shouldShowRationale delegates to host`() {
        val permission = "android.permission.CAMERA"
        `when`(host.shouldShowRequestPermissionRationale(permission)).thenReturn(true)

        assertTrue(handler.shouldShowRationale(permission))
    }

    @Test
    fun `markRequested stores permissions in history`() {
        handler.markRequested(listOf("android.permission.CAMERA", "android.permission.RECORD_AUDIO"))

        assertTrue(handler.wasRequested("android.permission.CAMERA"))
        assertTrue(handler.wasRequested("android.permission.RECORD_AUDIO"))
        assertFalse(handler.wasRequested("android.permission.POST_NOTIFICATIONS"))
    }

    @Test
    fun `mapResult returns granted when platform grants permission`() {
        val result = handler.mapResult(
            permission = "android.permission.CAMERA",
            granted = true,
            shouldShowRationale = false,
            wasRequestedBefore = true,
        )

        assertEquals(PermissionDecisionType.GRANTED, result)
    }

    @Test
    fun `mapResult returns denied when rationale should be shown`() {
        val result = handler.mapResult(
            permission = "android.permission.CAMERA",
            granted = false,
            shouldShowRationale = true,
            wasRequestedBefore = true,
        )

        assertEquals(PermissionDecisionType.DENIED, result)
    }

    @Test
    fun `mapResult returns permanently denied when permission was requested before without rationale`() {
        val result = handler.mapResult(
            permission = "android.permission.CAMERA",
            granted = false,
            shouldShowRationale = false,
            wasRequestedBefore = true,
        )

        assertEquals(PermissionDecisionType.PERMANENTLY_DENIED, result)
    }
}
