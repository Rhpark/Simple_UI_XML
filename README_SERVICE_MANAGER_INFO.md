# System Service Manager Info vs Pure Android - Complete Comparison Guide (System Service Manager Info vs 순수 Android - 완벽 비교 가이드)

> **"Simplify complex System Service information collection!"** See the immediate difference Simple UI Info makes compared to traditional Android System Service data retrieval.
>
> **"복잡한 System Service 정보 수집을 간단하게!"** 기존 Android System Service 정보 조회 대비 Simple UI Info가 주는 체감 차이를 한눈에 확인하세요.

<br>
</br>

## 🔎 At a Glance (한눈 비교)

| Category (항목) | Pure Android (순수 Android) | Simple UI Info | Impact (개선 효과) |
|:--|:--:|:--:|:--:|
| **Battery Info (배터리 정보)** | `BroadcastReceiver` + `IntentFilter` + Manual management (수동 관리) | `BatteryStateInfo().registerStart()` | **StateFlow automation (StateFlow 자동화)** |
| **Location Info (위치 정보)** | `LocationManager` + Permissions + Callback implementation (권한 + 콜백 구현) | `LocationStateInfo().registerStart()` | **Auto Provider management (Provider 자동 관리)** |
| **Display Info (디스플레이 정보)** | SDK branching + WindowManager + DisplayMetrics (SDK 분기) | `DisplayInfo().getFullScreenSize()` | **Auto SDK handling (SDK 자동 처리)** |
| **SIM Card Info (SIM 카드 정보)** | `SubscriptionManager` + Manual multi-SIM management (멀티 SIM 수동 관리) | `SimInfo().getActiveSimCount()` | **Multi-SIM automation (멀티 SIM 자동화)** |
| **Telephony Info (통신 정보)** | `TelephonyManager` + Manual Callback implementation (Callback 수동 구현) | `TelephonyInfo().registerCallback()` | **Auto API compatibility (API 자동 호환)** |
| **Network Connectivity (네트워크 연결)** | `ConnectivityManager` + Callback implementation (Callback 구현) | `NetworkConnectivityInfo().isNetworkConnected()` | **Auto Transport detection (Transport 자동 감지)** |

> **Key takeaway:** System Service Manager Info simplifies complex system information collection with **StateFlow-based** architecture.
>
> **핵심:** System Service Manager Info는 복잡한 시스템 정보 수집을 **StateFlow 기반**으로 단순화합니다.

<br>
</br>

## 💡 Why It Matters (왜 중요한가)

### StateFlow-Based Reactive Architecture (StateFlow 기반 반응형 구조)
- **Real-time Updates:** Manual BroadcastReceiver management → Automatic StateFlow collect
- **Lifecycle Safety:** Coroutine scope integration prevents memory leaks
- **Event Type Separation:** Type-safe event handling with Sealed Classes

<br>
</br>

- **실시간 업데이트**: BroadcastReceiver 수동 관리 → StateFlow 자동 collect
- **Lifecycle 안전**: 코루틴 스코프 연동으로 메모리 누수 방지
- **이벤트 타입 분리**: Sealed Class로 타입 안전한 이벤트 처리

<br>
</br>

### Automated Complex Configuration (복잡한 설정 자동화)
- **Automatic BroadcastReceiver Registration/Unregistration:** Auto-detect Battery, Location Provider changes
- **Automatic SDK Version Branching:** Display API (R+/below), Fused Provider (S+) auto-branching
- **Simplified Permission Handling:** Automatic required permission checks and safe exception handling

<br>
</br>

- **BroadcastReceiver 자동 등록/해제**: Battery, Location Provider 변경 자동 감지
- **SDK 버전 분기 자동 처리**: Display API (R 이상/이하), Fused Provider (S+) 자동 분기
- **권한 처리 간소화**: 필수 권한 자동 체크 및 안전한 예외 처리

<br>
</br>

