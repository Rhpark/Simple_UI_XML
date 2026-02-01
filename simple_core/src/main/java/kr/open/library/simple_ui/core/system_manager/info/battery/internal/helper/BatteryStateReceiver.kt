package kr.open.library.simple_ui.core.system_manager.info.battery.internal.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_NOT_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.DISABLE_UPDATE_CYCLE_TIME

/**
 * Internal helper class that manages battery broadcast receiver and periodic updates.<br>
 * Combines real-time broadcast events with periodic checks for comprehensive battery monitoring.<br><br>
 * 배터리 브로드캐스트 리시버와 주기적 업데이트를 관리하는 내부 헬퍼 클래스입니다.<br>
 * 실시간 브로드캐스트 이벤트와 일정 간격 확인을 결합하여 포괄적인 배터리 모니터링을 제공합니다.<br>
 */
internal class BatteryStateReceiver(
    private val context: Context,
    private val receiveBatteryInfo: () -> Unit
) {
    // 추가: parentJob completion 핸들 보관
    private var parentJobCompletionHandle: DisposableHandle? = null

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
                synchronized(batteryStatusLock) {
                    batteryStatus = intent
                }
            }
            receiveBatteryInfo.invoke()
        }
    }

    /**
     * IntentFilter for battery-related broadcast events.<br>
     * Listens to battery changes, low/okay battery, power connection/disconnection, and power usage summary.<br><br>
     * 배터리 관련 브로드캐스트 이벤트를 위한 IntentFilter입니다.<br>
     * 배터리 변경, 배터리 부족/정상, 전원 연결/해제, 전원 사용 요약을 수신합니다.<br>
     */
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
    @Volatile
    private var batteryStatus: Intent? = null

    /**
     * Lock object for synchronizing batteryStatus access.<br><br>
     * batteryStatus 접근을 동기화하기 위한 락 객체입니다.<br>
     */
    private val batteryStatusLock = Any()

    /**
     * Indicates whether the broadcast receiver is currently registered.<br><br>
     * 브로드캐스트 리시버가 현재 등록되어 있는지 나타냅니다.<br>
     */
    @Volatile
    private var isReceiverRegistered = false

    /**
     * Lock object for synchronizing receiver registration/unregistration.<br><br>
     * 리시버 등록/해제를 동기화하기 위한 락 객체입니다.<br>
     */
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
                positiveWork = {
                    val intent = context.registerReceiver(batteryBroadcastReceiver, intentFilter, RECEIVER_NOT_EXPORTED)
                    synchronized(batteryStatusLock) { batteryStatus = intent }
                },
                negativeWork = {
                    val intent = context.registerReceiver(batteryBroadcastReceiver, intentFilter)
                    synchronized(batteryStatusLock) { batteryStatus = intent }
                },
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

            // 정리 시 completion 핸들도 함께 제거
            parentJobCompletionHandle?.dispose()
            parentJobCompletionHandle = null

            context.unregisterReceiver(batteryBroadcastReceiver)
            isReceiverRegistered = false
            synchronized(batteryStatusLock) {
                batteryStatus = null
            }
            internalCoroutineScope?.cancel()
            internalCoroutineScope = null
        }
    }

    /**
     * Starts periodic battery state updates with the given coroutine scope.<br>
     * Before calling this method, ensure registerBatteryReceiver() has been called.<br><br>
     * If the CoroutineScope has no Job, automatic cleanup is NOT guaranteed.
     * In that case, you must call unRegister() explicitly. Using a Job-bound scope (e.g., lifecycleScope/viewModelScope) is strongly recommended.<br><br>
     * 주어진 코루틴 스코프로 주기적인 배터리 상태 업데이트를 시작합니다.<br>
     * 이 메서드를 호출하기 전에 registerBatteryReceiver()가 호출되었는지 확인하세요.<br>
     * CoroutineScope에 Job이 없으면 자동 해제는 보장되지 않습니다. 이 경우 반드시 unRegister()를 직접 호출해야 합니다.
     * Job이 있는 스코프(lifecycleScope/viewModelScope) 사용을 강력히 권장합니다.<br>
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

        // 이전 parentJob completion 핸들 제거 (재시작 안전)
        parentJobCompletionHandle?.dispose()
        parentJobCompletionHandle = null

        internalCoroutineScope?.cancel()
        internalCoroutineScope = null
        // coroutine init
        val parentJob = coroutineScope.coroutineContext[Job]
        if (parentJob == null) {
            Logx.w("BatteryStateInfo: coroutineScope has no Job. Call unRegister() explicitly!!!.")
        }
        val supervisor = SupervisorJob(parentJob)
        internalCoroutineScope = CoroutineScope(coroutineScope.coroutineContext + supervisor)

        // 부모 스코프 종료 시 자동 해제
        parentJobCompletionHandle = parentJob?.invokeOnCompletion {
            unRegisterReceiver()
            updateStop()
        }

        setupDataFlows() // Setup reactive flows for data updates

        val scope = checkNotNull(internalCoroutineScope) { "internalCoroutineScope must be initialized before starting updates" }

        updateJob = if (updateCycleTime == DISABLE_UPDATE_CYCLE_TIME) {
            scope.launch {
                receiveBatteryInfo.invoke()
            }
        } else {
            scope.launch {
                while (isActive) {
                    receiveBatteryInfo.invoke()
                    delay(updateCycleTime)
                }
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
    public fun getBatteryStatusIntent(): Intent? = synchronized(batteryStatusLock) {
        batteryStatus
    }

    /**
     * Fetches the latest battery status intent.<br>
     * First tries to return the cached intent, then queries the system for a fresh intent if unavailable.<br><br>
     * 최신 배터리 상태 인텐트를 가져옵니다.<br>
     * 먼저 캐시된 인텐트를 반환하려 시도하고, 없으면 시스템에서 새 인텐트를 조회합니다.<br>
     *
     * @return Fresh battery status intent, or `null` if unavailable.<br><br>
     *         새로운 배터리 상태 인텐트, 사용할 수 없으면 `null`.<br>
     */
    public fun fetchBatteryStatusIntent(): Intent? = safeCatch(null) {
        getBatteryStatusIntent()?.let { return@safeCatch it }

        val latest = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        if (latest != null) {
            synchronized(batteryStatusLock) { batteryStatus = latest }
        }
        latest
    }

    /**
     * Gets the internal coroutine scope.<br><br>
     * 내부 코루틴 스코프를 가져옵니다.<br>
     *
     * @return The internal coroutine scope, or `null` if not initialized.<br><br>
     *         내부 코루틴 스코프, 초기화되지 않았으면 `null`입니다.<br>
     */
    public fun getCoroutineScope() = internalCoroutineScope
}
