package kr.open.library.simple_ui.xml.ui.temp.base

/**
 * Thread contract validation mode for adapter APIs.<br><br>
 * 어댑터 API 스레드 계약 검증 모드입니다.<br>
 */
enum class AdapterThreadCheckMode {
    /**
     * Log warnings on violation.<br><br>
     * 위반 시 경고 로그를 남깁니다.<br>
     */
    LOG,

    /**
     * Throw an exception on violation.<br><br>
     * 위반 시 예외를 발생시킵니다.<br>
     */
    CRASH,

    /**
     * Disable thread checks.<br><br>
     * 스레드 검증을 비활성화합니다.<br>
     */
    OFF,
}
