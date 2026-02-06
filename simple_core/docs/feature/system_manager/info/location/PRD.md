# Location Info PRD

## 문서 정보
- 문서명: Location Info PRD
- 작성일: 2026-02-06
- 수정일: 2026-02-06
- 대상 모듈: simple_core
- 패키지: kr.open.library.simple_ui.core.system_manager.info.location
- 상태: 현행(as-is)

## 배경/문제 정의
- Android 위치 기능은 `LocationManager`, `LocationListener`, `BroadcastReceiver`, 권한 처리, SDK 분기 로직이 결합되어 구현 복잡도가 높습니다.
- 위치 제공자(GPS/NETWORK/PASSIVE/FUSED) 상태와 최근 위치를 함께 안정적으로 추적하려면 이벤트 수신 + 주기 점검이 동시에 필요합니다.
- 호출부에서 매번 권한/예외/기본값 처리까지 구현하면 중복 코드와 유지보수 비용이 빠르게 증가합니다.
- 위치 데이터는 정확도/시간/제공자에 따라 품질 편차가 크므로, 단순 최근값 반환만으로는 품질 보장이 어렵습니다.

## 목표
- 위치 상태 수집/조회/유틸 기능을 `LocationStateInfo` 단일 API로 제공한다.
- 위치 변경 이벤트와 제공자 상태 변경 이벤트를 `SharedFlow` 기반으로 일관되게 발행한다.
- 예외 상황에서 기본값 반환 + `Logx` 로깅으로 앱 크래시를 방지한다.
- 내부 품질 판단(`LocationQuality`)으로 최적의 최근 위치를 선택한다.

## 비목표
- 위치 권한 요청 UI/UX 제공 (권한 요청 오케스트레이션은 상위 레이어 책임)
- 경로 추적, 이동 분석, 지오펜싱 같은 고수준 도메인 기능 제공
- 지도/시각화 UI 컴포넌트 제공
- Google FusedLocationProviderClient 대체 구현 제공

## 범위

### 포함 범위
- `LocationStateInfo` 공개 API
  - 수집 시작/해제/정리: `registerStart`, `unRegister`, `onDestroy`
  - 상태 조회: provider enabled 여부, `getLocation`, 거리/방향/반경 계산
  - 저장소 연동: `saveApplyLocation`, `loadLocation`, `removeLocation`
- 내부 헬퍼 동작
  - `LocationQueryHelper`: provider 상태 조회/최적 위치 조회
  - `LocationStateReceiver`: 리스너/리시버 등록과 폴링 루프
  - `LocationStateEmitter`: 상태 캐시(StateFlow) + 이벤트 발행(SharedFlow)
  - `LocationUpdateManager`: 수신/발행 오케스트레이션
  - `LocationQuality`, `LocationCalculator`
- 이벤트 모델/상수
  - `LocationStateEvent`, `LocationStateConstants`, `LocationStateData`

### 제외 범위
- UI 계층(Activity/Fragment/Compose) 코드
- 권한 요청 화면 및 사용자 안내 UX
- 장기 이력 저장/서버 전송
- 백그라운드 위치 정책 최적화(Doze, 제조사 정책 대응 등)

## 핵심 기능

### 수집/이벤트
- `registerStart(coroutineScope, locationProvider, updateCycleTime, minDistanceM)` 호출로 추적 시작
- `sfUpdate`를 통해 5종 이벤트 발행
  - `OnLocationChanged`
  - `OnGpsEnabled`
  - `OnNetworkEnabled`
  - `OnPassiveEnabled`
  - `OnFusedEnabled`

### 조회 API
- 제공자 상태 조회: `isLocationEnabled`, `isGpsEnabled`, `isNetworkEnabled`, `isPassiveEnabled`, `isFusedEnabled`, `isAnyEnabled`
- 최근 위치 조회: `getLocation`(다중 provider 중 품질 우선 선택)
- 계산 유틸: `calculateDistance`, `calculateBearing`, `isLocationWithRadius`

### 저장/복원
- 위치 스냅샷을 `SharedPreferences`에 저장/복원/삭제
- 저장 항목: 위도/경도/정확도/시간/provider

## 예외 처리 정책
- **개발자 실수**
  - `updateCycleTime`이 `POLLING_DISABLED_UPDATE_CYCLE_TIME(-1)`가 아니면서 `MIN_UPDATE_CYCLE_TIME(1000)` 미만이면 `IllegalArgumentException`
  - `minDistanceM < MIN_UPDATE_CYCLE_DISTANCE(0.1)`이면 `IllegalArgumentException`
