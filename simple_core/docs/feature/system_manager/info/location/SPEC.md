# Location Info SPEC

## 문서 정보
- 문서명: Location Info SPEC
- 작성일: 2026-02-06
- 수정일: 2026-02-06
- 대상 모듈: simple_core
- 패키지: kr.open.library.simple_ui.core.system_manager.info.location
- 수준: 구현 재현 가능 수준(Implementation-ready)
- 상태: 현행(as-is)

## 전제/참조
- 기본 규칙/환경은 루트 `AGENTS.md`와 `docs/rules/*_RULE.md`를 따른다.
- 요구사항과 범위는 `PRD.md`를 따른다.
- 실제 구현은 `kr.open.library.simple_ui.core.system_manager.info.location` 패키지에 존재한다.

## 모듈 구조 및 책임

- `location/`
  - `LocationStateInfo`: 외부 공개 API, 등록/해제/조회/유틸/저장 연동
  - `LocationStateEvent`: 위치/제공자 이벤트 모델(sealed class, 5종)
  - `LocationStateConstants`: 기본값/하한값/품질 판단 임계치
  - `LocationSharedPreference`: 위치 저장/복원/삭제
- `location/internal/helper/`
  - `LocationQueryHelper`: provider 상태 조회 및 최적 최근 위치 선택
  - `LocationStateReceiver`: BroadcastReceiver + LocationListener + 주기 루프 제어
  - `LocationStateEmitter`: StateFlow 캐시 + SharedFlow 이벤트 발행
  - `LocationUpdateManager`: receiver/emitter 오케스트레이션
  - `LocationQuality`: 위치 품질 휴리스틱
  - `LocationCalculator`: 거리/방향/반경 계산
- `location/internal/model/`
  - `LocationStateData`: 내부 상태 스냅샷 모델

## 공개 API 설계

```kotlin
public open class LocationStateInfo(context: Context) :
    BaseSystemService(context, listOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) {

    val locationManager: LocationManager
    val sfUpdate: SharedFlow<LocationStateEvent>

    fun registerStart(
        coroutineScope: CoroutineScope,
        locationProvider: String,
        updateCycleTime: Long = DEFAULT_UPDATE_CYCLE_TIME,   // 5000L
        minDistanceM: Float = DEFAULT_UPDATE_CYCLE_DISTANCE  // 2.0f
    ): Boolean

    fun unRegister()
    override fun onDestroy()

    fun isLocationEnabled(): Boolean
    fun isGpsEnabled(): Boolean
    fun isNetworkEnabled(): Boolean
    fun isPassiveEnabled(): Boolean
    @RequiresApi(S) fun isFusedEnabled(): Boolean
    fun isAnyEnabled(): Boolean
    fun getLocation(): Location?

    fun calculateDistance(fromLocation: Location, toLocation: Location): Float
    fun calculateBearing(fromLocation: Location, toLocation: Location): Float
    fun isLocationWithRadius(fromLocation: Location, toLocation: Location, radius: Float): Boolean

    fun loadLocation(): Location?
    fun saveApplyLocation(location: Location)
    fun removeLocation()
}
```

## 공통 동작 규칙

### 1) `registerStart` 계약
- 입력 검증
  - `updateCycleTime == POLLING_DISABLED_UPDATE_CYCLE_TIME(-1)` 또는 `>= MIN_UPDATE_CYCLE_TIME(1000)` 이어야 한다.
  - `minDistanceM >= MIN_UPDATE_CYCLE_DISTANCE(0.1)` 이어야 한다.
  - 위반 시 `IllegalArgumentException` 발생.
- 실행 순서
1. `updateManager.registerReceiver()`
2. `updateManager.registerLocationListener(locationProvider, normalizedCycleTime, minDistanceM)`
3. `tryCatchSystemManager(false) { updateManager.updateStart(coroutineScope, updateCycleTime) }`
- 저전력 모드(`-1`) 처리
  - 주기 폴링은 비활성화.
  - listener `minTime`은 `DEFAULT_UPDATE_CYCLE_TIME(5000)`로 정규화되어 등록.
- 실패 처리
  - receiver 실패: `false` 반환
  - listener 실패: `updateManager.destroy()` 후 `false`
  - updateStart 실패: `updateManager.destroy()` 후 `false`

### 2) 권한 계약
- 필수 권한: `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` 동시 요구.
- `LocationStateInfo`는 `BaseSystemService` 상속으로 권한 캐시/검증을 사용.
- `getLocation`은 내부에서 `context.hasPermissions(FINE, COARSE)`를 다시 확인하고 누락 시 `null` 반환.
- provider enabled 조회 메서드(`isGpsEnabled` 등)는 권한 없이 동작 가능.

### 3) lifecycle/정리 계약
- 즉시 중지: `unRegister()` 호출 시 receiver/listener/update loop 해제.
- 완전 정리: `onDestroy()` 호출 시 `super.onDestroy()` + `updateManager.destroy()`.
- `LocationStateReceiver.updateStart`는 parent Job이 있을 경우 completion callback으로 자동 해제.
- parent Job이 없는 scope면 자동 해제 보장이 없어 수동 해제가 필요.

## 내부 헬퍼 상세 계약

