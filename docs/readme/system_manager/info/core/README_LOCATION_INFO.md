# Location Info vs Plain Android - Complete Comparison Guide
> **Location Info vs 순수 Android - 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_core` (UI-independent core module / UI 비의존 코어 모듈)
- **Package**: `kr.open.library.simple_ui.core.system_manager.info.location`

<br></br>

## Overview (개요)
Provides SharedFlow-based location tracking and location helper utilities.  
> SharedFlow 기반 위치 추적과 위치 헬퍼 유틸을 제공합니다.

<br></br>

## At a Glance (한눈 비교)
- **Real-time Updates:** `registerStart(coroutineScope, locationProvider, updateCycleTime, minDistanceM)` - SharedFlow-based location tracking
  - `coroutineScope` - Coroutine scope (Lifecycle integrated) (코루틴 스코프 (Lifecycle과 연동))
  - `locationProvider` - Location provider (GPS_PROVIDER, NETWORK_PROVIDER, PASSIVE_PROVIDER, FUSED_PROVIDER, etc.) (위치 제공자)
  - `updateCycleTime` - Update cycle time in milliseconds (default: 5000ms) (밀리초 단위 업데이트 주기 시간 (기본값: 5000ms))
    - 5000ms (default): Recommended for most cases - fast updates, moderate battery usage. (대부분의 경우 권장 - 빠른 업데이트, 적당한 배터리 사용)
    - 10000ms: Slower updates, lower battery consumption. (느린 업데이트, 낮은 배터리 소비)
    - 60000ms: Very slow updates, minimal battery impact. (매우 느린 업데이트, 최소 배터리 영향)
    - POLLING_DISABLED_UPDATE_CYCLE_TIME: Low-power mode (polling disabled, one initial sync) (alias: DISABLE_UPDATE_CYCLE_TIME) (저전력 모드: 폴링 비활성 + 초기 1회 동기화, DISABLE_UPDATE_CYCLE_TIME 별칭)
      - Note: In low-power mode, periodic polling is disabled, but LocationListener remains registered. (저전력 모드에서는 주기 폴링만 비활성화되며 LocationListener 등록은 유지됩니다.)
  - `minDistanceM` - Minimum movement distance (meters) (default: 2.0m) (최소 이동 거리 (미터) (기본값: 2.0m))
    - Minimum allowed value: 0.1m (최소 허용값: 0.1m)
  - Automatic LocationListener and BroadcastReceiver registration/unregistration (자동 LocationListener 및 BroadcastReceiver 등록/해제)
  - Re-calling `registerStart(...)` automatically re-registers LocationListener and reapplies provider/time/distance settings (재호출 시 LocationListener 자동 재등록 + provider/time/distance 설정 재적용)
- **Provider Status:** `isGpsEnabled()`, `isNetworkEnabled()`, `isPassiveEnabled()`, `isFusedEnabled()` (API 31+)
- **Extended Provider Status:**
  - `isLocationEnabled()` - Check whether system location service is enabled (시스템 위치 서비스 활성화 여부 확인)
  - `isAnyEnabled()` - Check if any Provider is enabled (includes Fused on API 31+) (모든 Provider 중 하나라도 활성화 확인 (API 31+에서는 Fused 포함))
- **Current Location:** `getLocation()` - Last known best location among available providers (사용 가능한 Provider 중 최적의 마지막 위치)
- **Distance Calculation:** `calculateDistance(from, to)` - Distance between two locations (meters) (두 위치 간 거리 (미터))
- **Bearing Calculation:** `calculateBearing(from, to)` - Bearing between two locations (degrees) (두 위치 간 방향 (도))
- **Radius Check:** `isLocationWithRadius(from, to, radius)` - Check location within specific radius (특정 반경 내 위치 확인)
- **Location Save/Load:**
  - `saveApplyLocation(location)` - Save location to SharedPreferences (immediate apply) (SharedPreferences에 위치 저장 (즉시 적용))
  - `loadLocation()` - Load saved location (저장된 위치 로드)
  - `removeLocation()` - Delete saved location (저장된 위치 삭제)
- **Lifecycle Management:**
  - `unRegister()` - Stop updates immediately and keep the instance reusable (즉시 업데이트 중지, 인스턴스 재사용 가능)
  - `onDestroy()` - Manual cleanup required to release resources (리소스 해제를 위해 수동 정리 필요)
- **Smart Location Filtering:** Intelligent algorithm filters out inaccurate or stale coordinates. It prioritizes recent data within **10 seconds** and compares accuracy (AccuracyDelta) to select a better last-known location.
  - **스마트 위치 필터링:** 부정확하거나 오래된 좌표를 걸러내는 지능형 알고리즘 탑재. **10초** 이내의 최신 데이터를 우선하고 정확도(AccuracyDelta)를 비교해 더 나은 마지막 위치 선택에 도움을 줍니다.
- **Stable Polling Mechanism:** Periodic polling (default 5000ms) runs alongside event listeners to double-check system state and reduce the chance of missing provider state changes.
  - **안정적 폴링 메커니즘:** 이벤트 리스너와 함께 주기적 폴링(기본 5000ms)으로 시스템 상태를 이중 확인하여 Provider 상태 변경 누락 가능성을 줄입니다.
  - **저전력 모드:** 필요 시 `POLLING_DISABLED_UPDATE_CYCLE_TIME`(별칭: `DISABLE_UPDATE_CYCLE_TIME`)로 폴링을 비활성화할 수 있습니다.
- **LocationStateEvent:** 5 event types (OnLocationChanged, OnGpsEnabled, OnNetworkEnabled, OnPassiveEnabled, OnFusedEnabled) (5가지 이벤트 타입)

<br></br>

## Why It Matters (중요한 이유)
**Issues**
- Manual LocationListener implementation and registration
- Manual permission check repetition
- Manual Provider status change handling
- Must set location update parameters directly
- Manual Lifecycle management
- Memory leak risk
> - LocationListener 수동 구현 및 등록
> - 권한 체크 수동 반복
> - Provider 상태 변경 수동 처리
> - 위치 업데이트 파라미터 직접 설정
> - Lifecycle 관리 수동
> - 메모리 누수 위험

**Advantages**
- **Dramatically simplified** (Complex Listener → One line registration)
- Automatic LocationListener management
- SharedFlow-based reactive updates
- 5 type-safe events (Location, GPS, Network, Passive, Fused)
- Automatic Provider status tracking
- Distance/bearing calculation helpers provided
- Explicit stop/destroy APIs (`unRegister()`, `onDestroy()`)
> - **대폭 간소화** (복잡한 Listener → 한 줄 등록)
> - LocationListener 자동 관리
> - SharedFlow 기반 반응형 업데이트
> - 5가지 타입 안전한 이벤트 (위치, GPS, Network, Passive, Fused)
> - Provider 상태 자동 추적
> - 거리/방향 계산 헬퍼 제공
> - 명시적 중지/정리 API 제공 (`unRegister()`, `onDestroy()`)

<br></br>

## Plain Android (순수 Android 방식)
```kotlin
// Traditional Location tracking method (기존의 Location 추적 방법)
class LocationTracker(private val context: Context) {

    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private var isGpsEnabled = false
    private var isNetworkEnabled = false

