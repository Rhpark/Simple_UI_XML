package kr.open.library.simple_ui.xml.ui.adapter.queue

/**
 * Shared operation queue engine for sequential processing.<br><br>
 * 연산을 순차 처리하기 위한 공통 큐 엔진입니다.<br>
 *
 * @param schedule Executor hook that runs queue processing on the desired thread.<br><br>
 *                 큐 처리를 원하는 스레드에서 실행하기 위한 스케줄러입니다.<br>
 *
 * @param getName Function to extract an operation name for logging.<br><br>
 *                로깅을 위해 연산 이름을 추출하는 함수입니다.<br>
 *
 * @param execute Function that runs an operation and calls complete with success flag.<br><br>
 *                연산을 실행하고 성공 여부를 complete로 전달하는 함수입니다.<br>
 *
 * @param onComplete Callback invoked after each operation completes.<br><br>
 *                   각 연산 완료 후 호출되는 콜백입니다.<br>
 *
 * @param onError Error handler for unexpected failures during processing.<br><br>
 *                처리 중 예외가 발생했을 때 호출되는 에러 핸들러입니다.<br>
 *
 * @param maxPending Maximum pending queue size; Int.MAX_VALUE means unlimited.<br><br>
 *                  최대 대기 큐 크기이며 Int.MAX_VALUE는 무제한입니다.<br>
 *
 * @param overflowPolicy Overflow handling policy when queue is full.<br><br>
 *                       큐가 가득 찼을 때 적용할 오버플로 정책입니다.<br>
 *
 * @param onDrop Callback invoked when an operation is dropped.<br><br>
 *               연산이 드롭될 때 호출되는 콜백입니다.<br>
 */
