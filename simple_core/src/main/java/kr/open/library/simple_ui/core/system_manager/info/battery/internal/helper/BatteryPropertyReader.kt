package kr.open.library.simple_ui.core.system_manager.info.battery.internal.helper

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.BatteryManager.EXTRA_TEMPERATURE
import android.os.BatteryManager.EXTRA_VOLTAGE
import android.os.Build
import androidx.annotation.RequiresApi
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.extensions.getBatteryManager
import kr.open.library.simple_ui.core.system_manager.info.battery.internal.helper.power.PowerProfile
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.BATTERY_ERROR_VALUE
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.BATTERY_ERROR_VALUE_BOOLEAN
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.BATTERY_ERROR_VALUE_DOUBLE
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.STR_BATTERY_HEALTH_COLD
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.STR_BATTERY_HEALTH_DEAD
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.STR_BATTERY_HEALTH_GOOD
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.STR_BATTERY_HEALTH_OVER_VOLTAGE
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.STR_BATTERY_HEALTH_UNKNOWN
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.STR_CHARGE_PLUG_AC
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.STR_CHARGE_PLUG_DOCK
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.STR_CHARGE_PLUG_NONE
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.STR_CHARGE_PLUG_UNKNOWN
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.STR_CHARGE_PLUG_USB
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.STR_CHARGE_PLUG_WIRELESS

/**
 * Internal helper that queries battery properties from the system services.<br><br>
 * 시스템 서비스에서 배터리 속성을 조회하는 내부 헬퍼입니다.<br>
 */
