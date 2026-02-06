# System Service Manager Info vs Plain Android - Complete Comparison Guide
> **System Service Manager Info vs 순수 Android - 비교 가이드**

## 📦 Module Information (모듈 정보)
- **Module**: `simple_core` (UI-independent core module / UI 비의존 코어 모듈)
- **Package**: `kr.open.library.simple_ui.core.system_manager.info.*`

<br></br>

**"Simplify complex System Service information collection!"** See the immediate difference Simple UI Info makes compared to traditional Android System Service data retrieval.
> **"복잡한 System Service 정보 수집을 간단하게!"** 기존 Android System Service 정보 조회 대비 Simple UI Info가 주는 체감 차이를 한눈에 확인하세요.

<br>
</br>

## 🔎 At a Glance (한눈 비교)

| Category                          |                           Plain Android                            |                  Simple UI Info                  |            Impact             |
|:----------------------------------|:------------------------------------------------------------------:|:------------------------------------------------:|:-----------------------------:|
| **Battery Info**                  |      `BroadcastReceiver` + `IntentFilter` + Manual management      |      `BatteryStateInfo().registerStart()`        |   **SharedFlow automation**   |
| **Location Info**                 |     `LocationManager` + Permissions + Callback implementation      |      `LocationStateInfo().registerStart()`       | **Auto Provider management**  |
| **Display Info**                  |           SDK branching + WindowManager + DisplayMetrics           |     `DisplayInfo().getPhysicalScreenSize()`      |     **Auto SDK handling**     |
| **SIM Card Info**                 |        `SubscriptionManager` + Manual multi-SIM management         |         `SimInfo().getActiveSimCount()`          |   **Multi-SIM automation**    |
| **Telephony Info**                |        `TelephonyManager` + Manual Callback implementation         |       `TelephonyInfo().registerCallback()`       |  **Auto API compatibility**   |
| **Network Connectivity**          |          `ConnectivityManager` + Callback implementation           | `NetworkConnectivityInfo().isNetworkConnected()` | **Auto Transport detection**  |

**Key takeaway:** System Service Manager Info simplifies complex system information collection with a **Flow/Callback-based** architecture.
> **핵심:** System Service Manager Info는 복잡한 시스템 정보 수집을 **Flow/Callback 기반**으로 단순화합니다.

<br></br>

## 💡 Why It Matters (왜 중요한가)

### Flow/Callback-Based Reactive Architecture (Flow/Callback 기반 반응형 구조)
- **Real-time Updates:** Manual BroadcastReceiver management → Automatic Flow/Callback handling
- **Lifecycle Safety:** Coroutine scope integration prevents memory leaks
- **Event Type Separation:** Type-safe event handling with Sealed Classes
> - **실시간 업데이트**: BroadcastReceiver 수동 관리 → Flow/Callback 자동 처리
> - **Lifecycle 안전**: 코루틴 스코프 연동으로 메모리 누수 방지
> - **이벤트 타입 분리**: Sealed Class로 타입 안전한 이벤트 처리

<br>
</br>

### Automated Complex Configuration (복잡한 설정 자동화)
- **Automatic BroadcastReceiver Registration/Unregistration:** Auto-detect Battery, Location Provider changes
- **Automatic SDK Version Branching:** Display API (R+/below), Fused Provider (S+) auto-branching
- **Simplified Permission Handling:** Automatic required permission checks and safe exception handling
> - **BroadcastReceiver 자동 등록/해제**: Battery, Location Provider 변경 자동 감지
> - **SDK 버전 분기 자동 처리**: Display API (R 이상/이하), Fused Provider (S+) 자동 분기
> - **권한 처리 간소화**: 필수 권한 자동 체크 및 안전한 예외 처리

<br>
</br>

### Developer-Friendly Interface (개발자 친화적 API)
- **Intuitive Method Names:** `getCapacity()`, `getTemperature()`, `isGpsEnabled()`
- **Various Helper Methods:** `isCharging()`, `isHealthGood()`, `calculateDistance()`
- **Data Classes Provided:** NetworkConnectivitySummary, NetworkCapabilitiesData, etc.
> - **직관적 메서드 이름**: `getCapacity()`, `getTemperature()`, `isGpsEnabled()`
> - **다양한 헬퍼 메서드**: `isCharging()`, `isHealthGood()`, `calculateDistance()`
> - **데이터 클래스 제공**: NetworkConnectivitySummary, NetworkCapabilitiesData 등

<br></br>

## SystemManager VS StateInfo 


### #1: BatteryStateInfo

<details>
<summary><strong>Plain Android - Battery Information Collection (순수 Android - Battery 정보 수집)</strong></summary>

```kotlin
// Traditional Battery information collection method (기존의 Battery 정보 수집 방법)
class BatteryMonitor(private val context: Context) {

    private var batteryReceiver: BroadcastReceiver? = null
    private var batteryStatus: Intent? = null

    // 1. Manual BroadcastReceiver implementation (BroadcastReceiver 수동 구현)
    fun startMonitoring() {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_BATTERY_OKAY)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }

        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                batteryStatus = intent
                // Extract battery information (배터리 정보 추출)
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val capacity = level * 100 / scale

                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING

                val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                val chargingType = when (plugged) {
                    BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                    BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                    else -> "Unknown"
                }

                val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
                val tempCelsius = temperature / 10.0

                val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
                val voltageV = voltage / 1000.0

                val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                val healthStr = when (health) {
                    BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                    BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                    BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                    else -> "Unknown"
                }

                // UI update or callback invocation (must implement manually)
                // (UI 업데이트 또는 콜백 호출 (수동으로 구현해야 함))
                updateUI(capacity, isCharging, chargingType, tempCelsius, voltageV, healthStr)
            }
        }

        context.registerReceiver(batteryReceiver, intentFilter)
    }

    // 2. Manual BatteryManager additional information query (BatteryManager 추가 정보 수동 조회)
    fun getCurrentAmpere(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
    }

    // 3. Manual cleanup handling (정리 작업 수동 처리)
    fun stopMonitoring() {
        batteryReceiver?.let {
            context.unregisterReceiver(it)
        }
        batteryReceiver = null
        batteryStatus = null
    }

    private fun updateUI(capacity: Int, isCharging: Boolean, chargingType: String,
                         temp: Double, voltage: Double, health: String) {
        // UI update logic (must implement yourself) (UI 업데이트 로직 (각자 구현해야 함))
    }
}
```
**Issues:**
- Manual BroadcastReceiver registration and unregistration required
- Must add all IntentFilter Actions manually
- Must implement Battery information extraction logic directly
- Manual temperature/voltage unit conversion
- Must implement callback mechanism manually
- Manual Lifecycle management
- Memory leak risk
> **문제점:**
> - BroadcastReceiver 수동 등록 및 해제 필요
> - IntentFilter 모든 Action 직접 추가
> - Battery 정보 추출 로직 직접 구현
> - 온도/전압 단위 변환 수동 처리
> - 콜백 메커니즘 직접 구현
> - Lifecycle 관리 수동
> - 메모리 누수 위험

