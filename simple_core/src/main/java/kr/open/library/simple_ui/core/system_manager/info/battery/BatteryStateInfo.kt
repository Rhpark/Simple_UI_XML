package kr.open.library.simple_ui.core.system_manager.info.battery

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.base.DataUpdate
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateVo.BATTERY_ERROR_VALUE
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateVo.BATTERY_ERROR_VALUE_DOUBLE
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateVo.DEFAULT_UPDATE_CYCLE_TIME
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateVo.STR_BATTERY_HEALTH_COLD
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateVo.STR_BATTERY_HEALTH_DEAD
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateVo.STR_BATTERY_HEALTH_GOOD
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateVo.STR_BATTERY_HEALTH_OVER_VOLTAGE
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateVo.STR_BATTERY_HEALTH_UNKNOWN
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateVo.STR_CHARGE_PLUG_AC
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateVo.STR_CHARGE_PLUG_DOCK
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateVo.STR_CHARGE_PLUG_UNKNOWN
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateVo.STR_CHARGE_PLUG_USB
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateVo.STR_CHARGE_PLUG_WIRELESS

/**
 * Provides information about the battery state of an Android device.<br><br>
 * Android 기기의 배터리 상태 정보를 제공합니다.<br>
 *
 * ⚠️ Permission notice / 권한 안내:<br>
 * - Requires `android.permission.BATTERY_STATS` (system / preloaded apps only).<br>
 * - When the permission is missing, `tryCatchSystemManager()` returns default values.<br><br>
 * - `android.permission.BATTERY_STATS` 권한이 필요합니다 (시스템 / 프리로드 앱만 가능).<br>
 * - 권한이 없으면 `tryCatchSystemManager()`가 기본값을 반환합니다.<br>
 *
 * It is recommended to call destroy() upon complete shutdown.<br><br>
 * 완전 종료 시 destroy()를 호출하는 것을 권장합니다.<br>
 *
 * To use update..Listener() method, you must update periodically to obtain a more accurate value.<br><br>
 * update..Listener()를 사용하기 위해서는 반드시 주기적으로 업데이트를 해야 조금 더 정확한 값을 가져올 수 있습니다.<br>
 *
 * Example usage:<br>
 * 1. Use `registerStart(coroutineScope: CoroutineScope)`<br>
 * 2. `registerStart()` should be called before using data flows<br><br>
 *
 * 사용 예시:<br>
 * 1. `registerStart(coroutineScope: CoroutineScope)`를 사용하세요.<br>
 * 2. `registerStart()`는 데이터 플로우를 사용하기 전에 호출해야 합니다.<br><br>
 *
 * @param context The application context.<br><br>
 *                애플리케이션 컨텍스트.
 */
