package kr.open.library.simple_ui.core.system_manager.info.battery

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.BATTERY_ERROR_VALUE
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.BATTERY_ERROR_VALUE_BOOLEAN
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.BATTERY_ERROR_VALUE_DOUBLE
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.BATTERY_ERROR_VALUE_LONG
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.DEFAULT_UPDATE_CYCLE_TIME
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.STR_CHARGE_PLUG_UNKNOWN
import kr.open.library.simple_ui.core.system_manager.info.battery.internal.helper.BatteryPropertyReader
import kr.open.library.simple_ui.core.system_manager.info.battery.internal.helper.BatteryStateEmitter
import kr.open.library.simple_ui.core.system_manager.info.battery.internal.helper.BatteryStateReceiver
import kr.open.library.simple_ui.core.system_manager.info.battery.internal.model.BatteryStateData

/**
 * Provides information about the battery state of an Android device.<br><br>
 * Android 기기의 배터리 상태 정보를 제공합니다.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Android's BatteryManager provides raw values but requires manual BroadcastReceiver setup and state management.<br>
 * - This class wraps the complexity into a simple reactive API with automatic change detection and Flow-based observation.<br>
 * - Eliminates boilerplate code for battery monitoring in apps.<br><br>
 * - Android의 BatteryManager는 원시 값을 제공하지만 수동 BroadcastReceiver 설정과 상태 관리가 필요합니다.<br>
 * - 이 클래스는 복잡성을 자동 변경 감지와 Flow 기반 관찰이 가능한 간단한 반응형 API로 감쌉니다.<br>
 * - 앱에서 배터리 모니터링을 위한 보일러플레이트 코드를 제거합니다.<br>
 *
 * **Design decisions / 설계 결정 이유:**<br>
 * - Uses MutableStateFlow for individual metrics to enable automatic duplicate filtering (distinctUntilChanged behavior).<br>
 * - Uses SharedFlow for unified event stream (sfUpdate) with buffering to prevent event loss during simultaneous updates.<br>
 * - Combines BroadcastReceiver (real-time events) + periodic polling (continuously changing values) for comprehensive monitoring.<br><br>
 * - 개별 지표는 MutableStateFlow를 사용하여 자동 중복 필터링(distinctUntilChanged 동작)을 활성화합니다.<br>
 * - 통합 이벤트 스트림(sfUpdate)은 SharedFlow를 사용하며 동시 업데이트 시 이벤트 손실을 방지하기 위해 버퍼링합니다.<br>
 * - BroadcastReceiver(실시간 이벤트) + 주기적 확인(지속적으로 변하는 값)을 결합하여 포괄적인 모니터링을 제공합니다.<br>
 *
 * **Permission notice / 권한 안내:**<br>
 * - `android.permission.BATTERY_STATS` is system/preloaded only.<br>
 * - This library does not enforce BATTERY_STATS (runtime/special-only validation policy).<br>
 * - Values may be limited depending on device/OS support; unsupported fields return default values.<br>
 * - Some values may be richer when BATTERY_STATS is available on system/preloaded apps.<br><br>
 * - `android.permission.BATTERY_STATS`는 시스템/프리로드 전용 권한입니다.<br>
 * - 본 라이브러리는 BATTERY_STATS를 강제 검증하지 않습니다(런타임/특수 권한만 검증).<br>
 * - 기기/OS 지원 범위에 따라 일부 값이 제한될 수 있으며, 미지원 값은 기본값을 반환합니다.<br>
 * - BATTERY_STATS가 가능한 환경(시스템/프리로드 앱)에서는 일부 값이 더 풍부할 수 있습니다.<br>
 *
 * **Usage / 사용법:**<br>
 * 1. Call `registerStart(coroutineScope)` before collecting flows to start receiver and periodic updates.<br>
 * 2. Call `destroy()` upon complete shutdown to release resources.<br><br>
 * 1. 플로우 수집 전에 `registerStart(coroutineScope)`를 호출하여 리시버와 주기 업데이트를 시작하세요.<br>
 * 2. 완전 종료 시 `destroy()`를 호출하여 리소스를 해제하세요.<br>
 *
 * @param context The application context.<br><br>
 *                애플리케이션 컨텍스트.<br>
 */
