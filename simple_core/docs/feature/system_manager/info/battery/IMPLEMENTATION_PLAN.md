# Battery Info Implementation Plan (As-Is)

## 문서 정보
- 문서명: Battery Info Implementation Plan
- 작성일: 2026-02-01
- 수정일: 2026-02-01
- 대상 모듈: simple_core
- 패키지: kr.open.library.simple_ui.core.system_manager.info.battery
- 상태: 현행(as-is)

## 목표
- 현재 구현된 배터리 정보 모듈의 동작 규칙을 문서로 정리한다.
- 코드 없이도 구현 흐름을 재현할 수 있도록 단계별 로직을 명시한다.

## 구현 범위(현행)
- `BatteryStateInfo`의 등록/해제/조회 API
- BroadcastReceiver + 주기 폴링 결합 수집
- SharedFlow 이벤트 발행 및 StateFlow 캐싱
- BatteryManager/Intent 기반 값 변환
- PowerProfile reflection 기반 총 용량 조회 + fallback
- Logx 기반 오류 처리
- Robolectric/Unit 테스트

## 구현 상세(파일 기준)

### 1) BatteryStateInfo 초기화
- 상속: `BaseSystemService(context)` (권한 리스트 전달 없음)
- 구성 요소
  - `BatteryPropertyReader`: BatteryManager 접근 및 값 변환
  - `BatteryStateEmitter`: StateFlow 캐싱 + SharedFlow 발행
  - `BatteryStateReceiver`: 브로드캐스트 수신 + 주기 갱신
- 이벤트 스트림: `sfUpdate`는 `BatteryStateEmitter`의 SharedFlow를 노출

### 2) registerStart 흐름
1. `updateCycleTime` 검증
   - `MIN_UPDATE_CYCLE_TIME` 미만이면 `IllegalArgumentException` 발생
2. `registerReceiver()` 호출
   - 실패 시 false 반환 + Logx 에러
3. `tryCatchSystemManager(false)`로 `receiver.updateStart()` 호출
4. 실패 시 `unRegister()` 후 false 반환

### 3) BatteryStateReceiver 동작
- `registerReceiver()`
  - IntentFilter 등록(ACTION_BATTERY_CHANGED/LOW/OKAY/POWER_CONNECTED/DISCONNECTED/POWER_USAGE_SUMMARY)
  - SDK 33+는 `RECEIVER_NOT_EXPORTED` 사용
  - 최초 등록 시 반환된 Intent가 있으면 캐시 (null일 수 있음. Robolectric 환경에서는 sticky broadcast가 null일 수 있음)
- `updateStart()`
  - 등록되지 않은 상태면 자동 등록
  - 기존 업데이트 Job 취소 후 재시작
  - 부모 Job 기반 `SupervisorJob` + 내부 `CoroutineScope` 생성
  - 부모 Job 종료 시 자동 `unRegisterReceiver()` + `updateStop()`
  - `DISABLE_UPDATE_CYCLE_TIME`이면 1회 호출만 수행
  - 그 외에는 `while (isActive)` 루프로 주기 호출
  - `internalCoroutineScope`는 `checkNotNull`로 검증
- `updateStop()`
  - updateJob 취소 후 null 처리
- `getBatteryStatusIntent()` / `fetchBatteryStatusIntent()`
  - 캐시된 Intent 우선, 없으면 `registerReceiver(null, filter)`로 fallback

### 4) BatteryStateEmitter 동작
- `MutableSharedFlow` 버퍼 설정
  - `replay=1`, `extraBufferCapacity=16`, `DROP_OLDEST`
- 각 지표는 `MutableStateFlow`로 캐싱
- `setupDataFlows()`
  - `dropWhile`로 초기 센티널(오류) 값만 필터링. 이후 센티널 값은 의도적으로 통과(실제값→센티널 순 업데이트 시 센티널도 이벤트로 발행)
  - 각 StateFlow를 collect하여 이벤트로 전환(`BatteryStateEvent`)
- `updateBatteryInfo()`
  - `BatteryStateData`로 받은 값들을 StateFlow에 반영

### 5) BatteryPropertyReader 동작
- Intent extra 기반 값 읽기 + BatteryManager property 기반 값 조회
- 온도: `EXTRA_TEMPERATURE / 10`, 범위(`MIN_TEMPERATURE_CELSIUS`~`MAX_TEMPERATURE_CELSIUS`, -40~120) 밖이면 오류 값 (로그 없음)
- 전압: `EXTRA_VOLTAGE / 1000`, `MIN_VOLTAGE`(0.0) 이하이면 오류 값 (로그 없음)
- 충전 상태: `BATTERY_PROPERTY_STATUS` 우선, 실패 시 Intent extra fallback
- 충전 타입 리스트: bitmask → 문자열 리스트 (NONE/UNKNOWN 포함)
- 총 용량: PowerProfile → chargeCounter 기반 추정 → 오류 값
  - 추정 범위: `MIN_REASONABLE_CAPACITY_MAH`~`MAX_REASONABLE_CAPACITY_MAH` (1000~30000mAh). 범위 밖이면 `Logx.w` 기록

### 6) PowerProfile 반사(reflection) 경로
- 내부 API `com.android.internal.os.PowerProfile` 접근
- `getAveragePower` 메서드 리플렉션 호출
- `PowerProfile.getBatteryCapacity()` 실패 시 `DEFAULT_BATTERY_CAPACITY`(0.0) 반환 + Logx 경고
- 호출부(`BatteryPropertyReader.getPowerProfileBatteryCapacity()`)에서 `> 0` 체크 후 `BATTERY_ERROR_VALUE_DOUBLE`로 전환
- 배터리 총 용량은 `POWER_BATTERY_CAPACITY` 키 사용

### 7) 오류 처리/로그 정책
- 외부 API는 `tryCatchSystemManager`로 예외를 기본값으로 전환하고 Logx 기록
- 내부 로직은 `safeCatch`로 보호
- 개발자 실수(잘못된 `updateCycleTime`)는 예외로 명확히 알림

## 구현 흐름 요약

1. `BatteryStateInfo` 생성 → Receiver/Emitter/Reader 준비
2. `registerStart()` 호출 → 검증 → BroadcastReceiver 등록 → 업데이트 시작
3. 각 조회 메서드는 `getBatteryStatus()` 체인(`getBatteryStatusIntent() ?: fetchBatteryStatusIntent()`)으로 Intent를 획득
4. `sfUpdate` 수집 → 이벤트 기반 처리
5. `onDestroy()` 또는 `unRegister()` 호출 → 리소스 해제

## 테스트 현황
- Unit
  - `BatteryPropertyReaderEventUnitTest`
  - `BatteryStateEmitterUnitTest`
- Robolectric
  - `BatteryStateInfoRobolectricTest`
  - `BatteryStateReceiverRobolectricTest`
  - `BatteryPropertyReaderRobolectricTest`

## 운영/유지보수 체크리스트
- `updateCycleTime`은 `MIN_UPDATE_CYCLE_TIME` 이상으로 사용
- 백그라운드/저전력 상황에서는 주기(10s/60s) 사용 고려
- `DISABLE_UPDATE_CYCLE_TIME` 사용 시 전류/평균 전류 값 갱신 지연 가능
- PowerProfile 실패 시 총 용량은 오류 값 반환 가능
- `CoroutineScope`에 Job이 없으면 자동 정리가 보장되지 않음 — 반드시 `unRegister()`를 직접 호출할 것
- Job이 있는 스코프(`lifecycleScope`/`viewModelScope`) 사용을 강력 권장
