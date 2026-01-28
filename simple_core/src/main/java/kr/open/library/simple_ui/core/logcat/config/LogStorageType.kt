package kr.open.library.simple_ui.core.logcat.config

/**
 * Defines storage targets for log file output.<br><br>
 * 로그 파일 저장 대상 위치를 정의한다.<br>
 */
enum class LogStorageType {
    /**
     * Internal app storage.<br><br>
     * 앱 내부 저장소.<br>
     */
    INTERNAL,

    /**
     * App-specific external storage.<br><br>
     * 앱 전용 외부 저장소.<br>
     */
    APP_EXTERNAL,

    /**
     * Public external storage (requires permission on API 28 and below).<br><br>
     * 공용 외부 저장소(API 28 이하에서 권한 필요).<br>
     */
    PUBLIC_EXTERNAL,
}
