# Battery Info SPEC

## 문서 정보
- 문서명: Battery Info SPEC
- 작성일: 2026-02-01
- 수정일: 2026-02-01
- 대상 모듈: simple_core
- 패키지: kr.open.library.simple_ui.core.system_manager.info.battery
- 수준: 구현 재현 가능 수준(Implementation-ready)
- 상태: 현행(as-is)

## 전제/참조
- 기본 규칙/환경은 루트 AGENTS.md에서 연결되는 *_RULE.md를 따른다.
- 상세 요구와 범위는 `PRD.md`를 따른다.
- 실제 구현은 `kr.open.library.simple_ui.core.system_manager.info.battery` 패키지에 존재한다.

## 모듈 구조 및 책임

- `battery/`
  - `BatteryStateInfo`: 외부 공개 API, 등록/해제/조회/이벤트 제공
  - `BatteryStateEvent`: 11종 이벤트 모델(sealed class)
  - `BatteryStateConstants`: 오류 값, 문자열 상수, 업데이트 주기 상수
- `battery/internal/helper/`
  - `BatteryStateReceiver`: BroadcastReceiver 등록 및 주기 갱신
  - `BatteryStateEmitter`: StateFlow 캐싱 + SharedFlow 이벤트 발행
  - `BatteryPropertyReader`: BatteryManager/Intent 기반 값 조회 및 변환
- `battery/internal/helper/power/`
  - `PowerProfile`: reflection 기반 배터리 용량 조회
  - `PowerProfileVO`: PowerProfile 리소스 키 정의
- `battery/internal/model/`
  - `BatteryStateData`: 내부 상태 스냅샷 모델

## API 설계

```kotlin
public open class BatteryStateInfo(context: Context) : BaseSystemService(context) {
    // 이벤트 스트림
    val sfUpdate: SharedFlow<BatteryStateEvent>

    // 수집/해제
    fun registerStart(coroutine: CoroutineScope, updateCycleTime: Long = DEFAULT_UPDATE_CYCLE_TIME): Boolean
    fun unRegister()
    override fun onDestroy()

    // 상태 조회
    fun getCapacity(): Int
    fun getChargeCounter(): Int
    fun getChargeStatus(): Int
    fun getChargePlug(): Int
    fun getChargePlugList(): List<String>
    fun getCurrentAmpere(): Int
    fun getCurrentAverageAmpere(): Int
    fun getEnergyCounter(): Long
    fun getHealth(): Int
    fun getCurrentHealthStr(): String
    fun getPresent(): Boolean?
    fun getTemperature(): Double
    fun getVoltage(): Double
    fun getTechnology(): String?
    fun getTotalCapacity(): Double

    // 편의 메서드
    fun isCharging(): Boolean
    fun isDischarging(): Boolean
    fun isNotCharging(): Boolean
    fun isFull(): Boolean
    fun isChargingUsb(): Boolean
    fun isChargingAc(): Boolean
    fun isChargingWireless(): Boolean
    @RequiresApi(TIRAMISU) fun isChargingDock(): Boolean
}
```

## 공통 동작 규칙

### registerStart 동작
- `updateCycleTime >= MIN_UPDATE_CYCLE_TIME`을 **요구**하며, 미만이면 `IllegalArgumentException` 발생
- 내부에서 `registerReceiver()` 호출 후 `BatteryStateReceiver.updateStart()`로 주기 갱신 시작
- `registerReceiver()` 실패 시 `false` 반환 + Logx 에러 로그 (정리할 리소스가 없으므로 별도 정리 호출 없음)
- `updateStart()` 실패 시 `unRegister()` 호출(이미 등록된 리시버 정리) 후 `false` 반환 + Logx 에러 로그

### BatteryStateReceiver 동작
- 등록되지 않은 상태에서 `updateStart()`가 호출되면 자동으로 `registerReceiver()` 수행
- 내부 스코프는 `SupervisorJob(parentJob)` 기반으로 생성
- 부모 Job 완료 시 자동으로 `unRegisterReceiver()` + `updateStop()` 수행
- `DISABLE_UPDATE_CYCLE_TIME`이면 1회만 갱신하고 반복 루프를 만들지 않음

### 이벤트 발행 규칙
- `BatteryStateEmitter`는 `MutableStateFlow`로 값 캐싱(동일 값 재발행 방지)
- 초기 센티널(오류) 값은 `dropWhile`로 필터링하여 첫 이벤트를 지연. 이후 센티널 값은 의도적으로 통과(실제값→센티널 순 업데이트 시 센티널도 이벤트로 발행)
- `SharedFlow` 버퍼: `replay=1`, `extraBufferCapacity=16`, `DROP_OLDEST`
- 버퍼 정책상 이벤트가 드롭될 수 있음(문서/주석에 명시)

## 값 변환 및 보정 규칙

- **온도**: `EXTRA_TEMPERATURE / 10` → 섭씨. 범위(`MIN_TEMPERATURE_CELSIUS`~`MAX_TEMPERATURE_CELSIUS`, -40~120) 벗어나면 오류 값 반환 (로그 없음)
- **전압**: `EXTRA_VOLTAGE / 1000` → V. `MIN_VOLTAGE`(0.0) 이하이면 오류 값 반환 (로그 없음)
- **충전 상태**: `BATTERY_PROPERTY_STATUS` 우선, 실패 시 Intent extra로 fallback
- **충전 타입 리스트**:
  - bitmask 기반(USB/AC/WIRELESS)
  - `0`이면 `NONE`, 오류 값이면 `UNKNOWN`
  - DOCK는 TIRAMISU 이상에서만 추가
- **총 용량**:
  1) PowerProfile(리플렉션) 조회
  2) `chargeCounter` + `capacity` 기반 추정(범위 `MIN_REASONABLE_CAPACITY_MAH`~`MAX_REASONABLE_CAPACITY_MAH`, 1000~30000mAh). 범위 밖이면 `Logx.w` 기록
  3) 실패 시 오류 값 반환

### 배터리 Intent 캐시/폴백

- `BatteryStateInfo.getBatteryStatus()`는 `getBatteryStatusIntent() ?: fetchBatteryStatusIntent()` 체인으로 동작
- 캐시된 Intent가 있으면 즉시 반환, 없으면 `registerReceiver(null, filter)`로 시스템에서 최신 Intent 조회
- Robolectric 환경에서는 sticky broadcast가 null일 수 있음 (실기기에서는 정상 동작)

## 권한/매니페스트 정책
- `android.permission.BATTERY_STATS`는 **시스템/프리로드 전용**
- 라이브러리는 해당 권한을 검증하지 않음(매니페스트 경고 미출력)
- `BatteryStateInfo`는 `BaseSystemService(context)`로 생성되며 권한 리스트를 전달하지 않음

## 오류 처리/로그 정책
- 외부 API는 `tryCatchSystemManager`로 예외를 기본값으로 전환하고 Logx 기록
- 내부 로직은 `safeCatch`로 보호
- 개발자 실수(잘못된 `updateCycleTime`)는 예외로 명확히 알림

## 테스트
- Unit
  - `BatteryPropertyReaderEventUnitTest`
  - `BatteryStateEmitterUnitTest`
- Robolectric
  - `BatteryStateInfoRobolectricTest`
  - `BatteryStateReceiverRobolectricTest`
  - `BatteryPropertyReaderRobolectricTest`
