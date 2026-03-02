package kr.open.library.simple_ui.xml.ui.adapter.list.base.result

/**
 * Shared drop reason exposed by adapter result APIs.<br><br>
 * 어댑터 결과 API에서 공통으로 노출하는 드롭 사유입니다.<br>
 */
public enum class AdapterDropReason {
    /**
     * Dropped because the queue was full and the new operation was rejected.<br><br>
     * 큐가 가득 차 새 연산이 거부되어 드롭되었습니다.<br>
     */
    DROP_NEW,

    /**
     * Dropped because the queue was full and the oldest pending operation was removed.<br><br>
     * 큐가 가득 차 가장 오래된 대기 연산이 제거되어 드롭되었습니다.<br>
     */
    DROP_OLDEST,

    /**
     * Dropped because the queue was cleared before enqueueing the latest operation.<br><br>
     * 최신 연산을 추가하기 전에 큐를 비우는 과정에서 드롭되었습니다.<br>
     */
    CLEAR_AND_ENQUEUE,

    /**
     * Dropped by an explicit clear-and-enqueue request.<br><br>
     * 명시적 clear-and-enqueue 요청으로 드롭되었습니다.<br>
     */
    CLEARED_EXPLICIT,

    /**
     * Dropped because the queue was cleared through API call.<br><br>
     * API 호출로 큐가 비워지면서 드롭되었습니다.<br>
     */
    CLEARED_BY_API,
}
