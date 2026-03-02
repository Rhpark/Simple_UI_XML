package kr.open.library.simple_ui.xml.robolectric.ui.adapter.queue

import android.os.Build
import android.os.Looper
import kr.open.library.simple_ui.xml.ui.adapter.list.base.queue.AdapterOperationQueue
import kr.open.library.simple_ui.xml.ui.adapter.list.base.queue.QueueDropReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Robolectric tests for AdapterOperationQueue terminal callback behavior.<br><br>
 * AdapterOperationQueue의 터미널 콜백 동작을 검증하는 Robolectric 테스트입니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class AdapterOperationQueueRobolectricTest {
    @Test
    fun droppedOperation_invokesCallbackAsTerminalSignal() {
        // Given
        var state: List<String> = listOf("A", "B")
        var holdCurrentOperationCompletion: (() -> Unit)? = null
        var holdFirstApplyCompletion = true
        var droppedState: AdapterOperationQueue.OperationTerminalState? = null

        val queue =
            AdapterOperationQueue(
                getCurrentList = { state },
                applyList = { _, _, updatedList, callback ->
                    state = updatedList
                    if (holdFirstApplyCompletion) {
                        holdFirstApplyCompletion = false
                        holdCurrentOperationCompletion = callback
                    } else {
                        callback?.invoke()
                    }
                },
            )

        // first operation keeps queue in processing state
        queue.clearQueueAndExecute(AdapterOperationQueue.SetItemsOp(listOf("A", "B"), null))

        // queued operation that will be dropped by clear-and-enqueue
        queue.enqueueOperation(
            AdapterOperationQueue.RemoveItemOp(
                item = "B",
                callback = { terminalState -> droppedState = terminalState },
            ),
        )

        // clear pending queue and enqueue latest operation
        queue.clearQueueAndExecute(AdapterOperationQueue.SetItemsOp(listOf("X", "Y"), null))
        holdCurrentOperationCompletion?.invoke()

        // When
        drainMainLooper()

        // Then
        val dropped = droppedState as? AdapterOperationQueue.OperationTerminalState.Dropped
        assertTrue(droppedState is AdapterOperationQueue.OperationTerminalState.Dropped)
        assertEquals(QueueDropReason.CLEARED_EXPLICIT, dropped?.reason)
        assertEquals(listOf("X", "Y"), state)
    }

    @Test
    fun failedOperation_invokesCallbackAsTerminalSignal() {
        // Given
        var state: List<String> = emptyList()
        var failedState: AdapterOperationQueue.OperationTerminalState? = null
        var nextState: AdapterOperationQueue.OperationTerminalState? = null

        val queue =
            AdapterOperationQueue(
                getCurrentList = { state },
                applyList = { _, _, updatedList, callback ->
                    state = updatedList
                    callback?.invoke()
                },
            )

        // When
        queue.enqueueOperation(
            AdapterOperationQueue.RemoveAtOp(
                position = 0,
                callback = { terminalState -> failedState = terminalState },
            ),
        )
        queue.enqueueOperation(
            AdapterOperationQueue.AddItemOp(
                item = "NEXT",
                callback = { terminalState -> nextState = terminalState },
            ),
        )
        drainMainLooper()

        // Then
        assertTrue(failedState is AdapterOperationQueue.OperationTerminalState.ExecutionError)
        assertEquals(AdapterOperationQueue.OperationTerminalState.Applied, nextState)
        assertEquals(listOf("NEXT"), state)
    }

    private fun drainMainLooper() {
        val mainLooper = shadowOf(Looper.getMainLooper())
        mainLooper.idle()
        mainLooper.runToEndOfTasks()
        mainLooper.idle()
    }
}
