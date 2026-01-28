package kr.open.library.simple_ui.core.logcat.config

import android.util.Log

/**
 * Defines log output types and how they map to Android Log calls.<br><br>
 * 로그 출력 타입과 Android Log 호출 매핑을 정의한다.<br>
 *
 * @property outputChar Single character used in file output.<br><br>
 *                     파일 출력에 사용하는 1글자 식별자.<br>
 */
enum class LogType(
    val outputChar: Char
) {
    /**
     * Verbose log type.<br><br>
     * VERBOSE 로그 타입이다.<br>
     */
    VERBOSE('V'),

    /**
     * Debug log type.<br><br>
     * DEBUG 로그 타입이다.<br>
     */
    DEBUG('D'),

    /**
     * Info log type.<br><br>
     * INFO 로그 타입이다.<br>
     */
    INFO('I'),

    /**
     * Warning log type.<br><br>
     * WARN 로그 타입이다.<br>
     */
    WARN('W'),

    /**
     * Error log type.<br><br>
     * ERROR 로그 타입이다.<br>
     */
    ERROR('E'),

    /**
     * Parent trace log type.<br><br>
     * PARENT 로그 타입이다.<br>
     */
    PARENT('P'),

    /**
     * JSON log type.<br><br>
     * JSON 로그 타입이다.<br>
     */
    JSON('J'),

    /**
     * Thread id log type.<br><br>
     * THREAD 로그 타입이다.<br>
     */
    THREAD('T'),
    ;

    /**
     * Writes a message using the Android Log level for this type.<br><br>
     * 타입에 맞는 Android Log 레벨로 메시지를 출력한다.<br>
     *
     * @param tag Tag string to use for logging.<br><br>
     *            로그에 사용할 태그 문자열.<br>
     * @param message Message to output.<br><br>
     *                출력할 메시지.<br>
     */
    fun writeToLog(tag: String, message: String) {
        when (this) {
            VERBOSE -> Log.v(tag, message)
            DEBUG -> Log.d(tag, message)
            INFO -> Log.i(tag, message)
            WARN -> Log.w(tag, message)
            ERROR -> Log.e(tag, message)
            PARENT, JSON, THREAD -> Log.d(tag, message)
        }
    }
}
