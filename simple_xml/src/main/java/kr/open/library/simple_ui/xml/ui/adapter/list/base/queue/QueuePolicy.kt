package kr.open.library.simple_ui.xml.ui.adapter.list.base.queue

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
}