<br></br>
</details>

<details>
<summary><strong>Simple UI - Battery State Info</strong></summary>

```kotlin
// Simple Battery information collection - SharedFlow based (간단한 Battery 정보 수집 - SharedFlow 기반)
class MainActivity : BaseDataBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val batteryInfo by lazy { BatteryStateInfo(this) }

    override fun onCreate(binding: ActivityMainBinding, savedInstanceState: Bundle?) {
        // 1. Start battery monitoring with default update cycle (2000ms)
        // (기본 업데이트 주기로 배터리 모니터링 시작 (2000ms))
        val success = batteryInfo.registerStart(lifecycleScope)
        if (!success) {
            Log.e("Battery", "Failed to start battery monitoring (배터리 모니터링 시작 실패)")
            return
        }

        // Or use custom update cycle (10000ms) (또는 커스텀 업데이트 주기 사용 (10000ms))
        // val success = batteryInfo.registerStart(lifecycleScope, updateCycleTime = 10000L)

        // 2. Query initial values - Simple getters (초기 값 조회 - 간단한 getter)
        val capacity = batteryInfo.getCapacity()
        val temp = batteryInfo.getTemperature()
        val voltage = batteryInfo.getVoltage()
        val health = batteryInfo.getCurrentHealthStr()

        // 3. SharedFlow-based real-time updates - Auto collect (SharedFlow 기반 실시간 업데이트 - 자동 collect)
        lifecycleScope.launch {
            batteryInfo.sfUpdate.collect { event ->
                when (event) {
                    is BatteryStateEvent.OnCapacity ->
                        updateCapacity(event.percent)
                    is BatteryStateEvent.OnTemperature ->
                        updateTemperature(event.temperature)
                    is BatteryStateEvent.OnVoltage ->
                        updateVoltage(event.voltage)
                    is BatteryStateEvent.OnCurrentAmpere ->
                        updateCurrent(event.current)
                    // 11 event types supported (11가지 이벤트 타입 지원)
                    else -> {}
                }
            }
        }
    }
    // Auto cleanup in onDestroy() - internally calls unRegister()
    // (onDestroy()에서 자동 정리 - 내부적으로 unRegister() 호출)
}

private fun updateCapacity(percent: Int) {
    // UI update logic (UI 업데이트 로직)
}

private fun updateTemperature(temperature: Double) {
    // UI update logic (UI 업데이트 로직)
}

private fun updateVoltage(voltage: Double) {
    // UI update logic (UI 업데이트 로직)
}

private fun updateCurrent(current: Int) {
    // UI update logic (UI 업데이트 로직)
}
```
**Advantages:**
- **Dramatically simplified** (Complex Receiver → One line registration)
- Automatic BroadcastReceiver management
- SharedFlow-based reactive updates
- 11 type-safe events
- Automatic Lifecycle cleanup
> **장점:**
> - **대폭 간소화** (복잡한 Receiver → 한 줄 등록)
> - BroadcastReceiver 자동 관리
> - SharedFlow 기반 반응형 업데이트
> - 11가지 타입 안전한 이벤트
> - Lifecycle 자동 정리
</details>

<br>
</br>

### #2: LocationStateInfo

<details>
<summary><strong>Plain Android - Manual Location Tracking (순수 Android - Location 수동 추적)</strong></summary>

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
**Issues:**
- Manual LocationListener implementation and registration
- Manual permission check repetition
- Manual Provider status change handling
- Must set location update parameters directly
- Manual Lifecycle management
- Memory leak risk
> **문제점:**
> - LocationListener 수동 구현 및 등록
> - 권한 체크 수동 반복
> - Provider 상태 변경 수동 처리
> - 위치 업데이트 파라미터 직접 설정
> - Lifecycle 관리 수동
> - 메모리 누수 위험

<br></br>
</details>

<details>
<summary><strong>Simple UI - Location State Info</strong></summary>

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
            updateCycleTime = 1000L,
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
**Advantages:**
- **Dramatically simplified** (Complex Listener → One line registration)
- Automatic LocationListener management
- SharedFlow-based reactive updates
- 5 type-safe events (Location, GPS, Network, Passive, Fused)
- Automatic Provider status tracking
- Distance/bearing calculation helpers provided
- Explicit stop/destroy APIs (`unRegister()`, `onDestroy()`)
> **장점:**
> - **대폭 간소화** (복잡한 Listener → 한 줄 등록)
> - LocationListener 자동 관리
> - SharedFlow 기반 반응형 업데이트
> - 5가지 타입 안전한 이벤트 (위치, GPS, Network, Passive, Fused)
> - Provider 상태 자동 추적
> - 거리/방향 계산 헬퍼 제공
> - 명시적 중지/정리 API 제공 (`unRegister()`, `onDestroy()`)
</details>

<br>
</br>

### #3: DisplayInfo

<details>
<summary><strong>Plain Android - Manual Display Query (순수 Android - Display 수동 조회)</strong></summary>

```kotlin
// Traditional Display information query method (기존의 Display 정보 조회 방법)
class DisplayHelper(private val context: Context) {

    // 1. Manual SDK version branching (SDK 버전별 분기 처리 (수동))
    fun getFullScreenSize(): Point {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android R (API 30) and above (Android R (API 30) 이상)
            val windowMetrics = windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            point.x = bounds.width()
            point.y = bounds.height()
        } else {
            // Below Android R (Android R 미만)
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getRealSize(point)
        }

        return point
    }

    // 2. Available screen size (manual calculation) (사용 가능 화면 크기 (수동 계산))
    fun getAvailableScreenSize(): Point {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            val bounds = windowMetrics.bounds
            point.x = bounds.width()
            point.y = bounds.height() - insets.top - insets.bottom
        } else {
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getSize(point)
        }

        return point
    }

    // 3. Query status bar height (manual Resources access) (상태바 높이 조회 (Resources 수동 접근))
    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier(
            "status_bar_height",
            "dimen",
            "android"
        )
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    // 4. Query navigation bar height (manual Resources access) (네비게이션바 높이 조회 (Resources 수동 접근))
    fun getNavigationBarHeight(): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier(
            "navigation_bar_height",
            "dimen",
            "android"
        )
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}
```
**Issues:**
- Manual SDK version branching
- Direct use of Deprecated API
- Manual Resources ID query
- Complex Insets calculation
- Code duplication (repeated version branching)
> **문제점:**
> - SDK 버전별 분기 수동 처리
> - Deprecated API 직접 사용
> - Resources ID 수동 조회
> - 복잡한 Insets 계산
> - 코드 중복 (버전별 분기 반복)

<br></br>
</details>

<details>
<summary><strong>Simple UI - Display Info</strong></summary>