internal class OperationQueueProcessor<OPERATION>(
    private val schedule: ((() -> Unit) -> Unit),
    private val getName: (OPERATION) -> String,
    private val execute: (OPERATION, (Boolean) -> Unit) -> Unit,
    private val onComplete: (OPERATION, Boolean) -> Unit,
    private val onError: (message: String, cause: Exception?) -> Unit,
    /**
     * Maximum pending queue size; Int.MAX_VALUE means unlimited.<br><br>
     * 최대 대기 큐 크기이며 Int.MAX_VALUE는 무제한입니다.<br>
     */
    maxPending: Int = Int.MAX_VALUE,
    /**
     * Overflow handling policy when queue is full.<br><br>
     * 큐가 가득 찼을 때 적용할 오버플로 정책입니다.<br>
     */
    overflowPolicy: QueueOverflowPolicy = QueueOverflowPolicy.DROP_NEW,
    /**
     * Callback invoked when an operation is dropped.<br><br>
     * 연산이 드롭될 때 호출되는 콜백입니다.<br>
     */
    private val onDrop: ((OPERATION, QueueDropReason) -> Unit)? = null,
) {
    /**
     * Internal queue storing pending operations.<br><br>
     * 대기 중인 연산을 보관하는 내부 큐입니다.<br>
     */
    private val operationQueue = ArrayDeque<OPERATION>()

    /**
     * Flag indicating whether an operation is currently running.<br><br>
     * 현재 연산 처리 중인지 나타내는 플래그입니다.<br>
     */
    private var isProcessing = false

    /**
     * Lock object for queue synchronization.<br><br>
     * 큐 동기화를 위한 락 객체입니다.<br>
     */
    private val queueLock = Any()

    /**
     * Maximum pending queue size; Int.MAX_VALUE means unlimited.<br><br>
     * 최대 대기 큐 크기이며 Int.MAX_VALUE는 무제한입니다.<br>
     */
    private var maxPending: Int = maxPending

    /**
     * Overflow handling policy when queue is full.<br><br>
     * 큐가 가득 찼을 때 적용할 오버플로 정책입니다.<br>
     */
    private var overflowPolicy: QueueOverflowPolicy = overflowPolicy

    /**
     * Merge keys used to coalesce consecutive operations with the same name.<br><br>
     * 동일 이름의 연속 연산을 병합하기 위한 머지 키 집합입니다.<br>
     */
    private var mergeKeys: Set<String> = emptySet()

    /**
     * Optional debug listener for queue events.<br><br>
     * 큐 이벤트를 수신하는 선택적 디버그 리스너입니다.<br>
     */
    @Volatile
    private var debugListener: ((QueueDebugEvent) -> Unit)? = null

    /**
     * Updates queue policy for overflow handling.<br><br>
     * 큐 오버플로 처리를 위한 정책을 업데이트합니다.<br>
     */
    fun setQueuePolicy(maxPending: Int, overflowPolicy: QueueOverflowPolicy) {
        synchronized(queueLock) {
            this.maxPending = if (maxPending <= 0) Int.MAX_VALUE else maxPending
            this.overflowPolicy = overflowPolicy
        }
    }

    /**
     * Sets the debug listener to receive queue events.<br><br>
     * 큐 이벤트를 수신할 디버그 리스너를 설정합니다.<br>
     */
    fun setDebugListener(listener: ((QueueDebugEvent) -> Unit)?) {
        debugListener = listener
    }

    /**
     * Sets merge keys to coalesce consecutive operations with the same name.<br><br>
     * 동일 이름의 연속 연산을 병합하기 위한 머지 키를 설정합니다.<br>
     */
    fun setQueueMergeKeys(mergeKeys: Set<String>) {
        synchronized(queueLock) {
            this.mergeKeys = mergeKeys.toSet()
        }
    }

    /**
     * Enqueues an operation and starts processing if idle.<br><br>
     * 연산을 큐에 추가하고 유휴 상태면 처리를 시작합니다.<br>
     */
    fun enqueue(operation: OPERATION) {
        val dropped = mutableListOf<Pair<OPERATION, QueueDropReason>>()
        val operationName = getName(operation)
        val shouldStart: Boolean
        var pendingSize = 0
        synchronized(queueLock) {
            val currentMergeKeys = mergeKeys
            if (currentMergeKeys.isNotEmpty() && currentMergeKeys.contains(operationName) && operationQueue.isNotEmpty()) {
                val last = operationQueue.last()
                if (getName(last) == operationName) {
                    operationQueue.removeLast()
                    dropped.add(last to QueueDropReason.MERGED)
                }
            }

            val limit = maxPending
            if (limit != Int.MAX_VALUE && operationQueue.size >= limit) {
                when (overflowPolicy) {
                    QueueOverflowPolicy.DROP_NEW -> {
                        dropped.add(operation to QueueDropReason.QUEUE_FULL_DROP_NEW)
                    }
                    QueueOverflowPolicy.DROP_OLDEST -> {
                        if (operationQueue.isNotEmpty()) {
                            val removed = operationQueue.removeFirst()
                            dropped.add(removed to QueueDropReason.QUEUE_FULL_DROP_OLDEST)
                        }
                        operationQueue.add(operation)
                    }
                    QueueOverflowPolicy.CLEAR_AND_ENQUEUE -> {
                        while (operationQueue.isNotEmpty()) {
                            dropped.add(operationQueue.removeFirst() to QueueDropReason.QUEUE_FULL_CLEAR)
                        }
                        operationQueue.add(operation)
                    }
                }
            } else {
                operationQueue.add(operation)
            }
            pendingSize = operationQueue.size
            shouldStart = operationQueue.isNotEmpty() && !isProcessing
            if (shouldStart) {
                isProcessing = true
            }
        }
        if (dropped.isNotEmpty()) {
            dropped.forEach { (droppedOperation, reason) ->
                onDrop?.invoke(droppedOperation, reason)
                emitDebug(
                    type = QueueEventType.DROPPED,
                    operationName = getName(droppedOperation),
                    pendingSize = pendingSize,
                    dropReason = reason,
                )
            }
        }
        if (dropped.none { it.first == operation }) {
            emitDebug(
                type = QueueEventType.ENQUEUED,
                operationName = operationName,
                pendingSize = pendingSize,
            )
        }
        if (shouldStart) {
            schedule { processNext() }
        }
    }

    /**
     * Clears queued operations and processes the provided one immediately.<br><br>
     * 대기 큐를 비우고 전달된 연산을 즉시 처리합니다.<br>
     */
    fun clearAndEnqueue(operation: OPERATION) {
        val dropped = mutableListOf<OPERATION>()
        val shouldStart: Boolean
        var pendingSize = 0
        synchronized(queueLock) {
            while (operationQueue.isNotEmpty()) {
                dropped.add(operationQueue.removeFirst())
            }
            operationQueue.add(operation)
            pendingSize = operationQueue.size
            shouldStart = !isProcessing && operationQueue.isNotEmpty()
            if (shouldStart) {
                isProcessing = true
            }
        }
        if (dropped.isNotEmpty()) {
            dropped.forEach { droppedOperation ->
                onDrop?.invoke(droppedOperation, QueueDropReason.CLEARED_EXPLICIT)
                emitDebug(
                    type = QueueEventType.DROPPED,
                    operationName = getName(droppedOperation),
                    pendingSize = pendingSize,
                    dropReason = QueueDropReason.CLEARED_EXPLICIT,
                )
            }
            emitDebug(
                type = QueueEventType.CLEARED,
                operationName = getName(operation),
                pendingSize = pendingSize,
                dropReason = QueueDropReason.CLEARED_EXPLICIT,
            )
        }
        emitDebug(
            type = QueueEventType.ENQUEUED,
            operationName = getName(operation),
            pendingSize = pendingSize,
        )
        if (shouldStart) {
            schedule { processNext() }
        }
    }

    /**
     * Clears pending operations without enqueueing a new one.<br><br>
     * 새 연산을 추가하지 않고 대기 큐를 비웁니다.<br>
     */
    fun clearQueue() {
        val dropped = mutableListOf<OPERATION>()
        var pendingSize = 0
        synchronized(queueLock) {
            while (operationQueue.isNotEmpty()) {
                dropped.add(operationQueue.removeFirst())
            }
            pendingSize = operationQueue.size
        }
        if (dropped.isEmpty()) {
            return
        }
        dropped.forEach { droppedOperation ->
            onDrop?.invoke(droppedOperation, QueueDropReason.CLEARED_BY_API)
            emitDebug(
                type = QueueEventType.DROPPED,
                operationName = getName(droppedOperation),
                pendingSize = pendingSize,
                dropReason = QueueDropReason.CLEARED_BY_API,
            )
        }
        emitDebug(
            type = QueueEventType.CLEARED,
            operationName = "clearQueue",
            pendingSize = pendingSize,
            dropReason = QueueDropReason.CLEARED_BY_API,
        )
    }

    /**
     * Processes the next queued operation.<br><br>
     * 큐의 다음 연산을 처리합니다.<br>
     */
    private fun processNext() {
        val operation =
            synchronized(queueLock) {
                if (operationQueue.isEmpty()) {
                    isProcessing = false
                    return
                }
                operationQueue.removeFirst()
            }

        try {
            emitDebug(
                type = QueueEventType.STARTED,
                operationName = getName(operation),
                pendingSize = operationQueue.size,
            )
            execute(operation) { success -> complete(operation, success) }
        } catch (e: Exception) {
            reportError("Error executing operation: ${getName(operation)}", e)
            emitDebug(
                type = QueueEventType.ERROR,
                operationName = getName(operation),
                pendingSize = operationQueue.size,
                message = e.message,
            )
            complete(operation, false)
        }
    }

    /**
     * Completes the operation and schedules the next one if available.<br><br>
     * 연산을 완료 처리하고 다음 연산이 있으면 이어서 처리합니다.<br>
     */
    private fun complete(operation: OPERATION, success: Boolean) {
        try {
            onComplete(operation, success)
        } catch (e: Exception) {
            reportError("Error in operation completion: ${getName(operation)}", e)
        }

        val hasNext: Boolean
        val pendingSize: Int
        synchronized(queueLock) {
            if (operationQueue.isEmpty()) {
                isProcessing = false
                hasNext = false
            } else {
                hasNext = true
            }
            pendingSize = operationQueue.size
        }
        emitDebug(
            type = QueueEventType.COMPLETED,
            operationName = getName(operation),
            pendingSize = pendingSize,
        )
        if (hasNext) {
            schedule { processNext() }
        }
    }

    /**
     * Reports errors through the injected handler safely.<br><br>
     * 주입된 에러 핸들러로 안전하게 에러를 전달합니다.<br>
     */
    private fun reportError(message: String, cause: Exception?) {
        try {
            onError(message, cause)
        } catch (_: Exception) {
            // Swallow errors from error reporting to avoid breaking the queue flow.<br><br>
            // 에러 보고 중 예외는 큐 흐름을 깨지 않도록 무시합니다.<br>
        }
    }

    /**
     * Emits debug events to the listener when configured.<br><br>
     * 설정된 리스너로 디버그 이벤트를 전달합니다.<br>
     */
    private fun emitDebug(
        type: QueueEventType,
        operationName: String?,
        pendingSize: Int,
        dropReason: QueueDropReason? = null,
        message: String? = null,
    ) {
        debugListener?.invoke(
            QueueDebugEvent(
                type = type,
                operationName = operationName,
                pendingSize = pendingSize,
                isProcessing = isProcessing,
                threadName = Thread.currentThread().name,
                dropReason = dropReason,
                message = message,
            ),
        )
    }
}
