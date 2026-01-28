package kr.open.library.simple_ui.core.logcat.internal.formatter

/**
 * Constants for log formatting.<br><br>
 * 로그 포맷팅에 사용하는 상수 모음입니다.<br>
 */
internal object LogxFormatConstants {
    /**
     * Null message literal.<br><br>
     * null 메시지 리터럴.<br>
     */
    const val NULL_MESSAGE: String = "null"

    /**
     * Message separator between meta and body.<br><br>
     * 메타와 본문 사이 구분자.<br>
     */
    const val MESSAGE_SEPARATOR: String = " - "

    /**
     * Message separator for JSON header (no trailing space).<br><br>
     * JSON 헤더용 구분자(뒤 공백 없음).<br>
     */
    const val JSON_HEADER_SEPARATOR: String = " -"

    /**
     * Parent header prefix.<br><br>
     * 부모 헤더 프리픽스.<br>
     */
    const val PARENT_HEADER_PREFIX: String = "┌[PARENT] "

    /**
     * Parent header without trailing space.<br><br>
     * 뒤 공백 없는 부모 헤더.<br>
     */
    const val PARENT_HEADER_PLAIN: String = "┌[PARENT]"

    /**
     * Parent footer prefix.<br><br>
     * 부모 풋터 프리픽스.<br>
     */
    const val PARENT_FOOTER_PREFIX: String = "└[PARENT] "

    /**
     * JSON header marker.<br><br>
     * JSON 헤더 마커.<br>
     */
    const val JSON_HEADER_MARKER: String = "[JSON]"

    /**
     * JSON end marker.<br><br>
     * JSON 종료 마커.<br>
     */
    const val JSON_END_MARKER: String = "[End]"
}
