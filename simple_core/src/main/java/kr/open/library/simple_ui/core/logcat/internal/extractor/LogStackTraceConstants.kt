package kr.open.library.simple_ui.core.logcat.internal.extractor

/**
 * Constants used for stack trace extraction.<br><br>
 * 스택 트레이스 추출에 사용하는 상수 모음입니다.<br>
 */
internal object LogStackTraceConstants {
    /**
     * Internal prefix for Logx package.<br><br>
     * Logx 내부 패키지 접두사.<br>
     */
    const val LOGCAT_INTERNAL_PREFIX: String = "kr.open.library.simple_ui.core.logcat"

    /**
     * Default fallback start index.<br><br>
     * 내부 프레임을 찾지 못했을 때 사용할 기본 시작 인덱스.<br>
     */
    const val DEFAULT_FALLBACK_START_INDEX: Int = 4

    /**
     * Unknown file name marker.<br><br>
     * 알 수 없는 파일명 마커.<br>
     */
    const val UNKNOWN_FILE_NAME: String = "Unknown"

    /**
     * Unknown method marker.<br><br>
     * 알 수 없는 메서드명 마커.<br>
     */
    const val UNKNOWN_METHOD: String = "unknown"

    /**
     * Unknown class marker.<br><br>
     * 알 수 없는 클래스명 마커.<br>
     */
    const val UNKNOWN_CLASS: String = "unknown"

    /**
     * Default fallback line number.<br><br>
     * 기본 라인 번호 값.<br>
     */
    const val FALLBACK_LINE_NUMBER: Int = 0

    /**
     * Excluded prefixes when inferring start prefix candidates.<br><br>
     * 시작 프레픽스 후보에서 제외할 접두사 목록.<br>
     */
    val START_PREFIX_EXCLUDES: List<String> = listOf(
        "java.",
        "kotlin.",
        "kotlinx.",
        "android.",
        "androidx.",
        "dalvik.",
    )

    /**
     * D8 synthetic class marker.<br><br>
     * D8 합성 클래스 마커.<br>
     */
    const val D8_SYNTHETIC_CLASS: String = "D8" + '$' + '$' + "SyntheticClass"

    /**
     * Access method prefix marker.<br><br>
     * 접근자 메서드 접두사 마커.<br>
     */
    const val ACCESS_PREFIX: String = "access" + '$'
}