### Developer-Friendly Interface (개발자 친화적 API)
- **Intuitive Method Names:** `getCapacity()`, `getTemperature()`, `isGpsEnabled()`
- **Various Helper Methods:** `isCharging()`, `isHealthGood()`, `calculateDistance()`
- **Data Classes Provided:** NetworkConnectivitySummary, NetworkCapabilitiesData, etc.

<br>
</br>

- **직관적 메서드 이름**: `getCapacity()`, `getTemperature()`, `isGpsEnabled()`
- **다양한 헬퍼 메서드**: `isCharging()`, `isHealthGood()`, `calculateDistance()`
- **데이터 클래스 제공**: NetworkConnectivitySummary, NetworkCapabilitiesData 등

<br>
</br>

## Real Code Comparison (실제 코드 비교)

<br>
</br>

### First: Battery Information Collection Comparison (첫째: Battery 정보 수집 비교)

<details>
<summary><strong>Pure Android - Battery Information Collection (순수 Android - Battery 정보 수집)</strong></summary>

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
**Issues (문제점):**
- Manual BroadcastReceiver registration and unregistration required
- Must add all IntentFilter Actions manually
- Must implement Battery information extraction logic directly
- Manual temperature/voltage unit conversion
- Must implement callback mechanism manually
- Manual Lifecycle management
- Memory leak risk

<br>
</br>

**문제점:**
- BroadcastReceiver 수동 등록 및 해제 필요
- IntentFilter 모든 Action 직접 추가
- Battery 정보 추출 로직 직접 구현
- 온도/전압 단위 변환 수동 처리
- 콜백 메커니즘 직접 구현
- Lifecycle 관리 수동
- 메모리 누수 위험
</details>

<details>
<summary><strong>Simple UI - Battery State Info</strong></summary>

```kotlin
// Simple Battery information collection - StateFlow based (간단한 Battery 정보 수집 - StateFlow 기반)
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val batteryInfo by lazy { BatteryStateInfo(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Start battery monitoring - One line (배터리 모니터링 시작 - 한 줄)
        batteryInfo.registerStart(lifecycleScope)

        // 2. Query initial values - Simple getters (초기 값 조회 - 간단한 getter)
        val capacity = batteryInfo.getCapacity()
        val temp = batteryInfo.getTemperature()
        val voltage = batteryInfo.getVoltage()
        val health = batteryInfo.getCurrentHealthStr()

        // 3. StateFlow-based real-time updates - Auto collect (StateFlow 기반 실시간 업데이트 - 자동 collect)
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
                    // 12 event types supported (12가지 이벤트 타입 지원)
                    else -> {}
                }
            }
        }
    }
    // Auto cleanup in onDestroy() (onDestroy()에서 자동 정리)
}
```
**Advantages (장점):**
- **Dramatically simplified** (Complex Receiver → One line registration)
- Automatic BroadcastReceiver management
- StateFlow-based reactive updates
- 12 type-safe events
- Automatic Lifecycle cleanup

<br>
</br>

**장점:**
- **대폭 간소화** (복잡한 Receiver → 한 줄 등록)
- BroadcastReceiver 자동 관리
- StateFlow 기반 반응형 업데이트
- 12가지 타입 안전한 이벤트
- Lifecycle 자동 정리
</details>

<br>
</br>

### Second: Location Tracking Comparison (둘째: Location 위치 추적 비교)

<details>
<summary><strong>Pure Android - Manual Location Tracking (순수 Android - Location 수동 추적)</strong></summary>

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
**Issues (문제점):**
- Manual LocationListener implementation and registration
- Manual permission check repetition
- Manual Provider status change handling
- Must set location update parameters directly
- Manual Lifecycle management
- Memory leak risk

<br>
</br>

**문제점:**
- LocationListener 수동 구현 및 등록
- 권한 체크 수동 반복
- Provider 상태 변경 수동 처리
- 위치 업데이트 파라미터 직접 설정
- Lifecycle 관리 수동
- 메모리 누수 위험
</details>

