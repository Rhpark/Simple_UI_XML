package kr.open.library.simple_ui.xml.ui.adapter.list.base.queue

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.core.extensions.trycatch.requireInBounds
import kr.open.library.simple_ui.core.logcat.Logx

/**
 * Operation queue manager for RecyclerView adapters.<br><br>
 * RecyclerView 어댑터 연산 큐 관리 클래스입니다.<br>
 *
 * Handles sequential execution of list modification operations to prevent race conditions.<br><br>
 * 리스트 변경 연산을 순차 실행하여 레이스 컨디션을 방지합니다.<br>
 *
 * @param ITEM Type of items in the adapter.<br><br>
 *             어댑터 아이템 타입입니다.<br>
 * @param getCurrentList Function to get current list from adapter.<br><br>
 *                       어댑터의 현재 리스트를 가져오는 함수입니다.<br>
 * @param applyList Function that applies operation result to adapter state/UI.<br><br>
 *                  연산 결과를 어댑터 상태/UI에 반영하는 함수입니다.<br>
 */
internal class AdapterOperationQueue<ITEM>(
    private val getCurrentList: () -> List<ITEM>,
    private val applyList: (Operation<ITEM>, List<ITEM>, List<ITEM>, (() -> Unit)?) -> Unit,
) {
    /**
     * Terminal state delivered to operation callbacks.<br><br>
     * 연산 콜백에 전달되는 종료 상태입니다.<br>
     */
    internal sealed interface OperationTerminalState {
        /**
         * Indicates successful application of the operation.<br><br>
         * 연산이 성공적으로 반영되었음을 나타냅니다.<br>
         */
        data object Applied : OperationTerminalState

        /**
         * Indicates that the operation was dropped before execution.<br><br>
         * 연산이 실행 전에 드롭되었음을 나타냅니다.<br>
         */
        data class Dropped(
            val reason: QueueDropReason,
        ) : OperationTerminalState

        /**
         * Indicates that the operation failed during execution.<br><br>
         * 연산이 실행 중 오류로 실패했음을 나타냅니다.<br>
         */
        data class ExecutionError(
            val cause: Throwable?,
        ) : OperationTerminalState
    }

    /**
     * Main thread handler for queued operations.<br><br>
     * 큐 연산 실행을 위한 메인 스레드 핸들러입니다.<br>
     */
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Shared processor for queued adapter operations.<br><br>
     * 어댑터 연산을 공통 엔진으로 처리하는 프로세서입니다.<br>
     */
    private val operationProcessor =
        OperationQueueProcessor<Operation<ITEM>>(
            schedule = { action -> runOnMainThread(action) },
            getName = { operation -> operation::class.simpleName ?: "Operation" },
            execute = { operation, complete ->
                val currentList = getCurrentList()
                val updatedList = operation.execute(currentList)
                applyList(operation, currentList, updatedList) { complete(QueueExecutionResult.Success) }
            },
            onComplete = { operation, result ->
                when (result) {
                    is QueueExecutionResult.Success -> {
                        invokeOperationCallbackSafely(
                            operation = operation,
                            state = OperationTerminalState.Applied,
                            phase = "operation-complete",
                        )
                    }
                    is QueueExecutionResult.Failure -> {
                        Logx.w("Operation failed: ${operation::class.simpleName}. Callback is invoked as terminal signal.")
                        invokeOperationCallbackSafely(
                            operation = operation,
                            state = OperationTerminalState.ExecutionError(result.cause),
                            phase = "operation-failed",
                        )
                    }
                }
            },
            onDrop = { operation, reason ->
                Logx.w(
                    "Operation dropped: ${operation::class.simpleName}, reason: $reason. Callback is invoked as terminal signal."
                )
                invokeOperationCallbackSafely(
                    operation = operation,
                    state = OperationTerminalState.Dropped(reason),
                    phase = "operation-dropped",
                )
            },
            onError = { message, error ->
                if (error != null) {
                    Logx.e(message, error)
                } else {
                    Logx.e(message)
                }
            },
        )

    /**
     * Sealed class representing list operations.<br><br>
     * 리스트 연산을 표현하는 Sealed 클래스입니다.<br>
     */
    internal sealed class Operation<ITEM> {
        abstract val callback: ((OperationTerminalState) -> Unit)?

        /**
         * Executes operation against current snapshot and returns new snapshot.<br>
         * 현재 스냅샷에 연산을 적용해 새 스냅샷을 반환합니다.<br>
         *
         * Some subclasses re-validate bounds inside [execute] even though the caller already validated
         * before enqueuing. This is intentional: between enqueue and execution, other operations may
         * have mutated the list, making a previously valid position invalid at execution time.<br>
         * If [execute] throws, the queue catches it and delivers [OperationTerminalState.ExecutionError]
         * to the callback — not [OperationTerminalState.Dropped] — because the operation had already
         * been accepted into the queue and the failure occurred during execution, not at acceptance.<br><br>
         * 일부 서브클래스는 호출자가 이미 검증한 후에도 [execute] 내부에서 bounds를 재검증합니다.
         * 이는 의도적 설계입니다: 큐 등록 시점과 실행 시점 사이에 다른 연산이 리스트를 변경해
         * 이전에는 유효했던 위치가 실행 시점에 무효가 될 수 있기 때문입니다.<br>
         * [execute]에서 예외가 발생하면 큐는 이를 잡아 [OperationTerminalState.ExecutionError]로
         * 콜백에 전달합니다 — [OperationTerminalState.Dropped]가 아닙니다. 연산이 이미 큐에
         * 수락된 후 실행 중에 실패했기 때문입니다.<br>
         */
        abstract fun execute(currentList: List<ITEM>): List<ITEM>
    }

    /**
     * Operation that replaces entire list.<br><br>
     * 전체 리스트를 교체하는 연산입니다.<br>
     */
    internal data class SetItemsOp<ITEM>(
        val items: List<ITEM>,
        override val callback: ((OperationTerminalState) -> Unit)?,
    ) : Operation<ITEM>() {
        private val snapshot: List<ITEM> = items.toList()

        override fun execute(currentList: List<ITEM>): List<ITEM> = snapshot
    }

    /**
     * Operation that appends one item.<br><br>
     * 아이템 1개를 끝에 추가하는 연산입니다.<br>
     */
    internal data class AddItemOp<ITEM>(
        val item: ITEM,
        override val callback: ((OperationTerminalState) -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> = currentList.toMutableList().apply { add(item) }
    }

    /**
     * Operation that inserts one item at position.<br><br>
     * 지정 위치에 아이템 1개를 삽입하는 연산입니다.<br>
     */
    internal data class AddItemAtOp<ITEM>(
        val position: Int,
        val item: ITEM,
        override val callback: ((OperationTerminalState) -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> {
            requireInBounds(position >= 0 && position <= currentList.size) {
                "Cannot add item at position $position. Valid range: 0..${currentList.size}"
            }
            return currentList.toMutableList().apply { add(position, item) }
        }
    }

    /**
     * Operation that appends multiple items.<br><br>
     * 아이템 여러 개를 끝에 추가하는 연산입니다.<br>
     */
    internal data class AddItemsOp<ITEM>(
        val items: List<ITEM>,
        override val callback: ((OperationTerminalState) -> Unit)?,
    ) : Operation<ITEM>() {
        private val snapshot: List<ITEM> = items.toList()

        override fun execute(currentList: List<ITEM>): List<ITEM> = currentList.toMutableList().apply { addAll(snapshot) }
    }

    /**
     * Operation that inserts multiple items at position.<br><br>
     * 지정 위치에 아이템 여러 개를 삽입하는 연산입니다.<br>
     */
    internal data class AddItemsAtOp<ITEM>(
        val position: Int,
        val items: List<ITEM>,
        override val callback: ((OperationTerminalState) -> Unit)?,
    ) : Operation<ITEM>() {
        private val snapshot: List<ITEM> = items.toList()

        override fun execute(currentList: List<ITEM>): List<ITEM> {
            requireInBounds(position >= 0 && position <= currentList.size) {
                "Cannot add items at position $position. Valid range: 0..${currentList.size}"
            }
            return currentList.toMutableList().apply { addAll(position, snapshot) }
        }
    }

    /**
     * Operation that removes one item at position.<br><br>
     * 지정 위치의 아이템 1개를 제거하는 연산입니다.<br>
     */
    internal data class RemoveAtOp<ITEM>(
        val position: Int,
        override val callback: ((OperationTerminalState) -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> {
            requireInBounds(position >= 0 && position < currentList.size) {
                "Cannot remove item at position $position. Valid range: 0 until ${currentList.size}"
            }
            return currentList.toMutableList().apply { removeAt(position) }
        }
    }

    /**
     * Operation that removes first matching item.<br><br>
     * 일치하는 첫 번째 아이템을 제거하는 연산입니다.<br>
     */
    internal data class RemoveItemOp<ITEM>(
        val item: ITEM,
        override val callback: ((OperationTerminalState) -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> {
            val position = currentList.indexOf(item)
            requireInBounds(position != RecyclerView.NO_POSITION) { "Item not found in the list" }
            return currentList.toMutableList().apply { removeAt(position) }
        }
    }

    /**
     * Operation that removes all matching items (best-effort).<br><br>
     * 일치하는 아이템들을 best-effort로 제거하는 연산입니다.<br>
     */
    internal data class RemoveItemsOp<ITEM>(
        val items: List<ITEM>,
        override val callback: ((OperationTerminalState) -> Unit)?,
    ) : Operation<ITEM>() {
        private val snapshot: List<ITEM> = items.toList()

        override fun execute(currentList: List<ITEM>): List<ITEM> {
            if (snapshot.isEmpty()) return currentList
            val removeSet = snapshot.toHashSet()
            return currentList.filter { it !in removeSet }
        }
    }

    /**
     * Operation that removes a contiguous range.<br><br>
     * 연속된 구간을 제거하는 연산입니다.<br>
     */
    internal data class RemoveRangeOp<ITEM>(
        val start: Int,
        val count: Int,
        override val callback: ((OperationTerminalState) -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> {
            requireInBounds(count > 0) {
                "Cannot remove range, count must be positive. count: $count"
            }
            requireInBounds(start >= 0 && start < currentList.size) {
                "Cannot remove range from start $start. Valid range: 0..${currentList.lastIndex}"
            }
            requireInBounds(count <= currentList.size - start) {
                "Cannot remove range start=$start, count=$count. Valid max count: ${currentList.size - start}"
            }
            return currentList.toMutableList().apply {
                subList(start, start + count).clear()
            }
        }
    }

    /**
     * Operation that clears all items.<br><br>
     * 모든 아이템을 비우는 연산입니다.<br>
     */
    internal data class ClearItemsOp<ITEM>(
        override val callback: ((OperationTerminalState) -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> = emptyList()
    }

    /**
     * Operation that moves one item between positions.<br><br>
     * 아이템 1개를 위치 간 이동하는 연산입니다.<br>
     */
    internal data class MoveItemOp<ITEM>(
        val fromPosition: Int,
        val toPosition: Int,
        override val callback: ((OperationTerminalState) -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> {
            requireInBounds(fromPosition >= 0 && fromPosition < currentList.size) {
                "Invalid fromPosition $fromPosition. Valid range: 0 until ${currentList.size}"
            }
            requireInBounds(toPosition >= 0 && toPosition < currentList.size) {
                "Invalid toPosition $toPosition. Valid range: 0 until ${currentList.size}"
            }

            return currentList.toMutableList().apply {
                val item = removeAt(fromPosition)
                add(toPosition.coerceAtMost(size), item)
            }
        }
    }

    /**
     * Operation that replaces one item at position.<br><br>
     * 지정 위치의 아이템 1개를 교체하는 연산입니다.<br>
     */
    internal data class ReplaceItemAtOp<ITEM>(
        val position: Int,
        val item: ITEM,
        override val callback: ((OperationTerminalState) -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> {
            requireInBounds(position >= 0 && position < currentList.size) {
                "Invalid position $position. Valid range: 0 until ${currentList.size}"
            }

            return currentList.toMutableList().apply { set(position, item) }
        }
    }

    /**
     * Enqueues an operation and processes it.<br><br>
     * 연산을 큐에 추가하고 처리합니다.<br>
     */
    fun enqueueOperation(operation: Operation<ITEM>) {
        operationProcessor.enqueue(operation)
    }

    /**
     * Clears the operation queue and processes the given operation immediately.<br><br>
     * 큐를 비우고 전달된 연산을 즉시 처리합니다.<br>
     */
    fun clearQueueAndExecute(operation: Operation<ITEM>) {
        operationProcessor.clearAndEnqueue(operation)
    }

    /**
     * Updates queue overflow policy and max pending size.<br><br>
     * 큐 오버플로 정책과 최대 대기 크기를 설정합니다.<br>
     */
    fun setQueuePolicy(maxPending: Int, overflowPolicy: QueueOverflowPolicy) {
        operationProcessor.setQueuePolicy(maxPending, overflowPolicy)
    }

    /**
     * Executes the action on the main thread, posting if necessary.<br><br>
     * 메인 스레드에서 실행하며 필요 시 메인으로 포스트합니다.<br>
     */
    private fun runOnMainThread(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }

    /**
     * Invokes operation callback safely.<br><br>
     * 연산 콜백을 안전하게 호출합니다.<br>
     */
    private fun invokeOperationCallbackSafely(
        operation: Operation<ITEM>,
        state: OperationTerminalState,
        phase: String,
    ) {
        try {
            operation.callback?.invoke(state)
        } catch (e: RuntimeException) {
            Logx.e("Error in operation callback ($phase)", e)
        }
    }
}
