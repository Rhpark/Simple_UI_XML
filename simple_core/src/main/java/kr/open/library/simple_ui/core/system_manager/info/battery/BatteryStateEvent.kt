package kr.open.library.simple_ui.core.system_manager.info.battery

/**
 * Sealed class representing various battery state events.<br>
 * Each event carries observable battery information.<br><br>
 * 다양한 배터리 상태 이벤트를 나타내는 sealed 클래스입니다.<br>
 * 각 이벤트는 관찰 가능한 배터리 정보를 포함합니다.<br>
 */
public sealed class BatteryStateEvent {
    /**
     * Event for battery capacity updates.<br><br>
     * 배터리 잔량 변화에 대한 이벤트입니다.<br>
     *
     * @param percent Battery remaining capacity as percentage (0-100).<br><br>
     *                배터리 잔량(0~100%)<br>
     */
    public data class OnCapacity(
        val percent: Int,
    ) : BatteryStateEvent()

    /**
     * Event for battery charge counter updates.<br><br>
     * 배터리 충전 카운터 값 변경에 대한 이벤트입니다.<br>
     *
     * @param counter Battery charge counter in microampere-hours (µAh).<br><br>
     *                배터리 충전 카운터 값(µAh 단위)<br>
     */
    public data class OnChargeCounter(
        val counter: Int,
    ) : BatteryStateEvent()

    /**
     * Event for power source connection type updates.<br><br>
     * 전원 연결 형태 변경에 대한 이벤트입니다.<br>
     *
     * @param type Power source type as defined in [android.os.BatteryManager].<br><br>
     *             [android.os.BatteryManager]에 정의된 전원 타입<br>
     */
    public data class OnChargePlug(
        val type: Int,
    ) : BatteryStateEvent()

    /**
     * Event for battery temperature updates.<br><br>
     * 배터리 온도 변경에 대한 이벤트입니다.<br>
     *
     * @param temperature Battery temperature in Celsius.<br><br>
     *                    배터리 온도(섭씨)<br>
     */
    public data class OnTemperature(
        val temperature: Double,
    ) : BatteryStateEvent()

    /**
     * Event for battery voltage updates.<br><br>
     * 배터리 전압 변경에 대한 이벤트입니다.<br>
     *
     * @param voltage Battery voltage in volts.<br><br>
     *                배터리 전압(볼트)<br>
     */
    public data class OnVoltage(
        val voltage: Double,
    ) : BatteryStateEvent()

    /**
     * Event for instantaneous battery current updates.<br><br>
     * 순간 배터리 전류 변경에 대한 이벤트입니다.<br>
     *
     * @param current Current in microamperes (µA); positive when charging, negative when discharging.<br><br>
     *                마이크로암페어(µA) 단위 전류, 충전 시 양수·방전 시 음수<br>
     */
    public data class OnCurrentAmpere(
        val current: Int,
    ) : BatteryStateEvent()

    /**
     * Event for average battery current updates.<br><br>
     * 평균 배터리 전류 변경에 대한 이벤트입니다.<br>
     *
     * @param current Average current in microamperes (µA); positive when charging, negative when discharging.<br><br>
     *                마이크로암페어(µA) 단위 평균 전류, 충전 시 양수·방전 시 음수<br>
     */
    public data class OnCurrentAverageAmpere(
        val current: Int,
    ) : BatteryStateEvent()

    /**
     * Event for battery charge status updates.<br><br>
     * 배터리 충전 상태 변경에 대한 이벤트입니다.<br>
     *
     * @param status Charge status as defined in [android.os.BatteryManager].<br><br>
     *               [android.os.BatteryManager]에 정의된 충전 상태<br>
     */
    public data class OnChargeStatus(
        val status: Int,
    ) : BatteryStateEvent()

    /**
     * Event for battery health updates.<br><br>
     * 배터리 건강 상태 변경에 대한 이벤트입니다.<br>
     *
     * @param health Health status as defined in [android.os.BatteryManager].<br><br>
     *               [android.os.BatteryManager]에 정의된 배터리 건강 상태<br>
     */
    public data class OnHealth(
        val health: Int,
    ) : BatteryStateEvent()

    /**
     * Event for battery energy counter updates.<br><br>
     * 배터리 에너지 카운터 변경에 대한 이벤트입니다.<br>
     *
     * @param energy Energy in nanowatt-hours (nWh).<br><br>
     *               나노와트시(nWh) 단위 에너지 값<br>
     */
    public data class OnEnergyCounter(
        val energy: Long,
    ) : BatteryStateEvent()

    /**
     * Event for battery presence status updates.<br><br>
     * 배터리 존재 여부 변경에 대한 이벤트입니다.<br>
     *
     * @param present `true` if battery is present; `false` otherwise.<br><br>
     *                배터리가 있으면 `true`, 없으면 `false`<br>
     */
    public data class OnPresent(
        val present: Boolean,
    ) : BatteryStateEvent()

    /**
     * Event for battery total capacity updates.<br><br>
     * 배터리 총 용량 추정값 변경에 대한 이벤트입니다.<br>
     *
     * @param totalCapacity Total battery capacity in milliampere-hours (mAh).<br><br>
     *                      배터리 총 용량(mAh)<br>
     */
    public data class OnTotalCapacity(
        val totalCapacity: Double,
    ) : BatteryStateEvent()
}
