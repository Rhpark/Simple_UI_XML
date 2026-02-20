package kr.open.library.simple_ui.xml.ui.adapter.queue

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
 * @param submitList Function to submit updated list (e.g., ListAdapter.submitList or AsyncListDiffer.submitList).<br><br>
 *                   업데이트된 리스트를 제출하는 함수입니다.<br>
 */
internal class AdapterOperationQueue<ITEM>(
    private val getCurrentList: () -> List<ITEM>,
    private val submitList: (List<ITEM>, (() -> Unit)?) -> Unit,
) {
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
                submitList(updatedList) { complete(true) }
            },
            onComplete = { operation, success ->
                if (!success) return@OperationQueueProcessor
                try {
                    operation.callback?.invoke()
                } catch (e: RuntimeException) {
                    Logx.e("Error in operation callback", e)
                }
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
        abstract val callback: (() -> Unit)?

        abstract fun execute(currentList: List<ITEM>): List<ITEM>
    }

    internal data class SetItemsOp<ITEM>(
        val items: List<ITEM>,
        override val callback: (() -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> = items
    }

    internal data class AddItemOp<ITEM>(
        val item: ITEM,
        override val callback: (() -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> = currentList.toMutableList().apply { add(item) }
    }

    internal data class AddItemAtOp<ITEM>(
        val position: Int,
        val item: ITEM,
        override val callback: (() -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> {
            requireInBounds(position >= 0 && position <= currentList.size) {
                "Cannot add item at position $position. Valid range: 0..${currentList.size}"
            }
            return currentList.toMutableList().apply { add(position, item) }
        }
    }

    internal data class AddItemsOp<ITEM>(
        val items: List<ITEM>,
        override val callback: (() -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> = currentList.toMutableList().apply { addAll(items) }
    }

    internal data class AddItemsAtOp<ITEM>(
        val position: Int,
        val items: List<ITEM>,
        override val callback: (() -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> {
            requireInBounds(position >= 0 && position <= currentList.size) {
                "Cannot add items at position $position. Valid range: 0..${currentList.size}"
            }
            return currentList.toMutableList().apply { addAll(position, items) }
        }
    }

    internal data class RemoveAtOp<ITEM>(
        val position: Int,
        override val callback: (() -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> {
            requireInBounds(position >= 0 && position < currentList.size) {
                "Cannot remove item at position $position. Valid range: 0 until ${currentList.size}"
            }
            return currentList.toMutableList().apply { removeAt(position) }
        }
    }

    internal data class RemoveItemOp<ITEM>(
        val item: ITEM,
        override val callback: (() -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> {
            val position = currentList.indexOf(item)
            require(position != RecyclerView.NO_POSITION) { "Item not found in the list" }
            return currentList.toMutableList().apply { removeAt(position) }
        }
    }

    internal data class ClearItemsOp<ITEM>(
        override val callback: (() -> Unit)?,
    ) : Operation<ITEM>() {
        override fun execute(currentList: List<ITEM>): List<ITEM> = emptyList()
    }

    internal data class MoveItemOp<ITEM>(
        val fromPosition: Int,
        val toPosition: Int,
        override val callback: (() -> Unit)?,
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

    internal data class ReplaceItemAtOp<ITEM>(
        val position: Int,
        val item: ITEM,
        override val callback: (() -> Unit)?,
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
     * Sets debug listener for queue events.<br><br>
     * 큐 이벤트 디버그 리스너를 설정합니다.<br>
     */
    fun setQueueDebugListener(listener: ((QueueDebugEvent) -> Unit)?) {
        operationProcessor.setDebugListener(listener)
    }

    /**
     * Sets merge keys to coalesce consecutive operations with the same name.<br><br>
     * 동일 이름의 연속 연산을 병합하기 위한 머지 키를 설정합니다.<br>
     */
    fun setQueueMergeKeys(mergeKeys: Set<String>) {
        operationProcessor.setQueueMergeKeys(mergeKeys)
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
}