    // 1. Manual LocationListener implementation (LocationListener 수동 구현)
    fun startTracking() {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // 2. Manual permission check (권한 체크 (수동))
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Handle no permission (권한 없음 처리)
            return
        }

        // 3. LocationListener implementation (LocationListener 구현)
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // Handle location updates (위치 업데이트 처리)
                val latitude = location.latitude
                val longitude = location.longitude
                updateLocation(latitude, longitude)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                // Handle Provider status change (Provider 상태 변경 처리)
            }

            override fun onProviderEnabled(provider: String) {
                when (provider) {
                    LocationManager.GPS_PROVIDER -> isGpsEnabled = true
                    LocationManager.NETWORK_PROVIDER -> isNetworkEnabled = true
                }
            }

            override fun onProviderDisabled(provider: String) {
                when (provider) {
                    LocationManager.GPS_PROVIDER -> isGpsEnabled = false
                    LocationManager.NETWORK_PROVIDER -> isNetworkEnabled = false
                }
            }
        }

        // 4. Request location updates (Provider selection, parameter setup)
        // (위치 업데이트 요청 (Provider 선택, 파라미터 설정))
        try {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,  // minTime
                10f,    // minDistance
                locationListener!!
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // 5. Query last location (manual) (마지막 위치 조회 (수동))
    fun getLastLocation(): Location? {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        return locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    }

    // 6. Manual cleanup handling (정리 작업 수동 처리)
    fun stopTracking() {
        locationListener?.let {
            locationManager?.removeUpdates(it)
        }
        locationListener = null
        locationManager = null
    }

    private fun updateLocation(lat: Double, lng: Double) {
        // UI update logic (must implement yourself) (UI 업데이트 로직 (각자 구현해야 함))
    }
}
```

<br></br>

## Simple UI Approach (Simple UI 방식)
```kotlin
// Simple Location tracking - SharedFlow based (간단한 Location 추적 - SharedFlow 기반)
class MainActivity : BaseDataBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val locationInfo by lazy { LocationStateInfo(this) }

    override fun onCreate(binding: ActivityMainBinding, savedInstanceState: Bundle?) {
        // Permission request (Simple UI auto handling) (권한 요청 (Simple UI 자동 처리))
        requestPermissions(
            permissions = listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            onDeniedResult = { deniedResults ->
                if (deniedResults.isEmpty()) {
                    startLocationTracking()
                }
            },
        )
    }

    private fun startLocationTracking() {
        // 1. Start location tracking - One line (위치 추적 시작 - 한 줄)
        locationInfo.registerStart(
            coroutineScope = lifecycleScope,
            locationProvider = LocationManager.GPS_PROVIDER,
            // Low-power mode (polling disabled) (저전력 모드 - 폴링 비활성)
            // Alias: POLLING_DISABLED_UPDATE_CYCLE_TIME (별칭)
            updateCycleTime = DISABLE_UPDATE_CYCLE_TIME,
            minDistanceM = 10f
        )

        // 2. Query initial values - Simple getters (초기 값 조회 - 간단한 getter)
        val lastLocation = locationInfo.getLocation()
        val isGpsEnabled = locationInfo.isGpsEnabled()

        // 3. SharedFlow-based real-time updates - Auto collect (SharedFlow 기반 실시간 업데이트 - 자동 collect)
        lifecycleScope.launch {
            locationInfo.sfUpdate.collect { event ->
                when (event) {
                    is LocationStateEvent.OnLocationChanged -> {
                        val location = event.location
                        updateLocation(location?.latitude, location?.longitude)
                    }
                    is LocationStateEvent.OnGpsEnabled -> {
                        updateGpsStatus(event.isEnabled)
                    }
                    is LocationStateEvent.OnNetworkEnabled -> {
                        updateNetworkStatus(event.isEnabled)
                    }
                    is LocationStateEvent.OnFusedEnabled -> {
                        // API 31+ Fused Provider auto support (API 31+ Fused Provider 자동 지원)
                        updateFusedStatus(event.isEnabled)
                    }
                    else -> {}
                }
            }
        }

        // 4. Distance calculation helper methods (거리 계산 헬퍼 메서드)
        val distance = locationInfo.calculateDistance(fromLocation, toLocation)
        val bearing = locationInfo.calculateBearing(fromLocation, toLocation)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Manual cleanup required - Call onDestroy() to release resources
        // 수동 정리 필요 - 리소스 해제를 위해 onDestroy() 호출
        locationInfo.onDestroy()
    }
}
```

<br></br>

## Permissions (권한)
See the permission guide for required permissions and policy.  
> 필수 권한과 정책은 권한 가이드를 참고하세요.

- [README_PERMISSION.md](../../../README_PERMISSION.md)

<br></br>

## Related Docs (관련 문서)
- Summary: [README_SERVICE_MANAGER_INFO.md](../README_SERVICE_MANAGER_INFO.md)
- Permission Guide: [README_PERMISSION.md](../../../README_PERMISSION.md)

<br></br>