<details>
<summary><strong>Simple UI - Location State Info</strong></summary>

```kotlin
// Simple Location tracking - StateFlow based (간단한 Location 추적 - StateFlow 기반)
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val locationInfo by lazy { LocationStateInfo(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permission request (Simple UI auto handling) (권한 요청 (Simple UI 자동 처리))
        onRequestPermissions(listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )) { deniedPermissions ->
            if (deniedPermissions.isEmpty()) {
                startLocationTracking()
            }
        }
    }

    private fun startLocationTracking() {
        // 1. Start location tracking - One line (위치 추적 시작 - 한 줄)
        locationInfo.registerStart(
            scope = lifecycleScope,
            provider = LocationManager.GPS_PROVIDER,
            minTime = 1000L,
            minDistance = 10f
        )

        // 2. Query initial values - Simple getters (초기 값 조회 - 간단한 getter)
        val lastLocation = locationInfo.getLocation()
        val isGpsEnabled = locationInfo.isGpsEnabled()

        // 3. StateFlow-based real-time updates - Auto collect (StateFlow 기반 실시간 업데이트 - 자동 collect)
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
    // Auto cleanup in onDestroy() (onDestroy()에서 자동 정리)
}
```
**Advantages (장점):**
- **Dramatically simplified** (Complex Listener → One line registration)
- Automatic LocationListener management
- StateFlow-based reactive updates
- 5 type-safe events (Location, GPS, Network, Passive, Fused)
- Automatic Provider status tracking
- Distance/bearing calculation helpers provided
- Automatic Lifecycle cleanup

<br>
</br>

**장점:**
- **대폭 간소화** (복잡한 Listener → 한 줄 등록)
- LocationListener 자동 관리
- StateFlow 기반 반응형 업데이트
- 5가지 타입 안전한 이벤트 (위치, GPS, Network, Passive, Fused)
- Provider 상태 자동 추적
- 거리/방향 계산 헬퍼 제공
- Lifecycle 자동 정리
</details>

<br>
</br>

### Third: Display Information Query Comparison (셋째: Display 정보 조회 비교)

<details>
<summary><strong>Pure Android - Manual Display Query (순수 Android - Display 수동 조회)</strong></summary>

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
**Issues (문제점):**
- Manual SDK version branching
- Direct use of Deprecated API
- Manual Resources ID query
- Complex Insets calculation
- Code duplication (repeated version branching)

<br>
</br>

**문제점:**
- SDK 버전별 분기 수동 처리
- Deprecated API 직접 사용
- Resources ID 수동 조회
- 복잡한 Insets 계산
- 코드 중복 (버전별 분기 반복)
</details>

<details>
<summary><strong>Simple UI - Display Info</strong></summary>

```kotlin
// Simple Display information query - Auto SDK handling (간단한 Display 정보 조회 - SDK 자동 처리)
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val displayInfo by lazy { DisplayInfo(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Full screen size (auto SDK branching) (전체 화면 크기 (SDK 자동 분기))
        val fullSize = displayInfo.getFullScreenSize()
        Log.d("Display", "Full: ${fullSize.x} x ${fullSize.y}") // (전체)

        // 2. Available screen size (exclude status bar, navigation bar)
        // (사용 가능 화면 크기 (상태바, 네비게이션바 제외))
        val availableSize = displayInfo.getScreen()
        Log.d("Display", "Available: ${availableSize.x} x ${availableSize.y}") // (사용 가능)

        // 3. Screen size with status bar (exclude navigation bar only)
        // (상태바 포함 화면 크기 (네비게이션바만 제외))
        val screenWithStatusBar = displayInfo.getScreenWithStatusBar()
        Log.d("Display", "With status bar: ${screenWithStatusBar.x} x ${screenWithStatusBar.y}") // (상태바 포함)

        // 4. Status bar height (상태바 높이)
        val statusBarHeight = displayInfo.getStatusBarHeight()
        Log.d("Display", "Status bar height: $statusBarHeight") // (상태바 높이)

        // 5. Navigation bar height (네비게이션바 높이)
        val navBarHeight = displayInfo.getNavigationBarHeight()
        Log.d("Display", "Navigation bar height: $navBarHeight") // (네비게이션바 높이)
    }
}
```
**Advantages (장점):**
- **Dramatically simplified** (Auto SDK branching)
- Automatic Android R (API 30) branching
- Automatic Deprecated API avoidance
- Automatic Resources query
- Automatic Insets calculation
- Simple getter methods

