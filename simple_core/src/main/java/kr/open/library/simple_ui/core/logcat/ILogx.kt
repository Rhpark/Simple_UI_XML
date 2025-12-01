package kr.open.library.simple_ui.core.logcat

import android.content.Context
import kr.open.library.simple_ui.core.logcat.model.LogxType
import java.util.EnumSet

/**
 * Core interface for the Logx logging library.<br>
 * Provides an abstraction layer for testability and extensibility.<br><br>
 * Logx 로깅 라이브러리의 핵심 인터페이스입니다.<br>
 * 테스트 가능성과 확장성을 위한 추상화 레이어를 제공합니다.<br>
 */
interface ILogx {
    /**
     * Initializes the Logx logging system with Android Context.<br>
     * This enables Context-dependent features such as file logging with optimal storage paths and lifecycle-based flush management.<br><br>
     * Android Context로 Logx 로깅 시스템을 초기화합니다.<br>
     * 최적 저장소 경로를 사용한 파일 로깅 및 Lifecycle 기반 플러시 관리와 같은 Context 의존 기능을 활성화합니다.<br>
     *
     * @param context The Android application context.<br><br>
     *                Android 애플리케이션 컨텍스트.
     */
    fun init(context: Context)

    // ============================================================
    // Standard Logging Methods | 표준 로깅 메서드
    // ============================================================

    /**
     * Logs a verbose message with current stack trace information.<br><br>
     * 현재 스택 트레이스 정보와 함께 Verbose 메시지를 로깅합니다.<br>
     */
    fun v()

    /**
     * Logs a verbose message.<br><br>
     * Verbose 메시지를 로깅합니다.<br>
     *
     * @param msg The message to log. Can be any object (will be converted to string).<br><br>
     *            로깅할 메시지. 모든 객체 가능 (문자열로 변환됨).
     */
    fun v(msg: Any? = "")

    /**
     * Logs a verbose message with a custom tag.<br><br>
     * 커스텀 태그와 함께 Verbose 메시지를 로깅합니다.<br>
     *
     * @param tag The custom tag for this log entry.<br><br>
     *            이 로그 항목의 커스텀 태그.
     *
     * @param msg The message to log.<br><br>
     *            로깅할 메시지.
     */
    fun v(
        tag: String,
        msg: Any?,
    )

    /**
     * Logs a debug message with current stack trace information.<br><br>
     * 현재 스택 트레이스 정보와 함께 Debug 메시지를 로깅합니다.<br>
     */
    fun d()

    /**
     * Logs a debug message.<br><br>
     * Debug 메시지를 로깅합니다.<br>
     *
     * @param msg The message to log. Can be any object (will be converted to string).<br><br>
     *            로깅할 메시지. 모든 객체 가능 (문자열로 변환됨).
     */
    fun d(msg: Any? = "")

    /**
     * Logs a debug message with a custom tag.<br><br>
     * 커스텀 태그와 함께 Debug 메시지를 로깅합니다.<br>
     *
     * @param tag The custom tag for this log entry.<br><br>
     *            이 로그 항목의 커스텀 태그.
     *
     * @param msg The message to log.<br><br>
     *            로깅할 메시지.
     */
    fun d(
        tag: String,
        msg: Any?,
    )

    /**
     * Logs an info message with current stack trace information.<br><br>
     * 현재 스택 트레이스 정보와 함께 Info 메시지를 로깅합니다.<br>
     */
    fun i()

    /**
     * Logs an info message.<br><br>
     * Info 메시지를 로깅합니다.<br>
     *
     * @param msg The message to log. Can be any object (will be converted to string).<br><br>
     *            로깅할 메시지. 모든 객체 가능 (문자열로 변환됨).
     */
    fun i(msg: Any? = "")

    /**
     * Logs an info message with a custom tag.<br><br>
     * 커스텀 태그와 함께 Info 메시지를 로깅합니다.<br>
     *
     * @param tag The custom tag for this log entry.<br><br>
     *            이 로그 항목의 커스텀 태그.
     *
     * @param msg The message to log.<br><br>
     *            로깅할 메시지.
     */
    fun i(
        tag: String,
        msg: Any?,
    )