```kotlin
// Simple Display information query - Auto SDK handling (간단한 Display 정보 조회 - SDK 자동 처리)
class MainActivity : BaseDataBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val displayInfo by lazy { getDisplayInfo(this) }

    override fun onCreate(binding: ActivityMainBinding, savedInstanceState: Bundle?) {
        // 1. Physical screen size (auto SDK branching) (물리적 화면 크기 (SDK 자동 분기))
        val physicalSize = displayInfo.getPhysicalScreenSize()
        Log.d("Display", "Physical: ${physicalSize.width} x ${physicalSize.height}") // (물리적)

        // 2. App window size (exclude bars automatically, supports multi-window)
        // (앱 윈도우 크기 (상단/하단 바 자동 제외, 멀티윈도우 지원))
        val windowSize = displayInfo.getAppWindowSize(this)
        windowSize?.let {
            Log.d("Display", "Window: ${it.width} x ${it.height}") // (윈도우)
        }

        // 3. Status bar size (상태바 크기)
        val statusBarSize = displayInfo.getStatusBarSize()
        statusBarSize?.let {
            Log.d("Display", "Status bar: ${it.width} x ${it.height}") // (상태바)
        }

        // 4. Navigation bar size (네비게이션바 크기)
        val navBarSize = displayInfo.getNavigationBarSize()
        navBarSize?.let {
            Log.d("Display", "Navigation bar: ${it.width} x ${it.height}") // (네비게이션바)
        }

        // 5. Multi-window mode check (멀티윈도우 모드 확인)
        val isMultiWindow = displayInfo.isInMultiWindowMode(this)
        Log.d("Display", "Multi-window: $isMultiWindow")
    }
}
```
**Advantages:**
- **Dramatically simplified** (Auto SDK branching)
- Context-based architecture (no Activity required for construction)
- Automatic Android R (API 30) branching
- Automatic Deprecated API avoidance
- Automatic Resources query
- Automatic Insets calculation
- DisplayInfoSize data class for structured size information
- Multi-window mode support
- Optional Activity parameter for methods requiring it
> **장점:**
> - **대폭 간소화** (SDK 분기 자동 처리)
> - Context 기반 아키텍처 (생성 시 Activity 불필요)
> - Android R (API 30) 자동 분기
> - Deprecated API 자동 회피
> - Resources 자동 조회
> - Insets 자동 계산
> - DisplayInfoSize 데이터 클래스로 구조화된 크기 정보 제공
> - 멀티윈도우 모드 지원
> - 필요한 메서드에만 선택적 Activity 파라미터 사용
</details>

<br>
</br>

## Core Advantages of System Service Manager Info

### 1. **Flow/Callback-Based Reactive Architecture (Flow/Callback 기반 반응형 구조)**
- Battery: BroadcastReceiver + polling → SharedFlow 이벤트 스트림 (`sfUpdate`)
- Location: LocationListener + provider 변화 → SharedFlow 이벤트 스트림 (`sfUpdate`)
- Telephony: TelephonyCallback → StateFlow + 콜백
- Network: ConnectivityManager 콜백 (registerNetworkCallback / registerDefaultNetworkCallback)
- Sealed Class type safety

<br>
</br>

### 2. **Automatic SDK Version Handling (SDK 버전 자동 처리)**
- Display: Automatic Android SDK branching (Android SDK 자동 분기)
- Location: Automatic Fused Provider support (Fused Provider 자동 지원)
- Developers don't need to worry! (개발자는 신경 쓸 필요 없음!)

<br>
</br>

### 3. **Lifecycle Management (Lifecycle 관리)**
- Manual onDestroy() invocation required (onDestroy() 수동 호출 필요)
- Resource cleanup through onDestroy() (onDestroy()를 통한 리소스 정리)
- Memory leak prevention (메모리 누수 방지)

<br>
</br>

## See Real Implementation Examples (실제 구현 예제보기)

**Example Code:**

System Service Manager Info : [ServiceManagerInfoActivity.kt](../../app/src/main/java/kr/open/library/simpleui_xml/system_service_manager/info/ServiceManagerInfoActivity.kt)
> - Run the app to see actual implementation examples! (실제로 앱을 구동 시켜서 실제 구현 예제를 확인해 보세요!)

<br>
</br>

## 🎯 Available Info List

**System Service Manager Info** provides 6 core system information types:
<br></br>
**System Service Manager Info**는 6가지 핵심 시스템 정보를 제공합니다:

### **Battery State Info** - Battery Status Information
- **Real-time Updates:** `registerStart(coroutine: CoroutineScope, updateCycleTime: Long = 2000L): Boolean` - SharedFlow-based event updates
  - `coroutineScope` - Coroutine scope (Lifecycle integrated) (코루틴 스코프 (Lifecycle과 연동))
  - `updateCycleTime` - Update cycle in milliseconds (default: 2000ms) (밀리초 단위 업데이트 주기 (기본값: 2000ms))
    - 2000ms (default): Recommended for most cases - fast updates, moderate battery usage. (대부분의 경우 권장 - 빠른 업데이트, 적당한 배터리 사용)
    - 10000ms: Slower updates, lower battery consumption. (느린 업데이트, 낮은 배터리 소비)
    - 60000ms: Very slow updates, minimal battery impact. (매우 느린 업데이트, 최소 배터리 영향)
  - **Returns**: `true` if registration and update start succeeded, `false` otherwise (등록 및 업데이트 시작 성공 시 `true`, 실패 시 `false`)
  - Automatic BroadcastReceiver registration/unregistration (자동 BroadcastReceiver 등록/해제)
- **Capacity Info:** `getCapacity()` - Battery level (0~100%) (배터리 잔량 (0~100%))
- **Current Info:** `getCurrentAmpere()`, `getCurrentAverageAmpere()` - Instant/average current (microamperes) (순간/평균 전류 (마이크로암페어))
- **Charging Status:** `isCharging()`, `isDischarging()`, `isFull()`, `isNotCharging()` - Check charging status (충전 상태 확인)
- **Charging Type:** `isChargingUsb()`, `isChargingAc()`, `isChargingWireless()`, `isChargingDock()` (API 33+) - Check charging method (충전 방식 확인)
- **Charging Type String:** `getChargePlugStr()` - Return charging type string (USB, AC, WIRELESS, DOCK, UNKNOWN) (충전 타입 문자열 반환)
- **Battery Health:** `isHealthGood()`, `isHealthCold()`, `isHealthDead()`, `isHealthOverVoltage()` - Check battery health (배터리 상태 확인)
- **Health Status String:** `getCurrentHealthStr()`, `getHealthStr(healthType)` - Convert battery health to string (배터리 상태 문자열 변환)
- **Temperature/Voltage:** `getTemperature()` - Battery temperature (Celsius) (배터리 온도 (섭씨)), `getVoltage()` - Battery voltage (Volts) (배터리 전압 (볼트))
- **Total Capacity:** `getTotalCapacity()` - Total battery capacity (mAh) (배터리 총 용량 (mAh))
- **Battery Technology:** `getTechnology()` - Battery technology info (Li-ion, Li-poly, etc.) (배터리 기술 정보 (Li-ion, Li-poly 등))
- **Lifecycle Management:**
  - `onDestroy()` - Automatic cleanup (internally calls unRegister()) (자동 정리 (내부적으로 unRegister() 호출))
  - `unRegister()` - Manual early unregistration if needed before destruction (파괴 전에 조기 해제가 필요한 경우 수동 호출)
