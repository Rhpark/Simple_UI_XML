package kr.open.library.simple_ui.core.logcat.internal.writer

import kr.open.library.simple_ui.core.logcat.config.LogType

/**
 * Logcat 출력을 담당하는 내부 writer입니다.
 *
 * Internal writer responsible for Logcat output.
 * <br><br>
 * Logcat으로 로그를 출력하는 역할을 합니다.
 */
internal object LogxConsoleWriter {
    /**
     * 단일 로그 메시지를 출력합니다.
     *
     * Writes a single log message to Logcat.
     * <br><br>
     * 단일 로그 메시지를 Logcat에 출력합니다.
     *
     * @param type 로그 타입.
     * @param tag 출력 태그.
     * @param message 출력 메시지.
     */
    fun write(type: LogType, tag: String, message: String) {
        type.writeToLog(tag, message)
    }

    /**
     * 여러 라인의 로그 메시지를 순차 출력합니다.
     *
     * Writes multiple log lines to Logcat.
     * <br><br>
     * 다중 라인 로그를 Logcat에 순차적으로 출력합니다.
     *
     * @param type 로그 타입.
     * @param tag 출력 태그.
     * @param lines 출력 라인 목록.
     */
    fun writeLines(type: LogType, tag: String, lines: List<String>) {
        lines.forEach { line -> type.writeToLog(tag, line) }
    }
}