    /**
     * Logs a warning message with current stack trace information.<br><br>
     * 현재 스택 트레이스 정보와 함께 Warning 메시지를 로깅합니다.<br>
     */
    fun w()

    /**
     * Logs a warning message.<br><br>
     * Warning 메시지를 로깅합니다.<br>
     *
     * @param msg The message to log. Can be any object (will be converted to string).<br><br>
     *            로깅할 메시지. 모든 객체 가능 (문자열로 변환됨).
     */
    fun w(msg: Any? = "")

    /**
     * Logs a warning message with a custom tag.<br><br>
     * 커스텀 태그와 함께 Warning 메시지를 로깅합니다.<br>
     *
     * @param tag The custom tag for this log entry.<br><br>
     *            이 로그 항목의 커스텀 태그.
     *
     * @param msg The message to log.<br><br>
     *            로깅할 메시지.
     */
    fun w(
        tag: String,
        msg: Any?,
    )

    /**
     * Logs an error message with current stack trace information.<br><br>
     * 현재 스택 트레이스 정보와 함께 Error 메시지를 로깅합니다.<br>
     */
    fun e()

    /**
     * Logs an error message.<br><br>
     * Error 메시지를 로깅합니다.<br>
     *
     * @param msg The message to log. Can be any object (will be converted to string).<br><br>
     *            로깅할 메시지. 모든 객체 가능 (문자열로 변환됨).
     */
    fun e(msg: Any? = "")

    /**
     * Logs an error message with a custom tag.<br><br>
     * 커스텀 태그와 함께 Error 메시지를 로깅합니다.<br>
     *
     * @param tag The custom tag for this log entry.<br><br>
     *            이 로그 항목의 커스텀 태그.
     *
     * @param msg The message to log.<br><br>
     *            로깅할 메시지.
     */
    fun e(
        tag: String,
        msg: Any?,
    )

    // ============================================================
    // Extended Features | 확장 기능
    // ============================================================

    /**
     * Logs parent method call information with current stack trace.<br><br>
     * 현재 스택 트레이스와 함께 부모 메서드 호출 정보를 로깅합니다.<br>
     */
    fun p()

    /**
     * Logs parent method call information with a message.<br><br>
     * 메시지와 함께 부모 메서드 호출 정보를 로깅합니다.<br>
     *
     * @param msg The message to log.<br><br>
     *            로깅할 메시지.
     */
    fun p(msg: Any? = "")

    /**
     * Logs parent method call information with a custom tag and message.<br><br>
     * 커스텀 태그와 메시지와 함께 부모 메서드 호출 정보를 로깅합니다.<br>
     *
     * @param tag The custom tag for this log entry.<br><br>
     *            이 로그 항목의 커스텀 태그.
     *
     * @param msg The message to log.<br><br>
     *            로깅할 메시지.
     */
    fun p(
        tag: String,
        msg: Any?,
    )

    /**
     * Logs the current thread ID with stack trace information.<br><br>
     * 스택 트레이스 정보와 함께 현재 스레드 ID를 로깅합니다.<br>
     */
    fun t()

    /**
     * Logs the current thread ID with a message.<br><br>
     * 메시지와 함께 현재 스레드 ID를 로깅합니다.<br>
     *
     * @param msg The message to log.<br><br>
     *            로깅할 메시지.
     */
    fun t(msg: Any? = "")

    /**
     * Logs the current thread ID with a custom tag and message.<br><br>
     * 커스텀 태그와 메시지와 함께 현재 스레드 ID를 로깅합니다.<br>
     *
     * @param tag The custom tag for this log entry.<br><br>
     *            이 로그 항목의 커스텀 태그.
     *
     * @param msg The message to log.<br><br>
     *            로깅할 메시지.
     */
    fun t(
        tag: String,
        msg: Any?,
    )