<br>
</br>

**장점:**
- **대폭 간소화** (SDK 분기 자동 처리)
- Android R (API 30) 자동 분기
- Deprecated API 자동 회피
- Resources 자동 조회
- Insets 자동 계산
- 간단한 getter 메서드
</details>

<br>
</br>

## Core Advantages of System Service Manager Info (System Service Manager Info의 핵심 장점)

### 1. **StateFlow-Based Reactive Architecture (StateFlow 기반 반응형 구조)**
- Battery: BroadcastReceiver → StateFlow
- Location: LocationListener → StateFlow
- Sealed Class type safety (Sealed Class 타입 안전)

<br>
</br>

### 2. **Automatic SDK Version Handling (SDK 버전 자동 처리)**
- Display: Automatic Android R branching (Android R 자동 분기)
- Location: Automatic Fused Provider support (Fused Provider 자동 지원)
- Developers don't need to worry! (개발자는 신경 쓸 필요 없음!)

<br>
</br>

### 3. **Automatic Lifecycle Management (Lifecycle 자동 관리)**
- Automatic onDestroy() invocation (onDestroy() 자동 호출)
- Automatic resource cleanup (리소스 자동 정리)
- Memory leak prevention (메모리 누수 방지)

<br>
</br>

## See Real Implementation Examples (실제 구현 예제보기)

**Live Example Code (라이브 예제 코드):**
> - System Service Manager Info : [ServiceManagerInfoActivity.kt](app/src/main/java/kr/open/library/simpleui_xml/system_service_manager/info/ServiceManagerInfoActivity.kt)
> - Run the app to see actual implementation examples! (실제로 앱을 구동 시켜서 실제 구현 예제를 확인해 보세요!)

<br>
</br>

## 🎯 Available Info List (제공되는 Info 목록)

**System Service Manager Info** provides 6 core system information types:
<br></br>
**System Service Manager Info**는 6가지 핵심 시스템 정보를 제공합니다:

### **Battery State Info (배터리 상태 정보)** - Battery Status Information
- **Real-time Updates:** `registerStart(scope, updateCycleTime)` - StateFlow-based auto-updates
  - `updateCycleTime` - Update cycle (default: 1000ms) (업데이트 주기 (기본값: 1000ms))
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
- **Manual Control:**
  - `updateBatteryState()` - Trigger one-time battery state update (일회성 배터리 상태 업데이트 트리거)
  - `unRegister()` - Manual unregistration (stop BroadcastReceiver and updates) (수동 등록 해제 (BroadcastReceiver 및 업데이트 중지))
- **Error Handling:** `BATTERY_ERROR_VALUE = Integer.MIN_VALUE` - Return value on error (오류 시 반환값)
- **BatteryStateEvent:** 12 event types (OnCapacity, OnTemperature, OnVoltage, OnCurrentAmpere, OnCurrentAverageAmpere, OnChargeStatus, OnChargePlug, OnHealth, OnChargeCounter, OnEnergyCounter, OnPresent, OnTotalCapacity) (12가지 이벤트 타입)

<br>
</br>

