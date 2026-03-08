package kr.open.library.simple_ui.xml.unit.permissions.coordinator

import kr.open.library.simple_ui.core.permissions.model.PermissionDecisionType
import kr.open.library.simple_ui.xml.permissions.coordinator.RequestEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RequestEntryTest {
    @Test
    fun `pendingPermissions preserves original permission order`() {
        val entry = createEntry(
            permissions = listOf(
                "android.permission.CAMERA",
                "android.permission.RECORD_AUDIO",
                "android.permission.POST_NOTIFICATIONS",
            ),
            results = mutableMapOf(
                "android.permission.RECORD_AUDIO" to PermissionDecisionType.DENIED,
            ),
        )

        assertEquals(
            listOf("android.permission.CAMERA", "android.permission.POST_NOTIFICATIONS"),
            entry.pendingPermissions(),
        )
    }

    @Test
    fun `isCompleted returns true when all permissions have results`() {
        val permissions = listOf("android.permission.CAMERA", "android.permission.RECORD_AUDIO")
        val entry = createEntry(
            permissions = permissions,
            results = mutableMapOf(
                permissions[0] to PermissionDecisionType.GRANTED,
                permissions[1] to PermissionDecisionType.DENIED,
            ),
        )

        assertTrue(entry.isCompleted())
    }

    @Test
    fun `isCompleted returns false when at least one permission is pending`() {
        val permissions = listOf("android.permission.CAMERA", "android.permission.RECORD_AUDIO")
        val entry = createEntry(
            permissions = permissions,
            results = mutableMapOf(
                permissions[0] to PermissionDecisionType.GRANTED,
            ),
        )

        assertFalse(entry.isCompleted())
    }

    @Test
    fun `toState copies current request values`() {
        val permissions = listOf("android.permission.CAMERA", "android.permission.RECORD_AUDIO")
        val results = mutableMapOf(
            permissions[0] to PermissionDecisionType.GRANTED,
            permissions[1] to PermissionDecisionType.PERMANENTLY_DENIED,
        )
        val entry = createEntry(
            requestId = "state-request",
            permissions = permissions,
            results = results,
        )

        val state = entry.toState()

        assertEquals("state-request", state.requestId)
        assertEquals(permissions, state.permissions)
        assertEquals(results, state.results)
    }

    private fun createEntry(
        requestId: String = "request-id",
        permissions: List<String>,
        results: MutableMap<String, PermissionDecisionType>,
    ): RequestEntry = RequestEntry(
        requestId = requestId,
        permissions = permissions,
        results = results,
        isRestored = false,
        onDeniedResult = null,
        onRationaleNeeded = null,
        onNavigateToSettings = null,
    )
}