- **Error Handling:** `BATTERY_ERROR_VALUE = Integer.MIN_VALUE` - Return value on error (오류 시 반환값)
- **BatteryStateEvent:** 11 event types (OnCapacity, OnTemperature, OnVoltage, OnCurrentAmpere, OnCurrentAverageAmpere, OnChargeStatus, OnChargePlug, OnHealth, OnChargeCounter, OnEnergyCounter, OnPresent) (11가지 이벤트 타입)

<br>
</br>

### **Location State Info** - Location Status Information
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

<br>
</br>

### **Display Info** - Display Information
- **Physical Screen Size:** `getPhysicalScreenSize()` - Physical screen size (물리적 화면 크기)
- **App Window Size:** `getAppWindowSize(activity?)` - App window size excluding bars, supports multi-window (상태바/네비게이션바 제외한 앱 윈도우 크기, 멀티윈도우 지원)
- **Status Bar Size:** `getStatusBarSize()` - Status bar size as DisplayInfoSize (상태바 크기 (DisplayInfoSize))
- **Navigation Bar Size:** `getNavigationBarSize()` - Navigation bar size as DisplayInfoSize (네비게이션바 크기 (DisplayInfoSize))
- **Status Bar Insets:** `getStatusBarStableInsets(activity?)` - Status bar stable insets (상태바 고정 인셋)
- **Navigation Bar Insets:** `getNavigationBarStableInsets(activity?)` - Navigation bar stable insets (네비게이션바 고정 인셋)
- **Multi-window Mode:** `isInMultiWindowMode(activity)` - Check if app is in multi-window mode (멀티윈도우 모드 확인)
- **Context-based:** Uses Context for construction, Activity only when needed (생성 시 Context 사용, 필요 시에만 Activity 사용)
- **Auto SDK Branching:** Automatic handling for Android R (API 30) and above/below (Android R (API 30) 이상/이하 자동 처리)

<br>
</br>

### **SIM Info (SIM 카드 정보)** - SIM Card Information
- **Basic Info:** `isDualSim()`, `isSingleSim()`, `isMultiSim()` - Check SIM type (SIM 타입 확인)
- **Active SIM:** `getActiveSimCount()`, `getActiveSimSlotIndexList()` - Active SIM information (활성화된 SIM 정보)
- **Read Permission:** `isCanReadSimInfo()` - Check if SIM info can be read (permission and initialization status) (SIM 정보 읽기 가능 여부 확인 (권한 및 초기화 상태))
- **Permission fallback:** 요청 권한이 없으면 빈 리스트/null을 반환하고 Logx에 경고가 남습니다. 권한 허용 후 `refreshPermissions()` 호출을 권장합니다.
- **Subscription Info:** `getActiveSubscriptionInfoList()` - Query all subscription info (모든 구독 정보 조회)
- **Subscription ID:**
  - `getSubIdFromDefaultUSim()` - Query default SIM subscription ID (기본 SIM의 구독 ID 조회)
  - `getSubId(slotIndex)` - Query specific SIM slot subscription ID (특정 SIM 슬롯의 구독 ID 조회)
  - `subIdToSimSlotIndex(currentSubId)` - Convert Subscription ID to SIM slot index (Subscription ID를 SIM 슬롯 인덱스로 변환)
- **SubscriptionInfo Query:**
  - `getActiveSubscriptionInfoSubId(subId)` - Return SubscriptionInfo for specific SubID (특정 SubID의 SubscriptionInfo 반환)
  - `getActiveSubscriptionInfoSimSlot(slotIndex)` - Return SubscriptionInfo for specific SIM slot (특정 SIM 슬롯의 SubscriptionInfo 반환)
  - `getSubscriptionInfoSubIdFromDefaultUSim()` - Return SubscriptionInfo for default SIM (기본 SIM의 SubscriptionInfo 반환)
  - `getSubscriptionInfoSimSlotFromDefaultUSim()` - Return SubscriptionInfo for default SIM slot (기본 SIM 슬롯의 SubscriptionInfo 반환)
- **MCC/MNC (Default SIM):** `getMccFromDefaultUSimString()`, `getMncFromDefaultUSimString()` - Carrier codes (통신사 코드)
- **MCC/MNC (Per Slot):** `getMcc(slotIndex)`, `getMnc(slotIndex)` - Carrier codes for specific slot (특정 슬롯의 통신사 코드)
- **Phone Number:** `getPhoneNumberFromDefaultUSim()`, `getPhoneNumber(slotIndex)` - Query phone number (전화번호 조회)
- **SIM Status:** `getStatusFromDefaultUSim()`, `getActiveSimStatus(slotIndex)` - Check SIM status (SIM 상태 확인)
- **eSIM Support:** `isESimSupported()`, `isRegisterESim(slotIndex)` - Check eSIM (eSIM 확인)
- **Display Info:** `getDisplayNameFromDefaultUSim()`, `getCountryIsoFromDefaultUSim()` - Display name, country code (표시명, 국가 코드)
- **Roaming Status:** `isNetworkRoamingFromDefaultUSim()` - Check roaming status (로밍 여부 확인)
- **TelephonyManager Management:**
  - `updateUSimTelephonyManagerList()` - Update TelephonyManager list per SIM slot (SIM 슬롯별 TelephonyManager 목록 업데이트)
  - `getTelephonyManagerFromUSim(slotIndex)` - Return TelephonyManager for specific SIM slot (특정 SIM 슬롯의 TelephonyManager 반환)

<br>
</br>

### **Telephony Info (통신 정보)** - Telephony Information
- **Carrier Info:** `getCarrierName()`, `getMobileCountryCode()`, `getMobileNetworkCode()` - Carrier name, MCC/MNC (통신사명, MCC/MNC)
- **SIM Status:** `getSimState()`, `isSimReady()`, `getSimOperatorName()`, `getSimCountryIso()` - Check SIM status (SIM 상태 확인)
- **SIM Status String:** `getSimStateString()` - Convert SIM status to string (READY, ABSENT, PIN_REQUIRED, etc.) (SIM 상태를 문자열로 변환)
- **Phone Number:** `getPhoneNumber()` - Query phone number (전화번호 조회)
- **Call State:** `getCallState()` - Check call state (IDLE, RINGING, OFFHOOK) (통화 상태 확인)
- **Network Type:** `getNetworkType()`, `getDataNetworkType()` - Check network type (네트워크 타입 확인)
- **Network Type String:** `getNetworkTypeString()` - Convert 20+ network types to string (20가지 이상 네트워크 타입 문자열 변환)
  - 5G NR, LTE_CA, LTE, HSPA+, HSDPA, UMTS, EDGE, GPRS, CDMA, EVDO, GSM, TD_SCDMA, IWLAN, etc. (등)