### **Location State Info (위치 상태 정보)** - Location Status Information
- **Real-time Updates:** `registerStart(coroutineScope, locationProvider, minTimeMs, minDistanceM)` - StateFlow-based location tracking
  - `coroutineScope` - Coroutine scope (Lifecycle integrated) (코루틴 스코프 (Lifecycle과 연동))
  - `locationProvider` - Location provider (GPS_PROVIDER, NETWORK_PROVIDER, PASSIVE_PROVIDER, FUSED_PROVIDER, etc.) (위치 제공자)
  - `minTimeMs` - Minimum update time interval (milliseconds) (최소 업데이트 시간 간격 (밀리초))
  - `minDistanceM` - Minimum movement distance (meters) (최소 이동 거리 (미터))
  - Automatic LocationListener and BroadcastReceiver registration/unregistration (자동 LocationListener 및 BroadcastReceiver 등록/해제)
- **Provider Status:** `isGpsEnabled()`, `isNetworkEnabled()`, `isPassiveEnabled()`, `isFusedEnabled()` (API 31+)
- **Extended Provider Status:**
  - `isLocationEnabled()` - Check GPS Provider enabled (same as isGpsEnabled()) (GPS Provider 활성화 확인 (isGpsEnabled()와 동일))
  - `isAnyEnabled()` - Check if any Provider is enabled (includes Fused on API 31+) (모든 Provider 중 하나라도 활성화 확인 (API 31+에서는 Fused 포함))
- **Current Location:** `getLocation()` - Last known location (GPS Provider priority) (마지막으로 알려진 위치 (GPS Provider 우선))
- **Distance Calculation:** `calculateDistance(from, to)` - Distance between two locations (meters) (두 위치 간 거리 (미터))
- **Bearing Calculation:** `calculateBearing(from, to)` - Bearing between two locations (degrees) (두 위치 간 방향 (도))
- **Radius Check:** `isLocationWithRadius(from, to, radius)` - Check location within specific radius (특정 반경 내 위치 확인)
- **Location Save/Load:**
  - `saveApplyLocation(location)` - Save location to SharedPreferences (immediate apply) (SharedPreferences에 위치 저장 (즉시 적용))
  - `loadLocation()` - Load saved location (저장된 위치 로드)
  - `removeLocation()` - Delete saved location (저장된 위치 삭제)
- **Manual Control:** `unregister()` - Stop location updates and release all resources (위치 업데이트 중지 및 모든 리소스 해제)
- **LocationStateEvent:** 5 event types (OnLocationChanged, OnGpsEnabled, OnNetworkEnabled, OnPassiveEnabled, OnFusedEnabled) (5가지 이벤트 타입)

<br>
</br>

### **Display Info (디스플레이 정보)** - Display Information
- **Full Screen Size:** `getFullScreenSize()` - Full screen size (includes status bar, navigation bar) (전체 화면 크기 (상태바, 네비게이션바 포함))
- **Available Screen:** `getScreen()` - Screen size excluding status bar and navigation bar (상태바, 네비게이션바 제외한 화면 크기)
- **Screen with Status Bar:** `getScreenWithStatusBar()` - Includes status bar, excludes navigation bar (상태바 포함, 네비게이션바 제외)
- **Status Bar Height:** `getStatusBarHeight()` - Status bar height (상태바 높이)
- **Navigation Bar Height:** `getNavigationBarHeight()` - Navigation bar height (네비게이션바 높이)
- **Auto SDK Branching:** Automatic handling for Android R (API 30) and above/below (Android R (API 30) 이상/이하 자동 처리)

<br>
</br>

### **SIM Info (SIM 카드 정보)** - SIM Card Information
- **Basic Info:** `isDualSim()`, `isSingleSim()`, `isMultiSim()` - Check SIM type (SIM 타입 확인)
- **Active SIM:** `getActiveSimCount()`, `getActiveSimSlotIndexList()` - Active SIM information (활성화된 SIM 정보)
- **Read Permission:** `isCanReadSimInfo()` - Check if SIM info can be read (permission and initialization status) (SIM 정보 읽기 가능 여부 확인 (권한 및 초기화 상태))
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
- **Signal Strength:** `getCurrentSignalStrength()` - StateFlow-based signal strength (StateFlow 기반 신호 강도)
- **Service State:** `getCurrentServiceState()` - StateFlow-based service state (StateFlow 기반 서비스 상태)
- **Multi-SIM:** `getActiveSimCount()`, `getActiveSubscriptionInfoList()` - Multi-SIM support (멀티 SIM 지원)
- **TelephonyManager Query:** `getTelephonyManagerFromUSim(slotIndex)` - Return TelephonyManager for specific SIM slot (특정 SIM 슬롯의 TelephonyManager 반환)
- **Real-time Callback (Basic):** `registerCallback(handler, onSignalStrength, onServiceState, onNetworkState)` - StateFlow-based real-time updates (StateFlow 기반 실시간 업데이트)
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


