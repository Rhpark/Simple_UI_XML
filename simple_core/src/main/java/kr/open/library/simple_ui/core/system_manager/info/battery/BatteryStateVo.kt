package kr.open.library.simple_ui.core.system_manager.info.battery

object BatteryStateVo {
    /**
     * Default update cycle time in milliseconds.<br><br>
     * 기본 업데이트 주기 시간 (밀리초)입니다.<br>
     */
    public const val DEFAULT_UPDATE_CYCLE_TIME = 1000L

    /**
     * Error value used when battery information cannot be retrieved.<br><br>
     * 배터리 정보를 가져올 수 없을 때 사용하는 오류 값입니다.<br>
     */
    public const val BATTERY_ERROR_VALUE: Int = Integer.MIN_VALUE
    public const val BATTERY_ERROR_VALUE_LONG: Long = Long.MIN_VALUE
    public const val BATTERY_ERROR_VALUE_DOUBLE: Double = Integer.MIN_VALUE.toDouble()
    public const val BATTERY_ERROR_VALUE_BOOLEAN: Boolean = false

    // Charge plug type strings / 충전 플러그 유형 문자열
    public const val STR_CHARGE_PLUG_USB: String = "USB"
    public const val STR_CHARGE_PLUG_AC: String = "AC"
    public const val STR_CHARGE_PLUG_DOCK: String = "DOCK"
    public const val STR_CHARGE_PLUG_UNKNOWN: String = "UNKNOWN"
    public const val STR_CHARGE_PLUG_WIRELESS: String = "WIRELESS"

    // Battery health status strings / 배터리 상태 문자열
    public const val STR_BATTERY_HEALTH_GOOD: String = "GOOD"
    public const val STR_BATTERY_HEALTH_COLD: String = "COLD"
    public const val STR_BATTERY_HEALTH_DEAD: String = "DEAD"
    public const val STR_BATTERY_HEALTH_OVER_VOLTAGE: String = "OVER_VOLTAGE"
    public const val STR_BATTERY_HEALTH_UNKNOWN: String = "UNKNOWN"
}