- **Roaming:** `isNetworkRoaming()` - Check roaming status (로밍 상태 확인)
- **Signal Strength:** `currentSignalStrength` StateFlow + `getCurrentSignalStrength()` latest value (StateFlow + 최신 값 getter)
- **Service State:** `currentServiceState` StateFlow + `getCurrentServiceState()` latest value (StateFlow + 최신 값 getter)
- **Multi-SIM:** `getActiveSimCount()`, `getActiveSubscriptionInfoList()` - Multi-SIM support (멀티 SIM 지원)
- **TelephonyManager Query:** `getTelephonyManagerFromUSim(slotIndex)` - Return TelephonyManager for specific SIM slot (특정 SIM 슬롯의 TelephonyManager 반환)
- **Permission fallback:** 권한이 없으면 안전한 기본값/빈 리스트를 반환하며 로그에 경고가 남습니다. 권한을 허용했다면 `refreshPermissions()`를 호출해 상태를 갱신하세요.
- **Real-time Callback (Basic):** `registerCallback(handler, onSignalStrength, onServiceState, onNetworkState)` - Callback + StateFlow updates (콜백 + StateFlow 자동 업데이트)
- **Unregister Callback:** `unregisterCallback()` - Unregister registered callback (등록된 콜백 해제)
- **Auto API Compatibility:** Automatic branching between TelephonyCallback (API 31+) vs PhoneStateListener (TelephonyCallback (API 31+) vs PhoneStateListener 자동 분기)

**Advanced Multi-SIM Per-Slot Callback System (API 31+) (고급 멀티 SIM 슬롯별 콜백 시스템 (API 31+)):**
- **Default SIM Callback:** `registerTelephonyCallBackFromDefaultUSim(executor, isGpsOn, ...)` - Complete callback for default SIM (기본 SIM에 대한 전체 콜백)
- **Per-Slot Callback:** `registerTelephonyCallBack(simSlotIndex, executor, isGpsOn, ...)` - Complete callback for specific SIM slot (특정 SIM 슬롯 전체 콜백)
  - `executor` - Executor for callback execution (콜백 실행을 위한 Executor)
  - `isGpsOn` - Enable GPS-based cell info callback (location permission required) (GPS 기반 셀 정보 콜백 활성화 여부 (위치 권한 필요))
  - `onActiveDataSubId` - Active data subscription ID change callback (활성 데이터 구독 ID 변경 콜백)
  - `onDataConnectionState` - Data connection state change callback (데이터 연결 상태 변경 콜백)
  - `onCellInfo` - Cell tower info change callback (CurrentCellInfo) (셀 타워 정보 변경 콜백)
  - `onSignalStrength` - Signal strength change callback (CurrentSignalStrength) (신호 강도 변경 콜백)
  - `onServiceState` - Service state change callback (CurrentServiceState) (서비스 상태 변경 콜백)
  - `onCallState` - Call state change callback (callState, phoneNumber) (통화 상태 변경 콜백)
  - `onDisplayInfo` - Display info change callback (TelephonyDisplayInfo - 5G icon, etc.) (디스플레이 정보 변경 콜백 (5G 아이콘 등))
  - `onTelephonyNetworkState` - Network type change callback (TelephonyNetworkState) (통신망 타입 변경 콜백)
- **Per-Slot Callback Unregister:** `unregisterCallBack(simSlotIndex)` - Unregister callback for specific slot (특정 슬롯의 콜백 해제)
- **Check Callback Registration:** `isRegistered(simSlotIndex)` - Check if callback is registered for specific slot (특정 슬롯의 콜백 등록 여부)

**Individual Callback Setters (Can be changed dynamically after registration) (개별 콜백 Setter (등록 후 동적 변경 가능)):**
- `setOnSignalStrength(slotIndex, callback)` - Set signal strength callback (신호 강도 콜백 설정)
- `setOnServiceState(slotIndex, callback)` - Set service state callback (서비스 상태 콜백 설정)
- `setOnActiveDataSubId(slotIndex, callback)` - Set active data SubID callback (활성 데이터 SubID 콜백 설정)
- `setOnDataConnectionState(slotIndex, callback)` - Set data connection state callback (데이터 연결 상태 콜백 설정)
- `setOnCellInfo(slotIndex, callback)` - Set cell info callback (셀 정보 콜백 설정)
- `setOnCallState(slotIndex, callback)` - Set call state callback (통화 상태 콜백 설정)
- `setOnDisplayState(slotIndex, callback)` - Set display info callback (디스플레이 정보 콜백 설정)
- `setOnTelephonyNetworkType(slotIndex, callback)` - Set network type callback (통신망 타입 콜백 설정)

<br>
</br>

### **Network Connectivity Info (네트워크 연결 정보)** - Network Connection Information
- **Basic Connectivity:** `isNetworkConnected()` - Check network connection status (네트워크 연결 여부)
- **Transport Type Connection Check (Transport 타입별 연결 확인):**
  - `isConnectedWifi()` - Check WiFi connection (WiFi 연결 여부)
  - `isConnectedMobile()` - Check mobile data connection (모바일 데이터 연결 여부)
  - `isConnectedVPN()` - Check VPN connection (VPN 연결 여부)
  - `isConnectedBluetooth()` - Check Bluetooth connection (블루투스 연결 여부)
  - `isConnectedWifiAware()` - Check WiFi Aware connection (WiFi Aware 연결 여부)
  - `isConnectedEthernet()` - Check Ethernet connection (이더넷 연결 여부)
  - `isConnectedLowPan()` - Check LowPan connection (LowPan 연결 여부)
  - `isConnectedUSB()` - Check USB connection (API 31+) (USB 연결 여부)
- **WiFi Status:** `isWifiEnabled()` - Check if WiFi is enabled (WiFi 활성화 여부)
- **Network Capabilities:** `getNetworkCapabilities()` - Return NetworkCapabilities object (NetworkCapabilities 객체 반환)
- **Link Properties:** `getLinkProperties()` - Return LinkProperties object (LinkProperties 객체 반환)
- **IP Address Query:** `getIPAddressByNetworkType(type)` - Query IP address by network type (IPv4 only) (네트워크 타입별 IP 주소 조회 (IPv4 전용))
  - `TRANSPORT_ETHERNET` - Ethernet IP address (이더넷 IP 주소)
  - `TRANSPORT_WIFI` - WiFi IP address (WiFi IP 주소)
  - `TRANSPORT_CELLULAR` - Mobile data IP address (모바일 데이터 IP 주소)
  - Exclude loopback addresses, return IPv4 only (Loopback 주소 제외, IPv4만 반환)
  - Direct NetworkInterface query (NetworkInterface 직접 조회)
- **Callback Management (콜백 관리):**
  - `registerNetworkCallback(handler, ...)` - Register general network callback (일반 네트워크 콜백 등록)
  - `registerDefaultNetworkCallback(handler, ...)` - Register default network callback (기본 네트워크 콜백 등록)
  - `unregisterNetworkCallback()` - Unregister general network callback (일반 네트워크 콜백 해제)
  - `unregisterDefaultNetworkCallback()` - Unregister default network callback (기본 네트워크 콜백 해제)