public open class BatteryStateInfo(
    context: Context
) : BaseSystemService(context, listOf(android.Manifest.permission.BATTERY_STATS)) {
    private val batteryState = BatteryState(context)

    private val receiver = BatteryStateReceiver(context) { updateBatteryInfo() }

    /**
     * Mutable flow that stores the latest battery event.<br><br>
     * 최신 배터리 이벤트를 보관하는 MutableStateFlow입니다.<br>
     */
    private val msfUpdate: MutableStateFlow<BatteryStateEvent> = MutableStateFlow(BatteryStateEvent.OnCapacity(getCapacity()))

    /**
     * StateFlow that emits battery state events whenever battery information changes.<br><br>
     * 배터리 정보가 변경될 때마다 배터리 상태 이벤트를 방출하는 StateFlow입니다.<br>
     */
    public val sfUpdate: StateFlow<BatteryStateEvent> = msfUpdate.asStateFlow()

    /**
     * Cached battery metrics wrapped in DataUpdate for reactive flows.<br><br>
     * 반응형 플로우용 DataUpdate로 감싼 배터리 메트릭 캐시입니다.<br>
     */
    private val capacity = DataUpdate(getCapacity())
    private val currentAmpere = DataUpdate(getCurrentAmpere())
    private val currentAverageAmpere = DataUpdate(getCurrentAverageAmpere())
    private val chargeStatus = DataUpdate(getChargeStatus())
    private val chargeCounter = DataUpdate(getChargeCounter())
    private val chargePlug = DataUpdate(getChargePlug())
    private val energyCounter = DataUpdate(getEnergyCounter())
    private val health = DataUpdate(getHealth())
    private val present = DataUpdate(getPresent())
    private val totalCapacity = DataUpdate(getTotalCapacity())
    private val temperature = DataUpdate(getTemperature())
    private val voltage = DataUpdate(getVoltage())

    /**
     * Safely sends a battery state event to the flow.<br><br>
     * 배터리 상태 이벤트를 플로우로 안전하게 전송합니다.<br>
     *
     * @param event The event to send.<br><br>
     *              전송할 이벤트.
     */
    private fun sendFlow(event: BatteryStateEvent) {
        msfUpdate.value = event
    }

    /**
     * Sets up reactive flows for all battery data updates.<br>
     * This method should be called after setting the coroutineScope.<br><br>
     * 모든 배터리 데이터 업데이트를 위한 반응형 플로우를 설정합니다.<br>
     * 이 메서드는 coroutineScope를 설정한 후에 호출해야 합니다.<br>
     */
    private fun setupDataFlows() {
        receiver.getCoroutineScope()?.let { scope ->
            scope.launch { capacity.state.collect { sendFlow(BatteryStateEvent.OnCapacity(it)) } }
            scope.launch { currentAmpere.state.collect { sendFlow(BatteryStateEvent.OnCurrentAmpere(it)) } }
            scope.launch { currentAverageAmpere.state.collect { sendFlow(BatteryStateEvent.OnCurrentAverageAmpere(it)) } }
            scope.launch { chargeStatus.state.collect { sendFlow(BatteryStateEvent.OnChargeStatus(it)) } }
            scope.launch { chargeCounter.state.collect { sendFlow(BatteryStateEvent.OnChargeCounter(it)) } }
            scope.launch { chargePlug.state.collect { sendFlow(BatteryStateEvent.OnChargePlug(it)) } }
            scope.launch { energyCounter.state.collect { sendFlow(BatteryStateEvent.OnEnergyCounter(it)) } }
            scope.launch { health.state.collect { sendFlow(BatteryStateEvent.OnHealth(it)) } }
            scope.launch { present.state.collect { sendFlow(BatteryStateEvent.OnPresent(it)) } }
            scope.launch { totalCapacity.state.collect { sendFlow(BatteryStateEvent.OnTotalCapacity(it)) } }
            scope.launch { temperature.state.collect { sendFlow(BatteryStateEvent.OnTemperature(it)) } }
            scope.launch { voltage.state.collect { sendFlow(BatteryStateEvent.OnVoltage(it)) } }
        }
    }

    /**
     * Starts listening to battery broadcasts and periodic updates with the given scope.<br><br>
     * 지정한 코루틴 스코프로 배터리 브로드캐스트 등록과 주기적 업데이트를 시작합니다.<br>
     *
     * @param coroutine Coroutine scope used for collecting and emitting updates.<br><br>
     *                  업데이트 수집·발행에 사용할 코루틴 스코프입니다.<br>
     * @param updateCycleTime Update interval in milliseconds.<br><br>
     *                        업데이트 주기(밀리초)입니다.<br>
     * @throws Exception When receiver registration or update start fails.<br><br>
     *                   리시버 등록 또는 업데이트 시작에 실패하면 예외를 발생시킵니다.<br>
     */
    public fun registerStart(coroutine: CoroutineScope, updateCycleTime: Long = DEFAULT_UPDATE_CYCLE_TIME) {
        if (!registerReceiver()) {
            throw Exception("BatteryStateInfo: Receiver not registered!!.")
        }
        if (!updateStart(coroutine, updateCycleTime)) {
            receiver.unRegisterReceiver()
            throw Exception("BatteryStateInfo: updateStart() failed!!.")
        }
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
     * Starts periodic battery state updates with the given coroutine scope.<br>
     * Before calling this method, ensure registerBatteryReceiver() has been called.<br><br>
     * 주어진 코루틴 스코프로 주기적인 배터리 상태 업데이트를 시작합니다.<br>
     * 이 메서드를 호출하기 전에 registerBatteryReceiver()가 호출되었는지 확인하세요.<br>
     *
     * @param coroutine The coroutine scope to use for updates.<br><br>
     *                  업데이트에 사용할 코루틴 스코프.
     * @param updateCycleTime Update cycle time in milliseconds.<br><br>
     *                        밀리초 단위의 업데이트 주기 시간.
     * @return `true` if update started successfully, `false` otherwise.<br><br>
     *         업데이트 시작 성공 시 `true`, 실패 시 `false`.<br>
     */
    private fun updateStart(coroutine: CoroutineScope, updateCycleTime: Long = DEFAULT_UPDATE_CYCLE_TIME): Boolean =
        tryCatchSystemManager(false) {
            return receiver.updateStart(coroutine, updateCycleTime) { setupDataFlows() }
        }

    /**
     * Triggers a one-time update of battery state information.<br><br>
     * 배터리 상태 정보의 일회성 업데이트를 트리거합니다.<br>
     *
     * @return `true` if update triggered successfully, `false` otherwise.<br><br>
     *         업데이트 트리거 성공 시 `true`, 실패 시 `false`.<br>
     */
    public fun updateBatteryState(): Boolean = tryCatchSystemManager(false) {
//        return receiver.sendBroadcast()
        updateBatteryInfo()
        return true
    }

    /**
     * Refreshes cached battery metrics and emits events.<br><br>
     * 캐시된 배터리 지표를 갱신하고 이벤트를 방출합니다.<br>
     */
    private fun updateBatteryInfo() {
        capacity.update(getCapacity())
        chargeCounter.update(getChargeCounter())
        chargePlug.update(getChargePlug())
        chargeStatus.update(getChargeStatus())
        currentAmpere.update(getCurrentAmpere())
        currentAverageAmpere.update(getCurrentAverageAmpere())
        energyCounter.update(getEnergyCounter())
        health.update(getHealth())
        present.update(getPresent())
        temperature.update(getTemperature())
        totalCapacity.update(getTotalCapacity())
        voltage.update(getVoltage())
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
     *         순간 배터리 전류 (마이크로암페어).
     */
    public fun getCurrentAmpere(): Int = batteryState.getCurrentAmpere()

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
     *         평균 배터리 전류 (마이크로암페어, µA).
     */
    public fun getCurrentAverageAmpere(): Int = batteryState.getCurrentAverageAmpere()

    /**
     * Battery charge status, from a BATTERY_STATUS_* value.<br><br>
     * 배터리 충전 상태를 반환합니다.<br>
     *
     * @return The battery charge status.<br><br>
     *         배터리 충전 상태.
     * @see BatteryManager.BATTERY_STATUS_CHARGING
     * @see BatteryManager.BATTERY_STATUS_FULL
     * @see BatteryManager.BATTERY_STATUS_DISCHARGING
     * @see BatteryManager.BATTERY_STATUS_NOT_CHARGING
     * @see BatteryManager.BATTERY_STATUS_UNKNOWN
     */
    public fun getChargeStatus(): Int = tryCatchSystemManager(BATTERY_ERROR_VALUE) {
        return batteryState.getChargeStatus(receiver.getBatteryStatus())
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
     *         남은 배터리 용량 (백분율).
     */
    public fun getCapacity(): Int = batteryState.getCapacity()

    /**
     * Battery capacity in microampere-hours, as an integer.<br><br>
     * 배터리 용량을 마이크로암페어시 단위로 반환합니다.<br>
     *
     * @return The battery capacity in microampere-hours.<br><br>
     *         배터리 용량 (마이크로암페어시).
     */
    public fun getChargeCounter(): Int = batteryState.getChargeCounter()

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
     *         배터리 잔여 에너지 (나노와트시).
     */
    public fun getEnergyCounter(): Long = batteryState.getEnergyCounter()

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
        return batteryState.getChargePlug(receiver.getBatteryStatus())
    }

    /**
     * Checks if the device is charging via USB.<br><br>
     * 기기가 USB를 통해 충전 중인지 확인합니다.<br>
     *
     * @return `true` if charging via USB, `false` otherwise.<br><br>
     *         USB로 충전 중이면 `true`, 아니면 `false`.<br>
     */
    public fun isChargingUsb(): Boolean = getChargePlug() == BatteryManager.BATTERY_PLUGGED_USB

    /**
     * Checks if the device is charging via AC.<br><br>
     * 기기가 AC를 통해 충전 중인지 확인합니다.<br>
     *
     * @return `true` if charging via AC, `false` otherwise.<br><br>
     *         AC로 충전 중이면 `true`, 아니면 `false`.<br>
     */
    public fun isChargingAc(): Boolean = getChargePlug() == BatteryManager.BATTERY_PLUGGED_AC

    /**
     * Checks if the device is charging wirelessly.<br><br>
     * 기기가 무선으로 충전 중인지 확인합니다.<br>
     *
     * @return `true` if charging wirelessly, `false` otherwise.<br><br>
     *         무선 충전 중이면 `true`, 아니면 `false`.<br>
     */
    public fun isChargingWireless(): Boolean = getChargePlug() == BatteryManager.BATTERY_PLUGGED_WIRELESS

    /**
     * Checks if the device is charging via dock (API 33+).<br><br>
     * 기기가 독을 통해 충전 중인지 확인합니다 (API 33+).<br>
     *
     * @return `true` if charging via dock, `false` otherwise.<br><br>
     *         독으로 충전 중이면 `true`, 아니면 `false`.<br>
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    public fun isChargingDock(): Boolean = getChargePlug() == BatteryManager.BATTERY_PLUGGED_DOCK

    /**
     * Returns a readable label for the current power source.<br><br>
     * 현재 전원 타입을 읽기 쉬운 문자열로 반환합니다.<br>
     */
    public fun getChargePlugStr(): String = when (getChargePlug()) {
        BatteryManager.BATTERY_PLUGGED_USB -> STR_CHARGE_PLUG_USB
        BatteryManager.BATTERY_PLUGGED_AC -> STR_CHARGE_PLUG_AC
        BatteryManager.BATTERY_PLUGGED_DOCK -> STR_CHARGE_PLUG_DOCK
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> STR_CHARGE_PLUG_WIRELESS
        else -> STR_CHARGE_PLUG_UNKNOWN
    }

    /**
     * Gets the battery temperature in Celsius.<br>
     * Android returns temperature in tenths of a degree Celsius, so we divide by 10.<br><br>
     * 배터리 온도를 섭씨로 가져옵니다.<br>
     * Android는 온도를 섭씨 1/10도 단위로 반환하므로 10으로 나눕니다.<br>
     *
     * @return Battery temperature in Celsius (°C), or @ERROR_VALUE_FLOAT if unavailable.<br><br>
     *         배터리 온도 (섭씨), 사용할 수 없는 경우 @ERROR_VALUE_FLOAT.
     *
     * Example: Android returns 350 → 35.0°C<br><br>
     * 예시: Android가 350을 반환 → 35.0°C<br>
     *
     * Note: If you see very negative values like -214748364.8°C, it means temperature is unavailable.<br><br>
     * 참고: -214748364.8°C 같은 매우 낮은 음수가 보이면 온도를 사용할 수 없다는 뜻입니다.<br>
     */
    public fun getTemperature(): Double = tryCatchSystemManager(BATTERY_ERROR_VALUE_DOUBLE) {
        return batteryState.getTemperature(receiver.getBatteryStatus())
    }

    /**
     * Checks if a battery is present in the device.<br><br>
     * 기기에 배터리가 장착되어 있는지 확인합니다.<br>
     */
    public fun getPresent(): Boolean = tryCatchSystemManager(false) {
        return batteryState.getPresent(receiver.getBatteryStatus())
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
        return batteryState.getHealth(receiver.getBatteryStatus())
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
     * Gets current battery health as text.<br><br>
     * 현재 배터리 건강 상태를 문자열로 반환합니다.<br>
     */
    public fun getCurrentHealthStr(): String = getHealthStr(getHealth())

    /**
     * get battery voltage.<br><br>
     * 배터리 전압을 반환합니다.<br>
     *
     * @return Battery voltage in volts (ex 3.5). Returns [BATTERY_ERROR_VALUE_DOUBLE] on error.<br><br>
     *         볼트 단위의 배터리 전압 (예: 3.5). 오류 시 [BATTERY_ERROR_VALUE_DOUBLE] 반환.<br>
     */
    public fun getVoltage(): Double = tryCatchSystemManager(BATTERY_ERROR_VALUE_DOUBLE) {
        return batteryState.getVoltage(receiver.getBatteryStatus())
    }

    /**
     * get battery technology.<br><br>
     * 배터리 기술 방식을 반환합니다.<br>
     *
     * @return Battery technology (ex Li-ion). Returns null on error.<br><br>
     *         배터리 기술 방식 (예: Li-ion). 오류 시 null 반환.<br>
     */
    public fun getTechnology(): String? = tryCatchSystemManager(null) {
        return batteryState.getTechnology(receiver.getBatteryStatus())
    }

    /**
     * Gets the total battery capacity (rated capacity) in milliampere-hours (mAh).<br>
     * Uses multiple fallback methods for better compatibility across Android versions.<br><br>
     * 배터리의 총 용량(정격 용량)을 밀리암페어시(mAh) 단위로 가져옵니다.<br>
     * Android 버전 간 호환성을 위해 여러 fallback 방법을 사용합니다.<br>
     *
     * @return The total battery capacity in mAh, or default value if unavailable.<br><br>
     *         총 배터리 용량(mAh), 사용할 수 없는 경우 기본값.
     */
    public fun getTotalCapacity(): Double = tryCatchSystemManager(BATTERY_ERROR_VALUE_DOUBLE) {
        return batteryState.getTotalCapacity()
    }

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
