package kr.open.library.simple_ui.core.system_manager.info.battery.internal.model

/**
 * Internal data class representing a snapshot of battery metrics.<br><br>
 * 배터리 메트릭 스냅샷을 나타내는 내부 데이터 클래스입니다.<br>
 *
 * @param capacity Battery capacity percentage (0-100).<br><br>
 *                 배터리 용량 백분율 (0-100).<br>
 * @param chargeCounter Remaining battery charge in microampere-hours (µAh).<br><br>
 *                      마이크로암페어시 단위의 남은 배터리 충전량 (µAh).<br>
 * @param chargePlug Charging plug type (AC, USB, Wireless, etc.).<br><br>
 *                   충전 플러그 타입 (AC, USB, Wireless 등).<br>
 * @param currentAmpere Current battery current in microamperes (µA).<br><br>
 *                      마이크로암페어 단위의 현재 배터리 전류 (µA).<br>
 * @param chargeStatus Charging status (Charging, Discharging, Full, etc.).<br><br>
 *                     충전 상태 (충전 중, 방전 중, 완충 등).<br>
 * @param currentAverageAmpere Average battery current in microamperes (µA).<br><br>
 *                             마이크로암페어 단위의 평균 배터리 전류 (µA).<br>
 * @param energyCounter Battery energy counter in nanowatt-hours (nWh).<br><br>
 *                      나노와트시 단위의 배터리 에너지 카운터 (nWh).<br>
 * @param health Battery health status (Good, Overheat, Dead, etc.).<br><br>
 *               배터리 건강 상태 (양호, 과열, 손상 등).<br>
 * @param present Whether the battery is present in the device.<br><br>
 *                기기에 배터리가 장착되어 있는지 여부.<br>
 * @param temperature Battery temperature in degrees Celsius (°C).<br><br>
 *                    섭씨 온도 단위의 배터리 온도 (°C).<br>
 * @param voltage Battery voltage in volts (V).<br><br>
 *                볼트 단위의 배터리 전압 (V).<br>
 */
internal data class BatteryStateData(
    val capacity: Int,
    val chargeCounter: Int,
    val chargePlug: Int,
    val currentAmpere: Int,
    val chargeStatus: Int,
    val currentAverageAmpere: Int,
    val energyCounter: Long,
    val health: Int,
    val present: Boolean?,
    val temperature: Double,
    val voltage: Double
)
