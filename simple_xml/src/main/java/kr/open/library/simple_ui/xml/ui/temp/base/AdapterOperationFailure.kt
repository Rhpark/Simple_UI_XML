package kr.open.library.simple_ui.xml.ui.temp.base

import kr.open.library.simple_ui.xml.ui.adapter.queue.QueueDropReason

/**
 * Failure details for queued operations.<br><br>
 * 큐 연산 실패 상세 정보입니다.<br>
 */
sealed class AdapterOperationFailure {
    /**
     * Validation failure caused by invalid arguments or state.<br><br>
     * 잘못된 인자 또는 상태로 인한 검증 실패입니다.<br>
     */
    data class Validation(
        /**
         * Failure message for validation.<br><br>
         * 검증 실패 메시지입니다.<br>
         */
        val message: String,
    ) : AdapterOperationFailure()

    /**
     * Exception thrown during operation execution.<br><br>
     * 연산 실행 중 발생한 예외입니다.<br>
     */
    data class Exception(
        /**
         * Exception instance for debugging.<br><br>
         * 디버깅을 위한 예외 인스턴스입니다.<br>
         */
        val error: kotlin.Exception,
    ) : AdapterOperationFailure()

    /**
     * Operation dropped due to queue policy or clear action.<br><br>
     * 큐 정책 또는 clear 동작으로 연산이 드롭되었습니다.<br>
     */
    data class Dropped(
        /**
         * Drop reason emitted by queue policy.<br><br>
         * 큐 정책에서 전달된 드롭 사유입니다.<br>
         */
        val reason: QueueDropReason,
        /**
         * Optional message for context.<br><br>
         * 맥락을 위한 선택적 메시지입니다.<br>
         */
        val message: String? = null,
    ) : AdapterOperationFailure()
}
