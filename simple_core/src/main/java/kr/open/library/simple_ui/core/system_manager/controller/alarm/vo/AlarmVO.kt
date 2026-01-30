package kr.open.library.simple_ui.core.system_manager.controller.alarm.vo

import android.net.Uri
import kr.open.library.simple_ui.core.system_manager.controller.alarm.AlarmConstants
import java.util.Calendar
import java.util.Locale

/**
 * Alarm date information for fixed-date scheduling.<br><br>
 * 특정 날짜 알람 스케줄 정보를 담는 DTO입니다.<br>
 *
 * @param year Target year (>= 1970).<br><br>
 *             대상 연도(1970 이상)입니다.<br>
 * @param month Target month (1-12).<br><br>
 *              대상 월(1-12)입니다.<br>
 * @param day Target day of month (1-31, validated with Calendar).<br><br>
 *            대상 일(1-31)이며 Calendar 기반으로 실제 유효 날짜를 검증합니다.<br>
 * @throws IllegalArgumentException If the date is invalid (e.g., 2026-02-31).<br><br>
 *                                 유효하지 않은 날짜인 경우 예외가 발생합니다.<br>
 */
public data class AlarmDateVO(
    public val year: Int,
    public val month: Int, // 1~12
    public val day: Int,
) {
    init {
        require(year >= 1970) { "Year must be >= 1970, got: $year" }
        require(month in 1..12) { "Month must be between 1-12, got: $month" }
        require(day in 1..31) { "Day must be between 1-31, got: $day" }

        val calendar = Calendar.getInstance().apply {
            isLenient = false
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        calendar.time
    }
}

/**
 * Alarm schedule information for time-based triggers.<br><br>
 * 시간 기반 알람 스케줄 정보를 담는 DTO입니다.<br>
 * If date is provided, the alarm is scheduled for the specific date.<br><br>
 * date가 제공되면 특정 날짜 알람으로 동작합니다.<br>
 *
 * @param hour Hour of day (0-23).<br><br>
 *             시(0-23) 값입니다.<br>
 * @param minute Minute of hour (0-59).<br><br>
 *               분(0-59) 값입니다.<br>
 * @param second Second of minute (0-59).<br><br>
 *               초(0-59) 값입니다.<br>
 * @param idleMode Idle handling strategy for scheduling.<br><br>
 *                 유휴 모드 동작 전략입니다.<br>
 * @param date Optional fixed date; if set and in the past, registration throws.<br><br>
 *             고정 날짜이며 과거인 경우 등록 시 예외가 발생합니다.<br>
 */
public data class AlarmScheduleVO(
    public val hour: Int,
    public val minute: Int,
    public val second: Int = 0,
    public val idleMode: AlarmIdleMode = AlarmIdleMode.NONE,
    public val date: AlarmDateVO? = null,
) {
    init {
        require(hour in 0..23) { "Hour must be between 0-23, got: $hour" }
        require(minute in 0..59) { "Minute must be between 0-59, got: $minute" }
        require(second in 0..59) { "Second must be between 0-59, got: $second" }
    }
}

/**
 * Alarm idle mode for deciding scheduling strategy.<br><br>
 * 알람 유휴 모드 동작 방식을 정의합니다.<br>
 * AlarmController.registerBySchedule/resolveRegisterType에서 이 값에 따라 API를 선택합니다.<br>
 */
public enum class AlarmIdleMode {
    /**
     * No idle handling. Use alarm clock API (status bar indicator).<br><br>
     * 유휴 모드 처리 없음. 알람 시계 API(상태바 표시)를 사용합니다.<br>
     */
    NONE,

    /**
     * Allow while idle (inexact) using setAndAllowWhileIdle.<br><br>
     * setAndAllowWhileIdle 기반의 유휴 모드 허용(부정확) 알람입니다.<br>
     */
    INEXACT,

    /**
     * Allow while idle (exact) using setExactAndAllowWhileIdle.<br><br>
     * setExactAndAllowWhileIdle 기반의 유휴 모드 허용(정확) 알람입니다.<br>
     * Android 12+에서는 SCHEDULE_EXACT_ALARM 권한이 필요합니다.<br>
     */
    EXACT,
}

/**
 * Alarm notification payload for UI delivery.<br><br>
 * 알람 알림(UI) 정보를 담는 DTO입니다.<br>
 *
 * @param title Notification title (non-blank).<br><br>
 *              알림 제목(공백 불가)입니다.<br>
 * @param message Notification message (non-blank).<br><br>
 *                알림 메시지(공백 불가)입니다.<br>
 * @param vibrationPattern Optional vibration pattern in milliseconds.<br><br>
 *                          진동 패턴(밀리초) 목록입니다.<br>
 * @param soundUri Optional custom sound URI.<br><br>
 *                 커스텀 사운드 URI입니다.<br>
 */
public data class AlarmNotificationVO(
    public val title: String,
    public val message: String,
    public val vibrationPattern: List<Long>? = null,
    public val soundUri: Uri? = null,
) {
    init {
        require(title.isNotBlank()) { "Alarm title cannot be blank" }
        require(message.isNotBlank()) { "Alarm message cannot be blank" }

        vibrationPattern?.let { pattern ->
            require(pattern.isNotEmpty()) { "Vibration pattern cannot be empty if provided" }
            require(pattern.all { it >= 0 }) { "Vibration pattern values must be non-negative" }
        }
    }
}

/**
 * Alarm data that splits schedule and notification responsibilities.<br>
 * Represents all necessary data for creating and managing alarms.<br><br>
 * 스케줄 정보와 알림 정보를 분리해 책임을 명확히 한 알람 DTO입니다.<br>
 * 알람 생성 및 관리를 위한 모든 데이터를 포함합니다.<br>
 *
 * @param key Unique identifier for the alarm (must be positive).<br><br>
 *            알람 고유 식별자이며 양수여야 합니다.<br>
 * @param schedule Time scheduling information.<br><br>
 *                 알람 스케줄 정보입니다.<br>
 * @param notification Notification payload for UI.<br><br>
 *                     알림(UI) 전달 정보입니다.<br>
 * @param isActive Whether the alarm is currently active.<br><br>
 *                 알람 활성 상태 여부입니다.<br>
 * @param acquireTime Maximum WakeLock acquire time in milliseconds.<br><br>
 *                    WakeLock 최대 획득 시간(밀리초)입니다.<br>
 * @throws IllegalArgumentException If key/acquireTime is invalid.<br><br>
 *                                 key/acquireTime 값이 유효하지 않으면 예외가 발생합니다.<br>
 */
public data class AlarmVO(
    public val key: Int,
    public val schedule: AlarmScheduleVO,
    public val notification: AlarmNotificationVO,
    public val isActive: Boolean = true,
    public val acquireTime: Long = AlarmConstants.DEFAULT_ACQUIRE_TIME_MS,
) {
    init {
        require(key > 0) { "Alarm key must be positive, got: $key" }
        require(acquireTime > 0) { "Acquire time must be positive, got: $acquireTime" }
    }

    /**
     * Creates a copy of this alarm with modified active state.<br><br>
     * 활성 상태만 변경한 복사본을 생성합니다.<br>
     *
     * @param active The new active state.<br><br>
     *               변경할 활성 상태입니다.<br>
     * @return A copy with the specified active state.<br><br>
     *         변경된 활성 상태를 가진 복사본입니다.<br>
     */
    public fun withActiveState(active: Boolean): AlarmVO = copy(isActive = active)

    /**
     * Creates a copy of this alarm with modified time.<br><br>
     * 시간만 변경한 복사본을 생성합니다.<br>
     * date/idleMode 등 나머지 스케줄 값은 유지됩니다.<br>
     *
     * @param hour The new hour (0-23).<br><br>
     *             변경할 시(0-23)입니다.<br>
     * @param minute The new minute (0-59).<br><br>
     *               변경할 분(0-59)입니다.<br>
     * @param second The new second (0-59), defaults to current second.<br><br>
     *               변경할 초(0-59)이며 기본값은 기존 초입니다.<br>
     * @return A copy with the specified time.<br><br>
     *         변경된 시간 값을 가진 복사본입니다.<br>
     */
    public fun withTime(hour: Int, minute: Int, second: Int = schedule.second): AlarmVO =
        copy(schedule = schedule.copy(hour = hour, minute = minute, second = second))

    /**
     * Formats the alarm time as HH:MM:SS string.<br><br>
     * 알람 시간을 HH:MM:SS 형식 문자열로 반환합니다.<br>
     *
     * @return Formatted time string in HH:MM:SS format.<br><br>
     *         HH:MM:SS 형식 문자열입니다.<br>
     */
    public fun getFormattedTime(): String = String.format(
        Locale.getDefault(),
        "%02d:%02d:%02d",
        schedule.hour,
        schedule.minute,
        schedule.second
    )

    /**
     * Calculates total seconds since midnight for easy comparison.<br><br>
     * 자정 기준 누적 초를 계산합니다.<br>
     *
     * @return Total seconds since midnight.<br><br>
     *         자정 기준 누적 초입니다.<br>
     */
    public fun getTotalSeconds(): Int =
        schedule.hour * 3600 + schedule.minute * 60 + schedule.second

    /**
     * Returns a brief description of the alarm for logging purposes.<br><br>
     * 로그 기록을 위한 알람 요약 문자열을 반환합니다.<br>
     *
     * @return A brief description string.<br><br>
     *         알람 요약 문자열입니다.<br>
     */
    public fun getDescription(): String =
        "Alarm[$key]: '${notification.title}' at ${getFormattedTime()}, active: $isActive"

    companion object {
        /**
         * Creates a simple alarm with minimal configuration.<br><br>
         * 최소 구성의 알람을 생성합니다.<br>
         *
         * @param key Unique identifier for the alarm.<br><br>
         *            알람 고유 식별자입니다.<br>
         * @param title Display title for the alarm notification.<br><br>
         *              알람 알림 제목입니다.<br>
         * @param message Alarm message.<br><br>
         *                알람 메시지입니다.<br>
         * @param hour Hour of day (0-23).<br><br>
         *             시(0-23) 값입니다.<br>
         * @param minute Minute of hour (0-59).<br><br>
         *               분(0-59) 값입니다.<br>
         * @return A simple AlarmVO instance.<br><br>
         *         간단한 AlarmVO 인스턴스입니다.<br>
         */
        public fun createSimple(
            key: Int,
            title: String,
            message: String,
            hour: Int,
            minute: Int
        ): AlarmVO = AlarmVO(
            key = key,
            schedule = AlarmScheduleVO(hour = hour, minute = minute),
            notification = AlarmNotificationVO(title = title, message = message),
        )

        /**
         * Creates an alarm that can fire during device idle time (inexact).<br><br>
         * 유휴 모드에서도 동작 가능한(부정확) 알람을 생성합니다.<br>
         *
         * @param key Unique identifier for the alarm.<br><br>
         *            알람 고유 식별자입니다.<br>
         * @param title Display title for the alarm notification.<br><br>
         *              알람 알림 제목입니다.<br>
         * @param message Alarm message.<br><br>
         *                알람 메시지입니다.<br>
         * @param hour Hour of day (0-23).<br><br>
         *             시(0-23) 값입니다.<br>
         * @param minute Minute of hour (0-59).<br><br>
         *               분(0-59) 값입니다.<br>
         * @param second Second of minute (0-59), defaults to 0.<br><br>
         *               초(0-59) 값이며 기본값은 0입니다.<br>
         * @return An idle-allowed AlarmVO instance.<br><br>
         *         유휴 모드 허용 AlarmVO 인스턴스입니다.<br>
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
            schedule = AlarmScheduleVO(
                hour = hour,
                minute = minute,
                second = second,
                idleMode = AlarmIdleMode.INEXACT
            ),
            notification = AlarmNotificationVO(title = title, message = message),
        )

        /**
         * Creates an exact alarm that can fire during device idle time.<br><br>
         * 유휴 모드에서도 정확하게 동작하는 알람을 생성합니다.<br>
         *
         * @param key Unique identifier for the alarm.<br><br>
         *            알람 고유 식별자입니다.<br>
         * @param title Display title for the alarm notification.<br><br>
         *              알람 알림 제목입니다.<br>
         * @param message Alarm message.<br><br>
         *                알람 메시지입니다.<br>
         * @param hour Hour of day (0-23).<br><br>
         *             시(0-23) 값입니다.<br>
         * @param minute Minute of hour (0-59).<br><br>
         *               분(0-59) 값입니다.<br>
         * @param second Second of minute (0-59), defaults to 0.<br><br>
         *               초(0-59) 값이며 기본값은 0입니다.<br>
         * @return An exact idle-allowed AlarmVO instance.<br><br>
         *         정확 유휴 모드 알람용 AlarmVO 인스턴스입니다.<br>
         */
        public fun createExactIdleAllowed(
            key: Int,
            title: String,
            message: String,
            hour: Int,
            minute: Int,
            second: Int = 0,
        ): AlarmVO = AlarmVO(
            key = key,
            schedule = AlarmScheduleVO(
                hour = hour,
                minute = minute,
                second = second,
                idleMode = AlarmIdleMode.EXACT
            ),
            notification = AlarmNotificationVO(title = title, message = message),
        )

        /**
         * Creates an alarm scheduled for a specific date.<br><br>
         * 특정 날짜에 실행되는 알람을 생성합니다.<br>
         *
         * @param key Unique identifier for the alarm.<br><br>
         *            알람 고유 식별자입니다.<br>
         * @param title Display title for the alarm notification.<br><br>
         *              알람 알림 제목입니다.<br>
         * @param message Alarm message.<br><br>
         *                알람 메시지입니다.<br>
         * @param date Date information for scheduling.<br><br>
         *             스케줄링 날짜 정보입니다.<br>
         * @param hour Hour of day (0-23).<br><br>
         *             시(0-23) 값입니다.<br>
         * @param minute Minute of hour (0-59).<br><br>
         *               분(0-59) 값입니다.<br>
         * @param second Second of minute (0-59), defaults to 0.<br><br>
         *               초(0-59) 값이며 기본값은 0입니다.<br>
         * @return An AlarmVO instance scheduled for a specific date.<br><br>
         *         특정 날짜 알람용 AlarmVO 인스턴스입니다.<br>
         */
        public fun createOnDate(
            key: Int,
            title: String,
            message: String,
            date: AlarmDateVO,
            hour: Int,
            minute: Int,
            second: Int = 0,
        ): AlarmVO = AlarmVO(
            key = key,
            schedule = AlarmScheduleVO(
                hour = hour,
                minute = minute,
                second = second,
                date = date,
            ),
            notification = AlarmNotificationVO(title = title, message = message),
        )
    }
}
