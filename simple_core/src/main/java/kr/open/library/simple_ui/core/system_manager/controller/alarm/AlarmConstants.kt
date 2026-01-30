package kr.open.library.simple_ui.core.system_manager.controller.alarm

/**
 * Constants for alarm functionality throughout the system.<br><br>
 * 시스템 전반에서 사용하는 알람 관련 상수입니다.<br>
 */
public object AlarmConstants {
    /**
     * Key for storing alarm identifier in Intent extras.<br><br>
     * Intent extras에 알람 식별자를 저장하는 키입니다.<br>
     */
    public const val ALARM_KEY: String = "AlarmKey"

    /**
     * Default value for alarm key when not found in Intent extras.<br><br>
     * Intent extras에 키가 없을 때 사용하는 기본값입니다.<br>
     */
    public const val ALARM_KEY_DEFAULT_VALUE: Int = -1

    /**
     * Tag identifier for WakeLock instances.<br><br>
     * WakeLock 인스턴스 식별용 태그입니다.<br>
     */
    public const val WAKELOCK_TAG: String = "SystemManager:AlarmReceiver"

    /**
     * Maximum timeout for WakeLock to prevent battery drain (10 minutes).<br><br>
     * 배터리 소모를 방지하기 위한 WakeLock 최대 시간(10분)입니다.<br>
     */
    public const val WAKELOCK_TIMEOUT_MS: Long = 10 * 60 * 1000L // 10 minutes

    /**
     * Default time to hold WakeLock for alarm processing (3 seconds).<br><br>
     * 알람 처리용 WakeLock 기본 유지 시간(3초)입니다.<br>
     */
    public const val DEFAULT_ACQUIRE_TIME_MS: Long = 3000L // 3 seconds

    /**
     * Action for exact alarm permission state changes (Android 12+).<br><br>
     * 정확 알람 권한 상태 변경 브로드캐스트 액션입니다(Android 12+).<br>
     */
    public const val ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED: String =
        "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
}
