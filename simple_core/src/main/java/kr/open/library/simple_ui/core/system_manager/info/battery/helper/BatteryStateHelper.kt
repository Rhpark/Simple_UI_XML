package kr.open.library.simple_ui.core.system_manager.info.battery.helper

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.BatteryManager.EXTRA_TEMPERATURE
import android.os.BatteryManager.EXTRA_VOLTAGE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private suspend fun sendFlow(event: BatteryStateEvent) {
        msfUpdate.emit(event)
    }

    /**
     * Sets up reactive flows for all battery data updates.<br>
     * This method should be called after setting the coroutineScope.<br><br>
     * 모든 배터리 데이터 업데이트를 위한 반응형 플로우를 설정합니다.<br>
     * 이 메서드는 coroutineScope를 설정한 후에 호출해야 합니다.<br>
     */
    public fun setupDataFlows(coroutineScope: CoroutineScope) {
        coroutineScope.let { scope ->
            scope.launch { msfCapacity.collect { sendFlow(BatteryStateEvent.OnCapacity(it)) } }
            scope.launch { msfCurrentAmpere.collect { sendFlow(BatteryStateEvent.OnCurrentAmpere(it)) } }
            scope.launch { msfCurrentAverageAmpere.collect { sendFlow(BatteryStateEvent.OnCurrentAverageAmpere(it)) } }
            scope.launch { msfChargeStatus.collect { sendFlow(BatteryStateEvent.OnChargeStatus(it)) } }
            scope.launch { msfChargeCounter.collect { sendFlow(BatteryStateEvent.OnChargeCounter(it)) } }
            scope.launch { msfChargePlug.collect { sendFlow(BatteryStateEvent.OnChargePlug(it)) } }
            scope.launch { msfEnergyCounter.collect { sendFlow(BatteryStateEvent.OnEnergyCounter(it)) } }
            scope.launch { msfHealth.collect { sendFlow(BatteryStateEvent.OnHealth(it)) } }
            scope.launch { msfPresent.collect { sendFlow(BatteryStateEvent.OnPresent(it)) } }
            scope.launch { msfTemperature.collect { sendFlow(BatteryStateEvent.OnTemperature(it)) } }
            scope.launch { msfVoltage.collect { sendFlow(BatteryStateEvent.OnVoltage(it)) } }
        }
    }

    /**
     * Refreshes cached battery metrics and emits events.<br><br>
     * 캐시된 배터리 지표를 갱신하고 이벤트를 방출합니다.<br>
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
        present: Boolean,
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
     * Gets the instantaneous battery current in microamperes.<br>
     * Positive values indicate charging, negative values indicate discharging.<br><br>
     * 순간 배터리 전류를 마이크로암페어 단위로 반환합니다.<br>
     * 양수 값은 충전 소스에서 배터리로 들어오는 순 전류, 음수 값은 배터리에서 방전되는 순 전류입니다.<br>
     *
     * @return The instantaneous battery current in microamperes.<br><br>
     *         순간 배터리 전류 (마이크로암페어).<br>
     */
    public fun getCurrentAmpere(): Int = safeCatch(BATTERY_ERROR_VALUE) {
        bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
    }

    /**
     * Average battery current in microamperes, as an integer.<br><br>
     * 평균 배터리 전류를 마이크로암페어 단위로 반환합니다.<br>
     *
     * Positive values indicate net current entering the battery from a charge source,<br>
     * negative values indicate net current discharging from the battery.<br><br>
     * 양수 값은 충전 소스에서 배터리로 들어오는 순 전류, 음수 값은 배터리에서 방전되는 순 전류입니다.<br>
     *
     *
     * @return The average battery current in microamperes (µA).<br><br>
     *         평균 배터리 전류 (마이크로암페어, µA).<br>
     */
    public fun getCurrentAverageAmpere(): Int = safeCatch(BATTERY_ERROR_VALUE) {
        bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
    }

    /**
     * Reads a battery extra value from the given intent, falling back to a fresh system query when needed.<br><br>
     * 배터리 인텐트에서 값을 읽고, 없을 경우 시스템 쿼리로 폴백합니다.<br>
     *
     * @param batteryStatus Current battery status intent, if available.<br><br>
     *                      현재 배터리 상태 인텐트(가능한 경우).<br>
     * @param type Intent extra key to read (e.g., BatteryManager.EXTRA_STATUS).<br><br>
     *             읽을 인텐트 extra 키 (예: BatteryManager.EXTRA_STATUS).<br>
     * @param defaultValue Default value returned when the extra is unavailable.<br><br>
     *                     값이 없을 때 반환할 기본값.<br>
     * @return The resolved value from intent or fallback query.<br><br>
     *         인텐트 또는 폴백 조회로 얻은 값.<br>
     */
    private fun getStatus(batteryStatus: Intent?, type: String, defaultValue: Int = BATTERY_ERROR_VALUE): Int = safeCatch(defaultValue) {
        // Try to get charge status from current batteryStatus first
        // 먼저 현재 batteryStatus에서 충전 상태를 가져오기 시도
        var chargeStatus = defaultValue
        batteryStatus?.let {
            chargeStatus = it.getIntExtra(type, defaultValue)
        }

        // If batteryStatus is null or doesn't have charge status, get fresh battery intent
        // batteryStatus가 null이거나 충전 상태가 없는 경우, 새로운 배터리 intent를 가져옴
        if (chargeStatus != defaultValue) {
            return chargeStatus
        }
        context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))?.let {
            chargeStatus = it.getIntExtra(type, defaultValue)
        }
        return chargeStatus
    }

    /**
     * Battery charge status, from a BATTERY_STATUS_* value.<br><br>
     * 배터리 충전 상태를 반환합니다.<br>
     *
     * @return The battery charge status.<br><br>
     *         배터리 충전 상태.<br>
     * @see BatteryManager.BATTERY_STATUS_CHARGING
     * @see BatteryManager.BATTERY_STATUS_FULL
     * @see BatteryManager.BATTERY_STATUS_DISCHARGING
     * @see BatteryManager.BATTERY_STATUS_NOT_CHARGING
     * @see BatteryManager.BATTERY_STATUS_UNKNOWN
     */
    public fun getChargeStatus(batteryStatus: Intent? = null): Int = safeCatch(BATTERY_ERROR_VALUE) {
        val res = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
        return if (res == BATTERY_ERROR_VALUE) {
            getStatus(batteryStatus, BatteryManager.EXTRA_STATUS, BATTERY_ERROR_VALUE)
        } else {
            res
        }
    }

    /**
     * Gets an integer battery property from `BatteryManager`.<br><br>
     * `BatteryManager`에서 정수형 배터리 속성을 가져옵니다.<br>
     */
    private fun getIntProperty(batteryType: Int) = bm.getIntProperty(batteryType)

    /**
     * Gets a long battery property from `BatteryManager`.<br><br>
     * `BatteryManager`에서 Long 타입 배터리 속성을 가져옵니다.<br>
     */
    private fun getLongProperty(batteryType: Int) = bm.getLongProperty(batteryType)

    /**
     * Gets the remaining battery capacity as a percentage (0-100).<br><br>
     * 남은 배터리 용량을 백분율(0-100)로 가져옵니다.<br>
     *
     * @return The remaining battery capacity as a percentage.<br><br>
     *         남은 배터리 용량 (백분율).<br>
     */
    public fun getCapacity(): Int = safeCatch(BATTERY_ERROR_VALUE) {
        getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    /**
     * Battery capacity in microampere-hours, as an integer.<br><br>
     * 배터리 용량을 마이크로암페어시 단위로 반환합니다.<br>
     *
     * @return The battery capacity in microampere-hours.<br><br>
     *         배터리 용량 (마이크로암페어시).<br>
     */
    public fun getChargeCounter(): Int = safeCatch(BATTERY_ERROR_VALUE) {
        getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
    }

    /**
     * Returns the battery remaining energy in nanowatt-hours.<br><br>
     * 배터리 잔여 에너지를 나노와트시 단위로 반환합니다.<br>
     *
     * Warning!!, Values may not be accurate.<br><br>
     * 경고!!, 값이 정확하지 않을 수 있습니다.<br>
     *
     * Error value may be Long.MIN_VALUE.<br><br>
     * 오류 값은 Long.MIN_VALUE일 수 있습니다.<br>
     *
     * @return The battery remaining energy in nanowatt-hours.<br><br>
     *         배터리 잔여 에너지 (나노와트시).<br>
     */
    public fun getEnergyCounter(): Long = safeCatch(BATTERY_ERROR_VALUE_LONG) {
        getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
    }

    /**
     * get charge plug type.<br><br>
     * 충전 플러그 타입을 반환합니다.<br>
     *
     * @param batteryStatus Intent of battery status.<br><br>
     *                      배터리 상태 인텐트<br>
     * @return Charge plug type.<br><br>
     *         충전 플러그 타입.(Error  = BATTERY_ERROR_VALUE(-999))<br>
     * @see BatteryManager.BATTERY_PLUGGED_USB
     * @see BatteryManager.BATTERY_PLUGGED_AC
     * @see BatteryManager.BATTERY_PLUGGED_DOCK
     * @see BatteryManager.BATTERY_PLUGGED_WIRELESS
     */
    public fun getChargePlug(batteryStatus: Intent?): Int = getStatus(batteryStatus, BatteryManager.EXTRA_PLUGGED, BATTERY_ERROR_VALUE)

    /**
     * Gets the battery temperature in Celsius.<br>
     * Android returns temperature in tenths of a degree Celsius, so we divide by 10.<br><br>
     * 배터리 온도를 섭씨로 가져옵니다.<br>
     * Android는 온도를 섭씨 1/10도 단위로 반환하므로 10으로 나눕니다.<br>
     *
     * @return Battery temperature in Celsius (°C), or [BATTERY_ERROR_VALUE_DOUBLE] if unavailable.<br><br>
     *         배터리 온도 (섭씨), 사용할 수 없는 경우 [BATTERY_ERROR_VALUE_DOUBLE].<br>
     *
     * Example: Android returns 350 → 35.0°C<br><br>
     * 예시: Android가 350을 반환 → 35.0°C<br>
     *
     * Note: If you see very negative values like -214748364.8°C, it means temperature is unavailable.<br><br>
     * 참고: -214748364.8°C 같은 매우 낮은 음수가 보이면 온도를 사용할 수 없다는 뜻입니다.<br>
     */
    public fun getTemperature(batteryStatus: Intent?): Double = safeCatch(BATTERY_ERROR_VALUE_DOUBLE) {
        val rawTemperature = getStatus(batteryStatus, EXTRA_TEMPERATURE, BATTERY_ERROR_VALUE)

        return if (rawTemperature == BATTERY_ERROR_VALUE) {
            BATTERY_ERROR_VALUE_DOUBLE // Use a reasonable error value instead of Integer.MIN_VALUE / 10
        } else {
            val convertedTemp = rawTemperature.toDouble() / 10.0
            // Sanity check: reasonable battery temperature range (-40°C to 100°C)
            // 정상성 검사: 합리적인 배터리 온도 범위 (-40°C ~ 100°C)
            if (convertedTemp in -40.0..100.0) {
                convertedTemp
            } else {
                BATTERY_ERROR_VALUE_DOUBLE // Invalid temperature value
            }
        }
    }

    /**
     * Checks if a battery is present in the device.<br><br>
     * 기기에 배터리가 장착되어 있는지 확인합니다.<br>
     */
    public fun getPresent(batteryStatus: Intent?): Boolean = safeCatch(BATTERY_ERROR_VALUE_BOOLEAN) {
        // Try to get present status from current batteryStatus first
        // 먼저 현재 batteryStatus에서 배터리 존재 상태를 가져오기 시도
        batteryStatus?.let {
            return it.getBooleanExtra(BatteryManager.EXTRA_PRESENT, BATTERY_ERROR_VALUE_BOOLEAN)
        }

        context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))?.let {
            return it.getBooleanExtra(BatteryManager.EXTRA_PRESENT, BATTERY_ERROR_VALUE_BOOLEAN)
        }

        return BATTERY_ERROR_VALUE_BOOLEAN
    }

    /**
     * get battery health status.<br><br>
     * 배터리 건강 상태를 반환합니다.<br>
     *
     * @param batteryStatus Intent of battery status.<br><br>
     *                      배터리 상태 인텐트<br>
     * @return Battery health status.<br><br>
     *         배터리 건강 상태(Error  = BATTERY_ERROR_VALUE(-999).<br>
     * @see BatteryManager.BATTERY_HEALTH_GOOD
     * @see BatteryManager.BATTERY_HEALTH_COLD
     * @see BatteryManager.BATTERY_HEALTH_DEAD
     * @see BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE
     * @see BatteryManager.BATTERY_HEALTH_UNKNOWN
     */
    public fun getHealth(batteryStatus: Intent?): Int = getStatus(batteryStatus, BatteryManager.EXTRA_HEALTH)

    /**
     * get battery voltage.<br><br>
     * 배터리 전압을 반환합니다.<br>
     *
     * @param batteryStatus Intent of battery status.<br><br>
     *                      배터리 상태 인텐트<br>
     * @return Battery voltage in volts (ex 3.5). Returns [BATTERY_ERROR_VALUE_DOUBLE] on error.<br><br>
     *         볼트 단위의 배터리 전압 (예: 3.5). 오류 시 [BATTERY_ERROR_VALUE_DOUBLE] 반환.<br>
     */
    public fun getVoltage(batteryStatus: Intent?): Double = safeCatch(BATTERY_ERROR_VALUE_DOUBLE) {
        val voltage = getStatus(batteryStatus, EXTRA_VOLTAGE, BATTERY_ERROR_VALUE)
        if (voltage == BATTERY_ERROR_VALUE) {
            return BATTERY_ERROR_VALUE_DOUBLE
        }
        return voltage.toDouble() / 1000
    }

    /**
     * get battery technology.<br><br>
     * 배터리 기술 방식을 반환합니다.<br>
     *
     * @param batteryStatus Intent of battery status.<br><br>
     *                      배터리 상태 인텐트<br>
     * @return Battery technology (ex Li-ion). Returns null on error.<br><br>
     *         배터리 기술 방식 (예: Li-ion). 오류 시 null 반환.<br>
     */
    public fun getTechnology(batteryStatus: Intent?): String? = safeCatch(null) {
        // Try to get technology from current batteryStatus first
        // 먼저 현재 batteryStatus에서 배터리 기술을 가져오기 시도
        batteryStatus?.let {
            val technology = it.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
            if (technology != null) {
                return technology
            }
        }
        context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))?.let {
            return it.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
        }
        return null
    }

    /**
     * Gets the total battery capacity (rated capacity) in milliampere-hours (mAh).<br>
     * Uses multiple fallback methods for better compatibility across Android versions.<br><br>
     * 배터리의 총 용량(정격 용량)을 밀리암페어시(mAh) 단위로 가져옵니다.<br>
     * Android 버전 간 호환성을 위해 여러 fallback 방법을 사용합니다.<br>
     *
     * @return The total battery capacity in mAh, or default value if unavailable.<br><br>
     *         총 배터리 용량(mAh), 사용할 수 없는 경우 기본값.<br>
     */
    public fun getTotalCapacity(): Double = safeCatch(BATTERY_ERROR_VALUE_DOUBLE) {
        // Primary method: Use PowerProfile
        // 주요 방법: PowerProfile 사용
        val powerProfileCapacity = powerProfile.getBatteryCapacity()
        if (powerProfileCapacity > 0) {
            return powerProfileCapacity
        }

        // Fallback 1: Try to estimate from charge counter (API 21+)
        // Fallback 1: 충전 카운터로부터 추정 (API 21+)
        val estimatedCapacity = getEstimatedCapacityFromChargeCounter()
        if (estimatedCapacity > 0) {
            return estimatedCapacity
        }

        // Last resort: return reasonable default
        // 최후 수단: 합리적인 기본값 반환
        Logx.w("Unable to determine battery capacity, using default: $BATTERY_ERROR_VALUE_DOUBLE mAh")
        return BATTERY_ERROR_VALUE_DOUBLE
    }

    /**
     * Estimates total battery capacity from current charge counter and battery percentage.<br>
     * This is a fallback method when PowerProfile is not available.<br><br>
     * 현재 충전 카운터와 배터리 백분율로부터 총 배터리 용량을 추정합니다.<br>
     * PowerProfile을 사용할 수 없을 때의 fallback 방법입니다.<br>
     *
     * Formula: Total Capacity = (Current Charge Counter / Current Percentage) * 100<br><br>
     * 공식: 총 용량 = (현재 충전량 / 현재 백분율) * 100<br>
     */
    private fun getEstimatedCapacityFromChargeCounter(): Double = safeCatch(BATTERY_ERROR_VALUE_DOUBLE) {
        val chargeCounter = getChargeCounter() // Current charge in µAh
        val capacity = getCapacity() // Current percentage (0-100)

        return if (chargeCounter > 0 && capacity > 5 && capacity <= 100) { // Avoid division by very small numbers
            // Calculate total capacity: (current_charge_µAh / current_percentage) * 100 / 1000 = mAh
            // 총 용량 계산: (현재_충전량_µAh / 현재_백분율) * 100 / 1000 = mAh
            val estimatedTotalCapacity = (chargeCounter.toDouble() / capacity.toDouble()) * 100.0 / 1000.0

            // Sanity check: reasonable mobile device battery capacity range
            // 정상성 검사: 합리적인 모바일 기기 배터리 용량 범위
            if (estimatedTotalCapacity in 1000.0..10000.0) {
                estimatedTotalCapacity
            } else {
                Logx.w("Estimated capacity out of range: $estimatedTotalCapacity mAh")
                BATTERY_ERROR_VALUE_DOUBLE
            }
        } else {
            Logx.w("Invalid values for estimation - chargeCounter: $chargeCounter µAh, capacity: $capacity%")
            BATTERY_ERROR_VALUE_DOUBLE
        }
    }
}
