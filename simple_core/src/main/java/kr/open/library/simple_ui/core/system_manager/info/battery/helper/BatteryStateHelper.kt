package kr.open.library.simple_ui.core.system_manager.info.battery.helper

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.extensions.getBatteryManager
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateEvent
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateVo.BATTERY_ERROR_VALUE
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateVo.BATTERY_ERROR_VALUE_BOOLEAN
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateVo.BATTERY_ERROR_VALUE_DOUBLE
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateVo.BATTERY_ERROR_VALUE_LONG
import kr.open.library.simple_ui.core.system_manager.info.battery.helper.power.PowerProfile

/**
 * Internal helper that queries battery properties from the system services.<br><br>
 * 시스템 서비스에서 배터리 속성을 조회하는 내부 헬퍼입니다.<br>
 */
internal class BatteryStateHelper(
    private val context: Context,
    /**
     * Lazy BatteryManager instance used to query battery properties.<br><br>
     * 배터리 속성을 조회하기 위해 사용하는 지연 초기화 BatteryManager입니다.<br>
     */
    public val bm: BatteryManager = context.getBatteryManager(),
    /**
     * Lazy PowerProfile instance used for capacity estimation fallbacks.<br><br>
     * 용량 추정 폴백에 사용하는 지연 초기화 PowerProfile 인스턴스입니다.<br>
     */
    private val powerProfile: PowerProfile = PowerProfile(context)
) {
    /**
     * SharedFlow that stores battery events with buffering.<br>
     * Buffer size: 1 (replay) + 16 (extra) = 17 total.<br><br>
     * 버퍼링을 통해 배터리 이벤트를 보관하는 SharedFlow입니다.<br>
     * 버퍼 크기: 1 (replay) + 16 (추가) = 총 17개.<br>
     */
    private val msfUpdate: MutableSharedFlow<BatteryStateEvent> = MutableSharedFlow(
        replay = 1,
        extraBufferCapacity = 16,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )

    /**
     * Returns the SharedFlow that emits battery state events.<br><br>
     * 배터리 상태 이벤트를 방출하는 SharedFlow를 반환합니다.<br>
     *
     * @return SharedFlow that emits BatteryStateEvent whenever battery information changes.<br><br>
     *         배터리 정보가 변경될 때마다 BatteryStateEvent를 방출하는 SharedFlow입니다.<br>
     */
    public fun getSfUpdate(): SharedFlow<BatteryStateEvent> = msfUpdate.asSharedFlow()

    /**
     * Cached battery metrics using MutableStateFlow for reactive flows.<br><br>
     * 반응형 플로우용 MutableStateFlow로 관리되는 배터리 메트릭 캐시입니다.<br>
     */
    private val msfCapacity = MutableStateFlow(BATTERY_ERROR_VALUE)
    private val msfCurrentAmpere = MutableStateFlow(BATTERY_ERROR_VALUE)
    private val msfCurrentAverageAmpere = MutableStateFlow(BATTERY_ERROR_VALUE)
    private val msfChargeStatus = MutableStateFlow(BATTERY_ERROR_VALUE)
    private val msfChargeCounter = MutableStateFlow(BATTERY_ERROR_VALUE)
    private val msfChargePlug = MutableStateFlow(BATTERY_ERROR_VALUE)
    private val msfEnergyCounter = MutableStateFlow(BATTERY_ERROR_VALUE_LONG)
    private val msfHealth = MutableStateFlow(BATTERY_ERROR_VALUE)
    private val msfPresent = MutableStateFlow(BATTERY_ERROR_VALUE_BOOLEAN)
    private val msfTemperature = MutableStateFlow(BATTERY_ERROR_VALUE_DOUBLE)
    private val msfVoltage = MutableStateFlow(BATTERY_ERROR_VALUE_DOUBLE)

    /**
     * Safely sends a battery state event to the flow.<br><br>
     * 배터리 상태 이벤트를 플로우로 안전하게 전송합니다.<br>
     *
     * @param event The event to send.<br><br>
     *              전송할 이벤트.<br>
     */
    private fun sendFlow(event: BatteryStateEvent) {
        msfUpdate.tryEmit(event)
    }

    /**
     * Sets up reactive flows for all battery data updates.<br>
     * This method should be called after setting the coroutineScope.<br><br>
     * 모든 배터리 데이터 업데이트를 위한 반응형 플로우를 설정합니다.<br>
     * 이 메서드는 coroutineScope를 설정한 후에 호출해야 합니다.<br>
     */
    public fun setupDataFlows(coroutineScope: CoroutineScope) {
        coroutineScope.let { scope ->
            scope.launch {
                msfCapacity
                    .dropWhile { it == BATTERY_ERROR_VALUE }
                    .collect { sendFlow(BatteryStateEvent.OnCapacity(it)) }
            }
            scope.launch {
                msfCurrentAmpere
                    .dropWhile { it == BATTERY_ERROR_VALUE }
                    .collect { sendFlow(BatteryStateEvent.OnCurrentAmpere(it)) }
            }
            scope.launch {
                msfCurrentAverageAmpere
                    .dropWhile { it == BATTERY_ERROR_VALUE }
                    .collect { sendFlow(BatteryStateEvent.OnCurrentAverageAmpere(it)) }
            }
            scope.launch {
                msfChargeStatus
                    .dropWhile { it == BATTERY_ERROR_VALUE }
                    .collect { sendFlow(BatteryStateEvent.OnChargeStatus(it)) }
            }
            scope.launch {
                msfChargeCounter
                    .dropWhile { it == BATTERY_ERROR_VALUE }
                    .collect { sendFlow(BatteryStateEvent.OnChargeCounter(it)) }
            }
            scope.launch {
                msfChargePlug
                    .dropWhile { it == BATTERY_ERROR_VALUE }
                    .collect { sendFlow(BatteryStateEvent.OnChargePlug(it)) }
            }
            scope.launch {
                msfHealth
                    .dropWhile { it == BATTERY_ERROR_VALUE }
                    .collect { sendFlow(BatteryStateEvent.OnHealth(it)) }
            }
            scope.launch {
                msfEnergyCounter
                    .dropWhile { it == BATTERY_ERROR_VALUE_LONG }
                    .collect { sendFlow(BatteryStateEvent.OnEnergyCounter(it)) }
            }
            scope.launch {
                msfPresent
                    .dropWhile { it == BATTERY_ERROR_VALUE_BOOLEAN }
                    .collect { sendFlow(BatteryStateEvent.OnPresent(it)) }
            }
            scope.launch {
                msfTemperature
                    .dropWhile { it == BATTERY_ERROR_VALUE_DOUBLE }
                    .collect { sendFlow(BatteryStateEvent.OnTemperature(it)) }
            }
            scope.launch {
                msfVoltage
                    .dropWhile { it == BATTERY_ERROR_VALUE_DOUBLE }
                    .collect { sendFlow(BatteryStateEvent.OnVoltage(it)) }
            }
        }
    }

    /**
     * Refreshes cached battery metrics and emits events.<br><br>
     * 캐시된 배터리 지표를 갱신하고 이벤트를 방출합니다.<br>
     *
     * @param capacity Battery capacity percentage (0-100).<br><br>
     *                 배터리 용량 백분율 (0-100).<br>
     * @param currentAmpere Current battery current in microamperes (µA).<br><br>
     *                      마이크로암페어 단위의 현재 배터리 전류 (µA).<br>
     * @param chargeCounter Remaining battery charge in microampere-hours (µAh).<br><br>
     *                      마이크로암페어시 단위의 남은 배터리 충전량 (µAh).<br>
     * @param chargePlug Charging plug type (AC, USB, Wireless, etc.).<br><br>
     *                   충전 플러그 타입 (AC, USB, Wireless 등).<br>
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
    public fun updateBatteryInfo(
        capacity: Int,
        currentAmpere: Int,
        chargeCounter: Int,
        chargePlug: Int,
        chargeStatus: Int,
        currentAverageAmpere: Int,
        energyCounter: Long,
        health: Int,
        present: Boolean?,
        temperature: Double,
        voltage: Double
    ) {
        msfCapacity.value = capacity
        msfCurrentAmpere.value = currentAmpere
        msfChargeCounter.value = chargeCounter
        msfChargePlug.value = chargePlug
        msfChargeStatus.value = chargeStatus
        msfCurrentAverageAmpere.value = currentAverageAmpere
        msfEnergyCounter.value = energyCounter
        msfHealth.value = health
        msfPresent.value = present
        msfTemperature.value = temperature
        msfVoltage.value = voltage
    }

    /**
     * Reads an integer extra from the given battery intent.<br><br>
     * This method only reads the provided intent and does not query the system.<br>
     * For a stable value, pass a cached or refreshed intent (e.g., BatteryStateInfo#getBatteryStatus()).<br><br>
     *
     * 전달된 battery intent에서 정수 extra만 읽습니다.<br>
     * 이 메서드는 시스템 조회/receiver 등록을 수행하지 않습니다.<br>
     * 안정적인 값을 원하면 캐시/보완된 intent를 넘겨주세요(예: BatteryStateInfo#getBatteryStatus()).<br><br>
     *
     * @param batteryStatus Current battery status intent, if available.<br><br>
     *                      현재 battery status intent(있다면).<br>
     * @param type Intent extra key to read (e.g., BatteryManager.EXTRA_STATUS).<br><br>
     *             읽을 extra 키(예: BatteryManager.EXTRA_STATUS).<br>
     * @param defaultValue Value returned when the intent or extra is unavailable.<br><br>
     *                     intent/extra가 없을 때 반환할 기본값.<br>
     * @return The resolved value from the given intent, or defaultValue if missing.<br><br>
     *         intent에서 읽은 값, 없으면 defaultValue.<br>
     */
    public fun getStatus(batteryStatus: Intent?, type: String, defaultValue: Int = BATTERY_ERROR_VALUE): Int =
        safeCatch(defaultValue) {
            // Try to get charge status from current batteryStatus first
            // 먼저 현재 batteryStatus에서 충전 상태를 가져오기 시도
            var chargeStatus = defaultValue
            batteryStatus?.let {
                chargeStatus = it.getIntExtra(type, defaultValue)
            }

            return chargeStatus
        }

    /**
     * Gets an integer battery property from `BatteryManager`.<br><br>
     * `BatteryManager`에서 정수형 배터리 속성을 가져옵니다.<br>
     *
     * @param batteryType Battery property type constant (e.g., BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER).<br><br>
     *                    배터리 속성 타입 상수 (예: BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER).<br>
     * @return Integer value of the requested battery property.<br><br>
     *         요청한 배터리 속성의 정수 값.<br>
     */
    public fun getIntProperty(batteryType: Int) = bm.getIntProperty(batteryType)

    /**
     * Gets a long battery property from `BatteryManager`.<br><br>
     * `BatteryManager`에서 Long 타입 배터리 속성을 가져옵니다.<br>
     *
     * @param batteryType Battery property type constant (e.g., BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER).<br><br>
     *                    배터리 속성 타입 상수 (예: BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER).<br>
     * @return Long value of the requested battery property.<br><br>
     *         요청한 배터리 속성의 Long 값.<br>
     */
    public fun getLongProperty(batteryType: Int) = bm.getLongProperty(batteryType)

    /**
     * Gets the total battery capacity (rated capacity) in milliampere-hours (mAh).<br>
     * Uses multiple fallback methods for better compatibility across Android versions.<br><br>
     * 배터리의 총 용량(정격 용량)을 밀리암페어시(mAh) 단위로 가져옵니다.<br>
     * Android 버전 간 호환성을 위해 여러 fallback 방법을 사용합니다.<br>
     *
     * @return The total battery capacity in mAh, or default value if unavailable.<br><br>
     *         총 배터리 용량(mAh), 사용할 수 없는 경우 기본값.<br>
     */
    public fun getTotalCapacity(chargeCounter: Int, capacity: Int): Double = safeCatch(BATTERY_ERROR_VALUE_DOUBLE) {
        // Primary method: Use PowerProfile

        // 주요 방법: PowerProfile 사용
        val powerProfileCapacity = getPowerProfileBatteryCapacity()
        if (powerProfileCapacity > 0) {
            Logx.d("PowerProfile capacity: $powerProfileCapacity mAh")
            return powerProfileCapacity
        }

        // Fallback 1: Try to estimate from charge counter (API 21+)
        // Fallback 1: 충전 카운터로부터 추정 (API 21+)
        val estimatedCapacity = getChargeCounterBatteryCapacity(chargeCounter, capacity)
        if (estimatedCapacity > 0) {
            Logx.d("Estimated capacity: $estimatedCapacity mAh")
            return estimatedCapacity
        }

        // Last resort: return reasonable default
        // 최후 수단: 합리적인 기본값 반환
        Logx.w("getTotalCapacity failed; powerProfile=$powerProfileCapacity, chargeCounter=$chargeCounter μAh, capacity=$capacity%, " +
            "estimated=$estimatedCapacity -> default $BATTERY_ERROR_VALUE_DOUBLE mAh")
        return BATTERY_ERROR_VALUE_DOUBLE
    }

    /**
     * Gets battery capacity in mAh from PowerProfile using reflection.<br><br>
     * Reflection을 사용하여 PowerProfile에서 배터리 용량을 mAh 단위로 가져옵니다.<br>
     *
     * @return Battery capacity in mAh, or BATTERY_ERROR_VALUE_DOUBLE if unavailable.<br><br>
     *         배터리 용량 (mAh), 사용 불가능한 경우 BATTERY_ERROR_VALUE_DOUBLE.<br>
     */
    private fun getPowerProfileBatteryCapacity(): Double = safeCatch(BATTERY_ERROR_VALUE_DOUBLE) {
        val powerProfileCapacity = powerProfile.getBatteryCapacity()
        return if (powerProfileCapacity > 0) {
            powerProfileCapacity
        } else {
            BATTERY_ERROR_VALUE_DOUBLE
        }
    }

    /**
     * Estimates total battery capacity from current charge counter and battery percentage.<br>
     * This is a fallback method when PowerProfile is not available.<br><br>
     * 현재 충전 카운터와 배터리 백분율로부터 총 배터리 용량을 추정합니다.<br>
     * PowerProfile을 사용할 수 없을 때의 fallback 방법입니다.<br>
     *
     * Formula: Total Capacity = (Current Charge Counter / Current Percentage) * 100<br><br>
     * 공식: 총 용량 = (현재 충전량 / 현재 백분율) * 100<br>
     *
     * @param chargeCounter Current battery charge in microampere-hours (µAh).<br><br>
     *                      마이크로암페어시 단위의 현재 배터리 충전량 (µAh).<br>
     * @param capacity Current battery percentage (0-100).<br><br>
     *                 현재 배터리 백분율 (0-100).<br>
     * @return Estimated total battery capacity in mAh, or BATTERY_ERROR_VALUE_DOUBLE if estimation fails or values are invalid.<br><br>
     *         추정된 총 배터리 용량 (mAh), 추정 실패 또는 값이 유효하지 않은 경우 BATTERY_ERROR_VALUE_DOUBLE.<br>
     */
    private fun getChargeCounterBatteryCapacity(chargeCounter: Int, capacity: Int): Double = safeCatch(BATTERY_ERROR_VALUE_DOUBLE) {
        return if (chargeCounter > 0 && capacity > 5 && capacity <= 100) { // Avoid division by very small numbers
            // Calculate total capacity: (current_charge_µAh / current_percentage) * 100 / 1000 = mAh
            // 총 용량 계산: (현재_충전량_µAh / 현재_백분율) * 100 / 1000 = mAh
            val estimatedTotalCapacity = (chargeCounter.toDouble() / capacity.toDouble()) * 100.0 / 1000.0

            // Sanity check: reasonable mobile device battery capacity range
            // 정상성 검사: 합리적인 모바일 기기 배터리 용량 범위
            if (estimatedTotalCapacity in 1000.0..15000.0) {
                estimatedTotalCapacity
            } else {
                Logx.w("Estimated capacity out of range: $estimatedTotalCapacity mAh (expected 1000..15000)")
                BATTERY_ERROR_VALUE_DOUBLE
            }
        } else {
            Logx.w("Invalid values for estimation - chargeCounter: $chargeCounter µAh, capacity: $capacity%")
            BATTERY_ERROR_VALUE_DOUBLE
        }
    }
}