    /**
     * Logs a JSON string with proper formatting and visual markers.<br><br>
     * 적절한 포맷팅과 시각적 마커를 사용하여 JSON 문자열을 로깅합니다.<br>
     *
     * @param msg The JSON string to log and format.<br><br>
     *            로깅하고 포맷할 JSON 문자열.
     */
    fun j(msg: String)

    /**
     * Logs a JSON string with a custom tag, proper formatting and visual markers.<br><br>
     * 커스텀 태그, 적절한 포맷팅과 시각적 마커를 사용하여 JSON 문자열을 로깅합니다.<br>
     *
     * @param tag The custom tag for this log entry.<br><br>
     *            이 로그 항목의 커스텀 태그.
     *
     * @param msg The JSON string to log and format.<br><br>
     *            로깅하고 포맷할 JSON 문자열.
     */
    fun j(
        tag: String,
        msg: String,
    )

    // ============================================================
    // Configuration Methods | 설정 메서드
    // ============================================================

    /**
     * Enables or disables debug logging globally.<br><br>
     * 디버그 로깅을 전역적으로 활성화 또는 비활성화합니다.<br>
     *
     * @param isDebug `true` to enable debug logging, `false` to disable.<br><br>
     *                디버그 로깅을 활성화하려면 `true`, 비활성화하려면 `false`.
     */
    fun setDebugMode(isDebug: Boolean)

    /**
     * Enables or disables tag-based filtering for debug logs.<br><br>
     * 디버그 로그에 대한 태그 기반 필터링을 활성화 또는 비활성화합니다.<br>
     *
     * @param isFilter `true` to enable filtering (only logs matching debugFilterList will be displayed), `false` to show all logs.<br><br>
     *                 필터링을 활성화하려면 `true` (debugFilterList와 일치하는 로그만 표시됨), 모든 로그를 표시하려면 `false`.
     */
    fun setDebugFilter(isFilter: Boolean)

    /**
     * Enables or disables saving logs to a file.<br><br>
     * 로그를 파일에 저장하는 기능을 활성화 또는 비활성화합니다.<br>
     *
     * @param isSave `true` to save logs to file, `false` to disable file logging.<br><br>
     *               로그를 파일에 저장하려면 `true`, 파일 로깅을 비활성화하려면 `false`.
     */
    fun setSaveToFile(isSave: Boolean)

    /**
     * Sets the absolute path where log files will be saved.<br><br>
     * 로그 파일이 저장될 절대 경로를 설정합니다.<br>
     *
     * @param path The absolute file path for log storage.<br><br>
     *             로그 저장을 위한 절대 파일 경로.
     */
    fun setFilePath(path: String)

    /**
     * Sets the application name used in log file naming and organization.<br><br>
     * 로그 파일 이름 지정 및 구성에 사용되는 애플리케이션 이름을 설정합니다.<br>
     *
     * @param name The application name.<br><br>
     *             애플리케이션 이름.
     */
    fun setAppName(name: String)

    /**
     * Sets which log types should be displayed.<br><br>
     * 표시할 로그 타입을 설정합니다.<br>
     *
     * @param types An EnumSet of LogxType values to enable (VERBOSE, DEBUG, INFO, WARN, ERROR, PARENT, JSON, THREAD_ID).<br><br>
     *              활성화할 LogxType 값의 EnumSet (VERBOSE, DEBUG, INFO, WARN, ERROR, PARENT, JSON, THREAD_ID).
     */
    fun setDebugLogTypeList(types: EnumSet<LogxType>)

    /**
     * Sets the list of tags to filter when debug filtering is enabled.<br><br>
     * 디버그 필터링이 활성화되었을 때 필터링할 태그 목록을 설정합니다.<br>
     *
     * @param tags A list of tag strings. Only logs with matching tags will be displayed when filtering is enabled.<br><br>
     *             태그 문자열 목록. 필터링이 활성화되면 일치하는 태그의 로그만 표시됩니다.
     */
    fun setDebugFilterList(tags: List<String>)
}
