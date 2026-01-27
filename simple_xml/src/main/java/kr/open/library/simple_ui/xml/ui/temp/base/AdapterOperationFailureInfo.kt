package kr.open.library.simple_ui.xml.ui.temp.base

/**
 * Failure information delivered for queued operations.<br><br>
 * 큐 연산 실패 전달 정보입니다.<br>
 */
data class AdapterOperationFailureInfo(
    /**
     * Operation name where failure occurred.<br><br>
     * 실패가 발생한 연산 이름입니다.<br>
     */
    val operationName: String,
    /**
     * Failure details for the operation.<br><br>
     * 해당 연산의 실패 상세 정보입니다.<br>
     */
    val failure: AdapterOperationFailure,
)
