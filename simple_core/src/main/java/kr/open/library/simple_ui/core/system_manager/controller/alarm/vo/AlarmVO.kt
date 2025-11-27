package kr.open.library.simple_ui.core.system_manager.controller.alarm.vo

import android.net.Uri
import java.util.Locale

/**
 * Constants for alarm functionality throughout the system
 * 시스템 전체에서 사용되는 알람 관련 상수들
 */
public object AlarmConstants {
    
    /**
     * Intent extras
     */
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
     * WakeLock settings
     */
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
     * Calendar settings
     */
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
     * Alarm type identifiers
     */
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
     * Error codes
     */
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


/**
 * Data Transfer Object for alarm information with input validation and immutable design.
 * Represents all necessary data for creating and managing alarms in the system.
 *
 * 입력 유효성 검증과 불변 설계를 가진 알람 정보용 데이터 전송 객체입니다.
 * 시스템에서 알람을 생성하고 관리하는 데 필요한 모든 데이터를 나타냅니다.
 *
 * @param key Unique identifier for the alarm (must be positive)
 * @param title Display title for the alarm notification
 * @param message Detailed message for the alarm notification
 * @param isActive Whether the alarm is currently active
 * @param isAllowIdle Whether the alarm can fire during device idle time
 * @param vibrationPattern Vibration pattern as an immutable list (null for no vibration)
 * @param soundUri URI for alarm sound (null for default system sound)
 * @param hour Hour of day (0-23)
 * @param minute Minute of hour (0-59)
 * @param second Second of minute (0-59)
 * @param acquireTime Maximum WakeLock acquire time in milliseconds
 */
public data class AlarmVo(
    public val key: Int,
    public val title: String,
    public val message: String,
    public val isActive: Boolean = true, // Default to active for immediate use
    public val isAllowIdle: Boolean = false,
    public val vibrationPattern: List<Long>? = null, // Immutable list instead of array
    public val soundUri: Uri? = null, // RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    public val hour: Int,
    public val minute: Int,
    public val second: Int = 0, // Default to 0 seconds for cleaner times
    public val acquireTime: Long = AlarmConstants.DEFAULT_ACQUIRE_TIME_MS
) {

    /**
     * Initialization block that validates all input parameters.<br>
     * Ensures data integrity by checking ranges and business rules.<br><br>
     * 모든 입력 파라미터의 유효성을 검증하는 초기화 블록입니다.<br>
     * 범위와 비즈니스 규칙을 확인하여 데이터 무결성을 보장합니다.<br>
     */
    init {
        // Input validation to ensure data integrity
        require(key > 0) { "Alarm key must be positive, got: $key" }
        require(title.isNotBlank()) { "Alarm title cannot be blank" }
        require(message.isNotBlank()) { "Alarm message cannot be blank" }
        require(hour in 0..23) { "Hour must be between 0-23, got: $hour" }
        require(minute in 0..59) { "Minute must be between 0-59, got: $minute" }
        require(second in 0..59) { "Second must be between 0-59, got: $second" }
        require(acquireTime > 0) { "Acquire time must be positive, got: $acquireTime" }

        // Validate vibration pattern if provided
        vibrationPattern?.let { pattern ->
            require(pattern.isNotEmpty()) { "Vibration pattern cannot be empty if provided" }
            require(pattern.all { it >= 0 }) { "Vibration pattern values must be non-negative" }
        }
    }

    /**
     * Creates a copy of this alarmVo with modified active state.
     * 활성 상태가 수정된 이 alarmVo의 복사본을 생성합니다.
     */
    public fun withActiveState(active: Boolean): AlarmVo = copy(isActive = active)

    /**
     * Creates a copy of this alarmVo with modified time.
     * 시간이 수정된 이 alarmVo의 복사본을 생성합니다.
     */
    public fun withTime(hour: Int, minute: Int, second: Int = this.second): AlarmVo {
        return copy(hour = hour, minute = minute, second = second)
    }

    /**
     * Formats the alarm time as HH:MM:SS string.
     * 알람 시간을 HH:MM:SS 문자열로 형식화합니다.
     */
    public fun getFormattedTime(): String = String.format(Locale.getDefault(),"%02d:%02d:%02d", hour, minute, second)

    /**
     * Calculates total seconds since midnight for easy comparison.
     * 자정 이후 총 초를 계산하여 쉬운 비교를 가능하게 합니다.
     */
    public fun getTotalSeconds(): Int = hour * 3600 + minute * 60 + second

    /**
     * Returns a brief description of the alarm for logging purposes.
     * 로깅 목적으로 알람의 간단한 설명을 반환합니다.
     */
    public fun getDescription(): String = "Alarm[$key]: '$title' at ${getFormattedTime()}, active: $isActive"

    /**
     * Legacy compatibility property for vibrationEffect.
     * @deprecated Use vibrationPattern instead
     */
    @Deprecated(
        message = "Use vibrationPattern instead",
        replaceWith = ReplaceWith("vibrationPattern?.toLongArray()")
    )
    public val vibrationEffect: LongArray?
        get() = vibrationPattern?.toLongArray()

    /**
     * Legacy compatibility property for msg.
     * @deprecated Use message instead
     */
    @Deprecated(
        message = "Use message instead",
        replaceWith = ReplaceWith("message")
    )
    public val msg: String
        get() = message

    companion object {
        /**
         * Creates a simple alarm with minimal configuration.
         * 최소한의 구성으로 간단한 알람을 생성합니다.
         */
        public fun createSimple(
            key: Int,
            title: String,
            message: String,
            hour: Int,
            minute: Int
        ): AlarmVo {
            return AlarmVo(
                key = key,
                title = title,
                message = message,
                hour = hour,
                minute = minute
            )
        }

        /**
         * Creates an alarm that can fire during device idle time.
         * 기기 유휴 시간에도 실행될 수 있는 알람을 생성합니다.
         */
        public fun createIdleAllowed(
            key: Int,
            title: String,
            message: String,
            hour: Int,
            minute: Int,
            second: Int = 0
        ): AlarmVo {
            return AlarmVo(
                key = key,
                title = title,
                message = message,
                hour = hour,
                minute = minute,
                second = second,
                isAllowIdle = true
            )
        }
    }
}