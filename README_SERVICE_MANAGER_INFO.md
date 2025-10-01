# System Service Manager Info vs 순수 Android - 완벽 비교 가이드

> **"복잡한 System Service 정보 수집을 간단하게!"** 기존 Android System Service 정보 조회 대비 Simple UI Info가 주는 체감 차이를 한눈에 확인하세요.

<br>
</br>

## 🔎 한눈 비교 (At a glance)

| 항목 | 순수 Android | Simple UI Info | 개선 효과 |
|:--|:--:|:--:|:--:|
| **배터리 Battery 정보** | `BroadcastReceiver` + `IntentFilter` + 수동 관리 | `BatteryStateInfo().registerStart()` | **StateFlow 자동화** |
| **위치 Location 정보** | `LocationManager` + 권한 + 콜백 구현 | `LocationStateInfo().registerStart()` | **Provider 자동 관리** |
| **디스플레이 Display 정보** | SDK 분기 + WindowManager + DisplayMetrics | `DisplayInfo().getFullScreenSize()` | **SDK 자동 처리** |
| **SIM 카드 정보** | `SubscriptionManager` + 멀티 SIM 수동 관리 | `SimInfo().getActiveSimCount()` | **멀티 SIM 자동화** |
| **통신 Telephony 정보** | `TelephonyManager` + Callback 수동 구현 | `TelephonyInfo().registerCallback()` | **API 자동 호환** |
| **네트워크 Network 연결** | `ConnectivityManager` + Callback 구현 | `NetworkConnectivityInfo().isNetworkConnected()` | **Transport 자동 감지** |

> **핵심:** System Service Manager Info는 복잡한 시스템 정보 수집을 **StateFlow 기반**으로 단순화합니다.

<br>
</br>

## 💡 왜 중요한가:

### StateFlow 기반 반응형 구조
- **실시간 업데이트**: BroadcastReceiver 수동 관리 → StateFlow 자동 collect
- **Lifecycle 안전**: 코루틴 스코프 연동으로 메모리 누수 방지
- **이벤트 타입 분리**: Sealed Class로 타입 안전한 이벤트 처리

### 복잡한 설정 자동화
- **BroadcastReceiver 자동 등록/해제**: Battery, Location Provider 변경 자동 감지
- **SDK 버전 분기 자동 처리**: Display API (R 이상/이하), Fused Provider (S+) 자동 분기
- **권한 처리 간소화**: 필수 권한 자동 체크 및 안전한 예외 처리

### 개발자 친화적 API
- **직관적 메서드 이름**: `getCapacity()`, `getTemperature()`, `isGpsEnabled()`
- **다양한 헬퍼 메서드**: `isCharging()`, `isHealthGood()`, `calculateDistance()`
- **데이터 클래스 제공**: NetworkConnectivitySummary, NetworkCapabilitiesData 등

<br>
</br>

## 🎯 제공되는 Info 목록

**System Service Manager Info**는 6가지 핵심 시스템 정보를 제공합니다:

### **배터리 Battery State Info** - 배터리 상태 정보
- **실시간 업데이트**: `registerStart(scope)` - StateFlow 기반 자동 업데이트
- **용량 정보**: `getCapacity()` - 배터리 잔량 (0~100%)
- **전류 정보**: `getCurrentAmpere()`, `getCurrentAverageAmpere()` - 순간/평균 전류
- **충전 상태**: `isCharging()`, `isDischarging()`, `isFull()` - 충전 상태 확인
- **충전 타입**: `isChargingUsb()`, `isChargingAc()`, `isChargingWireless()` - 충전 방식 확인
- **배터리 건강**: `isHealthGood()`, `isHealthCold()`, `isHealthDead()` - 배터리 상태 확인
- **온도/전압**: `getTemperature()`, `getVoltage()` - 배터리 온도 및 전압
- **총 용량**: `getTotalCapacity()` - 배터리 총 용량 (mAh)
- **BatteryStateEvent**: 12가지 이벤트 타입 (OnCapacity, OnTemperature, OnVoltage 등)

