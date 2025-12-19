package kr.open.library.simple_ui.core.system_manager.info.battery.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_NOT_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx

/**
 * Internal helper class that manages battery broadcast receiver and periodic updates.<br>
 * Combines real-time broadcast events with periodic checks for comprehensive battery monitoring.<br><br>
 * 배터리 브로드캐스트 리시버와 주기적 업데이트를 관리하는 내부 헬퍼 클래스입니다.<br>
 * 실시간 브로드캐스트 이벤트와 일정 간격 확인을 결합하여 포괄적인 배터리 모니터링을 제공합니다.<br>
 */
internal class BatteryStateReceiverHelper(
    private val context: Context,
    private val receiveBatteryInfo: () -> Unit
) {
    /**
     * Job handling periodic battery updates.<br><br>
     * 주기적 배터리 업데이트를 담당하는 Job입니다.<br>
     */
    private var updateJob: Job? = null

    /**
     * Coroutine scope used for collecting and emitting updates.<br><br>
     * 업데이트 수집·발행에 사용하는 코루틴 스코프입니다.<br>
     */
    private var internalCoroutineScope: CoroutineScope? = null

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
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                batteryStatus = intent
            }
            receiveBatteryInfo.invoke()
        }
    }

    private val intentFilter = IntentFilter().apply {
        addAction(Intent.ACTION_BATTERY_CHANGED)
        addAction(Intent.ACTION_BATTERY_LOW)
        addAction(Intent.ACTION_BATTERY_OKAY)
        addAction(Intent.ACTION_POWER_CONNECTED)
        addAction(Intent.ACTION_POWER_DISCONNECTED)
        addAction(Intent.ACTION_POWER_USAGE_SUMMARY)
    }

    /**
     * Cache for the latest battery intent.<br><br>
     * 최신 배터리 인텐트를 보관하는 캐시입니다.<br>
     */
    private var batteryStatus: Intent? = null

    /**
     * Indicates whether the broadcast receiver is currently registered.<br><br>
     * 브로드캐스트 리시버가 현재 등록되어 있는지 나타냅니다.<br>
     */
    @Volatile
    private var isReceiverRegistered = false

    private val registerLock = Any()

    /**
     * Registers a broadcast receiver for battery-related events.<br>
     * Call this method before starting updates.<br><br>
     * 배터리 관련 이벤트를 위한 브로드캐스트 리시버를 등록합니다.<br>
     * 업데이트를 시작하기 전에 이 메서드를 호출하세요.<br>
     *
     * @return `true` if registration succeeded, `false` otherwise.<br><br>
     *         등록 성공 시 `true`, 실패 시 `false`.<br>
     */
    public fun registerReceiver(): Boolean = synchronized(registerLock) {
        safeCatch(false) {
            unRegisterReceiver()
            checkSdkVersion(
                Build.VERSION_CODES.TIRAMISU,
                positiveWork = { batteryStatus = context.registerReceiver(batteryBroadcastReceiver, intentFilter, RECEIVER_NOT_EXPORTED) },
                negativeWork = { batteryStatus = context.registerReceiver(batteryBroadcastReceiver, intentFilter) },
            )
            isReceiverRegistered = true
            return true
        }
    }

    /**
     * Unregisters the battery broadcast receiver.<br><br>
     * 배터리 브로드캐스트 리시버의 등록을 해제합니다.<br>
     *
     * @return `true` if unregistration succeeded, `false` otherwise.<br><br>
     *         등록 해제 성공 시 `true`, 실패 시 `false`.<br>
     */
    public fun unRegisterReceiver() = synchronized(registerLock) {
        safeCatch {
            if (!isReceiverRegistered) return

            context.unregisterReceiver(batteryBroadcastReceiver)
            isReceiverRegistered = false
            batteryStatus = null
            internalCoroutineScope?.cancel()
            internalCoroutineScope = null
            updateJob = null
        }
    }

    /**
     * Starts periodic battery state updates with the given coroutine scope.<br>
     * Before calling this method, ensure registerBatteryReceiver() has been called.<br><br>
     * 주어진 코루틴 스코프로 주기적인 배터리 상태 업데이트를 시작합니다.<br>
     * 이 메서드를 호출하기 전에 registerBatteryReceiver()가 호출되었는지 확인하세요.<br>
     *
     * @param coroutine The coroutine scope to use for updates.<br><br>
     *                  업데이트에 사용할 코루틴 스코프.<br>
     *
     * @param updateCycleTime Update cycle time in milliseconds.<br><br>
     *                        밀리초 단위의 업데이트 주기 시간.<br>
     *
     * @param setupDataFlows Callback to setup reactive data flows.<br><br>
     *                       반응형 데이터 플로우를 설정하는 콜백입니다.<br>
     * @return `true` if update started successfully, `false` otherwise.<br><br>
     *         업데이트 시작 성공 시 `true`, 실패 시 `false`.<br>
     */
    public fun updateStart(coroutineScope: CoroutineScope, updateCycleTime: Long, setupDataFlows: () -> Unit): Boolean = safeCatch(false) {
        if (!isReceiverRegistered) {
            Logx.w("BatteryStateInfo: Receiver not registered, calling registerBatteryReceiver().")
            if (!registerReceiver()) {
                return false
            }
        }

        updateStop()

        // coroutine init
        val parentJob = coroutineScope.coroutineContext[Job]
        val supervisor = SupervisorJob(parentJob)
        internalCoroutineScope = CoroutineScope(coroutineScope.coroutineContext + supervisor)

        // 부모 스코프 종료 시 자동 해제
        parentJob?.invokeOnCompletion {
            unRegisterReceiver()
            updateStop()
        }

        setupDataFlows() // Setup reactive flows for data updates

        // For Current Ampere, Current Average Ampere 더 자주 받기 위함
        updateJob = internalCoroutineScope!!.launch {
            while (isActive) {
                receiveBatteryInfo.invoke()
                delay(updateCycleTime)
            }
        }
        return true
    }

    /**
     * Stops periodic battery updates.<br><br>
     * 주기적인 배터리 업데이트를 중지합니다.<br>
     *
     * @return `true` if stop succeeded, `false` otherwise.<br><br>
     *         중지 성공 시 `true`, 실패 시 `false`.<br>
     */
    public fun updateStop(): Boolean = safeCatch(false) {
        if (updateJob == null) return true
        updateJob?.cancel()
        updateJob = null
        return true
    }

    /**
     * Gets the cached battery status intent.<br><br>
     * 캐시된 배터리 상태 인텐트를 가져옵니다.<br>
     *
     * @return The battery status intent, or `null` if not available.<br><br>
     *         배터리 상태 인텐트, 없으면 `null`입니다.<br>
     */
    public fun getBatteryStatusIntent() = batteryStatus

    /**
     * Gets the internal coroutine scope.<br><br>
     * 내부 코루틴 스코프를 가져옵니다.<br>
     *
     * @return The internal coroutine scope, or `null` if not initialized.<br><br>
     *         내부 코루틴 스코프, 초기화되지 않았으면 `null`입니다.<br>
     */
    public fun getCoroutineScope() = internalCoroutineScope
}
