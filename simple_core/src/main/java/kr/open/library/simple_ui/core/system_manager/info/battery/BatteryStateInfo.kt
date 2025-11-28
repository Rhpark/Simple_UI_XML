package kr.open.library.simple_ui.core.system_manager.info.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.base.DataUpdate
import kr.open.library.simple_ui.core.system_manager.extensions.getBatteryManager
import kr.open.library.simple_ui.core.system_manager.info.battery.power.PowerProfile

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
 * 1. Use `startUpdate(coroutineScope: CoroutineScope)`<br>
 * 2. Call `startUpdate()` periodically from outside<br><br>
 *
 * @param context The application context.<br><br>
 *                애플리케이션 컨텍스트.
 */
public open class BatteryStateInfo(context: Context) :
    BaseSystemService(context, listOf(android.Manifest.permission.BATTERY_STATS)) {

    /**
     * Lazy BatteryManager instance used to query battery properties.<br><br>
     * 배터리 속성을 조회하기 위해 사용하는 지연 초기화 BatteryManager입니다.<br>
     */
    public val batteryManager: BatteryManager by lazy { context.getBatteryManager() }

    /**
     * Internal broadcast action for battery updates.<br><br>
     * 배터리 업데이트에 사용하는 내부 브로드캐스트 액션입니다.<br>
     */
    private val UPDATE_BATTERY = BATTERY_UPDATE_ACTION
    private val DEFAULT_UPDATE_CYCLE_MS = DEFAULT_UPDATE_CYCLE_TIME_MS

    /**
     * Common error value returned on failure (Int).<br><br>
     * 실패 시 반환되는 공통 오류 값(Int)입니다.<br>
     */
    public val ERROR_VALUE: Int = BATTERY_ERROR_VALUE
    /**
     * Common error value returned on failure (Double).<br><br>
     * 실패 시 반환되는 공통 오류 값(Double)입니다.<br>
     */
    public val ERROR_VALUE_DOUBLE: Double = BATTERY_ERROR_VALUE.toDouble()

    companion object {
        /**
         * Default update cycle time in milliseconds.<br><br>
         * 기본 업데이트 주기 시간 (밀리초)입니다.<br>
         */
        public const val DEFAULT_UPDATE_CYCLE_TIME_MS = 1000L
        
        /**
         * Custom battery update action for internal broadcasts.<br><br>
         * 내부 브로드캐스트용 사용자 정의 배터리 업데이트 액션입니다.<br>
         */
        public const val BATTERY_UPDATE_ACTION = "RHPARK_BATTERY_STATE_UPDATE"
        
        /**
         * Error value used when battery information cannot be retrieved.<br><br>
         * 배터리 정보를 가져올 수 없을 때 사용하는 오류 값입니다.<br>
         */
        public const val BATTERY_ERROR_VALUE: Int = Integer.MIN_VALUE

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

    /**
     * Lazy PowerProfile instance used for capacity estimation fallbacks.<br><br>
     * 용량 추정 폴백에 사용하는 지연 초기화 PowerProfile 인스턴스입니다.<br>
     */
    private val powerProfile: PowerProfile by lazy { PowerProfile(context) }

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
    private val capacity                = DataUpdate(getCapacity())
    private val currentAmpere           = DataUpdate(getCurrentAmpere())
    private val currentAverageAmpere    = DataUpdate(getCurrentAverageAmpere())
    private val chargeStatus            = DataUpdate(getChargeStatus())
    private val chargeCounter           = DataUpdate(getChargeCounter())
    private val chargePlug              = DataUpdate(getChargePlug())
    private val energyCounter           = DataUpdate(getEnergyCounter())
    private val health                  = DataUpdate(getHealth())
    private val present                 = DataUpdate(getPresent())
    private val totalCapacity           = DataUpdate(getTotalCapacity())
    private val temperature             = DataUpdate(getTemperature())
    private val voltage                 = DataUpdate(getVoltage())

    /**
     * Battery broadcast receiver for ACTION_BATTERY_* events.<br><br>
     * ACTION_BATTERY_* 이벤트를 처리하는 배터리 브로드캐스트 리시버입니다.<br>
     */
    private val batteryBroadcastReceiver = object : BroadcastReceiver() {
        /**
         * Receives battery intents and updates cached state.<br><br>
         * 배터리 인텐트를 수신해 캐시된 상태를 갱신합니다.<br>
         */
        override fun onReceive(context: Context, intent: Intent) {
            batteryStatus = intent
            updateBatteryInfo()
        }
    }

    private val intentFilter = IntentFilter().apply {
        addAction(Intent.ACTION_BATTERY_CHANGED)
        addAction(Intent.ACTION_BATTERY_LOW)
        addAction(Intent.ACTION_BATTERY_OKAY)
        addAction(Intent.ACTION_POWER_CONNECTED)
        addAction(Intent.ACTION_POWER_DISCONNECTED)
        addAction(Intent.ACTION_POWER_USAGE_SUMMARY)
        addAction(UPDATE_BATTERY)
    }

    /**
     * Cache for the latest battery intent.<br><br>
     * 최신 배터리 인텐트를 보관하는 캐시입니다.<br>
     */
    private var batteryStatus: Intent? = null
    /**
     * Job handling periodic battery updates.<br><br>
     * 주기적 배터리 업데이트를 담당하는 Job입니다.<br>
     */
    private var updateJob: Job? = null
    /**
     * Coroutine scope used for collecting and emitting updates.<br><br>
     * 업데이트 수집·발행에 사용하는 코루틴 스코프입니다.<br>
     */
    private var coroutineScope: CoroutineScope? = null
    /**
     * Indicates whether the broadcast receiver is currently registered.<br><br>
     * 브로드캐스트 리시버가 현재 등록되어 있는지 나타냅니다.<br>
     */
    private var isReceiverRegistered = false

    /**
     * Safely sends a battery state event to the flow.<br><br>
     * 배터리 상태 이벤트를 플로우로 안전하게 전송합니다.<br>
     *
     * @param event The event to send.<br><br>
     *              전송할 이벤트.
     */
    private fun sendFlow(event: BatteryStateEvent) {
        coroutineScope?.launch { msfUpdate.emit(event) } ?: run {
            Logx.e("Error: Cannot send event - coroutineScope is null. Call updateStart() first.")
        }
    }

    /**
     * Sets up reactive flows for all battery data updates.<br>
     * This method should be called after setting the coroutineScope.<br><br>
     * 모든 배터리 데이터 업데이트를 위한 반응형 플로우를 설정합니다.<br>
     * 이 메서드는 coroutineScope를 설정한 후에 호출해야 합니다.<br>
     */
    private fun setupDataFlows() {
        coroutineScope?.let { scope ->
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
    public fun registerStart(coroutine: CoroutineScope, updateCycleTime: Long = DEFAULT_UPDATE_CYCLE_MS) {
        if(registerReceiver()) {
            if(!updateStart(coroutine,updateCycleTime)) {
                unRegisterReceiver()
                throw Exception("BatteryStateInfo: updateStart() failed!!.")
            }
        } else {
            throw Exception("BatteryStateInfo: Receiver not registered!!.")
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
        unRegisterReceiver()
        checkSdkVersion(Build.VERSION_CODES.TIRAMISU,
            positiveWork = {
                batteryStatus = context.registerReceiver(batteryBroadcastReceiver, intentFilter, RECEIVER_EXPORTED)
            }, negativeWork = {
                batteryStatus = context.registerReceiver(batteryBroadcastReceiver, intentFilter)
            }
        )
        isReceiverRegistered = true
        return true
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
    private fun updateStart(coroutine: CoroutineScope, updateCycleTime: Long = DEFAULT_UPDATE_CYCLE_MS): Boolean = tryCatchSystemManager(false) {
        if (!isReceiverRegistered) {
            Logx.w("BatteryStateInfo: Receiver not registered, calling registerBatteryReceiver().")
            if (!registerReceiver()) {
                return@tryCatchSystemManager false
            }
        }

        updateStop()

        coroutineScope = coroutine
        setupDataFlows()  // Setup reactive flows for data updates
        updateJob = coroutine.launch {
            while (isActive) {
                sendBroadcast()
                delay(updateCycleTime)
            }
            updateStop()
        }
        return true
    }

    /**
     * Triggers a one-time update of battery state information.<br><br>
     * 배터리 상태 정보의 일회성 업데이트를 트리거합니다.<br>
     * 
     * @return `true` if update triggered successfully, `false` otherwise.<br><br>
     *         업데이트 트리거 성공 시 `true`, 실패 시 `false`.<br>
     */
    public fun updateBatteryState(): Boolean = tryCatchSystemManager(false) {
        sendBroadcast()
        return true
    }

    /**
     * Stops periodic battery updates.<br><br>
     * 주기적인 배터리 업데이트를 중지합니다.<br>
     * 
     * @return `true` if stop succeeded, `false` otherwise.<br><br>
     *         중지 성공 시 `true`, 실패 시 `false`.<br>
     */
    private fun updateStop(): Boolean = tryCatchSystemManager(false) {
        if(updateJob == null) return@tryCatchSystemManager true
        updateJob?.cancel()
        updateJob = null
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
     * Sends the latest battery intent to registered listeners.<br><br>
     * 최신 배터리 인텐트를 등록된 리스너로 전송합니다.<br>
     */
    private fun sendBroadcast() {
        batteryStatus?.let {
            it.action = UPDATE_BATTERY
            context.sendBroadcast(it)
        }
    }

    /**
     * Stops updates and unregisters the battery receiver.<br><br>
     * 업데이트를 중단하고 배터리 리시버를 해제합니다.<br>
     */
    public fun unRegister() {
        unRegisterReceiver()
        updateStop()
    }

    /**
     * Unregisters the battery broadcast receiver.<br><br>
     * 배터리 브로드캐스트 리시버의 등록을 해제합니다.<br>
     * 
     * @return `true` if unregistration succeeded, `false` otherwise.<br><br>
     *         등록 해제 성공 시 `true`, 실패 시 `false`.<br>
     */
    private fun unRegisterReceiver(): Boolean = tryCatchSystemManager(false) {
        if (!isReceiverRegistered) return@tryCatchSystemManager true

        context.unregisterReceiver(batteryBroadcastReceiver)
        isReceiverRegistered = false
        batteryStatus = null
        return true
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
    public fun getCurrentAmpere(): Int = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

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
    public fun getCurrentAverageAmpere(): Int = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)

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
    public fun getChargeStatus(): Int = tryCatchSystemManager(ERROR_VALUE) {
        val res = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
        return if (res == ERROR_VALUE) {
            // Try to get charge status from current batteryStatus first
            // 먼저 현재 batteryStatus에서 충전 상태를 가져오기 시도
            var chargeStatus = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, ERROR_VALUE) ?: ERROR_VALUE
            
            // If batteryStatus is null or doesn't have charge status, get fresh battery intent
            // batteryStatus가 null이거나 충전 상태가 없는 경우, 새로운 배터리 intent를 가져옴
            if (chargeStatus == ERROR_VALUE) {
                val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                chargeStatus = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, ERROR_VALUE) ?: ERROR_VALUE
            }
            chargeStatus
        } else {
            res
        }
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
     * Gets an integer battery property from `BatteryManager`.<br><br>
     * `BatteryManager`에서 정수형 배터리 속성을 가져옵니다.<br>
     */
    private fun getIntProperty(batteryType:Int) = batteryManager.getIntProperty(batteryType)

    /**
     * Gets a long battery property from `BatteryManager`.<br><br>
     * `BatteryManager`에서 Long 타입 배터리 속성을 가져옵니다.<br>
     */
    private fun getLongProperty(batteryType: Int) = batteryManager.getLongProperty(batteryType)

    /**
     * Gets the remaining battery capacity as a percentage (0-100).<br><br>
     * 남은 배터리 용량을 백분율(0-100)로 가져옵니다.<br>
     *
     * @return The remaining battery capacity as a percentage.<br><br>
     *         남은 배터리 용량 (백분율).
     */
    public fun getCapacity(): Int = getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

    /**
     * Battery capacity in microampere-hours, as an integer.<br><br>
     * 배터리 용량을 마이크로암페어시 단위로 반환합니다.<br>
     *
     * @return The battery capacity in microampere-hours.<br><br>
     *         배터리 용량 (마이크로암페어시).
     */
    public fun getChargeCounter(): Int = getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)

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
    public fun getEnergyCounter(): Long = getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
    


    /**
     * BatteryChargingPlugType
     * return BatteryManager
     * @see BatteryManager.BATTERY_PLUGGED_USB
     * @see BatteryManager.BATTERY_PLUGGED_AC
     * @see BatteryManager.BATTERY_PLUGGED_DOCK
     * @see BatteryManager.BATTERY_PLUGGED_WIRELESS
     * )
     * errorValue(-999)
     */
    public fun getChargePlug(): Int = tryCatchSystemManager( ERROR_VALUE) {
        // Try to get charge plug from current batteryStatus first
        // 먼저 현재 batteryStatus에서 충전 플러그 정보를 가져오기 시도
        var chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, ERROR_VALUE) ?: ERROR_VALUE
        
        // If batteryStatus is null or doesn't have charge plug info, get fresh battery intent
        // batteryStatus가 null이거나 충전 플러그 정보가 없는 경우, 새로운 배터리 intent를 가져옴
        if (chargePlug == ERROR_VALUE) {
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            chargePlug = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, ERROR_VALUE) ?: ERROR_VALUE
        }
        
        return chargePlug
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
    public fun getTemperature(): Double = tryCatchSystemManager(ERROR_VALUE_DOUBLE) {
        // Try to get temperature from current batteryStatus first
        // 먼저 현재 batteryStatus에서 온도를 가져오기 시도
        var rawTemperature = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, ERROR_VALUE) ?: ERROR_VALUE
        
        // If batteryStatus is null or doesn't have temperature, get fresh battery intent
        // batteryStatus가 null이거나 온도가 없는 경우, 새로운 배터리 intent를 가져옴
        if (rawTemperature == ERROR_VALUE) {
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            rawTemperature = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, ERROR_VALUE) ?: ERROR_VALUE
        }

        return if (rawTemperature == ERROR_VALUE) {
            ERROR_VALUE_DOUBLE  // Use a reasonable error value instead of Integer.MIN_VALUE / 10
        } else {
            val convertedTemp = rawTemperature.toDouble() / 10.0
            // Sanity check: reasonable battery temperature range (-40°C to 100°C)
            // 정상성 검사: 합리적인 배터리 온도 범위 (-40°C ~ 100°C)
            if (convertedTemp in -40.0..100.0) {
                convertedTemp
            } else {
                ERROR_VALUE_DOUBLE  // Invalid temperature value
            }
        }
    }

    /**
     * Checks if a battery is present in the device.<br><br>
     * 기기에 배터리가 장착되어 있는지 확인합니다.<br>
     */
    public fun getPresent(): Boolean = tryCatchSystemManager(false) {
        // Try to get present status from current batteryStatus first
        // 먼저 현재 batteryStatus에서 배터리 존재 상태를 가져오기 시도
        var present = batteryStatus?.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false)
        
        // If batteryStatus is null, get fresh battery intent
        // batteryStatus가 null인 경우, 새로운 배터리 intent를 가져옴
        if (present == null) {
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            present = batteryIntent?.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false)
        }
        
        return present ?: false
    }



    /**
     * Battery Health Status
     *
     * return BatteryManager(
     *  BATTERY_HEALTH_GOOD or
     *  BATTERY_HEALTH_COLD or
     *  BATTERY_HEALTH_DEAD or
     *  )
     * error return errorValue(-999)
     */
    public fun getHealth(): Int = tryCatchSystemManager(ERROR_VALUE) {
        // Try to get health status from current batteryStatus first
        // 먼저 현재 batteryStatus에서 배터리 상태를 가져오기 시도
        var health = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, ERROR_VALUE) ?: ERROR_VALUE
        
        // If batteryStatus is null or doesn't have health info, get fresh battery intent
        // batteryStatus가 null이거나 상태 정보가 없는 경우, 새로운 배터리 intent를 가져옴
        if (health == ERROR_VALUE) {
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            health = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, ERROR_VALUE) ?: ERROR_VALUE
        }
        
        return health
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
    public fun getCurrentHealthStr():String = getHealthStr(getHealth())

    /**
     * return volt (ex 3.5)
     * error is errorValue(@ERROR_VALUE_FLOAT)
     */
    public fun getVoltage(): Double = tryCatchSystemManager(ERROR_VALUE_DOUBLE) {
        // Try to get voltage from current batteryStatus first
        // 먼저 현재 batteryStatus에서 전압을 가져오기 시도
        var voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, ERROR_VALUE * 1000) ?: ERROR_VALUE * 1000
        
        // If batteryStatus is null or doesn't have voltage info, get fresh battery intent
        // batteryStatus가 null이거나 전압 정보가 없는 경우, 새로운 배터리 intent를 가져옴
        if (voltage == ERROR_VALUE * 1000) {
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            voltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, ERROR_VALUE * 1000) ?: ERROR_VALUE * 1000
        }

        return voltage.toDouble() / 1000
    }

    /**
     * return (ex Li-ion)
     * error is null
     */
    public fun getTechnology(): String? = tryCatchSystemManager(null) {
        // Try to get technology from current batteryStatus first
        // 먼저 현재 batteryStatus에서 배터리 기술을 가져오기 시도
        var technology = batteryStatus?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
        
        // If batteryStatus is null or doesn't have technology info, get fresh battery intent
        // batteryStatus가 null이거나 기술 정보가 없는 경우, 새로운 배터리 intent를 가져옴
        if (technology == null) {
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            technology = batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
        }

        return technology
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
    public fun getTotalCapacity(): Double = tryCatchSystemManager(ERROR_VALUE_DOUBLE) {
        // Primary method: Use PowerProfile
        // 주요 방법: PowerProfile 사용
        val powerProfileCapacity = powerProfile.getBatteryCapacity()
        if (powerProfileCapacity > 0) {
            return@tryCatchSystemManager powerProfileCapacity
        }
        
        // Fallback 1: Try to estimate from charge counter (API 21+)
        // Fallback 1: 충전 카운터로부터 추정 (API 21+)
        val estimatedCapacity = getEstimatedCapacityFromChargeCounter()
        if (estimatedCapacity > 0) {
            return@tryCatchSystemManager estimatedCapacity
        }
        
        // Last resort: return reasonable default
        // 최후 수단: 합리적인 기본값 반환
        Logx.w("Unable to determine battery capacity, using default: $ERROR_VALUE_DOUBLE mAh")
        return ERROR_VALUE_DOUBLE
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
    private fun getEstimatedCapacityFromChargeCounter(): Double = tryCatchSystemManager(ERROR_VALUE_DOUBLE) {
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
                ERROR_VALUE_DOUBLE
            }
        } else {
            Logx.w("Invalid values for estimation - chargeCounter: $chargeCounter µAh, capacity: $capacity%")
            ERROR_VALUE_DOUBLE
        }
    }


    /**
     * Releases all resources used by this instance.<br>
     * Call this method when you're done using BatteryStateInfo.<br><br>
     * 이 인스턴스가 사용하는 모든 리소스를 해제합니다.<br>
     * BatteryStateInfo 사용이 끝나면 이 메서드를 호출하세요.<br>
     */
    public override fun onDestroy() {
        super.onDestroy()
        updateStop()
        unRegisterReceiver()
        coroutineScope?.cancel()
        coroutineScope = null
    }
}