<br>
</br>

## 🔐 **Required Permissions by Info (Info별 필수 권한)**

Each Info requires permissions **based on features used**. Add only the permissions for the Info you need.
<br></br>

각 Info는 **사용하는 기능에 따라** 권한이 필요합니다. 필요한 Info의 권한만 추가하세요.

### 📋 Permission Requirements Summary (권한 요구사항 요약)

| Info | Required Permissions (필수 권한) | Runtime Permission (런타임 권한) | No Permission Required (권한 불필요) |
|:--|:--|:--:|:--:|
| **BatteryStateInfo** | - | - | ✅ |
| **DisplayInfo** | - | - | ✅ |
| **LocationStateInfo** | `ACCESS_FINE_LOCATION`<br>`ACCESS_COARSE_LOCATION` | ✅ | - |
| **SimInfo** | `READ_PHONE_STATE` | ✅ | - |
| **TelephonyInfo** | `READ_PHONE_STATE`<br>`READ_PHONE_NUMBERS` (선택)<br>`ACCESS_FINE_LOCATION` (선택) | ✅ | - |
| **NetworkConnectivityInfo** | `ACCESS_NETWORK_STATE`<br>`ACCESS_WIFI_STATE` (선택) | - | - |

<br>
</br>

### ⚙️ Detailed Permission Settings by Info (Info별 상세 권한 설정)


<br>
</br>


#### 1️⃣ **Battery State Info** - No Permission Required (권한 불필요) ✅

Battery information queries **do not require permissions**.
<br></br>

배터리 정보 조회는 **권한이 필요하지 않습니다**.

**Usage Example (사용 예시)**:
```kotlin
// Ready to use immediately (바로 사용 가능)
val batteryInfo = BatteryStateInfo(context)
batteryInfo.registerStart(lifecycleScope)

// Real-time battery status via StateFlow (StateFlow로 배터리 상태 실시간 수신)
lifecycleScope.launch {
    batteryInfo.sfUpdate.collect { event ->
        when (event) {
            is BatteryStateEvent.OnCapacity -> {
                Log.d("Battery", "Capacity (용량): ${event.capacity}%")
            }
            is BatteryStateEvent.OnCharging -> {
                Log.d("Battery", "Charging (충전 중): ${event.isCharging}")
            }
        }
    }
}
```

> **Note (참고)**: Battery information is provided via system broadcasts and does not require permissions.
>
> **참고**: 배터리 정보는 시스템 브로드캐스트로 제공되어 권한이 필요하지 않습니다.


<br>
</br>


#### 2️⃣ **Display Info** - No Permission Required (권한 불필요) ✅

Display information queries **do not require permissions**.
<br></br>

디스플레이 정보 조회는 **권한이 필요하지 않습니다**.

**Usage Example (사용 예시)**:
```kotlin
// Ready to use immediately (바로 사용 가능)
val displayInfo = DisplayInfo(context)

// Full screen size (including status bar, navigation bar)
// (전체 화면 크기 (상태바, 네비게이션바 포함))
val fullSize = displayInfo.getFullScreenSize()
Log.d("Display", "Full Screen (전체 화면): ${fullSize.x} x ${fullSize.y}")

// Status bar height (상태바 높이)
val statusBarHeight = displayInfo.getStatusBarHeight()
Log.d("Display", "Status Bar Height (상태바 높이): $statusBarHeight")
```

