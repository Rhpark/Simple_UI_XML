package kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard

/**
 * Failure reason for keyboard operation result.<br><br>
 * 키보드 동작 결과 실패 사유입니다.<br>
 */
public enum class SoftKeyboardFailureReason {
    OFF_MAIN_THREAD,
    INVALID_ARGUMENT,
    FOCUS_REQUEST_FAILED,
    WINDOW_TOKEN_MISSING,
    IME_REQUEST_REJECTED,
    EXCEPTION_OCCURRED,
}

/**
 * Result model for keyboard operations that wait for actual IME visibility change.<br><br>
 * 실제 IME 가시성 변경까지 대기하는 키보드 동작의 결과 모델입니다.<br>
 */
public sealed interface SoftKeyboardActionResult {
    /**
     * Operation completed and expected IME visibility was observed.<br><br>
     * 동작이 완료되었고 기대한 IME 가시성 상태가 확인되었습니다.<br>
     */
    public data object Success : SoftKeyboardActionResult

    /**
     * IME request was issued, but expected visibility was not observed within timeout.<br><br>
     * IME 요청은 전달되었지만 제한 시간 내에 기대한 가시성 상태를 확인하지 못했습니다. 이후 시점에 상태가 변경될 가능성은 남아 있습니다.<br>
     */
    public data object Timeout : SoftKeyboardActionResult

    /**
     * Operation failed with explicit reason.<br><br>
     * 명시적인 사유로 동작이 실패했습니다.<br>
     */
    public data class Failure(
        val reason: SoftKeyboardFailureReason,
        val message: String? = null,
    ) : SoftKeyboardActionResult
}
