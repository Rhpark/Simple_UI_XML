package kr.open.library.simple_ui.xml.unit.ui.adapter.list.base.result

import kr.open.library.simple_ui.xml.ui.adapter.list.base.queue.QueueDropReason
import kr.open.library.simple_ui.xml.ui.adapter.list.base.result.AdapterDropReason
import kr.open.library.simple_ui.xml.ui.adapter.list.base.result.ListAdapterResult
import kr.open.library.simple_ui.xml.ui.adapter.list.base.result.toAdapterDropReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class ListAdapterResultTest {
    @Test
    fun `fold invokes applied branch only`() {
        var invokedBranch = ""

        ListAdapterResult.Applied.fold(
            onApplied = { invokedBranch = "applied" },
            onRejected = { invokedBranch = "rejected" },
            onFailed = { invokedBranch = "failed" },
        )

        assertEquals("applied", invokedBranch)
    }

    @Test
    fun `fold passes rejected result to rejected branch`() {
        val expected = ListAdapterResult.Rejected.NoMatchingItems
        var actual: ListAdapterResult.Rejected? = null

        expected.fold(
            onApplied = {},
            onRejected = { actual = it },
            onFailed = {},
        )

        assertSame(expected, actual)
    }

    @Test
    fun `fold passes dropped failure and reason to failed branch`() {
        val expected = ListAdapterResult.Failed.Dropped(AdapterDropReason.DROP_OLDEST)
        var actual: ListAdapterResult.Failed? = null

        expected.fold(
            onApplied = {},
            onRejected = {},
            onFailed = { actual = it },
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `fold passes execution error and cause to failed branch`() {
        val cause = IllegalStateException("operation failed")
        val expected = ListAdapterResult.Failed.ExecutionError(cause)
        var actual: ListAdapterResult.Failed? = null

        expected.fold(
            onApplied = {},
            onRejected = {},
            onFailed = { actual = it },
        )

        assertSame(expected, actual)
        assertSame(cause, (actual as ListAdapterResult.Failed.ExecutionError).cause)
    }

    @Test
    fun `queue drop reasons map to public adapter drop reasons`() {
        val expectedMappings = mapOf(
            QueueDropReason.QUEUE_FULL_DROP_NEW to AdapterDropReason.DROP_NEW,
            QueueDropReason.QUEUE_FULL_DROP_OLDEST to AdapterDropReason.DROP_OLDEST,
            QueueDropReason.QUEUE_FULL_CLEAR to AdapterDropReason.CLEAR_AND_ENQUEUE,
            QueueDropReason.CLEARED_EXPLICIT to AdapterDropReason.CLEARED_EXPLICIT,
            QueueDropReason.CLEARED_BY_API to AdapterDropReason.CLEARED_BY_API,
        )

        expectedMappings.forEach { (queueReason, expected) ->
            assertEquals(expected, queueReason.toAdapterDropReason())
        }
    }
}