> **Note (참고)**: Display information is available via public APIs and does not require permissions.
>
> **참고**: 디스플레이 정보는 공개 API로 권한이 필요하지 않습니다.


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
onRequestPermissions(listOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)) { deniedPermissions ->
    if (deniedPermissions.isEmpty()) {
        // Permissions granted - Start location tracking (권한 허용됨 - 위치 추적 시작)
        val locationInfo = LocationStateInfo(context)
        locationInfo.registerStart(
            scope = lifecycleScope,
            provider = LocationManager.GPS_PROVIDER,
            minTime = 1000L,
            minDistance = 10f
        )

        // Real-time location updates via StateFlow (StateFlow로 위치 변경 실시간 수신)
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
}
```

> **Note (참고)**:
> - `ACCESS_FINE_LOCATION` - GPS location (precise location) (GPS 위치 (정확한 위치))
> - `ACCESS_COARSE_LOCATION` - Network location (approximate location) (네트워크 위치 (대략적 위치))
> - Both permissions are **dangerous permissions** requiring runtime request (두 권한 모두 **위험 권한**으로 런타임 요청 필수)


<br>
</br>


#### 4️⃣ **SIM Info** - Phone State Permission Required (전화 상태 권한 필수)

**AndroidManifest.xml**:
```xml
<!-- Required: Read phone state (필수: 전화 상태 읽기) -->
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

**Runtime Permission Request (런타임 권한 요청)**:
```kotlin
// Request phone state permission (required) (전화 상태 권한 요청 (필수))
onRequestPermissions(listOf(
    Manifest.permission.READ_PHONE_STATE
)) { deniedPermissions ->
    if (deniedPermissions.isEmpty()) {
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
}
```

> **Note (참고)**:
> - `READ_PHONE_STATE` is a **dangerous permission** requiring runtime request (`READ_PHONE_STATE`는 **위험 권한**으로 런타임 요청 필수)
> - Phone number reading may be restricted on Android 10+ (API 29+) (Android 10+ (API 29+)부터 전화번호 읽기가 제한될 수 있음)


<br>
</br>


#### 5️⃣ **Telephony Info** - Phone State + Optional Permissions (전화 상태 + 선택적 권한)

**AndroidManifest.xml**:
```xml
<!-- Required: Read phone state (필수: 전화 상태 읽기) -->
<uses-permission android:name="android.permission.READ_PHONE_STATE" />

<!-- Optional: Read phone numbers (Android 8.0+) (선택: 전화번호 읽기 (Android 8.0+)) -->
<uses-permission android:name="android.permission.READ_PHONE_NUMBERS" />

<!-- Optional: Location-based network info (cell tower location, etc.) -->
<!-- (선택: 위치 기반 통신망 정보 (셀 타워 위치 등)) -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

**Runtime Permission Request (Basic) (런타임 권한 요청 (기본))**:
```kotlin
// Request basic permissions only (required) (기본 권한만 요청 (필수))
onRequestPermissions(listOf(
    Manifest.permission.READ_PHONE_STATE
)) { deniedPermissions ->
    if (deniedPermissions.isEmpty()) {
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
            telephonyInfo.getCurrentSignalStrength().collect { signalStrength ->
                Log.d("Telephony", "Signal Strength (신호 강도): ${signalStrength?.level}")
            }
        }
    }
}
```

**Runtime Permission Request (Full - Phone Number + Location) (런타임 권한 요청 (전체 - 전화번호 + 위치))**:
```kotlin
// For full features (optional) (전체 기능 사용 시 (선택))
onRequestPermissions(listOf(
    Manifest.permission.READ_PHONE_STATE,
    Manifest.permission.READ_PHONE_NUMBERS,
    Manifest.permission.ACCESS_FINE_LOCATION
)) { deniedPermissions ->
    if (deniedPermissions.isEmpty()) {
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
        // Partial permissions granted - Use basic info only
        // (일부 권한만 허용됨 - 기본 정보만 사용)
        Log.d("Telephony", "Denied Permissions (거부된 권한): $deniedPermissions")
    }
}
```

> **Note (참고)**:
> - `READ_PHONE_STATE` - Required (Carrier, network type, etc.) (필수 (통신사, 네트워크 타입 등))
> - `READ_PHONE_NUMBERS` - Optional (Phone number query) (선택 (전화번호 조회))
> - `ACCESS_FINE_LOCATION` - Optional (Detailed cell tower location) (선택 (셀 타워 상세 위치))


<br>
</br>


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

// Real-time network changes via StateFlow (optional)
// (StateFlow로 네트워크 변경 실시간 수신 (선택))
networkInfo.registerDefaultNetworkCallback()
lifecycleScope.launch {
    // Detect network state changes (네트워크 상태 변경 감지)
}
```

