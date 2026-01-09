package kr.open.library.simple_ui.core.system_manager.controller.alarm.vo

import android.net.Uri
import kr.open.library.simple_ui.core.system_manager.controller.alarm.AlarmConstants
import java.util.Locale

/**
 * Data Transfer Object for alarm information with input validation and immutable design.<br>
 * Represents all necessary data for creating and managing alarms in the system.<br><br>
 * 입력 유효성 검증과 불변 설계를 가진 알람 정보용 데이터 전송 객체입니다.<br>
 * 시스템에서 알람을 생성하고 관리하는 데 필요한 모든 데이터를 나타냅니다.<br>
 *
 * @param key Unique identifier for the alarm (must be positive).<br><br>
 *            알람의 고유 식별자 (양수여야 함).
 *
 * @param title Display title for the alarm notification.<br><br>
 *              알람 알림의 표시 제목.
 *
 * @param message Detailed message for the alarm notification.<br><br>
 *                알람 알림의 상세 메시지.
 *
 * @param isActive Whether the alarm is currently active.<br><br>
 *                 알람이 현재 활성 상태인지 여부.
 *
 * @param isAllowIdle Whether the alarm can fire during device idle time.<br><br>
 *                    기기 유휴 시간에 알람이 실행될 수 있는지 여부.
 *
 * @param vibrationPattern Vibration pattern as an immutable list (null for no vibration).<br><br>
 *                         불변 리스트로서의 진동 패턴 (진동 없음은 null).
 *
 * @param soundUri URI for alarm sound (null for default system sound).<br><br>
 *                 알람 소리의 URI (기본 시스템 소리는 null).
 *
 * @param hour Hour of day (0-23).<br><br>
 *             시간 (0-23).
 *
 * @param minute Minute of hour (0-59).<br><br>
 *               분 (0-59).
 *
 * @param second Second of minute (0-59).<br><br>
 *               초 (0-59).
 *
 * @param acquireTime Maximum WakeLock acquire time in milliseconds.<br><br>
 *                    WakeLock 획득 최대 시간 (밀리초 단위).
 */
public data class AlarmVO(
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
    public val acquireTime: Long = AlarmConstants.DEFAULT_ACQUIRE_TIME_MS,
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
     * Creates a copy of this alarmVo with modified active state.<br><br>
     * 활성 상태가 수정된 이 alarmVo의 복사본을 생성합니다.<br>
     *
     * @param active The new active state.<br><br>
     *               새로운 활성 상태.
     *
     * @return A copy of this AlarmVo with the specified active state.<br><br>
     *         지정된 활성 상태를 가진 AlarmVo의 복사본.<br>
     */
    public fun withActiveState(active: Boolean): AlarmVO = copy(isActive = active)

    /**
     * Creates a copy of this alarmVo with modified time.<br><br>
     * 시간이 수정된 이 alarmVo의 복사본을 생성합니다.<br>
     *
     * @param hour The new hour (0-23).<br><br>
     *             새로운 시간 (0-23).
     *
     * @param minute The new minute (0-59).<br><br>
     *               새로운 분 (0-59).
     *
     * @param second The new second (0-59), defaults to current second.<br><br>
     *               새로운 초 (0-59), 기본값은 현재 초.
     *
     * @return A copy of this AlarmVo with the specified time.<br><br>
     *         지정된 시간을 가진 AlarmVo의 복사본.<br>
     */
    public fun withTime(
        hour: Int,
        minute: Int,
        second: Int = this.second,
    ): AlarmVO = copy(hour = hour, minute = minute, second = second)

    /**
     * Formats the alarm time as HH:MM:SS string.<br><br>
     * 알람 시간을 HH:MM:SS 문자열로 형식화합니다.<br>
     *
     * @return Formatted time string in HH:MM:SS format.<br><br>
     *         HH:MM:SS 형식의 형식화된 시간 문자열.<br>
     */
    public fun getFormattedTime(): String = String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second)

    /**
     * Calculates total seconds since midnight for easy comparison.<br><br>
     * 자정 이후 총 초를 계산하여 쉬운 비교를 가능하게 합니다.<br>
     *
     * @return Total seconds since midnight.<br><br>
     *         자정 이후 총 초.<br>
     */
    public fun getTotalSeconds(): Int = hour * 3600 + minute * 60 + second

    /**
     * Returns a brief description of the alarm for logging purposes.<br><br>
     * 로깅 목적으로 알람의 간단한 설명을 반환합니다.<br>
     *
     * @return A brief description string.<br><br>
     *         간단한 설명 문자열.<br>
     */
    public fun getDescription(): String = "Alarm[$key]: '$title' at ${getFormattedTime()}, active: $isActive"

    /**
     * Legacy compatibility property for msg.
     * @deprecated Use message instead
     */
    @Deprecated(
        message = "Use message instead",
        replaceWith = ReplaceWith("message"),
    )
    companion object {
        /**
         * Creates a simple alarm with minimal configuration.<br><br>
         * 최소한의 구성으로 간단한 알람을 생성합니다.<br>
         *
         * @param key Unique identifier for the alarm.<br><br>
         *            알람의 고유 식별자.
         *
         * @param title Display title for the alarm.<br><br>
         *              알람의 표시 제목.
         *
         * @param message Alarm message.<br><br>
         *                알람 메시지.
         *
         * @param hour Hour of day (0-23).<br><br>
         *             시간 (0-23).
         *
         * @param minute Minute of hour (0-59).<br><br>
         *               분 (0-59).
         *
         * @return A simple AlarmVo instance.<br><br>
         *         간단한 AlarmVo 인스턴스.<br>
         */
        public fun createSimple(
            key: Int,
            title: String,
            message: String,
            hour: Int,
            minute: Int,
        ): AlarmVO = AlarmVO(
            key = key,
            title = title,
            message = message,
            hour = hour,
            minute = minute,
        )

        /**
         * Creates an alarm that can fire during device idle time.<br><br>
         * 기기 유휴 시간에도 실행될 수 있는 알람을 생성합니다.<br>
         *
         * @param key Unique identifier for the alarm.<br><br>
         *            알람의 고유 식별자.
         *
         * @param title Display title for the alarm.<br><br>
         *              알람의 표시 제목.
         *
         * @param message Alarm message.<br><br>
         *                알람 메시지.
         *
         * @param hour Hour of day (0-23).<br><br>
         *             시간 (0-23).
         *
         * @param minute Minute of hour (0-59).<br><br>
         *               분 (0-59).
         *
         * @param second Second of minute (0-59), defaults to 0.<br><br>
         *               초 (0-59), 기본값은 0.
         *
         * @return An idle-allowed AlarmVo instance.<br><br>
         *         유휴 모드 허용 AlarmVo 인스턴스.<br>
         */
        public fun createIdleAllowed(
            key: Int,
            title: String,
            message: String,
            hour: Int,
            minute: Int,
            second: Int = 0,
        ): AlarmVO = AlarmVO(
            key = key,
            title = title,
            message = message,
            hour = hour,
            minute = minute,
            second = second,
            isAllowIdle = true,
        )
    }
}
