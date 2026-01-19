package kr.open.library.simple_ui.xml.ui.adapter.queue

/**
 * Queue overflow handling policy when pending operations reach the limit.<br><br>
 * 대기 연산이 한도에 도달했을 때 적용하는 오버플로 정책입니다.<br>
 */
enum class QueueOverflowPolicy {
    /**
     * Drop the new incoming operation and keep existing ones.<br><br>
     * 새로 들어온 연산을 버리고 기존 큐를 유지합니다.<br>
     */
    DROP_NEW,

    /**
     * Drop the oldest pending operation and enqueue the new one.<br><br>
     * 가장 오래된 대기 연산을 버리고 새 연산을 추가합니다.<br>
     */
    DROP_OLDEST,

    /**
     * Clear all pending operations and enqueue the new one.<br><br>
     * 모든 대기 연산을 비우고 새 연산만 추가합니다.<br>
     */
    CLEAR_AND_ENQUEUE,
}

/**
 * Drop reason emitted when an operation is not executed.<br><br>
 * 연산이 실행되지 못했을 때 전달되는 드롭 사유입니다.<br>
 */
enum class QueueDropReason {
    /**
     * Dropped because the queue is full and policy is DROP_NEW.<br><br>
     * 큐가 가득 찬 상태에서 DROP_NEW 정책으로 드롭되었습니다.<br>
     */
    QUEUE_FULL_DROP_NEW,

    /**
     * Dropped because the queue is full and policy is DROP_OLDEST.<br><br>
     * 큐가 가득 찬 상태에서 DROP_OLDEST 정책으로 드롭되었습니다.<br>
     */
    QUEUE_FULL_DROP_OLDEST,

    /**
     * Dropped because the queue is full and policy is CLEAR_AND_ENQUEUE.<br><br>
     * 큐가 가득 찬 상태에서 CLEAR_AND_ENQUEUE 정책으로 드롭되었습니다.<br>
     */
    QUEUE_FULL_CLEAR,

    /**
     * Dropped due to explicit queue clear-and-enqueue call.<br><br>
     * 명시적 clear-and-enqueue 호출로 드롭되었습니다.<br>
     */
    CLEARED_EXPLICIT,

    /**
     * Dropped because the queue was cleared via API call.<br><br>
     * clearQueue API 호출로 큐가 비워져 드롭되었습니다.<br>
     */
    CLEARED_BY_API,

    /**
     * Dropped because the operation was merged with a newer one.<br><br>
     * 더 최신 연산과 병합되어 드롭되었습니다.<br>
     */
    MERGED,
}

/**
 * Queue debug event type for operational tracing.<br><br>
 * 운영 추적을 위한 큐 디버그 이벤트 타입입니다.<br>
 */
enum class QueueEventType {
    /**
     * Operation enqueued successfully.<br><br>
     * 연산이 정상적으로 큐에 추가되었습니다.<br>
     */
    ENQUEUED,

    /**
     * Operation dropped due to policy or clear call.<br><br>
     * 정책 또는 clear 호출로 연산이 드롭되었습니다.<br>
     */
    DROPPED,

    /**
     * Operation started execution.<br><br>
     * 연산 실행이 시작되었습니다.<br>
     */
    STARTED,

    /**
     * Operation completed execution.<br><br>
     * 연산 실행이 완료되었습니다.<br>
     */
    COMPLETED,

    /**
     * Unexpected error occurred during processing.<br><br>
     * 처리 중 예기치 못한 오류가 발생했습니다.<br>
     */
    ERROR,

    /**
     * Queue cleared explicitly.<br><br>
     * 큐가 명시적으로 비워졌습니다.<br>
     */
    CLEARED,
}

/**
 * Debug event payload for queue operations.<br><br>
 * 큐 연산 디버그 이벤트 페이로드입니다.<br>
 */
data class QueueDebugEvent(
    /**
     * Event type for the queue operation.<br><br>
     * 큐 연산 이벤트 타입입니다.<br>
     */
    val type: QueueEventType,
    /**
     * Operation name (may be null when not applicable).<br><br>
     * 연산 이름이며, 필요 없을 경우 null일 수 있습니다.<br>
     */
    val operationName: String?,
    /**
     * Current pending queue size after the event.<br><br>
     * 이벤트 이후 현재 대기 큐 크기입니다.<br>
     */
    val pendingSize: Int,
    /**
     * Whether an operation is currently being processed.<br><br>
     * 현재 연산 처리 중인지 여부입니다.<br>
     */
    val isProcessing: Boolean,
    /**
     * Thread name where the event is emitted.<br><br>
     * 이벤트가 발생한 스레드 이름입니다.<br>
     */
    val threadName: String,
    /**
     * Drop reason when type is DROPPED or CLEARED.<br><br>
     * 이벤트 타입이 DROPPED 또는 CLEARED일 때 드롭 사유입니다.<br>
     */
    val dropReason: QueueDropReason? = null,
    /**
     * Optional message for debug context.<br><br>
     * 디버그 맥락을 위한 선택적 메시지입니다.<br>
     */
    val message: String? = null,
)