> **Note (참고)**:
> - `ACCESS_NETWORK_STATE` is a **normal permission** (no runtime request needed) (`ACCESS_NETWORK_STATE`는 **일반 권한**으로 런타임 요청 불필요)
> - `ACCESS_WIFI_STATE` is a **normal permission** (no runtime request needed) (`ACCESS_WIFI_STATE`는 **일반 권한**으로 런타임 요청 불필요)

<br>
</br>

### 📊 Permission Types Summary (권한 타입별 정리)

| Permission Type (권한 타입) | Permissions (권한 목록) | Request Method (요청 방법) | Used by Info (사용 Info) |
|:--|:--|:--|:--|
| **Normal Permissions (일반 권한)** | `ACCESS_NETWORK_STATE`<br>`ACCESS_WIFI_STATE` | Auto-granted with Manifest declaration (Manifest 선언만으로 자동 허용) | NetworkConnectivityInfo |
| **Dangerous Permissions (위험 권한)** | `ACCESS_FINE_LOCATION`<br>`ACCESS_COARSE_LOCATION`<br>`READ_PHONE_STATE`<br>`READ_PHONE_NUMBERS` | Runtime permission request required (런타임 권한 요청 필수) | LocationStateInfo<br>SimInfo<br>TelephonyInfo |

<br>
</br>

### 💡 Permission Request Tips (권한 요청 팁)

#### **Start with Minimum Permissions (최소 권한으로 시작)**
```kotlin
// LocationStateInfo usage example - Minimum permissions
// (LocationStateInfo 사용 예시 - 최소 권한)
onRequestPermissions(listOf(
    Manifest.permission.ACCESS_COARSE_LOCATION  // Approximate location only (대략적 위치만)
)) { deniedPermissions ->
    if (deniedPermissions.isEmpty()) {
        // Use network-based location only (네트워크 기반 위치만 사용)
        locationInfo.registerStart(provider = LocationManager.NETWORK_PROVIDER)
    }
}
```

#### **Request Additional Permissions When Needed (필요 시 추가 권한 요청)**
```kotlin
// When more precise location is needed (더 정확한 위치가 필요할 때)
onRequestPermissions(listOf(
    Manifest.permission.ACCESS_FINE_LOCATION  // Precise location (정확한 위치)
)) { deniedPermissions ->
    if (deniedPermissions.isEmpty()) {
        // Use GPS-based location (GPS 기반 위치 사용)
        locationInfo.registerStart(provider = LocationManager.GPS_PROVIDER)
    }
}
```

#### **Simple UI's Automatic Permission Handling (Simple UI의 자동 권한 처리)**
```kotlin
// Request multiple permissions at once (여러 권한을 한 번에 요청)
onRequestPermissions(listOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.READ_PHONE_STATE
)) { deniedPermissions ->
    if (deniedPermissions.isEmpty()) {
        // All permissions granted (모든 권한 허용됨)
        startLocationTracking()
        loadSimInfo()
    } else {
        // Handle partial permission grant (일부만 허용된 경우 처리)
        Log.d("Permission", "Denied Permissions (거부된 권한): $deniedPermissions")
    }
}
```

<br>
</br>

.
