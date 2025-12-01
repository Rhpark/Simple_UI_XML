package kr.open.library.simple_ui.core.logcat.model

/**
 * Defines the available log types for the Logx logging system.<br>
 * Includes standard Android log levels and extended features for specialized logging.<br><br>
 * Logx 로깅 시스템에서 사용 가능한 로그 타입을 정의합니다.<br>
 * 표준 Android 로그 레벨과 특수 로깅을 위한 확장 기능을 포함합니다.<br>
 *
 * @property logTypeString The single-character identifier for this log type.<br><br>
 *                         이 로그 타입의 단일 문자 식별자.<br>
 */
public enum class LogxType(
    public val logTypeString: String,
) {
    /**
     * Verbose log level (lowest priority).<br>
     * Maps to Android's Log.v().<br><br>
     * Verbose 로그 레벨 (가장 낮은 우선순위).<br>
     * Android의 Log.v()에 매핑됩니다.<br>
     */
    VERBOSE("V"),

    /**
     * Debug log level.<br>
     * Maps to Android's Log.d().<br><br>
     * Debug 로그 레벨.<br>
     * Android의 Log.d()에 매핑됩니다.<br>
     */
    DEBUG("D"),

    /**
     * Info log level.<br>
     * Maps to Android's Log.i().<br><br>
     * Info 로그 레벨.<br>
     * Android의 Log.i()에 매핑됩니다.<br>
     */
    INFO("I"),

    /**
     * Warning log level.<br>
     * Maps to Android's Log.w().<br><br>
     * Warning 로그 레벨.<br>
     * Android의 Log.w()에 매핑됩니다.<br>
     */
    WARN("W"),

    /**
     * Error log level (highest priority).<br>
     * Maps to Android's Log.e().<br><br>
     * Error 로그 레벨 (가장 높은 우선순위).<br>
     * Android의 Log.e()에 매핑됩니다.<br>
     */
    ERROR("E"),

    /**
     * Extended feature: Parent method call tracking.<br>
     * Displays the calling method's information along with the log message.<br>
     * Maps to Android's Log.d().<br><br>
     * 확장 기능: 부모 메서드 호출 추적.<br>
     * 로그 메시지와 함께 호출한 메서드의 정보를 표시합니다.<br>
     * Android의 Log.d()에 매핑됩니다.<br>
     */
    PARENT("P"),

    /**
     * Extended feature: JSON formatting.<br>
     * Formats and displays JSON strings with proper indentation and markers.<br>
     * Maps to Android's Log.i().<br><br>
     * 확장 기능: JSON 포맷팅.<br>
     * JSON 문자열을 적절한 들여쓰기와 마커로 포맷하여 표시합니다.<br>
     * Android의 Log.i()에 매핑됩니다.<br>
     */
    JSON("J"),

    /**
     * Extended feature: Thread ID display.<br>
     * Includes the current thread ID in the log output.<br>
     * Maps to Android's Log.d().<br><br>
     * 확장 기능: 스레드 ID 표시.<br>
     * 로그 출력에 현재 스레드 ID를 포함합니다.<br>
     * Android의 Log.d()에 매핑됩니다.<br>
     */
    THREAD_ID("T"),
}
