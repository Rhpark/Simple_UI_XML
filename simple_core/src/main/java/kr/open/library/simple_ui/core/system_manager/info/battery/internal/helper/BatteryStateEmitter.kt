package kr.open.library.simple_ui.core.system_manager.info.battery.internal.helper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.BATTERY_ERROR_VALUE
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.BATTERY_ERROR_VALUE_BOOLEAN
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.BATTERY_ERROR_VALUE_DOUBLE
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.BATTERY_ERROR_VALUE_LONG
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateEvent
import kr.open.library.simple_ui.core.system_manager.info.battery.internal.model.BatteryStateData

/**
 * Manages battery state event emission and StateFlow-based metric caching.<br><br>
 * 배터리 상태 이벤트 발행과 StateFlow 기반 메트릭 캐싱을 관리합니다.<br>
 */
internal class BatteryStateEmitter {
    /**
     * List of collector jobs for managing coroutine lifecycles.<br><br>
     * 코루틴 라이프사이클 관리를 위한 수집기 작업 목록입니다.<br>
     */
    private val collectorJobs = mutableListOf<Job>()

    /**
     * SharedFlow that stores battery events with buffering.<br>
     * Buffer size: 1 (replay) + 16 (extra) = 17 total.<br><br>
     * 버퍼링을 통해 배터리 이벤트를 보관하는 SharedFlow입니다.<br>
     * 버퍼 크기: 1 (replay) + 16 (추가) = 총 17개.<br>
     * 현재 버퍼 설정은 고정값이며 옵션화는 제공하지 않습니다.<br>
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
     * Refreshes cached battery metrics and emits events.<br>
     * Each field in [stateInfoData] is written to its corresponding MutableStateFlow,
     * triggering change-based emission through [setupDataFlows].<br><br>
     * [stateInfoData]의 각 필드를 대응하는 MutableStateFlow에 기록하여
     * [setupDataFlows]를 통해 변경 기반 이벤트를 발행합니다.<br>
     *
     * @param stateInfoData Snapshot of all battery metrics to update.<br><br>
     *                      갱신할 모든 배터리 메트릭의 스냅샷입니다.<br>
     * @see BatteryStateData
     */
    public fun updateBatteryInfo(stateInfoData: BatteryStateData) {
        with(stateInfoData) {
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
    }

    /**
     * Cancels all active collector jobs and clears the job list.<br><br>
     * 모든 활성 수집기 작업을 취소하고 작업 목록을 초기화합니다.<br>
     */
    private fun resetCollectors() {
        collectorJobs.forEach { it.cancel() }
        collectorJobs.clear()
    }

    /**
     * Sets up reactive flows for all battery data updates.<br>
     * This method should be called after setting the coroutineScope.<br>
     * Note: dropWhile filters only the initial sentinel value; subsequent sentinel values may be emitted as events by design.<br><br>
     * 모든 배터리 데이터 업데이트를 위한 반응형 플로우를 설정합니다.<br>
     * 이 메서드는 coroutineScope를 설정한 후에 호출해야 합니다.<br>
     * 참고: dropWhile는 초기 센티널 값만 필터링하며, 이후 센티널 값은 의도적으로 이벤트로 발행될 수 있습니다.<br>
     *
     * @param coroutineScope The coroutine scope to use for collecting flows.<br><br>
     *                       플로우 수집에 사용할 코루틴 스코프입니다.<br>
     */
    public fun setupDataFlows(coroutineScope: CoroutineScope) {
        resetCollectors()
        coroutineScope.let { scope ->
            collectorJobs += scope.launch {
                msfCapacity
                    .dropWhile { it == BATTERY_ERROR_VALUE }
                    .collect { sendFlow(BatteryStateEvent.OnCapacity(it)) }
            }
            collectorJobs += scope.launch {
                msfCurrentAmpere
                    .dropWhile { it == BATTERY_ERROR_VALUE }
                    .collect { sendFlow(BatteryStateEvent.OnCurrentAmpere(it)) }
            }
            collectorJobs += scope.launch {
                msfCurrentAverageAmpere
                    .dropWhile { it == BATTERY_ERROR_VALUE }
                    .collect { sendFlow(BatteryStateEvent.OnCurrentAverageAmpere(it)) }
            }
            collectorJobs += scope.launch {
                msfChargeStatus
                    .dropWhile { it == BATTERY_ERROR_VALUE }
                    .collect { sendFlow(BatteryStateEvent.OnChargeStatus(it)) }
            }
            collectorJobs += scope.launch {
                msfChargeCounter
                    .dropWhile { it == BATTERY_ERROR_VALUE }
                    .collect { sendFlow(BatteryStateEvent.OnChargeCounter(it)) }
            }
            collectorJobs += scope.launch {
                msfChargePlug
                    .dropWhile { it == BATTERY_ERROR_VALUE }
                    .collect { sendFlow(BatteryStateEvent.OnChargePlug(it)) }
            }
            collectorJobs += scope.launch {
                msfHealth
                    .dropWhile { it == BATTERY_ERROR_VALUE }
                    .collect { sendFlow(BatteryStateEvent.OnHealth(it)) }
            }
            collectorJobs += scope.launch {
                msfEnergyCounter
                    .dropWhile { it == BATTERY_ERROR_VALUE_LONG }
                    .collect { sendFlow(BatteryStateEvent.OnEnergyCounter(it)) }
            }
            collectorJobs += scope.launch {
                msfPresent
                    .dropWhile { it == BATTERY_ERROR_VALUE_BOOLEAN }
                    .collect { sendFlow(BatteryStateEvent.OnPresent(it)) }
            }
            collectorJobs += scope.launch {
                msfTemperature
                    .dropWhile { it == BATTERY_ERROR_VALUE_DOUBLE }
                    .collect { sendFlow(BatteryStateEvent.OnTemperature(it)) }
            }
            collectorJobs += scope.launch {
                msfVoltage
                    .dropWhile { it == BATTERY_ERROR_VALUE_DOUBLE }
                    .collect { sendFlow(BatteryStateEvent.OnVoltage(it)) }
            }
        }
    }
}