internal class BatteryPropertyReader(
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
     * Reads an integer extra from the given battery intent.<br><br>
     * 전달된 배터리 인텐트에서 정수 extra를 읽습니다.<br>
     *
     * @param batteryStatus Current battery status intent, if available.<br><br>
     *                      현재 배터리 상태 인텐트(사용 가능한 경우).<br>
     * @param type Intent extra key to read (e.g., BatteryManager.EXTRA_STATUS).<br><br>
     *             읽을 extra 키(예: BatteryManager.EXTRA_STATUS).<br>
     * @param defaultValue Value returned when the intent or extra is unavailable.<br><br>
     *                     인텐트/extra가 없을 때 반환할 기본값.<br>
     * @return The resolved value from the given intent, or defaultValue if missing.<br><br>
     *         인텐트에서 읽은 값, 없으면 defaultValue.<br>
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
     * Checks if the device is charging via USB.<br><br>
     * 기기가 USB를 통해 충전 중인지 확인합니다.<br>
     *
     * @return `true` if charging via USB, `false` otherwise.<br><br>
     *         USB로 충전 중이면 `true`, 아니면 `false`.<br>
     */
    public fun isChargingUsb(chargePlugType: Int): Boolean = chargePlugType != BATTERY_ERROR_VALUE &&
        (chargePlugType and BatteryManager.BATTERY_PLUGGED_USB) != 0

    /**
     * Checks if the device is charging via AC.<br><br>
     * 기기가 AC를 통해 충전 중인지 확인합니다.<br>
     *
     * @return `true` if charging via AC, `false` otherwise.<br><br>
     *         AC로 충전 중이면 `true`, 아니면 `false`.<br>
     */
    public fun isChargingAc(chargePlugType: Int): Boolean = chargePlugType != BATTERY_ERROR_VALUE &&
        (chargePlugType and BatteryManager.BATTERY_PLUGGED_AC) != 0

    /**
     * Checks if the device is charging wirelessly.<br><br>
     * 기기가 무선으로 충전 중인지 확인합니다.<br>
     *
     * @return `true` if charging wirelessly, `false` otherwise.<br><br>
     *         무선 충전 중이면 `true`, 아니면 `false`.<br>
     */
    public fun isChargingWireless(chargePlugType: Int): Boolean = chargePlugType != BATTERY_ERROR_VALUE &&
        (chargePlugType and BatteryManager.BATTERY_PLUGGED_WIRELESS) != 0

    /**
     * Checks if the device is charging via dock (API 33+).<br><br>
     * 기기가 독을 통해 충전 중인지 확인합니다 (API 33+).<br>
     *
     * @return `true` if charging via dock, `false` otherwise.<br><br>
     *         독으로 충전 중이면 `true`, 아니면 `false`.<br>
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    public fun isChargingDock(chargePlugType: Int): Boolean = chargePlugType != BATTERY_ERROR_VALUE &&
        (chargePlugType and BatteryManager.BATTERY_PLUGGED_DOCK) != 0

    /**
     * Returns a list of readable labels for currently active power sources.<br>
     * Multiple power sources can be active simultaneously as the value is a BitMask.<br><br>
     * 현재 활성화된 전원 타입을 읽기 쉬운 문자열 리스트로 반환합니다.<br>
     * 값이 BitMask이므로 여러 전원이 동시에 활성화될 수 있습니다.<br>
     *
     * @return List of human-readable power source strings (e.g., ["USB"], ["AC", "Wireless"], etc.).<br>
     *         Returns ["Unknown"] on error, ["None"] if not charging.<br><br>
     *         사람이 읽기 쉬운 전원 타입 문자열 리스트 (예: ["USB"], ["AC", "Wireless"] 등).<br>
     *         에러 시 ["Unknown"], 충전 중이 아니면 ["None"]을 반환합니다.<br>
     */
    public fun getChargePlugList(chargePlugType: Int): List<String> = safeCatch(listOf(STR_CHARGE_PLUG_UNKNOWN)) {
        if (chargePlugType == BATTERY_ERROR_VALUE) return listOf(STR_CHARGE_PLUG_UNKNOWN)
        if (chargePlugType == 0) return listOf(STR_CHARGE_PLUG_NONE)

        val types = mutableListOf<String>()

        if (isChargingUsb(chargePlugType)) types.add(STR_CHARGE_PLUG_USB)
        if (isChargingAc(chargePlugType)) types.add(STR_CHARGE_PLUG_AC)
        if (isChargingWireless(chargePlugType)) types.add(STR_CHARGE_PLUG_WIRELESS)
        checkSdkVersion(Build.VERSION_CODES.TIRAMISU) {
            if (isChargingDock(chargePlugType)) types.add(STR_CHARGE_PLUG_DOCK)
        }

        return if (types.isEmpty()) {
            listOf(STR_CHARGE_PLUG_UNKNOWN)
        } else {
            types
        }
    }

    /**
     * Gets the battery temperature in Celsius.<br>
     * Android returns temperature in tenths of a degree Celsius, so we divide by 10.<br>
     * Returns [BATTERY_ERROR_VALUE_DOUBLE] if temperature is unavailable or out of valid range (-40°C to 120°C).<br><br>
     * 배터리 온도를 섭씨로 가져옵니다.<br>
     * Android는 온도를 섭씨 1/10도 단위로 반환하므로 10으로 나눕니다.<br>
     * 온도를 사용할 수 없거나 유효 범위(-40°C ~ 120°C)를 벗어나면 [BATTERY_ERROR_VALUE_DOUBLE]을 반환합니다.<br>
     *
     * @return Battery temperature in Celsius (°C), or [BATTERY_ERROR_VALUE_DOUBLE] if unavailable or invalid.<br><br>
     *         배터리 온도 (섭씨), 사용할 수 없거나 유효하지 않은 경우 [BATTERY_ERROR_VALUE_DOUBLE].<br>
     *
     * Example: Android returns 350 → 35.0°C<br><br>
     * 예시: Android가 350을 반환 → 35.0°C<br>
     */
    public fun getTemperature(batteryStatus: Intent?): Double {
        val raw = batteryStatus?.let {
            if (it.hasExtra(EXTRA_TEMPERATURE)) it.getIntExtra(EXTRA_TEMPERATURE, BATTERY_ERROR_VALUE) else null
        }

        if (raw == null || raw == BATTERY_ERROR_VALUE) return BATTERY_ERROR_VALUE_DOUBLE

        val temp = raw / 10.0
        return if (temp in -40.0..120.0) temp else BATTERY_ERROR_VALUE_DOUBLE
    }

    /**
     * Checks if a battery is present in the device.<br><br>
     * 기기에 배터리가 장착되어 있는지 확인합니다.<br>
     *
     * @return `true` if battery is present, `false` if not present, `null` if status is unavailable.<br><br>
     *         배터리가 장착되어 있으면 `true`, 장착되어 있지 않으면 `false`, 상태를 알 수 없으면 `null`.<br>
     */
    public fun getPresent(batteryStatus: Intent?): Boolean? {
        batteryStatus?.let {
            if (it.hasExtra(BatteryManager.EXTRA_PRESENT)) {
                return it.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false)
            }
        }
        return BATTERY_ERROR_VALUE_BOOLEAN
    }

    /**
     * get battery health status.<br><br>
     * 배터리 건강 상태를 반환합니다.<br>
     *
     * @return Battery health status.<br><br>
     *         배터리 건강 상태.<br>
     * @see BatteryManager.BATTERY_HEALTH_GOOD
     * @see BatteryManager.BATTERY_HEALTH_COLD
     * @see BatteryManager.BATTERY_HEALTH_DEAD
     * @see BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE
     * @see BatteryManager.BATTERY_HEALTH_UNKNOWN
     */
    public fun getHealth(batteryStatus: Intent?): Int = getStatus(batteryStatus, BatteryManager.EXTRA_HEALTH)

    /**
     * Converts a battery health constant to text.<br><br>
     * 배터리 건강 상태 상수를 문자열로 변환합니다.<br>
     *
     * @param healthType `BATTERY_HEALTH_*` constant.<br><br>
     *                   `BATTERY_HEALTH_*` 상수입니다.<br>
     * @return Human-readable health string.<br><br>
     *         사람이 읽기 쉬운 상태 문자열입니다.<br>
     */
    public fun getHealthStr(healthType: Int): String = when (healthType) {
        BatteryManager.BATTERY_HEALTH_GOOD -> STR_BATTERY_HEALTH_GOOD
        BatteryManager.BATTERY_HEALTH_COLD -> STR_BATTERY_HEALTH_COLD
        BatteryManager.BATTERY_HEALTH_DEAD -> STR_BATTERY_HEALTH_DEAD
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> STR_BATTERY_HEALTH_OVER_VOLTAGE
        else -> STR_BATTERY_HEALTH_UNKNOWN
    }

    /**
     * get battery voltage.<br><br>
     * 배터리 전압을 반환합니다.<br>
     *
     * @return Battery voltage in volts (ex 3.5). Returns [BATTERY_ERROR_VALUE_DOUBLE] on error.<br><br>
     *         볼트 단위의 배터리 전압 (예: 3.5). 오류 시 [BATTERY_ERROR_VALUE_DOUBLE] 반환.<br>
     */
    public fun getVoltage(batteryStatus: Intent?): Double {
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
     * @return Battery technology (ex Li-ion). Returns null on error.<br><br>
     *         배터리 기술 방식 (예: Li-ion). 오류 시 null 반환.<br>
     */
    public fun getTechnology(batteryStatus: Intent?): String? {
        batteryStatus?.let {
            val technology = it.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
            if (technology != null) {
                return technology
            }
        }
        return null
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
    public fun getChargeStatus(batteryStatus: Intent?): Int {
        val res = getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
        return if (res == BATTERY_ERROR_VALUE) {
            getStatus(batteryStatus, BatteryManager.EXTRA_STATUS, BATTERY_ERROR_VALUE)
        } else {
            res
        }
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
        Logx.w(
            "getTotalCapacity failed; powerProfile=$powerProfileCapacity, chargeCounter=$chargeCounter μAh, capacity=$capacity%, " +
                "estimated=$estimatedCapacity -> default $BATTERY_ERROR_VALUE_DOUBLE mAh"
        )
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
