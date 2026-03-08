package kr.open.library.simple_ui.xml.unit.permissions.result

import kr.open.library.simple_ui.core.permissions.classifier.PermissionClassifier
import kr.open.library.simple_ui.core.permissions.model.PermissionDecisionType
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedType
import kr.open.library.simple_ui.core.permissions.queue.PermissionQueue
import kr.open.library.simple_ui.xml.permissions.coordinator.RequestEntry
import kr.open.library.simple_ui.xml.permissions.result.PermissionResultAggregator
import kr.open.library.simple_ui.xml.permissions.state.PermissionStateStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class PermissionResultAggregatorTest {
    @Test
    fun `tryCompleteRequest stores orphaned denied result when callback is missing`() {
        val fixture = createFixture()
        val requestId = "request-denied"
        val permission = "android.permission.CAMERA"
        val entry = createEntry(
            requestId = requestId,
            permissions = listOf(permission),
            results = mutableMapOf(permission to PermissionDecisionType.DENIED),
        )

        fixture.aggregator.registerRequest(entry)
        fixture.queue.enqueue(requestId)

        fixture.aggregator.tryCompleteRequest(requestId)

        val orphanedResults = fixture.stateSnapshot.orphanedResults
        assertEquals(1, orphanedResults.size)
        assertEquals(requestId, orphanedResults.first().requestId)
        assertEquals(permission, orphanedResults
            .first()
            .deniedResults
            .first()
            .permission)
        assertEquals(PermissionDeniedType.DENIED, orphanedResults
            .first()
            .deniedResults
            .first()
            .result)
        assertFalse(fixture.requests.containsKey(requestId))
        assertTrue(fixture.queue.isEmpty())
        assertFalse(fixture.stateSnapshot.requestStates.containsKey(requestId))
    }

    @Test
    fun `tryCompleteRequest does not store orphaned result when all permissions are granted`() {
        val fixture = createFixture()
        val requestId = "request-granted"
        val permission = "android.permission.CAMERA"
        val entry = createEntry(
            requestId = requestId,
            permissions = listOf(permission),
            results = mutableMapOf(permission to PermissionDecisionType.GRANTED),
        )

        fixture.aggregator.registerRequest(entry)
        fixture.queue.enqueue(requestId)

        fixture.aggregator.tryCompleteRequest(requestId)

        assertTrue(fixture.stateSnapshot.orphanedResults.isEmpty())
        assertFalse(fixture.requests.containsKey(requestId))
        assertTrue(fixture.queue.isEmpty())
        assertFalse(fixture.stateSnapshot.requestStates.containsKey(requestId))
    }

    @Test
    fun `tryCompleteRequest invokes callback with denied items only`() {
        val fixture = createFixture()
        val requestId = "request-callback"
        val permissions = listOf("android.permission.CAMERA", "android.permission.RECORD_AUDIO")
        var deniedResultsSize = -1
        var deniedPermission = ""
        var deniedType: PermissionDeniedType? = null
        val entry = createEntry(
            requestId = requestId,
            permissions = permissions,
            results = mutableMapOf(
                permissions[0] to PermissionDecisionType.GRANTED,
                permissions[1] to PermissionDecisionType.PERMANENTLY_DENIED,
            ),
            onDeniedResult = { deniedItems ->
                deniedResultsSize = deniedItems.size
                deniedPermission = deniedItems.first().permission
                deniedType = deniedItems.first().result
            },
        )

        fixture.aggregator.registerRequest(entry)
        fixture.queue.enqueue(requestId)

        fixture.aggregator.tryCompleteRequest(requestId)

        assertEquals(1, deniedResultsSize)
        assertEquals(permissions[1], deniedPermission)
        assertEquals(PermissionDeniedType.PERMANENTLY_DENIED, deniedType)
        assertTrue(fixture.stateSnapshot.orphanedResults.isEmpty())
    }

    private fun createFixture(): AggregatorFixture {
        val stateStore = PermissionStateStore()
        val stateSnapshot = stateStore.getSnapshot()
        val queue = PermissionQueue(stateSnapshot.requestQueue)
        val requests = mutableMapOf<String, RequestEntry>()
        val inFlightWaiters = mutableMapOf<String, MutableSet<String>>()
        val classifier = mock(PermissionClassifier::class.java)
        val aggregator = PermissionResultAggregator(
            stateStore = stateStore,
            stateSnapshot = stateSnapshot,
            queue = queue,
            requests = requests,
            inFlightWaiters = inFlightWaiters,
            classifier = classifier,
        )
        return AggregatorFixture(
            aggregator = aggregator,
            stateSnapshot = stateSnapshot,
            queue = queue,
            requests = requests,
        )
    }

    private fun createEntry(
        requestId: String,
        permissions: List<String>,
        results: MutableMap<String, PermissionDecisionType>,
        onDeniedResult: ((List<kr.open.library.simple_ui.core.permissions.model.PermissionDeniedItem>) -> Unit)? = null,
    ): RequestEntry = RequestEntry(
        requestId = requestId,
        permissions = permissions,
        results = results,
        isRestored = false,
        onDeniedResult = onDeniedResult,
        onRationaleNeeded = null,
        onNavigateToSettings = null,
    )

    private data class AggregatorFixture(
        val aggregator: PermissionResultAggregator,
        val stateSnapshot: kr.open.library.simple_ui.xml.permissions.state.PermissionStateSnapshot,
        val queue: PermissionQueue,
        val requests: MutableMap<String, RequestEntry>,
    )
}
