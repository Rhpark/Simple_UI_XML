package kr.open.library.simple_ui.core.system_manager.controller.alarm

/**
 * Constants for alarm functionality throughout the system.<br><br>
 * 시스템 전체에서 사용되는 알람 관련 상수들.<br>
 */
public object AlarmConstants {
    /**
     * Key for storing alarm identifier in Intent extras.<br><br>
     * Intent extras에 알람 식별자를 저장하기 위한 키입니다.<br>
     */
    public const val ALARM_KEY: String = "AlarmKey"

    /**
     * Default value for alarm key when not found in Intent extras.<br><br>
     * Intent extras에서 찾을 수 없을 때 알람 키의 기본값입니다.<br>
     */
    public const val ALARM_KEY_DEFAULT_VALUE: Int = -1

    /**
     * Tag identifier for WakeLock instances.<br><br>
     * WakeLock 인스턴스의 태그 식별자입니다.<br>
     */
    public const val WAKELOCK_TAG: String = "SystemManager:AlarmReceiver"

    /**
     * Maximum timeout for WakeLock to prevent battery drain (10 minutes).<br><br>
     * 배터리 소모를 방지하기 위한 WakeLock의 최대 타임아웃(10분)입니다.<br>
     */
    public const val WAKELOCK_TIMEOUT_MS: Long = 10 * 60 * 1000L // 10 minutes

    /**
     * Default time to hold WakeLock for alarm processing (3 seconds).<br><br>
     * 알람 처리를 위해 WakeLock을 유지하는 기본 시간(3초)입니다.<br>
     */
    public const val DEFAULT_ACQUIRE_TIME_MS: Long = 3000L // 3 seconds

    /**
     * Milliseconds in one second for time calculations.<br><br>
     * 시간 계산을 위한 1초당 밀리초입니다.<br>
     */
    public const val MILLISECONDS_IN_SECOND: Long = 1000L

    /**
     * Seconds in one minute for time calculations.<br><br>
     * 시간 계산을 위한 1분당 초입니다.<br>
     */
    public const val SECONDS_IN_MINUTE: Long = 60L

    /**
     * Minutes in one hour for time calculations.<br><br>
     * 시간 계산을 위한 1시간당 분입니다.<br>
     */
    public const val MINUTES_IN_HOUR: Long = 60L

    /**
     * Identifier for standard alarm clock type.<br><br>
     * 표준 알람 시계 타입의 식별자입니다.<br>
     */
    public const val ALARM_TYPE_CLOCK: String = "ALARM_CLOCK"

    /**
     * Identifier for idle-allowed alarm type.<br><br>
     * 유휴 모드 허용 알람 타입의 식별자입니다.<br>
     */
    public const val ALARM_TYPE_IDLE: String = "ALLOW_WHILE_IDLE"

    /**
     * Identifier for exact idle-allowed alarm type.<br><br>
     * 정확한 유휴 모드 허용 알람 타입의 식별자입니다.<br>
     */
    public const val ALARM_TYPE_EXACT_IDLE: String = "EXACT_AND_ALLOW_WHILE_IDLE"

    /**
     * Error code for invalid time values.<br><br>
     * 유효하지 않은 시간 값에 대한 오류 코드입니다.<br>
     */
    public const val ERROR_INVALID_TIME: Int = -1001

    /**
     * Error code for PendingIntent creation failure.<br><br>
     * PendingIntent 생성 실패에 대한 오류 코드입니다.<br>
     */
    public const val ERROR_PENDING_INTENT_FAILED: Int = -1002

    /**
     * Error code for alarm registration failure.<br><br>
     * 알람 등록 실패에 대한 오류 코드입니다.<br>
     */
    public const val ERROR_ALARM_REGISTRATION_FAILED: Int = -1003
}
