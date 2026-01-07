package kr.open.library.simple_ui.core.system_manager.info.location.internal.helper

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_NOT_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresPermission
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

/**
 * Manages location updates via LocationListener, BroadcastReceiver, and periodic polling.<br><br>
 * LocationListener, BroadcastReceiver, 주기적 폴링을 통한 위치 업데이트를 관리합니다.<br>
 *
 * @param context The application context.<br><br>
 *                애플리케이션 컨텍스트입니다.<br>
 * @param locationManager The LocationManager instance.<br><br>
 *                        LocationManager 인스턴스입니다.<br>
 * @param receiveLocationChangedInfo Callback invoked when location or provider state changes.<br><br>
 *                                   위치 또는 제공자 상태가 변경될 때 호출되는 콜백입니다.<br>
 */
internal class LocationStateReceiver(
    val context: Context,
    private val locationManager: LocationManager,
    private val receiveLocationChangedInfo: () -> Unit
) {
    /**
     * Indicates whether the broadcast receiver is currently registered.<br><br>
     * 브로드캐스트 리시버가 현재 등록되어 있는지 나타냅니다.<br>
     */
    @Volatile
    private var isReceiverRegistered = false

    /**
     * Indicates whether the location listener is currently registered.<br><br>
     * 위치 리스너가 현재 등록되어 있는지 나타냅니다.<br>
     */
    @Volatile
    private var isLocationListenerRegistered = false

    /**
     * Lock object for synchronizing receiver registration/unregistration.<br><br>
     * 리시버 등록/해제를 동기화하기 위한 락 객체입니다.<br>
     */
    private val registerLock = Any()

    /**
     * Coroutine scope used for collecting/emitting updates.<br><br>
     * 업데이트 수집·방출에 사용하는 코루틴 스코프입니다.<br>
     */
    private var internalCoroutineScope: CoroutineScope? = null

    /**
     * Job for periodic location state updates.<br><br>
     * 주기적 위치 상태 업데이트를 위한 Job입니다.<br>
     */
    private var updateJob: Job? = null

    /**
     * Handle for parent job completion callback.<br><br>
     * 부모 Job 완료 콜백 핸들입니다.<br>
     */
    private var parentJobCompletionHandle: DisposableHandle? = null

    /**
     * BroadcastReceiver for provider on/off changes.<br><br>
     * 제공자 온·오프 변화를 수신하는 브로드캐스트 리시버입니다.<br>
     */
    private var gpsStateBroadcastReceiver: BroadcastReceiver? = null

    /**
     * Intent filter for provider change broadcasts.<br><br>
     * 제공자 변경 브로드캐스트를 위한 인텐트 필터입니다.<br>
     */
    private val intentFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)

    /**
     * LocationListener for real-time location updates.<br><br>
     * 실시간 위치 업데이트를 위한 LocationListener입니다.<br>
     */
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Logx.d("Location updated: lat=${location.latitude}, lng=${location.longitude}, accuracy=${location.accuracy}m")
            receiveLocationChangedInfo.invoke()
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            Logx.d("Location status changed: provider=$provider, status=$status")
        }

        override fun onProviderEnabled(provider: String) {
            Logx.i("Location provider enabled: $provider")
            receiveLocationChangedInfo.invoke()
        }

        override fun onProviderDisabled(provider: String) {
            Logx.i("Location provider disabled: $provider")
            receiveLocationChangedInfo.invoke()
        }
    }

    /**
     * Registers broadcast receiver for provider state changes.<br><br>
     * 제공자 상태 변화를 수신하기 위해 브로드캐스트 리시버를 등록합니다.<br>
     *
     * @return `true` if registration succeeded; `false` otherwise.<br><br>
     *         등록 성공 시 `true`, 실패 시 `false`입니다.<br>
     */
    public fun registerReceiver(): Boolean = synchronized(registerLock) {
        safeCatch(false) {
            if (isReceiverRegistered) {
                Logx.w("LocationStateReceiver: Receiver already registered")
                return true
            }

            gpsStateBroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                        receiveLocationChangedInfo.invoke()
                    }
                }
            }

            checkSdkVersion(
                Build.VERSION_CODES.TIRAMISU,
                positiveWork = { context.registerReceiver(gpsStateBroadcastReceiver, intentFilter, RECEIVER_NOT_EXPORTED) },
                negativeWork = { context.registerReceiver(gpsStateBroadcastReceiver, intentFilter) }
            )

            isReceiverRegistered = true
            true
        }
    }

    /**
     * Unregisters all location listeners and cleans up resources.<br><br>
     * 모든 위치 리스너를 해제하고 리소스를 정리합니다.<br>
     */
    public fun destroy() {
        unRegisterReceiver()
        unRegisterLocationListener()
        updateStop()
    }

    /**
     * Unregisters the broadcast receiver safely.<br><br>
     * 브로드캐스트 리시버를 안전하게 해제합니다.<br>
     */
    private fun unRegisterReceiver() = synchronized(registerLock) {
        safeCatch {
            if (isReceiverRegistered) {
                gpsStateBroadcastReceiver?.let { context.unregisterReceiver(it) }
                gpsStateBroadcastReceiver = null
                isReceiverRegistered = false
            }
        }
        safeCatch {
            // 정리 시 completion 핸들도 함께 제거
            parentJobCompletionHandle?.dispose()
            parentJobCompletionHandle = null
        }
    }

    /**
     * Registers location listener for the specified provider.<br><br>
     * 지정된 제공자에 대한 위치 리스너를 등록합니다.<br>
     *
     * @param locationProvider The location provider to use (GPS, NETWORK, etc.).<br><br>
     *                         사용할 위치 제공자(GPS, NETWORK 등)입니다.<br>
     * @param updateCycleTime Minimum time interval between location updates in milliseconds.<br><br>
     *                        위치 업데이트 최소 시간 간격(밀리초)입니다.<br>
     * @param minDistanceM Minimum distance between location updates in meters.<br><br>
     *                     위치 업데이트 최소 거리(미터)입니다.<br>
     * @return `true` if registration succeeded; `false` otherwise.<br><br>
     *         등록 성공 시 `true`, 실패 시 `false`입니다.<br>
     */
    @RequiresPermission(allOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    public fun registerLocationListener(locationProvider: String, updateCycleTime: Long, minDistanceM: Float): Boolean =
        synchronized(registerLock) {
            safeCatch(false) {
                if (isLocationListenerRegistered) {
                    Logx.w("LocationStateReceiver: LocationListener already registered")
                    return true
                }
                locationManager.requestLocationUpdates(
                    locationProvider,
                    updateCycleTime,
                    minDistanceM,
                    locationListener
                )
                isLocationListenerRegistered = true
                Logx.d("LocationListener registered for provider: $locationProvider")
                return true
            }
        }

    /**
     * Unregisters location listener safely.<br><br>
     * 위치 리스너를 안전하게 해제합니다.<br>
     */
    private fun unRegisterLocationListener() = synchronized(registerLock) {
        safeCatch {
            if (isLocationListenerRegistered) {
                locationManager.removeUpdates(locationListener)
                isLocationListenerRegistered = false
                Logx.d("LocationListener unregistered")
            }
        }
    }

    /**
     * Starts periodic location state updates with the given coroutine scope.<br>
     * Before calling this method, ensure registerReceiver() and registerLocationListener() have been called.<br><br>
     * If the CoroutineScope has no Job, automatic cleanup is NOT guaranteed.
     * In that case, you must call updateStop() explicitly. Using a Job-bound scope (e.g., lifecycleScope/viewModelScope) is strongly recommended.<br><br>
     * 주어진 코루틴 스코프로 주기적인 위치 상태 업데이트를 시작합니다.<br>
     * 이 메서드를 호출하기 전에 registerReceiver()와 registerLocationListener()가 호출되었는지 확인하세요.<br>
     * CoroutineScope에 Job이 없으면 자동 해제는 보장되지 않습니다. 이 경우 반드시 updateStop()을 직접 호출해야 합니다.
     * Job이 있는 스코프(lifecycleScope/viewModelScope) 사용을 강력히 권장합니다.<br>
     *
     * @param coroutineScope The coroutine scope to use for updates.<br><br>
     *                       업데이트에 사용할 코루틴 스코프입니다.<br>
     *
     * @param updateCycleTime Update cycle time in milliseconds.<br><br>
     *                        밀리초 단위의 업데이트 주기 시간입니다.<br>
     *
     * @param setupDataFlows Callback to setup reactive data flows.<br><br>
     *                       반응형 데이터 플로우를 설정하는 콜백입니다.<br>
     *
     * @return `true` if update started successfully, `false` otherwise.<br><br>
     *         업데이트 시작 성공 시 `true`, 실패 시 `false`입니다.<br>
     */
    public fun updateStart(coroutineScope: CoroutineScope, updateCycleTime: Long, setupDataFlows: () -> Unit): Boolean = safeCatch(false) {
        if (!isReceiverRegistered) {
            Logx.w("LocationStateReceiver: Receiver not registered, calling registerReceiver().")
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
            Logx.w("LocationStateReceiver: coroutineScope has no Job. Call updateStop() explicitly!!!.")
        }
        val supervisor = SupervisorJob(parentJob)
        internalCoroutineScope = CoroutineScope(coroutineScope.coroutineContext + supervisor)

        // 부모 스코프 종료 시 자동 해제
        parentJobCompletionHandle = parentJob?.invokeOnCompletion {
            unRegisterReceiver()
            unRegisterLocationListener()
            updateStop()
        }

        setupDataFlows() // Setup reactive flows for data updates

        /**
         * This polling does not guarantee new GPS updates; it only checks for refreshed location/provider state on a regular interval.<br><br>
         * 이 폴링은 새로운 GPS 업데이트를 보장하지 않으며, 일정 주기로 위치/제공자 상태 갱신 여부를 확인하기 위한 것입니다.<br>
         */
        updateJob = internalCoroutineScope!!.launch {
            while (isActive) {
                receiveLocationChangedInfo.invoke()
                delay(updateCycleTime)
            }
        }

        true
    }

    /**
     * Stops periodic updates and cancels the coroutine scope.<br><br>
     * 주기적 업데이트를 중단하고 코루틴 스코프를 취소합니다.<br>
     */
    private fun updateStop() {
        updateJob?.cancel()
        updateJob = null
        internalCoroutineScope?.cancel()
        internalCoroutineScope = null
    }

    /**
     * Returns the current coroutine scope.<br><br>
     * 현재 코루틴 스코프를 반환합니다.<br>
     *
     * @return The current coroutine scope, or `null` if not initialized.<br><br>
     *         현재 코루틴 스코프이며, 초기화되지 않았으면 `null`입니다.<br>
     */
    public fun getCoroutineScope(): CoroutineScope? = internalCoroutineScope
}
