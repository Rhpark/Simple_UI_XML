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
     * Public external storage on API 28 and below; falls back to app-specific external storage (Documents) on API 29+.<br>
     * Requires WRITE_EXTERNAL_STORAGE permission on API 28 and below.<br><br>
     * API 28 이하에서는 공용 외부 저장소를 사용하며, API 29+에서는 앱 전용 외부 저장소(Documents 디렉터리)로 대체됩니다.<br>
     * API 28 이하에서는 WRITE_EXTERNAL_STORAGE 권한이 필요합니다.<br>
     */
    PUBLIC_EXTERNAL,
}
