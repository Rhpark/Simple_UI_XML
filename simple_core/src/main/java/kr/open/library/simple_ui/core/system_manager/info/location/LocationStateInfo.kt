package kr.open.library.simple_ui.core.system_manager.info.location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.permissions.extentions.hasPermissions
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.extensions.getLocationManager
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateConstants.DEFAULT_UPDATE_CYCLE_DISTANCE
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateConstants.DEFAULT_UPDATE_CYCLE_TIME
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateConstants.MIN_UPDATE_CYCLE_DISTANCE
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateConstants.MIN_UPDATE_CYCLE_TIME
import kr.open.library.simple_ui.core.system_manager.info.location.internal.helper.LocationStateEmitter
import kr.open.library.simple_ui.core.system_manager.info.location.internal.helper.LocationStateReceiver
import kr.open.library.simple_ui.core.system_manager.info.location.internal.model.LocationStateData

/**
 * Provides location state information and manages location updates from multiple providers.<br><br>
 * 여러 제공자로부터 위치 상태 정보와 위치 업데이트를 관리합니다.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Android's LocationManager requires manual LocationListener setup, BroadcastReceiver registration for provider state changes, and complex lifecycle management.<br>
 * - This class wraps the complexity into a simple reactive API with automatic Flow-based observation and multi-provider support.<br>
 * - Reduces boilerplate code for location monitoring, provider state tracking, permission validation, and SDK version branching in apps.<br>
 * - Provides best location selection algorithm and utility methods (distance, bearing, radius check) out of the box.<br><br>
 * - Android의 LocationManager는 수동 LocationListener 설정, 제공자 상태 변경을 위한 BroadcastReceiver 등록, 복잡한 라이프사이클 관리가 필요합니다.<br>
 * - 이 클래스는 복잡성을 자동 Flow 기반 관찰과 다중 제공자 지원이 가능한 간단한 반응형 API로 감쌉니다.<br>
 * - 앱에서 위치 모니터링, 제공자 상태 추적, 권한 검증, SDK 버전 분기를 위한 보일러플레이트 코드를 감소시킵니다.<br>
 * - 최적 위치 선택 알고리즘과 유틸리티 메서드(거리, 방위각, 반경 확인)를 기본 제공합니다.<br>
 *
 * **Design decisions / 설계 결정 이유:**<br>
 * - Uses MutableStateFlow for individual provider enabled states (GPS/NETWORK/PASSIVE/FUSED) to enable automatic duplicate filtering (distinctUntilChanged behavior).<br>
 * - Uses SharedFlow for unified location update event stream (sfUpdate) with buffering (replay=1, extraBufferCapacity=8) to prevent event loss during rapid location changes.<br>
 * - Combines LocationListener (real-time location updates) + BroadcastReceiver (provider state changes) + periodic polling for comprehensive monitoring.<br>
 * - Uses LocationStateReceiver to encapsulate all receiving logic (LocationListener, BroadcastReceiver, periodic polling).<br>
 * - Provides isBetterLocation() algorithm based on Android's official LocationManager documentation for intelligent location filtering.<br>
 * - Supports multi-provider registration (GPS, NETWORK, PASSIVE, FUSED) with flexible updateCycleTime and minDistance parameters.<br><br>
 * - 개별 제공자 활성 상태(GPS/NETWORK/PASSIVE/FUSED)는 MutableStateFlow를 사용하여 자동 중복 필터링(distinctUntilChanged 동작)을 활성화합니다.<br>
 * - 통합 위치 업데이트 이벤트 스트림(sfUpdate)은 SharedFlow를 사용하며 빠른 위치 변경 시 이벤트 손실을 방지하기 위해 버퍼링(replay=1, extraBufferCapacity=8)합니다.<br>
 * - LocationListener(실시간 위치 업데이트) + BroadcastReceiver(제공자 상태 변경) + 주기적 폴링을 결합하여 포괄적인 모니터링을 제공합니다.<br>
 * - LocationStateReceiver를 사용하여 모든 수신 로직(LocationListener, BroadcastReceiver, 주기적 폴링)을 캡슐화합니다.<br>
 * - 지능적인 위치 필터링을 위해 Android 공식 LocationManager 문서 기반 isBetterLocation() 알고리즘을 제공합니다.<br>
 * - 유연한 updateCycleTime과 minDistance 파라미터로 다중 제공자 등록(GPS, NETWORK, PASSIVE, FUSED)을 지원합니다.<br>
 *
 * **Permission notice / 권한 안내:**<br>
 * - Requires both `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` runtime permissions.<br>
 * - This library enforces both permissions for registration and location queries.<br>
 * - Provider state queries (isGpsEnabled, isNetworkEnabled, etc.) work without permissions but location updates require runtime permissions.<br>
 * - Permission checks are automatically performed by BaseSystemService before calling LocationManager APIs.<br><br>
 * - `ACCESS_FINE_LOCATION`과 `ACCESS_COARSE_LOCATION` 런타임 권한이 모두 필요합니다.<br>
 * - 본 라이브러리는 등록 및 위치 조회 시 두 권한을 모두 강제 검증합니다.<br>
 * - 제공자 상태 조회(isGpsEnabled, isNetworkEnabled 등)는 권한 없이 작동하지만 위치 업데이트는 런타임 권한이 필요합니다.<br>
 * - BaseSystemService가 LocationManager API 호출 전 권한 검사를 자동으로 수행합니다.<br>
 *
 * **Important notes / 주의사항:**<br>
 * - Always call onDestroy() when the owning lifecycle ends to prevent memory leaks and battery drain.<br>
 * - The isBetterLocation() algorithm favors newer, more accurate locations within a reasonable time window (10 sec).<br>
 * - FUSED provider requires Google Play Services; fallback to GPS/NETWORK if unavailable.<br><br>
 * - 메모리 누수와 배터리 소모를 방지하기 위해 라이프사이클 종료 시 반드시 onDestroy()를 호출하세요.<br>
 * - isBetterLocation() 알고리즘은 합리적인 시간 창(10 초) 내에서 더 새롭고 정확한 위치를 선호합니다.<br>
 * - FUSED 제공자는 Google Play Services가 필요하며, 없으면 GPS/NETWORK로 대체하세요.<br>
 *
 * **Usage / 사용법:**<br>
 * 1. Create LocationStateInfo with application context.<br>
 * 2. Call registerStart(scope, provider, updateCycleTime, minDistanceM) to start location updates.<br>
 * 3. Collect sfUpdate flow to receive LocationStateEvent events.<br>
 * 4. Call onDestroy() upon complete shutdown to release resources.<br><br>
 * 1. application context로 LocationStateInfo를 생성합니다.<br>
 * 2. registerStart(scope, provider, updateCycleTime, minDistanceM)를 호출하여 위치 업데이트를 시작합니다.<br>
 * 3. sfUpdate 플로우를 수집하여 LocationStateEvent 이벤트를 수신합니다.<br>
 * 4. 완전 종료 시 onDestroy()를 호출하여 리소스를 해제하세요.<br>
 *
 * @param context context.<br><br>
 *                컨텍스트.<br>
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
     * Manages event emission to SharedFlow and caches location metrics in StateFlows.<br><br>
     * SharedFlow로 이벤트를 발행하고 StateFlow에 위치 메트릭을 캐싱합니다.<br>
     */
    private val sender = LocationStateEmitter()

    /**
     * Manages location updates via LocationListener, BroadcastReceiver, and periodic polling.<br><br>
     * LocationListener, BroadcastReceiver, 주기적 폴링을 통한 위치 업데이트를 관리합니다.<br>
     */
    private val receiver = LocationStateReceiver(context, locationManager) {
        sender.updateLocationInfo(
            LocationStateData(
                location = getLocation(),
                isGpsEnabled = isGpsEnabled(),
                isNetworkEnabled = isNetworkEnabled(),
                isPassiveEnabled = isPassiveEnabled(),
                isFusedEnabled = checkSdkVersion(
                    Build.VERSION_CODES.S,
                    positiveWork = { isFusedEnabled() },
                    negativeWork = { null }
                )
            )
        )
    }

    /**
     * Event-only stream for location changes.<br><br>
     * 위치 변경을 이벤트 단위로 전달하는 스트림입니다.<br>
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
    public val sfUpdate: SharedFlow<LocationStateEvent> = sender.getSfUpdate()

    /**
     * Registers and starts location updates with the specified parameters.<br><br>
     * 지정한 매개변수로 위치 업데이트를 등록하고 시작합니다.<br>
     *
     * @param coroutineScope The coroutine scope for managing location updates.<br><br>
     *                       위치 업데이트를 관리할 코루틴 스코프입니다.<br>
     *
     * @param locationProvider The location provider to use (GPS, NETWORK, etc.).<br><br>
     *                         사용할 위치 제공자(GPS, NETWORK 등)입니다.<br>
     *
     * @param updateCycleTime Interval (in milliseconds) to periodically check location values and emit updates when changes are detected.<br>
     *                        Must be greater than or equal to `MIN_UPDATE_CYCLE_TIME`; otherwise IllegalArgumentException is thrown.<br>
     *                        LocationListener provides real-time updates for location changes,<br>
     *                        This polling does not guarantee new GPS updates; it only checks for refreshed location/provider state on a regular interval.<br>
     *                        - 2000ms (default): Recommended for most cases - fast updates, moderate battery usage.<br>
     *                        - 10000ms: Slower updates, lower battery consumption.<br>
     *                        - 60000ms: Very slow updates, minimal battery impact.<br><br>
     *                        일정 간격(밀리초)으로 위치 값을 확인하고 변경 시 업데이트를 발행하는 주기입니다.<br>
     *                        `MIN_UPDATE_CYCLE_TIME` 이상이어야 하며, 미만이면 IllegalArgumentException이 발생합니다.<br>
     *                        LocationListener가 위치 변경에 대한 실시간 업데이트를 제공하지만,<br>
     *                        이 폴링은 새로운 GPS 업데이트를 보장하지 않으며, 일정 주기로 위치/제공자 상태 갱신 여부를 확인하기 위한 것입니다.<br>
     *                        - 2000ms (기본값): 대부분의 경우 권장 - 빠른 업데이트, 적당한 배터리 사용.<br>
     *                        - 10000ms: 느린 업데이트, 낮은 배터리 소비.<br>
     *                        - 60000ms: 매우 느린 업데이트, 최소 배터리 영향.<br>
     *
     * @param minDistanceM Minimum distance between location updates in meters.<br><br>
     *                     위치 업데이트 최소 거리(미터)입니다.<br>
     *
     * @throws IllegalArgumentException if updateCycleTime is less than `MIN_UPDATE_CYCLE_TIME`.<br><br>
     *                                  updateCycleTime이 `MIN_UPDATE_CYCLE_TIME`보다 작으면 IllegalArgumentException이 발생합니다.<br>
     */
    @RequiresPermission(allOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    public fun registerStart(
        coroutineScope: CoroutineScope,
        locationProvider: String,
        updateCycleTime: Long = DEFAULT_UPDATE_CYCLE_TIME,
        minDistanceM: Float = DEFAULT_UPDATE_CYCLE_DISTANCE
    ): Boolean {
        require(updateCycleTime >= MIN_UPDATE_CYCLE_TIME) { "updateCycleTime must be greater than or equal to $MIN_UPDATE_CYCLE_TIME" }
        require(minDistanceM >= MIN_UPDATE_CYCLE_DISTANCE) { "minDistanceM must be greater than or equal to $MIN_UPDATE_CYCLE_DISTANCE" }

        val isRegister = receiver.registerReceiver()
        if (!isRegister) {
            Logx.e("LocationStateInfo: Receiver not registered!!.")
            return false
        }

        val isLocationListenerRegister = receiver.registerLocationListener(locationProvider, updateCycleTime, minDistanceM)
        if (!isLocationListenerRegister) {
            Logx.e("LocationStateInfo: LocationListener not registered!!.")
            receiver.destroy()
            return false
        }

        val result = tryCatchSystemManager(false) {
            receiver.updateStart(coroutineScope, updateCycleTime) {
                receiver.getCoroutineScope()?.let { sender.setupDataFlows(it) }
            }
        }

        if (!result) {
            receiver.destroy()
            Logx.e("LocationStateInfo: updateStart() failed!!.")
        }
        return result
    }

    /**
     * Cleans up resources on destruction.<br><br>
     * 파괴 시 리소스를 정리합니다.<br>
     */
    public override fun onDestroy() {
        super.onDestroy()
        receiver.destroy()
    }

    /**
     * Checks if location services are enabled.<br><br>
     * 위치 서비스가 활성화되어 있는지 확인합니다.<br>
     *
     * @return `true` if location is enabled; `false` otherwise.<br><br>
     *         위치가 활성화되어 있으면 `true`, 아니면 `false`입니다.<br>
     */
    public fun isLocationEnabled(): Boolean = safeCatch(false) {
        locationManager.isLocationEnabled
    }

    /**
     * Checks if GPS provider is enabled.<br><br>
     * GPS 제공자가 활성화되어 있는지 확인합니다.<br>
     *
     * @return `true` if GPS is enabled; `false` otherwise.<br><br>
     *         GPS가 활성화되어 있으면 `true`, 아니면 `false`입니다.<br>
     */
    public fun isGpsEnabled(): Boolean = safeCatch(false) {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /**
     * Checks if Network provider is enabled.<br><br>
     * 네트워크 제공자가 활성화되어 있는지 확인합니다.<br>
     *
     * @return `true` if the Network provider is enabled; `false` otherwise.<br><br>
     *         네트워크 제공자가 활성화되어 있으면 `true`, 아니면 `false`입니다.<br>
     */
    public fun isNetworkEnabled(): Boolean = safeCatch(false) {
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Checks if Passive provider is enabled.<br><br>
     * Passive 제공자가 활성화되어 있는지 확인합니다.<br>
     *
     * @return `true` if the Passive provider is enabled; `false` otherwise.<br><br>
     *         Passive 제공자가 활성화되어 있으면 `true`, 아니면 `false`입니다.<br>
     */
    public fun isPassiveEnabled(): Boolean = safeCatch(false) {
        locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)
    }

    /**
     * Checks if Fused provider is enabled (API 31+).<br><br>
     * Fused 제공자가 활성화되어 있는지 확인합니다(API 31+).<br>
     *
     * @return `true` if the Fused provider is enabled; `false` otherwise.<br><br>
     *         Fused 제공자가 활성화되어 있으면 `true`, 아니면 `false`입니다.<br>
     */
    @RequiresApi(Build.VERSION_CODES.S)
    public fun isFusedEnabled(): Boolean = safeCatch(false) {
        locationManager.isProviderEnabled(LocationManager.FUSED_PROVIDER)
    }

    /**
     * Checks if any location provider is enabled.<br><br>
     * 하나라도 활성화된 위치 제공자가 있는지 확인합니다.<br>
     *
     * @return `true` if any provider is enabled; `false` otherwise.<br><br>
     *         하나라도 제공자가 활성화되어 있으면 `true`, 아니면 `false`입니다.<br>
     */
    public fun isAnyEnabled(): Boolean = checkSdkVersion(
        Build.VERSION_CODES.S,
        positiveWork = { (isLocationEnabled() || isGpsEnabled() || isNetworkEnabled() || isPassiveEnabled() || isFusedEnabled()) },
        negativeWork = { (isLocationEnabled() || isGpsEnabled() || isNetworkEnabled() || isPassiveEnabled()) },
    )

    /**
     * Gets the last known location from the location provider.<br><br>
     * 위치 제공자로부터 마지막으로 알려진 위치를 가져옵니다.<br>
     *
     * @return The last known Location, or `null` if unavailable.<br><br>
     *         마지막으로 알려진 Location이며, 없으면 `null`을 반환합니다.<br>
     */
    @RequiresPermission(allOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    public fun getLocation(): Location? {
        if (!isAnyEnabled()) {
            var logcatData =
                "can not find location!, isLocationEnabled ${isLocationEnabled()}, isGpsEnabled ${isGpsEnabled()}, isNetworkEnabled ${isNetworkEnabled()}, isPassiveEnabled ${isPassiveEnabled()}"
            logcatData += checkSdkVersion(Build.VERSION_CODES.S) { ", isFusedEnabled ${isFusedEnabled()}" } ?: ""
            Logx.e(logcatData)
            return null
        }

        if (!context.hasPermissions(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)) {
            Logx.d("ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION permission is not granted")
            return null
        }

        return getBestLastKnownLocation()
    }

    /**
     * Selects the best last known location from available providers.<br><br>
     * Chooses among Fused/GPS/Network/Passive based on permission availability and provider enabled state,
     * and prefers newer/more accurate values.<br><br>
     * 사용 가능한 제공자 중에서 가장 적절한 최근 위치를 선택합니다.<br>
     * 권한 여부와 제공자 활성 상태를 고려해 Fused/GPS/Network/Passive를 비교하고,
     * 더 새로운/정확한 값을 우선합니다.<br>
     *
     * @return The best last known location or `null` if none is available; no logging is performed here.<br><br>
     *         사용 가능한 최근 위치가 없으면 `null`을 반환하며, 여기서는 로그를 남기지 않습니다.<br>
     */
    @RequiresPermission(allOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    private fun getBestLastKnownLocation(): Location? {
        val hasFine = context.hasPermissions(ACCESS_FINE_LOCATION)
        val hasCoarse = context.hasPermissions(ACCESS_COARSE_LOCATION)

        // 최적화: 최대 4개(Fused, GPS, Network, Passive) 제공자를 고려해 초기 용량을 지정
        val providers = ArrayList<String>(4)

        // API 31+면 FUSED 우선 고려
        checkSdkVersion(Build.VERSION_CODES.S) {
            if (isFusedEnabled()) providers.add(LocationManager.FUSED_PROVIDER)
        }

        // 정밀 권한이 있으면 GPS
        if (hasFine && isGpsEnabled()) {
            providers.add(LocationManager.GPS_PROVIDER)
        }

        // 대략 권한이 있으면 NETWORK
        if (hasCoarse && isNetworkEnabled()) {
            providers.add(LocationManager.NETWORK_PROVIDER)
        }

        // PASSIVE는 권한에 영향 덜 받으므로 마지막 후보
        if (isPassiveEnabled()) {
            providers.add(LocationManager.PASSIVE_PROVIDER)
        }

        var best: Location? = null
        for (provider in providers) {
            val loc = locationManager.getLastKnownLocation(provider) ?: continue
            if (isBetterLocation(loc, best)) {
                best = loc
            }
        }
        return best
    }

    /**
     * Determines whether the new location is better than the current best.<br><br>
     * Compares recency and accuracy using the standard time/accuracy thresholds.<br><br>
     * 새 위치가 현재 최적 위치보다 나은지 판단합니다.<br>
     * 시간/정확도 기준을 사용해 최신성과 정확도를 비교합니다.<br>
     *
     * @param newLoc Candidate location to compare.<br><br>
     *               비교할 후보 위치입니다.<br>
     *
     * @param currentBest Current best location; may be `null`.<br><br>
     *                    현재 최적 위치이며 `null`일 수 있습니다.<br>
     * @return Returns `true` if newLoc is considered better; no logging is performed here.<br><br>
     *         newLoc가 더 낫다고 판단되면 `true`를 반환하며, 여기서는 로그를 남기지 않습니다.<br>
     */
    private fun isBetterLocation(newLoc: Location, currentBest: Location?): Boolean {
        if (currentBest == null) return true

        val timeDelta = newLoc.time - currentBest.time
        val isSignificantlyNewer = timeDelta > 10 * 1000L // 2 * 60 * 1000L
        val isSignificantlyOlder = timeDelta < -10 * 1000L // -2 * 60 * 1000L
        val isNewer = timeDelta > 0

        if (isSignificantlyNewer) return true
        if (isSignificantlyOlder) return false

        val accuracyDelta = (newLoc.accuracy - currentBest.accuracy).toInt()
        val isMoreAccurate = accuracyDelta < 0
        val isLessAccurate = accuracyDelta > 0
        val isSignificantlyLessAccurate = accuracyDelta > 200

        val isFromSameProvider = newLoc.provider == currentBest.provider

        return when {
            isMoreAccurate -> true
            isNewer && !isLessAccurate -> true
            isNewer && !isSignificantlyLessAccurate && isFromSameProvider -> true
            else -> false
        }
    }

    /**
     * Calculates the distance between two locations in meters.<br><br>
     * 두 위치 간의 거리를 미터 단위로 계산합니다.<br>
     *
     * @param fromLocation Starting location.<br><br>
     *                     시작 위치입니다.<br>
     *
     * @param toLocation Destination location.<br><br>
     *                   도착 위치입니다.<br>
     * @return Distance in meters.<br><br>
     *         미터 단위 거리입니다.<br>
     */
    public fun calculateDistance(fromLocation: Location, toLocation: Location): Float = fromLocation.distanceTo(toLocation)

    /**
     * Calculates the bearing between two locations in degrees.<br><br>
     * 두 위치 간의 방위각을 도 단위로 계산합니다.<br>
     *
     * @param fromLocation Starting location.<br><br>
     *                     시작 위치입니다.<br>
     *
     * @param toLocation Destination location.<br><br>
     *                   도착 위치입니다.<br>
     * @return Bearing in degrees.<br><br>
     *         도 단위 방위각입니다.<br>
     */
    public fun calculateBearing(fromLocation: Location, toLocation: Location): Float = fromLocation.bearingTo(toLocation)

    /**
     * Checks if two locations are within a specified radius.<br><br>
     * 두 위치가 지정한 반경 안에 있는지 확인합니다.<br>
     *
     * @param fromLocation First location.<br><br>
     *                     첫 번째 위치입니다.<br>
     *
     * @param toLocation Second location.<br><br>
     *                   두 번째 위치입니다.<br>
     *
     * @param radius Radius in meters.<br><br>
     *               미터 단위 반경입니다.<br>
     * @return `true` if locations are within radius; `false` otherwise.<br><br>
     *         반경 안에 있으면 `true`, 아니면 `false`입니다.<br>
     */
    public fun isLocationWithRadius(fromLocation: Location, toLocation: Location, radius: Float): Boolean =
        calculateDistance(fromLocation, toLocation) <= radius

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
}
