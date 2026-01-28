package kr.open.library.simple_ui.core.logcat.internal.common

/**
 * Central constants for Logx behavior.<br><br>
 * Logx 동작에 사용하는 상수 모음이다.<br>
 */
internal object LogxConstants {
    /**
     * Default application name used in tags and file names.<br><br>
     * 태그/파일명에 사용하는 기본 앱 이름.<br>
     */
    const val DEFAULT_APP_NAME: String = "AppName"

    /**
     * JSON indentation size (spaces).<br><br>
     * JSON 들여쓰기 크기(공백 수).<br>
     */
    const val JSON_INDENT: Int = 4

    /**
     * Default log directory name.<br><br>
     * 기본 로그 디렉터리 이름.<br>
     */
    const val LOG_DIR_NAME: String = "AppLogs"

    /**
     * Default error tag used for internal error logs.<br><br>
     * 내부 에러 로그에 사용하는 기본 태그.<br>
     */
    const val ERROR_TAG: String = "ERROR"
}