### `LocationQueryHelper`
- `isLocationEnabled/isGpsEnabled/isNetworkEnabled/isPassiveEnabled/isFusedEnabled`는 `safeCatch(false)` 보호.
- `getFusedEnabledOrNull`은 API 31 미만에서 `null`.
- `isAnyEnabled`
  - API 31+: LOCATION/GPS/NETWORK/PASSIVE/FUSED 중 하나라도 활성화면 `true`
  - API 31 미만: LOCATION/GPS/NETWORK/PASSIVE 기준
- `getLocation`
1. provider 모두 비활성 시 `null` 반환 + 로그
2. 권한 미충족 시 `null` 반환 + 로그
3. `getBestLastKnownLocation()` 결과 반환
- `getBestLastKnownLocation`
  - 후보 provider 순서: FUSED(API31+, enabled) -> GPS(FINE) -> NETWORK(COARSE) -> PASSIVE
  - 각 provider의 lastKnownLocation 중 `LocationQuality.isBetter` 기준 최적값 선택

### `LocationStateReceiver`
- `registerReceiver`
  - `PROVIDERS_CHANGED_ACTION` 수신 BroadcastReceiver 등록
  - API 33+ `RECEIVER_NOT_EXPORTED` 적용
  - 이미 등록 상태면 경고 로그 후 `true`
- `registerLocationListener`
  - 이미 등록 상태면 기존 listener 해제 후 재등록(설정 즉시 반영)
  - `LocationManager.requestLocationUpdates(provider, minTime, minDistance, listener)` 호출
- `updateStart`
  - receiver 미등록 상태면 자동 등록 시도
  - 기존 루프 정리 후 새 루프 시작
  - `SupervisorJob(parentJob)` 기반 내부 scope 생성
  - `setupDataFlows()` 콜백 실행 후 폴링 루프 시작
  - `updateCycleTime == -1`이면 1회 동기화만 수행
  - 그 외 `while (isActive) { invoke(); delay(updateCycleTime) }`
- `destroy`
  - receiver 해제 + listener 해제 + update loop 중지

### `LocationStateEmitter`
- 이벤트 버퍼 정책
  - `MutableSharedFlow(replay=1, extraBufferCapacity=8, DROP_OLDEST)`
- 내부 캐시
  - `Location?`, `Gps?`, `Network?`, `Passive?`, `Fused?`를 `MutableStateFlow`로 보유
- `setupDataFlows`
  - 기존 collector 취소 후 재설정
  - 각 상태플로우를 수집해 `LocationStateEvent`로 변환 발행
  - `dropWhile`는 초기 센티널만 필터링, 이후 센티널은 발행 가능
- 동일 값 연속 업데이트는 StateFlow 특성상 중복 발행되지 않음

### `LocationUpdateManager`
- receiver와 emitter 조합을 단일 진입점으로 제공.
- `updateStart`에서 receiver 내부 scope를 가져와 emitter collector를 연결.
- `destroy`로 receiver 자원 일괄 정리.

### `LocationQuality`
- 시간 기준
  - `SIGNIFICANT_TIME_DELTA_MS = 10_000`
  - 후보가 현저히 최신이면 채택, 현저히 오래되면 거절
- 정확도 기준
  - `SIGNIFICANT_ACCURACY_DELTA_METERS = 200`
  - 더 정확하면 채택
  - 더 최신 + 크게 나쁘지 않음(+같은 provider) 조건에서 채택

### `LocationCalculator`
- `distanceTo`, `bearingTo` 직접 위임
- 반경 내 여부는 `distance <= radius`로 판정

### `LocationSharedPreference`
- 저장 키: latitude/longitude/accuracy/time/provider
- `time == 0L`이면 저장값 없음으로 간주하고 `null` 반환
- provider가 `null`이면 `null` 반환

## 이벤트/데이터 계약
- `sfUpdate`는 이벤트 스트림이며 전체 스냅샷 일관성을 보장하지 않는다.
- 지표 간 순서 보장은 없다.
- 버퍼 정책상 이벤트 드롭 가능성이 있다.
- 초기 유효값 도달 전에는 일부 이벤트가 발생하지 않을 수 있다.

## 오류 처리/로그 정책
- 공개 진입점: `tryCatchSystemManager`로 권한/예외를 기본값으로 전환.
- 내부 구현: `safeCatch`로 시스템 예외 보호.
- 중요 실패(등록 실패, provider 비활성 등)는 `Logx`로 로그 남김.

## 테스트 매핑
- 상수/이벤트/Emitter(Unit)
  - `LocationStateConstantsUnitTest`
  - `LocationStateEventUnitTest`
  - `LocationStateEmitterUnitTest`
- 공개 API/파라미터 검증(Robolectric)
  - `LocationStateInfoRobolectricTest`
- 내부 헬퍼(Robolectric)
  - `LocationQueryHelperRobolectricTest`
  - `LocationQualityRobolectricTest`
  - `LocationCalculatorRobolectricTest`
  - `LocationUpdateManagerRobolectricTest`
  - `LocationStateReceiverRobolectricTest` (helper/internal 경로 모두)
- 저장소(Robolectric)
  - `LocationSharedPreferenceTest`
