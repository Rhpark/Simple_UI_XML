package kr.open.library.simple_ui.core.system_manager.info.location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.permissions.extentions.hasPermissions
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.base.DataUpdate
import kr.open.library.simple_ui.core.system_manager.extensions.getLocationManager

/**
 * Provides location state information and manages location updates.<br><br>
 * 위치 상태 정보를 제공하고 위치 업데이트를 관리합니다.<br>
 *
 * Required Permissions:<br>
 * - `ACCESS_FINE_LOCATION` for precise location<br>
 * - `ACCESS_COARSE_LOCATION` for approximate location<br><br>
 * 필수 권한:<br>
 * - `ACCESS_FINE_LOCATION` 정밀 위치 확인<br>
 * - `ACCESS_COARSE_LOCATION` 대략적 위치 확인<br>
 *
 * @param context The application context.<br><br>
 *                애플리케이션 컨텍스트입니다.<br>
 */
public open class LocationStateInfo(
    context: Context,
) : BaseSystemService(context, listOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) {
    /**
     * Lazy-initialized LocationManager instance.<br><br>
     * 지연 초기화된 LocationManager 인스턴스입니다.<br>
     */
    public val locationManager: LocationManager by lazy { context.getLocationManager() }

    /**
     * Mutable flow caching the latest location state event.<br><br>
     * 최신 위치 상태 이벤트를 보관하는 MutableStateFlow입니다.<br>
     */
    private val msfUpdate: MutableStateFlow<LocationStateEvent> =
        MutableStateFlow(LocationStateEvent.OnGpsEnabled(isGpsEnabled()))

    /**
     * StateFlow that emits location state events whenever location information changes.<br><br>
     * 위치 정보가 바뀔 때마다 위치 상태 이벤트를 방출하는 StateFlow입니다.<br>
     */
    public val sfUpdate: StateFlow<LocationStateEvent> = msfUpdate.asStateFlow()

    /**
     * Last known location cache for reactive flow.<br><br>
     * 반응형 플로우용 마지막 위치 캐시입니다.<br>
     */
    private val locationChanged = DataUpdate<Location?>(getLocation())

    /**
     * GPS provider enabled state cache.<br><br>
     * GPS 제공자 활성 상태 캐시입니다.<br>
     */
    private val isGpsEnabled = DataUpdate<Boolean>(isGpsEnabled())

    /**
     * Network provider enabled state cache.<br><br>
     * 네트워크 제공자 활성 상태 캐시입니다.<br>
     */
    private val isNetworkEnabled = DataUpdate<Boolean>(isNetworkEnabled())

    /**
     * Passive provider enabled state cache.<br><br>
     * Passive 제공자 활성 상태 캐시입니다.<br>
     */
    private val isPassiveEnabled = DataUpdate<Boolean>(isPassiveEnabled())

    /**
     * Fused provider enabled state cache (API 31+).<br><br>
     * Fused 제공자 활성 상태 캐시(API 31+)입니다.<br>
     */
    private val isFusedEnabled =
        DataUpdate<Boolean>(
            checkSdkVersion(
                Build.VERSION_CODES.S,
                positiveWork = { isFusedEnabled() },
                negativeWork = { false }
            ),
        )

    /**
     * Coroutine scope used for collecting/emitting updates.<br><br>
     * 업데이트 수집·방출에 사용하는 코루틴 스코프입니다.<br>
     */
    private var coroutineScope: CoroutineScope? = null

    /**
     * Subscribes data updates and emits LocationStateEvents.<br><br>
     * 위치 데이터 변화를 구독해 LocationStateEvent를 방출합니다.<br>
     */
    private fun setupDataFlows() {
        coroutineScope?.let { scope ->
            scope.launch { locationChanged.state.collect { sendFlow(LocationStateEvent.OnLocationChanged(it)) } }
            scope.launch { isGpsEnabled.state.collect { sendFlow(LocationStateEvent.OnGpsEnabled(it)) } }
            scope.launch { isNetworkEnabled.state.collect { sendFlow(LocationStateEvent.OnNetworkEnabled(it)) } }
            scope.launch { isPassiveEnabled.state.collect { sendFlow(LocationStateEvent.OnPassiveEnabled(it)) } }
            scope.launch { isFusedEnabled.state.collect { sendFlow(LocationStateEvent.OnFusedEnabled(it)) } }
        }
    }

    /**
     * Listener handling location/provider callbacks.<br><br>
     * 위치/제공자 콜백을 처리하는 리스너입니다.<br>
     */
    private val locationListener =
        object : LocationListener {
            /**
             * Called when location is updated.<br><br>
             * 위치가 갱신될 때 호출됩니다.<br>
             */
            override fun onLocationChanged(location: Location) {
                Logx.d(
                    "Location updated: lat=${location.latitude}, lng=${location.longitude}, accuracy=${location.accuracy}m",
                )
                locationChanged.update(location)
            }

            /**
             * Called when provider status changes.<br><br>
             * 제공자 상태가 바뀔 때 호출됩니다.<br>
             */
            override fun onStatusChanged(
                provider: String?,
                status: Int,
                extras: Bundle?,
            ) {
                Logx.d("Location status changed: provider=$provider, status=$status")
            }

            /**
             * Called when a provider is enabled.<br><br>
             * 제공자가 활성화될 때 호출됩니다.<br>
             */
            override fun onProviderEnabled(provider: String) {
                Logx.i("Location provider enabled: $provider")
                when (provider) {
                    LocationManager.GPS_PROVIDER -> isGpsEnabled.update(true)
                    LocationManager.NETWORK_PROVIDER -> isNetworkEnabled.update(true)
                    LocationManager.PASSIVE_PROVIDER -> isPassiveEnabled.update(true)
                    LocationManager.FUSED_PROVIDER -> {
                        checkSdkVersion(Build.VERSION_CODES.S) { isFusedEnabled.update(true) }
                    }
                }
            }

            /**
             * Called when a provider is disabled.<br><br>
             * 제공자가 비활성화될 때 호출됩니다.<br>
             */
            override fun onProviderDisabled(provider: String) {
                Logx.i("Location provider disabled: $provider")
                when (provider) {
                    LocationManager.GPS_PROVIDER -> isGpsEnabled.update(false)
                    LocationManager.NETWORK_PROVIDER -> isNetworkEnabled.update(false)
                    LocationManager.PASSIVE_PROVIDER -> isPassiveEnabled.update(false)
                    LocationManager.FUSED_PROVIDER -> {
                        checkSdkVersion(Build.VERSION_CODES.S) { isFusedEnabled.update(false) }
                    }
                }
            }
        }

    /**
     * Emits a location event if a coroutine scope is active.<br><br>
     * 코루틴 스코프가 활성 상태일 때 위치 이벤트를 방출합니다.<br>
     */
    private fun sendFlow(event: LocationStateEvent) = coroutineScope?.launch { msfUpdate.emit(event) }

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
     * Registers and starts location updates with the specified parameters.<br><br>
     * 지정한 매개변수로 위치 업데이트를 등록하고 시작합니다.<br>
     *
     * @param coroutineScope The coroutine scope for managing location updates.<br><br>
     *                       위치 업데이트를 관리할 코루틴 스코프입니다.
     *
     * @param locationProvider The location provider to use (GPS, NETWORK, etc.).<br><br>
     *                         사용할 위치 제공자(GPS, NETWORK 등)입니다.
     *
     * @param minTimeMs Minimum time interval between location updates in milliseconds.<br><br>
     *                  위치 업데이트 최소 시간 간격(밀리초)입니다.
     *
     * @param minDistanceM Minimum distance between location updates in meters.<br><br>
     *                     위치 업데이트 최소 거리(미터)입니다.
     */
    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    public fun registerStart(
        coroutineScope: CoroutineScope,
        locationProvider: String,
        minTimeMs: Long,
        minDistanceM: Float,
    ) {
        if (registerLocation()) {
            this.coroutineScope = coroutineScope
            registerLocationUpdateStart(locationProvider, minTimeMs, minDistanceM)
            setupDataFlows()
        } else {
            Logx.e("LocationStateInfo: registerStart failed")
        }
    }

    /**
     * Registers broadcast receiver for provider state changes.<br><br>
     * 제공자 상태 변화를 수신하기 위해 브로드캐스트 리시버를 등록합니다.<br>
     *
     * @return `true` if registration succeeded; `false` otherwise.<br><br>
     *         등록 성공 시 `true`, 실패 시 `false`입니다.<br>
     */
    private fun registerLocation(): Boolean =
        tryCatchSystemManager(false) {
            unregisterGpsState()
            gpsStateBroadcastReceiver =
                object : BroadcastReceiver() {
                    /**
                     * Handles provider change broadcasts.<br><br>
                     * 제공자 변경 브로드캐스트를 처리합니다.<br>
                     */
                    override fun onReceive(
                        context: Context,
                        intent: Intent,
                    ) {
                        if (intent.action.equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                            isGpsEnabled.update(isGpsEnabled())
                            isNetworkEnabled.update(isNetworkEnabled())
                            isPassiveEnabled.update(isPassiveEnabled())
                            checkSdkVersion(Build.VERSION_CODES.S) { isFusedEnabled.update(isFusedEnabled()) }
                        }
                    }
                }
            context.registerReceiver(gpsStateBroadcastReceiver, intentFilter)
            return true
        }

    /**
     * Requests location updates from the specified provider.<br><br>
     * 지정된 제공자로부터 위치 업데이트를 요청합니다.<br>
     */
    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    private fun registerLocationUpdateStart(
        locationProvider: String,
        minTimeMs: Long,
        minDistanceM: Float,
    ) {
        locationManager.requestLocationUpdates(locationProvider, minTimeMs, minDistanceM, locationListener)
    }

    /**
     * Cleans up resources on destruction.<br><br>
     * 파괴 시 리소스를 정리합니다.<br>
     */
    public override fun onDestroy() {
        unregister()
    }

    /**
     * Unregisters location listener safely.<br><br>
     * 위치 리스너 등록을 안전하게 해제합니다.<br>
     */
    private fun unregisterLocationUpdateListener() {
        safeCatch { locationManager.removeUpdates(locationListener) }
    }

    /**
     * Unregisters provider state broadcast receiver.<br><br>
     * 제공자 상태 브로드캐스트 리시버를 해제합니다.<br>
     */
    private fun unregisterGpsState() {
        gpsStateBroadcastReceiver?.let { safeCatch { context.unregisterReceiver(it) } }
        gpsStateBroadcastReceiver = null
    }

    /**
     * Checks if location services are enabled.<br><br>
     * 위치 서비스가 활성화되어 있는지 확인합니다.<br>
     *
     * @return `true` if location is enabled; `false` otherwise.<br><br>
     *         위치가 활성화되어 있으면 `true`, 아니면 `false`입니다.<br>
     */
    public fun isLocationEnabled(): Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    /**
     * Checks if GPS provider is enabled.<br><br>
     * GPS 제공자가 활성화되어 있는지 확인합니다.<br>
     *
     * @return `true` if GPS is enabled; `false` otherwise.<br><br>
     *         GPS가 활성화되어 있으면 `true`, 아니면 `false`입니다.<br>
     */
    public fun isGpsEnabled(): Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    /**
     * Checks if Network provider is enabled.<br><br>
     * 네트워크 제공자가 활성화되어 있는지 확인합니다.<br>
     *
     * @return `true` if the Network provider is enabled; `false` otherwise.<br><br>
     *         네트워크 제공자가 활성화되어 있으면 `true`, 아니면 `false`입니다.<br>
     */
    public fun isNetworkEnabled(): Boolean = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    /**
     * Checks if Passive provider is enabled.<br><br>
     * Passive 제공자가 활성화되어 있는지 확인합니다.<br>
     *
     * @return `true` if the Passive provider is enabled; `false` otherwise.<br><br>
     *         Passive 제공자가 활성화되어 있으면 `true`, 아니면 `false`입니다.<br>
     */
    public fun isPassiveEnabled(): Boolean = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)

    /**
     * Checks if Fused provider is enabled (API 31+).<br><br>
     * Fused 제공자가 활성화되어 있는지 확인합니다(API 31+).<br>
     *
     * @return `true` if the Fused provider is enabled; `false` otherwise.<br><br>
     *         Fused 제공자가 활성화되어 있으면 `true`, 아니면 `false`입니다.<br>
     */
    @RequiresApi(Build.VERSION_CODES.S)
    public fun isFusedEnabled(): Boolean = locationManager.isProviderEnabled(LocationManager.FUSED_PROVIDER)

    /**
     * Checks if any location provider is enabled.<br><br>
     * 하나라도 활성화된 위치 제공자가 있는지 확인합니다.<br>
     *
     * @return `true` if any provider is enabled; `false` otherwise.<br><br>
     *         하나라도 제공자가 활성화되어 있으면 `true`, 아니면 `false`입니다.<br>
     */
    public fun isAnyEnabled(): Boolean =
        checkSdkVersion(
            Build.VERSION_CODES.S,
            positiveWork = {
                (isLocationEnabled() || isGpsEnabled() || isNetworkEnabled() || isPassiveEnabled() || isFusedEnabled())
            },
            negativeWork = {
                (isLocationEnabled() || isGpsEnabled() || isNetworkEnabled() || isPassiveEnabled())
            },
        )

    /**
     * Gets the last known location from the location provider.<br><br>
     * 위치 제공자로부터 마지막으로 알려진 위치를 가져옵니다.<br>
     *
     * @return The last known Location, or `null` if unavailable.<br><br>
     *         마지막으로 알려진 Location이며, 없으면 `null`을 반환합니다.<br>
     */
    @SuppressLint("MissingPermission")
    public fun getLocation(): Location? {
        Logx.d(
            "isAnyEnabled() ${isAnyEnabled()} ${context.hasPermissions(
                ACCESS_COARSE_LOCATION,
            )}, ${context.hasPermissions(ACCESS_FINE_LOCATION)}",
        )
        return if (!isAnyEnabled()) {
            checkSdkVersion(
                Build.VERSION_CODES.S,
                positiveWork = {
                    Logx.e(
                        "can not find location!, isLocationEnabled ${isLocationEnabled()}, isGpsEnabled ${isGpsEnabled()}, isNetworkEnabled ${isNetworkEnabled()}, isPassiveEnabled ${isPassiveEnabled()}, isFusedEnabled ${isFusedEnabled()}",
                    )
                },
                negativeWork = {
                    Logx.e(
                        "can not find location!, isLocationEnabled ${isLocationEnabled()}, isGpsEnabled ${isGpsEnabled()}, isNetworkEnabled ${isNetworkEnabled()}, isPassiveEnabled ${isPassiveEnabled()}",
                    )
                },
            )
            null
        } else if (context.hasPermissions(ACCESS_COARSE_LOCATION) ||
            context.hasPermissions(ACCESS_FINE_LOCATION)
        ) {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } else {
            Logx.e(
                "can not find location!, ACCESS_COARSE_LOCATION ${context.hasPermissions(
                    ACCESS_COARSE_LOCATION,
                )}, ACCESS_FINE_LOCATION  ${context.hasPermissions(ACCESS_FINE_LOCATION)}",
            )
            null
        }
    }

    /**
     * Calculates the distance between two locations in meters.<br><br>
     * 두 위치 간의 거리를 미터 단위로 계산합니다.<br>
     *
     * @param fromLocation Starting location.<br><br>
     *                     시작 위치입니다.<br>
     * @param toLocation Destination location.<br><br>
     *                   도착 위치입니다.<br>
     * @return Distance in meters.<br><br>
     *         미터 단위 거리입니다.<br>
     */
    public fun calculateDistance(
        fromLocation: Location,
        toLocation: Location,
    ): Float = fromLocation.distanceTo(toLocation)

    /**
     * Calculates the bearing between two locations in degrees.<br><br>
     * 두 위치 간의 방위각을 도 단위로 계산합니다.<br>
     *
     * @param fromLocation Starting location.<br><br>
     *                     시작 위치입니다.<br>
     * @param toLocation Destination location.<br><br>
     *                   도착 위치입니다.<br>
     * @return Bearing in degrees.<br><br>
     *         도 단위 방위각입니다.<br>
     */
    public fun calculateBearing(
        fromLocation: Location,
        toLocation: Location,
    ): Float = fromLocation.bearingTo(toLocation)

    /**
     * Checks if two locations are within a specified radius.<br><br>
     * 두 위치가 지정한 반경 안에 있는지 확인합니다.<br>
     *
     * @param fromLocation First location.<br><br>
     *                     첫 번째 위치입니다.<br>
     * @param toLocation Second location.<br><br>
     *                   두 번째 위치입니다.<br>
     * @param radius Radius in meters.<br><br>
     *               미터 단위 반경입니다.<br>
     * @return `true` if locations are within radius; `false` otherwise.<br><br>
     *         반경 안에 있으면 `true`, 아니면 `false`입니다.<br>
     */
    public fun isLocationWithRadius(
        fromLocation: Location,
        toLocation: Location,
        radius: Float,
    ): Boolean = calculateDistance(fromLocation, toLocation) <= radius

    /**
     * Helper for persisting last known location.<br><br>
     * 마지막 위치를 저장/복원하는 헬퍼입니다.<br>
     */
    private val locationStorage by lazy { LocationSharedPreference(context) }

    /**
     * Loads the saved location from SharedPreferences.<br><br>
     * SharedPreferences에 저장된 위치를 불러옵니다.<br>
     *
     * @return The saved Location, or `null` if none exists.<br><br>
     *         저장된 위치가 있으면 Location, 없으면 `null`입니다.<br>
     */
    public fun loadLocation(): Location? = locationStorage.loadLocation()

    /**
     * Saves the location to SharedPreferences.<br><br>
     * 위치를 SharedPreferences에 저장합니다.<br>
     *
     * @param location The location to save.<br><br>
     *                 저장할 위치입니다.<br>
     */
    public fun saveApplyLocation(location: Location) {
        locationStorage.saveApplyLocation(location)
    }

    /**
     * Removes the saved location from SharedPreferences.<br><br>
     * SharedPreferences에 저장된 위치를 삭제합니다.<br>
     */
    public fun removeLocation() {
        locationStorage.removeApply()
    }

    /**
     * Unregisters all location listeners and cleans up resources.<br><br>
     * 모든 위치 리스너를 해제하고 리소스를 정리합니다.<br>
     */
    public fun unregister() {
        unregisterGpsState()
        unregisterLocationUpdateListener()
        coroutineScope?.cancel()
        coroutineScope = null
    }
}
