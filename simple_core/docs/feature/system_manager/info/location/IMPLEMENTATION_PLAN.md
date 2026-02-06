# Location Info Implementation Plan (As-Is)

## 문서 정보
- 문서명: Location Info Implementation Plan
- 작성일: 2026-02-06
- 수정일: 2026-02-06
- 대상 모듈: simple_core
- 패키지: kr.open.library.simple_ui.core.system_manager.info.location
- 상태: 현행(as-is)

## 목표
- 현재 구현된 Location Info 모듈의 동작을 코드 기준으로 문서화한다.
- 기능 변경 계획이 아니라, 현재 로직을 재현 가능한 수준으로 정리한다.

## 구현 범위(현행)
- `LocationStateInfo` 공개 API
- BroadcastReceiver + LocationListener + 주기 폴링 결합 수집
- SharedFlow 이벤트 발행 및 StateFlow 캐시
- provider 상태/최근 위치 조회 + 품질 판단
- 거리/방향/반경 계산 유틸
- 위치 SharedPreferences 저장/복원
- Logx 기반 오류 처리
- Unit/Robolectric 테스트

## 구현 상세(파일 기준)

### 1) `LocationStateInfo` 초기화
- 상속: `BaseSystemService(context, listOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))`
- 주요 구성 요소
  - `queryHelper: LocationQueryHelper`
  - `updateManager: LocationUpdateManager`
  - `locationStorage: LocationSharedPreference`
- `sfUpdate`는 `updateManager.getSfUpdate()`를 그대로 노출

### 2) `registerStart` 흐름
1. 파라미터 검증
   - `updateCycleTime == -1` 또는 `>= 1000`
   - `minDistanceM >= 0.1`
2. `updateManager.registerReceiver()` 수행
3. listener 등록 시 `normalizedLocationListenerCycleTime` 계산
   - `updateCycleTime == -1`이면 listener `minTime = DEFAULT_UPDATE_CYCLE_TIME(5000)`
4. `updateManager.registerLocationListener(...)` 수행
5. `tryCatchSystemManager(false)`로 `updateManager.updateStart(...)` 수행
6. 실패 시 `updateManager.destroy()`로 자원 정리 후 `false` 반환

### 3) `LocationUpdateManager` 오케스트레이션
- `sender: LocationStateEmitter`, `receiver: LocationStateReceiver`를 조합
- `dataProvider` 람다를 통해 최신 스냅샷(`LocationStateData`) 생성
- `updateStart`에서 receiver 내부 scope를 사용해 emitter collector를 연결

### 4) `LocationStateReceiver` 동작
- `registerReceiver`
  - `PROVIDERS_CHANGED_ACTION` 수신 등록
  - API 33+는 `RECEIVER_NOT_EXPORTED` 사용
  - 중복 등록 시 기존 상태 유지하고 `true` 반환
- `registerLocationListener`
  - 이미 등록된 listener가 있으면 해제 후 재등록
  - provider/time/distance 변경을 즉시 반영
- `updateStart`
  - receiver 미등록 시 자동 등록 시도
  - 기존 루프 정리 후 `SupervisorJob(parentJob)` 기반 내부 scope 생성
  - `setupDataFlows` 실행 후 업데이트 루프 시작
  - `updateCycleTime == -1`: 1회 동기화만 수행
  - 그 외: `while (isActive)` 주기 루프 실행
  - parent Job completion 시 자동 정리 콜백 수행
- `destroy`
  - receiver/listener/update loop 전부 정리

### 5) `LocationStateEmitter` 동작
- 이벤트 스트림
  - `MutableSharedFlow(replay=1, extraBufferCapacity=8, DROP_OLDEST)`
- 상태 캐시
  - location/gps/network/passive/fused를 `MutableStateFlow`로 보유
- `setupDataFlows`
  - 기존 collector 취소 후 5개 collector 재설정
  - 각 상태를 `LocationStateEvent`로 매핑해 발행
  - 초기 센티널은 `dropWhile`로 필터링

### 6) `LocationQueryHelper` 동작
- provider enabled 조회를 `safeCatch(false)`로 보호
- `isAnyEnabled`는 SDK별(FUSED 포함 여부)로 분기
- `getLocation`
  1. provider 모두 비활성 시 `null`
  2. 권한(FINE+COARSE) 미충족 시 `null`
  3. `getBestLastKnownLocation` 결과 반환
- `getBestLastKnownLocation`
  - 후보 provider 순서대로 lastKnownLocation 수집
  - `LocationQuality.isBetter` 기준으로 최적값 선택

### 7) 품질/계산/저장 헬퍼
- `LocationQuality`
  - 시간(10초), 정확도(200m), provider 동일성 기준으로 후보 우선순위 계산
- `LocationCalculator`
  - 거리/방위각/반경 판정
- `LocationSharedPreference`
  - lat/lon/accuracy/time/provider 저장
  - 저장값 없으면 `null` 반환

### 8) 오류 처리/로그 정책
- 공개 API는 `tryCatchSystemManager`로 보호
- 내부 구현은 `safeCatch`로 보호
- 등록 실패/provider 비활성/권한 누락 등은 `Logx` 기록

## 구현 흐름 요약

1. `LocationStateInfo` 생성
2. `registerStart(...)` 호출
3. receiver/listener 등록
4. 폴링 모드에 따라 1회 또는 주기 동기화
5. 상태 변경 시 `LocationStateData` 갱신
6. emitter가 `LocationStateEvent`로 변환해 `sfUpdate` 발행
7. `unRegister()` 또는 `onDestroy()`로 정리

## 테스트 현황
- Unit
  - `LocationStateConstantsUnitTest`
  - `LocationStateEventUnitTest`
  - `LocationStateEmitterUnitTest`
- Robolectric
  - `LocationStateInfoRobolectricTest`
  - `LocationSharedPreferenceTest`
  - `LocationQueryHelperRobolectricTest`
  - `LocationQualityRobolectricTest`
  - `LocationCalculatorRobolectricTest`
  - `LocationUpdateManagerRobolectricTest`
  - `LocationStateReceiverRobolectricTest` (helper/internal 경로)

## 운영/유지보수 체크리스트
- `updateCycleTime`은 `-1` 또는 `>=1000`으로 사용
- `minDistanceM`은 `>=0.1`로 사용
- 저전력 모드(`-1`)는 폴링만 비활성화하며 listener는 유지됨
- `sfUpdate`는 이벤트 스트림이므로 스냅샷/전역 순서를 가정하지 말 것
- `CoroutineScope`에 Job이 없으면 자동 정리가 보장되지 않으므로 수동 해제 필요