- **권한/환경 이슈**
  - 필수 권한(`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`) 미충족 시 기본값(`false`/`null`) 반환 + 로그
  - provider 비활성/미지원 시 `null` 반환 가능
- **실행 예외**
  - `safeCatch`, `tryCatchSystemManager`로 예외를 기본값으로 전환하고 `Logx` 기록

## 비기능 요구사항
- **안정성**: 리소스 정리(`unRegister`/`onDestroy`)와 parent Job completion 연동으로 누수 위험을 낮춘다.
- **성능/배터리**:
  - 기본 폴링 주기 `5000ms`
  - 저전력 모드(`POLLING_DISABLED_UPDATE_CYCLE_TIME`) 지원
  - 최소 거리 하한 `0.1m`, 기본값 `2.0m`
- **호환성**:
  - minSdk 28
  - API 31+에서 FUSED provider 상태 조회 지원
  - API 33+에서 `RECEIVER_NOT_EXPORTED` 사용
- **관측성**: `Logx` 기반 경고/오류 로그 일관화

## 제약/전제
- 권한 모델은 현행 구현 기준으로 `FINE + COARSE` 동시 요구를 전제로 한다.
- `sfUpdate`는 스냅샷이 아닌 이벤트 스트림이며 지표 간 전역 순서를 보장하지 않는다.
- SharedFlow 버퍼 정책(`DROP_OLDEST`)에 따라 고빈도 상황에서 일부 이벤트 드롭 가능.
- 저전력 모드(`-1`)는 주기 폴링만 비활성화하며 listener 등록 자체는 유지된다.
- `CoroutineScope`에 Job이 없으면 자동 정리가 보장되지 않아 수동 해제가 필요하다.

## 성공 기준
- 단일 API로 위치 수집/조회/계산/저장을 수행할 수 있다.
- 잘못된 파라미터는 예외로 즉시 드러나고, 환경 예외는 기본값으로 안전하게 처리된다.
- 위치/제공자 이벤트가 `sfUpdate`로 일관 발행된다.
- 주요 흐름(파라미터 검증, 리시버 등록, 저전력 모드, 품질 판단, emitter 동작)이 테스트로 검증된다.

## 관련 파일
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/location/LocationStateInfo.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/location/LocationStateConstants.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/location/LocationStateEvent.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/location/LocationSharedPreference.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/location/internal/helper/LocationQueryHelper.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/location/internal/helper/LocationStateReceiver.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/location/internal/helper/LocationStateEmitter.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/location/internal/helper/LocationUpdateManager.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/location/internal/helper/LocationQuality.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/location/internal/helper/LocationCalculator.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/location/internal/model/LocationStateData.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/extensions/SystemServiceExtensions.kt`

## 테스트
- Unit
  - `simple_core/src/test/java/kr/open/library/simple_ui/core/unit/system_manager/info/location/LocationStateConstantsUnitTest.kt`
  - `simple_core/src/test/java/kr/open/library/simple_ui/core/unit/system_manager/info/location/LocationStateEventUnitTest.kt`
  - `simple_core/src/test/java/kr/open/library/simple_ui/core/unit/system_manager/info/location/internal/LocationStateEmitterUnitTest.kt`
- Robolectric
  - `simple_core/src/test/java/kr/open/library/simple_ui/core/robolectric/system_manager/info/location/LocationStateInfoRobolectricTest.kt`
  - `simple_core/src/test/java/kr/open/library/simple_ui/core/robolectric/system_manager/info/location/LocationSharedPreferenceTest.kt`
  - `simple_core/src/test/java/kr/open/library/simple_ui/core/robolectric/system_manager/info/location/internal/LocationStateReceiverRobolectricTest.kt`
  - `simple_core/src/test/java/kr/open/library/simple_ui/core/robolectric/system_manager/info/location/internal/helper/LocationQueryHelperRobolectricTest.kt`
  - `simple_core/src/test/java/kr/open/library/simple_ui/core/robolectric/system_manager/info/location/internal/helper/LocationQualityRobolectricTest.kt`
  - `simple_core/src/test/java/kr/open/library/simple_ui/core/robolectric/system_manager/info/location/internal/helper/LocationCalculatorRobolectricTest.kt`
  - `simple_core/src/test/java/kr/open/library/simple_ui/core/robolectric/system_manager/info/location/internal/helper/LocationUpdateManagerRobolectricTest.kt`
  - `simple_core/src/test/java/kr/open/library/simple_ui/core/robolectric/system_manager/info/location/internal/helper/LocationStateReceiverRobolectricTest.kt`
