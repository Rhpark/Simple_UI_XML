package kr.open.library.simple_ui.core.system_manager.info.location.internal.helper

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateEvent
import kr.open.library.simple_ui.core.system_manager.info.location.internal.model.LocationStateData

/**
 * Coordinates receiver registration and Flow emission for location updates.<br><br>
 * 위치 업데이트의 리시버 등록과 Flow 발행을 조율합니다.<br>
 *
 * @param context Application context used for receiver registration.<br><br>
 *                리시버 등록에 사용하는 애플리케이션 컨텍스트입니다.<br>
 * @param locationManager LocationManager instance.<br><br>
 *                        LocationManager 인스턴스입니다.<br>
 * @param dataProvider Supplies a snapshot of current location metrics.<br><br>
 *                     현재 위치 지표 스냅샷을 제공하는 람다입니다.<br>
 */
internal class LocationUpdateManager(
    context: Context,
    locationManager: LocationManager,
    private val dataProvider: () -> LocationStateData
) {
    /**
     * Manages event emission to SharedFlow and caches location metrics in StateFlows.<br><br>
     * SharedFlow로 이벤트를 발행하고 StateFlow에 위치 메트릭을 캐싱합니다.<br>
     */
    private val sender = LocationStateEmitter()

    /**
     * Manages location updates via LocationListener, BroadcastReceiver, and periodic polling.<br><br>
     * LocationListener, BroadcastReceiver, 주기적 폴링을 통한 위치 업데이트를 관리합니다.<br>
     */
    @SuppressLint("MissingPermission")
    private val receiver = LocationStateReceiver(context, locationManager) {
        sender.updateLocationInfo(dataProvider.invoke())
    }

    /**
     * Returns the SharedFlow for location events.<br><br>
     * 위치 이벤트를 방출하는 SharedFlow를 반환합니다.<br>
     */
    internal fun getSfUpdate(): SharedFlow<LocationStateEvent> = sender.getSfUpdate()

    /**
     * Registers broadcast receiver for provider state changes.<br><br>
     * 제공자 상태 변화를 위한 브로드캐스트 리시버를 등록합니다.<br>
     */
    internal fun registerReceiver(): Boolean = receiver.registerReceiver()

    /**
     * Registers location listener with the specified settings.<br><br>
     * 지정된 설정으로 위치 리스너를 등록합니다.<br>
     */
    internal fun registerLocationListener(
        locationProvider: String,
        updateCycleTime: Long,
        minDistanceM: Float
    ): Boolean = receiver.registerLocationListener(locationProvider, updateCycleTime, minDistanceM)

    /**
     * Starts periodic updates and sets up data flows.<br><br>
     * 주기적 업데이트를 시작하고 데이터 플로우를 설정합니다.<br>
     */
    internal fun updateStart(coroutineScope: CoroutineScope, updateCycleTime: Long): Boolean =
        receiver.updateStart(coroutineScope, updateCycleTime) {
            receiver.getCoroutineScope()?.let { sender.setupDataFlows(it) }
        }

    /**
     * Releases all receiver/listener resources.<br><br>
     * 리시버/리스너 리소스를 모두 해제합니다.<br>
     */
    internal fun destroy() {
        receiver.destroy()
    }
}