### **위치 Location State Info** - 위치 상태 정보
- **실시간 업데이트**: `registerStart(scope, provider, minTime, minDistance)` - StateFlow 기반 위치 추적
- **Provider 상태**: `isGpsEnabled()`, `isNetworkEnabled()`, `isPassiveEnabled()`, `isFusedEnabled()` (API 31+)
- **현재 위치**: `getLocation()` - 마지막으로 알려진 위치
- **거리 계산**: `calculateDistance(from, to)` - 두 위치 간 거리
- **방향 계산**: `calculateBearing(from, to)` - 두 위치 간 방향
- **반경 확인**: `isLocationWithRadius(from, to, radius)` - 특정 반경 내 위치 확인
- **위치 저장**: `saveApplyLocation()`, `loadLocation()` - SharedPreferences 저장/로드
- **LocationStateEvent**: 5가지 이벤트 타입 (OnLocationChanged, OnGpsEnabled 등)

### **디스플레이 Display Info** - 디스플레이 정보
- **전체 화면 크기**: `getFullScreenSize()` - 전체 화면 크기 (상태바, 네비게이션바 포함)
- **사용 가능 화면**: `getScreen()` - 상태바, 네비게이션바 제외한 화면 크기
- **상태바 포함 화면**: `getScreenWithStatusBar()` - 상태바 포함, 네비게이션바 제외
- **상태바 높이**: `getStatusBarHeight()` - 상태바 높이
- **네비게이션바 높이**: `getNavigationBarHeight()` - 네비게이션바 높이
- **SDK 자동 분기**: Android R (API 30) 이상/이하 자동 처리

### **SIM 카드 Sim Info** - SIM 카드 정보
- **기본 정보**: `isDualSim()`, `isSingleSim()`, `isMultiSim()` - SIM 타입 확인
- **활성 SIM**: `getActiveSimCount()`, `getActiveSimSlotIndexList()` - 활성화된 SIM 정보
- **구독 정보**: `getActiveSubscriptionInfoList()` - 모든 구독 정보 조회
- **Subscription ID**: `getSubIdFromDefaultUSim()`, `getSubId(slotIndex)` - 구독 ID 조회
- **MCC/MNC**: `getMccFromDefaultUSimString()`, `getMncFromDefaultUSimString()` - 통신사 코드
- **전화번호**: `getPhoneNumberFromDefaultUSim()`, `getPhoneNumber(slotIndex)` - 전화번호 조회
- **SIM 상태**: `getStatusFromDefaultUSim()`, `getActiveSimStatus(slotIndex)` - SIM 상태 확인
- **eSIM 지원**: `isESimSupported()`, `isRegisterESim(slotIndex)` - eSIM 확인
- **표시 정보**: `getDisplayNameFromDefaultUSim()`, `getCountryIsoFromDefaultUSim()` - 표시명, 국가 코드
- **로밍 상태**: `isNetworkRoamingFromDefaultUSim()` - 로밍 여부 확인

### **통신 Telephony Info** - Telephony 정보
- **통신사 정보**: `getCarrierName()`, `getMobileCountryCode()`, `getMobileNetworkCode()` - 통신사명, MCC/MNC
- **SIM 상태**: `getSimState()`, `isSimReady()`, `getSimOperatorName()`, `getSimCountryIso()` - SIM 상태 확인
- **전화번호**: `getPhoneNumber()` - 전화번호 조회
- **네트워크 타입**: `getNetworkType()`, `getDataNetworkType()`, `getNetworkTypeString()` - 네트워크 타입 확인
- **로밍**: `isNetworkRoaming()` - 로밍 상태 확인
- **신호 강도**: `getCurrentSignalStrength()` - StateFlow 기반 신호 강도
- **서비스 상태**: `getCurrentServiceState()` - StateFlow 기반 서비스 상태
- **멀티 SIM**: `getActiveSimCount()`, `getActiveSubscriptionInfoList()` - 멀티 SIM 지원
- **실시간 콜백**: `registerCallback()` - StateFlow 기반 실시간 업데이트
- **슬롯별 콜백**: `registerTelephonyCallBack(slotIndex)` - SIM 슬롯별 콜백 (API 31+)
- **API 자동 호환**: TelephonyCallback (API 31+) vs PhoneStateListener 자동 분기

