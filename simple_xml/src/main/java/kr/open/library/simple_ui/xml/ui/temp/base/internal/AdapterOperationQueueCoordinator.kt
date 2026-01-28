package kr.open.library.simple_ui.xml.ui.temp.base.internal

import kr.open.library.simple_ui.xml.ui.adapter.queue.OperationQueueProcessor
import kr.open.library.simple_ui.xml.ui.adapter.queue.QueueDebugEvent
import kr.open.library.simple_ui.xml.ui.adapter.queue.QueueDropReason
import kr.open.library.simple_ui.xml.ui.adapter.queue.QueueOverflowPolicy
import kr.open.library.simple_ui.xml.ui.temp.base.AdapterOperationFailure
import java.util.concurrent.Executor

/**
 * Shared coordinator for queued operations with background execution support.<br><br>
 * 백그라운드 실행을 지원하는 공통 큐 연산 코디네이터입니다.<br>
 */
internal class AdapterOperationQueueCoordinator<ITEM : Any, META>(
    /**
     * Executes actions on the main thread (or posts if needed).<br><br>
     * 메인 스레드에서 실행하며 필요 시 메인으로 포스트합니다.<br>
     */
    private val runOnMainThread: ((() -> Unit) -> Unit),
    /**
     * Provides a snapshot of current items for operation transforms.<br><br>
     * 연산 변환을 위한 현재 아이템 스냅샷을 제공합니다.<br>
     */
    private val getCurrentItems: () -> List<ITEM>,
    /**
     * Provides an optional executor for background transformation.<br><br>
     * 백그라운드 변환용 Executor를 제공합니다.<br>
     */
    private val operationExecutorProvider: () -> Executor?,
    /**
     * Applies the operation result to the adapter and calls complete when done.<br><br>
     * 연산 결과를 어댑터에 적용하고 완료 시 complete를 호출합니다.<br>
     */
    private val applyResult: (oldItems: List<ITEM>, result: OperationResult<ITEM, META>, complete: (Boolean) -> Unit) -> Unit,
    /**
     * Receives failure details for operations.<br><br>
     * 연산 실패 상세를 수신합니다.<br>
     */
    private val onFailure: (operationName: String, failure: AdapterOperationFailure) -> Unit,
    /**
     * Error handler for unexpected failures during processing.<br><br>
     * 처리 중 예외 발생 시 호출되는 에러 핸들러입니다.<br>
     */
    private val onError: (message: String, cause: Exception?) -> Unit,
) {
    /**
     * Result of a queued operation including metadata.<br><br>
     * 메타데이터를 포함한 큐 연산 결과입니다.<br>
     */
    data class OperationResult<ITEM : Any, META>(
        /**
         * Updated items for the adapter.<br><br>
         * 어댑터에 적용될 아이템 목록입니다.<br>
         */
        val items: List<ITEM>,
        /**
         * Validation success flag for the operation.<br><br>
         * 연산 검증 성공 여부입니다.<br>
         */
        val success: Boolean,
        /**
         * Optional metadata for adapter-specific updates.<br><br>
         * 어댑터별 업데이트를 위한 선택적 메타데이터입니다.<br>
         */
        val meta: META? = null,
        /**
         * Failure details when success is false.<br><br>
         * 성공이 false일 때의 실패 상세 정보입니다.<br>
         */
        val failure: AdapterOperationFailure? = null,
    )

    /**
     * Pending operation descriptor for serialized processing.<br><br>
     * 직렬 처리할 대기 연산 정보입니다.<br>
     */
    private data class PendingOperation<ITEM : Any, META>(
        /**
         * Operation name for logging and debugging.<br><br>
         * 로깅과 디버깅을 위한 연산 이름입니다.<br>
         */
        val name: String,
        /**
         * Operation transform from current list to new list with validation result.<br><br>
         * 현재 리스트를 새 리스트로 변환하고 검증 결과를 포함하는 함수입니다.<br>
         */
        val apply: (List<ITEM>) -> OperationResult<ITEM, META>,
        /**
         * Completion callback invoked on the main thread with success flag.<br><br>
         * 메인 스레드에서 성공 여부를 전달하는 완료 콜백입니다.<br>
         */
        val callback: ((Boolean) -> Unit)?,
    )

    /**
     * Shared processor for queued operations.<br><br>
     * 큐 연산을 직렬 처리하는 공통 프로세서입니다.<br>
     */
    private val operationProcessor =
        OperationQueueProcessor<PendingOperation<ITEM, META>>(
            schedule = { action -> runOnMainThread(action) },
            getName = { it.name },
            execute = { operation, complete -> processOperation(operation, complete) },
            onComplete = { operation, success -> dispatchCommitCallback(operation.callback, success) },
            onDrop = { operation, reason -> dispatchDrop(operation, reason) },
            onError = onError,
        )

    /**
     * Enqueues an operation and starts processing when idle.<br><br>
     * 연산을 큐에 추가하고 유휴 상태면 처리를 시작합니다.<br>
     */
    fun enqueue(
        name: String,
        commitCallback: ((Boolean) -> Unit)?,
        apply: (List<ITEM>) -> OperationResult<ITEM, META>,
    ) {
        operationProcessor.enqueue(PendingOperation(name, apply, commitCallback))
    }

    /**
     * Clears queued operations and enqueues the provided one immediately.<br><br>
     * 큐를 비우고 전달된 연산을 즉시 추가합니다.<br>
     */
    fun clearAndEnqueue(
        name: String,
        commitCallback: ((Boolean) -> Unit)?,
        apply: (List<ITEM>) -> OperationResult<ITEM, META>,
    ) {
        operationProcessor.clearAndEnqueue(PendingOperation(name, apply, commitCallback))
    }

    /**
     * Clears pending operations without enqueuing a new one.<br><br>
     * 새 연산을 추가하지 않고 대기 큐를 비웁니다.<br>
     */
    fun clearQueue() {
        operationProcessor.clearQueue()
    }

    /**
     * Sets queue overflow policy and max pending size.<br><br>
     * 큐 오버플로 정책과 최대 대기 크기를 설정합니다.<br>
     */
    fun setQueuePolicy(maxPending: Int, overflowPolicy: QueueOverflowPolicy) {
        operationProcessor.setQueuePolicy(maxPending, overflowPolicy)
    }

    /**
     * Sets the queue debug listener for operational tracing.<br><br>
     * 운영 추적을 위한 큐 디버그 리스너를 설정합니다.<br>
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
     * Executes a queued operation and applies the result.<br><br>
     * 큐에 등록된 연산을 실행하고 결과를 적용합니다.<br>
     */
    private fun processOperation(
        operation: PendingOperation<ITEM, META>,
        complete: (Boolean) -> Unit,
    ) {
        // Snapshot of old items for diff calculation.<br><br>Diff 계산을 위한 기존 아이템 스냅샷입니다.<br>
        val oldItems = getCurrentItems()
        val executor = operationExecutorProvider()
        if (executor == null) {
            processOperationOnMain(oldItems, operation, complete)
        } else {
            executor.execute { processOperationOnWorker(oldItems, operation, complete) }
        }
    }

    /**
     * Processes an operation on the main thread.<br><br>
     * 메인 스레드에서 연산을 처리합니다.<br>
     */
    private fun processOperationOnMain(
        oldItems: List<ITEM>,
        operation: PendingOperation<ITEM, META>,
        complete: (Boolean) -> Unit,
    ) {
        // Operation result containing new items and validation status.<br><br>연산 결과로 새 아이템과 검증 결과를 포함합니다.<br>
        val result =
            try {
                operation.apply(oldItems)
            } catch (e: Exception) {
                onError("Error executing operation: ${operation.name}", e)
                onFailure(operation.name, AdapterOperationFailure.Exception(e))
                complete(false)
                return
            }

        if (!result.success) {
            val failure = result.failure ?: AdapterOperationFailure.Validation("Operation failed")
            onFailure(operation.name, failure)
            complete(false)
            return
        }

        // New items produced by the operation.<br><br>연산 결과로 생성된 새 아이템 목록입니다.<br>
        val newItems = result.items
        if (oldItems == newItems) {
            complete(true)
            return
        }

        applyResult(oldItems, result, complete)
    }

    /**
     * Processes an operation on a worker thread and posts UI updates to main.<br><br>
     * 워커 스레드에서 연산을 처리하고 UI 업데이트를 메인으로 포스트합니다.<br>
     */
    private fun processOperationOnWorker(
        oldItems: List<ITEM>,
        operation: PendingOperation<ITEM, META>,
        complete: (Boolean) -> Unit,
    ) {
        // Operation result containing new items and validation status.<br><br>연산 결과로 새 아이템과 검증 결과를 포함합니다.<br>
        val result =
            try {
                operation.apply(oldItems)
            } catch (e: Exception) {
                onError("Error executing operation: ${operation.name}", e)
                onFailure(operation.name, AdapterOperationFailure.Exception(e))
                runOnMainThread { complete(false) }
                return
            }

        if (!result.success) {
            val failure = result.failure ?: AdapterOperationFailure.Validation("Operation failed")
            onFailure(operation.name, failure)
            runOnMainThread { complete(false) }
            return
        }

        // New items produced by the operation.<br><br>연산 결과로 생성된 새 아이템 목록입니다.<br>
        val newItems = result.items
        if (oldItems == newItems) {
            runOnMainThread { complete(true) }
            return
        }

        runOnMainThread { applyResult(oldItems, result, complete) }
    }

    /**
     * Dispatches the commit callback on the main thread.<br><br>
     * 완료 콜백을 메인 스레드에서 호출합니다.<br>
     */
    private fun dispatchCommitCallback(callback: ((Boolean) -> Unit)?, success: Boolean) {
        if (callback != null) {
            runOnMainThread { callback.invoke(success) }
        }
    }

    /**
     * Handles dropped operations by notifying failure and commit callbacks.<br><br>
     * 드롭된 연산을 실패/커밋 콜백으로 전달합니다.<br>
     */
    private fun dispatchDrop(operation: PendingOperation<ITEM, META>, reason: QueueDropReason) {
        onFailure(operation.name, AdapterOperationFailure.Dropped(reason))
        dispatchCommitCallback(operation.callback, false)
    }
}

