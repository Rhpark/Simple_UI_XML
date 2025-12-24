package kr.open.library.simple_ui.core.system_manager.info.battery

object BatteryStateConstants {
    /**
     * Default update cycle time in milliseconds.<br><br>
     * 기본 업데이트 주기 시간 (밀리초)입니다.<br>
     */
    public const val DEFAULT_UPDATE_CYCLE_TIME = 2000L

    /**
     * Error value (Integer.MIN_VALUE) used when integer battery information cannot be retrieved.<br><br>
     * 정수형 배터리 정보를 가져올 수 없을 때 사용하는 오류 값(Integer.MIN_VALUE)입니다.<br>
     */
    public const val BATTERY_ERROR_VALUE: Int = Integer.MIN_VALUE

    /**
     * Error value (Long.MIN_VALUE) used when long battery information cannot be retrieved.<br><br>
     * Long형 배터리 정보를 가져올 수 없을 때 사용하는 오류 값(Long.MIN_VALUE)입니다.<br>
     */
    public const val BATTERY_ERROR_VALUE_LONG: Long = Long.MIN_VALUE

    /**
     * Error value (Integer.MIN_VALUE as Double) used when double battery information cannot be retrieved.<br><br>
     * Double형 배터리 정보를 가져올 수 없을 때 사용하는 오류 값(Integer.MIN_VALUE를 Double로 변환)입니다.<br>
     */
    public const val BATTERY_ERROR_VALUE_DOUBLE: Double = Integer.MIN_VALUE.toDouble()

    /**
     * Error value (null) used when boolean battery information cannot be retrieved or status is unavailable.<br><br>
     * Boolean형 배터리 정보를 가져올 수 없거나 상태를 알 수 없을 때 사용하는 오류 값(null)입니다.<br>
     */
    public val BATTERY_ERROR_VALUE_BOOLEAN: Boolean? = null

    /**
     * String representation for USB charging.<br><br>
     * USB 충전을 나타내는 문자열입니다.<br>
     */
    public const val STR_CHARGE_PLUG_USB: String = "USB"

    /**
     * String representation for AC (wall adapter) charging.<br><br>
     * AC(벽면 어댑터) 충전을 나타내는 문자열입니다.<br>
     */
    public const val STR_CHARGE_PLUG_AC: String = "AC"

    /**
     * String representation for dock charging.<br><br>
     * 독 충전을 나타내는 문자열입니다.<br>
     */
    public const val STR_CHARGE_PLUG_DOCK: String = "DOCK"

    /**
     * String representation for unknown charging type.<br><br>
     * 알 수 없는 충전 타입을 나타내는 문자열입니다.<br>
     */
    public const val STR_CHARGE_PLUG_UNKNOWN: String = "UNKNOWN"

    /**
     * String representation for wireless charging.<br><br>
     * 무선 충전을 나타내는 문자열입니다.<br>
     */
    public const val STR_CHARGE_PLUG_WIRELESS: String = "WIRELESS"

    /**
     * String representation for not charging state.<br><br>
     * 충전 중이 아닌 상태를 나타내는 문자열입니다.<br>
     */
    public const val STR_CHARGE_PLUG_NONE: String = "NONE"

    /**
     * String representation for good battery health.<br><br>
     * 양호한 배터리 상태를 나타내는 문자열입니다.<br>
     */
    public const val STR_BATTERY_HEALTH_GOOD: String = "GOOD"

    /**
     * String representation for cold battery health.<br><br>
     * 저온 배터리 상태를 나타내는 문자열입니다.<br>
     */
    public const val STR_BATTERY_HEALTH_COLD: String = "COLD"

    /**
     * String representation for dead battery health.<br><br>
     * 손상된 배터리 상태를 나타내는 문자열입니다.<br>
     */
    public const val STR_BATTERY_HEALTH_DEAD: String = "DEAD"

    /**
     * String representation for over-voltage battery health.<br><br>
     * 과전압 배터리 상태를 나타내는 문자열입니다.<br>
     */
    public const val STR_BATTERY_HEALTH_OVER_VOLTAGE: String = "OVER_VOLTAGE"

    /**
     * String representation for unknown battery health.<br><br>
     * 알 수 없는 배터리 상태를 나타내는 문자열입니다.<br>
     */
    public const val STR_BATTERY_HEALTH_UNKNOWN: String = "UNKNOWN"
}
