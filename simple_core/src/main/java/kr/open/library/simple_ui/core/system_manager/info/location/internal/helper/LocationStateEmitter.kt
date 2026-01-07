package kr.open.library.simple_ui.core.system_manager.info.location.internal.helper

import android.location.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateConstants.LOCATION_ERROR_VALUE_BOOLEAN
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateConstants.LOCATION_ERROR_VALUE_LOCATION
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateEvent
import kr.open.library.simple_ui.core.system_manager.info.location.internal.model.LocationStateData

/**
 * Manages location state event emission and StateFlow-based metric caching.<br><br>
 * 위치 상태 이벤트 발행과 StateFlow 기반 메트릭 캐싱을 관리합니다.<br>
 */
internal class LocationStateEmitter {
    /**
     * List of collector jobs for managing coroutine lifecycles.<br><br>
     * 코루틴 라이프사이클 관리를 위한 수집기 작업 목록입니다.<br>
     */
    private val collectorJobs = mutableListOf<Job>()

    /**
     * SharedFlow that stores location events with buffering.<br>
     * Buffer size: 1 (replay) + 8 (extra) = 9 total.<br><br>
     * 버퍼링을 통해 위치 이벤트를 보관하는 SharedFlow입니다.<br>
     * 버퍼 크기: 1 (replay) + 8 (추가) = 총 9개.<br>
     */
    private val msfUpdate: MutableSharedFlow<LocationStateEvent> = MutableSharedFlow(
        replay = 1,
        extraBufferCapacity = 8,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )

    /**
     * Returns the SharedFlow that emits location state events.<br><br>
     * 위치 상태 이벤트를 방출하는 SharedFlow를 반환합니다.<br>
     *
     * @return SharedFlow that emits LocationStateEvent whenever location information changes.<br><br>
     *         위치 정보가 변경될 때마다 LocationStateEvent를 방출하는 SharedFlow입니다.<br>
     */
    public fun getSfUpdate(): SharedFlow<LocationStateEvent> = msfUpdate.asSharedFlow()

    /**
     * Cached location metrics using MutableStateFlow for reactive flows.<br><br>
     * 반응형 플로우용 MutableStateFlow로 관리되는 위치 메트릭 캐시입니다.<br>
     */
    private val msfLocationChanged = MutableStateFlow<Location?>(LOCATION_ERROR_VALUE_LOCATION)
    private val msfIsGpsEnabled = MutableStateFlow<Boolean?>(LOCATION_ERROR_VALUE_BOOLEAN)
    private val msfIsNetworkEnabled = MutableStateFlow<Boolean?>(LOCATION_ERROR_VALUE_BOOLEAN)
    private val msfIsPassiveEnabled = MutableStateFlow<Boolean?>(LOCATION_ERROR_VALUE_BOOLEAN)
    private val msfIsFusedEnabled = MutableStateFlow<Boolean?>(LOCATION_ERROR_VALUE_BOOLEAN)

    /**
     * Safely sends a location state event to the flow.<br><br>
     * 위치 상태 이벤트를 플로우로 안전하게 전송합니다.<br>
     *
     * @param event The event to send.<br><br>
     *              전송할 이벤트.<br>
     */
    private fun sendFlow(event: LocationStateEvent) {
        msfUpdate.tryEmit(event)
    }

    /**
     * Refreshes cached location metrics and emits events.<br><br>
     * 캐시된 위치 지표를 갱신하고 이벤트를 방출합니다.<br>
     *
     * @param data Snapshot of location metrics to update.<br><br>
     *             업데이트할 위치 메트릭 스냅샷입니다.<br>
     */
    public fun updateLocationInfo(data: LocationStateData) {
        msfLocationChanged.value = data.location
        msfIsGpsEnabled.value = data.isGpsEnabled
        msfIsNetworkEnabled.value = data.isNetworkEnabled
        msfIsPassiveEnabled.value = data.isPassiveEnabled
        msfIsFusedEnabled.value = data.isFusedEnabled
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
     * Sets up reactive flows for all location data updates.<br>
     * This method should be called after setting the coroutineScope.<br>
     * Note: dropWhile filters only the initial sentinel value; subsequent sentinel values may be emitted as events by design.<br><br>
     * 모든 위치 데이터 업데이트를 위한 반응형 플로우를 설정합니다.<br>
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
                msfLocationChanged
                    .dropWhile { it == LOCATION_ERROR_VALUE_LOCATION }
                    .collect { sendFlow(LocationStateEvent.OnLocationChanged(it)) }
            }
            collectorJobs += scope.launch {
                msfIsGpsEnabled
                    .dropWhile { it == LOCATION_ERROR_VALUE_BOOLEAN }
                    .collect { sendFlow(LocationStateEvent.OnGpsEnabled(it!!)) }
            }
            collectorJobs += scope.launch {
                msfIsNetworkEnabled
                    .dropWhile { it == LOCATION_ERROR_VALUE_BOOLEAN }
                    .collect { sendFlow(LocationStateEvent.OnNetworkEnabled(it!!)) }
            }
            collectorJobs += scope.launch {
                msfIsPassiveEnabled
                    .dropWhile { it == LOCATION_ERROR_VALUE_BOOLEAN }
                    .collect { sendFlow(LocationStateEvent.OnPassiveEnabled(it!!)) }
            }
            collectorJobs += scope.launch {
                msfIsFusedEnabled
                    .dropWhile { it == LOCATION_ERROR_VALUE_BOOLEAN }
                    .collect { sendFlow(LocationStateEvent.OnFusedEnabled(it!!)) }
            }
        }
    }
}