### **네트워크 Network Connectivity Info** - 네트워크 연결 정보
- **기본 연결성**: `isNetworkConnected()` - 네트워크 연결 여부
- **Transport 타입**: `isConnectedWifi()`, `isConnectedMobile()`, `isConnectedVPN()` - 전송 타입별 확인
- **다양한 Transport**: Bluetooth, WiFi Aware, Ethernet, LowPan, USB (API 31+)
- **WiFi 상태**: `isWifiEnabled()` - WiFi 활성화 여부
- **네트워크 능력**: `getNetworkCapabilities()` - NetworkCapabilities 객체 반환
- **링크 속성**: `getLinkProperties()` - LinkProperties 객체 반환
- **콜백 관리**: `registerNetworkCallback()`, `registerDefaultNetworkCallback()` - 네트워크 변경 감지
- **요약 정보**: `getNetworkConnectivitySummary()` - 모든 연결 상태 한 번에 조회

<br>
</br>

## 실제 코드 비교

<br>
</br>

### 첫째: Battery 정보 수집 비교

<details>
<summary><strong>순수 Android - Battery 정보 수집</strong></summary>

```kotlin
// 기존의 Battery 정보 수집 방법
class BatteryMonitor(private val context: Context) {

    private var batteryReceiver: BroadcastReceiver? = null
    private var batteryStatus: Intent? = null

    // 1. BroadcastReceiver 수동 구현
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
                // 배터리 정보 추출
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

                // UI 업데이트 또는 콜백 호출 (수동으로 구현해야 함)
                updateUI(capacity, isCharging, chargingType, tempCelsius, voltageV, healthStr)
            }
        }

        context.registerReceiver(batteryReceiver, intentFilter)
    }

    // 2. BatteryManager 추가 정보 수동 조회
    fun getCurrentAmpere(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
    }

    // 3. 정리 작업 수동 처리
    fun stopMonitoring() {
        batteryReceiver?.let {
            context.unregisterReceiver(it)
        }
        batteryReceiver = null
        batteryStatus = null
    }

    private fun updateUI(capacity: Int, isCharging: Boolean, chargingType: String,
                         temp: Double, voltage: Double, health: String) {
        // UI 업데이트 로직 (각자 구현해야 함)
    }
}
```
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
// 간단한 Battery 정보 수집 - StateFlow 기반
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val batteryInfo by lazy { BatteryStateInfo(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 배터리 모니터링 시작 - 한 줄
        batteryInfo.registerStart(lifecycleScope)

        // 2. 초기 값 조회 - 간단한 getter
        val capacity = batteryInfo.getCapacity()
        val temp = batteryInfo.getTemperature()
        val voltage = batteryInfo.getVoltage()
        val health = batteryInfo.getCurrentHealthStr()

        // 3. StateFlow 기반 실시간 업데이트 - 자동 collect
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
                    // 12가지 이벤트 타입 지원
                    else -> {}
                }
            }
        }
    }
    // onDestroy()에서 자동 정리
}
```
**장점:**
- **대폭 간소화** (복잡한 Receiver → 한 줄 등록)
- BroadcastReceiver 자동 관리
- StateFlow 기반 반응형 업데이트
- 12가지 타입 안전한 이벤트
- Lifecycle 자동 정리
</details>

<br>
</br>

---

## System Service Manager Info의 핵심 장점

### 1. **StateFlow 기반 반응형 구조**
- Battery: BroadcastReceiver → StateFlow
- Location: LocationListener → StateFlow
- Sealed Class 타입 안전

### 2. **SDK 버전 자동 처리**
- Display: Android R 자동 분기
- Location: Fused Provider 자동 지원
- 개발자는 신경 쓸 필요 없음!

### 3. **Lifecycle 자동 관리**
- onDestroy() 자동 호출
- 리소스 자동 정리
- 메모리 누수 방지

---

<br>
</br>

## 실제 구현 예제보기

**라이브 예제 코드:**
> - System Service Manager Info : [ServiceManagerInfoActivity.kt](app/src/main/java/kr/open/library/simpleui_xml/system_service_manager/info/ServiceManagerInfoActivity.kt)
> - 실제로 앱을 구동 시켜서 실제 구현 예제를 확인해 보세요!

**필수 권한 설정:**
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.BATTERY_STATS" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```