- **Callback Parameters (콜백 파라미터):**
  - `onNetworkAvailable` - Network connected (네트워크 연결됨)
  - `onNetworkLosing` - Network about to disconnect (네트워크 끊어질 예정)
  - `onNetworkLost` - Network disconnected (네트워크 끊어짐)
  - `onUnavailable` - Network unavailable (네트워크 사용 불가)
  - `onNetworkCapabilitiesChanged` - Network capabilities changed (NetworkCapabilitiesData type) (네트워크 능력 변경 (NetworkCapabilitiesData 타입))
  - `onLinkPropertiesChanged` - Link properties changed (NetworkLinkPropertiesData type) (링크 속성 변경 (NetworkLinkPropertiesData 타입))
  - `onBlockedStatusChanged` - Blocked status changed (차단 상태 변경)
- **Summary Info:** `getNetworkConnectivitySummary()` - Query all connection states at once (NetworkConnectivitySummary data class) (모든 연결 상태 한 번에 조회 (NetworkConnectivitySummary 데이터 클래스))

<br></br>

## 🔐 **Required Permissions by Info (Info별 필수 권한)**

Each Info requires permissions **based on features used**. Add only the permissions for the Info you need.
>각 Info는 **사용하는 기능에 따라** 권한이 필요합니다. 필요한 Info의 권한만 추가하세요.

### 📋 Permission Requirements Summary (권한 요구사항 요약)

| Info                        | Required Permissions (필수 권한)                                         | Runtime Permission (런타임 권한) | No Permission Required (권한 불필요) |
|:----------------------------|:---------------------------------------------------------------------|:--:|:--:|
| **BatteryStateInfo**        | `BATTERY_STATS` (system-only, optional)                              | - | ✅ |
| **DisplayInfo**             | -                                                                    | - | ✅ |
| **LocationStateInfo**       | `ACCESS_FINE_LOCATION`<br>`ACCESS_COARSE_LOCATION`                   | ✅ | - |
| **SimInfo**                 | `READ_PHONE_STATE`<br>`READ_PHONE_NUMBERS`<br>`ACCESS_FINE_LOCATION` | ✅ | - |
| **TelephonyInfo**           | `READ_PHONE_STATE`<br>`READ_PHONE_NUMBERS`<br>`ACCESS_FINE_LOCATION` | ✅ | - |
| **NetworkConnectivityInfo** | `ACCESS_NETWORK_STATE`<br>`ACCESS_WIFI_STATE` (선택)                   | - | - |

### 🧭 Permission Policy (BaseSystemService 기준)
- **Runtime/Special only** – BaseSystemService validates runtime/special permissions only; non-dangerous permissions are treated as granted by design.
- **Default value fallback** – If runtime/special permission is missing or an error occurs, tryCatchSystemManager() returns a default value and logs a warning.
- **Refresh after grant** – Call BaseSystemService.refreshPermissions() after permissions are granted to update the internal cache.
> - **런타임/특수 권한만 검증** – BaseSystemService는 런타임/특수 권한만 검증하며, non-dangerous 권한은 설계상 허용된 것으로 간주합니다.
> - **기본값 반환** – 런타임/특수 권한이 없거나 오류가 발생하면 tryCatchSystemManager()가 기본값을 반환하고 경고를 기록합니다.
> - **권한 갱신** – 권한 허용 후 BaseSystemService.refreshPermissions()를 호출해 내부 캐시를 갱신하세요.

### ⚠️ Permission Troubleshooting Checklist (권한 점검 체크리스트)
- **Runtime/Special only** – BaseSystemService validates runtime/special permissions only; non-dangerous permissions are treated as granted by design.
- **Check Manifest declaration** – Verify all required permissions are added to AndroidManifest.xml.
- **Runtime request** – Request dangerous permissions with requestPermissions() or your own ActivityResult logic.
- **Call refreshPermissions()** – After granting permission, call BaseSystemService.refreshPermissions() and Simple UI will immediately reflect the new state.
- **Check Logx** – If runtime/special permission is missing, tryCatchSystemManager() returns a default value and logs a warning to Logx.
> - **런타임/특수 권한만 검증** – BaseSystemService는 런타임/특수 권한만 검증하며, non-dangerous 권한은 설계상 허용된 것으로 간주합니다.
> - **Manifest 선언 확인** – 필요한 모든 권한을 `AndroidManifest.xml`에 추가했는지 확인하세요.
> - **런타임 요청** – 위험 권한은 `requestPermissions()` 또는 자체 ActivityResult 로직으로 요청합니다.
> - **refreshPermissions() 호출** – 권한 허용 후 `BaseSystemService.refreshPermissions()`를 호출하면 Simple UI가 즉시 새 상태를 반영합니다.
> - **Logx 확인** – 런타임/특수 권한이 없으면 `tryCatchSystemManager()`가 기본값을 반환하며 Logx에 경고를 남깁니다.

<br>
</br>

## ⚙️ Detailed Permission Settings by Info (Info별 상세 권한 설정)


### 1️⃣ **Battery State Info** - `BATTERY_STATS` (System Only, Optional)

`BatteryStateInfo` does not enforce `android.permission.BATTERY_STATS`. BaseSystemService validates runtime/special permissions only.
>`BatteryStateInfo`는 `android.permission.BATTERY_STATS`를 강제 검증하지 않습니다. BaseSystemService는 런타임/특수 권한만 검증합니다.

**Usage Example (사용 예시)**:
```kotlin
// Ready to use immediately (바로 사용 가능)
val batteryInfo = BatteryStateInfo(context)
val success = batteryInfo.registerStart(lifecycleScope)
if (!success) {
    Log.e("Battery", "Failed to start battery monitoring (배터리 모니터링 시작 실패)")
    return
}

// Real-time battery status via SharedFlow (SharedFlow로 배터리 상태 실시간 수신)
lifecycleScope.launch {
    batteryInfo.sfUpdate.collect { event ->
        when (event) {
            is BatteryStateEvent.OnCapacity -> {
                Log.d("Battery", "Capacity (용량): ${event.percent}%")
            }
            is BatteryStateEvent.OnChargeStatus -> {
                val isCharging = event.status == BatteryManager.BATTERY_STATUS_CHARGING
                Log.d("Battery", "Charging (충전 중): $isCharging")
            }
            else -> {}
        }
    }
}
```
**Note** BATTERY_STATS is system-only. This library does not enforce it, and some values may be limited depending on device/OS support.
**Note** Unsupported fields return default values.
> **Note (참고)**: BATTERY_STATS는 시스템 전용 권한입니다. 본 라이브러리는 이를 강제 검증하지 않으며, 기기/OS 지원 범위에 따라 일부 값이 제한될 수 있습니다.
> **참고**: 미지원 필드는 기본값을 반환합니다.

<br></br>

#### 2️⃣ **Display Info** - No Permission Required (권한 불필요) ✅