public open class BatteryStateInfo(
    context: Context
) : BaseSystemService(
        context, listOf(android.Manifest.permission.BATTERY_STATS)
    ) {
    /**
     * Helper that queries battery properties from BatteryManager and manages StateFlow caching.<br>
     * Provides battery property access methods and maintains reactive flows for battery metrics.<br><br>
     * BatteryManager에서 배터리 속성을 조회하고 StateFlow 캐싱을 관리하는 헬퍼입니다.<br>
     * 배터리 속성 접근 메서드를 제공하고 배터리 메트릭에 대한 반응형 플로우를 유지합니다.<br>
     */
    private val batteryPropertyReader = BatteryPropertyReader(context)

    /**
     * Manages event emission to SharedFlow and caches battery metrics in StateFlows.<br><br>
     * SharedFlow로 이벤트를 발행하고 StateFlow에 배터리 메트릭을 캐싱합니다.<br>
     */
    private val sender = BatteryStateEmitter()

    /**
     * BroadcastReceiver helper that listens to ACTION_BATTERY_CHANGED and periodically updates battery metrics.<br>
     * On each update, it invokes the callback lambda which fetches current battery values and updates batteryStateHelper.<br><br>
     * ACTION_BATTERY_CHANGED를 수신하고 주기적으로 배터리 메트릭을 업데이트하는 BroadcastReceiver 헬퍼입니다.<br>
     * 각 업데이트 시 콜백 람다를 호출하여 현재 배터리 값을 가져오고 batteryStateHelper를 업데이트합니다.<br>
     */
    private val receiver = BatteryStateReceiver(context) {
        sender.updateBatteryInfo(
            BatteryStateData(
                capacity = getCapacity(),
                chargeCounter = getChargeCounter(),
                chargeStatus = getChargeStatus(),
                chargePlug = getChargePlug(),
                currentAmpere = getCurrentAmpere(),
                currentAverageAmpere = getCurrentAverageAmpere(),
                energyCounter = getEnergyCounter(),
                health = getHealth(),
                present = getPresent(),
                temperature = getTemperature(),
                voltage = getVoltage()
            )
        )
    }

    /**
     * Event-only stream for battery changes.<br><br>
     * 배터리 변경을 이벤트 단위로 전달하는 스트림입니다.<br>
     *
     * This is NOT a snapshot of all metrics at the same time.<br><br>
     * 모든 값을 동일 시점 스냅샷으로 보장하지 않습니다.<br>
     *
     * Ordering across different metrics is not guaranteed.<br><br>
     * 서로 다른 지표 간의 순서 보장은 없습니다.<br>
     *
     * Depending on the buffer policy, events may occasionally be dropped.<br><br>
     * 버퍼 정책에 따라 간혹 이벤트가 드롭될 수 있습니다.<br>
     *
     * Unsupported or unavailable metrics may emit no events until a valid value becomes available.<br><br>
     * 미지원/미사용 가능 값은 유효한 값이 나오기 전까지 이벤트가 발생하지 않을 수 있습니다.<br>
     */
    public val sfUpdate: SharedFlow<BatteryStateEvent> = sender.getSfUpdate()

    /**
     * Starts listening to battery broadcasts and periodic updates with the given scope.<br><br>
     * 지정한 코루틴 스코프로 배터리 브로드캐스트 등록과 주기적 업데이트를 시작합니다.<br>
     *
     * @param coroutine Coroutine scope used for collecting and emitting updates.<br><br>
     *                  업데이트 수집·발행에 사용할 코루틴 스코프입니다.<br>
     * @param updateCycleTime Interval (in milliseconds) to periodically check battery values and emit updates when changes are detected.<br>
     *                        BroadcastReceiver provides real-time updates for most battery events,<br>
     *                        but some values (e.g., Current Ampere, Current Average Ampere) continuously change without triggering broadcasts.<br>
     *                        - 2000ms (default): Recommended for most cases - fast updates, moderate battery usage.<br>
     *                        - 10000ms: Slower updates, lower battery consumption.<br>
     *                        - 60000ms: Very slow updates, minimal battery impact.<br><br>
     *                        일정 간격(밀리초)으로 배터리 값을 확인하고 변경 시 업데이트를 발행하는 주기입니다.<br>
     *                        BroadcastReceiver가 대부분의 배터리 이벤트를 실시간으로 제공하지만,<br>
     *                        일부 값(예: Current Ampere, Current Average Ampere)은 브로드캐스트 없이 지속적으로 변합니다.<br>
     *                        - 2000ms (기본값): 대부분의 경우 권장 - 빠른 업데이트, 적당한 배터리 사용.<br>
     *                        - 10000ms: 느린 업데이트, 낮은 배터리 소비.<br>
     *                        - 60000ms: 매우 느린 업데이트, 최소 배터리 영향.<br>
     * @throws Exception When receiver registration or update start fails.<br><br>
     *                   리시버 등록 또는 업데이트 시작에 실패하면 예외를 발생시킵니다.<br>
     */
    public fun registerStart(coroutine: CoroutineScope, updateCycleTime: Long = DEFAULT_UPDATE_CYCLE_TIME): Boolean {
        val isRegister = registerReceiver()

        if (!isRegister) {
            Logx.e("BatteryStateInfo: Receiver not registered!!.")
            return false
        }

        val result = tryCatchSystemManager(false) {
            receiver.updateStart(coroutine, updateCycleTime) {
                receiver.getCoroutineScope()?.let { sender.setupDataFlows(it) }
            }
        }

        if (!result) {
            unRegister()
            Logx.e("BatteryStateInfo: updateStart() failed!!.")
        }
        return result
    }

    /**
     * Registers a broadcast receiver for battery-related events.<br>
     * Call this method before starting updates.<br><br>
     * 배터리 관련 이벤트를 위한 브로드캐스트 리시버를 등록합니다.<br>
     * 업데이트를 시작하기 전에 이 메서드를 호출하세요.<br>
     *
     * @return `true` if registration succeeded, `false` otherwise.<br><br>
     *         등록 성공 시 `true`, 실패 시 `false`.<br>
     */
    private fun registerReceiver(): Boolean = tryCatchSystemManager(false) {
        return receiver.registerReceiver()
    }

    /**
     * Stops updates and unregisters the battery receiver.<br><br>
     * 업데이트를 중단하고 배터리 리시버를 해제합니다.<br>
     */
    public fun unRegister() {
        receiver.unRegisterReceiver()
        receiver.updateStop()
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
    public fun getCurrentAmpere(): Int = tryCatchSystemManager(BATTERY_ERROR_VALUE) {
        batteryPropertyReader.bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
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
    public fun getCurrentAverageAmpere(): Int = tryCatchSystemManager(BATTERY_ERROR_VALUE) {
        batteryPropertyReader.bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
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
    public fun getChargeStatus(): Int = tryCatchSystemManager(BATTERY_ERROR_VALUE) {
        return batteryPropertyReader.getChargeStatus(getBatteryStatus())
    }

    /**
     * Checks if the battery is currently charging.<br><br>
     * 배터리가 현재 충전 중인지 확인합니다.<br>
     *
     * @return `true` if battery is charging, `false` otherwise.<br><br>
     *         배터리 충전 중이면 `true`, 아니면 `false`.<br>
     */
    public fun isCharging(): Boolean = getChargeStatus() == BatteryManager.BATTERY_STATUS_CHARGING

    /**
     * Checks if the battery is currently discharging.<br><br>
     * 배터리가 현재 방전 중인지 확인합니다.<br>
     *
     * @return `true` if battery is discharging, `false` otherwise.<br><br>
     *         배터리 방전 중이면 `true`, 아니면 `false`.<br>
     */
    public fun isDischarging(): Boolean = getChargeStatus() == BatteryManager.BATTERY_STATUS_DISCHARGING

    /**
     * Checks if the battery is not charging.<br><br>
     * 배터리가 충전되지 않는 상태인지 확인합니다.<br>
     *
     * @return `true` if battery is not charging, `false` otherwise.<br><br>
     *         배터리가 충전되지 않으면 `true`, 아니면 `false`.<br>
     */
    public fun isNotCharging(): Boolean = getChargeStatus() == BatteryManager.BATTERY_STATUS_NOT_CHARGING

    /**
     * Checks if the battery is fully charged.<br><br>
     * 배터리가 완전히 충전되었는지 확인합니다.<br>
     *
     * @return `true` if battery is full, `false` otherwise.<br><br>
     *         배터리가 완전 충전이면 `true`, 아니면 `false`.<br>
     */
    public fun isFull(): Boolean = getChargeStatus() == BatteryManager.BATTERY_STATUS_FULL

    /**
     * Gets the remaining battery capacity as a percentage (0-100).<br><br>
     * 남은 배터리 용량을 백분율(0-100)로 가져옵니다.<br>
     *
     * @return The remaining battery capacity as a percentage.<br><br>
     *         남은 배터리 용량 (백분율).<br>
     */
    public fun getCapacity(): Int = tryCatchSystemManager(BATTERY_ERROR_VALUE) {
        batteryPropertyReader.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    /**
     * Battery capacity in microampere-hours, as an integer.<br><br>
     * 배터리 용량을 마이크로암페어시 단위로 반환합니다.<br>
     *
     * @return The battery capacity in microampere-hours.<br><br>
     *         배터리 용량 (마이크로암페어시).<br>
     */
    public fun getChargeCounter(): Int = tryCatchSystemManager(BATTERY_ERROR_VALUE) {
        batteryPropertyReader.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
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
    public fun getEnergyCounter(): Long = tryCatchSystemManager(BATTERY_ERROR_VALUE_LONG) {
        batteryPropertyReader.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
    }

    /**
     * get charge plug type.<br><br>
     * 충전 플러그 타입을 반환합니다.<br>
     *
     * @return Charge plug type.<br><br>
     *         충전 플러그 타입.<br>
     * @see BatteryManager.BATTERY_PLUGGED_USB
     * @see BatteryManager.BATTERY_PLUGGED_AC
     * @see BatteryManager.BATTERY_PLUGGED_DOCK
     * @see BatteryManager.BATTERY_PLUGGED_WIRELESS
     */
    public fun getChargePlug(): Int = tryCatchSystemManager(BATTERY_ERROR_VALUE) {
        return batteryPropertyReader.getStatus(getBatteryStatus(), BatteryManager.EXTRA_PLUGGED, BATTERY_ERROR_VALUE)
    }

    /**
     * Checks if the device is charging via USB.<br><br>
     * 기기가 USB를 통해 충전 중인지 확인합니다.<br>
     *
     * @return `true` if charging via USB, `false` otherwise.<br><br>
     *         USB로 충전 중이면 `true`, 아니면 `false`.<br>
     */
    public fun isChargingUsb(): Boolean = batteryPropertyReader.isChargingUsb(getChargePlug())

    /**
     * Checks if the device is charging via AC.<br><br>
     * 기기가 AC를 통해 충전 중인지 확인합니다.<br>
     *
     * @return `true` if charging via AC, `false` otherwise.<br><br>
     *         AC로 충전 중이면 `true`, 아니면 `false`.<br>
     */
    public fun isChargingAc(): Boolean = batteryPropertyReader.isChargingAc(getChargePlug())

    /**
     * Checks if the device is charging wirelessly.<br><br>
     * 기기가 무선으로 충전 중인지 확인합니다.<br>
     *
     * @return `true` if charging wirelessly, `false` otherwise.<br><br>
     *         무선 충전 중이면 `true`, 아니면 `false`.<br>
     */
    public fun isChargingWireless(): Boolean = batteryPropertyReader.isChargingWireless(getChargePlug())

    /**
     * Checks if the device is charging via dock (API 33+).<br><br>
     * 기기가 독을 통해 충전 중인지 확인합니다 (API 33+).<br>
     *
     * @return `true` if charging via dock, `false` otherwise.<br><br>
     *         독으로 충전 중이면 `true`, 아니면 `false`.<br>
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    public fun isChargingDock(): Boolean = batteryPropertyReader.isChargingDock(getChargePlug())

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
    public fun getChargePlugList(): List<String> = tryCatchSystemManager(listOf(STR_CHARGE_PLUG_UNKNOWN)) {
        batteryPropertyReader.getChargePlugList(getChargePlug())
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
    public fun getTemperature(): Double = tryCatchSystemManager(BATTERY_ERROR_VALUE_DOUBLE) {
        batteryPropertyReader.getTemperature(getBatteryStatus())
    }

    /**
     * Checks if a battery is present in the device.<br><br>
     * 기기에 배터리가 장착되어 있는지 확인합니다.<br>
     *
     * @return `true` if battery is present, `false` if not present, `null` if status is unavailable.<br><br>
     *         배터리가 장착되어 있으면 `true`, 장착되어 있지 않으면 `false`, 상태를 알 수 없으면 `null`.<br>
     */
    public fun getPresent(): Boolean? = tryCatchSystemManager(BATTERY_ERROR_VALUE_BOOLEAN) {
        return batteryPropertyReader.getPresent(getBatteryStatus())
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
    public fun getHealth(): Int = tryCatchSystemManager(BATTERY_ERROR_VALUE) {
        return batteryPropertyReader.getHealth(getBatteryStatus())
    }

    /**
     * Checks if battery health is good.<br><br>
     * 배터리 상태가 양호한지 확인합니다.<br>
     *
     * @return `true` if battery health is good, `false` otherwise.<br><br>
     *         배터리 상태가 양호하면 `true`, 아니면 `false`.<br>
     */
    public fun isHealthGood(): Boolean = getHealth() == BatteryManager.BATTERY_HEALTH_GOOD

    /**
     * Checks if battery health is cold.<br><br>
     * 배터리 상태가 저온인지 확인합니다.<br>
     *
     * @return `true` if battery health is cold, `false` otherwise.<br><br>
     *         배터리 상태가 저온이면 `true`, 아니면 `false`.<br>
     */
    public fun isHealthCold(): Boolean = getHealth() == BatteryManager.BATTERY_HEALTH_COLD

    /**
     * Checks if battery health is dead.<br><br>
     * 배터리 상태가 손상되었는지 확인합니다.<br>
     *
     * @return `true` if battery health is dead, `false` otherwise.<br><br>
     *         배터리 상태가 손상되었으면 `true`, 아니면 `false`.<br>
     */
    public fun isHealthDead(): Boolean = getHealth() == BatteryManager.BATTERY_HEALTH_DEAD

    /**
     * Checks if battery has over voltage.<br><br>
     * 배터리가 과전압 상태인지 확인합니다.<br>
     *
     * @return `true` if battery has over voltage, `false` otherwise.<br><br>
     *         배터리가 과전압 상태면 `true`, 아니면 `false`.<br>
     */
    public fun isHealthOverVoltage(): Boolean = getHealth() == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE

    /**
     * Gets current battery health as text.<br><br>
     * 현재 배터리 건강 상태를 문자열로 반환합니다.<br>
     *
     * @return Human-readable battery health string (e.g., "Good", "Cold", "Dead", "Over Voltage", "Unknown").<br><br>
     *         사람이 읽기 쉬운 배터리 건강 상태 문자열 (예: "Good", "Cold", "Dead", "Over Voltage", "Unknown").<br>
     */
    public fun getCurrentHealthStr(): String = batteryPropertyReader.getHealthStr(getHealth())

    /**
     * get battery voltage.<br><br>
     * 배터리 전압을 반환합니다.<br>
     *
     * @return Battery voltage in volts (ex 3.5). Returns [BATTERY_ERROR_VALUE_DOUBLE] on error.<br><br>
     *         볼트 단위의 배터리 전압 (예: 3.5). 오류 시 [BATTERY_ERROR_VALUE_DOUBLE] 반환.<br>
     */
    public fun getVoltage(): Double = tryCatchSystemManager(BATTERY_ERROR_VALUE_DOUBLE) {
        return batteryPropertyReader.getVoltage(getBatteryStatus())
    }

    /**
     * get battery technology.<br><br>
     * 배터리 기술 방식을 반환합니다.<br>
     *
     * @return Battery technology (ex Li-ion). Returns null on error.<br><br>
     *         배터리 기술 방식 (예: Li-ion). 오류 시 null 반환.<br>
     */
    public fun getTechnology(): String? = tryCatchSystemManager(null) {
        return batteryPropertyReader.getTechnology(getBatteryStatus())
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
    public fun getTotalCapacity(): Double = tryCatchSystemManager(BATTERY_ERROR_VALUE_DOUBLE) {
        return batteryPropertyReader.getTotalCapacity(getChargeCounter(), getCapacity())
    }

    /**
     * Gets the battery manager instance.<br><br>
     * 배터리 매니저 인스턴스를 가져옵니다.<br>
     *
     * @return The battery manager instance.<br><br>
     *         배터리 매니저 인스턴스.<br>
     */
    public fun getBatteryManager() = batteryPropertyReader.bm

    /**
     * Gets the current battery status intent from the receiver.<br>
     * First tries to get the cached intent, then falls back to fetching a fresh one if unavailable.<br><br>
     * 리시버로부터 현재 배터리 상태 인텐트를 가져옵니다.<br>
     * 먼저 캐시된 인텐트를 가져오려 시도하고, 없으면 새 인텐트를 가져옵니다.<br>
     *
     * @return Battery status intent, or `null` if unavailable.<br><br>
     *         배터리 상태 인텐트, 사용할 수 없으면 `null`.<br>
     */
    private fun getBatteryStatus(): Intent? = receiver.getBatteryStatusIntent()
        ?: receiver.fetchBatteryStatusIntent()

    /**
     * Releases all resources used by this instance.<br>
     * Call this method when you're done using BatteryStateInfo.<br><br>
     * 이 인스턴스가 사용하는 모든 리소스를 해제합니다.<br>
     * BatteryStateInfo 사용이 끝나면 이 메서드를 호출하세요.<br>
     */
    public override fun onDestroy() {
        super.onDestroy()
        unRegister()
    }
}
