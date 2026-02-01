# Battery Info vs Plain Android - Complete Comparison Guide
> **Battery Info vs 순수 Android - 완벽 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_core` (UI-independent core module / UI 비의존 코어 모듈)
- **Package**: `kr.open.library.simple_ui.core.system_manager.info.battery`

<br></br>

## Overview (개요)
Provides real-time battery state collection with SharedFlow and helper getters.  
> SharedFlow 기반 실시간 배터리 상태 수집과 헬퍼 getter를 제공합니다.

<br></br>

## Quick Usage Flow (사용 흐름 요약)
1) `registerStart(lifecycleScope)` 호출  
2) `sfUpdate` 수집  
3) 화면/컴포넌트 종료 시 `onDestroy()` 호출 (내부적으로 `unRegister()` 수행)

```kotlin
class MainActivity : AppCompatActivity() {
    private val batteryInfo by lazy { BatteryStateInfo(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) 시작
        if (!batteryInfo.registerStart(lifecycleScope)) return

        // 2) 수집
        lifecycleScope.launch {
            batteryInfo.sfUpdate.collect { event ->
                // 이벤트 처리
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 3) 종료
        batteryInfo.onDestroy()
    }
}
```

<br></br>

## At a Glance (한눈 비교)
- **Real-time Updates:** `registerStart(coroutine: CoroutineScope, updateCycleTime: Long = 2000L): Boolean` - SharedFlow-based event updates
  - `coroutineScope` - Coroutine scope (Lifecycle integrated) (코루틴 스코프 (Lifecycle과 연동))
  - `updateCycleTime` - Update cycle in milliseconds (default: 2000ms) (밀리초 단위 업데이트 주기 (기본값: 2000ms))
    - 2000ms (default): Recommended for most cases - fast updates, moderate battery usage. (대부분의 경우 권장 - 빠른 업데이트, 적당한 배터리 사용)
    - 10000ms: Slower updates, lower battery consumption. (느린 업데이트, 낮은 배터리 소비)
    - 60000ms: Very slow updates, minimal battery impact. (매우 느린 업데이트, 최소 배터리 영향)
    - MIN_UPDATE_CYCLE_TIME (1000ms): Lower than this may cause excessive polling and battery drain; throws IllegalArgumentException. (MIN_UPDATE_CYCLE_TIME(1000ms) 미만은 과도한 폴링으로 배터리 부담이 커질 수 있으며 IllegalArgumentException 발생)
    - DISABLE_UPDATE_CYCLE_TIME: Disables periodic polling and relies on broadcasts only; current/average may update slowly. (DISABLE_UPDATE_CYCLE_TIME은 주기 폴링을 중단하고 브로드캐스트 기반 갱신만 수행하며 전류/평균 전류는 갱신이 느릴 수 있음)
  - **Returns**: `true` if registration and update start succeeded, `false` if a runtime failure occurred (e.g., BroadcastReceiver registration failed). **Throws** `IllegalArgumentException` if `updateCycleTime < MIN_UPDATE_CYCLE_TIME` (programming error). (등록 및 업데이트 시작 성공 시 `true`, 런타임 실패 시 `false`. `updateCycleTime < MIN_UPDATE_CYCLE_TIME`이면 `IllegalArgumentException` 발생 (프로그래밍 오류))
  - Automatic BroadcastReceiver registration/unregistration (자동 BroadcastReceiver 등록/해제)
- **Capacity Info:** `getCapacity()` - Battery level (0~100%) (배터리 잔량 (0~100%))
- **Current Info:** `getCurrentAmpere()`, `getCurrentAverageAmpere()` - Instant/average current (microamperes) (순간/평균 전류 (마이크로암페어))
- **Charging Status:** `isCharging()`, `isDischarging()`, `isFull()`, `isNotCharging()` - Check charging status (충전 상태 확인)
- **Charging Type:** `isChargingUsb()`, `isChargingAc()`, `isChargingWireless()`, `isChargingDock()` (API 33+) - Check charging method (충전 방식 확인)
- **Charging Type List:** `getChargePlugList()` - Return charging type labels (USB, AC, WIRELESS, DOCK, NONE, UNKNOWN) (충전 타입 라벨 리스트 반환)
- **Battery Health:** `isHealthGood()`, `isHealthCold()`, `isHealthDead()`, `isHealthOverVoltage()` - Check battery health (배터리 상태 확인)
- **Health Status String:** `getCurrentHealthStr()` - Convert battery health to string (배터리 상태 문자열 변환)
- **Temperature/Voltage:** `getTemperature()` - Battery temperature (Celsius, valid range: -40~120°C, returns error value outside range) (배터리 온도 (섭씨, 유효 범위: -40~120°C, 범위 밖이면 오류 값 반환)), `getVoltage()` - Battery voltage (Volts, returns error value if ≤ 0V) (배터리 전압 (볼트, 0V 이하면 오류 값 반환))
- **Total Capacity:** `getTotalCapacity()` - Total battery capacity (mAh). Fallback strategy: PowerProfile(reflection) → chargeCounter-based estimation (valid range: 1000~30000mAh) → error value. (배터리 총 용량 (mAh). Fallback 전략: PowerProfile(리플렉션) → chargeCounter 기반 추정 (유효 범위: 1000~30000mAh) → 오류 값)
- **Battery Technology:** `getTechnology()` - Battery technology info (Li-ion, Li-poly, etc.) (배터리 기술 정보 (Li-ion, Li-poly 등))
- **Lifecycle Management:**
  - `onDestroy()` - Automatic cleanup (internally calls unRegister()) (자동 정리 (내부적으로 unRegister() 호출))
  - `unRegister()` - Manual early unregistration if needed before destruction (파괴 전에 조기 해제가 필요한 경우 수동 호출)
  - If CoroutineScope has no Job, call `unRegister()` manually; use `lifecycleScope`/`viewModelScope` recommended (Job이 없으면 자동 정리 보장 없음; `lifecycleScope`/`viewModelScope` 사용 강력 권장)
- **Event Stream Notes:** `sfUpdate` is event-only (not snapshot), ordering is not guaranteed, and events may be dropped by buffer policy. Initial sentinel (error) values are filtered by `dropWhile`; subsequent sentinel values pass through intentionally. (sfUpdate는 스냅샷이 아닌 이벤트 스트림이며 순서 보장이 없고 버퍼 정책에 따라 이벤트가 드롭될 수 있음. 초기 센티널(오류) 값은 `dropWhile`로 필터링되며, 이후 센티널 값은 의도적으로 통과)
- **Error Handling:** `BATTERY_ERROR_VALUE`, `BATTERY_ERROR_VALUE_LONG`, `BATTERY_ERROR_VALUE_DOUBLE`, `BATTERY_ERROR_VALUE_BOOLEAN(null)` - Return values on error (오류 시 반환값)
- **BatteryStateEvent:** 11 event types (OnCapacity, OnTemperature, OnVoltage, OnCurrentAmpere, OnCurrentAverageAmpere, OnChargeStatus, OnChargePlug, OnHealth, OnChargeCounter, OnEnergyCounter, OnPresent) (11가지 이벤트 타입)

<br></br>

## Why It Matters (중요한 이유)
**Issues**
- Manual BroadcastReceiver registration and unregistration required
- Must add all IntentFilter Actions manually
- Must implement Battery information extraction logic directly
- Manual temperature/voltage unit conversion
- Must implement callback mechanism manually
- Manual Lifecycle management
- Memory leak risk
> - BroadcastReceiver 수동 등록 및 해제 필요
> - IntentFilter 모든 Action 직접 추가
> - Battery 정보 추출 로직 직접 구현
> - 온도/전압 단위 변환 수동 처리
> - 콜백 메커니즘 직접 구현
> - Lifecycle 관리 수동
> - 메모리 누수 위험

**Advantages**
- **Dramatically simplified** (Complex Receiver → One line registration)
- Automatic BroadcastReceiver management
- SharedFlow-based reactive updates
- 11 type-safe events
- Automatic Lifecycle cleanup
> - **대폭 간소화** (복잡한 Receiver → 한 줄 등록)
> - BroadcastReceiver 자동 관리
> - SharedFlow 기반 반응형 업데이트
> - 11가지 타입 안전한 이벤트
> - Lifecycle 자동 정리

<br></br>

## 순수 Android 방식 (Plain Android)
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

<br></br>

## Simple UI Approach (Simple UI 방식)
```kotlin
// Simple Battery information collection - SharedFlow based (간단한 Battery 정보 수집 - SharedFlow 기반)
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val batteryInfo by lazy { BatteryStateInfo(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Start battery monitoring with default update cycle (2000ms)
        // (기본 업데이트 주기로 배터리 모니터링 시작 (2000ms))
        val success = batteryInfo.registerStart(lifecycleScope)
        if (!success) {
            Logx.e("Battery: Failed to start battery monitoring (배터리 모니터링 시작 실패)")
            return
        }

        // Or use custom update cycle (10000ms) (또는 커스텀 업데이트 주기 사용 (10000ms))
        // updateCycleTime < MIN_UPDATE_CYCLE_TIME 이면 IllegalArgumentException 발생
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

<br></br>

## Permissions (권한)
See the permission guide for required permissions and policy.  
> 필수 권한과 정책은 권한 가이드를 참고하세요.

- [README_PERMISSION.md](../../../README_PERMISSION.md)
- `android.permission.BATTERY_STATS`는 시스템/프리로드 전용이며 런타임 권한 요청 대상이 아닙니다.

<br></br>

## Related Docs (관련 문서)
- Summary: [README_SERVICE_MANAGER_INFO.md](../README_SERVICE_MANAGER_INFO.md)
- Permission Guide: [README_PERMISSION.md](../../../README_PERMISSION.md)
- Feature PRD: [PRD.md](../../../../simple_core/docs/feature/system_manager/info/battery/PRD.md)
- Feature SPEC: [SPEC.md](../../../../simple_core/docs/feature/system_manager/info/battery/SPEC.md)
- Feature Plan: [IMPLEMENTATION_PLAN.md](../../../../simple_core/docs/feature/system_manager/info/battery/IMPLEMENTATION_PLAN.md)

<br></br>