Display information queries **do not require permissions**.
> 디스플레이 정보 조회는 **권한이 필요하지 않습니다**.

**Usage Example (사용 예시)**:
```kotlin
// Ready to use immediately (바로 사용 가능)
val displayInfo = DisplayInfo(context)

// Physical screen size (물리 화면 크기)
val physicalSize = displayInfo.getPhysicalScreenSize()
Log.d("Display", "Physical (물리 화면): ${physicalSize.width} x ${physicalSize.height}")

// Status bar size (상태바 크기)
val statusBarSize = displayInfo.getStatusBarSize()
Log.d("Display", "Status Bar (상태바): ${statusBarSize?.width} x ${statusBarSize?.height}")
```

**Note:** Display information is available via public APIs and does not require permissions.
> **참고:** 디스플레이 정보는 공개 API로 권한이 필요하지 않습니다.


<br>
</br>

#### 3️⃣ **Location State Info** - Location Permission Required (위치 권한 필수)

**AndroidManifest.xml**:
```xml
<!-- Required: Location information query (필수: 위치 정보 조회) -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

**Runtime Permission Request (런타임 권한 요청)**:
```kotlin
// Request location permissions (required) (위치 권한 요청 (필수))
requestPermissions(
    permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ),
    onDeniedResult = { deniedResults ->
        if (deniedResults.isEmpty()) {
            // Permissions granted - Start location tracking (권한 허용됨 - 위치 추적 시작)
            val locationInfo = LocationStateInfo(context)
            locationInfo.registerStart(
                coroutineScope = lifecycleScope,
                locationProvider = LocationManager.GPS_PROVIDER,
                // Low-power mode (polling disabled) (저전력 모드 - 폴링 비활성)
                // Alias: POLLING_DISABLED_UPDATE_CYCLE_TIME (별칭)
                updateCycleTime = DISABLE_UPDATE_CYCLE_TIME,
                minDistanceM = 10f
            )

            // Real-time location updates via SharedFlow (SharedFlow로 위치 변경 실시간 수신)
            lifecycleScope.launch {
                locationInfo.sfUpdate.collect { event ->
                    when (event) {
                        is LocationStateEvent.OnLocationChanged -> {
                            val location = event.location
                            Log.d("Location", "Lat (위도): ${location?.latitude}, Lng (경도): ${location?.longitude}")
                        }
                        is LocationStateEvent.OnGpsEnabled -> {
                            Log.d("Location", "GPS Enabled (GPS 활성화): ${event.isEnabled}")
                        }
                    }
                }
            }
        } else {
            // Permissions denied (권한 거부됨)
            toastShowShort("Location permission required (위치 권한이 필요합니다)")
        }
    },
)
```

**Note**:
- `ACCESS_FINE_LOCATION` - GPS location (precise location) (GPS 위치 (정확한 위치))
- `ACCESS_COARSE_LOCATION` - Network location (approximate location) (네트워크 위치 (대략적 위치))
- Both permissions are **dangerous permissions** requiring runtime request (두 권한 모두 **위험 권한**으로 런타임 요청 필수)


<br>
</br>


#### 4️⃣ **SIM Info** - Phone/Location Permissions Required (전화/위치 권한 필수)

**AndroidManifest.xml**:
```xml
<!-- Required: Read phone state (필수: 전화 상태 읽기) -->
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<!-- Required: Read phone numbers (필수: 전화번호 읽기) -->
<uses-permission android:name="android.permission.READ_PHONE_NUMBERS" />
<!-- Required: Cell-based location helpers (필수: 셀 기반 위치) -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

**Runtime Permission Request (런타임 권한 요청)**:
```kotlin
// Request phone state permission (required) (전화 상태 권한 요청 (필수))
requestPermissions(
    permissions = listOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_PHONE_NUMBERS,
        Manifest.permission.ACCESS_FINE_LOCATION
    ),
    onDeniedResult = { deniedResults ->
        if (deniedResults.isEmpty()) {
            // Permissions granted - Query SIM info (권한 허용됨 - SIM 정보 조회)
            val simInfo = SimInfo(context)

            // Check dual SIM (듀얼 SIM 확인)
            val isDualSim = simInfo.isDualSim()
            Log.d("SIM", "Dual SIM (듀얼 SIM): $isDualSim")

            // Active SIM count (활성 SIM 개수)
            val activeCount = simInfo.getActiveSimCount()
            Log.d("SIM", "Active SIM Count (활성 SIM 개수): $activeCount")

            // Query phone number (전화번호 조회)
            val phoneNumber = simInfo.getPhoneNumberFromDefaultUSim()
            Log.d("SIM", "Phone Number (전화번호): $phoneNumber")
        } else {
            // Permissions denied (권한 거부됨)
            toastShowShort("Phone state permission required (전화 상태 권한이 필요합니다)")
        }
    },
)
```

**Note (참고)**:
- `READ_PHONE_STATE`, `READ_PHONE_NUMBERS`, `ACCESS_FINE_LOCATION` are **dangerous permissions** requiring runtime request
- Phone number reading may be restricted on Android 10+ (API 29+) (Android 10+ (API 29+)부터 전화번호 읽기가 제한될 수 있음)

<br></br>

#### 5️⃣ **Telephony Info** - Phone/Location Permissions Required (전화/위치 권한 필수)

