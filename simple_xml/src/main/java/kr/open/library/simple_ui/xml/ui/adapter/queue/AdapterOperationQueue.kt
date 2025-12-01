package kr.open.library.simple_ui.xml.ui.adapter.queue

import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.core.logcat.Logx

/**
 * Operation Queue Manager for RecyclerView Adapters
 * RecyclerView Adapter의 작업 큐 관리자
 *
 * Handles sequential execution of list modification operations to prevent race conditions
 * 리스트 수정 작업의 순차 실행을 처리하여 Race Condition 방지
 *
 * @param ITEM Type of items in the adapter
 * @param getCurrentList Function to get current list from adapter
 * @param submitList Function to submit updated list (e.g., ListAdapter.submitList or AsyncListDiffer.submitList)
 */
internal class AdapterOperationQueue<ITEM>(
    private val getCurrentList: () -> List<ITEM>,
    private val submitList: (List<ITEM>, (() -> Unit)?) -> Unit,
) {
    private val operationQueue = ArrayDeque<Operation<ITEM>>()
    private var isProcessingOperation = false
    private val queueLock = Any()

    /**
     * Sealed class representing list operations
     * 리스트 작업을 나타내는 Sealed 클래스
     */
    sealed class Operation<ITEM> {
        abstract val callback: (() -> Unit)?

        abstract fun execute(currentList: List<ITEM>): List<ITEM>
    }

    data class SetItemsOp<ITEM>(
        val items: List<ITEM>,
        override val callback: (() -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> = items
    }

    data class AddItemOp<ITEM>(
        val item: ITEM,
        override val callback: (() -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> = currentList.toMutableList().apply { add(item) }
    }

    data class AddItemAtOp<ITEM>(
        val position: Int,
        val item: ITEM,
        override val callback: (() -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> {
            if (position < 0 || position > currentList.size) {
                throw IndexOutOfBoundsException(
                    "Cannot add item at position $position. Valid range: 0..${currentList.size}",
                )
            }
            return currentList.toMutableList().apply { add(position, item) }
        }
    }

    data class AddItemsOp<ITEM>(
        val items: List<ITEM>,
        override val callback: (() -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> = currentList.toMutableList().apply { addAll(items) }
    }

    data class AddItemsAtOp<ITEM>(
        val position: Int,
        val items: List<ITEM>,
        override val callback: (() -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> {
            if (position < 0 || position > currentList.size) {
                throw IndexOutOfBoundsException(
                    "Cannot add items at position $position. Valid range: 0..${currentList.size}",
                )
            }
            return currentList.toMutableList().apply { addAll(position, items) }
        }
    }

    data class RemoveAtOp<ITEM>(
        val position: Int,
        override val callback: (() -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> {
            if (position < 0 || position >= currentList.size) {
                throw IndexOutOfBoundsException(
                    "Cannot remove item at position $position. Valid range: 0 until ${currentList.size}",
                )
            }
            return currentList.toMutableList().apply { removeAt(position) }
        }
    }

    data class RemoveItemOp<ITEM>(
        val item: ITEM,
        override val callback: (() -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> {
            val position = currentList.indexOf(item)
            if (position == RecyclerView.NO_POSITION) {
                throw IllegalArgumentException("Item not found in the list")
            }
            return currentList.toMutableList().apply { removeAt(position) }
        }
    }

    data class ClearItemsOp<ITEM>(
        override val callback: (() -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> = emptyList()
    }

    data class MoveItemOp<ITEM>(
        val fromPosition: Int,
        val toPosition: Int,
        override val callback: (() -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> {
            if (fromPosition < 0 || fromPosition >= currentList.size) {
                throw IndexOutOfBoundsException(
                    "Invalid fromPosition $fromPosition. Valid range: 0 until ${currentList.size}",
                )
            }
            if (toPosition < 0 || toPosition >= currentList.size) {
                throw IndexOutOfBoundsException(
                    "Invalid toPosition $toPosition. Valid range: 0 until ${currentList.size}",
                )
            }
            return currentList.toMutableList().apply {
                val item = removeAt(fromPosition)
                add(toPosition.coerceAtMost(size), item)
            }
        }
    }

    data class ReplaceItemAtOp<ITEM>(
        val position: Int,
        val item: ITEM,
        override val callback: (() -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> {
            if (position < 0 || position >= currentList.size) {
                throw IndexOutOfBoundsException(
                    "Invalid position $position. Valid range: 0 until ${currentList.size}",
                )
            }
            return currentList.toMutableList().apply { set(position, item) }
        }
    }

    /**
     * Enqueues an operation and processes it
     * 작업을 큐에 추가하고 처리
     */
    fun enqueueOperation(operation: Operation<ITEM>) {
        synchronized(queueLock) {
            operationQueue.add(operation)
            if (!isProcessingOperation) {
                processNextOperation()
            }
        }
    }

    /**
     * Clears the operation queue and processes the given operation immediately
     * 작업 큐를 비우고 주어진 작업을 즉시 처리 (setItems 전용)
     */
    fun clearQueueAndExecute(operation: Operation<ITEM>) {
        synchronized(queueLock) {
            operationQueue.clear()
            operationQueue.add(operation)
            if (!isProcessingOperation) {
                processNextOperation()
            }
        }
    }

    /**
     * Processes the next operation in the queue
     * 큐의 다음 작업 처리
     */
    private fun processNextOperation() {
        synchronized(queueLock) {
            if (operationQueue.isEmpty()) {
                isProcessingOperation = false
                return
            }

            isProcessingOperation = true
            val operation = operationQueue.removeFirst()

            try {
                val currentList = getCurrentList()
                val updatedList = operation.execute(currentList)

                submitList(updatedList) {
                    try {
                        operation.callback?.invoke()
                    } catch (e: Exception) {
                        Logx.e("Error in operation callback", e)
                    }
                    processNextOperation()
                }
            } catch (e: Exception) {
                Logx.e("Error executing operation: ${operation::class.simpleName}", e)
                // Continue processing next operation even if current one fails
                processNextOperation()
            }
        }
    }
}