**AndroidManifest.xml**:
```xml
<!-- Required: Read phone state (필수: 전화 상태 읽기) -->
<uses-permission android:name="android.permission.READ_PHONE_STATE" />

<!-- Required: Read phone numbers (Android 8.0+) (필수: 전화번호 읽기 (Android 8.0+)) -->
<uses-permission android:name="android.permission.READ_PHONE_NUMBERS" />

<!-- Required: Location-based network info (cell tower location, etc.) -->
<!-- (필수: 위치 기반 통신망 정보 (셀 타워 위치 등)) -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

**Runtime Permission Request (Basic) (런타임 권한 요청 (기본))**:
```kotlin
// Request required permissions together (필수 권한 일괄 요청)
requestPermissions(
    permissions = listOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_PHONE_NUMBERS,
        Manifest.permission.ACCESS_FINE_LOCATION
    ),
    onDeniedResult = { deniedResults ->
        if (deniedResults.isEmpty()) {
            // Permissions granted - Query network info (권한 허용됨 - 통신망 정보 조회)
            val telephonyInfo = TelephonyInfo(context)

            // Carrier info (통신사 정보)
            val carrierName = telephonyInfo.getCarrierName()
            Log.d("Telephony", "Carrier (통신사): $carrierName")

            // Network type (네트워크 타입)
            val networkType = telephonyInfo.getNetworkTypeString()
            Log.d("Telephony", "Network (네트워크): $networkType")

            // SIM status (SIM 상태)
            val isSimReady = telephonyInfo.isSimReady()
            Log.d("Telephony", "SIM Ready (SIM 준비): $isSimReady")

            // Real-time signal strength via StateFlow (StateFlow로 신호 강도 실시간 수신)
            telephonyInfo.registerCallback()
            lifecycleScope.launch {
                telephonyInfo.currentSignalStrength.collect { signalStrength ->
                    Log.d("Telephony", "Signal Strength (신호 강도): ${signalStrength?.level}")
                }
            }
        }
    },
)
```

**Runtime Permission Request (Detailed Rationale) (런타임 권한 요청 (자세한 사유))**:
```kotlin
// Same permission set with explicit rationale (필요 권한을 이유와 함께 요청)
requestPermissions(
    permissions = listOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_PHONE_NUMBERS,
        Manifest.permission.ACCESS_FINE_LOCATION
    ),
    onDeniedResult = { deniedResults ->
        if (deniedResults.isEmpty()) {
            // All permissions granted - Full info access (모든 권한 허용됨 - 전체 정보 접근)
            val telephonyInfo = TelephonyInfo(context)

            // Query phone number (requires READ_PHONE_NUMBERS)
            // (전화번호 조회 (READ_PHONE_NUMBERS 필요))
            val phoneNumber = telephonyInfo.getPhoneNumber()
            Log.d("Telephony", "Phone Number (전화번호): $phoneNumber")

            // Cell tower location info (requires ACCESS_FINE_LOCATION)
            // (셀 타워 위치 정보 (ACCESS_FINE_LOCATION 필요))
            // ... Detailed cell info can be queried (상세 셀 정보 조회 가능)
        } else {
            // Partial permissions granted - Only safe APIs return data
            // (일부 권한만 허용됨 - 허용된 범위의 API만 사용 가능)
            Log.d("Telephony", "Denied Permissions (거부된 권한): ${deniedResults.map { it.permission }}")
        }
    },
)
```

**Note (참고)**:
- `READ_PHONE_STATE`, `READ_PHONE_NUMBERS`, `ACCESS_FINE_LOCATION` are all requested up front (모두 선요청)
- 권한이 거부된 항목과 관련된 API는 `tryCatchSystemManager()`를 통해 기본값을 반환합니다.

<br></br>

#### 6️⃣ **Network Connectivity Info** - Network State Permissions (네트워크 상태 권한)

**AndroidManifest.xml**:
```xml
<!-- Required: Query network state (필수: 네트워크 상태 조회) -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Optional: Query WiFi state (선택: WiFi 상태 조회) -->
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

**Usage Example (사용 예시)**:
```kotlin
// Ready to use with permission declaration only (no runtime request needed)
// (권한 선언만으로 바로 사용 가능 (런타임 요청 불필요))
val networkInfo = NetworkConnectivityInfo(context)

// Network connection status (네트워크 연결 여부)
val isConnected = networkInfo.isNetworkConnected()
Log.d("Network", "Network Connected (네트워크 연결): $isConnected")

// WiFi connection status (WiFi 연결 여부)
val isWifi = networkInfo.isConnectedWifi()
Log.d("Network", "WiFi Connected (WiFi 연결): $isWifi")

// Mobile data connection status (모바일 데이터 연결 여부)
val isMobile = networkInfo.isConnectedMobile()
Log.d("Network", "Mobile Connected (모바일 연결): $isMobile")

// Network summary info (네트워크 요약 정보)
val summary = networkInfo.getNetworkConnectivitySummary()
Log.d("Network", "Summary (요약): $summary")

// Real-time network changes via ConnectivityManager callback (optional)
// (ConnectivityManager 콜백으로 네트워크 변경 실시간 수신 (선택))
networkInfo.registerDefaultNetworkCallback()
lifecycleScope.launch {
    // Detect network state changes (네트워크 상태 변경 감지)
}
```

**Note (참고)**:
- `ACCESS_NETWORK_STATE` is a **normal permission** (no runtime request needed) 
- `ACCESS_WIFI_STATE` is a **normal permission** (no runtime request needed) 
>- (`ACCESS_NETWORK_STATE`는 **일반 권한**으로 런타임 요청 불필요)
>- (`ACCESS_WIFI_STATE`는 **일반 권한**으로 런타임 요청 불필요)

<br></br>

### 📊 Permission Types Summary (권한 타입별 정리)

| Permission Type                               | Permissions                                                                                      | Request Method                                               | Used by Info                                  |
|:----------------------------------------------|:-------------------------------------------------------------------------------------------------|:-------------------------------------------------------------|:----------------------------------------------|
| **Normal Permissions**                        | `ACCESS_NETWORK_STATE`<br>`ACCESS_WIFI_STATE`                                                    | Auto-granted with Manifest declaration                       | NetworkConnectivityInfo                       |
| **Dangerous Permissions**                     | `ACCESS_FINE_LOCATION`<br>`ACCESS_COARSE_LOCATION`<br>`READ_PHONE_STATE`<br>`READ_PHONE_NUMBERS` | Runtime permission request required                          | LocationStateInfo<br>SimInfo<br>TelephonyInfo |
| **Signature/System Permissions**              | `BATTERY_STATS`                                                                                  | System/privileged apps only (library does not enforce)       | BatteryStateInfo (optional)                   |

<br>
</br>

### 💡 Permission Request Tips (권한 요청 팁)

#### **Start with Required Permissions (필수 권한으로 시작)**
```kotlin
// LocationStateInfo usage example - Required permissions
// (LocationStateInfo 사용 예시 - 필수 권한)
requestPermissions(
    permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ),
    onDeniedResult = { deniedResults ->
        if (deniedResults.isEmpty()) {
            // Start location tracking (위치 추적 시작)
            locationInfo.registerStart(
                coroutineScope = lifecycleScope,
                locationProvider = LocationManager.NETWORK_PROVIDER,
                updateCycleTime = 5000L,
                minDistanceM = 0.1f
            )
        }
    },
)
```

#### **Request Additional Permissions When Needed (필요 시 추가 권한 요청)**
```kotlin
// When more precise location is needed (더 정확한 위치가 필요할 때)
requestPermissions(
    permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION  // Precise location (정확한 위치)
    ),
    onDeniedResult = { deniedResults ->
        if (deniedResults.isEmpty()) {
            // Use GPS-based location (GPS 기반 위치 사용)
            locationInfo.registerStart(
                coroutineScope = lifecycleScope,
                locationProvider = LocationManager.GPS_PROVIDER,
                updateCycleTime = 5000L,
                minDistanceM = 0.1f
            )
        }
    },
)
```

#### **Simple UI's Automatic Permission Handling (Simple UI의 자동 권한 처리)**
```kotlin
// Request multiple permissions at once (여러 권한을 한 번에 요청)
requestPermissions(
    permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_PHONE_STATE
    ),
    onDeniedResult = { deniedResults ->
        if (deniedResults.isEmpty()) {
            // All permissions granted (모든 권한 허용됨)
            startLocationTracking()
            loadSimInfo()
        } else {
            // Handle partial permission grant (일부만 허용된 경우 처리)
            Log.d("Permission", "Denied Permissions (거부된 권한): ${deniedResults.map { it.permission }}")
        }
    },
)
```

<br>
</br>

.

